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

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

/**
 * Implementation of {@link HtmlHelper} for XSSF files.
 *
 * @author Ken Arnold, Industrious Media LLC
 */
public class XSSFHtmlHelper implements HtmlHelper {
    @Override
    public void colorStyles(CellStyle style, Formatter out) {
        XSSFCellStyle cs = (XSSFCellStyle) style;
        styleColor(out, "background-color", cs.getFillForegroundXSSFColor());
        styleColor(out, "text-color", cs.getFont().getXSSFColor());
    }

    private void styleColor(Formatter out, String attr, XSSFColor color) {
        if (color == null || color.isAuto()) {
            return;
        }

        byte[] rgb = color.getRGB();
        if (rgb == null) {
            return;
        }

        // This is done twice -- rgba is new with CSS 3, and browser that don't
        // support it will ignore the rgba specification and stick with the
        // solid color, which is declared first
        out.format("  %s: #%02x%02x%02x;%n", attr, rgb[0], rgb[1], rgb[2]);
        byte[] argb = color.getARGB();
        if (argb == null) {
            return;
        }
        out.format("  %s: rgba(0x%02x, 0x%02x, 0x%02x, 0x%02x);%n", attr,
                argb[3], argb[0], argb[1], argb[2]);
    }
}