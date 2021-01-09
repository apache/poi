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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;

public final class TestXSSFSheetRowGrouping {

    private static final int ROWS_NUMBER = 200;
    private static final int GROUP_SIZE = 5;

    @Test
    void test55640() {
        //long startTime = System.currentTimeMillis();
        Workbook wb = new XSSFWorkbook();
        fillData(wb);
        writeToFile(wb);

        //System.out.println("Number of groups: " + o_groupsNumber);
        //System.out.println("Execution time: " + (System.currentTimeMillis()-startTime) + " ms");
    }


    private void fillData(Workbook p_wb) {
        Sheet sheet = p_wb.createSheet("sheet123");
        sheet.setRowSumsBelow(false);

        for (int i = 0; i < ROWS_NUMBER; i++) {
            Row row = sheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(i+1);
        }

        int i = 1;
        while (i < ROWS_NUMBER) {
            int end = i+(GROUP_SIZE-2);
            int start = i;                    // natural order
//            int start = end - 1;                // reverse order
            while (start < end) {             // natural order
//                while (start >= i) {            // reverse order
                sheet.groupRow(start, end);
                //o_groupsNumber++;
                boolean collapsed = isCollapsed();
                //System.out.println("Set group " + start + "->" + end + " to " + collapsed);
                sheet.setRowGroupCollapsed(start, collapsed);
                start++;                      // natural order
//                start--;                        // reverse order
            }
            i += GROUP_SIZE;
        }
    }

    private boolean isCollapsed() {
        return Math.random() > 0.5d;
    }

    private void writeToFile(Workbook p_wb) {
//        FileOutputStream fileOut = new FileOutputStream("/tmp/55640.xlsx");
//        try {
//            p_wb.write(fileOut);
//        } finally {
//            fileOut.close();
//        }
        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(p_wb));
    }

    @Test
    void test55640reduce1() {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("sheet123");
        sheet.setRowSumsBelow(false);

        for (int i = 0; i < ROWS_NUMBER; i++) {
            Row row = sheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(i+1);
        }

        int i = 1;
        while (i < ROWS_NUMBER) {
            int end = i+(GROUP_SIZE-2);
            int start = i;                    // natural order
            while (start < end) {             // natural order
                sheet.groupRow(start, end);
                //o_groupsNumber++;
                boolean collapsed = (start % 2) != 0;
                //System.out.println("Set group " + start + "->" + end + " to " + collapsed);
                sheet.setRowGroupCollapsed(start, collapsed);
                start++;                      // natural order
            }
            i += GROUP_SIZE;
        }
        writeToFile(wb);
    }

    @Test
    void test55640_VerifyCases() {
        // NOTE: This is currently based on current behavior of POI, somehow
        // what POI returns in the calls to collapsed/hidden is not fully matching
        // the examples in the spec or I did not fully understand how POI stores the data internally...

        // all expanded
        verifyGroupCollapsed(
                // level1, level2, level3
                false, false, false,
                // collapsed:
                new Boolean[] { false, false, false, false, false},
                // hidden:
                new boolean[] { false, false, false, false, false},
                // outlineLevel
                new int[] { 1, 2, 3, 3, 3 }
                );


        // Level 1 collapsed, others expanded, should only have 4 rows, all hidden:
        verifyGroupCollapsed(
                // level1, level2, level3
                true, false, false,
                // collapsed:
                new Boolean[] { false, false, false, false, false},
                // hidden:
                new boolean[] { true, true, true, true, true},
                // outlineLevel
                new int[] { 1, 2, 3, 3, 3 }
                );

        // Level 1 and 2 collapsed, Level 3 expanded,
        verifyGroupCollapsed(
                // level1, level2, level3
                true, true, false,
                // collapsed:
                new Boolean[] { false, false, false, false, true, false},
                // hidden:
                new boolean[] { true, true, true, true, true, false},
                // outlineLevel
                new int[] { 1, 2, 3, 3, 3, 0 }
                );

        // Level 1 collapsed, Level 2 expanded, Level 3 collapsed
        verifyGroupCollapsed(
                // level1, level2, level3
                true, false, true,
                // collapsed:
                new Boolean[] { false, false, false, false, false, true},
                // hidden:
                new boolean[] { true, true, true, true, true, false},
                // outlineLevel
                new int[] { 1, 2, 3, 3, 3, 0 }
                );

        // Level 2 collapsed, others expanded:
        verifyGroupCollapsed(
                // level1, level2, level3
                false, true, false,
                // collapsed:
                new Boolean[] { false, false, false, false, false, false},
                // hidden:
                new boolean[] { false, true, true, true, true, false},
                // outlineLevel
                new int[] { 1, 2, 3, 3, 3, 0 }
                );

        // Level 3 collapsed, others expanded
        verifyGroupCollapsed(
                // level1, level2, level3
                false, false, true,
                // collapsed:
                new Boolean[] { false, false, false, false, false, true},
                // hidden:
                new boolean[] { false, false, true, true, true, false},
                // outlineLevel
                new int[] { 1, 2, 3, 3, 3, 0 }
                );

        // All collapsed
        verifyGroupCollapsed(
                // level1, level2, level3
                true, true, true,
                // collapsed:
                new Boolean[] { false, false, false, false, true, true},
                // hidden:
                new boolean[] { true, true, true, true, true, false},
                // outlineLevel
                new int[] { 1, 2, 3, 3, 3, 0 }
                );
    }


    private void verifyGroupCollapsed(boolean level1, boolean level2, boolean level3,
            Boolean[] collapsed, boolean[] hidden, int[] outlineLevel) {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("sheet123");

        for (int i = 0; i < 4; i++) {
            sheet.createRow(i);
        }

        sheet.groupRow(0, 4);
        sheet.groupRow(1, 4);
        sheet.groupRow(2, 4);

        sheet.setRowGroupCollapsed(0, level1);
        sheet.setRowGroupCollapsed(1, level2);
        sheet.setRowGroupCollapsed(2, level3);

        checkWorkbookGrouping(wb, collapsed, hidden, outlineLevel);
    }

    @Test
    void test55640_VerifyCasesSpec() {
        // NOTE: This is currently based on current behavior of POI, somehow
        // what POI returns in the calls to collapsed/hidden is not fully matching
        // the examples in the spec or I did not fully understand how POI stores the data internally...

        // all expanded
        verifyGroupCollapsedSpec(
                // level3, level2, level1
                false, false, false,
                // collapsed:
                new Boolean[] { false, false, false, false},
                // hidden:
                new boolean[] { false, false, false, false},
                // outlineLevel
                new int[] { 3, 3, 2, 1 }
                );


        verifyGroupCollapsedSpec(
                // level3, level2, level1
                false, false, true,
                // collapsed:
                new Boolean[] { false, false, false, true},
                // hidden:
                new boolean[] { true, true, true, false},
                // outlineLevel
                new int[] { 3, 3, 2, 1 }
                );

        verifyGroupCollapsedSpec(
                // level3, level2, level1
                false, true, false,
                // collapsed:
                new Boolean[] { false, false, true, false},
                // hidden:
                new boolean[] { true, true, true, false},
                // outlineLevel
                new int[] { 3, 3, 2, 1 }
                );

        verifyGroupCollapsedSpec(
                // level3, level2, level1
                false, true, true,
                // collapsed:
                new Boolean[] { false, false, true, true},
                // hidden:
                new boolean[] { true, true, true, false},
                // outlineLevel
                new int[] { 3, 3, 2, 1 }
                );
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyGroupCollapsedSpec(boolean level1, boolean level2, boolean level3,
                                          Boolean[] collapsed, boolean[] hidden, int[] outlineLevel) {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("sheet123");

        for (int i = 5; i < 9; i++) {
            sheet.createRow(i);
        }

        sheet.groupRow(5, 6);
        sheet.groupRow(5, 7);
        sheet.groupRow(5, 8);

        sheet.setRowGroupCollapsed(6, level1);
        sheet.setRowGroupCollapsed(7, level2);
        sheet.setRowGroupCollapsed(8, level3);

        checkWorkbookGrouping(wb, collapsed, hidden, outlineLevel);
    }

    private void checkWorkbookGrouping(Workbook wb, Boolean[] collapsed, boolean[] hidden, int[] outlineLevel) {
        Sheet sheet = wb.getSheetAt(0);

        assertEquals(collapsed.length, hidden.length);
        assertEquals(collapsed.length, outlineLevel.length);
        assertEquals(collapsed.length, sheet.getLastRowNum()-sheet.getFirstRowNum()+1,
            "Expected " + collapsed.length + " rows with collapsed state, but had " + (sheet.getLastRowNum()-sheet.getFirstRowNum()+1) + " rows ("
                + sheet.getFirstRowNum() + "-" + sheet.getLastRowNum() + ")");
        for(int i = sheet.getFirstRowNum(); i < sheet.getLastRowNum();i++) {
            if(collapsed[i-sheet.getFirstRowNum()] == null) {
                continue;
            }
            XSSFRow row = (XSSFRow) sheet.getRow(i);
            assertNotNull(row, "Could not read row " + i);
            assertNotNull(row.getCTRow(), "Could not read row " + i);
            assertEquals(collapsed[i - sheet.getFirstRowNum()], row.getCTRow().getCollapsed(), "Row: " + i + ": collapsed");
            assertEquals(hidden[i-sheet.getFirstRowNum()], row.getCTRow().getHidden(), "Row: " + i + ": hidden");

            assertEquals(outlineLevel[i-sheet.getFirstRowNum()], row.getCTRow().getOutlineLevel(), "Row: " + i + ": level");
        }

        writeToFile(wb);
    }

    @Test
    void test55640working() {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("sheet123");

        sheet.groupRow(1, 4);
        sheet.groupRow(2, 5);
        sheet.groupRow(3, 6);

        sheet.setRowGroupCollapsed(1, true);
        sheet.setRowGroupCollapsed(2, false);
        sheet.setRowGroupCollapsed(3, false);

        writeToFile(wb);
    }

    @Test
    void testGroupingTest() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("GroupTest.xlsx")) {

            assertEquals(31, wb.getSheetAt(0).getLastRowNum());

            // NOTE: This is currently based on current behavior of POI, somehow
            // what POI returns in the calls to collapsed/hidden is not fully matching
            // the examples in the spec or I did not fully understand how POI stores the data internally...
            checkWorkbookGrouping(wb,
                    new Boolean[]{
                            // 0-4
                            false, false, false, false, false, null, null,
                            // 7-11
                            false, false, true, true, true, null, null,
                            // 14-18
                            false, false, true, false, false, null,
                            // 20-24
                            false, false, true, true, false, null, null,
                            // 27-31
                            false, false, false, true, false},
                    new boolean[]{
                            // 0-4
                            false, false, false, false, false, false, false,
                            // 7-11
                            true, true, true, true, false, false, false,
                            // 14-18
                            true, true, false, false, false, false,
                            // 20-24
                            true, true, true, false, false, false, false,
                            // 27-31
                            true, true, true, true, false},
                    // outlineLevel
                    new int[]{
                            // 0-4
                            3, 3, 2, 1, 0, 0, 0,
                            // 7-11
                            3, 3, 2, 1, 0, 0, 0,
                            // 14-18
                            3, 3, 2, 1, 0, 0,
                            // 20-24
                            3, 3, 2, 1, 0, 0, 0,
                            // 27-31
                            3, 3, 2, 1, 0,
                    }
            );
        }
        /*
Row: 0: Level: 3 Collapsed: false Hidden: false
Row: 1: Level: 3 Collapsed: false Hidden: false
Row: 2: Level: 2 Collapsed: false Hidden: false
Row: 3: Level: 1 Collapsed: false Hidden: false
Row: 4: Level: 0 Collapsed: false Hidden: false
Row: 7: Level: 3 Collapsed: false Hidden: true
Row: 8: Level: 3 Collapsed: false Hidden: true
Row: 9: Level: 2 Collapsed: true Hidden: true
Row: 10: Level: 1 Collapsed: true Hidden: true
Row: 11: Level: 0 Collapsed: true Hidden: false
Row: 14: Level: 3 Collapsed: false Hidden: true
Row: 15: Level: 3 Collapsed: false Hidden: true
Row: 16: Level: 2 Collapsed: true Hidden: false
Row: 17: Level: 1 Collapsed: false Hidden: false
Row: 18: Level: 0 Collapsed: false Hidden: false
Row: 20: Level: 3 Collapsed: false Hidden: true
Row: 21: Level: 3 Collapsed: false Hidden: true
Row: 22: Level: 2 Collapsed: true Hidden: true
Row: 23: Level: 1 Collapsed: true Hidden: false
Row: 24: Level: 0 Collapsed: false Hidden: false
Row: 27: Level: 3 Collapsed: false Hidden: true
Row: 28: Level: 3 Collapsed: false Hidden: true
Row: 29: Level: 2 Collapsed: false Hidden: true
Row: 30: Level: 1 Collapsed: true Hidden: true
Row: 31: Level: 0 Collapsed: true Hidden: false
         */
    }
}