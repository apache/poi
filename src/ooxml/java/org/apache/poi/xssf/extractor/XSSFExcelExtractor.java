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
package org.apache.poi.xssf.extractor;

import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.HeaderFooter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlException;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.Package;

/**
 * Helper class to extract text from an OOXML Excel file
 */
public class XSSFExcelExtractor extends POIXMLTextExtractor {
	private XSSFWorkbook workbook;
	private boolean includeSheetNames = true;
	private boolean formulasNotResults = false;
	private boolean includeCellComments = false;
	
	public XSSFExcelExtractor(String path) throws XmlException, OpenXML4JException, IOException {
		this(new XSSFWorkbook(path));
	}
	public XSSFExcelExtractor(Package container) throws XmlException, OpenXML4JException, IOException {
		this(new XSSFWorkbook(container));
	}
	public XSSFExcelExtractor(XSSFWorkbook workbook) {
		super(workbook);
		this.workbook = workbook;
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length < 1) {
			System.err.println("Use:");
			System.err.println("  HXFExcelExtractor <filename.xlsx>");
			System.exit(1);
		}
		POIXMLTextExtractor extractor = 
			new XSSFExcelExtractor(args[0]);
		System.out.println(extractor.getText());
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
     * Should cell comments be included? Default is true
     */
    public void setIncludeCellComments(boolean includeCellComments) {
        this.includeCellComments = includeCellComments;
    }
	
	/**
	 * Retreives the text contents of the file
	 */
	public String getText() {
		StringBuffer text = new StringBuffer();
		
		for(int i=0; i<workbook.getNumberOfSheets(); i++) {
			XSSFSheet sheet = (XSSFSheet)workbook.getSheetAt(i);
			if(includeSheetNames) {
				text.append(workbook.getSheetName(i) + "\n");
			}
			
			// Header(s), if present
			text.append(
					extractHeaderFooter(sheet.getFirstHeader())
			);
			text.append(
					extractHeaderFooter(sheet.getOddHeader())
			);
			text.append(
					extractHeaderFooter(sheet.getEvenHeader())
			);

			// Rows and cells
			for (Object rawR : sheet) {
				Row row = (Row)rawR;
				for(Iterator<Cell> ri = row.cellIterator(); ri.hasNext();) {
					Cell cell = ri.next();
					
					// Is it a formula one?
					if(cell.getCellType() == Cell.CELL_TYPE_FORMULA && formulasNotResults) {
						text.append(cell.getCellFormula());
					} else if(cell.getCellType() == Cell.CELL_TYPE_STRING) {
						text.append(cell.getRichStringCellValue().getString());
					} else {
						XSSFCell xc = (XSSFCell)cell;
						text.append(xc.getRawValue());
					}
					
					// Output the comment, if requested and exists
				    Comment comment = cell.getCellComment();
					if(includeCellComments && comment != null) {
					    // Replace any newlines with spaces, otherwise it
					    //  breaks the output
					    String commentText = comment.getString().getString().replace('\n', ' ');
					    text.append(" Comment by "+comment.getAuthor()+": "+commentText);
					}
					
					if(ri.hasNext())
						text.append("\t");
				}
				text.append("\n");
			}
			
			// Finally footer(s), if present
			text.append(
					extractHeaderFooter(sheet.getFirstFooter())
			);
			text.append(
					extractHeaderFooter(sheet.getOddFooter())
			);
			text.append(
					extractHeaderFooter(sheet.getEvenFooter())
			);
		}
		
		return text.toString();
	}
	
	private String extractHeaderFooter(HeaderFooter hf) {
		return ExcelExtractor._extractHeaderFooter(hf);
	}
}
