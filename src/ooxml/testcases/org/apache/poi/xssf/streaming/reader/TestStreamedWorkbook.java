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
package org.apache.poi.xssf.streaming.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

public class TestStreamedWorkbook {

    private static String TEST_FILE = "SpreadSheetSample04022017.xlsx";

    @Test
    public void testInvalidFilePath() throws Exception {
        StreamedWorkbook workbook = null;
        try {
            workbook = new StreamedWorkbook(null);
            fail("expected exception");
        } catch (Exception e) {
            assertEquals("No sheets found", e.getMessage());
        }

        if (workbook != null) {
            workbook.close();
        }

    }

    @Test
    public void testInvalidFile() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile("InvalidFile.txt");
        StreamedWorkbook workbook = null;
        try {
            workbook = new StreamedWorkbook(f.getAbsolutePath());
            workbook.getSheetIterator();
            fail("expected an exception");
        } catch (Exception e) {
            assertEquals("No valid entries or contents found, this is not a valid OOXML (Office Open XML) file",
                    e.getMessage());
        }

        if (workbook != null) {
            workbook.close();
        }
    }

    @Test
    public void testSheetCount() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());
        Workbook workbook = new XSSFWorkbook(f);
        int streamedSheetcount = 0;
        int count = 0;

        Iterator<StreamedSheet> streamedSheetIterator = streamedWorkbook.getSheetIterator();

        while (streamedSheetIterator.hasNext()) {
            streamedSheetIterator.next();
            streamedSheetcount++;
        }

        Iterator<Sheet> sheetIterator = workbook.sheetIterator();

        while (sheetIterator.hasNext()) {
            sheetIterator.next();
            count++;
        }

        assertEquals(count, streamedSheetcount);

        streamedWorkbook.close();
        workbook.close();

    }

    @Test
    public void testTotalNumberOfSheets() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());
        Workbook workbook = new XSSFWorkbook(f);

        int sheetCount = workbook.getNumberOfSheets();
        int streamedSheetCount = streamedWorkbook.getNumberOfSheets();

        assertEquals(sheetCount, streamedSheetCount);

        workbook.close();
        streamedWorkbook.close();
    }

    @Test
    public void testTotalRowCount() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());
        Workbook workbook = new XSSFWorkbook(f);
        int streamedSheetCount = 0;

        long streamedRowCount = 0;

        Iterator<StreamedSheet> streamedSheetIterator = streamedWorkbook.getSheetIterator();

        while ((streamedSheetIterator.hasNext()) && (streamedSheetCount == 0)) {
            StreamedSheet sheet = streamedSheetIterator.next();

            Iterator<StreamedRow> rows = sheet.getAllRows();

            while (rows.hasNext()) {
                rows.next();
                streamedRowCount++;
            }

            streamedSheetCount++;
        }

        int sheetCount = 0;
        int rowCount = 0;
        Iterator<Sheet> sheetIterator = workbook.sheetIterator();
        while (sheetIterator.hasNext() && sheetCount == 0) {
            Sheet sheet = sheetIterator.next();
            Iterator<Row> rowIterator = sheet.iterator();

            while (rowIterator.hasNext()) {
                rowIterator.next();
                rowCount++;
            }

            sheetCount++;
        }

        assertEquals(rowCount, streamedRowCount);

        streamedWorkbook.close();
        workbook.close();
    }

    @Test
    public void testNRowCount() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook workbook = new StreamedWorkbook(f.getAbsolutePath());

        long count = 0;
        int sheetCount = 0;

        Iterator<StreamedSheet> sheetIterator = workbook.getSheetIterator();

        while ((sheetIterator.hasNext()) && (sheetCount == 0)) {
            StreamedSheet sheet = sheetIterator.next();

            Iterator<StreamedRow> rows = sheet.getNRows(4);

            while (rows.hasNext()) {
                rows.next();
                count++;
            }

            assertEquals(4, count);
            sheetCount++;
        }

        workbook.close();
    }

    @Test
    public void testTotalCellCount() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());
        Workbook workbook = new XSSFWorkbook(f);
        int streamedSheetCount = 0;
        long streamedCellCount = 0;
        long cellCount = 0;

        Iterator<StreamedSheet> streamedSheetIterator = streamedWorkbook.getSheetIterator();

        while ((streamedSheetIterator.hasNext()) && (streamedSheetCount == 0)) {
            StreamedSheet streamedSheet = streamedSheetIterator.next();

            Iterator<StreamedRow> streamedRows = streamedSheet.getAllRows();

            while (streamedRows.hasNext()) {
                StreamedRow streamedRow = streamedRows.next();

                Iterator<StreamedCell> streamedCellIterator = streamedRow.getCellIterator();

                while (streamedCellIterator.hasNext()) {
                    streamedCellIterator.next();
                    streamedCellCount++;
                }

            }

            streamedSheetCount++;
        }

        int sheetCount = 0;
        Iterator<Sheet> sheetIterator = workbook.sheetIterator();

        while ((sheetIterator.hasNext()) && (sheetCount == 0)) {
            Sheet sheet = sheetIterator.next();

            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                Iterator<Cell> cellIterator = row.cellIterator();

                while (cellIterator.hasNext()) {
                    cellIterator.next();
                    cellCount++;
                }
            }

            sheetCount++;
        }

        assertEquals(cellCount, streamedCellCount);

        streamedWorkbook.close();
        workbook.close();
    }

    @Test
    public void testStartingRowAndCellNumber() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());
        Workbook workbook = new XSSFWorkbook(f);

        List<Short> workbookStartingCellNumbers = new ArrayList<Short>();
        List<Short> streamedWorkbookStartingCellNumbers = new ArrayList<Short>();

        int workbookStartingRowNum = 0;
        int streamedWorkbookStartingRowNumber = 0;

        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        int workbookRowCount = 0;

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (workbookRowCount == 0) {
                workbookStartingRowNum = row.getRowNum();
                workbookRowCount++;
            }

            if (row != null) {
                workbookStartingCellNumbers.add(row.getFirstCellNum());
            }

        }

        StreamedSheet streamedSheet = (StreamedSheet) streamedWorkbook.getSheetAt(0);
        Iterator<StreamedRow> streamedRowIterator = streamedSheet.getAllRows();
        int streamedWorkbookRowCount = 0;

        while (streamedRowIterator.hasNext()) {
            StreamedRow row = streamedRowIterator.next();
            if (streamedWorkbookRowCount == 0) {
                streamedWorkbookStartingRowNumber = row.getRowNum();
                streamedWorkbookRowCount++;
            }

            if (row != null) {
                streamedWorkbookStartingCellNumbers.add(row.getFirstCellNum());
            }
        }

        assertEquals(workbookStartingRowNum, streamedWorkbookStartingRowNumber);
        assertEquals(workbookStartingCellNumbers, streamedWorkbookStartingCellNumbers);

        workbook.close();
        streamedWorkbook.close();
    }

    @Test
    public void testSheetData() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook workbook = new StreamedWorkbook(f.getAbsolutePath());
        int sheetCount = 0;

        Iterator<StreamedSheet> sheetIterator = workbook.getSheetIterator();

        while ((sheetIterator.hasNext()) && (sheetCount == 0)) {
            int rowCount = 0;

            StreamedSheet sheet = sheetIterator.next();

            Iterator<StreamedRow> rows = sheet.getAllRows();

            while (rows.hasNext()) {

                StreamedRow row = rows.next();

                int cellCount = 0;

                Iterator<StreamedCell> cellIterator = row.getCellIterator();

                while (cellIterator.hasNext()) {
                    StreamedCell cell = cellIterator.next();
                    if (rowCount == 1) {

                        if (cellCount == 0) {
                            assertEquals("1", cell.getValue());
                        } else if (cellCount == 1) {
                            assertEquals("Item1", cell.getValue());
                        } else if (cellCount == 2) {
                            assertEquals("201", cell.getValue());
                        } else if (cellCount == 3) {
                            assertEquals("100.11", cell.getValue());
                        } else if (cellCount == 4) {
                            assertEquals("TRUE", cell.getValue());
                        } else if (cellCount == 5) {
                            assertEquals("04/02/1917", cell.getValue());
                        } else if (cellCount == 6) {
                            assertEquals("90.11", cell.getValue());
                        }
                    } else if (rowCount == 3) {
                        if (cellCount == 4) {
                            assertEquals(null, cell.getValue());
                        }
                    }

                    cellCount++;

                }

                rowCount++;

            }

            sheetCount++;
        }

        workbook.close();
    }

    @Test
    public void testBatchData() throws Exception {

        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook workbook = new StreamedWorkbook(f.getAbsolutePath());
        int sheetCount = 0;

        Iterator<StreamedSheet> sheetIterator = workbook.getSheetIterator();

        while (sheetIterator.hasNext()) {

            StreamedSheet sheet = sheetIterator.next();

            if (sheetCount == 1) {
                Iterator<StreamedRow> rows = sheet.getNRows(1);

                while (rows.hasNext()) {
                    StreamedRow row = rows.next();
                    assertEquals(
                            "Row Number:0 --> Item | item description | Strore | Price | Promotion applied | MFD | Discount rate |",
                            row.toString().trim());
                }

                rows = sheet.getNRows(4);

                while (rows.hasNext()) {
                    StreamedRow row = rows.next();
                    assertEquals("Row Number:1 --> 1 | Item1 | 201 | 100.11 | TRUE | 04/02/1917 | 90.11 |",
                            row.toString().trim());
                    row = rows.next();
                    assertEquals("Row Number:2 --> 2 | Item2 | 202 | 101.11 | TRUE | 05/02/1917 | 91.11 |",
                            row.toString().trim());
                    row = rows.next();
                    assertEquals("Row Number:3 --> 3 | Item3 | 203 | 102.11 | TRUE | 06/02/1917 | 92.11 |",
                            row.toString().trim());
                    row = rows.next();
                    assertEquals("Row Number:4 --> 4 | Item4 | 204 | 103.11 | TRUE | 07/02/1917 | 93.11 |",
                            row.toString().trim());
                }

                rows = sheet.getNRows(4);

                while (rows.hasNext()) {
                    StreamedRow row = rows.next();
                    assertEquals("Row Number:5 --> 5 | Item5 | 205 | 104.11 | TRUE | 08/02/1917 | 94.11 |",
                            row.toString().trim());
                    row = rows.next();
                    assertEquals("Row Number:6 --> 6 | Item6 | 206 | 105.11 | TRUE | 09/02/1917 | 95.11 |",
                            row.toString().trim());
                    row = rows.next();
                    assertEquals("Row Number:7 --> 7 | Item7 | 207 | 106.11 | TRUE | 10/02/1917 | 96.11 |",
                            row.toString().trim());
                    row = rows.next();
                    assertEquals("Row Number:8 --> 8 | Item8 | 208 | 107.11 | FALSE | 11/02/1917 | 97.11 |",
                            row.toString().trim());
                }

            }

            sheetCount++;

        }

        workbook.close();

    }

    @Test
    public void testGetCell() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook workbook = new StreamedWorkbook(f.getAbsolutePath());
        int sheetCount = 0;

        Iterator<StreamedSheet> sheetIterator = workbook.getSheetIterator();

        while ((sheetIterator.hasNext()) && (sheetCount == 0)) {

            StreamedSheet sheet = sheetIterator.next();

            Iterator<StreamedRow> rows = sheet.getAllRows();

            while (rows.hasNext()) {

                StreamedRow row = rows.next();

                if (row.getRowNum() == 3) {
                    StreamedCell cell = (StreamedCell) row.getCell(5);
                    assertEquals("06/02/1917", cell.getValue());

                    try {
                        cell = (StreamedCell) row.getCell(10);
                        fail("expected an exception");
                    } catch (IndexOutOfBoundsException exception) {
                        //expected
                    }
                }

            }

            sheetCount++;
        }

        workbook.close();
    }

    @Test
    public void testGetFirstAndLastCellNum() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook workbook = new StreamedWorkbook(f.getAbsolutePath());
        int sheetCount = 0;

        Iterator<StreamedSheet> sheetIterator = workbook.getSheetIterator();

        while ((sheetIterator.hasNext()) && (sheetCount == 0)) {

            StreamedSheet sheet = sheetIterator.next();

            Iterator<StreamedRow> rows = sheet.getAllRows();

            while (rows.hasNext()) {

                StreamedRow row = rows.next();

                if (row.getRowNum() == 3) {
                    StreamedCell cell = (StreamedCell) row.getCell(row.getFirstCellNum());
                    assertEquals("3", cell.getValue());
                    assertEquals("3", cell.getStringCellValue());

                    cell = (StreamedCell) row.getCell(row.getLastCellNum() - 1);
                    assertEquals("92.11", cell.getValue());
                    assertEquals("92.11", cell.getStringCellValue());
                }
            }

            sheetCount++;
        }

        workbook.close();
    }

    @Test
    public void testReopenClosedWorkbook() {

        try {
            POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
            File f = files.getFile(TEST_FILE);
            StreamedWorkbook workbook = new StreamedWorkbook(f.getAbsolutePath());

            workbook.close();

            workbook.getSheetIterator();
            fail("expected an exception");
        } catch (Exception e) {
            assertEquals("Workbook already closed", e.getMessage());
        }

    }

    @Test
    public void testGetActiveSheetIndex() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());

        Workbook workbook = new XSSFWorkbook(f);

        assertEquals(workbook.getActiveSheetIndex(), streamedWorkbook.getActiveSheetIndex());
        streamedWorkbook.close();
        workbook.close();
    }

    @Test
    public void testGetSheetNameWithSheetIndex() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());

        Workbook workbook = new XSSFWorkbook(f);

        assertEquals(workbook.getSheetName(1), streamedWorkbook.getSheetName(1));
        streamedWorkbook.close();
        workbook.close();
    }

    @Test
    public void testGetSheetIndex() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());

        Workbook workbook = new XSSFWorkbook(f);

        assertEquals(workbook.getSheetIndex("Sheet3"), streamedWorkbook.getSheetIndex("Sheet3"));
        streamedWorkbook.close();
        workbook.close();
    }

    @Test
    public void testGetFirstVisibleTab() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());

        Workbook workbook = new XSSFWorkbook(f);

        assertEquals(workbook.getFirstVisibleTab(), streamedWorkbook.getFirstVisibleTab());
        streamedWorkbook.close();
        workbook.close();
    }

    @Test
    public void testGetSheetName() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());

        List<String> workbookSheetNames = new ArrayList<String>();
        List<String> streamedWorkbookSheetNames = new ArrayList<String>();

        Workbook workbook = new XSSFWorkbook(f);

        Iterator<Sheet> sheetIterator = workbook.sheetIterator();

        while (sheetIterator.hasNext()) {
            workbookSheetNames.add(sheetIterator.next().getSheetName());
        }

        Iterator<StreamedSheet> streamedSheetIterator = streamedWorkbook.getSheetIterator();

        while (streamedSheetIterator.hasNext()) {
            streamedWorkbookSheetNames.add(streamedSheetIterator.next().getSheetName());
        }

        assertEquals(workbookSheetNames, streamedWorkbookSheetNames);

        streamedWorkbook.close();
        workbook.close();
    }

    @Test
    public void testIsSheetHidden() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());
        Workbook workbook = new XSSFWorkbook(f);

        assertEquals(workbook.isSheetHidden(0), streamedWorkbook.isSheetHidden(0));
        assertEquals(workbook.isSheetHidden(1), streamedWorkbook.isSheetHidden(1));

        streamedWorkbook.close();
        workbook.close();

    }

    @Test
    public void testGetSheetVisibility() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());
        Workbook workbook = new XSSFWorkbook(f);

        assertEquals(workbook.getSheetVisibility(0), streamedWorkbook.getSheetVisibility(0));
        assertEquals(workbook.getSheetVisibility(1), streamedWorkbook.getSheetVisibility(1));

        streamedWorkbook.close();
        workbook.close();

    }

    @Test
    public void testGetSheet() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());
        Workbook workbook = new XSSFWorkbook(f);

        Sheet sheet = workbook.getSheet("Sheet2");
        Sheet streamedSheet = streamedWorkbook.getSheet("Sheet2");

        assertEquals(sheet.getSheetName(), streamedSheet.getSheetName());

        streamedWorkbook.close();
        workbook.close();

    }

    @Test(expected = NullPointerException.class)
    public void testGetSheetAfterClose() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());
        streamedWorkbook.close();
        Sheet streamedSheet = streamedWorkbook.getSheet("Sheet2");
        System.out.println(streamedSheet.getSheetName());
    }

    @Test
    public void testGetSheetAt() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());
        Workbook workbook = new XSSFWorkbook(f);

        Sheet sheet = workbook.getSheetAt(0);
        Sheet streamedSheet = streamedWorkbook.getSheetAt(0);

        assertEquals(sheet.getSheetName(), streamedSheet.getSheetName());

        streamedWorkbook.close();
        workbook.close();

    }

    @Test(expected = NullPointerException.class)
    public void testGetSheetAtAfterClose() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());
        streamedWorkbook.close();

        Sheet streamedSheet = streamedWorkbook.getSheetAt(0);
        System.out.println(streamedSheet.getSheetName());

    }

    @Test(expected = NullPointerException.class)
    public void testGetSheetException() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());

        Sheet sheet = streamedWorkbook.getSheet("Sheet200");

        sheet.getSheetName();

        streamedWorkbook.close();

    }

    @Test
    public void testGetFirstCellNumber() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());
        Workbook workbook = new XSSFWorkbook(f);

        List<Short> workbookFirstCellNumList = new ArrayList<Short>();
        List<Short> sworkbookFirstCellNumList = new ArrayList<Short>();

        Iterator<Sheet> workbookSheetIterator = workbook.iterator();
        while (workbookSheetIterator.hasNext()) {
            Sheet sheet = workbookSheetIterator.next();
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                workbookFirstCellNumList.add(row.getFirstCellNum());
            }
        }

        Iterator<StreamedSheet> sworkbookSheetIterator = streamedWorkbook.getSheetIterator();
        while (sworkbookSheetIterator.hasNext()) {
            StreamedSheet sheet = sworkbookSheetIterator.next();
            Iterator<StreamedRow> rowIterator = sheet.getAllRows();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                sworkbookFirstCellNumList.add(row.getFirstCellNum());
            }
        }

        assertEquals(workbookFirstCellNumList, sworkbookFirstCellNumList);

        streamedWorkbook.close();
        workbook.close();

    }

    @Test
    public void testGetLastCellNumber() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());
        Workbook workbook = new XSSFWorkbook(f);

        List<Short> workbookLastCellNumList = new ArrayList<Short>();
        List<Short> sworkbookLastCellNumList = new ArrayList<Short>();

        Iterator<Sheet> workbookSheetIterator = workbook.iterator();
        while (workbookSheetIterator.hasNext()) {
            Sheet sheet = workbookSheetIterator.next();
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                workbookLastCellNumList.add(row.getLastCellNum());
            }
        }

        Iterator<StreamedSheet> sworkbookSheetIterator = streamedWorkbook.getSheetIterator();
        while (sworkbookSheetIterator.hasNext()) {
            StreamedSheet sheet = sworkbookSheetIterator.next();
            Iterator<StreamedRow> rowIterator = sheet.getAllRows();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                sworkbookLastCellNumList.add(row.getLastCellNum());
            }
        }

        assertEquals(workbookLastCellNumList, sworkbookLastCellNumList);

        streamedWorkbook.close();
        workbook.close();

    }

    @Test
    public void testGetPhysicalNumOfCells() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());
        Workbook workbook = new XSSFWorkbook(f);

        List<Integer> workbookPhysicalCellNums = new ArrayList<Integer>();
        List<Integer> sworkbookPhysicalCellNums = new ArrayList<Integer>();

        Iterator<Sheet> workbookSheetIterator = workbook.iterator();
        while (workbookSheetIterator.hasNext()) {
            Sheet sheet = workbookSheetIterator.next();
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                workbookPhysicalCellNums.add(row.getPhysicalNumberOfCells());
            }
        }

        Iterator<StreamedSheet> sworkbookSheetIterator = streamedWorkbook.getSheetIterator();
        while (sworkbookSheetIterator.hasNext()) {
            StreamedSheet sheet = sworkbookSheetIterator.next();
            Iterator<StreamedRow> rowIterator = sheet.getAllRows();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                sworkbookPhysicalCellNums.add(row.getPhysicalNumberOfCells());
            }
        }

        assertEquals(workbookPhysicalCellNums, sworkbookPhysicalCellNums);

        streamedWorkbook.close();
        workbook.close();

    }

    @Test
    public void testGetColumnIndex() throws Exception {
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f = files.getFile(TEST_FILE);
        StreamedWorkbook streamedWorkbook = new StreamedWorkbook(f.getAbsolutePath());
        Workbook workbook = new XSSFWorkbook(f);

        List<Integer> workbookColIndexList = new ArrayList<Integer>();
        List<Integer> sworkbookColIndexList = new ArrayList<Integer>();

        Iterator<Sheet> workbookSheetIterator = workbook.iterator();
        while (workbookSheetIterator.hasNext()) {
            Sheet sheet = workbookSheetIterator.next();
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    workbookColIndexList.add(cell.getColumnIndex());
                }
            }
        }

        Iterator<StreamedSheet> sworkbookSheetIterator = streamedWorkbook.getSheetIterator();
        while (sworkbookSheetIterator.hasNext()) {
            StreamedSheet sheet = sworkbookSheetIterator.next();
            Iterator<StreamedRow> rowIterator = sheet.getAllRows();
            while (rowIterator.hasNext()) {
                StreamedRow row = rowIterator.next();

                Iterator<StreamedCell> cellIterator = row.getCellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    sworkbookColIndexList.add(cell.getColumnIndex());
                }
            }
        }

        assertEquals(workbookColIndexList, sworkbookColIndexList);

        streamedWorkbook.close();
        workbook.close();

    }

}
