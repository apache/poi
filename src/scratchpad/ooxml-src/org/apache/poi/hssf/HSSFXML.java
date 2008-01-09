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
package org.apache.poi.hssf;

import java.io.IOException;

import org.apache.poi.hssf.model.SharedStringsTable;
import org.apache.poi.hxf.HXFDocument;
import org.apache.xmlbeans.XmlException;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheets;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.WorkbookDocument;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.WorksheetDocument;

/**
 * Experimental class to do low level processing
 *  of xlsx files.
 *  
 * If you are using these low level classes, then you
 *  will almost certainly need to refer to the OOXML
 *  specifications from
 *  http://www.ecma-international.org/publications/standards/Ecma-376.htm
 * 
 * WARNING - APIs expected to change rapidly
 */
public class HSSFXML extends HXFDocument {
	public static final String MAIN_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml";
	public static final String SHEET_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml";
	public static final String SHARED_STRINGS_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml";
	public static final String SHARED_STRINGS_RELATION_TYPE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings";
	
	private WorkbookDocument workbookDoc;
	private SharedStringsTable sharedStrings;

	public HSSFXML(Package container) throws OpenXML4JException, IOException, XmlException {
		super(container, MAIN_CONTENT_TYPE);
		
		workbookDoc =
			WorkbookDocument.Factory.parse(basePart.getInputStream());
		
		PackagePart ssPart = getSinglePartByRelationType(SHARED_STRINGS_RELATION_TYPE, basePart);
		if (ssPart != null) {
			sharedStrings = new SharedStringsTable(ssPart);
		} else {
			
		}
	}
	
	/**
	 * Returns the low level workbook base object
	 */
	public CTWorkbook getWorkbook() {
		return workbookDoc.getWorkbook();
	}
	/**
	 * Returns the references from the workbook to its
	 *  sheets.
	 * You'll need these to figure out the sheet ordering,
	 *  and to get at the actual sheets themselves
	 */
	public CTSheets getSheetReferences() {
		return getWorkbook().getSheets();
	}
	/**
	 * Returns the low level (work)sheet object from
	 *  the supplied sheet reference
	 */
	public CTWorksheet getSheet(CTSheet sheet) throws IOException, XmlException {
		PackagePart sheetPart =
			getRelatedPackagePart(sheet.getId());
		WorksheetDocument sheetDoc =
			WorksheetDocument.Factory.parse(sheetPart.getInputStream());
		return sheetDoc.getWorksheet();
	}

	/**
	 * Returns the shared string at the given index
	 */
	public String getSharedString(int index) {
		return this.sharedStrings.get(index);
	}
	protected SharedStringsTable _getSharedStringsTable() {
		return sharedStrings;
	}
}
