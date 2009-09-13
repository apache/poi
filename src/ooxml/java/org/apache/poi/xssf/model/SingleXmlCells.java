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
import java.util.List;
import java.util.Vector;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.helpers.XSSFSingleXmlCell;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSingleXmlCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSingleXmlCells;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.SingleXmlCellsDocument;


/**
 * 
 * This class implements the Single Cell Tables Part (Open Office XML Part 4:
 * chapter 3.5.2)
 * 
 *
 * @author Roberto Manicardi
 */
public class SingleXmlCells extends POIXMLDocumentPart {
	
	
	private CTSingleXmlCells singleXMLCells;

	public SingleXmlCells() {
		super();
		singleXMLCells = CTSingleXmlCells.Factory.newInstance();

	}

	public SingleXmlCells(PackagePart part, PackageRelationship rel)
			throws IOException {
		super(part, rel);
		readFrom(part.getInputStream());
	}

	public void readFrom(InputStream is) throws IOException {
		try {
			SingleXmlCellsDocument doc = SingleXmlCellsDocument.Factory.parse(is);
			singleXMLCells = doc.getSingleXmlCells();
		} catch (XmlException e) {
			throw new IOException(e.getLocalizedMessage());
		}
	}
	
	public XSSFSheet getXSSFSheet(){
		return (XSSFSheet) getParent();
	}

	protected void writeTo(OutputStream out) throws IOException {
		SingleXmlCellsDocument doc = SingleXmlCellsDocument.Factory.newInstance();
		doc.setSingleXmlCells(singleXMLCells);
		doc.save(out, DEFAULT_XML_OPTIONS);
	}

	@Override
	protected void commit() throws IOException {
		PackagePart part = getPackagePart();
		OutputStream out = part.getOutputStream();
		writeTo(out);
		out.close();
	}
	
	public CTSingleXmlCells getCTSingleXMLCells(){
		return singleXMLCells;
	}
	
	/**
	 * 
	 * @return all the SimpleXmlCell contained in this SingleXmlCells element
	 */
	public List<XSSFSingleXmlCell> getAllSimpleXmlCell(){
		List<XSSFSingleXmlCell> list = new Vector<XSSFSingleXmlCell>();
		CTSingleXmlCell[] singleXMLCellArray = singleXMLCells.getSingleXmlCellArray();
		
		for(CTSingleXmlCell singleXmlCell: singleXMLCellArray){			
			list.add(new XSSFSingleXmlCell(singleXmlCell,this));
		}		
		return list;
	}
}
