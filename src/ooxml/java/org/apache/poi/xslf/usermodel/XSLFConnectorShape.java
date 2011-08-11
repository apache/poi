/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xslf.usermodel;

import org.apache.poi.util.Beta;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineEndProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndType;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndWidth;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndLength;
import org.openxmlformats.schemas.presentationml.x2006.main.CTConnector;
import org.openxmlformats.schemas.presentationml.x2006.main.CTConnectorNonVisual;

/**
 *
 * Specifies a connection shape. 
 *
 * @author Yegor Kozlov
 */
@Beta
public class XSLFConnectorShape extends XSLFSimpleShape {

    /*package*/ XSLFConnectorShape(CTConnector shape, XSLFSheet sheet){
        super(shape, sheet);
    }

    /**
     * @param shapeId 1-based shapeId
     */
    static CTConnector prototype(int shapeId) {
        CTConnector ct = CTConnector.Factory.newInstance();
        CTConnectorNonVisual nvSpPr = ct.addNewNvCxnSpPr();
        CTNonVisualDrawingProps cnv = nvSpPr.addNewCNvPr();
        cnv.setName("Connector " + shapeId);
        cnv.setId(shapeId + 1);
        nvSpPr.addNewCNvCxnSpPr();
        nvSpPr.addNewNvPr();
        CTShapeProperties spPr = ct.addNewSpPr();
        CTPresetGeometry2D prst = spPr.addNewPrstGeom();
        prst.setPrst(STShapeType.LINE);
        prst.addNewAvLst();
        CTLineProperties ln = spPr.addNewLn();
        return ct;
    }

    /**
     * Specifies the line end decoration, such as a triangle or arrowhead.
     */
    public void setLineHeadDecoration(LineDecoration style){
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetHeadEnd() ? ln.getHeadEnd() : ln.addNewHeadEnd();
        if(style == null){
            if(lnEnd.isSetType()) lnEnd.unsetType();
        } else {
            lnEnd.setType(STLineEndType.Enum.forInt(style.ordinal() + 1));
        }
    }

    public LineDecoration getLineHeadDecoration(){
        CTLineProperties ln = getSpPr().getLn();
        if(!ln.isSetHeadEnd()) return LineDecoration.NONE;

        STLineEndType.Enum end = ln.getHeadEnd().getType();
        return end == null ? LineDecoration.NONE : LineDecoration.values()[end.intValue() - 1];
    }

    /**
     * specifies decorations which can be added to the head of a line.
     */
    public void setLineHeadWidth(LineEndWidth style){
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetHeadEnd() ? ln.getHeadEnd() : ln.addNewHeadEnd();
        if(style == null){
            if(lnEnd.isSetW()) lnEnd.unsetW();
        } else {
            lnEnd.setW(STLineEndWidth.Enum.forInt(style.ordinal() + 1));
        }
    }

    public LineEndWidth getLineHeadWidth(){
        CTLineProperties ln = getSpPr().getLn();
        if(!ln.isSetHeadEnd()) return null;

        STLineEndWidth.Enum w = ln.getHeadEnd().getW();
        return w == null ? null : LineEndWidth.values()[w.intValue() - 1];
    }

    /**
     * Specifies the line end width in relation to the line width.
     */
    public void setLineHeadLength(LineEndLength style){
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetHeadEnd() ? ln.getHeadEnd() : ln.addNewHeadEnd();

        if(style == null){
            if(lnEnd.isSetLen()) lnEnd.unsetLen();
        } else {
            lnEnd.setLen(STLineEndLength.Enum.forInt(style.ordinal() + 1));
        }
    }

    public LineEndLength getLineHeadLength(){
        CTLineProperties ln = getSpPr().getLn();
        if(!ln.isSetHeadEnd()) return null;

        STLineEndLength.Enum len = ln.getHeadEnd().getLen();
        return len == null ? null : LineEndLength.values()[len.intValue() - 1];
    }

    /**
     * Specifies the line end decoration, such as a triangle or arrowhead.
     */
    public void setLineTailDecoration(LineDecoration style){
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetTailEnd() ? ln.getTailEnd() : ln.addNewTailEnd();
        if(style == null){
            if(lnEnd.isSetType()) lnEnd.unsetType();
        } else {
            lnEnd.setType(STLineEndType.Enum.forInt(style.ordinal() + 1));
        }
    }

    public LineDecoration getLineTailDecoration(){
        CTLineProperties ln = getSpPr().getLn();
        if(!ln.isSetTailEnd()) return LineDecoration.NONE;

        STLineEndType.Enum end = ln.getTailEnd().getType();
        return end == null ? LineDecoration.NONE : LineDecoration.values()[end.intValue() - 1];
    }

    /**
     * specifies decorations which can be added to the tail of a line.
     */
    public void setLineTailWidth(LineEndWidth style){
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetTailEnd() ? ln.getTailEnd() : ln.addNewTailEnd();
        if(style == null){
            if(lnEnd.isSetW()) lnEnd.unsetW();
        } else {
            lnEnd.setW(STLineEndWidth.Enum.forInt(style.ordinal() + 1));
        }
    }

    public LineEndWidth getLineTailWidth(){
        CTLineProperties ln = getSpPr().getLn();
        if(!ln.isSetTailEnd()) return null;

        STLineEndWidth.Enum w = ln.getTailEnd().getW();
        return w == null ? null : LineEndWidth.values()[w.intValue() - 1];
    }

    /**
     * Specifies the line end width in relation to the line width.
     */
    public void setLineTailLength(LineEndLength style){
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetTailEnd() ? ln.getTailEnd() : ln.addNewTailEnd();

        if(style == null){
            if(lnEnd.isSetLen()) lnEnd.unsetLen();
        } else {
            lnEnd.setLen(STLineEndLength.Enum.forInt(style.ordinal() + 1));
        }
    }

    public LineEndLength getLineTailLength(){
        CTLineProperties ln = getSpPr().getLn();
        if(!ln.isSetTailEnd()) return null;

        STLineEndLength.Enum len = ln.getTailEnd().getLen();
        return len == null ? null : LineEndLength.values()[len.intValue() - 1];
    }

}