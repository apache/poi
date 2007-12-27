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

import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hssf.HSSFXML;
import org.apache.poi.hssf.usermodel.HSSFXMLCell;
import org.apache.poi.hssf.usermodel.HSSFXMLWorkbook;
import org.apache.xmlbeans.XmlException;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.Package;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellFormula;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRow;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

public class HXFExcelExtractor extends POIXMLTextExtractor {
	private HSSFXMLWorkbook workbook;
	private boolean includeSheetNames = true;
	private boolean formulasNotResults = false;
	
	public HXFExcelExtractor(Package container) throws XmlException, OpenXML4JException, IOException {
		this(new HSSFXMLWorkbook(
				new HSSFXML(container)
		));
	}
	public HXFExcelExtractor(HSSFXMLWorkbook workbook) {
		super(workbook);
		this.workbook = workbook;
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
		
		CTSheet[] sheetRefs =
			workbook._getHSSFXML().getSheetReferences().getSheetArray();
		for(int i=0; i<sheetRefs.length; i++) {
			try {
				CTWorksheet sheet =
					workbook._getHSSFXML().getSheet(sheetRefs[i]);
				CTRow[] rows =
					sheet.getSheetData().getRowArray();
				
				if(i > 0) {
					text.append("\n");
				}
				if(includeSheetNames) {
					text.append(sheetRefs[i].getName() + "\n");
				}
				
				for(int j=0; j<rows.length; j++) {
					CTCell[] cells = rows[j].getCArray();
					for(int k=0; k<cells.length; k++) {
						CTCell cell = cells[k];
						if(k > 0) {
							text.append("\t");
						}
						
						boolean done = false;
						
						// Is it a formula one?
						if(cell.getF() != null) {
							if(formulasNotResults) {
								text.append(cell.getF().getStringValue());
								done = true;
							}
						}
						if(!done) {
							HSSFXMLCell uCell = new HSSFXMLCell(cell);
							text.append(uCell.getStringValue());
						}
					}
					text.append("\n");
				}
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		return text.toString();
	}
}
