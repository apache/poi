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

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public final class TestXSSFTable {

    @Test
    public void bug56274() throws IOException {
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

            assertEquals("number of headers in xml table should match number of header cells in worksheet",
                    headers.size(), ctTableColumnArray.length);
            for (int i = 0; i < headers.size(); i++) {
                assertEquals("header name in xml table should match number of header cells in worksheet",
                        headers.get(i), ctTableColumnArray[i].getName());
            }
            assertTrue(outputFile.delete());
            wb2.close();
        }
    }

    @Test
    public void testCTTableStyleInfo() throws IOException {
        XSSFWorkbook outputWorkbook = new XSSFWorkbook();
        XSSFSheet sheet = outputWorkbook.createSheet();

        //Create
        XSSFTable outputTable = sheet.createTable();
        outputTable.setDisplayName("Test");
        CTTable outputCTTable = outputTable.getCTTable();

        //Style configurations
        CTTableStyleInfo outputStyleInfo = outputCTTable.addNewTableStyleInfo();
        outputStyleInfo.setName("TableStyleLight1");
        outputStyleInfo.setShowColumnStripes(false);
        outputStyleInfo.setShowRowStripes(true);

        XSSFWorkbook inputWorkbook = XSSFTestDataSamples.writeOutAndReadBack(outputWorkbook);
        List<XSSFTable> tables = inputWorkbook.getSheetAt(0).getTables();
        assertEquals("Tables number", 1, tables.size());

        XSSFTable inputTable = tables.get(0);
        assertEquals("Table display name", outputTable.getDisplayName(), inputTable.getDisplayName());

        CTTableStyleInfo inputStyleInfo = inputTable.getCTTable().getTableStyleInfo();
        assertEquals("Style name", outputStyleInfo.getName(), inputStyleInfo.getName());
        assertEquals("Show column stripes",
                outputStyleInfo.getShowColumnStripes(), inputStyleInfo.getShowColumnStripes());
        assertEquals("Show row stripes",
                outputStyleInfo.getShowRowStripes(), inputStyleInfo.getShowRowStripes());

        inputWorkbook.close();
        outputWorkbook.close();
    }

    @Test
    public void findColumnIndex() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {

            XSSFTable table = wb.getTable("\\_Prime.1");
            assertNotNull(table);
            assertEquals("column header has special escaped characters",
                    0, table.findColumnIndex("calc='#*'#"));
            assertEquals(1, table.findColumnIndex("Name"));
            assertEquals(2, table.findColumnIndex("Number"));

            assertEquals("case insensitive", 2, table.findColumnIndex("NuMbEr"));

            // findColumnIndex should return -1 if no column header name matches
            assertEquals(-1, table.findColumnIndex(null));
            assertEquals(-1, table.findColumnIndex(""));
            assertEquals(-1, table.findColumnIndex("one"));
        }
    }

    @Test
    public void findColumnIndexIsRelativeToTableNotSheet() throws IOException {
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
    public void getSheetName() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertEquals("Table", table.getSheetName());
        }
    }

    @Test
    public void isHasTotalsRow() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertFalse(table.getTotalsRowCount() > 0);
        }
    }

    @Test
    public void getStartColIndex() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertEquals(0, table.getStartColIndex());
        }
    }

    @Test
    public void getEndColIndex() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertEquals(2, table.getEndColIndex());
        }
    }

    @Test
    public void getStartRowIndex() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertEquals(0, table.getStartRowIndex());
        }
    }

    @Test
    public void getEndRowIndex() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertEquals(6, table.getEndRowIndex());
        }
    }

    @Test
    public void getStartCellReference() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertEquals(new CellReference("A1"), table.getStartCellReference());
        }
    }

    @Test
    public void getEndCellReference() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertEquals(new CellReference("C7"), table.getEndCellReference());
        }
    }

    @Test
    public void getEndCellReferenceFromSingleCellTable() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("SingleCellTable.xlsx")) {
            XSSFTable table = wb.getTable("Table3");
            assertEquals(new CellReference("A2"), table.getEndCellReference());
        }
    }

    @Test
    public void getNumberOfMappedColumns() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            //noinspection deprecation
            assertEquals(3, table.getNumberOfMappedColumns());
        }
    }

    @Test
    public void getColumnCount() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertEquals(3, table.getColumnCount());
        }
    }

    @Test
    public void getAndSetDisplayName() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx")) {
            XSSFTable table = wb.getTable("\\_Prime.1");
            assertEquals("\\_Prime.1", table.getDisplayName());

            table.setDisplayName("Display name");
            assertEquals("Display name", table.getDisplayName());
            assertEquals("\\_Prime.1", table.getName()); // name and display name are different
        }
    }

    @Test
    public void getCellReferences() throws IOException {
        // make sure that cached start and end cell references
        // can be synchronized with the underlying CTTable
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sh = wb.createSheet();
            XSSFTable table = sh.createTable();
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

            IOUtils.closeQuietly(wb);
        }
    }

    @Test
    public void getRowCount() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sh = wb.createSheet();
            XSSFTable table = sh.createTable();
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

            IOUtils.closeQuietly(wb);
        }
    }

    @Test
    public void testGetDataRowCount() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sh = wb.createSheet();
            AreaReference tableArea = new AreaReference("B2:B6", wb.getSpreadsheetVersion());
            XSSFTable table = sh.createTable(tableArea);

            assertEquals(5, table.getRowCount()); // includes column header
            assertEquals(4, table.getDataRowCount());

            table.setArea(new AreaReference("B2:B7", wb.getSpreadsheetVersion()));

            assertEquals(6, table.getRowCount());
            assertEquals(5, table.getDataRowCount());

            IOUtils.closeQuietly(wb);
        }
    }

    @Test
    public void testSetDataRowCount() throws IOException {
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

            IOUtils.closeQuietly(wb);
        }
    }

    @Test
    public void testCreateTableIds() throws IOException {
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
    public void testSetArea() throws IOException {
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
    public void testCreateColumn() throws IOException {
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
            assertEquals("Column c ID", 1, c1.getId());
            assertTrue("Column B ID", c1.getId() < cB.getId());
            assertTrue("Column D ID", cB.getId() < cD.getId());
            assertTrue("Column C ID", cD.getId() < cC.getId());
            assertEquals("Column 1", table.getColumns().get(0).getName()); // generated name
            assertEquals("Column B", table.getColumns().get(1).getName());
            assertEquals("Column C", table.getColumns().get(2).getName());
            assertEquals("Column D", table.getColumns().get(3).getName());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateColumnInvalidIndex() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sh = wb.createSheet();
            AreaReference tableArea = new AreaReference("D2:D3", wb.getSpreadsheetVersion());
            XSSFTable table = sh.createTable(tableArea);

            // add columns
            table.createColumn("Column 2", 1);
            table.createColumn("Column 3", 3); // out of bounds
        }
    }

    @Test
    public void testDifferentHeaderTypes() throws IOException {
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

    /**
     * See https://stackoverflow.com/questions/44407111/apache-poi-cant-format-filled-cells-as-numeric
     */
    @Test
    public void testNumericCellsInTable() throws IOException {
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
            XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb);
            IOUtils.closeQuietly(wb);
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

            // Done
            IOUtils.closeQuietly(wb2);
        }
    }

    @Test
    public void testSetDisplayName() throws IOException {
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

    @Test(expected = IllegalArgumentException.class)
    public void testSetDisplayNameNull() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();

            AreaReference reference1 = wb.getCreationHelper().createAreaReference(
                    new CellReference(0, 0), new CellReference(2, 2));

            XSSFTable table1 = sheet.createTable(reference1);
            table1.setDisplayName(null);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDisplayNameEmpty() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();

            AreaReference reference1 = wb.getCreationHelper().createAreaReference(
                    new CellReference(0, 0), new CellReference(2, 2));

            XSSFTable table1 = sheet.createTable(reference1);
            table1.setDisplayName("");
        }
    }

    /**
     * Delete table2, and create a named range in sheet0; it should automatically be assigned the name "Table4"
     */
    @Test
    public void testBug63401And62906() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet0 = workbook.createSheet();
            XSSFTable table = addTable(sheet0, 3, 0, 2, 2);

            final String procName = "testXSSFTableGetName";
            final String name = table.getName();
            System.out.println(String.format(Locale.ROOT, "%s: table.getName=%s", procName, name));
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
}
