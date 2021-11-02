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
package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.DataBarFormatting;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataBar;

/**
 * High level representation for DataBar / Data Bar Formatting 
 *  component of Conditional Formatting settings
 */
public class XSSFDataBarFormatting implements DataBarFormatting {
    IndexedColorMap _colorMap;
    CTDataBar _databar;

    /*package*/ XSSFDataBarFormatting(CTDataBar databar, IndexedColorMap colorMap){
        _databar = databar;
        _colorMap = colorMap;
    }

    public boolean isIconOnly() {
        if (_databar.isSetShowValue())
            return !_databar.getShowValue();
        return false;
    }
    public void setIconOnly(boolean only) {
        _databar.setShowValue(!only);
    }

    public boolean isLeftToRight() {
        return true;
    }
    public void setLeftToRight(boolean ltr) {
        // TODO How does XSSF encode this?
    }

    public int getWidthMin() {
        return (int)_databar.getMinLength();
    }
    public void setWidthMin(int width) {
        _databar.setMinLength(width);
    }

    public int getWidthMax() {
        return (int)_databar.getMaxLength();
    }
    public void setWidthMax(int width) {
        _databar.setMaxLength(width);
    }

    public XSSFColor getColor() {
        return XSSFColor.from(_databar.getColor(), _colorMap);
    }
    public void setColor(Color color) {
        _databar.setColor( ((XSSFColor)color).getCTColor() );
    }

    public XSSFConditionalFormattingThreshold getMinThreshold() {
        return new XSSFConditionalFormattingThreshold(_databar.getCfvoArray(0));
    }
    public XSSFConditionalFormattingThreshold getMaxThreshold() {
        return new XSSFConditionalFormattingThreshold(_databar.getCfvoArray(1));
    }

    public XSSFConditionalFormattingThreshold createThreshold() {
        return new XSSFConditionalFormattingThreshold(_databar.addNewCfvo());
    }
}
