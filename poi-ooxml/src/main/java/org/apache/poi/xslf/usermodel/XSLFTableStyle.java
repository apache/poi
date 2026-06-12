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

import org.openxmlformats.schemas.drawingml.x2006.main.CTTablePartStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableStyle;

/**
 * Represents a table style in a .pptx presentation
 */
public class XSLFTableStyle {
    private CTTableStyle _tblStyle;

    public enum TablePartStyle {
        wholeTbl, band1H, band2H, band1V, band2V, firstCol, lastCol, firstRow, lastRow, seCell, swCell, neCell, nwCell
    }
    
    /*package*/ XSLFTableStyle(CTTableStyle style){
        _tblStyle = style;
    }

    public CTTableStyle getXmlObject(){
        return _tblStyle;
    }

    public String getStyleName(){
        return _tblStyle.getStyleName();
    }

    public String getStyleId(){
        return _tblStyle.getStyleId();
    }

    /**
     * @since 3.15-beta2
     */
    protected CTTablePartStyle getTablePartStyle(TablePartStyle tps) {
        return switch (tps) {
            case band1H -> _tblStyle.getBand1H();
            case band2H -> _tblStyle.getBand2H();
            case band1V -> _tblStyle.getBand1V();
            case band2V -> _tblStyle.getBand2V();
            case firstCol -> _tblStyle.getFirstCol();
            case lastCol -> _tblStyle.getLastCol();
            case firstRow -> _tblStyle.getFirstRow();
            case lastRow -> _tblStyle.getLastRow();
            case seCell -> _tblStyle.getSeCell();
            case swCell -> _tblStyle.getSwCell();
            case neCell -> _tblStyle.getNeCell();
            case nwCell -> _tblStyle.getNwCell();
            default -> _tblStyle.getWholeTbl();
        };
    }
}