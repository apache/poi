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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;

/**
 * Test asserts the POI produces &lt;cols&gt; element that could be read and properly interpreted by the MS Excel.
 * For specification of the "cols" element see the chapter 3.3.1.16 of the "Office Open XML Part 4 - Markup Language Reference.pdf".
 * The specification can be downloaded at http://www.ecma-international.org/publications/files/ECMA-ST/Office%20Open%20XML%201st%20edition%20Part%204%20(PDF).zip.
 *
 * <p><em>
 * The test saves xlsx file on a disk if the system property is set:
 * -Dpoi.test.xssf.output.dir=${workspace_loc}/poi/build/xssf-output
 * </em>
 *
 */
class TestXSSFColGrouping {

	private static final POILogger logger = POILogFactory.getLogger(TestXSSFColGrouping.class);


    /**
     * Tests that POI doesn't produce "col" elements without "width" attribute.
     * POI-52186
     */
	@Test
    void testNoColsWithoutWidthWhenGrouping() throws IOException {
		try (XSSFWorkbook wb1 = new XSSFWorkbook()) {
            XSSFSheet sheet = wb1.createSheet("test");

            sheet.setColumnWidth(4, 5000);
            sheet.setColumnWidth(5, 5000);

            sheet.groupColumn((short) 4, (short) 7);
            sheet.groupColumn((short) 9, (short) 12);

            try (XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1, "testNoColsWithoutWidthWhenGrouping")) {
                sheet = wb2.getSheet("test");

                CTCols cols = sheet.getCTWorksheet().getColsArray(0);
                logger.log(POILogger.DEBUG, "test52186/cols:" + cols);
                for (CTCol col : cols.getColArray()) {
                    assertTrue(col.isSetWidth(), "Col width attribute is unset: " + col);
                }

            }
        }
    }

    /**
     * Tests that POI doesn't produce "col" elements without "width" attribute.
     * POI-52186
     */
	@Test
	void testNoColsWithoutWidthWhenGroupingAndCollapsing() throws IOException {
		try (XSSFWorkbook wb1 = new XSSFWorkbook()) {
            XSSFSheet sheet = wb1.createSheet("test");

            sheet.setColumnWidth(4, 5000);
            sheet.setColumnWidth(5, 5000);

            sheet.groupColumn((short) 4, (short) 5);

            sheet.setColumnGroupCollapsed(4, true);

            CTCols cols = sheet.getCTWorksheet().getColsArray(0);
            logger.log(POILogger.DEBUG, "test52186_2/cols:" + cols);

            try (XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1, "testNoColsWithoutWidthWhenGroupingAndCollapsing")) {
                sheet = wb2.getSheet("test");

                for (int i = 4; i <= 5; i++) {
                    assertEquals(5000, sheet.getColumnWidth(i), "Unexpected width of column " + i);
                }
                cols = sheet.getCTWorksheet().getColsArray(0);
                for (CTCol col : cols.getColArray()) {
                    assertTrue(col.isSetWidth(), "Col width attribute is unset: " + col);
                }
            }
        }
    }

    /**
     * Test the cols element is correct in case of NumericRanges.OVERLAPS_2_WRAPS
     */
	@Test
    void testMergingOverlappingCols_OVERLAPS_2_WRAPS() throws IOException {
		try (XSSFWorkbook wb1 = new XSSFWorkbook()) {
            XSSFSheet sheet = wb1.createSheet("test");

            CTCols cols = sheet.getCTWorksheet().getColsArray(0);
            CTCol col = cols.addNewCol();
            col.setMin(1 + 1);
            col.setMax(4 + 1);
            col.setWidth(20);
            col.setCustomWidth(true);

            sheet.groupColumn((short) 2, (short) 3);

            sheet.getCTWorksheet().getColsArray(0);
            logger.log(POILogger.DEBUG, "testMergingOverlappingCols_OVERLAPS_2_WRAPS/cols:" + cols);

            assertEquals(0, cols.getColArray(0).getOutlineLevel());
            assertEquals(2, cols.getColArray(0).getMin()); // 1 based
            assertEquals(2, cols.getColArray(0).getMax()); // 1 based
            assertTrue(cols.getColArray(0).getCustomWidth());

            assertEquals(1, cols.getColArray(1).getOutlineLevel());
            assertEquals(3, cols.getColArray(1).getMin()); // 1 based
            assertEquals(4, cols.getColArray(1).getMax()); // 1 based
            assertTrue(cols.getColArray(1).getCustomWidth());

            assertEquals(0, cols.getColArray(2).getOutlineLevel());
            assertEquals(5, cols.getColArray(2).getMin()); // 1 based
            assertEquals(5, cols.getColArray(2).getMax()); // 1 based
            assertTrue(cols.getColArray(2).getCustomWidth());

            assertEquals(3, cols.sizeOfColArray());

            try (XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1, "testMergingOverlappingCols_OVERLAPS_2_WRAPS")) {
                sheet = wb2.getSheet("test");

                for (int i = 1; i <= 4; i++) {
                    assertEquals(20 * 256, sheet.getColumnWidth(i), "Unexpected width of column " + i);
                }

            }
        }
    }

    /**
     * Test the cols element is correct in case of NumericRanges.OVERLAPS_1_WRAPS
     */
	@Test
    void testMergingOverlappingCols_OVERLAPS_1_WRAPS() throws IOException {
		try (XSSFWorkbook wb1 = new XSSFWorkbook()) {
            XSSFSheet sheet = wb1.createSheet("test");

            CTCols cols = sheet.getCTWorksheet().getColsArray(0);
            CTCol col = cols.addNewCol();
            col.setMin(2 + 1);
            col.setMax(4 + 1);
            col.setWidth(20);
            col.setCustomWidth(true);

            sheet.groupColumn((short) 1, (short) 5);

            cols = sheet.getCTWorksheet().getColsArray(0);
            logger.log(POILogger.DEBUG, "testMergingOverlappingCols_OVERLAPS_1_WRAPS/cols:" + cols);

            assertEquals(1, cols.getColArray(0).getOutlineLevel());
            assertEquals(2, cols.getColArray(0).getMin()); // 1 based
            assertEquals(2, cols.getColArray(0).getMax()); // 1 based
            assertFalse(cols.getColArray(0).getCustomWidth());

            assertEquals(1, cols.getColArray(1).getOutlineLevel());
            assertEquals(3, cols.getColArray(1).getMin()); // 1 based
            assertEquals(5, cols.getColArray(1).getMax()); // 1 based
            assertTrue(cols.getColArray(1).getCustomWidth());

            assertEquals(1, cols.getColArray(2).getOutlineLevel());
            assertEquals(6, cols.getColArray(2).getMin()); // 1 based
            assertEquals(6, cols.getColArray(2).getMax()); // 1 based
            assertFalse(cols.getColArray(2).getCustomWidth());

            assertEquals(3, cols.sizeOfColArray());

            try (XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1, "testMergingOverlappingCols_OVERLAPS_1_WRAPS")) {
                sheet = wb2.getSheet("test");

                for (int i = 2; i <= 4; i++) {
                    assertEquals(20 * 256, sheet.getColumnWidth(i), "Unexpected width of column " + i);
                }

            }
        }
    }

    /**
     * Test the cols element is correct in case of NumericRanges.OVERLAPS_1_MINOR
     */
	@Test
    void testMergingOverlappingCols_OVERLAPS_1_MINOR() throws IOException {
		try (XSSFWorkbook wb1 = new XSSFWorkbook()) {
            XSSFSheet sheet = wb1.createSheet("test");

            CTCols cols = sheet.getCTWorksheet().getColsArray(0);
            CTCol col = cols.addNewCol();
            col.setMin(2 + 1);
            col.setMax(4 + 1);
            col.setWidth(20);
            col.setCustomWidth(true);

            sheet.groupColumn((short) 3, (short) 5);

            cols = sheet.getCTWorksheet().getColsArray(0);
            logger.log(POILogger.DEBUG, "testMergingOverlappingCols_OVERLAPS_1_MINOR/cols:" + cols);

            assertEquals(0, cols.getColArray(0).getOutlineLevel());
            assertEquals(3, cols.getColArray(0).getMin()); // 1 based
            assertEquals(3, cols.getColArray(0).getMax()); // 1 based
            assertTrue(cols.getColArray(0).getCustomWidth());

            assertEquals(1, cols.getColArray(1).getOutlineLevel());
            assertEquals(4, cols.getColArray(1).getMin()); // 1 based
            assertEquals(5, cols.getColArray(1).getMax()); // 1 based
            assertTrue(cols.getColArray(1).getCustomWidth());

            assertEquals(1, cols.getColArray(2).getOutlineLevel());
            assertEquals(6, cols.getColArray(2).getMin()); // 1 based
            assertEquals(6, cols.getColArray(2).getMax()); // 1 based
            assertFalse(cols.getColArray(2).getCustomWidth());

            assertEquals(3, cols.sizeOfColArray());

            try (XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1, "testMergingOverlappingCols_OVERLAPS_1_MINOR")) {
                sheet = wb2.getSheet("test");

                for (int i = 2; i <= 4; i++) {
                    assertEquals(20 * 256L, sheet.getColumnWidth(i), "Unexpected width of column " + i);
                }
                assertEquals(sheet.getDefaultColumnWidth() * 256L, sheet.getColumnWidth(5), "Unexpected width of column " + 5);

            }
        }
    }

    /**
     * Test the cols element is correct in case of NumericRanges.OVERLAPS_2_MINOR
     */
	@Test
    void testMergingOverlappingCols_OVERLAPS_2_MINOR() throws IOException {
		try (XSSFWorkbook wb1 = new XSSFWorkbook()) {
            XSSFSheet sheet = wb1.createSheet("test");

            CTCols cols = sheet.getCTWorksheet().getColsArray(0);
            CTCol col = cols.addNewCol();
            col.setMin(2 + 1);
            col.setMax(4 + 1);
            col.setWidth(20);
            col.setCustomWidth(true);

            sheet.groupColumn((short) 1, (short) 3);

            cols = sheet.getCTWorksheet().getColsArray(0);
            logger.log(POILogger.DEBUG, "testMergingOverlappingCols_OVERLAPS_2_MINOR/cols:" + cols);

            assertEquals(1, cols.getColArray(0).getOutlineLevel());
            assertEquals(2, cols.getColArray(0).getMin()); // 1 based
            assertEquals(2, cols.getColArray(0).getMax()); // 1 based
            assertFalse(cols.getColArray(0).getCustomWidth());

            assertEquals(1, cols.getColArray(1).getOutlineLevel());
            assertEquals(3, cols.getColArray(1).getMin()); // 1 based
            assertEquals(4, cols.getColArray(1).getMax()); // 1 based
            assertTrue(cols.getColArray(1).getCustomWidth());

            assertEquals(0, cols.getColArray(2).getOutlineLevel());
            assertEquals(5, cols.getColArray(2).getMin()); // 1 based
            assertEquals(5, cols.getColArray(2).getMax()); // 1 based
            assertTrue(cols.getColArray(2).getCustomWidth());

            assertEquals(3, cols.sizeOfColArray());

            try (XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1, "testMergingOverlappingCols_OVERLAPS_2_MINOR")) {
                sheet = wb2.getSheet("test");

                for (int i = 2; i <= 4; i++) {
                    assertEquals(20 * 256L, sheet.getColumnWidth(i), "Unexpected width of column " + i);
                }
                assertEquals(sheet.getDefaultColumnWidth() * 256L, sheet.getColumnWidth(1), "Unexpected width of column " + 1);

            }
        }
    }

}
