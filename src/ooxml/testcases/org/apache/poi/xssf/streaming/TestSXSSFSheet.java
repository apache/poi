/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xssf.streaming;

import org.apache.poi.ss.usermodel.BaseTestSheet;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class TestSXSSFSheet extends BaseTestSheet {

    public TestSXSSFSheet() {
        super(SXSSFITestDataProvider.instance);
    }


    @Override
    public void tearDown(){
        SXSSFITestDataProvider.instance.cleanup();
    }


    /**
     * cloning of sheets is not supported in SXSSF
     */
    @Override
    public void testCloneSheet() {
        try {
            super.testCloneSheet();
            fail("expected exception");
        } catch (RuntimeException e){
            assertEquals("NotImplemented", e.getMessage());
        }
    }

    @Override
    public void testCloneSheetMultipleTimes() {
        try {
            super.testCloneSheetMultipleTimes();
            fail("expected exception");
        } catch (RuntimeException e){
            assertEquals("NotImplemented", e.getMessage());
        }
    }
    /**
     * shifting rows is not supported in SXSSF
     */
    @Override
    public void testShiftMerged(){
        try {
            super.testShiftMerged();
            fail("expected exception");
        } catch (RuntimeException e){
            assertEquals("NotImplemented", e.getMessage());
        }
    }

    /**
     *  Bug 35084: cloning cells with formula
     *
     *  The test is disabled because cloning of sheets is not supported in SXSSF
     */
    @Override
    public void test35084(){
        try {
            super.test35084();
            fail("expected exception");
        } catch (RuntimeException e){
            assertEquals("NotImplemented", e.getMessage());
        }
    }

    @Override
    public void testDefaultColumnStyle() {
        //TODO column styles are not yet supported by XSSF
    }

    public void testOverrideFlushedRows() {
        Workbook wb = new SXSSFWorkbook(3);
        Sheet sheet = wb.createSheet();

        sheet.createRow(1);
        sheet.createRow(2);
        sheet.createRow(3);
        sheet.createRow(4);
        try {
            sheet.createRow(1);
            fail("expected exception");
        } catch (Throwable e){
            assertEquals("Attempting to write a row[1] in the range [0,1] that is already written to disk.", e.getMessage());
        }

    }

    public void testOverrideRowsInTemplate() {
        XSSFWorkbook template = new XSSFWorkbook();
        template.createSheet().createRow(1);

        Workbook wb = new SXSSFWorkbook(template);
        Sheet sheet = wb.getSheetAt(0);

        try {
            sheet.createRow(1);
            fail("expected exception");
        } catch (Throwable e){
            assertEquals("Attempting to write a row[1] in the range [0,1] that is already written to disk.", e.getMessage());
        }
        try {
            sheet.createRow(0);
            fail("expected exception");
        } catch (Throwable e){
            assertEquals("Attempting to write a row[0] in the range [0,1] that is already written to disk.", e.getMessage());
        }
        sheet.createRow(2);

    }
}
