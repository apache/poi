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

import junit.framework.TestCase;
import org.apache.poi.hssf.HSSFTestDataSamples;

public final class TestUnicodeWorkbook extends TestCase {

    /**
     *  Tests Bug38230
     *  That a Umlat is written  and then read back.
     *  It should have been written as a compressed unicode.
     */
    public void testUmlatReadWrite() {
        HSSFWorkbook wb = new HSSFWorkbook();

        //Create a unicode sheet name (euro symbol)
        HSSFSheet s = wb.createSheet("test");

        HSSFRow r = s.createRow(0);
        HSSFCell c = r.createCell(1);
        c.setCellValue(new HSSFRichTextString("\u00e4"));

        //Confirm that the sring will be compressed
        assertEquals(c.getRichStringCellValue().getUnicodeString().getOptionFlags(), 0);

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);

        //Test the sheetname
        s = wb.getSheet("test");
        assertNotNull(s);

        c = r.getCell(1);
        assertEquals(c.getRichStringCellValue().getString(), "\u00e4");
    }
}
