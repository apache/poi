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

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;
import static org.apache.poi.xssf.usermodel.XSSFRelation.NS_SPREADSHEETML;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import com.microsoft.schemas.office.excel.CTClientData;
import com.microsoft.schemas.office.excel.STObjectType;
import com.microsoft.schemas.office.office.CTIdMap;
import com.microsoft.schemas.office.office.CTShapeLayout;
import com.microsoft.schemas.office.office.STConnectType;
import com.microsoft.schemas.office.office.STInsetMode;
import com.microsoft.schemas.office.office.ShapelayoutDocument;
import com.microsoft.schemas.vml.CTGroup;
import com.microsoft.schemas.vml.CTPath;
import com.microsoft.schemas.vml.CTShadow;
import com.microsoft.schemas.vml.CTShape;
import com.microsoft.schemas.vml.CTShapetype;
import com.microsoft.schemas.vml.STExt;
import com.microsoft.schemas.vml.STStrokeJoinStyle;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.schemas.vmldrawing.XmlDocument;
import org.apache.poi.util.Internal;
import org.apache.poi.util.ReplacingInputStream;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STTrueFalse;

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
 */
public final class XSSFVMLDrawing extends POIXMLDocumentPart {
    // this ID value seems to have significance to Excel >= 2010;
    // see https://issues.apache.org/bugzilla/show_bug.cgi?id=55409
    private static final String COMMENT_SHAPE_TYPE_ID = "_x0000_t202";

    /**
     * to actually process the namespace-less vmldrawing, we've introduced a proxy namespace.
     * this namespace is active in-memory, but will be removed on saving to the file
     */
    public static final QName QNAME_VMLDRAWING = new QName("urn:schemas-poi-apache-org:vmldrawing", "xml");

    /**
     * regexp to parse shape ids, in VML they have weird form of id="_x0000_s1026"
     */
    private static final Pattern ptrn_shapeId = Pattern.compile("_x0000_s(\\d+)");

    private XmlDocument root;
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
        try (InputStream stream = getPackagePart().getInputStream()) {
            read(stream);
        }
    }

    public XmlDocument getDocument() {
        return root;
    }


    protected void read(InputStream is) throws IOException, XmlException {
        XmlOptions xopt = new XmlOptions(DEFAULT_XML_OPTIONS);
        xopt.setLoadSubstituteNamespaces(Collections.singletonMap("", QNAME_VMLDRAWING.getNamespaceURI()));
        xopt.setDocumentType(XmlDocument.type);

        /*
         * This is a seriously sick fix for the fact that some .xlsx files contain raw bits
         * of HTML, without being escaped or properly turned into XML.
         * The result is that they contain things like &gt;br&lt;, which breaks the XML parsing.
         * This very sick InputStream wrapper attempts to spot these go past, and fix them.
         *
         * Furthermore some documents contain a default namespace of
         * http://schemas.openxmlformats.org/spreadsheetml/2006/main for the namespace-less "xml" document type.
         * this definition is wrong and removed.
         */
        root = XmlDocument.Factory.parse(
            new ReplacingInputStream(
            new ReplacingInputStream(is, "<br>", "<br/>"),
            " xmlns=\""+NS_SPREADSHEETML+"\"", "")
            , xopt);

        try (XmlCursor cur = root.getXml().newCursor()) {
            for (boolean found = cur.toFirstChild(); found; found = cur.toNextSibling()) {
                XmlObject xo = cur.getObject();
                if (xo instanceof CTShapetype) {
                    _shapeTypeId = ((CTShapetype)xo).getId();
                } else if (xo instanceof CTShape) {
                    CTShape shape = (CTShape)xo;
                    String id = shape.getId();
                    if(id != null) {
                        Matcher m = ptrn_shapeId.matcher(id);
                        if(m.find()) {
                            _shapeId = Math.max(_shapeId, Integer.parseInt(m.group(1)));
                        }
                    }
                }
            }
        }
    }

    protected List<XmlObject> getItems(){
        List<XmlObject> items = new ArrayList<>();

        try (XmlCursor cur = root.getXml().newCursor()) {
            for (boolean found = cur.toFirstChild(); found; found = cur.toNextSibling()) {
                items.add(cur.getObject());
            }
        }

        return items;
    }

    protected void write(OutputStream out) throws IOException {
        XmlOptions xopt = new XmlOptions(DEFAULT_XML_OPTIONS);
        xopt.setSaveImplicitNamespaces(Collections.singletonMap("", QNAME_VMLDRAWING.getNamespaceURI()));
        root.save(out, xopt);
    }

    @Override
    protected void commit() throws IOException {
        PackagePart part = getPackagePart();
        try (OutputStream out = part.getOutputStream()) {
            write(out);
        }
    }

    /**
     * Initialize a new Spreadsheet VML drawing
     */
    private void newDrawing(){
        root = XmlDocument.Factory.newInstance();
        try (final XmlCursor xml = root.addNewXml().newCursor()) {
            ShapelayoutDocument layDoc = ShapelayoutDocument.Factory.newInstance();
            CTShapeLayout layout = layDoc.addNewShapelayout();
            layout.setExt(STExt.EDIT);
            CTIdMap idmap = layout.addNewIdmap();
            idmap.setExt(STExt.EDIT);
            idmap.setData("1");

            xml.toEndToken();
            try (XmlCursor layCur = layDoc.newCursor()) {
                layCur.copyXmlContents(xml);
            }

            CTGroup grp = CTGroup.Factory.newInstance();
            CTShapetype shapetype = grp.addNewShapetype();
            _shapeTypeId = COMMENT_SHAPE_TYPE_ID;
            shapetype.setId(_shapeTypeId);
            shapetype.setCoordsize("21600,21600");
            shapetype.setSpt(202);
            shapetype.setPath2("m,l,21600r21600,l21600,xe");
            shapetype.addNewStroke().setJoinstyle(STStrokeJoinStyle.MITER);
            CTPath path = shapetype.addNewPath();
            path.setGradientshapeok(STTrueFalse.T);
            path.setConnecttype(STConnectType.RECT);

            xml.toEndToken();
            try (XmlCursor grpCur = grp.newCursor()) {
                grpCur.copyXmlContents(xml);
            }
        }
    }

    /**
     * This method is for internal POI use only.
     */
    @Internal
    public CTShape newCommentShape() {
        CTGroup grp = CTGroup.Factory.newInstance();

        CTShape shape = grp.addNewShape();
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
        cldata.addNewRow().setBigIntegerValue(BigInteger.valueOf(0));
        cldata.addNewColumn().setBigIntegerValue(BigInteger.valueOf(0));

        try (final XmlCursor xml = root.getXml().newCursor()){
            xml.toEndToken();
            try (final XmlCursor grpCur = grp.newCursor()){
                grpCur.copyXmlContents(xml);
                xml.toPrevSibling();
                shape = (CTShape)xml.getObject();
            }
        }

        return shape;
    }

    /**
     * Find a shape with ClientData of type "NOTE" and the specified row and column
     *
     * @return the comment shape or <code>null</code>
     */
    public CTShape findCommentShape(int row, int col){
        try (final XmlCursor cur = root.getXml().newCursor()){
            for (boolean found = cur.toFirstChild(); found; found = cur.toNextSibling()) {
                XmlObject itm = cur.getObject();
                if (matchCommentShape(itm, row, col)) {
                    return (CTShape)itm;
                }
            }
        }
        return null;
    }

    private boolean matchCommentShape(XmlObject itm, int row, int col) {
        if (!(itm instanceof CTShape)) {
            return false;
        }

        CTShape sh = (CTShape)itm;
        if (sh.sizeOfClientDataArray() == 0) {
            return false;
        }

        CTClientData cldata = sh.getClientDataArray(0);
        if(cldata.getObjectType() != STObjectType.NOTE) {
            return false;
        }

        int crow = cldata.getRowArray(0).intValue();
        int ccol = cldata.getColumnArray(0).intValue();
        return (crow == row && ccol == col);
    }

    protected boolean removeCommentShape(int row, int col){
        try (final XmlCursor cur = root.getXml().newCursor()) {
            for (boolean found = cur.toFirstChild(); found; found = cur.toNextSibling()) {
                XmlObject itm = cur.getObject();
                if (matchCommentShape(itm, row, col)) {
                    cur.removeXml();
                    return true;
                }
            }
        }
        return false;
    }
}