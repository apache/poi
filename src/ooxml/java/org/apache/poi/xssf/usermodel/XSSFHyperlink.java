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

import org.apache.poi.ss.usermodel.Hyperlink;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTHyperlink;

import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;

/**
 * XSSF Implementation of a Hyperlink.
 * Note - unlike with HSSF, many kinds of hyperlink
 *  are largely stored as relations of the sheet
 */
public class XSSFHyperlink implements Hyperlink {
	private int type;
	private XSSFSheet sheet;
	private CTHyperlink ctHyperlink;
	
	protected XSSFHyperlink(int type, XSSFSheet sheet) {
		this.type = type;
		this.sheet = sheet;
		this.ctHyperlink = CTHyperlink.Factory.newInstance();
	}
	protected XSSFHyperlink(CTHyperlink ctHyperlink, XSSFSheet sheet) {
		this.sheet = sheet;
		this.ctHyperlink = ctHyperlink;
		
		// Figure out the Hyperlink type
		// TODO
	}

	/**
	 * Returns the underlying hyperlink object
	 */
	protected CTHyperlink getCTHyperlink() {
		return ctHyperlink;
	}
	
	/**
	 * Do we need to a relation too, to represent
	 *  this hyperlink?
	 */
	public boolean needsRelationToo() {
		// TODO
		return false;
	}
	
	/**
	 * Generates the relation if required
	 */
	protected void generateRelationIfNeeded(Package pkg, PackagePart sheetPart) {
		// TODO
	}
	
	public int getType() {
		return type;
	}
	
	public String getAddress() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setLabel(String label) {
		// TODO Auto-generated method stub
	}
	public void setAddress(String address) {
		// TODO Auto-generated method stub
		
	}
	
	public short getFirstColumn() {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getFirstRow() {
		// TODO Auto-generated method stub
		return 0;
	}
	public short getLastColumn() {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getLastRow() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void setFirstColumn(short col) {
		// TODO Auto-generated method stub
	}
	public void setFirstRow(int row) {
		// TODO Auto-generated method stub
	}
	public void setLastColumn(short col) {
		// TODO Auto-generated method stub
	}
	public void setLastRow(int row) {
		// TODO Auto-generated method stub
	}
}
