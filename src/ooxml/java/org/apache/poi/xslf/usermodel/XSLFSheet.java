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
package org.apache.poi.xslf.usermodel;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.util.Beta;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.officeDocument.x2006.relationships.STRelationshipId;
import org.openxmlformats.schemas.presentationml.x2006.main.CTConnector;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPicture;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Beta
public abstract class XSLFSheet extends POIXMLDocumentPart {
    private XSLFDrawing _drawing;
    private List<XSLFShape> _shapes;
    private CTGroupShape _spTree;

    public XSLFSheet(){
        super();
    }

    public XSLFSheet(PackagePart part, PackageRelationship rel){
        super(part, rel);
    }

	public XMLSlideShow getSlideShow() {
		return (XMLSlideShow)getParent();
	}

    protected List<XSLFShape> buildShapes(CTGroupShape spTree){
        List<XSLFShape> shapes = new ArrayList<XSLFShape>();
        for(XmlObject ch : spTree.selectPath("*")){
            if(ch instanceof CTShape){ // simple shape
                XSLFAutoShape shape = XSLFAutoShape.create((CTShape)ch, this);
                shapes.add(shape);
            } else if (ch instanceof CTGroupShape){
                shapes.add(new XSLFGroupShape((CTGroupShape)ch, this));
            } else if (ch instanceof CTConnector){
                shapes.add(new XSLFConnectorShape((CTConnector)ch, this));
            } else if (ch instanceof CTPicture){
                shapes.add(new XSLFPictureShape((CTPicture)ch, this));
            } else if (ch instanceof CTGraphicalObjectFrame){
                XSLFGraphicFrame shape = XSLFGraphicFrame.create((CTGraphicalObjectFrame)ch, this);
                shapes.add(shape);
            }
        }
        return shapes;
    }

    public abstract XmlObject getXmlObject();


    private XSLFDrawing getDrawing(){
        if(_drawing == null) {
            _drawing = new XSLFDrawing(this, getSpTree());
        }
        return _drawing;
    }

    private List<XSLFShape> getShapeList(){
        if(_shapes == null){
            _shapes = buildShapes(getSpTree());
        }
        return _shapes;
    }

    // shape factory methods

    public XSLFAutoShape createAutoShape(){
        List<XSLFShape> shapes = getShapeList();
        XSLFAutoShape sh = getDrawing().createAutoShape();
        shapes.add(sh);
        return sh;
    }

    public XSLFFreeformShape createFreeform(){
        List<XSLFShape> shapes = getShapeList();
        XSLFFreeformShape sh = getDrawing().createFreeform();
        shapes.add(sh);
        return sh;
    }

    public XSLFTextBox createTextBox(){
        List<XSLFShape> shapes = getShapeList();
        XSLFTextBox sh = getDrawing().createTextBox();
        shapes.add(sh);
        return sh;
    }

    public XSLFConnectorShape createConnector(){
        List<XSLFShape> shapes = getShapeList();
        XSLFConnectorShape sh = getDrawing().createConnector();
        shapes.add(sh);
        return sh;
    }

    public XSLFGroupShape createGroup(){
        List<XSLFShape> shapes = getShapeList();
        XSLFGroupShape sh = getDrawing().createGroup();
        shapes.add(sh);
        return sh;
    }

    public XSLFPictureShape createPicture(int pictureIndex){
        List<PackagePart>  pics = getPackagePart().getPackage()
                .getPartsByName(Pattern.compile("/ppt/media/.*?"));

        PackagePart pic = pics.get(pictureIndex);

        PackageRelationship rel = getPackagePart().addRelationship(
                pic.getPartName(), TargetMode.INTERNAL, XSLFRelation.IMAGES.getRelation());
        addRelation(rel.getId(), new XSLFPictureData(pic, rel));

        XSLFPictureShape sh = getDrawing().createPicture(rel.getId());
        sh.resize();

        getShapeList().add(sh);
        return sh;
    }

    public XSLFTable createTable(){
        List<XSLFShape> shapes = getShapeList();
        XSLFTable sh = getDrawing().createTable();
        shapes.add(sh);
        return sh;
    }

    public XSLFShape[] getShapes(){
        return getShapeList().toArray(new XSLFShape[_shapes.size()]);
    }

    public boolean removeShape(XSLFShape xShape) {
        XmlObject obj = xShape.getXmlObject();
        CTGroupShape spTree = getSpTree();
        if(obj instanceof CTShape){
            spTree.getSpList().remove(obj);
        } else if (obj instanceof CTGroupShape){
            spTree.getGrpSpList().remove(obj);
        } else if (obj instanceof CTConnector){
            spTree.getCxnSpList().remove(obj);
        } else {
            throw new IllegalArgumentException("Unsupported shape: " + xShape);
        }
        return getShapeList().remove(xShape);
    }

    protected abstract String getRootElementName();

    protected CTGroupShape getSpTree(){
        if(_spTree == null) {
            XmlObject root = getXmlObject();
            XmlObject[] sp = root.selectPath(
                    "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' .//*/p:spTree");
            if(sp.length == 0) throw new IllegalStateException("CTGroupShape was not found");
            _spTree = (CTGroupShape)sp[0];
        }
        return _spTree;
    }

    protected final void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);

        Map<String, String> map = new HashMap<String, String>();
        map.put(STRelationshipId.type.getName().getNamespaceURI(), "r");
        map.put("http://schemas.openxmlformats.org/drawingml/2006/main", "a");
        map.put("http://schemas.openxmlformats.org/presentationml/2006/main", "p");
        xmlOptions.setSaveSuggestedPrefixes(map);
        String docName = getRootElementName();
        if(docName != null) {
            xmlOptions.setSaveSyntheticDocumentElement(
                    new QName("http://schemas.openxmlformats.org/presentationml/2006/main", docName));
        }

        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        getXmlObject().save(out, xmlOptions);
        out.close();
    }

    /**
     * Set the contents of this sheet to be a copy of the source sheet.
     *
     * @param src the source sheet to copy data from
     */
    public void copy(XSLFSheet src){
        _shapes = null;
        _spTree = null;
        _drawing = null;
        getXmlObject().set(src.getXmlObject());
    }

}