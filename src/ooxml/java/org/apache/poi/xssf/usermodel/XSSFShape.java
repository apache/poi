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

import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTTwoCellAnchor;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTAbsoluteAnchor;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTOneCellAnchor;
import org.openxmlformats.schemas.drawingml.x2006.chartDrawing.CTGroupShape;

/**
 * Represents a shape in a SpreadsheetML drawing.
 * 
 * @author Yegor Kozlov
 */
public abstract class XSSFShape {
    public static final int EMU_PER_POINT = 12700;


    /**
     * Shape container. Can be CTTwoCellAnchor, CTOneCellAnchor, CTAbsoluteAnchor or CTGroupShape
     */
    private XmlObject spContainer;

    /**
     * Parent drawing
     */
    private XSSFDrawing drawing;

    /**
     * The parent shape, always not-null for shapes in groups
     */
    private XSSFShape parent;

    /**
     * Construct a new XSSFSimpleShape object.
     *
     * @param parent the XSSFDrawing that owns this shape
     * @param anchor an object that encloses the shape bean,
     *   can be CTTwoCellAnchor, CTOneCellAnchor, CTAbsoluteAnchor or CTGroupShape
     */
    protected XSSFShape(XSSFDrawing parent, XmlObject anchor){
        drawing = parent;
        if(!(anchor instanceof CTTwoCellAnchor) && !(anchor instanceof CTOneCellAnchor) &&
           !(anchor instanceof CTAbsoluteAnchor) && !(anchor instanceof CTGroupShape)) {
            throw new IllegalArgumentException("anchor must be one of the following types: " +
                    "CTTwoCellAnchor, CTOneCellAnchor, CTAbsoluteAnchor or CTGroupShape");
        }
        spContainer = anchor;
    }

    /**
     * Return the anchor bean that encloses this shape.
     * Can be CTTwoCellAnchor, CTOneCellAnchor, CTAbsoluteAnchor or CTGroupShape.
     *
     * @return the anchor bean that encloses this shape
     */
    public XmlObject getShapeContainer(){
        return spContainer;
    }

    /**
     * Return the drawing that owns this shape
     *
     * @return the parent drawing that owns this shape
     */
    public XSSFDrawing getDrawing(){
        return drawing;
    }

    /**
     * Gets the parent shape.
     */
    public XSSFShape getParent()
    {
        return parent;
    }

}
