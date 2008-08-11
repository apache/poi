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

import java.net.URI;

import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.util.CellReference;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTHyperlink;

import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackageRelationship;


/**
 * XSSF Implementation of a Hyperlink.
 * Note - unlike with HSSF, many kinds of hyperlink
 *  are largely stored as relations of the sheet
 */
public class XSSFHyperlink implements Hyperlink {
	private int type;
	private PackageRelationship externalRel;
	private CTHyperlink ctHyperlink;
	private String location;
	
	protected XSSFHyperlink(int type) {
		this.type = type;
		this.ctHyperlink = CTHyperlink.Factory.newInstance();
	}
	protected XSSFHyperlink(CTHyperlink ctHyperlink, PackageRelationship hyperlinkRel) {
		this.ctHyperlink = ctHyperlink;
		this.externalRel = hyperlinkRel;
		
		// Figure out the Hyperlink type and distination
		
		// If it has a location, it's internal
		if(ctHyperlink.getLocation() != null) {
			type = Hyperlink.LINK_DOCUMENT;
			location = ctHyperlink.getLocation();
		} else {
			// Otherwise it's somehow external, check
			//  the relation to see how
			if(externalRel == null) {
				if(ctHyperlink.getId() != null) {
					throw new IllegalStateException("The hyperlink for cell " + ctHyperlink.getRef() + " references relation " + ctHyperlink.getId() + ", but that didn't exist!");
				} else {
					throw new IllegalStateException("A sheet hyperlink must either have a location, or a relationship. Found:\n" + ctHyperlink);
				}
			}
			
			URI target = externalRel.getTargetURI();
			location = target.toString();
			
			// Try to figure out the type
			if(location.startsWith("http://") || location.startsWith("https://")
					|| location.startsWith("ftp://")) {
				type = Hyperlink.LINK_URL;
			} else if(location.startsWith("mailto:")) {
				type = Hyperlink.LINK_EMAIL;
			} else {
				type = Hyperlink.LINK_FILE;
			}
		}
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
		return (type != Hyperlink.LINK_DOCUMENT);
	}
	
	/**
	 * Generates the relation if required
	 */
	protected void generateRelationIfNeeded(PackagePart sheetPart) {
		if(needsRelationToo()) {
			// Generate the relation
			PackageRelationship rel =
				sheetPart.addExternalRelationship(location, XSSFRelation.SHEET_HYPERLINKS.getRelation());
			
			// Update the r:id
			ctHyperlink.setId(rel.getId());
		}
	}
	
	public int getType() {
		return type;
	}
	
	/**
	 * Get the reference of the cell this applies to,
	 *  eg A55
	 */
	public String getCellRef() {
		return ctHyperlink.getRef();
	}
	
	public String getAddress() {
		return location;
	}
	public String getLabel() {
		return ctHyperlink.getDisplay();
	}
	
	public void setLabel(String label) {
		ctHyperlink.setDisplay(label);
	}
	public void setAddress(String address) {
		location = address;
	}

	/**
	 * Assigns this hyperlink to the given cell reference
	 */
	protected void setCellReference(String ref) {
		ctHyperlink.setRef(ref);
	}
	
	private CellReference buildCellReference() {
		return new CellReference(ctHyperlink.getRef());
	}
	
	public int getFirstColumn() {
		return buildCellReference().getCol();
	}
	public int getLastColumn() {
		return buildCellReference().getCol();
	}
	
	public int getFirstRow() {
		return buildCellReference().getRow();
	}
	public int getLastRow() {
		return buildCellReference().getRow();
	}
	
	public void setFirstColumn(int col) {
		ctHyperlink.setRef(
				new CellReference(
						getFirstRow(), col
				).formatAsString()
		);
	}
	public void setLastColumn(int col) {
		setFirstColumn(col);
	}
	public void setFirstRow(int row) {
		ctHyperlink.setRef(
				new CellReference(
						row, getFirstColumn()
				).formatAsString()
		);
	}
	public void setLastRow(int row) {
		setFirstRow(row);
	}
}
