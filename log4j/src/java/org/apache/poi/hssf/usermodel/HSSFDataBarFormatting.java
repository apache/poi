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
import org.apache.poi.hssf.record.cf.DataBarFormatting;
import org.apache.poi.hssf.record.cf.DataBarThreshold;
import org.apache.poi.ss.usermodel.Color;

/**
 * High level representation for DataBar / Data-Bar Formatting 
 *  component of Conditional Formatting settings
 */
public final class HSSFDataBarFormatting implements org.apache.poi.ss.usermodel.DataBarFormatting {
    private final HSSFSheet sheet;
    private final CFRule12Record cfRule12Record;
    private final DataBarFormatting databarFormatting;

    protected HSSFDataBarFormatting(CFRule12Record cfRule12Record, HSSFSheet sheet) {
        this.sheet = sheet;
        this.cfRule12Record = cfRule12Record;
        this.databarFormatting = this.cfRule12Record.getDataBarFormatting();
    }

    public boolean isLeftToRight() {
        return !databarFormatting.isReversed();
    }
    public void setLeftToRight(boolean ltr) {
        databarFormatting.setReversed(!ltr);
    }

    public int getWidthMin() {
        return databarFormatting.getPercentMin();
    }
    public void setWidthMin(int width) {
        databarFormatting.setPercentMin((byte)width);
    }

    public int getWidthMax() {
        return databarFormatting.getPercentMax();
    }
    public void setWidthMax(int width) {
        databarFormatting.setPercentMax((byte)width);
    }

    public HSSFExtendedColor getColor() {
        return new HSSFExtendedColor(databarFormatting.getColor());
    }
    public void setColor(Color color) {
        HSSFExtendedColor hcolor = (HSSFExtendedColor)color;
        databarFormatting.setColor(hcolor.getExtendedColor());
    }

    public HSSFConditionalFormattingThreshold getMinThreshold() {
        return new HSSFConditionalFormattingThreshold(databarFormatting.getThresholdMin(), sheet);
    }
    public HSSFConditionalFormattingThreshold getMaxThreshold() {
        return new HSSFConditionalFormattingThreshold(databarFormatting.getThresholdMax(), sheet);
    }

    public boolean isIconOnly() {
        return databarFormatting.isIconOnly();
    }
    public void setIconOnly(boolean only) {
        databarFormatting.setIconOnly(only);
    }

    public HSSFConditionalFormattingThreshold createThreshold() {
        return new HSSFConditionalFormattingThreshold(new DataBarThreshold(), sheet);
    }
}
