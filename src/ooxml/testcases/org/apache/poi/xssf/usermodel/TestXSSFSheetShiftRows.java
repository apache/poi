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

import org.apache.poi.ss.usermodel.BaseTestSheetShiftRows;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;

/**
 * @author Yegor Kozlov
 */
public final class TestXSSFSheetShiftRows extends BaseTestSheetShiftRows {

    public TestXSSFSheetShiftRows(){
        super(XSSFITestDataProvider.instance);
    }

    @Override
	public void testShiftRowBreaks() { // disabled test from superclass
        // TODO - support shifting of page breaks
    }

    @Override
	public void testShiftWithComments() { // disabled test from superclass
        // TODO - support shifting of comments.
    }

	public void testBug54524() throws IOException {
        XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("54524.xlsx");
        XSSFSheet sheet = workbook.getSheetAt(0);
		sheet.shiftRows(3, 5, -1);

        Cell cell = CellUtil.getCell(sheet.getRow(1), 0);
		assertEquals(1.0, cell.getNumericCellValue());
		cell = CellUtil.getCell(sheet.getRow(2), 0);
		assertEquals("SUM(A2:A2)", cell.getCellFormula());
		cell = CellUtil.getCell(sheet.getRow(3), 0);
		assertEquals("X", cell.getStringCellValue());
	}
	

	public void testBug53798() throws IOException {
		// NOTE that for HSSF (.xls) negative shifts combined with positive ones do work as expected  
		Workbook wb = XSSFTestDataSamples.openSampleWorkbook("53798.xlsx");

		Sheet testSheet	= wb.getSheetAt(0);
		// 1) corrupted xlsx (unreadable data in the first row of a shifted group) already comes about  
		// when shifted by less than -1 negative amount (try -2)
		testSheet.shiftRows(3, 3, -2);
		
		Row newRow = null; Cell newCell = null;
		// 2) attempt to create a new row IN PLACE of a removed row by a negative shift causes corrupted 
		// xlsx file with  unreadable data in the negative shifted row. 
		// NOTE it's ok to create any other row.
		newRow = testSheet.createRow(3);
		newCell = newRow.createCell(0);
		newCell.setCellValue("new Cell in row "+newRow.getRowNum());
		
		// 3) once a negative shift has been made any attempt to shift another group of rows 
		// (note: outside of previously negative shifted rows) by a POSITIVE amount causes POI exception: 
		// org.apache.xmlbeans.impl.values.XmlValueDisconnectedException.
		// NOTE: another negative shift on another group of rows is successful, provided no new rows in  
		// place of previously shifted rows were attempted to be created as explained above.
		testSheet.shiftRows(6, 7, 1);	// -- CHANGE the shift to positive once the behaviour of  
										// the above has been tested
		
		//saveReport(wb, new File("/tmp/53798.xlsx"));
		Workbook read = XSSFTestDataSamples.writeOutAndReadBack(wb);
		assertNotNull(read);
		
		Sheet readSheet = read.getSheetAt(0);
		verifyCellContent(readSheet, 0, "0.0");
		verifyCellContent(readSheet, 1, "3.0");
		verifyCellContent(readSheet, 2, "2.0");
		verifyCellContent(readSheet, 3, "new Cell in row 3");
		verifyCellContent(readSheet, 4, "4.0");
		verifyCellContent(readSheet, 5, "5.0");
		verifyCellContent(readSheet, 6, null);
		verifyCellContent(readSheet, 7, "6.0");
		verifyCellContent(readSheet, 8, "7.0");
	}

	private void verifyCellContent(Sheet readSheet, int row, String expect) {
		Row readRow = readSheet.getRow(row);
		if(expect == null) {
			assertNull(readRow);
			return;
		}
		Cell readCell = readRow.getCell(0);
		if(readCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			assertEquals(expect, Double.toString(readCell.getNumericCellValue()));
		} else {
			assertEquals(expect, readCell.getStringCellValue());
		}
	}
	
	public void testBug53798a() throws IOException {
		Workbook wb = XSSFTestDataSamples.openSampleWorkbook("53798.xlsx");

		Sheet testSheet	= wb.getSheetAt(0);
		testSheet.shiftRows(3, 3, -1);
        for (Row r : testSheet) {
        	r.getRowNum();
        }
		testSheet.shiftRows(6, 6, 1);
		
		//saveReport(wb, new File("/tmp/53798.xlsx"));
		Workbook read = XSSFTestDataSamples.writeOutAndReadBack(wb);
		assertNotNull(read);
		
		Sheet readSheet = read.getSheetAt(0);
		verifyCellContent(readSheet, 0, "0.0");
		verifyCellContent(readSheet, 1, "1.0");
		verifyCellContent(readSheet, 2, "3.0");
		verifyCellContent(readSheet, 3, null);
		verifyCellContent(readSheet, 4, "4.0");
		verifyCellContent(readSheet, 5, "5.0");
		verifyCellContent(readSheet, 6, null);
		verifyCellContent(readSheet, 7, "6.0");
		verifyCellContent(readSheet, 8, "8.0");
	}
}
