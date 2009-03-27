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

package org.apache.poi.ss.usermodel;

import junit.framework.TestCase;
import org.apache.poi.ss.ITestDataProvider;

/**
 * Tests of implementation of {@link org.apache.poi.ss.usermodel.Name}
 *
 */
public abstract class BaseTestNamedRange extends TestCase {

    /**
     * @return an object that provides test data in HSSF / XSSF specific way
     */
    protected abstract ITestDataProvider getTestDataProvider();

    public void testCreate(){
        // Create a new workbook
        Workbook wb = getTestDataProvider().createWorkbook();
        Sheet sheet1 = wb.createSheet("Test1");
        Sheet sheet2 = wb.createSheet("Testing Named Ranges");

        Name name1 = wb.createName();
        name1.setNameName("testOne");

        //setting a duplicate name should throw IllegalArgumentException
        Name name2 = wb.createName();
        try {
            name2.setNameName("testOne");
            fail("expected exception");
        } catch (IllegalArgumentException e){
            assertEquals("The workbook already contains this name: testOne", e.getMessage());
        }
        //the check for duplicates is case-insensitive
        try {
            name2.setNameName("TESTone");
            fail("expected exception");
        } catch (IllegalArgumentException e){
            assertEquals("The workbook already contains this name: TESTone", e.getMessage());
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

        assertEquals(-1, name1.getSheetIndex());
        name1.setSheetIndex(-1);
        assertEquals(-1, name1.getSheetIndex());
        try {
            name1.setSheetIndex(2);
            fail("should throw IllegalArgumentException");
        } catch(IllegalArgumentException e){
            assertEquals("Sheet index (2) is out of range (0..1)", e.getMessage());
        }

        name1.setSheetIndex(1);
        assertEquals(1, name1.getSheetIndex());

        //-1 means the name applies to the entire workbook
        name1.setSheetIndex(-1);
        assertEquals(-1, name1.getSheetIndex());
    }

    public void testUnicodeNamedRange() {
        Workbook workBook = getTestDataProvider().createWorkbook();
        workBook.createSheet("Test");
        Name name = workBook.createName();
        name.setNameName("\u03B1");
        name.setRefersToFormula("Test!$D$3:$E$8");


        Workbook workBook2 = getTestDataProvider().writeOutAndReadBack(workBook);
        Name name2 = workBook2.getNameAt(0);

        assertEquals("\u03B1", name2.getNameName());
        assertEquals("Test!$D$3:$E$8", name2.getRefersToFormula());
    }

    public void testAddRemove() {
        Workbook wb = getTestDataProvider().createWorkbook();
        assertEquals(0, wb.getNumberOfNames());
        Name name1 = wb.createName();
        name1.setNameName("name1");
        assertEquals(1, wb.getNumberOfNames());

        Name name2 = wb.createName();
        name2.setNameName("name2");
        assertEquals(2, wb.getNumberOfNames());

        Name name3 = wb.createName();
        name3.setNameName("name3");
        assertEquals(3, wb.getNumberOfNames());

        wb.removeName("name2");
        assertEquals(2, wb.getNumberOfNames());

        wb.removeName(0);
        assertEquals(1, wb.getNumberOfNames());
    }

    public void testScope() {
        Workbook wb = getTestDataProvider().createWorkbook();
        wb.createSheet();
        wb.createSheet();

        Name name;

        name = wb.createName();
        name.setNameName("aaa");
        name = wb.createName();
        try {
            name.setNameName("aaa");
            fail("Expected exception");
        } catch(Exception e){
            assertEquals("The workbook already contains this name: aaa", e.getMessage());
        }

        name = wb.createName();
        name.setSheetIndex(0);
        name.setNameName("aaa");
        name = wb.createName();
        name.setSheetIndex(0);
        try {
            name.setNameName("aaa");
            fail("Expected exception");
        } catch(Exception e){
            assertEquals("The sheet already contains this name: aaa", e.getMessage());
        }

        name = wb.createName();
        name.setSheetIndex(1);
        name.setNameName("aaa");
        name = wb.createName();
        name.setSheetIndex(1);
        try {
            name.setNameName("aaa");
            fail("Expected exception");
        } catch(Exception e){
            assertEquals("The sheet already contains this name: aaa", e.getMessage());
        }

        int cnt = 0;
        for (int i = 0; i < wb.getNumberOfNames(); i++) {
            if("aaa".equals(wb.getNameAt(i).getNameName())) cnt++;
        }
        assertEquals(3, cnt);
    }
}