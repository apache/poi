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


package org.apache.poi;

import java.io.File;

import junit.framework.TestCase;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

/**
 * Test setting extended and custom OOXML properties
 */
public class TestPOIXMLProperties extends TestCase {
	
    public void testWorkbookExtendedProperties() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        POIXMLProperties props = workbook.getProperties();
        assertNotNull(props);

        org.apache.poi.POIXMLProperties.ExtendedProperties properties =
                props.getExtendedProperties();

        org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.CTProperties
                ctProps = properties.getUnderlyingProperties();


        String appVersion = "3.5 beta";
        String application = "POI";

        ctProps.setApplication(application);
        ctProps.setAppVersion(appVersion);

        ctProps = null;
        properties = null;
        props = null;

        XSSFWorkbook newWorkbook =
                XSSFTestDataSamples.writeOutAndReadBack(workbook);

        assertTrue(workbook != newWorkbook);


        POIXMLProperties newProps = newWorkbook.getProperties();
        assertNotNull(newProps);
        org.apache.poi.POIXMLProperties.ExtendedProperties newProperties =
                newProps.getExtendedProperties();

        org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.CTProperties
                newCtProps = newProperties.getUnderlyingProperties();

        assertEquals(application, newCtProps.getApplication());
        assertEquals(appVersion, newCtProps.getAppVersion());


    }


    public void testWorkbookCustomProperties() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        POIXMLProperties props = workbook.getProperties();
        assertNotNull(props);

        org.apache.poi.POIXMLProperties.CustomProperties properties =
                props.getCustomProperties();

        org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperties
                ctProps = properties.getUnderlyingProperties();


        org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty
                property = ctProps.addNewProperty();


        String fmtid =
                "{A1A1A1A1A1A1A1A1-A1A1A1A1-A1A1A1A1-A1A1A1A1-A1A1A1A1A1A1A1A1}";
        int pId = 1;
        String name = "testProperty";
        String stringValue = "testValue";


        property.setFmtid(fmtid);
        property.setPid(pId);
        property.setName(name);
        property.setBstr(stringValue);


        property = null;
        ctProps = null;
        properties = null;
        props = null;

        XSSFWorkbook newWorkbook =
                XSSFTestDataSamples.writeOutAndReadBack(workbook);

        assertTrue(workbook != newWorkbook);


        POIXMLProperties newProps = newWorkbook.getProperties();
        assertNotNull(newProps);
        org.apache.poi.POIXMLProperties.CustomProperties newProperties =
                newProps.getCustomProperties();

        org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperties
                newCtProps = newProperties.getUnderlyingProperties();

        assertEquals(1, newCtProps.getPropertyArray().length);


        org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty
                newpProperty = newCtProps.getPropertyArray()[0];

        assertEquals(fmtid, newpProperty.getFmtid());
        assertEquals(pId, newpProperty.getPid());
        assertEquals(name, newpProperty.getName());
        assertEquals(stringValue, newpProperty.getBstr());


    }
    
    public void testDocumentProperties() throws Exception {
		File sampleFile = new File(
				System.getProperty("HWPF.testdata.path") +
				File.separator + "documentProperties.docx"
		);
		assertTrue(sampleFile.exists());
		XWPFDocument sampleDoc;
		sampleDoc = new XWPFDocument(
				POIXMLDocument.openPackage(sampleFile.toString())
		);
		POIXMLProperties props = sampleDoc.getProperties();
		assertNotNull(props);
		String title = props.getCoreProperties().getTitle();
		assertEquals("Hello World", title);
		String creator = props.getCoreProperties().getCreator();
		assertEquals("Paolo Mottadelli", creator);
		String subject = props.getCoreProperties().getSubject();
		assertEquals("Greetings", subject);
    }
}