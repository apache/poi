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

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import org.apache.poi.util.Beta;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.presentationml.x2006.main.CTConnector;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPicture;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;


@Beta
public class XSLFDrawing {
    private XSLFSheet _sheet;
    private CTGroupShape _spTree;

    /*package*/ XSLFDrawing(XSLFSheet sheet, CTGroupShape spTree){
        _sheet = sheet;
        _spTree = spTree;
        XmlObject[] cNvPr = sheet.getSpTree().selectPath(
                "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' .//*/p:cNvPr");
        for(XmlObject o : cNvPr) {
            // powerpoint generates AlternateContent elements which cNvPr elements aren't recognized
            // ignore them for now
            if (o instanceof CTNonVisualDrawingProps) {
                CTNonVisualDrawingProps p = (CTNonVisualDrawingProps)o;
                sheet.registerShapeId((int)p.getId());
            }
        }
    }

    public XSLFAutoShape createAutoShape(){
        CTShape sp = _spTree.addNewSp();
        sp.set(XSLFAutoShape.prototype(_sheet.allocateShapeId()));
        XSLFAutoShape shape = new XSLFAutoShape(sp, _sheet);
        shape.setAnchor(new Rectangle2D.Double());
        return shape;
    }

    public XSLFFreeformShape createFreeform(){
        CTShape sp = _spTree.addNewSp();
        sp.set(XSLFFreeformShape.prototype(_sheet.allocateShapeId()));
        XSLFFreeformShape shape = new XSLFFreeformShape(sp, _sheet);
        shape.setAnchor(new Rectangle2D.Double());
        return shape;
    }

    public XSLFTextBox createTextBox(){
        CTShape sp = _spTree.addNewSp();
        sp.set(XSLFTextBox.prototype(_sheet.allocateShapeId()));
        XSLFTextBox shape = new XSLFTextBox(sp, _sheet);
        shape.setAnchor(new Rectangle2D.Double());
        return shape;
    }

    public XSLFConnectorShape createConnector(){
        CTConnector sp = _spTree.addNewCxnSp();
        sp.set(XSLFConnectorShape.prototype(_sheet.allocateShapeId()));
        XSLFConnectorShape shape = new XSLFConnectorShape(sp, _sheet);
        shape.setAnchor(new Rectangle2D.Double());
        shape.setLineColor(Color.black);
        shape.setLineWidth(0.75);
        return shape;
    }

    public XSLFGroupShape createGroup(){
        CTGroupShape sp = _spTree.addNewGrpSp();
        sp.set(XSLFGroupShape.prototype(_sheet.allocateShapeId()));
        XSLFGroupShape shape = new XSLFGroupShape(sp, _sheet);
        shape.setAnchor(new Rectangle2D.Double());
        return shape;
    }

    public XSLFPictureShape createPicture(String rel){
        CTPicture sp = _spTree.addNewPic();
        sp.set(XSLFPictureShape.prototype(_sheet.allocateShapeId(), rel));
        XSLFPictureShape shape = new XSLFPictureShape(sp, _sheet);
        shape.setAnchor(new Rectangle2D.Double());
        return shape;
    }

    public XSLFTable createTable(){
        CTGraphicalObjectFrame sp = _spTree.addNewGraphicFrame();
        sp.set(XSLFTable.prototype(_sheet.allocateShapeId()));
        XSLFTable shape = new XSLFTable(sp, _sheet);
        shape.setAnchor(new Rectangle2D.Double());
        return shape;
    }

    public XSLFObjectShape createOleShape(String pictureRel) {
        CTGraphicalObjectFrame sp = _spTree.addNewGraphicFrame();
        sp.set(XSLFObjectShape.prototype(_sheet.allocateShapeId(), pictureRel));
        XSLFObjectShape shape = new XSLFObjectShape(sp, _sheet);
        shape.setAnchor(new Rectangle2D.Double());
        return shape;
    }
}
