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

package org.apache.poi.osgi;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;

/**
 * Test to ensure that all our main formats can create, write
 * and read back in, when running under OSGi
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OSGiSpreadsheetIT extends BaseOSGiTestCase {


    // create a workbook, validate and write back
    void testWorkbook(Workbook wb) throws Exception {
        Sheet s = wb.createSheet("OSGi");
        s.createRow(0).createCell(0).setCellValue("With OSGi");
        s.createRow(1).createCell(0).setCellFormula("SUM(A1:B3)");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wb.write(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        wb = WorkbookFactory.create(bais);
        assertEquals(1, wb.getNumberOfSheets());

        s = wb.getSheet("OSGi");
        assertEquals("With OSGi", s.getRow(0).getCell(0).toString());
        assertEquals("SUM(A1:B3)", s.getRow(1).getCell(0).toString());


    }

    @Test
    public void testHSSF() throws Exception {
        testWorkbook(new HSSFWorkbook());
    }

    @Test
    public void testXSSF() throws Exception {
        testWorkbook(new XSSFWorkbook());
    }

    @Test
    public void testSXSSF() throws Exception {
        testWorkbook(new XSSFWorkbook());
    }

    @Test
    public void testFormulaEvaluation() throws Exception {
        testWorkbook(new XSSFWorkbook());
    }

}
