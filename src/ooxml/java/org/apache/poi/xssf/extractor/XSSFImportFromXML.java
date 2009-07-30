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
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.model.Table;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFMap;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.helpers.XSSFSingleXmlCell;
import org.apache.poi.xssf.usermodel.helpers.XSSFXmlColumnPr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xml.internal.utils.PrefixResolverDefault;


/**
 * 
 *  Imports data from an external XML to an XLSX to an XML according to one of the mapping defined.
 *  
 *  The output XML Schema must respect this limitations:
 *  
 *  - the input XML must be valid according to the XML Schema used in the mapping
 *  - denormalized table mapping is not supported (see OpenOffice part 4: chapter 3.5.1.7)
 *  - all the namespaces used in the document must be declared in the root node 
 *  
 * 
 * @author Roberto Manicardi
 *
 */
public class XSSFImportFromXML {
	
	private XSSFMap map;
	
	private static POILogger logger = POILogFactory.getLogger(XSSFImportFromXML.class);
	
	public XSSFImportFromXML(XSSFMap map){
		
		this.map = map;
		
		
	}
	
	/**
	 * Imports an XML into the XLSX using the Custom XML mapping defined
	 * 
	 * @param xmlInputString the XML to import
	 * @throws SAXException raised if error occurs during XML parsing
	 * @throws XPathExpressionException raised if error occurs during XML navigation
	 */
	public void importFromXML(String xmlInputString) throws  SAXException, XPathExpressionException{
		
		try{
			
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true); 
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		Document doc = builder.parse(new InputSource(new StringReader(xmlInputString.trim())));
		
		List<XSSFSingleXmlCell> singleXmlCells = map.getRelatedSingleXMLCell();
		
		List<Table> tables = map.getRelatedTables();
		
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		
		
		
		// Setting namespace context to XPath
		// Assuming that the namespace prefix in the mapping xpath is the same as the one used in the document
		final PrefixResolver resolver = new PrefixResolverDefault(doc.getDocumentElement());
		
		NamespaceContext ctx = new NamespaceContext() {
		
		       public String getNamespaceURI(String prefix) {
		              return resolver.getNamespaceForPrefix(prefix);
		       }
		       // Dummy implementation - not used!
		       public Iterator getPrefixes(String val) {
		    	   return null;
		       }
		       // Dummy implemenation - not used!
		       public String getPrefix(String uri) {
		    	   return null;
		       }
		};
		xpath.setNamespaceContext(ctx);


		
		
		for(XSSFSingleXmlCell singleXmlCell :singleXmlCells ){
		
			
			
			String xpathString = singleXmlCell.getXpath();
			
			Node result =  (Node) xpath.evaluate(xpathString,doc, XPathConstants.NODE);		
			String textContent = result.getTextContent();
			logger.log(POILogger.DEBUG,"Extracting with xpath "+xpathString+" : value is '"+textContent+"'");
			XSSFCell cell = singleXmlCell.getReferencedCell();
			logger.log(POILogger.DEBUG,"Setting '"+textContent+"' to cell "+cell.getColumnIndex()+"-"+cell.getRowIndex()+" in sheet "+cell.getSheet().getSheetName());
			cell.setCellValue(textContent);
			
			
			
			
		}
		
		for(Table table : tables){
			
			String commonXPath = table.getCommonXpath();
		
			NodeList result =  (NodeList) xpath.evaluate(commonXPath,doc, XPathConstants.NODESET);
			
			int rowOffset = table.getStartCellReference().getRow()+1;//the first row contains the table header
			int columnOffset = table.getStartCellReference().getCol()-1;
			
			for(int i = 0; i< result.getLength();i++){
				
				// TODO: implement support for denormalized XMLs (see OpenOffice part 4: chapter 3.5.1.7)
				
				for(XSSFXmlColumnPr xmlColumnPr: table.getXmlColumnPrs()){
					
					int localColumnId = (int)xmlColumnPr.getId();
					
					int rowId = rowOffset+i;
					int columnId = columnOffset+localColumnId;
					
					
					String localXPath = xmlColumnPr.getLocalXPath();
					localXPath = localXPath.substring(localXPath.substring(1).indexOf('/')+1);
					
					// Build an XPath to select the right node (assuming that the commonXPath != "/")
					String nodeXPath = commonXPath+"["+(i+1)+"]"+localXPath;
					
					
					
					// TODO: convert the data to the cell format	
					String value = (String) xpath.evaluate(nodeXPath,result.item(i), XPathConstants.STRING);
					logger.log(POILogger.DEBUG,"Extracting with xpath "+nodeXPath+" : value is '"+value+"'");
					XSSFRow row = table.getXSSFSheet().getRow(rowId);
					if(row==null){
						row = table.getXSSFSheet().createRow(rowId);
					}
					
					XSSFCell cell = row.getCell(columnId);
					if(cell==null){
						cell = row.createCell(columnId);
					}
					logger.log(POILogger.DEBUG,"Setting '"+value+"' to cell "+cell.getColumnIndex()+"-"+cell.getRowIndex()+" in sheet "+table.getXSSFSheet().getSheetName());
					cell.setCellValue(value.trim());
					
				}
					
				
				
			}
			
			
			
		}
		}catch(IOException e){
			//Thrown by StringReader
			e.printStackTrace();
		}catch(ParserConfigurationException e){
			//Thrown by DocumentBuilderFactory
			e.printStackTrace();
		}
		
	}
	

}
