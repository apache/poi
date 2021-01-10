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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ss.usermodel.DifferentialStyleProvider;
import org.apache.poi.ss.usermodel.FontFormatting;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.Table;
import org.apache.poi.ss.usermodel.TableStyle;
import org.apache.poi.ss.usermodel.TableStyleInfo;
import org.apache.poi.ss.usermodel.TableStyleType;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;

/**
 * Test built-in table styles
 */
class TestTableStyles {

    /**
     * Test that a built-in style is initialized properly
     */
    @Test
    void testBuiltinStyleInit() {
        TableStyle style = XSSFBuiltinTableStyle.TableStyleMedium2.getStyle();
        assertNotNull(style, "no style found for Medium2");
        assertNull(style.getStyle(TableStyleType.blankRow), "Should not have style info for blankRow");
        DifferentialStyleProvider headerRow = style.getStyle(TableStyleType.headerRow);
        assertNotNull(headerRow, "no header row style");
        FontFormatting font = headerRow.getFontFormatting();
        assertNotNull(font, "No header row font formatting");
        assertTrue(font.isBold(), "header row not bold");
        PatternFormatting fill = headerRow.getPatternFormatting();
        assertNotNull(fill, "No header fill");
        assertEquals(4, ((XSSFColor) fill.getFillBackgroundColorColor()).getTheme(), "wrong header fill");
    }

    @Test
    void testCustomStyle() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("tableStyle.xlsx")) {
            Table table = wb.getTable("Table1");
            assertNotNull(table, "missing table");

            TableStyleInfo style = table.getStyle();
            assertNotNull(style, "Missing table style info");
            assertNotNull(style.getStyle(), "Missing table style");
            assertEquals("TestTableStyle", style.getName(), "Wrong name");
            assertEquals("TestTableStyle", style.getStyle().getName(), "Wrong name");

            DifferentialStyleProvider firstColumn = style.getStyle().getStyle(TableStyleType.firstColumn);
            assertNotNull(firstColumn, "no first column style");
            FontFormatting font = firstColumn.getFontFormatting();
            assertNotNull(font, "no first col font");
            assertTrue(font.isBold(), "wrong first col bold");
        }
    }
}
