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

package org.apache.poi.examples.ss.html;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class TestToHtml {
    @Test
    void cellTextIsEscapedHSSF() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            assertCellTextIsEscaped(wb);
        }
    }

    @Test
    void cellTextIsEscapedXSSF() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            assertCellTextIsEscaped(wb);
        }
    }

    private static void assertCellTextIsEscaped(Workbook wb) throws IOException {
        Row row = wb.createSheet().createRow(0);
        row.createCell(0).setCellValue("<script>alert(1)</script>");
        row.createCell(1).setCellValue("<a href=\"javascript:alert(1)\">x</a> & y");

        StringBuilder sb = new StringBuilder();
        ToHtml.create(wb, sb).printPage();
        String html = sb.toString();

        // the base style sheet is included
        assertThat(html, containsString("<style type=\"text/css\">"));
        assertThat(html, containsString(".excelDefaults {"));
        assertThat(html, not(containsString("<script>")));
        assertThat(html, not(containsString("<a href")));
        assertThat(html, containsString("&lt;script&gt;alert(1)&lt;/script&gt;"));
        assertThat(html, containsString("&lt;a href=&quot;javascript:alert(1)&quot;&gt;x&lt;/a&gt; &amp; y"));
    }
}
