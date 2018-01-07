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

import org.apache.poi.hssf.record.CFRuleBase;
import org.apache.poi.hssf.record.cf.PatternFormatting;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Color;

/**
 * High level representation for Conditional Formatting settings
 */
public class HSSFPatternFormatting implements org.apache.poi.ss.usermodel.PatternFormatting {
    private final HSSFWorkbook workbook;
    private final CFRuleBase cfRuleRecord;
    private final PatternFormatting patternFormatting;

    protected HSSFPatternFormatting(CFRuleBase cfRuleRecord, HSSFWorkbook workbook) {
        this.workbook = workbook;
        this.cfRuleRecord = cfRuleRecord; 
        this.patternFormatting = cfRuleRecord.getPatternFormatting();
    }

    protected PatternFormatting getPatternFormattingBlock()
    {
        return patternFormatting;
    }

    public HSSFColor getFillBackgroundColorColor() {
        return workbook.getCustomPalette().getColor(getFillBackgroundColor());
    }

    public HSSFColor getFillForegroundColorColor() {
        return workbook.getCustomPalette().getColor(getFillForegroundColor());
    }

    /**
     * @see org.apache.poi.hssf.record.cf.PatternFormatting#getFillBackgroundColor()
     */
    public short getFillBackgroundColor()
    {
        return (short)patternFormatting.getFillBackgroundColor();
    }

    /**
     * @see org.apache.poi.hssf.record.cf.PatternFormatting#getFillForegroundColor()
     */
    public short getFillForegroundColor()
    {
        return (short)patternFormatting.getFillForegroundColor();
    }

    /**
     * @see org.apache.poi.hssf.record.cf.PatternFormatting#getFillPattern()
     */
    public short getFillPattern()
    {
        return (short)patternFormatting.getFillPattern();
    }

    public void setFillBackgroundColor(Color bg) {
        HSSFColor hcolor = HSSFColor.toHSSFColor(bg);
        if (hcolor == null) {
            setFillBackgroundColor((short)0);
        } else {
            setFillBackgroundColor(hcolor.getIndex());
        }
    }

    public void setFillForegroundColor(Color fg) {
        HSSFColor hcolor = HSSFColor.toHSSFColor(fg);
        if (hcolor == null) {
            setFillForegroundColor((short)0);
        } else {
            setFillForegroundColor(hcolor.getIndex());
        }
    }

    /**
     * @param bg
     * @see org.apache.poi.hssf.record.cf.PatternFormatting#setFillBackgroundColor(int)
     */
    public void setFillBackgroundColor(short bg)
    {
        patternFormatting.setFillBackgroundColor(bg);
        if( bg != 0)
        {
            cfRuleRecord.setPatternBackgroundColorModified(true);
        }
    }

    /**
     * @param fg
     * @see org.apache.poi.hssf.record.cf.PatternFormatting#setFillForegroundColor(int)
     */
    public void setFillForegroundColor(short fg)
    {
        patternFormatting.setFillForegroundColor(fg);
        if( fg != 0)
        {
            cfRuleRecord.setPatternColorModified(true);
        }
    }

    /**
     * @param fp
     * @see org.apache.poi.hssf.record.cf.PatternFormatting#setFillPattern(int)
     */
    public void setFillPattern(short fp)
    {
        patternFormatting.setFillPattern(fp);
        if( fp != 0)
        {
            cfRuleRecord.setPatternStyleModified(true);
        }
    }
}
