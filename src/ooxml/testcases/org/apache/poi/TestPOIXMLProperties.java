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

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.poi.POIXMLProperties.CoreProperties;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

/**
 * Test setting extended and custom OOXML properties
 */
public final class TestPOIXMLProperties extends TestCase {
	private POIXMLProperties _props;
	private CoreProperties _coreProperties;

	public void setUp() {
		XWPFDocument sampleDoc = XWPFTestDataSamples.openSampleDocument("documentProperties.docx");
		_props = sampleDoc.getProperties();
		_coreProperties = _props.getCoreProperties();
		assertNotNull(_props);
	}

	public void testWorkbookExtendedProperties() {
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


	public void testWorkbookCustomProperties() {
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

	public void testDocumentProperties() {
		String category = _coreProperties.getCategory();
		assertEquals("test", category);
		String contentStatus = "Draft";
		_coreProperties.setContentStatus(contentStatus);
		assertEquals("Draft", contentStatus);
		Date created = _coreProperties.getCreated();
		SimpleDateFormat formatter = new SimpleDateFormat("EEE, MMM d, ''yy");
		assertEquals("Mon, Jul 20, '09", formatter.format(created));
		String creator = _coreProperties.getCreator();
		assertEquals("Paolo Mottadelli", creator);
		String subject = _coreProperties.getSubject();
		assertEquals("Greetings", subject);
		String title = _coreProperties.getTitle();
		assertEquals("Hello World", title);
	}

	public void testGetSetRevision() {
		String revision = _coreProperties.getRevision();
		assertTrue("Revision number is 1", Integer.parseInt(revision) > 1);
		_coreProperties.setRevision("20");
		assertEquals("20", _coreProperties.getRevision());
		_coreProperties.setRevision("20xx");
		assertEquals("20", _coreProperties.getRevision());
	}
}
