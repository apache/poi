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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.apache.poi.ooxml.POIXMLProperties.CoreProperties;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test setting extended and custom OOXML properties
 */
public final class TestPOIXMLProperties {
    private XWPFDocument sampleDoc;
    private XWPFDocument sampleNoThumb;
    private POIXMLProperties _props;
    private CoreProperties _coreProperties;

    @BeforeEach
    void setUp() throws IOException {
        sampleDoc = XWPFTestDataSamples.openSampleDocument("documentProperties.docx");
        sampleNoThumb = XWPFTestDataSamples.openSampleDocument("SampleDoc.docx");
        assertNotNull(sampleDoc);
        assertNotNull(sampleNoThumb);
        _props = sampleDoc.getProperties();
        _coreProperties = _props.getCoreProperties();
        assertNotNull(_props);
    }

    @AfterEach
    void closeResources() throws Exception {
        sampleDoc.close();
        sampleNoThumb.close();
    }

    @Test
    void testWorkbookExtendedProperties() throws Exception {
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
        assertNotSame(workbook, newWorkbook);


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

    @Test
    void testWorkbookExtendedPropertiesGettersSetters() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        POIXMLProperties props = workbook.getProperties();
        assertNotNull(props);

        POIXMLProperties.ExtendedProperties properties =
                props.getExtendedProperties();

        String appVersion = "3.5 beta";
        String application = "POI Modified";

        assertEquals("Apache POI", properties.getApplication());
        properties.setApplication(application);
        assertEquals(properties.getApplication(), application);

        assertNull(properties.getAppVersion());
        properties.setAppVersion(appVersion);
        assertEquals(properties.getAppVersion(), appVersion);

        XSSFWorkbook newWorkbook =
                XSSFTestDataSamples.writeOutAndReadBack(workbook);
        workbook.close();
        assertNotSame(workbook, newWorkbook);

        POIXMLProperties newProps = newWorkbook.getProperties();
        assertNotNull(newProps);
        POIXMLProperties.ExtendedProperties newProperties =
                newProps.getExtendedProperties();

        assertEquals(application, newProperties.getApplication());
        assertEquals(appVersion, newProperties.getAppVersion());

        newWorkbook.close();
    }


    /**
     * Test usermodel API for setting custom properties
     */
    @Test
    void testCustomProperties() throws Exception {
        try (XSSFWorkbook wb1 = new XSSFWorkbook()) {

            POIXMLProperties.CustomProperties customProps = wb1.getProperties().getCustomProperties();
            customProps.addProperty("test-1", "string val");
            customProps.addProperty("test-2", 1974);
            customProps.addProperty("test-3", 36.6);
            //adding a duplicate
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> customProps.addProperty("test-3", 36.6));
            assertEquals("A property with this name already exists in the custom properties", e.getMessage());
            customProps.addProperty("test-4", true);

            try (XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1)) {
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
                assertTrue(p.getBool());
                assertEquals(5, p.getPid());
            }
        }
    }

    @Test
    void testDocumentProperties() {
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
    void testTransitiveSetters() throws IOException {
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
    void testGetSetRevision() {
        String revision = _coreProperties.getRevision();
        assertTrue(Integer.parseInt(revision) > 1, "Revision number is 1");
        _coreProperties.setRevision("20");
        assertEquals("20", _coreProperties.getRevision());
        _coreProperties.setRevision("20xx");
        assertEquals("20", _coreProperties.getRevision());
    }

    @Test
    void testLastModifiedByUserProperty() {
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

    @Disabled("Fails to add some of the thumbnails, needs more investigation")
    @Test
    void testThumbnails() throws Exception {
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
        if (i >= 0 && i <= 9) {
            return "0" + i;
        } else {
            return String.valueOf(i);
        }
    }

    @Test
    void testAddProperty() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("documentProperties.docx")) {
            POIXMLProperties.CustomProperties cps = doc.getProperties().getCustomProperties();
            assertEquals(1, cps.getLastPid());
            cps.addProperty("prop1", "abc");
            assertEquals(2, cps.getLastPid());
            assertEquals(2, cps.getProperty("prop1").getPid());
            assertEquals("abc", cps.getProperty("prop1").getLpwstr());
        }
    }

    @Test
    void testBug60977() throws IOException {

        try (final XSSFWorkbook workbook = new XSSFWorkbook()) {
            final Sheet sheet = workbook.createSheet("sheet");
            final Row row = sheet.createRow(0);
            final Cell cell = row.createCell(0);
            cell.setCellValue("cell");

            final POIXMLProperties properties = workbook.getProperties();
            final POIXMLProperties.CustomProperties customProperties = properties.getCustomProperties();
            final String propName = "Project";
            final String propValue = "Some name";
            customProperties.addProperty(propName, propValue);

            // in the unit-test just try to write out the file more than once and see if we can still parse it
            XSSFWorkbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(workbook);
            assertNotNull(wbBack);
            // properties documents are read lazily, so we have to access them to verify they parse properly
            assertNotNull(wbBack.getProperties(), "First writeOutAndReadBack");
            assertEquals(propValue, wbBack.getProperties().getCustomProperties().getProperty(propName).getLpwstr(), "First prop check");

            customProperties.addProperty(propName + "1", propValue);
            wbBack = XSSFTestDataSamples.writeOutAndReadBack(workbook);
            assertNotNull(wbBack);
            // properties documents are read lazily, so we have to access them to verify they parse properly
            assertNotNull(wbBack.getProperties(), "Second writeOutAndReadBack");
            assertEquals(propValue, wbBack.getProperties().getCustomProperties().getProperty(propName).getLpwstr(), "Second prop check");
            assertEquals(propValue, wbBack.getProperties().getCustomProperties().getProperty(propName + "1").getLpwstr(), "Second prop check1");

            wbBack = XSSFTestDataSamples.writeOutAndReadBack(workbook);
            assertNotNull(wbBack);
            // properties documents are read lazily, so we have to access them to verify they parse properly
            assertNotNull(wbBack.getProperties(), "Third writeOutAndReadBack");
            assertEquals(propValue, wbBack.getProperties().getCustomProperties().getProperty(propName).getLpwstr(), "Third prop check");
            assertEquals(propValue, wbBack.getProperties().getCustomProperties().getProperty(propName + "1").getLpwstr(), "Third prop check1");

            /* Manual test to write out the file more than once:
            File test1 = File.createTempFile("test1", ".xlsx", new File("C:\\temp"));
            File test2 = File.createTempFile("test2", ".xlsx", new File("C:\\temp"));
            try (final java.io.FileOutputStream fs = new java.io.FileOutputStream(test1)) {
                workbook.write(fs);
            }
            try (final XSSFWorkbook wb = new XSSFWorkbook(test1)) {
                assertNotNull(wb.getProperties());
            } catch (InvalidFormatException e) {
                fail("Test1 copy failed: " + e.getMessage());
            }

            try (final java.io.FileOutputStream fs = new java.io.FileOutputStream(test2)) {
                workbook.write(fs);
            }

            try (final XSSFWorkbook wb = new XSSFWorkbook(test2)) {
                assertNotNull(wb.getProperties());
            } catch (InvalidFormatException e) {
                fail("Test2 copy failed: " + e.getMessage());
            }
             */
        }
    }
}
