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
package org.apache.poi.hssf.extractor;

import java.io.IOException;

import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * A text extractor for Excel files.
 * Returns the textual content of the file, suitable for 
 *  indexing by something like Lucene, but not really
 *  intended for display to the user.
 * To turn an excel file into a CSV or similar, then see
 *  the XLS2CSVmra example
 * @see org.apache.poi.hssf.eventusermodel.examples.XLS2CSVmra
 */
public class ExcelExtractor extends POIOLE2TextExtractor {
	private HSSFWorkbook wb;
	private boolean includeSheetNames = true;
	private boolean formulasNotResults = false;
	
	public ExcelExtractor(HSSFWorkbook wb) {
		super(wb);
		this.wb = wb;
	}
	public ExcelExtractor(POIFSFileSystem fs) throws IOException {
		this(new HSSFWorkbook(fs));
	}
	

	/**
	 * Should sheet names be included? Default is true
	 */
	public void setIncludeSheetNames(boolean includeSheetNames) {
		this.includeSheetNames = includeSheetNames;
	}
	/**
	 * Should we return the formula itself, and not
	 *  the result it produces? Default is false
	 */
	public void setFormulasNotResults(boolean formulasNotResults) {
		this.formulasNotResults = formulasNotResults;
	}
	
	/**
	 * Retreives the text contents of the file
	 */
	public String getText() {
		StringBuffer text = new StringBuffer();
		
		for(int i=0;i<wb.getNumberOfSheets();i++) {
			HSSFSheet sheet = wb.getSheetAt(i);
			if(sheet == null) { continue; }
			
			if(includeSheetNames) {
				String name = wb.getSheetName(i);
				if(name != null) {
					text.append(name);
					text.append("\n");
				}
			}
			
			int firstRow = sheet.getFirstRowNum();
			int lastRow = sheet.getLastRowNum();
			for(int j=firstRow;j<=lastRow;j++) {
				HSSFRow row = sheet.getRow(j);
				if(row == null) { continue; }

				// Check each cell in turn
				int firstCell = row.getFirstCellNum();
				int lastCell = row.getLastCellNum();
				for(int k=firstCell;k<lastCell;k++) {
					HSSFCell cell = row.getCell((short)k);
					boolean outputContents = false;
					if(cell == null) { continue; }
					
					switch(cell.getCellType()) {
						case HSSFCell.CELL_TYPE_STRING:
							text.append(cell.getRichStringCellValue().getString());
							outputContents = true;
							break;
						case HSSFCell.CELL_TYPE_NUMERIC:
							// Note - we don't apply any formatting!
							text.append(cell.getNumericCellValue());
							outputContents = true;
							break;
						case HSSFCell.CELL_TYPE_BOOLEAN:
							text.append(cell.getBooleanCellValue());
							outputContents = true;
							break;
						case HSSFCell.CELL_TYPE_FORMULA:
							if(formulasNotResults) {
								text.append(cell.getCellFormula());
							} else {
								// Try it as a string, if not as a number
								HSSFRichTextString str = 
									cell.getRichStringCellValue();
								if(str != null && str.length() > 0) {
									text.append(str.toString());
								} else {
									// Try and treat it as a number
									double val = cell.getNumericCellValue();
									text.append(val);
								}
							}
							outputContents = true;
							break;
					}
					
					// Output a tab if we're not on the last cell
					if(outputContents && k < (lastCell-1)) {
						text.append("\t");
					}
				}
				
				// Finish off the row
				text.append("\n");
			}
		}
		
		return text.toString();
	}
}
