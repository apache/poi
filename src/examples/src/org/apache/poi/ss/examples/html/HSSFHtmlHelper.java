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
package org.apache.poi.ss.examples.html;

import java.util.Formatter;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.CellStyle;

/**
 * Implementation of {@link HtmlHelper} for HSSF files.
 */
public class HSSFHtmlHelper implements HtmlHelper {
    private final HSSFWorkbook wb;
    private final HSSFPalette colors;

    private static final HSSFColor HSSF_AUTO = HSSFColorPredefined.AUTOMATIC.getColor();

    public HSSFHtmlHelper(HSSFWorkbook wb) {
        this.wb = wb;
        // If there is no custom palette, then this creates a new one that is
        // a copy of the default
        colors = wb.getCustomPalette();
    }

    @Override
    public void colorStyles(CellStyle style, Formatter out) {
        HSSFCellStyle cs = (HSSFCellStyle) style;
        out.format("  /* fill pattern = %d */%n", cs.getFillPattern().getCode());
        styleColor(out, "background-color", cs.getFillForegroundColor());
        styleColor(out, "color", cs.getFont(wb).getColor());
        styleColor(out, "border-left-color", cs.getLeftBorderColor());
        styleColor(out, "border-right-color", cs.getRightBorderColor());
        styleColor(out, "border-top-color", cs.getTopBorderColor());
        styleColor(out, "border-bottom-color", cs.getBottomBorderColor());
    }

    private void styleColor(Formatter out, String attr, short index) {
        HSSFColor color = colors.getColor(index);
        if (index == HSSF_AUTO.getIndex() || color == null) {
            out.format("  /* %s: index = %d */%n", attr, index);
        } else {
            short[] rgb = color.getTriplet();
            out.format("  %s: #%02x%02x%02x; /* index = %d */%n", attr, rgb[0],
                    rgb[1], rgb[2], index);
        }
    }
}