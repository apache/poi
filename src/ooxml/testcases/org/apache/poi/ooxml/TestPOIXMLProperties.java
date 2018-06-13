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

package org.apache.poi.ooxml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.apache.poi.ooxml.POIXMLProperties.CoreProperties;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test setting extended and custom OOXML properties
 */
public final class TestPOIXMLProperties {
    private XWPFDocument sampleDoc;
    private XWPFDocument sampleNoThumb;
    private POIXMLProperties _props;
    private CoreProperties _coreProperties;

    @Before
    public void setUp() throws IOException {
        sampleDoc = XWPFTestDataSamples.openSampleDocument("documentProperties.docx");
        sampleNoThumb = XWPFTestDataSamples.openSampleDocument("SampleDoc.docx");
        assertNotNull(sampleDoc);
        assertNotNull(sampleNoThumb);
        _props = sampleDoc.getProperties();
        _coreProperties = _props.getCoreProperties();
        assertNotNull(_props);
    }

    @After
    public void closeResources() throws Exception {
        sampleDoc.close();
        sampleNoThumb.close();
    }

    @Test
    public void testWorkbookExtendedProperties() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        POIXMLProperties props = workbook.getProperties();
        assertNotNull(props);

        POIXMLProperties.ExtendedProperties properties =
                props.getExtendedProperties();

        org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.CTProperties
                ctProps = properties.getUnderlyingProperties();


        String appVersion = "3.5 beta";
        String application = "POI";

        ctProps.setApplication(application);
        ctProps.setAppVersion(appVersion);

        XSSFWorkbook newWorkbook =
                XSSFTestDataSamples.writeOutAndReadBack(workbook);
        workbook.close();
        assertTrue(workbook != newWorkbook);


        POIXMLProperties newProps = newWorkbook.getProperties();
        assertNotNull(newProps);
        POIXMLProperties.ExtendedProperties newProperties =
                newProps.getExtendedProperties();

        assertEquals(application, newProperties.getApplication());
        assertEquals(appVersion, newProperties.getAppVersion());

        org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.CTProperties
                newCtProps = newProperties.getUnderlyingProperties();

        assertEquals(application, newCtProps.getApplication());
        assertEquals(appVersion, newCtProps.getAppVersion());

        newWorkbook.close();
    }


    /**
     * Test usermodel API for setting custom properties
     */
    @Test
    public void testCustomProperties() throws Exception {
        POIXMLDocument wb1 = new XSSFWorkbook();

        POIXMLProperties.CustomProperties customProps = wb1.getProperties().getCustomProperties();
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

        POIXMLDocument wb2 = XSSFTestDataSamples.writeOutAndReadBack((XSSFWorkbook)wb1);
        wb1.close();
        org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperties ctProps =
                wb2.getProperties().getCustomProperties().getUnderlyingProperties();
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
        assertEquals(36.6, p.getR8(), 0);
        assertEquals(4, p.getPid());

        p = ctProps.getPropertyArray(3);
        assertEquals("{D5CDD505-2E9C-101B-9397-08002B2CF9AE}", p.getFmtid());
        assertEquals("test-4", p.getName());
        assertEquals(true, p.getBool());
        assertEquals(5, p.getPid());
        
        wb2.close();
    }

    @Test
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

    @Test
    public void testTransitiveSetters() throws IOException {
        XWPFDocument doc = new XWPFDocument();
        CoreProperties cp = doc.getProperties().getCoreProperties();


        Date dateCreated = LocaleUtil.getLocaleCalendar(2010, 6, 15, 10, 0, 0).getTime();
        cp.setCreated(Optional.of(dateCreated));
        assertEquals(dateCreated, cp.getCreated());

        XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc);
        doc.close();
        cp = doc2.getProperties().getCoreProperties();
        Date dt3 = cp.getCreated();
        assertEquals(dateCreated, dt3);
        doc2.close();
    }

    @Test
    public void testGetSetRevision() {
        String revision = _coreProperties.getRevision();
        assertTrue("Revision number is 1", Integer.parseInt(revision) > 1);
        _coreProperties.setRevision("20");
        assertEquals("20", _coreProperties.getRevision());
        _coreProperties.setRevision("20xx");
        assertEquals("20", _coreProperties.getRevision());
    }

    @Test
    public void testLastModifiedByUserProperty() {
        String lastModifiedByUser = _coreProperties.getLastModifiedByUser();
        assertEquals("Paolo Mottadelli", lastModifiedByUser);
        _coreProperties.setLastModifiedByUser("Test User");
        assertEquals("Test User", _coreProperties.getLastModifiedByUser());
    }

    public static boolean dateTimeEqualToUTCString(Date dateTime, String utcString) {
        Calendar utcCalendar = LocaleUtil.getLocaleCalendar(LocaleUtil.TIMEZONE_UTC);
        utcCalendar.setTimeInMillis(dateTime.getTime());
        String dateTimeUtcString = utcCalendar.get(Calendar.YEAR) + "-" + 
                zeroPad((utcCalendar.get(Calendar.MONTH)+1)) + "-" + 
                zeroPad(utcCalendar.get(Calendar.DAY_OF_MONTH)) + "T" + 
                zeroPad(utcCalendar.get(Calendar.HOUR_OF_DAY)) + ":" +
                zeroPad(utcCalendar.get(Calendar.MINUTE)) + ":" + 
                zeroPad(utcCalendar.get(Calendar.SECOND)) + "Z";

        return utcString.equals(dateTimeUtcString);
    }

    @Ignore("Fails to add some of the thumbnails, needs more investigation")
    @Test
    public void testThumbnails() throws Exception {
        POIXMLProperties noThumbProps = sampleNoThumb.getProperties();

        assertNotNull(_props.getThumbnailPart());
        assertNull(noThumbProps.getThumbnailPart());

        assertNotNull(_props.getThumbnailFilename());
        assertNull(noThumbProps.getThumbnailFilename());

        assertNotNull(_props.getThumbnailImage());
        assertNull(noThumbProps.getThumbnailImage());

        assertEquals("/thumbnail.jpeg", _props.getThumbnailFilename());


        // Adding / changing
        ByteArrayInputStream imageData = new ByteArrayInputStream(new byte[1]);
        noThumbProps.setThumbnail("Testing.png", imageData);
        assertNotNull(noThumbProps.getThumbnailPart());
        assertEquals("/Testing.png", noThumbProps.getThumbnailFilename());
        assertNotNull(noThumbProps.getThumbnailImage());
        assertEquals(1, IOUtils.toByteArray(noThumbProps.getThumbnailImage()).length);

        imageData = new ByteArrayInputStream(new byte[2]);
        noThumbProps.setThumbnail("Testing2.png", imageData);
        assertNotNull(noThumbProps.getThumbnailPart());
        assertEquals("/Testing.png", noThumbProps.getThumbnailFilename());
        assertNotNull(noThumbProps.getThumbnailImage());
        assertEquals(2, IOUtils.toByteArray(noThumbProps.getThumbnailImage()).length);
    }

    private static String zeroPad(long i) {
        if (i >= 0 && i <=9) {
            return "0" + i;
        } else {
            return String.valueOf(i);
        }
    }
}
