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

package org.apache.poi.xssf.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.helpers.XSSFXmlColumnPr;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.TableDocument;

/**
 * 
 * This class implements the Table Part (Open Office XML Part 4:
 * chapter 3.5.1)
 * 
 * This implementation works under the assumption that a table contains mappings to a subtree of an XML.
 * The root element of this subtree an occur multiple times (one for each row of the table). The child nodes
 * of the root element can be only attributes or element with maxOccurs=1 property set
 * 
 *
 * @author Roberto Manicardi
 */
public class Table extends POIXMLDocumentPart {
	
	private CTTable ctTable;
	private List<XSSFXmlColumnPr> xmlColumnPr;
	private CellReference startCellReference;
	private CellReference endCellReference;	
	private String commonXPath; 
	
	
	public Table() {
		super();
		ctTable = CTTable.Factory.newInstance();

	}

	public Table(PackagePart part, PackageRelationship rel)
			throws IOException {
		super(part, rel);
		readFrom(part.getInputStream());
	}

	public void readFrom(InputStream is) throws IOException {
		try {
			TableDocument doc = TableDocument.Factory.parse(is);
			ctTable = doc.getTable();
		} catch (XmlException e) {
			throw new IOException(e.getLocalizedMessage());
		}
	}
	
	public XSSFSheet getXSSFSheet(){
		return (XSSFSheet) getParent();
	}

	public void writeTo(OutputStream out) throws IOException {
		TableDocument doc = TableDocument.Factory.newInstance();
		doc.setTable(ctTable);
		doc.save(out, DEFAULT_XML_OPTIONS);
	}

	@Override
	protected void commit() throws IOException {
		PackagePart part = getPackagePart();
		OutputStream out = part.getOutputStream();
		writeTo(out);
		out.close();
	}
	
	public CTTable getCTTable(){
		return ctTable;
	}
	
	/**
	 * Checks if this Table element contains even a single mapping to the map identified by id
	 * @param id the XSSFMap ID
	 * @return true if the Table element contain mappings
	 */
	public boolean mapsTo(long id){
		boolean maps =false;
		
		List<XSSFXmlColumnPr> pointers = getXmlColumnPrs();
		
		for(XSSFXmlColumnPr pointer: pointers){
			if(pointer.getMapId()==id){
				maps=true;
				break;
			}
		}
		
		return maps;
	}

	
	/**
	 * 
	 * Calculates the xpath of the root element for the table. This will be the common part
	 * of all the mapping's xpaths
	 * 
	 * @return the xpath of the table's root element
	 */
	public String getCommonXpath() {
		
		if(commonXPath == null){
		
		String[] commonTokens ={};
		
		for(CTTableColumn column :ctTable.getTableColumns().getTableColumnArray()){
			if(column.getXmlColumnPr()!=null){
				String xpath = column.getXmlColumnPr().getXpath();
				String[] tokens =  xpath.split("/");
				if(commonTokens.length==0){
					commonTokens = tokens;
					
				}else{
					int maxLenght = commonTokens.length>tokens.length? tokens.length:commonTokens.length;
					for(int i =0; i<maxLenght;i++){
						if(!commonTokens[i].equals(tokens[i])){
						 List<String> subCommonTokens = Arrays.asList(commonTokens).subList(0, i);
						 
						 String[] container = {};
						 
						 commonTokens = subCommonTokens.toArray(container);
						 break;
						 
						 
						}
					}
				}
				
			}
		}
		
		
		commonXPath ="";
		
		for(int i = 1 ; i< commonTokens.length;i++){
			commonXPath +="/"+commonTokens[i];
		
		}
		}
		
		return commonXPath;
	}

	
	public List<XSSFXmlColumnPr> getXmlColumnPrs() {
		
		if(xmlColumnPr==null){
			xmlColumnPr = new Vector<XSSFXmlColumnPr>();
			for(CTTableColumn column:ctTable.getTableColumns().getTableColumnArray()){
				if(column.getXmlColumnPr()!=null){
					XSSFXmlColumnPr columnPr = new XSSFXmlColumnPr(this,column,column.getXmlColumnPr());
					xmlColumnPr.add(columnPr);
				}
			}
		}
		return xmlColumnPr;
	}

	/**
	 *  the number of mapped table columns (see Open Office XML Part 4: chapter 3.5.1.4)
	 * @return 
	 */
	public long getNumerOfMappedColumns(){
		return ctTable.getTableColumns().getCount();
	}
	
	
	/**
	 * The reference for the cell in the top-left part of the table 
	 * (see Open Office XML Part 4: chapter 3.5.1.2, attribute ref) 
	 * @return
	 */
	public CellReference getStartCellReference() {
		
		if(startCellReference==null){			
				String ref = ctTable.getRef();
				String[] boundaries = ref.split(":");
				String from = boundaries[0];
				startCellReference = new CellReference(from);
		}
		return startCellReference;
	}
	
	/**
	 * The reference for the cell in the bottom-right part of the table
	 * (see Open Office XML Part 4: chapter 3.5.1.2, attribute ref)
	 * @return
	 */
	public CellReference getEndCellReference() {
		
		if(endCellReference==null){
			
				String ref = ctTable.getRef();
				String[] boundaries = ref.split(":");
				String from = boundaries[1];
				endCellReference = new CellReference(from);
		}
		return endCellReference;
	}
	
	
	/**
	 * Gets the total number of rows in the selection. (Note: in this version autofiltering is ignored)
	 * @return 
	 */
	public int getRowCount(){
		
		
		CellReference from = getStartCellReference();
		CellReference to = getEndCellReference();
		
		int rowCount = -1;
		if (from!=null && to!=null){
		 rowCount = to.getRow()-from.getRow();
		}
		return rowCount;
	}
}
