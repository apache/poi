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
import java.io.UnsupportedEncodingException;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
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

    private void verifyBug54084Unicode(Workbook wb) throws UnsupportedEncodingException {
        // expected data is stored in UTF-8 in a text-file
        String testData = new String(HSSFTestDataSamples.getTestDataFileContent("54084 - Greek - beyond BMP.txt"), "UTF-8").trim();

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

    
    @Test
    public void test57165() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("57171_57163_57165.xlsx");
        try {
            removeAllSheetsBut(3, wb);
            wb.cloneSheet(0); // Throws exception here
            wb.setSheetName(1, "New Sheet");
            //saveWorkbook(wb, fileName);
            
            XSSFWorkbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb);
            try {
                
            } finally {
                wbBack.close();
            }
        } finally {
            wb.close();
        }
    }

    private static void removeAllSheetsBut(int sheetIndex, Workbook wb)
    {
        int sheetNb = wb.getNumberOfSheets();
        // Move this sheet at the first position
        wb.setSheetOrder(wb.getSheetName(sheetIndex), 0);
        for (int sn = sheetNb - 1; sn > 0; sn--)
        {
            wb.removeSheetAt(sn);
        }
    }
}
