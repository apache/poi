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

package org.apache.poi.xssf.usermodel.helpers;

import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.model.SingleXmlCells;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSingleXmlCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXmlCellPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXmlPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STXmlDataType.Enum;

/**
 * 
 * This class is a wrapper around the CTSingleXmlCell  (Open Office XML Part 4:
 * chapter 3.5.2.1) 
 * 

 * 
 * @author Roberto Manicardi
 *
 */
public class XSSFSingleXmlCell {
	
	private CTSingleXmlCell singleXmlCell;
	private SingleXmlCells parent;
	
	
	public XSSFSingleXmlCell(CTSingleXmlCell singleXmlCell, SingleXmlCells parent){
		this.singleXmlCell = singleXmlCell;
		this.parent = parent;
	}
	
	/**
	 * Gets the XSSFCell referenced by the R attribute or creates a new one if cell doesn't exists
	 * @return the referenced XSSFCell, null if the cell reference is invalid
	 */
	public XSSFCell getReferencedCell(){
		XSSFCell cell = null;
		
		
		CellReference cellReference =  new CellReference(singleXmlCell.getR()); 
		
		XSSFRow row = parent.getXSSFSheet().getRow(cellReference.getRow());
		if(row==null){
			row = parent.getXSSFSheet().createRow(cellReference.getRow());
		}
		
		cell = row.getCell(cellReference.getCol());  
		if(cell==null){
			cell = row.createCell(cellReference.getCol());
		}
		
		
		return cell;
	}
	
	public String getXpath(){
		CTXmlCellPr xmlCellPr = singleXmlCell.getXmlCellPr();
		CTXmlPr xmlPr = xmlCellPr.getXmlPr();
		String xpath = xmlPr.getXpath();
		return xpath;
	}
	
	public long getMapId(){
		return singleXmlCell.getXmlCellPr().getXmlPr().getMapId();
	}

	public Enum getXmlDataType() {
		CTXmlCellPr xmlCellPr = singleXmlCell.getXmlCellPr();
		CTXmlPr xmlPr = xmlCellPr.getXmlPr();
		return xmlPr.getXmlDataType();
	}

}
