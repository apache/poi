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
package org.apache.poi.ss.usermodel.examples;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Various things from the quick guide documentation
 */
public class FromQuickGuide {
	public static void newWorkbook() throws IOException {
		boolean doHSSF = true;
		boolean doXSSF = true;
		
		if(doHSSF) {
		    Workbook wb = new HSSFWorkbook();
		    FileOutputStream fileOut = new FileOutputStream("workbook.xls");
		    wb.write(fileOut);
		    fileOut.close();
		}
		if(doXSSF) {
		    Workbook wb = new XSSFWorkbook();
		    FileOutputStream fileOut = new FileOutputStream("workbook.xlsx");
		    wb.write(fileOut);
		    fileOut.close();
		}
	}
	
	public static void newSheet() throws IOException {
		Workbook[] wbs = new Workbook[] {
				new HSSFWorkbook(), new XSSFWorkbook()
		};
		
		for (int i = 0; i < wbs.length; i++) {
			Workbook wb = wbs[i];
		    Sheet sheet1 = wb.createSheet("new sheet");
		    Sheet sheet2 = wb.createSheet("second sheet");
		    FileOutputStream fileOut = new FileOutputStream("workbook.xls");
		    wb.write(fileOut);
		    fileOut.close();
		}
	}
	
	public static void newCells() throws IOException {
		Workbook[] wbs = new Workbook[] {
				new HSSFWorkbook(), new XSSFWorkbook()
		};
		
		for (int i = 0; i < wbs.length; i++) {
			Workbook wb = wbs[i];
		    CreationHelper createHelper = wb.getCreationHelper();
		    Sheet sheet = wb.createSheet("new sheet");

		    // Create a row and put some cells in it. Rows are 0 based.
		    Row row = sheet.createRow((short)0);
		    // Create a cell and put a value in it.
		    Cell cell = row.createCell((short)0);
		    cell.setCellValue(1);

		    // Or do it on one line.
		    row.createCell((short)1).setCellValue(1.2);
		    row.createCell((short)2).setCellValue(
		    		createHelper.createRichTextString("This is a string"));
		    row.createCell((short)3).setCellValue(true);

		    // Write the output to a file
		    FileOutputStream fileOut = new FileOutputStream("workbook.xls");
		    wb.write(fileOut);
		    fileOut.close();
		}
	}
	
	public static void newDateCells() throws IOException {
	    Workbook wb = new HSSFWorkbook();
	    //Workbook wb = new XSSFWorkbook();
	    CreationHelper createHelper = wb.getCreationHelper();
	    Sheet sheet = wb.createSheet("new sheet");

	    // Create a row and put some cells in it. Rows are 0 based.
	    Row row = sheet.createRow((short)0);
		
	    // Create a cell and put a date value in it.  The first cell is not styled
	    // as a date.
	    Cell cell = row.createCell((short)0);
	    cell.setCellValue(new Date());

	    // we style the second cell as a date (and time).  It is important to
	    // create a new cell style from the workbook otherwise you can end up
	    // modifying the built in style and effecting not only this cell but other cells.
	    CellStyle cellStyle = wb.createCellStyle();
	    cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm"));
	    cell = row.createCell((short)1);
	    cell.setCellValue(new Date());
	    cell.setCellStyle(cellStyle);

	    // Write the output to a file
	    FileOutputStream fileOut = new FileOutputStream("workbook.xls");
	    wb.write(fileOut);
	    fileOut.close();
	}
	
	public static void iterating() {
	    Workbook wb = new HSSFWorkbook();
	    Sheet sheet = wb.createSheet("new sheet");
	    
	    for (Row row : sheet) {
	        for (Cell cell : row) {
	            // Do something here
	        	System.out.println(cell.getCellType());
	        }
	    }
	}
	
	public static void getCellContents(Sheet sheet) {
	    for (Row row : sheet) {
	        for (Cell cell : row) {
	        	CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());
	        	System.out.print(cellRef.formatAsString());
	        	System.out.print(" - ");
	        	
	        	switch(cell.getCellType()) {
	        	case Cell.CELL_TYPE_STRING:
	        		System.out.println(cell.getRichStringCellValue().getString());
	        		break;
	        	case Cell.CELL_TYPE_NUMERIC:
	        		if(DateUtil.isCellDateFormatted(cell)) {
	        			System.out.println(cell.getDateCellValue());
	        		} else {
	        			System.out.println(cell.getNumericCellValue());
	        		}
	        		break;
	        	case Cell.CELL_TYPE_BOOLEAN:
	        		System.out.println(cell.getBooleanCellValue());
	        		break;
	        	case Cell.CELL_TYPE_FORMULA:
	        		System.out.println(cell.getCellFormula());
	        		break;
	        	default:
	        		System.out.println();
	        	}
	        }
	    }
	}
	
	public static void main(String[] args) throws Exception {
		Workbook wb = WorkbookFactory.create(new FileInputStream("src/testcases/org/apache/poi/hssf/data/WithMoreVariousData.xlsx"));
		getCellContents(wb.getSheetAt(0));
	}
}
