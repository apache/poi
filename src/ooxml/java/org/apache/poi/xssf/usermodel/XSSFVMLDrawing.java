/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.xssf.usermodel;

import static org.apache.poi.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.DocumentHelper;
import org.apache.poi.util.ReplacingInputStream;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.microsoft.schemas.office.excel.CTClientData;
import com.microsoft.schemas.office.excel.STObjectType;
import com.microsoft.schemas.office.office.CTIdMap;
import com.microsoft.schemas.office.office.CTShapeLayout;
import com.microsoft.schemas.office.office.STConnectType;
import com.microsoft.schemas.office.office.STInsetMode;
import com.microsoft.schemas.vml.CTPath;
import com.microsoft.schemas.vml.CTShadow;
import com.microsoft.schemas.vml.CTShape;
import com.microsoft.schemas.vml.CTShapetype;
import com.microsoft.schemas.vml.STExt;
import com.microsoft.schemas.vml.STStrokeJoinStyle;
import com.microsoft.schemas.vml.STTrueFalse;

/**
 * Represents a SpreadsheetML VML drawing.
 *
 * <p>
 * In Excel 2007 VML drawings are used to describe properties of cell comments,
 * although the spec says that VML is deprecated:
 * </p>
 * <p>
 * The VML format is a legacy format originally introduced with Office 2000 and is included and fully defined
 * in this Standard for backwards compatibility reasons. The DrawingML format is a newer and richer format
 * created with the goal of eventually replacing any uses of VML in the Office Open XML formats. VML should be
 * considered a deprecated format included in Office Open XML for legacy reasons only and new applications that
 * need a file format for drawings are strongly encouraged to use preferentially DrawingML
 * </p>
 * 
 * <p>
 * Warning - Excel is known to put invalid XML into these files!
 *  For example, &gt;br&lt; without being closed or escaped crops up.
 * </p>
 *
 * See 6.4 VML - SpreadsheetML Drawing in Office Open XML Part 4 - Markup Language Reference.pdf
 *
 * @author Yegor Kozlov
 */
public final class XSSFVMLDrawing extends POIXMLDocumentPart {
    private static final QName QNAME_SHAPE_LAYOUT = new QName("urn:schemas-microsoft-com:office:office", "shapelayout");
    private static final QName QNAME_SHAPE_TYPE = new QName("urn:schemas-microsoft-com:vml", "shapetype");
    private static final QName QNAME_SHAPE = new QName("urn:schemas-microsoft-com:vml", "shape");
    private static final String COMMENT_SHAPE_TYPE_ID = "_x0000_t202"; // this ID value seems to have significance to Excel >= 2010; see https://issues.apache.org/bugzilla/show_bug.cgi?id=55409

    /**
     * regexp to parse shape ids, in VML they have weird form of id="_x0000_s1026"
     */
    private static final Pattern ptrn_shapeId = Pattern.compile("_x0000_s(\\d+)");

    private List<QName> _qnames = new ArrayList<QName>();
    private List<XmlObject> _items = new ArrayList<XmlObject>();
    private String _shapeTypeId;
    private int _shapeId = 1024;

    /**
     * Create a new SpreadsheetML drawing
     *
     * @see XSSFSheet#createDrawingPatriarch()
     */
    protected XSSFVMLDrawing() {
        super();
        newDrawing();
    }

    /**
     * Construct a SpreadsheetML drawing from a package part
     *
     * @param part the package part holding the drawing data,
     * the content type must be <code>application/vnd.openxmlformats-officedocument.drawing+xml</code>
     * 
     * @since POI 3.14-Beta1
     */
    protected XSSFVMLDrawing(PackagePart part) throws IOException, XmlException {
        super(part);
        read(getPackagePart().getInputStream());
    }

    protected void read(InputStream is) throws IOException, XmlException {
        Document doc;
        try {
            /*
             * This is a seriously sick fix for the fact that some .xlsx files contain raw bits
             * of HTML, without being escaped or properly turned into XML.
             * The result is that they contain things like &gt;br&lt;, which breaks the XML parsing.
             * This very sick InputStream wrapper attempts to spot these go past, and fix them.
             */
            doc = DocumentHelper.readDocument(new ReplacingInputStream(is, "<br>", "<br/>"));
        } catch (SAXException e) {
            throw new XmlException(e.getMessage(), e);
        }
        XmlObject root = XmlObject.Factory.parse(doc, DEFAULT_XML_OPTIONS);

        _qnames = new ArrayList<QName>();
        _items = new ArrayList<XmlObject>();
        for(XmlObject obj : root.selectPath("$this/xml/*")) {
            Node nd = obj.getDomNode();
            QName qname = new QName(nd.getNamespaceURI(), nd.getLocalName());
            if (qname.equals(QNAME_SHAPE_LAYOUT)) {
                _items.add(CTShapeLayout.Factory.parse(obj.xmlText(), DEFAULT_XML_OPTIONS));
            } else if (qname.equals(QNAME_SHAPE_TYPE)) {
                CTShapetype st = CTShapetype.Factory.parse(obj.xmlText(), DEFAULT_XML_OPTIONS);
                _items.add(st);
                _shapeTypeId = st.getId();
            } else if (qname.equals(QNAME_SHAPE)) {
                CTShape shape = CTShape.Factory.parse(obj.xmlText(), DEFAULT_XML_OPTIONS);
                String id = shape.getId();
                if(id != null) {
                    Matcher m = ptrn_shapeId.matcher(id);
                    if(m.find()) {
                        _shapeId = Math.max(_shapeId, Integer.parseInt(m.group(1)));
                    }
                }
                _items.add(shape);
            } else {
                Document doc2;
                try {
                    InputSource is2 = new InputSource(new StringReader(obj.xmlText()));
                    doc2 = DocumentHelper.readDocument(is2);
                } catch (SAXException e) {
                    throw new XmlException(e.getMessage(), e);
                }
                
                _items.add(XmlObject.Factory.parse(doc2, DEFAULT_XML_OPTIONS));
            }
            _qnames.add(qname);
        }
    }

    protected List<XmlObject> getItems(){
        return _items;
    }

    protected void write(OutputStream out) throws IOException {
        XmlObject rootObject = XmlObject.Factory.newInstance();
        XmlCursor rootCursor = rootObject.newCursor();
        rootCursor.toNextToken();
        rootCursor.beginElement("xml");

        for(int i=0; i < _items.size(); i++){
            XmlCursor xc = _items.get(i).newCursor();
            rootCursor.beginElement(_qnames.get(i));
            while(xc.toNextToken() == XmlCursor.TokenType.ATTR) {
                Node anode = xc.getDomNode();
                rootCursor.insertAttributeWithValue(anode.getLocalName(), anode.getNamespaceURI(), anode.getNodeValue());
            }
            xc.toStartDoc();
            xc.copyXmlContents(rootCursor);
            rootCursor.toNextToken();
            xc.dispose();
        }
        rootCursor.dispose();

        rootObject.save(out, DEFAULT_XML_OPTIONS);
    }

    @Override
    protected void commit() throws IOException {
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        write(out);
        out.close();
    }

    /**
     * Initialize a new Speadsheet VML drawing
     */
    private void newDrawing(){
        CTShapeLayout layout = CTShapeLayout.Factory.newInstance();
        layout.setExt(STExt.EDIT);
        CTIdMap idmap = layout.addNewIdmap();
        idmap.setExt(STExt.EDIT);
        idmap.setData("1");
        _items.add(layout);
        _qnames.add(QNAME_SHAPE_LAYOUT);

        CTShapetype shapetype = CTShapetype.Factory.newInstance();
        _shapeTypeId = COMMENT_SHAPE_TYPE_ID;
        shapetype.setId(_shapeTypeId);
        shapetype.setCoordsize("21600,21600");
        shapetype.setSpt(202);
        shapetype.setPath2("m,l,21600r21600,l21600,xe");
        shapetype.addNewStroke().setJoinstyle(STStrokeJoinStyle.MITER);
        CTPath path = shapetype.addNewPath();
        path.setGradientshapeok(STTrueFalse.T);
        path.setConnecttype(STConnectType.RECT);
        _items.add(shapetype);
        _qnames.add(QNAME_SHAPE_TYPE);
    }

    protected CTShape newCommentShape(){
        CTShape shape = CTShape.Factory.newInstance();
        shape.setId("_x0000_s" + (++_shapeId));
        shape.setType("#" + _shapeTypeId);
        shape.setStyle("position:absolute; visibility:hidden");
        shape.setFillcolor("#ffffe1");
        shape.setInsetmode(STInsetMode.AUTO);
        shape.addNewFill().setColor("#ffffe1");
        CTShadow shadow = shape.addNewShadow();
        shadow.setOn(STTrueFalse.T);
        shadow.setColor("black");
        shadow.setObscured(STTrueFalse.T);
        shape.addNewPath().setConnecttype(STConnectType.NONE);
        shape.addNewTextbox().setStyle("mso-direction-alt:auto");
        CTClientData cldata = shape.addNewClientData();
        cldata.setObjectType(STObjectType.NOTE);
        cldata.addNewMoveWithCells();
        cldata.addNewSizeWithCells();
        cldata.addNewAnchor().setStringValue("1, 15, 0, 2, 3, 15, 3, 16");
        cldata.addNewAutoFill().setStringValue("False");
        cldata.addNewRow().setBigIntegerValue(new BigInteger("0"));
        cldata.addNewColumn().setBigIntegerValue(new BigInteger("0"));
        _items.add(shape);
        _qnames.add(QNAME_SHAPE);
        return shape;
    }

    /**
     * Find a shape with ClientData of type "NOTE" and the specified row and column
     *
     * @return the comment shape or <code>null</code>
     */
    protected CTShape findCommentShape(int row, int col){
        for(XmlObject itm : _items){
            if(itm instanceof CTShape){
                CTShape sh = (CTShape)itm;
                if(sh.sizeOfClientDataArray() > 0){
                    CTClientData cldata = sh.getClientDataArray(0);
                    if(cldata.getObjectType() == STObjectType.NOTE){
                        int crow = cldata.getRowArray(0).intValue();
                        int ccol = cldata.getColumnArray(0).intValue();
                        if(crow == row && ccol == col) {
                            return sh;
                        }
                    }
                }
            }
        }
        return null;
    }

    protected boolean removeCommentShape(int row, int col){
        CTShape shape = findCommentShape(row, col);
        return shape != null && _items.remove(shape);
    }
}