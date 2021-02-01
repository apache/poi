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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

/**
 * @author aviks
 *
 * This testcase contains tests for bugs that are yet to be fixed. Therefore,
 * the standard ant test target does not run these tests. Run this testcase with
 * the single-test target. The names of the tests usually correspond to the
 * Bugzilla id's PLEASE MOVE tests from this class to TestBugs once the bugs are
 * fixed, so that they are then run automatically.
 */
final class TestUnfixedBugs {
    @Test
    void testFormulaRecordAggregate_1() throws Exception {
        // fails at formula "=MEHRFACH.OPERATIONEN(E$3;$B$5;$D4)"
        try (Workbook wb = HSSFTestDataSamples.openSampleWorkbook("44958_1.xls")) {
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet sheet = wb.getSheetAt(i);
                assertNotNull(wb.getSheet(sheet.getSheetName()));
                sheet.groupColumn((short) 4, (short) 5);
                sheet.setColumnGroupCollapsed(4, true);
                sheet.setColumnGroupCollapsed(4, false);

                for (Row row : sheet) {
                    for (Cell cell : row) {
                        try {
                            assertNotNull(cell.toString());
                        } catch (Exception e) {
                            throw new Exception("While handling: " + sheet.getSheetName() + "/" + row.getRowNum() + "/" + cell.getColumnIndex(), e);
                        }
                    }
                }
            }
        }
    }

    @Test
    void testFormulaRecordAggregate() throws Exception {
        // fails at formula "=MEHRFACH.OPERATIONEN(E$3;$B$5;$D4)"
        try (Workbook wb = HSSFTestDataSamples.openSampleWorkbook("44958.xls")) {
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet sheet = wb.getSheetAt(i);
                assertNotNull(wb.getSheet(sheet.getSheetName()));
                sheet.groupColumn((short) 4, (short) 5);
                sheet.setColumnGroupCollapsed(4, true);
                sheet.setColumnGroupCollapsed(4, false);

                for (Row row : sheet) {
                    for (Cell cell : row) {
                        try {
                            assertNotNull(cell.toString());
                        } catch (Exception e) {
                            throw new Exception("While handling: " + sheet.getSheetName() + "/" + row.getRowNum() + "/" + cell.getColumnIndex(), e);
                        }
                    }
                }
            }
        }
    }

    @Test
    void testBug57074() throws IOException {
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

    @Test
    void testBug62242() {
        CellFormat cfUK  = CellFormat.getInstance("_ * #,##0.00_ ;_ * \\-#,##0.00_ ;_ * \"-\"??_ ;_ @_");
        assertEquals("    -   ", cfUK.apply((double) 0).text);
    }

    @Test
    void testDataFormattingWithQuestionMarkBug62242() {
        // The question mark in the format should be replaced by blanks, but
        // this is currently not handled when producing the Java formatting and
        // so we end up with a trailing zero here
        CellFormat cfUK  = CellFormat.getInstance("??");
        assertEquals("  ", cfUK.apply((double) 0).text);
    }

    @Test
    void testDataFormattingWithQuestionMarkAndPoundBug62242() {
        char pound = '\u00A3';

        // The question mark in the format should be replaced by blanks, but
        // this is currently not handled when producing the Java formatting and
        // so we end up with a trailing zero here
        CellFormat cfUK  = CellFormat.getInstance("_-[$£-809]* \"-\"??_-");
        assertEquals(" "+pound+"   -   ", cfUK.apply((double) 0).text);

        cfUK  = CellFormat.getInstance("_-[$£-809]* \"-\"??_-a");
        assertEquals(" "+pound+"   -   a", cfUK.apply((double) 0).text);
    }
}
