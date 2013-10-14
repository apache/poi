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


import junit.framework.TestCase;

import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFMap;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 
 * @author Roberto Manicardi
 *
 */
public class TestXSSFImportFromXML extends TestCase {
	
	
	public void  testImportFromXML() throws Exception{
		
		 XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("CustomXMLMappings.xlsx");
		 
		 String name = "name";
		 String teacher = "teacher";
		 String tutor = "tutor";
		 String cdl = "cdl";
		 String duration = "duration";
		 String topic = "topic";
		 String project = "project";
		 String credits = "credits";
		 
		 String testXML = "<CORSO>"+
						  "<NOME>"+name+"</NOME>"+
						  "<DOCENTE>"+teacher+"</DOCENTE>"+ 
						  "<TUTOR>"+tutor+"</TUTOR>"+ 
						  "<CDL>"+cdl+"</CDL>"+
		 				  "<DURATA>"+duration+"</DURATA>"+ 
		 				  "<ARGOMENTO>"+topic+"</ARGOMENTO>"+ 
		 				  "<PROGETTO>"+project+"</PROGETTO>"+
		 				  "<CREDITI>"+credits+"</CREDITI>"+ 
		 				  "</CORSO>\u0000";		
		 
		 XSSFMap map = wb.getMapInfo().getXSSFMapByName("CORSO_mapping");
		 assertNotNull(map);
		 XSSFImportFromXML importer = new XSSFImportFromXML(map);
		 
		 importer.importFromXML(testXML);
		 
		 XSSFSheet sheet=wb.getSheetAt(0);
		 
		 XSSFRow row = sheet.getRow(0);
		 assertTrue(row.getCell(0).getStringCellValue().equals(name));
		 assertTrue(row.getCell(1).getStringCellValue().equals(teacher));
		 assertTrue(row.getCell(2).getStringCellValue().equals(tutor));
		 assertTrue(row.getCell(3).getStringCellValue().equals(cdl));
		 assertTrue(row.getCell(4).getStringCellValue().equals(duration));
		 assertTrue(row.getCell(5).getStringCellValue().equals(topic));
		 assertTrue(row.getCell(6).getStringCellValue().equals(project));
		 assertTrue(row.getCell(7).getStringCellValue().equals(credits));	
	}
	
	
	
	
	public void testMultiTable() throws Exception{
		
		
		XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("CustomXMLMappings-complex-type.xlsx");	 
		
		String cellC6 = "c6";
		String cellC7 = "c7";
		String cellC8 = "c8";
		String cellC9 = "c9";
		
		String testXML = "<ns1:MapInfo xmlns:ns1=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" SelectionNamespaces=\"\">" +
						 "<ns1:Schema ID=\""+cellC6+"\" SchemaRef=\"a\" />"+ 
						 "<ns1:Schema ID=\""+cellC7+"\" SchemaRef=\"b\" />"+ 
						 "<ns1:Schema ID=\""+cellC8+"\" SchemaRef=\"c\" />"+ 
						 "<ns1:Schema ID=\""+cellC9+"\" SchemaRef=\"d\" />"+ 
						 "<ns1:Map ID=\"1\" Name=\"\" RootElement=\"\" SchemaID=\"\" ShowImportExportValidationErrors=\"\" AutoFit=\"\" Append=\"\" PreserveSortAFLayout=\"\" PreserveFormat=\"\">"+
						 "<ns1:DataBinding DataBindingLoadMode=\"\" />"+ 
						 "</ns1:Map>"+
						 "<ns1:Map ID=\"2\" Name=\"\" RootElement=\"\" SchemaID=\"\" ShowImportExportValidationErrors=\"\" AutoFit=\"\" Append=\"\" PreserveSortAFLayout=\"\" PreserveFormat=\"\">"+
						 "<ns1:DataBinding DataBindingLoadMode=\"\" />"+ 
						 "</ns1:Map>"+
						 "<ns1:Map ID=\"3\" Name=\"\" RootElement=\"\" SchemaID=\"\" ShowImportExportValidationErrors=\"\" AutoFit=\"\" Append=\"\" PreserveSortAFLayout=\"\" PreserveFormat=\"\">"+
						 "<ns1:DataBinding DataBindingLoadMode=\"\" />"+ 
						 "</ns1:Map>"+
						 "</ns1:MapInfo>\u0000";

		XSSFMap map = wb.getMapInfo().getXSSFMapByName("MapInfo_mapping");
		assertNotNull(map);
		XSSFImportFromXML importer = new XSSFImportFromXML(map);
		 
		importer.importFromXML(testXML);
		 
		//Check for Schema element
		XSSFSheet sheet=wb.getSheetAt(1);
		 
		assertEquals(cellC6,sheet.getRow(5).getCell(2).getStringCellValue());
		assertEquals(cellC7,sheet.getRow(6).getCell(2).getStringCellValue());
		assertEquals(cellC8,sheet.getRow(7).getCell(2).getStringCellValue());
		assertEquals(cellC9,sheet.getRow(8).getCell(2).getStringCellValue());
		
		
	}

	
	public void testSingleAttributeCellWithNamespace() throws Exception{
		
		
		XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("CustomXMLMapping-singleattributenamespace.xlsx");	 
		
		String id = "a";
		String displayName = "dispName";
		String ref="19"; 
		String count = "21";
		
		String testXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>"+ 
						 "<ns1:table xmlns:ns1=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" id=\""+id+"\" displayName=\""+displayName+"\" ref=\""+ref+"\">"+
						 "<ns1:tableColumns count=\""+count+"\" />"+ 
						 "</ns1:table>\u0000"; 
		XSSFMap map = wb.getMapInfo().getXSSFMapByName("table_mapping");
		assertNotNull(map);
		XSSFImportFromXML importer = new XSSFImportFromXML(map);
		importer.importFromXML(testXML);
		
		//Check for Schema element
		XSSFSheet sheet=wb.getSheetAt(0);
		 
		assertEquals(id,sheet.getRow(28).getCell(1).getStringCellValue());
		assertEquals(displayName,sheet.getRow(11).getCell(5).getStringCellValue());
		assertEquals(ref,sheet.getRow(14).getCell(7).getStringCellValue());
		assertEquals(count,sheet.getRow(18).getCell(3).getStringCellValue());
		
	}
}
