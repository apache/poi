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

import junit.framework.TestCase;
import org.apache.poi.xssf.XSSFTestDataSamples;

/**
 * @author Yegor Kozlov
 */
public class TestXSSFName extends TestCase {

    public void testCreate(){
        // Create a new workbook
        XSSFWorkbook wb = new XSSFWorkbook();

        XSSFName name1 = wb.createName();
        name1.setNameName("testOne");

        //setting a duplicate name should throw IllegalArgumentException
        XSSFName name2 = wb.createName();
        try {
            name2.setNameName("testOne");
            fail("expected exception");
        } catch (IllegalArgumentException e){
            assertEquals("The workbook already contains this name: testOne", e.getMessage());
        }

        name2.setNameName("testTwo");

        String ref1 = "Test1!$A$1:$B$1";
        name1.setRefersToFormula(ref1);
        assertEquals(ref1, name1.getRefersToFormula());
        assertEquals("Test1", name1.getSheetName());

        String ref2 = "'Testing Named Ranges'!$A$1:$B$1";
        name1.setRefersToFormula(ref2);
        assertEquals("'Testing Named Ranges'!$A$1:$B$1", name1.getRefersToFormula());
        assertEquals("Testing Named Ranges", name1.getSheetName());

        assertEquals(-1, name1.getLocalSheetId());
        name1.setLocalSheetId(1);
        assertEquals(1, name1.getLocalSheetId());
    }

    public void testUnicodeNamedRange() {
        XSSFWorkbook workBook = new XSSFWorkbook();
        workBook.createSheet("Test");
        XSSFName name = workBook.createName();
        name.setNameName("\u03B1");
        name.setRefersToFormula("Test!$D$3:$E$8");


        XSSFWorkbook workBook2 = XSSFTestDataSamples.writeOutAndReadBack(workBook);
        XSSFName name2 = workBook2.getNameAt(0);

        assertEquals("\u03B1", name2.getNameName());
        assertEquals("Test!$D$3:$E$8", name2.getRefersToFormula());
    }

    public void testAddRemove() {
        XSSFWorkbook wb = new XSSFWorkbook();
        assertEquals(0, wb.getNumberOfNames());
        XSSFName name1 = wb.createName();
        name1.setNameName("name1");
        assertEquals(1, wb.getNumberOfNames());

        XSSFName name2 = wb.createName();
        name2.setNameName("name2");
        assertEquals(2, wb.getNumberOfNames());

        XSSFName name3 = wb.createName();
        name3.setNameName("name3");
        assertEquals(3, wb.getNumberOfNames());

        wb.removeName("name2");
        assertEquals(2, wb.getNumberOfNames());

        wb.removeName(0);
        assertEquals(1, wb.getNumberOfNames());
    }
}
