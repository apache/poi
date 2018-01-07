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
     * @since POI 3.15-beta2
     */
    protected CTTablePartStyle getTablePartStyle(TablePartStyle tps) {
        switch (tps) {
        default:
        case wholeTbl:
            return _tblStyle.getWholeTbl();
        case band1H:
            return _tblStyle.getBand1H();
        case band2H:
            return _tblStyle.getBand2H();
        case band1V:
            return _tblStyle.getBand1V();
        case band2V:
            return _tblStyle.getBand2V();
        case firstCol:
            return _tblStyle.getFirstCol();
        case lastCol:
            return _tblStyle.getLastCol();
        case firstRow:
            return _tblStyle.getFirstRow();
        case lastRow:
            return _tblStyle.getLastRow();
        case seCell:
            return _tblStyle.getSeCell();
        case swCell:
            return _tblStyle.getSwCell();
        case neCell:
            return _tblStyle.getNeCell();
        case nwCell:
            return _tblStyle.getNwCell();
        }
    }
}