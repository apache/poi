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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.RecordFormatException;
import org.junit.Test;

import junit.framework.AssertionFailedError;

/**
 * @author aviks
 * 
 * This testcase contains tests for bugs that are yet to be fixed. Therefore,
 * the standard ant test target does not run these tests. Run this testcase with
 * the single-test target. The names of the tests usually correspond to the
 * Bugzilla id's PLEASE MOVE tests from this class to TestBugs once the bugs are
 * fixed, so that they are then run automatically.
 */
public final class TestUnfixedBugs {

    @Test
	public void test43493() {
		// Has crazy corrupt sub-records on
		// a EmbeddedObjectRefSubRecord
		try {
			HSSFTestDataSamples.openSampleWorkbook("43493.xls");
		} catch (RecordFormatException e) {
			if (e.getCause().getCause() instanceof ArrayIndexOutOfBoundsException) {
				throw new AssertionFailedError("Identified bug 43493");
			}
			throw e;
		}
	}

	/**
	 * Note - some parts of this bug have been fixed, and have been
	 * transfered over to {@link TestBugs#bug49612_part()}
	 */
    @Test
    public void test49612() throws IOException {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("49612.xls");
        HSSFSheet sh = wb.getSheetAt(0);
        HSSFRow row = sh.getRow(0);
        HSSFCell c1 = row.getCell(2);
        HSSFCell d1 = row.getCell(3);
        HSSFCell e1 = row.getCell(2);

        assertEquals("SUM(BOB+JIM)", c1.getCellFormula());

        // Problem 1: Filename missing, see bug #56742
        assertEquals("SUM('49612.xls'!BOB+'49612.xls'!JIM)", d1.getCellFormula());

        //Problem 2: Filename missing, see bug #56742
        //junit.framework.ComparisonFailure:
        //Expected :SUM('49612.xls'!BOB+'49612.xls'!JIM)
        //Actual   :SUM(BOB+JIM)
        assertEquals("SUM('49612.xls'!BOB+'49612.xls'!JIM)", e1.getCellFormula());
        
        // Problem 3 - fixed and transfered
        wb.close();
    }

    @Test
    public void testFormulaRecordAggregate_1() throws Exception {
        // fails at formula "=MEHRFACH.OPERATIONEN(E$3;$B$5;$D4)"
        Workbook wb = HSSFTestDataSamples.openSampleWorkbook("44958_1.xls");
        try {
            for(int i = 0;i < wb.getNumberOfSheets();i++) {
                Sheet sheet = wb.getSheetAt(i);
                assertNotNull(wb.getSheet(sheet.getSheetName()));
                sheet.groupColumn((short) 4, (short) 5);
                sheet.setColumnGroupCollapsed(4, true);
                sheet.setColumnGroupCollapsed(4, false);
                
                for(Row row : sheet) {
                    for(Cell cell : row) {
                        try {
                            cell.toString();
                        } catch (Exception e) {
                            throw new Exception("While handling: " + sheet.getSheetName() + "/" + row.getRowNum() + "/" + cell.getColumnIndex(), e);
                        }
                    }
                }
            }
        } finally {
            wb.close();
        }
    }

    @Test
    public void testFormulaRecordAggregate() throws Exception {
        // fails at formula "=MEHRFACH.OPERATIONEN(E$3;$B$5;$D4)"
        Workbook wb = HSSFTestDataSamples.openSampleWorkbook("44958.xls");
        try {
            for(int i = 0;i < wb.getNumberOfSheets();i++) {
                Sheet sheet = wb.getSheetAt(i);
                assertNotNull(wb.getSheet(sheet.getSheetName()));
                sheet.groupColumn((short) 4, (short) 5);
                sheet.setColumnGroupCollapsed(4, true);
                sheet.setColumnGroupCollapsed(4, false);
                
                for(Row row : sheet) {
                    for(Cell cell : row) {
                        try {
                            cell.toString();
                        } catch (Exception e) {
                            throw new Exception("While handling: " + sheet.getSheetName() + "/" + row.getRowNum() + "/" + cell.getColumnIndex(), e);
                        }
                    }
                }
            } 
        } finally {
            wb.close();
        }
    }

    @Test
    public void testBug57074() throws IOException {
        Workbook wb = HSSFTestDataSamples.openSampleWorkbook("57074.xls");
        Sheet sheet = wb.getSheet("Sheet1");
        Row row = sheet.getRow(0);
        Cell cell = row.getCell(0);
        
        HSSFColor bgColor = (HSSFColor) cell.getCellStyle().getFillBackgroundColorColor();
        String bgColorStr = bgColor.getTriplet()[0]+", "+bgColor.getTriplet()[1]+", "+bgColor.getTriplet()[2];
        //System.out.println(bgColorStr);
        assertEquals("215, 228, 188", bgColorStr);

        HSSFColor fontColor = (HSSFColor) cell.getCellStyle().getFillForegroundColorColor();
        String fontColorStr = fontColor.getTriplet()[0]+", "+fontColor.getTriplet()[1]+", "+fontColor.getTriplet()[2];
        //System.out.println(fontColorStr);
        assertEquals("0, 128, 128", fontColorStr);
        wb.close();
    }
}
