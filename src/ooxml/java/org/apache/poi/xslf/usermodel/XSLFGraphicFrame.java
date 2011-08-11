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

import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.ShapeGroup;
import org.apache.poi.util.Beta;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;

import java.awt.geom.Rectangle2D;

/**
 * @author Yegor Kozlov
 */
@Beta
public class XSLFGraphicFrame extends XSLFShape {
    private final CTGraphicalObjectFrame _shape;
    private final XSLFSheet _sheet;

    /*package*/ XSLFGraphicFrame(CTGraphicalObjectFrame shape, XSLFSheet sheet){
        _shape = shape;
        _sheet = sheet;
    }

    public CTGraphicalObjectFrame getXmlObject(){
        return _shape;
    }

    public int getShapeType(){
        throw new RuntimeException("NotImplemented");
    }

    public int getShapeId(){
        return (int)_shape.getNvGraphicFramePr().getCNvPr().getId();
    }

    public String getShapeName(){
        return _shape.getNvGraphicFramePr().getCNvPr().getName();
    }

    public Rectangle2D getAnchor(){
        throw new RuntimeException("NotImplemented");
    }

    public void setAnchor(Rectangle2D anchor){
        throw new RuntimeException("NotImplemented");
    }

    public ShapeGroup getParent(){
        throw new RuntimeException("NotImplemented");
    }

    public Shape[] getShapes(){
        throw new RuntimeException("NotImplemented");
    }


    public boolean removeShape(Shape shape){
        throw new RuntimeException("NotImplemented");
    }
}