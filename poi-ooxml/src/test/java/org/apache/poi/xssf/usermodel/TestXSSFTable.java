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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

import static org.junit.jupiter.api.Assertions.*;

public final class TestXSSFTable {

    @Test
    void bug56274() throws IOException {
        // read sample file
        try (XSSFWorkbook wb1 = XSSFTestDataSamples.openSampleWorkbook("56274.xlsx")) {

            // read the original sheet header order
            XSSFRow row = wb1.getSheetAt(0).getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell cell : row) {
                headers.add(cell.getStringCellValue());
            }

            // save the worksheet as-is using SXSSF
            File outputFile = TempFile.createTempFile("poi-56274", ".xlsx");
            SXSSFWorkbook outputWorkbook = new SXSSFWorkbook(wb1);
            FileOutputStream fos = new FileOutputStream(outputFile);
            outputWorkbook.write(fos);
            fos.close();
            outputWorkbook.close();

            // re-read the saved file and make sure headers in the xml are in the original order
            FileInputStream fis = new FileInputStream(outputFile);
            XSSFWorkbook wb2 = new XSSFWorkbook(fis);
            fis.close();
            CTTable ctTable = wb2.getSheetAt(0).getTables().get(0).getCTTable();
            CTTableColumn[] ctTableColumnArray = ctTable.getTableColumns().getTableColumnArray();

            assertEquals(headers.size(), ctTableColumnArray.length,
                "number of headers in xml table should match number of header cells in worksheet");
            for (int i = 0; i < headers.size(); i++) {
                assertEquals(headers.get(i), ctTableColumnArray[i].getName(),
                    "header name in xml table should match number of header cells in worksheet");
            }
            assertTrue(outputFile.delete());
            wb2.close();
        }
    }

    @Test
    void testCTTableStyleInfo() throws IOException {
        try (XSSFWorkbook outputWorkbook = new XSSFWorkbook()) {
            XSSFSheet sheet = outputWorkbook.createSheet();

            //Create
            XSSFTable outputTable = sheet.createTable(null);
            outputTable.setDisplayName("Test");
            CTTable outputCTTable = outputTable.getCTTable();

            //Style configurations
            CTTableStyleInfo outputStyleInfo = outputCTTable.addNewTableStyleInfo();
            outputStyleInfo.setName("TableStyleLight1");
            outputStyleInfo.setShowColumnStripes(false);
            outputStyleInfo.setShowRowStripes(true);

            try (XSSFWorkbook inputWorkbook = XSSFTestDataSamples.writeOutAndReadBack(outputWorkbook)) {
                List<XSSFTable> tables = inputWorkbook.getSheetAt(0).getTables();
                assertEquals(1, tables.size(), "Tables number");

                XSSFTable inputTable = tables.get(0);
                assertEquals(outputTable.getDisplayName(), inputTable.getDisplayName(), "Table display name");

                CTTableStyleInfo inputStyleInfo = inputTable.getCTTable().getTableStyleInfo();
                assertEquals(outputStyleInfo.getName(), inputStyleInfo.getName(), "Style name");
                assertEquals(outputStyleInfo.getShowColumnStripes(), inputStyleInfo.getShowColumnStripes(), "Show column stripes");
                assertEquals(outputStyleInfo.getShowRowStripes(), inputStyleInfo.getShowRowStripes(), "Show row stripes");
            }
        }
    }

    @Test
    void findColumnIndex() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {

            XSSFTable table = wb.getTable("\\_Prime.1");
            assertNotNull(table);
            assertEquals(0, table.findColumnIndex("calc='#*'#"), "column header has special escaped characters");
            assertEquals(1, table.findColumnIndex("Name"));
            assertEquals(2, table.findColumnIndex("Number"));

            assertEquals(2, table.findColumnIndex("NuMbEr"), "case insensitive");

            // findColumnIndex should return -1 if no column header name matches
            assertEquals(-1, table.findColumnIndex(null));
            assertEquals(-1, table.findColumnIndex(""));
            assertEquals(-1, table.findColumnIndex("one"));
        }
    }

    @Test
    void findColumnIndexIsRelativeToTableNotSheet() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("DataTableCities.xlsx")) {
            XSSFTable table = wb.getTable("SmallCity");

            // Make sure that XSSFTable.findColumnIndex returns the column index relative to the first
            // column in the table, not the column number in the sheet
            assertEquals(0, table.findColumnIndex("City")); // column I in worksheet but 0th column in table
            assertEquals(1, table.findColumnIndex("Latitude"));
            assertEquals(2, table.findColumnIndex("Longitude"));
            assertEquals(3, table.findColumnIndex("Population"));
        }
    }

    @Test
    void getSheetName() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertEquals("Table", table.getSheetName());
        }
    }

    @Test
    void isHasTotalsRow() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertFalse(table.getTotalsRowCount() > 0);
        }
    }

    @Test
    void getStartColIndex() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertEquals(0, table.getStartColIndex());
        }
    }

    @Test
    void getEndColIndex() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertEquals(2, table.getEndColIndex());
        }
    }

    @Test
    void getStartRowIndex() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertEquals(0, table.getStartRowIndex());
        }
    }

    @Test
    void getEndRowIndex() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertEquals(6, table.getEndRowIndex());
        }
    }

    @Test
    void getStartCellReference() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertEquals(new CellReference("A1"), table.getStartCellReference());
        }
    }

    @Test
    void getEndCellReference() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertEquals(new CellReference("C7"), table.getEndCellReference());
        }
    }

    @Test
    void getEndCellReferenceFromSingleCellTable() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("SingleCellTable.xlsx")) {
            XSSFTable table = wb.getTable("Table3");
            assertEquals(new CellReference("A2"), table.getEndCellReference());
        }
    }

    @Test
    void getColumnCount() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertEquals(3, table.getColumnCount());
        }
    }

    @Test
    void getAndSetDisplayName() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertEquals("\\_Prime.1", table.getDisplayName());

            table.setDisplayName("Display name");
            assertEquals("Display name", table.getDisplayName());
            assertEquals("\\_Prime.1", table.getName()); // name and display name are different
        }
    }

    @Test
    void getCellReferences() throws IOException {
        // make sure that cached start and end cell references
        // can be synchronized with the underlying CTTable
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sh = wb.createSheet();
            XSSFTable table = sh.createTable(null);
            assertNotNull(table.getDisplayName());
            assertNotNull(table.getCTTable().getDisplayName());
            CTTable ctTable = table.getCTTable();
            ctTable.setRef("B2:E8");

            assertEquals(new CellReference("B2"), table.getStartCellReference());
            assertEquals(new CellReference("E8"), table.getEndCellReference());

            // At this point start and end cell reference are cached
            // and may not follow changes to the underlying CTTable
            ctTable.setRef("C1:M3");

            assertEquals(new CellReference("B2"), table.getStartCellReference());
            assertEquals(new CellReference("E8"), table.getEndCellReference());

            // Force a synchronization between CTTable and XSSFTable
            // start and end cell references
            table.updateReferences();

            assertEquals(new CellReference("C1"), table.getStartCellReference());
            assertEquals(new CellReference("M3"), table.getEndCellReference());
        }
    }

    @Test
    void getRowCount() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sh = wb.createSheet();
            XSSFTable table = sh.createTable(null);
            CTTable ctTable = table.getCTTable();

            assertEquals(0, table.getRowCount());

            ctTable.setRef("B2:B2");
            // update cell references to clear the cache
            table.updateReferences();
            assertEquals(1, table.getRowCount());

            ctTable.setRef("B2:B12");
            // update cell references to clear the cache
            table.updateReferences();
            assertEquals(11, table.getRowCount());
        }
    }

    @Test
    void testGetDataRowCount() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sh = wb.createSheet();
            AreaReference tableArea = new AreaReference("B2:B6", wb.getSpreadsheetVersion());
            XSSFTable table = sh.createTable(tableArea);

            assertEquals(5, table.getRowCount()); // includes column header
            assertEquals(4, table.getDataRowCount());

            table.setArea(new AreaReference("B2:B7", wb.getSpreadsheetVersion()));

            assertEquals(6, table.getRowCount());
            assertEquals(5, table.getDataRowCount());
        }
    }

    @Test
    void testSetDataRowCount() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sh = wb.createSheet();

            // 1 header row + 1 data row
            AreaReference tableArea = new AreaReference("C10:C11", wb.getSpreadsheetVersion());
            XSSFTable table = sh.createTable(tableArea);

            assertEquals(2, table.getRowCount()); // includes all data and header/footer rows

            assertEquals(1, table.getHeaderRowCount());
            assertEquals(1, table.getDataRowCount());
            assertEquals(0, table.getTotalsRowCount());

            table.setDataRowCount(5);

            assertEquals(6, table.getRowCount());

            assertEquals(1, table.getHeaderRowCount());
            assertEquals(5, table.getDataRowCount());
            assertEquals(0, table.getTotalsRowCount());

            assertEquals("C10:C15", table.getArea().formatAsString());
        }
    }

    @Test
    void testCreateTableIds() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();

            AreaReference reference1 = wb.getCreationHelper().createAreaReference(
                    new CellReference(0, 0), new CellReference(2, 2));

            XSSFTable table1 = sheet.createTable(reference1);
            assertEquals("A1:C3", table1.getCTTable().getRef());
            assertNotNull(table1.getDisplayName());
            assertNotNull(table1.getCTTable().getDisplayName());

            assertEquals(1, table1.getCTTable().getTableColumns().getTableColumnArray(0).getId());
            assertEquals(2, table1.getCTTable().getTableColumns().getTableColumnArray(1).getId());
            assertEquals(3, table1.getCTTable().getTableColumns().getTableColumnArray(2).getId());

            assertEquals(1, table1.getCTTable().getId());

            AreaReference reference2 = wb.getCreationHelper().createAreaReference(
                    new CellReference(10, 10), new CellReference(12, 12));

            XSSFTable table2 = sheet.createTable(reference2);
            assertEquals("K11:M13", table2.getCTTable().getRef());

            // these IDs duplicate those from table1 and may be cause of https://bz.apache.org/bugzilla/show_bug.cgi?id=62906
            assertEquals(1, table2.getCTTable().getTableColumns().getTableColumnArray(0).getId());
            assertEquals(2, table2.getCTTable().getTableColumns().getTableColumnArray(1).getId());
            assertEquals(3, table2.getCTTable().getTableColumns().getTableColumnArray(2).getId());

            assertEquals(2, table2.getCTTable().getId());
        }
    }

    @Test
    void testSetArea() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sh = wb.createSheet();

            AreaReference tableArea = new AreaReference("B10:D12", wb.getSpreadsheetVersion());
            XSSFTable table = sh.createTable(tableArea);

            assertEquals(3, table.getColumnCount());
            assertEquals(3, table.getRowCount());

            // move table without resizing, shouldn't change row or column count
            AreaReference tableArea2 = new AreaReference("B11:D13", wb.getSpreadsheetVersion());
            table.setArea(tableArea2);

            assertEquals(3, table.getColumnCount());
            assertEquals(3, table.getRowCount());

            // increase size by 1 row and 1 column
            AreaReference tableArea3 = new AreaReference("B11:E14", wb.getSpreadsheetVersion());
            table.setArea(tableArea3);

            assertEquals(4, table.getColumnCount());
            assertEquals(4, table.getRowCount());

            // reduce size by 2 rows and 2 columns
            AreaReference tableArea4 = new AreaReference("C12:D13", wb.getSpreadsheetVersion());
            table.setArea(tableArea4);

            assertEquals(2, table.getColumnCount());
            assertEquals(2, table.getRowCount());
        }
    }

    @Test
    void testCreateColumn() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sh = wb.createSheet();

            AreaReference tableArea = new AreaReference("A2:A3", wb.getSpreadsheetVersion());
            XSSFTable table = sh.createTable(tableArea);

            assertEquals(1, table.getColumnCount());
            assertEquals(2, table.getRowCount());

            // add columns
            XSSFTableColumn c1 = table.getColumns().get(0);
            XSSFTableColumn cB = table.createColumn("Column B");
            XSSFTableColumn cD = table.createColumn("Column D");
            XSSFTableColumn cC = table.createColumn("Column C", 2); // add between B and D
            table.updateReferences();
            table.updateHeaders();

            assertEquals(4, table.getColumnCount());
            assertEquals(2, table.getRowCount());

            // column IDs start at 1, and increase in the order columns are added (see bug #62740)
            assertEquals(1, c1.getId(), "Column c ID");
            assertTrue  (c1.getId() < cB.getId(), "Column B ID");
            assertTrue  (cB.getId() < cD.getId(), "Column D ID");
            assertTrue  (cD.getId() < cC.getId(), "Column C ID");
            // generated name
            assertEquals("Column 1", table.getColumns().get(0).getName());
            assertEquals("Column B", table.getColumns().get(1).getName());
            assertEquals("Column C", table.getColumns().get(2).getName());
            assertEquals("Column D", table.getColumns().get(3).getName());
        }
    }

    @Test
    void testCreateColumnInvalidIndex() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sh = wb.createSheet();
            AreaReference tableArea = new AreaReference("D2:D3", wb.getSpreadsheetVersion());
            XSSFTable table = sh.createTable(tableArea);

            // add columns
            table.createColumn("Column 2", 1);
            // out of bounds
            assertThrows(IllegalArgumentException.class, () -> table.createColumn("Column 3", 3));
        }
    }

    @Test
    void testDifferentHeaderTypes() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("TablesWithDifferentHeaders.xlsx")) {
            assertEquals(3, wb.getNumberOfSheets());
            XSSFSheet s;
            XSSFTable t;

            // TODO Nicer column fetching

            s = wb.getSheet("IntHeaders");
            assertEquals(1, s.getTables().size());
            t = s.getTables().get(0);
            assertEquals("A1:B2", t.getCellReferences().formatAsString());
            assertEquals("12", t.getCTTable().getTableColumns().getTableColumnArray(0).getName());
            assertEquals("34", t.getCTTable().getTableColumns().getTableColumnArray(1).getName());

            s = wb.getSheet("FloatHeaders");
            assertEquals(1, s.getTables().size());
            t = s.getTables().get(0);
            assertEquals("A1:B2", t.getCellReferences().formatAsString());
            assertEquals("12.34", t.getCTTable().getTableColumns().getTableColumnArray(0).getName());
            assertEquals("34.56", t.getCTTable().getTableColumns().getTableColumnArray(1).getName());

            s = wb.getSheet("NoExplicitHeaders");
            assertEquals(1, s.getTables().size());
            t = s.getTables().get(0);
            assertEquals("A1:B3", t.getCellReferences().formatAsString());
            assertEquals("Column1", t.getCTTable().getTableColumns().getTableColumnArray(0).getName());
            assertEquals("Column2", t.getCTTable().getTableColumns().getTableColumnArray(1).getName());
        }
    }

    @Test
    void testNumericCellsInTable() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet s = wb.createSheet();

            // Create some cells, some numeric, some not
            Cell c1 = s.createRow(0).createCell(0);
            Cell c2 = s.getRow(0).createCell(1);
            Cell c3 = s.getRow(0).createCell(2);
            Cell c4 = s.createRow(1).createCell(0);
            Cell c5 = s.getRow(1).createCell(1);
            Cell c6 = s.getRow(1).createCell(2);
            c1.setCellValue(12);
            c2.setCellValue(34.56);
            c3.setCellValue("ABCD");
            c4.setCellValue("AB");
            c5.setCellValue("CD");
            c6.setCellValue("EF");

            // Setting up the table
            XSSFTable t = s.createTable(new AreaReference("A1:C3", wb.getSpreadsheetVersion()));
            t.setName("TableTest");
            t.setDisplayName("CT_Table_Test");
            t.createColumn("Column 1");
            t.createColumn("Column 2");
            t.createColumn("Column 3");
            t.setCellReferences(wb.getCreationHelper().createAreaReference(
                    new CellReference(c1), new CellReference(c6)
            ));

            // Save and re-load
            try (XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb)) {
                s = wb2.getSheetAt(0);

                // Check
                assertEquals(1, s.getTables().size());
                t = s.getTables().get(0);
                assertEquals("A1", t.getStartCellReference().formatAsString());
                assertEquals("C2", t.getEndCellReference().formatAsString());

                // TODO Nicer column fetching
                assertEquals("12", t.getCTTable().getTableColumns().getTableColumnArray(0).getName());
                assertEquals("34.56", t.getCTTable().getTableColumns().getTableColumnArray(1).getName());
                assertEquals("ABCD", t.getCTTable().getTableColumns().getTableColumnArray(2).getName());
            }
        }
    }

    @Test
    void testSetDisplayName() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();

            AreaReference reference1 = wb.getCreationHelper().createAreaReference(
                    new CellReference(0, 0), new CellReference(2, 2));

            XSSFTable table1 = sheet.createTable(reference1);
            table1.setDisplayName("TableTest");
            assertEquals("TableTest", table1.getDisplayName());
            assertEquals("TableTest", table1.getCTTable().getDisplayName());
        }
    }

    @Test
    void testSetDisplayNameNull() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();

            AreaReference reference1 = wb.getCreationHelper().createAreaReference(
                    new CellReference(0, 0), new CellReference(2, 2));

            XSSFTable table1 = sheet.createTable(reference1);
            assertThrows(IllegalArgumentException.class, () -> table1.setDisplayName(null));
        }
    }

    @Test
    void testSetDisplayNameEmpty() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();

            AreaReference reference1 = wb.getCreationHelper().createAreaReference(
                    new CellReference(0, 0), new CellReference(2, 2));

            XSSFTable table1 = sheet.createTable(reference1);
            assertThrows(IllegalArgumentException.class, () -> table1.setDisplayName(""));
        }
    }

    /**
     * Delete table2, and create a named range in sheet0; it should automatically be assigned the name "Table4"
     */
    @Test
    void testBug63401And62906() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet0 = workbook.createSheet();
            XSSFTable table = addTable(sheet0, 3, 0, 2, 2);
            assertNotNull(table);

            // final String procName = "testXSSFTableGetName";
            // final String name = table.getName();
            // System.out.printf(Locale.ROOT, "%s: table.getName=%s%n", procName, name);
        }
    }

    @Test
    void testBug65669() throws IOException {
        String[] testValues = new String[] {"C'olumn", "Column"};
        for (String testValue : testValues) {
            try (XSSFWorkbook wb = new XSSFWorkbook()) {
                XSSFSheet sheet = wb.createSheet();

                // Set the values for the table
                for (int i = 0; i < 3; i++) {
                    // Create row
                    Row row = sheet.createRow(i);
                    for (int j = 0; j < 3; j++) {
                        // Create cell
                        Cell cell = row.createCell(j);
                        if (i == 0) {
                            final String columnName = testValue + (j + 1);
                            cell.setCellValue(columnName);
                        } else {
                            if (j != 2) {
                                cell.setCellValue((i + 1.0) * (j + 1.0));
                            }
                        }
                    }
                }

                // Create Table
                AreaReference reference = wb.getCreationHelper().createAreaReference(
                        new CellReference(0, 0), new CellReference(2, 2));
                XSSFTable table = sheet.createTable(reference);
                table.setName("Table1");
                table.setDisplayName("Table1");
                for (int i = 1; i < 3; i++) {
                    Cell cell = sheet.getRow(i).getCell(2);
                    assertNotNull(cell);
                    cell.setCellFormula("Table1[[#This Row],[" + testValue + "1]]");
                }
            }
        }
    }

    private static XSSFTable addTable(XSSFSheet sheet,int nRow, int nCol, int nNumRows, int nNumCols) {
        for (int i = 0; i < nNumRows; i++) {
            XSSFRow row = sheet.createRow(i + nRow);
            for (int j = 0; j < nNumCols; j++) {
                XSSFCell localXSSFCell = row.createCell(j + nCol);
                if (i == 0) {
                    localXSSFCell.setCellValue(String.format(Locale.ROOT, "Col%d", j + 1));
                } else {
                    localXSSFCell.setCellValue(String.format(Locale.ROOT, "(%d,%d)", i + 1, j + 1));
                }
            }
        }
        final CellReference upperLeft = new CellReference(nRow, nCol);
        final CellReference lowerRight = new CellReference(nNumRows - 1, nNumCols - 1);
        final AreaReference area = new AreaReference(upperLeft, lowerRight, SpreadsheetVersion.EXCEL2007);
        return sheet.createTable(area);
    }

    @Test
    void testNamesWithNewLines() throws IOException {
        try (
                XSSFWorkbook wb = new XSSFWorkbook();
                UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()
        ) {
            XSSFSheet sheet = wb.createSheet();

            // headers
            XSSFRow headersRow = sheet.createRow(0);
            headersRow.createCell(0).setCellValue("Column1");
            headersRow.createCell(1).setCellValue("Column2");

            // a second row
            XSSFRow row = sheet.createRow(1);
            row.createCell(0).setCellValue(1);
            row.createCell(1).setCellValue(2);

            // create a table
            AreaReference area = wb.getCreationHelper().createAreaReference(
                    new CellReference(sheet.getRow(0).getCell(0)),
                    new CellReference(sheet.getRow(1).getCell(1))
            );
            XSSFTable table = sheet.createTable(area);

            // styling (no problem here)
            sheet.setColumnWidth(0, 5000);
            sheet.setColumnWidth(1, 5000);
            CTTable cttable = table.getCTTable();
            cttable.addNewTableStyleInfo();
            XSSFTableStyleInfo style = (XSSFTableStyleInfo) table.getStyle();
            style.setName("TableStyleMedium6");
            style.setShowColumnStripes(false);
            style.setShowRowStripes(true);
            cttable.addNewAutoFilter().setRef(area.formatAsString());
            CellStyle cellStyle = wb.createCellStyle();
            cellStyle.setWrapText(true);
            headersRow.getCell(0).setCellStyle(cellStyle);
            headersRow.getCell(0).setCellValue("Column1\nwith a line break");

            wb.write(bos);

            try (XSSFWorkbook wb2 = new XSSFWorkbook(bos.toInputStream())) {
                XSSFSheet wb2Sheet = wb2.getSheetAt(0);
                List<XSSFTable> tables = wb2Sheet.getTables();
                assertEquals(1, tables.size());
                XSSFTable wb2Table = tables.get(0);
                List<XSSFTableColumn> tabColumns = wb2Table.getColumns();
                assertEquals(2, tabColumns.size());
                assertEquals("Column1_x000a_with a line break", tabColumns.get(0).getName());
            }
        }
    }

    @Test
    void bug66211() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("table-sample.xlsx")) {
            XSSFTable table = wb.getTable("Tabelle1");
            assertEquals(1, table.getHeaderRowCount());
            assertEquals(3, table.getStartRowIndex());
            List<XSSFTableColumn> cols = table.getColumns();
            assertEquals(5, cols.size());
            assertEquals("Field 1", cols.get(0).getName());
            XSSFSheet sheet = table.getXSSFSheet();
            XSSFRow headerRow = sheet.getRow(3);
            headerRow.getCell(2).setCellValue("Column 1");
            table.updateHeaders();
            List<XSSFTableColumn> updatedCols = table.getColumns();
            assertEquals(5, updatedCols.size());
            assertEquals("Column 1", updatedCols.get(0).getName());
            assertEquals(cols.get(1).getName(), updatedCols.get(1).getName());
            assertEquals(cols.get(2).getName(), updatedCols.get(2).getName());
            assertEquals(cols.get(3).getName(), updatedCols.get(3).getName());
            assertEquals(cols.get(4).getName(), updatedCols.get(4).getName());
        }
    }

    @Test
    void bug66212() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("table-sample.xlsx")) {
            XSSFTable table = wb.getTable("Tabelle1");
            XSSFSheet sheet = table.getXSSFSheet();
            assertEquals(1, sheet.getCTWorksheet().getTableParts().sizeOfTablePartArray());
            sheet.removeTable(table);
            assertEquals(0, sheet.getCTWorksheet().getTableParts().sizeOfTablePartArray());
        }
    }

    @Test
    void bug66213() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("table-sample.xlsx")) {
            wb.cloneSheet(0, "Test");
            try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
                wb.write(bos);
                try (XSSFWorkbook wb2 = new XSSFWorkbook(bos.toInputStream())) {
                    XSSFSheet sheet0 = wb2.getSheetAt(0);
                    XSSFSheet sheet1 = wb2.getSheetAt(1);
                    assertEquals(1, sheet0.getTables().size());
                    assertEquals(1, sheet1.getTables().size());
                    assertEquals("Tabelle1", sheet0.getTables().get(0).getName());
                    assertEquals("Table2", sheet1.getTables().get(0).getName());
                }
            }
        }
    }

    @Test
    void testCloneConditionalFormattingSamples() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("ConditionalFormattingSamples.xlsx")) {
            wb.cloneSheet(0, "Test");
            try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
                wb.write(bos);
                try (XSSFWorkbook wb2 = new XSSFWorkbook(bos.toInputStream())) {
                    XSSFSheet sheet0 = wb2.getSheetAt(0);
                    XSSFSheet sheet1 = wb2.getSheetAt(1);
                    assertEquals(0, sheet0.getTables().size());
                    assertEquals(0, sheet1.getTables().size());
                }
            }
        }
    }

    @Test
    void testCloneSingleCellTable() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("SingleCellTable.xlsx")) {
            wb.cloneSheet(0, "Test");
            try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
                wb.write(bos);
                try (XSSFWorkbook wb2 = new XSSFWorkbook(bos.toInputStream())) {
                    XSSFSheet sheet0 = wb2.getSheetAt(0);
                    XSSFSheet sheet1 = wb2.getSheetAt(1);
                    assertEquals(1, sheet0.getTables().size());
                    assertEquals(0, sheet1.getTables().size());
                }
            }
        }
    }
}
