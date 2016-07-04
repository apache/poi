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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;

public final class TestXSSFTable {

    @Test
    public void bug56274() throws IOException {
        // read sample file
        XSSFWorkbook wb1 = XSSFTestDataSamples.openSampleWorkbook("56274.xlsx");

        // read the original sheet header order
        XSSFRow row = wb1.getSheetAt(0).getRow(0);
        List<String> headers = new ArrayList<String>();
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
        wb1.close();
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
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx");

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

        wb.close();
    }

    @Test
    public void findColumnIndexIsRelativeToTableNotSheet() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("DataTableCities.xlsx");
        XSSFTable table = wb.getTable("SmallCity");

        // Make sure that XSSFTable.findColumnIndex returns the column index relative to the first
        // column in the table, not the column number in the sheet
        assertEquals(0, table.findColumnIndex("City")); // column I in worksheet but 0th column in table
        assertEquals(1, table.findColumnIndex("Latitude"));
        assertEquals(2, table.findColumnIndex("Longitude"));
        assertEquals(3, table.findColumnIndex("Population"));

        wb.close();
    }

    @Test
    public void getSheetName() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx");
        XSSFTable table = wb.getTable("\\_Prime.1");
        assertEquals("Table", table.getSheetName());
        wb.close(); 
    }

    @Test
    public void isHasTotalsRow() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx");
        XSSFTable table = wb.getTable("\\_Prime.1");
        assertFalse(table.isHasTotalsRow());
        wb.close(); 
    }

    @Test
    public void getStartColIndex() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx");
        XSSFTable table = wb.getTable("\\_Prime.1");
        assertEquals(0, table.getStartColIndex());
        wb.close(); 
    }

    @Test
    public void getEndColIndex() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx");
        XSSFTable table = wb.getTable("\\_Prime.1");
        assertEquals(2, table.getEndColIndex());
        wb.close(); 
    }

    @Test
    public void getStartRowIndex() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx");
        XSSFTable table = wb.getTable("\\_Prime.1");
        assertEquals(0, table.getStartRowIndex());
        wb.close(); 
    }

    @Test
    public void getEndRowIndex() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx");
        XSSFTable table = wb.getTable("\\_Prime.1");
        assertEquals(6, table.getEndRowIndex());
        wb.close(); 
    }

    @Test
    public void getStartCellReference() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx");
        XSSFTable table = wb.getTable("\\_Prime.1");
        assertEquals(new CellReference("A1"), table.getStartCellReference());
        wb.close(); 
    }

    @Test
    public void getEndCellReference() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx");
        XSSFTable table = wb.getTable("\\_Prime.1");
        assertEquals(new CellReference("C7"), table.getEndCellReference());
        wb.close(); 
    }

    @Test
    public void getNumberOfMappedColumns() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx");
        XSSFTable table = wb.getTable("\\_Prime.1");
        assertEquals(3, table.getNumberOfMappedColumns());
        wb.close(); 
    }

    @Test
    public void getAndSetDisplayName() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx");
        XSSFTable table = wb.getTable("\\_Prime.1");
        assertEquals("\\_Prime.1", table.getDisplayName());

        table.setDisplayName(null);
        assertNull(table.getDisplayName());
        assertEquals("\\_Prime.1", table.getName()); // name and display name are different

        table.setDisplayName("Display name");
        assertEquals("Display name", table.getDisplayName());
        assertEquals("\\_Prime.1", table.getName()); // name and display name are different

        wb.close();
    }

    @Test
    public void getCellReferences() {
        // make sure that cached start and end cell references
        // can be synchronized with the underlying CTTable
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sh = wb.createSheet();
        XSSFTable table = sh.createTable();
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

    @Test
    public void getRowCount() {
        XSSFWorkbook wb = new XSSFWorkbook();
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
    }
}
