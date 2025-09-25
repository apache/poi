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

package org.apache.poi.openxml4j.opc;

import static org.apache.poi.openxml4j.OpenXML4JTestDataSamples.getSampleFileName;
import static org.apache.poi.openxml4j.OpenXML4JTestDataSamples.openSampleStream;
import static org.apache.poi.openxml4j.opc.PackageRelationshipTypes.CORE_DOCUMENT;
import static org.apache.poi.openxml4j.opc.PackageRelationshipTypes.CORE_PROPERTIES;
import static org.apache.poi.openxml4j.opc.PackageRelationshipTypes.EXTENDED_PROPERTIES;
import static org.apache.poi.openxml4j.opc.TestContentType.isOldXercesActive;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty;

public final class TestPackageCoreProperties {
    /**
     * Test package core properties getters.
     */
    @Test
    void testGetProperties() throws Exception {
        // Open the package
        @SuppressWarnings("resource")
        OPCPackage p = OPCPackage.open(openSampleStream("TestPackageCoreProperiesGetters.docx"));
        compareProperties(p);
        p.revert();
    }

    /**
     * Test package core properties setters.
     */
    @Test
    void testSetProperties() throws Exception {
        String inputPath = getSampleFileName("TestPackageCoreProperiesSetters.docx");

        File outputFile = OpenXML4JTestDataSamples.getOutputFile("TestPackageCoreProperiesSettersOUTPUT.docx");

        // Open package
        @SuppressWarnings("resource")
        OPCPackage p = OPCPackage.open(inputPath, PackageAccess.READ_WRITE);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT);
        df.setTimeZone(LocaleUtil.TIMEZONE_UTC);
        Date dateToInsert = df.parse("2007-05-12T08:00:00Z", new ParsePosition(0));

        SimpleDateFormat msdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT);
        msdf.setTimeZone(LocaleUtil.TIMEZONE_UTC);

        PackageProperties props = p.getPackageProperties();

        //test various date formats
        props.setCreatedProperty("2007-05-12T08:00:00Z");
        assertEquals(dateToInsert, props.getCreatedProperty().orElse(null));

        props.setCreatedProperty("2007-05-12T08:00:00"); //no Z, assume Z
        assertEquals(dateToInsert, props.getCreatedProperty().orElse(null));

        props.setCreatedProperty("2007-05-12T08:00:00.123Z");//millis
        assertEquals(msdf.parse("2007-05-12T08:00:00.123Z"), props.getCreatedProperty().orElse(null));

        props.setCreatedProperty("2007-05-12T10:00:00+0200");
        assertEquals(dateToInsert, props.getCreatedProperty().orElse(null));

        props.setCreatedProperty("2007-05-12T10:00:00+02:00");//colon in tz
        assertEquals(dateToInsert, props.getCreatedProperty().orElse(null));

        props.setCreatedProperty("2007-05-12T06:00:00-0200");
        assertEquals(dateToInsert, props.getCreatedProperty().orElse(null));

        props.setCreatedProperty("2015-07-27");
        assertEquals(msdf.parse("2015-07-27T00:00:00.000Z"), props.getCreatedProperty().orElse(null));

        props.setCreatedProperty("2007-05-12T10:00:00.123+0200");
        assertEquals(msdf.parse("2007-05-12T08:00:00.123Z"), props.getCreatedProperty().orElse(null));

        props.setCategoryProperty("MyCategory");
        props.setContentStatusProperty("MyContentStatus");
        props.setContentTypeProperty("MyContentType");
        props.setCreatorProperty("MyCreator");
        props.setDescriptionProperty("MyDescription");
        props.setIdentifierProperty("MyIdentifier");
        props.setKeywordsProperty("MyKeywords");
        props.setLanguageProperty("MyLanguage");
        props.setLastModifiedByProperty("Julien Chable");
        props.setLastPrintedProperty(Optional.of(dateToInsert));
        props.setModifiedProperty(Optional.of(dateToInsert));
        props.setRevisionProperty("2");
        props.setTitleProperty("MyTitle");
        props.setSubjectProperty("MySubject");
        props.setVersionProperty("2");
        // Save the package in the output directory
        p.save(outputFile);
        p.revert();

        // Open the newly created file to check core properties saved values.
        @SuppressWarnings("resource")
        OPCPackage p2 = OPCPackage.open(outputFile.getAbsolutePath(), PackageAccess.READ);
        compareProperties(p2);
        p2.revert();
        assertTrue(outputFile.delete());
    }

    private void compareProperties(OPCPackage p) throws InvalidFormatException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT);
        df.setTimeZone(LocaleUtil.TIMEZONE_UTC);
        Date expectedDate = df.parse("2007-05-12T08:00:00Z", new ParsePosition(0));

        // Gets the core properties
        PackageProperties props = p.getPackageProperties();
        assertEquals("MyCategory", props.getCategoryProperty().orElse(null));
        assertEquals("MyContentStatus", props.getContentStatusProperty().orElse(null));
        assertEquals("MyContentType", props.getContentTypeProperty().orElse(null));
        assertEquals(expectedDate, props.getCreatedProperty().orElse(null));
        assertEquals("MyCreator", props.getCreatorProperty().orElse(null));
        assertEquals("MyDescription", props.getDescriptionProperty().orElse(null));
        assertEquals("MyIdentifier", props.getIdentifierProperty().orElse(null));
        assertEquals("MyKeywords", props.getKeywordsProperty().orElse(null));
        assertEquals("MyLanguage", props.getLanguageProperty().orElse(null));
        assertEquals("Julien Chable", props.getLastModifiedByProperty().orElse(null));
        assertEquals(expectedDate, props.getLastPrintedProperty().orElse(null));
        assertEquals(expectedDate, props.getModifiedProperty().orElse(null));
        assertEquals("2", props.getRevisionProperty().orElse(null));
        assertEquals("MySubject", props.getSubjectProperty().orElse(null));
        assertEquals("MyTitle", props.getTitleProperty().orElse(null));
        assertEquals("2", props.getVersionProperty().orElse(null));
    }

    @Test
    void testCoreProperties_bug51374() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT);
        df.setTimeZone(LocaleUtil.TIMEZONE_UTC);
        String strDate = "2007-05-12T08:00:00Z";
        Date date = df.parse(strDate);

        try (OPCPackage pkg = new ZipPackage()) {
            PackagePropertiesPart props = (PackagePropertiesPart) pkg.getPackageProperties();

            // created
            assertEquals("", props.getCreatedPropertyString());
            assertFalse(props.getCreatedProperty().isPresent());
            props.setCreatedProperty((String) null);
            assertEquals("", props.getCreatedPropertyString());
            assertFalse(props.getCreatedProperty().isPresent());
            props.setCreatedProperty(Optional.empty());
            assertEquals("", props.getCreatedPropertyString());
            assertFalse(props.getCreatedProperty().isPresent());
            props.setCreatedProperty(Optional.of(date));
            assertEquals(strDate, props.getCreatedPropertyString());
            assertEquals(date, props.getCreatedProperty().get());
            props.setCreatedProperty(strDate);
            assertEquals(strDate, props.getCreatedPropertyString());
            assertEquals(date, props.getCreatedProperty().get());

            // lastPrinted
            assertEquals("", props.getLastPrintedPropertyString());
            assertFalse(props.getLastPrintedProperty().isPresent());
            props.setLastPrintedProperty((String) null);
            assertEquals("", props.getLastPrintedPropertyString());
            assertFalse(props.getLastPrintedProperty().isPresent());
            props.setLastPrintedProperty(Optional.empty());
            assertEquals("", props.getLastPrintedPropertyString());
            assertFalse(props.getLastPrintedProperty().isPresent());
            props.setLastPrintedProperty(Optional.of(date));
            assertEquals(strDate, props.getLastPrintedPropertyString());
            assertEquals(date, props.getLastPrintedProperty().get());
            props.setLastPrintedProperty(strDate);
            assertEquals(strDate, props.getLastPrintedPropertyString());
            assertEquals(date, props.getLastPrintedProperty().get());

            // modified
            assertFalse(props.getModifiedProperty().isPresent());
            props.setModifiedProperty((String) null);
            assertFalse(props.getModifiedProperty().isPresent());
            props.setModifiedProperty(Optional.empty());
            assertFalse(props.getModifiedProperty().isPresent());
            props.setModifiedProperty(Optional.of(date));
            assertEquals(strDate, props.getModifiedPropertyString());
            assertEquals(date, props.getModifiedProperty().get());
            props.setModifiedProperty(strDate);
            assertEquals(strDate, props.getModifiedPropertyString());
            assertEquals(date, props.getModifiedProperty().get());
        }
    }

    @Test
    void testGetPropertiesLO() throws Exception {
        UnsynchronizedByteArrayOutputStream out = UnsynchronizedByteArrayOutputStream.builder().get();
        // Open the package
        try (OPCPackage pkg1 = OPCPackage.open(openSampleStream("51444.xlsx"))) {
            PackageProperties props1 = pkg1.getPackageProperties();
            assertFalse(props1.getTitleProperty().isPresent());
            props1.setTitleProperty("Bug 51444 fixed");
            pkg1.save(out);
        }

        try (OPCPackage pkg2 = OPCPackage.open(out.toInputStream())) {
            PackageProperties props2 = pkg2.getPackageProperties();
            props2.setTitleProperty("Bug 51444 fixed");
        }
    }

    @Test
    void testEntitiesInCoreProps_56164() throws Exception {
        try (InputStream is = openSampleStream("CorePropertiesHasEntities.ooxml");
             OPCPackage p = OPCPackage.open(is)) {

            // Should have 3 root relationships
            boolean foundDocRel = false, foundCorePropRel = false, foundExtPropRel = false;
            for (PackageRelationship pr : p.getRelationships()) {
                switch (pr.getRelationshipType()) {
                    case CORE_DOCUMENT:
                        foundDocRel = true;
                        break;
                    case CORE_PROPERTIES:
                        foundCorePropRel = true;
                        break;
                    case EXTENDED_PROPERTIES:
                        foundExtPropRel = true;
                        break;
                }
            }
            assertTrue(foundDocRel, "Core Doc Relationship not found in " + p.getRelationships());
            assertTrue(foundCorePropRel, "Core Props Relationship not found in " + p.getRelationships());
            assertTrue(foundExtPropRel, "Ext Props Relationship not found in " + p.getRelationships());

            // Get the Core Properties
            PackagePropertiesPart props = (PackagePropertiesPart) p.getPackageProperties();

            // used to resolve a value but now we ignore DTD entities for security reasons
            assertEquals(isOldXercesActive(), props.getCreatorProperty().isPresent());
        }
    }

    @Test
    void testListOfCustomProperties() throws Exception {
        File inp = POIDataSamples.getSpreadSheetInstance().getFile("ExcelWithAttachments.xlsm");
        try (OPCPackage pkg = OPCPackage.open(inp, PackageAccess.READ);
             XSSFWorkbook wb = new XSSFWorkbook(pkg)) {

            assertNotNull(wb.getProperties());
            assertNotNull(wb.getProperties().getCustomProperties());

            for (CTProperty prop : wb.getProperties().getCustomProperties().getUnderlyingProperties().getPropertyList()) {
                assertNotNull(prop);
            }
        }
    }

    @Test
    void testAlternateCorePropertyTimezones() throws Exception {
        // We need predictable dates for testing!
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT);
        df.setTimeZone(LocaleUtil.TIMEZONE_UTC);

        UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
        try (InputStream is = openSampleStream("OPCCompliance_CoreProperties_AlternateTimezones.docx");
            OPCPackage pkg1 = OPCPackage.open(is)) {
            PackagePropertiesPart props1 = (PackagePropertiesPart) pkg1.getPackageProperties();

            // Check text properties first
            assertEquals("Lorem Ipsum", props1.getTitleProperty().orElse(null));
            assertEquals("Apache POI", props1.getCreatorProperty().orElse(null));

            // Created at has a +3 timezone and milliseconds
            //   2006-10-13T18:06:00.123+03:00
            // = 2006-10-13T15:06:00.123+00:00
            assertEquals("2006-10-13T15:06:00Z", props1.getCreatedPropertyString());
            assertEquals("2006-10-13T15:06:00.123Z", df.format(props1.getCreatedProperty().orElse(null)));

            // Modified at has a -13 timezone but no milliseconds
            //   2007-06-20T07:59:00-13:00
            // = 2007-06-20T20:59:00-13:00
            assertEquals("2007-06-20T20:59:00Z", props1.getModifiedPropertyString());
            assertEquals("2007-06-20T20:59:00.000Z", df.format(props1.getModifiedProperty().orElse(null)));


            // Ensure we can change them with other timezones and still read back OK
            props1.setCreatedProperty("2007-06-20T20:57:00+13:00");
            props1.setModifiedProperty("2007-06-20T20:59:00.123-13:00");

            pkg1.save(baos);
        }
        try (OPCPackage pkg2 = OPCPackage.open(baos.toInputStream())) {
            PackagePropertiesPart props2 = (PackagePropertiesPart) pkg2.getPackageProperties();

            // Check text properties first - should be unchanged
            assertEquals("Lorem Ipsum", props2.getTitleProperty().orElse(null));
            assertEquals("Apache POI", props2.getCreatorProperty().orElse(null));

            // Check the updated times
            //   2007-06-20T20:57:00+13:00
            // = 2007-06-20T07:57:00Z
            assertEquals("2007-06-20T07:57:00.000Z", df.format(props2.getCreatedProperty().orElse(null)));

            //   2007-06-20T20:59:00.123-13:00
            // = 2007-06-21T09:59:00.123Z
            // CoreProperties are stored as xs:dateTime -> no milliseconds are saved!
            assertEquals("2007-06-21T09:59:00.000Z", df.format(props2.getModifiedProperty().orElse(null)));
        }
    }
}
