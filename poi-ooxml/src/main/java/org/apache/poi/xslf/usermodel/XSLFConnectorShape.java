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

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.sl.usermodel.ConnectorShape;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.util.Beta;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType;
import org.openxmlformats.schemas.presentationml.x2006.main.CTConnector;
import org.openxmlformats.schemas.presentationml.x2006.main.CTConnectorNonVisual;

/**
 * Specifies a connection shape.
 */
@Beta
public class XSLFConnectorShape extends XSLFSimpleShape
    implements ConnectorShape<XSLFShape,XSLFTextParagraph> {

    /*package*/ XSLFConnectorShape(CTConnector shape, XSLFSheet sheet) {
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
        cnv.setId(shapeId);
        nvSpPr.addNewCNvCxnSpPr();
        nvSpPr.addNewNvPr();
        CTShapeProperties spPr = ct.addNewSpPr();
        CTPresetGeometry2D prst = spPr.addNewPrstGeom();
        prst.setPrst(STShapeType.LINE);
        prst.addNewAvLst();
        /* CTLineProperties ln = */ spPr.addNewLn();
        return ct;
    }


    /**
     * YK: shadows of lines are suppressed for now.
     */
    @Override
    public XSLFShadow getShadow() {
        return null;
    }

    @Override
    public void setPlaceholder(Placeholder placeholder) {
        throw new POIXMLException("A connector shape can't be a placeholder.");
    }
}