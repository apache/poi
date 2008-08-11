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
package org.apache.poi.xssf.eventusermodel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.openxml4j.exceptions.InvalidFormatException;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackagePartName;
import org.openxml4j.opc.PackageRelationship;
import org.openxml4j.opc.PackageRelationshipCollection;
import org.openxml4j.opc.PackageRelationshipTypes;
import org.openxml4j.opc.PackagingURIHelper;

/**
 * This class makes it easy to get at individual parts
 *  of an OOXML .xlsx file, suitable for low memory sax
 *  parsing or similar.
 * It makes up the core part of the EventUserModel support
 *  for XSSF.
 */
public class XSSFReader {
	private Package pkg;
	private PackagePart workbookPart;
	
	/**
	 * Creates a new XSSFReader, for the given package
	 */
	public XSSFReader(Package pkg) throws IOException, OpenXML4JException {
		this.pkg = pkg;
		
        PackageRelationship coreDocRelationship = this.pkg.getRelationshipsByType(
                PackageRelationshipTypes.CORE_DOCUMENT).getRelationship(0);
    
        // Get the part that holds the workbook
        workbookPart = this.pkg.getPart(coreDocRelationship);
	}

	
	/**
	 * Opens up the Shared Strings Table, parses it, and
	 *  returns a handy object for working with 
	 *  shared strings.
	 */
	public SharedStringsTable getSharedStringsTable() throws IOException, InvalidFormatException {
		return new SharedStringsTable(getSharedStringsData());
	}
	
	/**
	 * Opens up the Styles Table, parses it, and
	 *  returns a handy object for working with cell styles
	 */
	public StylesTable getStylesTable() throws IOException, InvalidFormatException {
		return new StylesTable(getStylesData());
	}

	
	
	/**
	 * Returns an InputStream to read the contents of the
	 *  shared strings table.
	 */
	public InputStream getSharedStringsData() throws IOException, InvalidFormatException {
		return XSSFRelation.SHARED_STRINGS.getContents(workbookPart);
	}
	
	/**
	 * Returns an InputStream to read the contents of the
	 *  styles table.
	 */
	public InputStream getStylesData() throws IOException, InvalidFormatException {
		return XSSFRelation.STYLES.getContents(workbookPart);
	}
	
	/**
	 * Returns an InputStream to read the contents of the 
	 *  main Workbook, which contains key overall data for
	 *  the file, including sheet definitions.
	 */
	public InputStream getWorkbookData() throws IOException, InvalidFormatException {
		return workbookPart.getInputStream();
	}
	
	/**
	 * Returns an InputStream to read the contents of the
	 *  specified Sheet.
	 * @param relId The relationId of the sheet, from a r:id on the workbook
	 */
	public InputStream getSheet(String relId) throws IOException, InvalidFormatException {
        PackageRelationship rel = workbookPart.getRelationship(relId);
        if(rel == null) {
        	throw new IllegalArgumentException("No Sheet found with r:id " + relId);
        }
        
        PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
        PackagePart sheet = pkg.getPart(relName);
        if(sheet == null) {
        	throw new IllegalArgumentException("No data found for Sheet with r:id " + relId);
        }
        return sheet.getInputStream();
	}
	
	/**
	 * Returns an Iterator which will let you get at all the
	 *  different Sheets in turn.
	 * Each sheet's InputStream is only opened when fetched
	 *  from the Iterator. It's up to you to close the
	 *  InputStreams when done with each one.
	 */
	public Iterator<InputStream> getSheetsData() throws IOException, InvalidFormatException {
		return new SheetDataIterator();
	}
	
	private class SheetDataIterator implements Iterator<InputStream> {
		private Iterator<PackageRelationship> sheetRels;
		private SheetDataIterator() throws IOException, InvalidFormatException {
			// Find all the sheets
			PackageRelationshipCollection sheets =
				workbookPart.getRelationshipsByType(
						XSSFRelation.WORKSHEET.getRelation()
			);
			sheetRels = sheets.iterator();
		}

		public boolean hasNext() {
			return sheetRels.hasNext();
		}

		public InputStream next() {
			PackageRelationship sheet = sheetRels.next();
			try {
		        PackagePartName relName = PackagingURIHelper.createPartName(sheet.getTargetURI());
				PackagePart sheetPkg = pkg.getPart(relName);
				return sheetPkg.getInputStream();
			} catch(IOException e) {
				throw new RuntimeException(e);
			} catch(InvalidFormatException ife) {
				throw new RuntimeException(ife);
			}
		}

		public void remove() {
			throw new IllegalStateException("Not supported");
		}
	}
}
