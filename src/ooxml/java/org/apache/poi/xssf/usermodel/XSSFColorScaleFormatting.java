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
import org.apache.poi.ss.usermodel.ColorScaleFormatting;
import org.apache.poi.ss.usermodel.ConditionalFormattingThreshold;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfvo;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColorScale;

/**
 * High level representation for Color Scale / Color Gradient Formatting 
 *  component of Conditional Formatting settings
 */
public class XSSFColorScaleFormatting implements ColorScaleFormatting {
    private CTColorScale _scale;
    private IndexedColorMap _indexedColorMap;

    /*package*/ XSSFColorScaleFormatting(CTColorScale scale, IndexedColorMap colorMap){
        _scale = scale;
        _indexedColorMap = colorMap;
    }
    
    public int getNumControlPoints() {
        return _scale.sizeOfCfvoArray();
    }
    public void setNumControlPoints(int num) {
        while (num < _scale.sizeOfCfvoArray()) {
            _scale.removeCfvo(_scale.sizeOfCfvoArray()-1);
            _scale.removeColor(_scale.sizeOfColorArray()-1);
        }
        while (num > _scale.sizeOfCfvoArray()) {
            _scale.addNewCfvo();
            _scale.addNewColor();
        }
    }

    public XSSFColor[] getColors() {
        CTColor[] ctcols = _scale.getColorArray();
        XSSFColor[] c = new XSSFColor[ctcols.length];
        for (int i=0; i<ctcols.length; i++) {
            c[i] = XSSFColor.from(ctcols[i], _indexedColorMap);
        }
        return c;
    }
    public void setColors(Color[] colors) {
        CTColor[] ctcols = new CTColor[colors.length];
        for (int i=0; i<colors.length; i++) {
            ctcols[i] = ((XSSFColor)colors[i]).getCTColor();
        }
        _scale.setColorArray(ctcols);
    }

    public XSSFConditionalFormattingThreshold[] getThresholds() {
        CTCfvo[] cfvos = _scale.getCfvoArray();
        XSSFConditionalFormattingThreshold[] t = 
                new XSSFConditionalFormattingThreshold[cfvos.length];
        for (int i=0; i<cfvos.length; i++) {
            t[i] = new XSSFConditionalFormattingThreshold(cfvos[i]);
        }
        return t;
    }
    public void setThresholds(ConditionalFormattingThreshold[] thresholds) {
        CTCfvo[] cfvos = new CTCfvo[thresholds.length];
        for (int i=0; i<thresholds.length; i++) {
            cfvos[i] = ((XSSFConditionalFormattingThreshold)thresholds[i]).getCTCfvo();
        }
        _scale.setCfvoArray(cfvos);
    }
    
    /**
     * @return color from scale
     */
    public XSSFColor createColor() {
        return XSSFColor.from(_scale.addNewColor(), _indexedColorMap);
    }
    public XSSFConditionalFormattingThreshold createThreshold() {
        return new XSSFConditionalFormattingThreshold(_scale.addNewCfvo());
    }
}
