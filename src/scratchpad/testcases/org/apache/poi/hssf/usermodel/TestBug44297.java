package org.apache.poi.hssf.usermodel;
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

import junit.framework.TestCase;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;

/**
 * Bug 44297: 32767+32768 is evaluated to -1
 * Fix: IntPtg must operate with unsigned short. Reading signed short results in incorrect formula calculation
 * if a formula has values in the interval [Short.MAX_VALUE, (Short.MAX_VALUE+1)*2]
 *
 * @author Yegor Kozlov
 */

public class TestBug44297 extends TestCase {
    protected String cwd = System.getProperty("HSSF.testdata.path");

    public void test44297() throws IOException {
        FileInputStream in = new FileInputStream(new File(cwd, "44297.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();

        HSSFRow row;
        HSSFCell  cell;

        HSSFSheet sheet   = wb.getSheetAt(0);

        HSSFFormulaEvaluator eva = new HSSFFormulaEvaluator(sheet, wb);

        row = (HSSFRow)sheet.getRow(0);
        cell = row.getCell((short)0);
        assertEquals("31+46", cell.getCellFormula());
        eva.setCurrentRow(row);
        assertEquals(77, eva.evaluate(cell).getNumberValue(), 0);

        row = (HSSFRow)sheet.getRow(1);
        cell = row.getCell((short)0);
        assertEquals("30+53", cell.getCellFormula());
        eva.setCurrentRow(row);
        assertEquals(83, eva.evaluate(cell).getNumberValue(), 0);

        row = (HSSFRow)sheet.getRow(2);
        cell = row.getCell((short)0);
        assertEquals("SUM(A1:A2)", cell.getCellFormula());
        eva.setCurrentRow(row);
        assertEquals(160, eva.evaluate(cell).getNumberValue(), 0);

        row = (HSSFRow)sheet.getRow(4);
        cell = row.getCell((short)0);
        assertEquals("32767+32768", cell.getCellFormula());
        eva.setCurrentRow(row);
        assertEquals(65535, eva.evaluate(cell).getNumberValue(), 0);

        row = (HSSFRow)sheet.getRow(7);
        cell = row.getCell((short)0);
        assertEquals("32744+42333", cell.getCellFormula());
        eva.setCurrentRow(row);
        assertEquals(75077, eva.evaluate(cell).getNumberValue(), 0);

        row = (HSSFRow)sheet.getRow(8);
        cell = row.getCell((short)0);
        assertEquals("327680.0/32768", cell.getCellFormula());
        eva.setCurrentRow(row);
        assertEquals(10, eva.evaluate(cell).getNumberValue(), 0);

        row = (HSSFRow)sheet.getRow(9);
        cell = row.getCell((short)0);
        assertEquals("32767+32769", cell.getCellFormula());
        eva.setCurrentRow(row);
        assertEquals(65536, eva.evaluate(cell).getNumberValue(), 0);

        row = (HSSFRow)sheet.getRow(10);
        cell = row.getCell((short)0);
        assertEquals("35000+36000", cell.getCellFormula());
        eva.setCurrentRow(row);
        assertEquals(71000, eva.evaluate(cell).getNumberValue(), 0);

        row = (HSSFRow)sheet.getRow(11);
        cell = row.getCell((short)0);
        assertEquals("-1000000.0-3000000.0", cell.getCellFormula());
        eva.setCurrentRow(row);
        assertEquals(-4000000, eva.evaluate(cell).getNumberValue(), 0);
    }

}
