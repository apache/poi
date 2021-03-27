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

import java.io.IOException;
import java.util.List;

import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;

/**
 * Tests for XSSFShape
 */
public final class TestXSSFShape {

    @Test
    void test58325_one() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("58325_lt.xlsx")) {
            check58325(wb, 1);
        }
    }

    @Test
    void test58325_three() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("58325_db.xlsx")) {
            check58325(wb, 3);
        }
    }

    private void check58325(XSSFWorkbook wb, int expectedShapes) {
        XSSFSheet sheet = wb.getSheet("MetasNM001");
        assertNotNull(sheet);

        StringBuilder str = new StringBuilder();
        str.append("sheet ").append(sheet.getSheetName()).append(" - ");

        XSSFDrawing drawing = sheet.getDrawingPatriarch();
        //drawing = ((XSSFSheet)sheet).createDrawingPatriarch();

        List<XSSFShape> shapes = drawing.getShapes();
        str.append("drawing.getShapes().size() = ").append(shapes.size());
        for (XSSFShape shape : shapes) {
            str.append(", ").append(shape);
            str.append(", Col1:").append(((XSSFClientAnchor) shape.getAnchor()).getCol1());
            str.append(", Col2:").append(((XSSFClientAnchor) shape.getAnchor()).getCol2());
            str.append(", Row1:").append(((XSSFClientAnchor) shape.getAnchor()).getRow1());
            str.append(", Row2:").append(((XSSFClientAnchor) shape.getAnchor()).getRow2());
        }

        assertEquals(expectedShapes, shapes.size(), "Having shapes: " + str);
    }
}
