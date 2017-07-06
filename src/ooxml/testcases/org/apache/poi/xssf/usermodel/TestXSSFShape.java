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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Test;

/**
 * Tests for XSSFShape
 */
public final class TestXSSFShape {

    @Test
    public void test58325_one() {
        check58325(XSSFTestDataSamples.openSampleWorkbook("58325_lt.xlsx"), 1);
    }

    @Test
    public void test58325_three() {
        check58325(XSSFTestDataSamples.openSampleWorkbook("58325_db.xlsx"), 3);
    }

    private void check58325(XSSFWorkbook wb, int expectedShapes) {
        XSSFSheet sheet = wb.getSheet("MetasNM001");
        assertNotNull(sheet);

        StringBuilder str = new StringBuilder();
        str.append("sheet " + sheet.getSheetName() + " - ");

        XSSFDrawing drawing = sheet.getDrawingPatriarch();
        //drawing = ((XSSFSheet)sheet).createDrawingPatriarch();

        List<XSSFShape> shapes = drawing.getShapes();
        str.append("drawing.getShapes().size() = " + shapes.size());
        Iterator<XSSFShape> it = shapes.iterator();
        while(it.hasNext()) {           
            XSSFShape shape = it.next();
            str.append(", " + shape);
            str.append(", Col1:"+((XSSFClientAnchor)shape.getAnchor()).getCol1());
            str.append(", Col2:"+((XSSFClientAnchor)shape.getAnchor()).getCol2());
            str.append(", Row1:"+((XSSFClientAnchor)shape.getAnchor()).getRow1());
            str.append(", Row2:"+((XSSFClientAnchor)shape.getAnchor()).getRow2());
        }
        
        assertEquals("Having shapes: " + str, 
                expectedShapes, shapes.size());
    }
}
