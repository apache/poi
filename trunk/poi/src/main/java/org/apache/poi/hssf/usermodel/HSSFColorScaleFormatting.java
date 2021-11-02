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

package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.record.CFRule12Record;
import org.apache.poi.hssf.record.cf.ColorGradientFormatting;
import org.apache.poi.hssf.record.cf.ColorGradientThreshold;
import org.apache.poi.hssf.record.cf.Threshold;
import org.apache.poi.hssf.record.common.ExtendedColor;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.ConditionalFormattingThreshold;

/**
 * High level representation for Color Scale / Color Gradient 
 *  Formatting component of Conditional Formatting settings
 */
public final class HSSFColorScaleFormatting implements org.apache.poi.ss.usermodel.ColorScaleFormatting {
    private final HSSFSheet sheet;
    private final CFRule12Record cfRule12Record;
    private final ColorGradientFormatting colorFormatting;

    protected HSSFColorScaleFormatting(CFRule12Record cfRule12Record, HSSFSheet sheet) {
        this.sheet = sheet;
        this.cfRule12Record = cfRule12Record;
        this.colorFormatting = this.cfRule12Record.getColorGradientFormatting();
    }

    public int getNumControlPoints() {
        return colorFormatting.getNumControlPoints();
    }
    public void setNumControlPoints(int num) {
        colorFormatting.setNumControlPoints(num);
    }

    public HSSFExtendedColor[] getColors() {
        ExtendedColor[] colors = colorFormatting.getColors();
        HSSFExtendedColor[] hcolors = new HSSFExtendedColor[colors.length];
        for (int i=0; i<colors.length; i++) {
            hcolors[i] = new HSSFExtendedColor(colors[i]);
        }
        return hcolors;
    }
    public void setColors(Color[] colors) {
        ExtendedColor[] cr = new ExtendedColor[colors.length];
        for (int i=0; i<colors.length; i++) {
            cr[i] = ((HSSFExtendedColor)colors[i]).getExtendedColor();
        }
        colorFormatting.setColors(cr);
    }

    public HSSFConditionalFormattingThreshold[] getThresholds() {
        Threshold[] t = colorFormatting.getThresholds();
        HSSFConditionalFormattingThreshold[] ht = new HSSFConditionalFormattingThreshold[t.length];
        for (int i=0; i<t.length; i++) {
            ht[i] = new HSSFConditionalFormattingThreshold(t[i], sheet);
        }
        return ht;
    }

    public void setThresholds(ConditionalFormattingThreshold[] thresholds) {
        ColorGradientThreshold[] t = new ColorGradientThreshold[thresholds.length];
        for (int i=0; i<t.length; i++) {
            HSSFConditionalFormattingThreshold hssfT = (HSSFConditionalFormattingThreshold)thresholds[i];
            t[i] = (ColorGradientThreshold)hssfT.getThreshold();
        }
        colorFormatting.setThresholds(t);
    }

    public HSSFConditionalFormattingThreshold createThreshold() {
        return new HSSFConditionalFormattingThreshold(new ColorGradientThreshold(), sheet);
    }
}
