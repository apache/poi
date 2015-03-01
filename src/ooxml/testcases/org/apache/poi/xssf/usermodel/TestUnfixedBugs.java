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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.Test;

/**
 * @author centic
 *
 * This testcase contains tests for bugs that are yet to be fixed. Therefore,
 * the standard ant test target does not run these tests. Run this testcase with
 * the single-test target. The names of the tests usually correspond to the
 * Bugzilla id's PLEASE MOVE tests from this class to TestBugs once the bugs are
 * fixed, so that they are then run automatically.
 */
public final class TestUnfixedBugs extends TestCase {
    public void testBug54084Unicode() throws IOException {
        // sample XLSX with the same text-contents as the text-file above
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("54084 - Greek - beyond BMP.xlsx");

        verifyBug54084Unicode(wb);

//        OutputStream baos = new FileOutputStream("/tmp/test.xlsx");
//        try {
//            wb.write(baos);
//        } finally {
//            baos.close();
//        }

        // now write the file and read it back in
        XSSFWorkbook wbWritten = XSSFTestDataSamples.writeOutAndReadBack(wb);
        verifyBug54084Unicode(wbWritten);

        // finally also write it out via the streaming interface and verify that we still can read it back in
        Workbook wbStreamingWritten = SXSSFITestDataProvider.instance.writeOutAndReadBack(new SXSSFWorkbook(wb));
        verifyBug54084Unicode(wbStreamingWritten);
    }

    private void verifyBug54084Unicode(Workbook wb) {
        // expected data is stored in UTF-8 in a text-file
        byte data[] = HSSFTestDataSamples.getTestDataFileContent("54084 - Greek - beyond BMP.txt");
        String testData = new String(data, Charset.forName("UTF-8")).trim();

        Sheet sheet = wb.getSheetAt(0);
        Row row = sheet.getRow(0);
        Cell cell = row.getCell(0);

        String value = cell.getStringCellValue();
        //System.out.println(value);

        assertEquals("The data in the text-file should exactly match the data that we read from the workbook", testData, value);
    }

    public void test54071() {
        Workbook workbook = XSSFTestDataSamples.openSampleWorkbook("54071.xlsx");
        Sheet sheet = workbook.getSheetAt(0);
        int rows = sheet.getPhysicalNumberOfRows();
        System.out.println(">> file rows is:"+(rows-1)+" <<");
        Row title = sheet.getRow(0);

        Date prev = null;
        for (int row = 1; row < rows; row++) {
            Row rowObj = sheet.getRow(row);
            for (int col = 0; col < 1; col++) {
                String titleName = title.getCell(col).toString();
                Cell cell = rowObj.getCell(col);
                if (titleName.startsWith("time")) {
                    // here the output will produce ...59 or ...58 for the rows, probably POI is
                    // doing some different rounding or some other small difference...
                    System.out.println("==Time:"+cell.getDateCellValue());
                    if(prev != null) {
                        assertEquals(prev, cell.getDateCellValue());
                    }
                    
                    prev = cell.getDateCellValue();
                }
            }
        }
    }
    
    public void test54071Simple() {
        double value1 = 41224.999988425923;
        double value2 = 41224.999988368058;
        
        int wholeDays1 = (int)Math.floor(value1);
        int millisecondsInDay1 = (int)((value1 - wholeDays1) * DateUtil.DAY_MILLISECONDS + 0.5);

        int wholeDays2 = (int)Math.floor(value2);
        int millisecondsInDay2 = (int)((value2 - wholeDays2) * DateUtil.DAY_MILLISECONDS + 0.5);
        
        assertEquals(wholeDays1, wholeDays2);
        // here we see that the time-value is 5 milliseconds apart, one is 86399000 and the other is 86398995, 
        // thus one is one second higher than the other
        assertEquals("The time-values are 5 milliseconds apart", 
                millisecondsInDay1, millisecondsInDay2);

        // when we do the calendar-stuff, there is a boolean which determines if
        // the milliseconds are rounded or not, having this at "false" causes the 
        // second to be different here!
        int startYear = 1900;
        int dayAdjust = -1; // Excel thinks 2/29/1900 is a valid date, which it isn't
        Calendar calendar1 = new GregorianCalendar();
        calendar1.set(startYear,0, wholeDays1 + dayAdjust, 0, 0, 0);
        calendar1.set(Calendar.MILLISECOND, millisecondsInDay1);
      // this is the rounding part:
      calendar1.add(Calendar.MILLISECOND, 500);
      calendar1.clear(Calendar.MILLISECOND);

        Calendar calendar2 = new GregorianCalendar();
        calendar2.set(startYear,0, wholeDays2 + dayAdjust, 0, 0, 0);
        calendar2.set(Calendar.MILLISECOND, millisecondsInDay2);
      // this is the rounding part:
      calendar2.add(Calendar.MILLISECOND, 500);
      calendar2.clear(Calendar.MILLISECOND);

        // now the calendars are equal
        assertEquals(calendar1, calendar2);
        
        assertEquals(DateUtil.getJavaDate(value1, false), DateUtil.getJavaDate(value2, false));
    }

    public void test57236() {
        // Having very small numbers leads to different formatting, Excel uses the scientific notation, but POI leads to "0"
        
        /*
        DecimalFormat format = new DecimalFormat("#.##########", new DecimalFormatSymbols(Locale.getDefault()));
        double d = 3.0E-104;
        assertEquals("3.0E-104", format.format(d));
         */
        
        DataFormatter formatter = new DataFormatter(true);

        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("57236.xlsx");
        for(int sheetNum = 0;sheetNum < wb.getNumberOfSheets();sheetNum++) {
            Sheet sheet = wb.getSheetAt(sheetNum);
            for(int rowNum = sheet.getFirstRowNum();rowNum < sheet.getLastRowNum();rowNum++) {
                Row row = sheet.getRow(rowNum);
                for(int cellNum = row.getFirstCellNum();cellNum < row.getLastCellNum();cellNum++) {
                    Cell cell = row.getCell(cellNum);
                    String fmtCellValue = formatter.formatCellValue(cell);
                    
                    System.out.println("Cell: " + fmtCellValue);
                    assertNotNull(fmtCellValue);
                    assertFalse(fmtCellValue.equals("0"));
                }
            }
        }
    }

    // When this is fixed, the test case should go to BaseTestXCell with 
    // adjustments to use _testDataProvider to also verify this for XSSF
    public void testBug57294() throws IOException {
        Workbook wb = SXSSFITestDataProvider.instance.createWorkbook();
        
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        
        RichTextString str = new XSSFRichTextString("Test rich text string");
        str.applyFont(2, 4, (short)0);
        assertEquals(3, str.numFormattingRuns());
        cell.setCellValue(str);
        
        Workbook wbBack = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb);
        wb.close();
        
        // re-read after serializing and reading back
        Cell cellBack = wbBack.getSheetAt(0).getRow(0).getCell(0);
        assertNotNull(cellBack);
        RichTextString strBack = cellBack.getRichStringCellValue();
        assertNotNull(strBack);
        assertEquals(3, strBack.numFormattingRuns());
        assertEquals(0, strBack.getIndexOfFormattingRun(0));
        assertEquals(2, strBack.getIndexOfFormattingRun(1));
        assertEquals(4, strBack.getIndexOfFormattingRun(2));
    }

    @Test
    public void testBug56655() {
        XSSFWorkbook wb =  new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        
        setCellFormula(sheet, 0, 0, "#VALUE!");
        setCellFormula(sheet, 0, 1, "SUMIFS(A:A,A:A,#VALUE!)");

        XSSFFormulaEvaluator.evaluateAllFormulaCells(wb);

        assertEquals(XSSFCell.CELL_TYPE_ERROR, getCell(sheet, 0,0).getCachedFormulaResultType());
        assertEquals(FormulaError.VALUE.getCode(), getCell(sheet, 0,0).getErrorCellValue());
        assertEquals(XSSFCell.CELL_TYPE_ERROR, getCell(sheet, 0,1).getCachedFormulaResultType());
        assertEquals(FormulaError.VALUE.getCode(), getCell(sheet, 0,1).getErrorCellValue());
    }

    @Test
    public void testBug56655a() {
        XSSFWorkbook wb =  new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        
        setCellFormula(sheet, 0, 0, "B1*C1");
        sheet.getRow(0).createCell(1).setCellValue("A");
        setCellFormula(sheet, 1, 0, "B1*C1");
        sheet.getRow(1).createCell(1).setCellValue("A");
        setCellFormula(sheet, 0, 3, "SUMIFS(A:A,A:A,A2)");

        XSSFFormulaEvaluator.evaluateAllFormulaCells(wb);

        assertEquals(XSSFCell.CELL_TYPE_ERROR, getCell(sheet, 0, 0).getCachedFormulaResultType());
        assertEquals(FormulaError.VALUE.getCode(), getCell(sheet, 0, 0).getErrorCellValue());
        assertEquals(XSSFCell.CELL_TYPE_ERROR, getCell(sheet, 1, 0).getCachedFormulaResultType());
        assertEquals(FormulaError.VALUE.getCode(), getCell(sheet, 1, 0).getErrorCellValue());
        assertEquals(XSSFCell.CELL_TYPE_ERROR, getCell(sheet, 0, 3).getCachedFormulaResultType());
        assertEquals(FormulaError.VALUE.getCode(), getCell(sheet, 0, 3).getErrorCellValue());
    }

    /**
    * @param row 0-based
    * @param column 0-based
    */
   void setCellFormula(Sheet sheet, int row, int column, String formula) {
       Row r = sheet.getRow(row);
       if (r == null) {
           r = sheet.createRow(row);
       }
       Cell cell = r.getCell(column);
       if (cell == null) {
           cell = r.createCell(column);
       }
       cell.setCellType(XSSFCell.CELL_TYPE_FORMULA);
       cell.setCellFormula(formula);
   }

   /**
    * @param rowNo 0-based
    * @param column 0-based
    */
   Cell getCell(Sheet sheet, int rowNo, int column) {
       return sheet.getRow(rowNo).getCell(column);
   }
}
