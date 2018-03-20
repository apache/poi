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

import org.apache.poi.ss.usermodel.BorderFormatting;
import org.apache.poi.ss.usermodel.DifferentialStyleProvider;
import org.apache.poi.ss.usermodel.ExcelNumberFormat;
import org.apache.poi.ss.usermodel.FontFormatting;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTNumFmt;

/**
 * Style based on a dxf record - e.g. table style element or conditional formatting rule
 */
public class XSSFDxfStyleProvider implements DifferentialStyleProvider {
    
    private final IndexedColorMap colorMap;
    private final BorderFormatting border;
    private final FontFormatting font;
    private final ExcelNumberFormat number;
    private final PatternFormatting fill;
    private final int stripeSize;
    
    /**
     * @param dxf
     * @param stripeSize 0 for non-stripe styles, > 1 for stripes
     * @param colorMap 
     */
    public XSSFDxfStyleProvider(CTDxf dxf, int stripeSize, IndexedColorMap colorMap) {
        this.stripeSize = stripeSize;
        this.colorMap = colorMap;
        if (dxf == null) {
            border = null;
            font = null;
            number = null;
            fill = null;
        } else {
            border = dxf.isSetBorder() ? new XSSFBorderFormatting(dxf.getBorder(), colorMap) : null; 
            font = dxf.isSetFont() ? new XSSFFontFormatting(dxf.getFont(), colorMap) : null; 
            if (dxf.isSetNumFmt()) {
                CTNumFmt numFmt = dxf.getNumFmt();
                number = new ExcelNumberFormat((int) numFmt.getNumFmtId(), numFmt.getFormatCode());
            } else {
                number = null;
            }
            fill = dxf.isSetFill() ? new XSSFPatternFormatting(dxf.getFill(), colorMap) : null; 
        }
    }

    public BorderFormatting getBorderFormatting() {
        return border;
    }

    public FontFormatting getFontFormatting() {
        return font;
    }

    public ExcelNumberFormat getNumberFormat() {
        return number;
    }

    public PatternFormatting getPatternFormatting() {
        return fill;
    }
    
    public int getStripeSize() {
        return stripeSize;
    }

}
