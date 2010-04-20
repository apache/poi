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

import java.util.*;

import junit.framework.TestCase;

import org.apache.poi.POIXMLProperties.CoreProperties;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.openxml4j.util.Nullable;

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


    /**
     * Test usermodel API for setting custom properties
     */
    public void testCustomProperties() {
        POIXMLDocument wb = new XSSFWorkbook();

        POIXMLProperties.CustomProperties customProps = wb.getProperties().getCustomProperties();
        customProps.addProperty("test-1", "string val");
        customProps.addProperty("test-2", 1974);
        customProps.addProperty("test-3", 36.6);
        //adding a duplicate
        try {
            customProps.addProperty("test-3", 36.6);
            fail("expected exception");
        } catch(IllegalArgumentException e){
            assertEquals("A property with this name already exists in the custom properties", e.getMessage());
        }
        customProps.addProperty("test-4", true);

        wb = XSSFTestDataSamples.writeOutAndReadBack((XSSFWorkbook)wb);
        org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperties ctProps =
                wb.getProperties().getCustomProperties().getUnderlyingProperties();
        assertEquals(4, ctProps.sizeOfPropertyArray());
        org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty p;

        p = ctProps.getPropertyArray(0);
        assertEquals("{D5CDD505-2E9C-101B-9397-08002B2CF9AE}", p.getFmtid());
        assertEquals("test-1", p.getName());
        assertEquals("string val", p.getLpwstr());
        assertEquals(2, p.getPid());

        p = ctProps.getPropertyArray(1);
        assertEquals("{D5CDD505-2E9C-101B-9397-08002B2CF9AE}", p.getFmtid());
        assertEquals("test-2", p.getName());
        assertEquals(1974, p.getI4());
        assertEquals(3, p.getPid());

        p = ctProps.getPropertyArray(2);
        assertEquals("{D5CDD505-2E9C-101B-9397-08002B2CF9AE}", p.getFmtid());
        assertEquals("test-3", p.getName());
        assertEquals(36.6, p.getR8());
        assertEquals(4, p.getPid());

        p = ctProps.getPropertyArray(3);
        assertEquals("{D5CDD505-2E9C-101B-9397-08002B2CF9AE}", p.getFmtid());
        assertEquals("test-4", p.getName());
        assertEquals(true, p.getBool());
        assertEquals(5, p.getPid());
    }

    public void testDocumentProperties() {
		String category = _coreProperties.getCategory();
		assertEquals("test", category);
		String contentStatus = "Draft";
		_coreProperties.setContentStatus(contentStatus);
		assertEquals("Draft", contentStatus);
		Date created = _coreProperties.getCreated();
		// the original file contains a following value: 2009-07-20T13:12:00Z
		assertTrue(dateTimeEqualToUTCString(created, "2009-07-20T13:12:00Z"));
		String creator = _coreProperties.getCreator();
		assertEquals("Paolo Mottadelli", creator);
		String subject = _coreProperties.getSubject();
		assertEquals("Greetings", subject);
		String title = _coreProperties.getTitle();
		assertEquals("Hello World", title);
	}

    public void testTransitiveSetters() {
		XWPFDocument doc = new XWPFDocument();
        CoreProperties cp = doc.getProperties().getCoreProperties();

        Date dateCreated = new GregorianCalendar(2010, 6, 15, 10, 0, 0).getTime();
        cp.setCreated(new Nullable<Date>(dateCreated));
        assertEquals(dateCreated.toString(), cp.getCreated().toString());

        doc = XWPFTestDataSamples.writeOutAndReadBack(doc);
        cp = doc.getProperties().getCoreProperties();
        Date dt3 = cp.getCreated();
        assertEquals(dateCreated.toString(), dt3.toString());

    }

	public void testGetSetRevision() {
		String revision = _coreProperties.getRevision();
		assertTrue("Revision number is 1", Integer.parseInt(revision) > 1);
		_coreProperties.setRevision("20");
		assertEquals("20", _coreProperties.getRevision());
		_coreProperties.setRevision("20xx");
		assertEquals("20", _coreProperties.getRevision());
	}
	
	public static boolean dateTimeEqualToUTCString(Date dateTime, String utcString) {
		Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
        utcCalendar.setTimeInMillis(dateTime.getTime());
        String dateTimeUtcString = utcCalendar.get(Calendar.YEAR) + "-" + 
               zeroPad((utcCalendar.get(Calendar.MONTH)+1)) + "-" + 
               zeroPad(utcCalendar.get(Calendar.DAY_OF_MONTH)) + "T" + 
               zeroPad(utcCalendar.get(Calendar.HOUR_OF_DAY)) + ":" +
               zeroPad(utcCalendar.get(Calendar.MINUTE)) + ":" + 
               zeroPad(utcCalendar.get(Calendar.SECOND)) + "Z";
		
		
        return utcString.equals(dateTimeUtcString);
    }

	private static String zeroPad(long i) {
        if (i >= 0 && i <=9) {
            return "0" + i;
        } else {
            return String.valueOf(i);
        }
    }
}
