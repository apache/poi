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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.openxml4j.util.Nullable;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty;

public final class TestPackageCoreProperties {
	/**
	 * Test package core properties getters.
	 */
    @Test
	public void testGetProperties() throws Exception {
		// Open the package
		@SuppressWarnings("resource")
        OPCPackage p = OPCPackage.open(OpenXML4JTestDataSamples.openSampleStream("TestPackageCoreProperiesGetters.docx"));
		compareProperties(p);
		p.revert();
	}

	/**
	 * Test package core properties setters.
	 */
    @Test
    public void testSetProperties() throws Exception {
		String inputPath = OpenXML4JTestDataSamples.getSampleFileName("TestPackageCoreProperiesSetters.docx");

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
        assertEquals(dateToInsert, props.getCreatedProperty().getValue());

        props.setCreatedProperty("2007-05-12T08:00:00"); //no Z, assume Z
        assertEquals(dateToInsert, props.getCreatedProperty().getValue());

        props.setCreatedProperty("2007-05-12T08:00:00.123Z");//millis
        assertEquals(msdf.parse("2007-05-12T08:00:00.123Z"), props.getCreatedProperty().getValue());

        props.setCreatedProperty("2007-05-12T10:00:00+0200");
        assertEquals(dateToInsert, props.getCreatedProperty().getValue());

        props.setCreatedProperty("2007-05-12T10:00:00+02:00");//colon in tz
        assertEquals(dateToInsert, props.getCreatedProperty().getValue());

        props.setCreatedProperty("2007-05-12T06:00:00-0200");
        assertEquals(dateToInsert, props.getCreatedProperty().getValue());

        props.setCreatedProperty("2015-07-27");
        assertEquals(msdf.parse("2015-07-27T00:00:00.000Z"), props.getCreatedProperty().getValue());

        props.setCreatedProperty("2007-05-12T10:00:00.123+0200");
        assertEquals(msdf.parse("2007-05-12T08:00:00.123Z"), props.getCreatedProperty().getValue());

        props.setCategoryProperty("MyCategory");
		props.setContentStatusProperty("MyContentStatus");
		props.setContentTypeProperty("MyContentType");
		props.setCreatorProperty("MyCreator");
		props.setDescriptionProperty("MyDescription");
		props.setIdentifierProperty("MyIdentifier");
		props.setKeywordsProperty("MyKeywords");
		props.setLanguageProperty("MyLanguage");
		props.setLastModifiedByProperty("Julien Chable");
		props.setLastPrintedProperty(new Nullable<>(dateToInsert));
		props.setModifiedProperty(new Nullable<>(dateToInsert));
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
		outputFile.delete();
	}

	private void compareProperties(OPCPackage p) throws InvalidFormatException {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT);
		df.setTimeZone(LocaleUtil.TIMEZONE_UTC);
		Date expectedDate = df.parse("2007-05-12T08:00:00Z", new ParsePosition(0));

		// Gets the core properties
		PackageProperties props = p.getPackageProperties();
		assertEquals("MyCategory", props.getCategoryProperty().getValue());
		assertEquals("MyContentStatus", props.getContentStatusProperty().getValue());
		assertEquals("MyContentType", props.getContentTypeProperty().getValue());
		assertEquals(expectedDate, props.getCreatedProperty().getValue());
		assertEquals("MyCreator", props.getCreatorProperty().getValue());
		assertEquals("MyDescription", props.getDescriptionProperty().getValue());
		assertEquals("MyIdentifier", props.getIdentifierProperty().getValue());
		assertEquals("MyKeywords", props.getKeywordsProperty().getValue());
		assertEquals("MyLanguage", props.getLanguageProperty().getValue());
		assertEquals("Julien Chable", props.getLastModifiedByProperty().getValue());
		assertEquals(expectedDate, props.getLastPrintedProperty().getValue());
		assertEquals(expectedDate, props.getModifiedProperty().getValue());
		assertEquals("2", props.getRevisionProperty().getValue());
		assertEquals("MySubject", props.getSubjectProperty().getValue());
		assertEquals("MyTitle", props.getTitleProperty().getValue());
		assertEquals("2", props.getVersionProperty().getValue());
	}

	@Test
	public void testCoreProperties_bug51374() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT);
        df.setTimeZone(LocaleUtil.TIMEZONE_UTC);
        String strDate = "2007-05-12T08:00:00Z";
        Date date = df.parse(strDate);

        OPCPackage pkg = new ZipPackage();
        PackagePropertiesPart props = (PackagePropertiesPart)pkg.getPackageProperties();

        // created
        assertEquals("", props.getCreatedPropertyString());
        assertNull(props.getCreatedProperty().getValue());
        props.setCreatedProperty((String)null);
        assertEquals("", props.getCreatedPropertyString());
        assertNull(props.getCreatedProperty().getValue());
        props.setCreatedProperty(new Nullable<>());
        assertEquals("", props.getCreatedPropertyString());
        assertNull(props.getCreatedProperty().getValue());
        props.setCreatedProperty(new Nullable<>(date));
        assertEquals(strDate, props.getCreatedPropertyString());
        assertEquals(date, props.getCreatedProperty().getValue());
        props.setCreatedProperty(strDate);
        assertEquals(strDate, props.getCreatedPropertyString());
        assertEquals(date, props.getCreatedProperty().getValue());

        // lastPrinted
        assertEquals("", props.getLastPrintedPropertyString());
        assertNull(props.getLastPrintedProperty().getValue());
        props.setLastPrintedProperty((String)null);
        assertEquals("", props.getLastPrintedPropertyString());
        assertNull(props.getLastPrintedProperty().getValue());
        props.setLastPrintedProperty(new Nullable<>());
        assertEquals("", props.getLastPrintedPropertyString());
        assertNull(props.getLastPrintedProperty().getValue());
        props.setLastPrintedProperty(new Nullable<>(date));
        assertEquals(strDate, props.getLastPrintedPropertyString());
        assertEquals(date, props.getLastPrintedProperty().getValue());
        props.setLastPrintedProperty(strDate);
        assertEquals(strDate, props.getLastPrintedPropertyString());
        assertEquals(date, props.getLastPrintedProperty().getValue());

        // modified
        assertNull(props.getModifiedProperty().getValue());
        props.setModifiedProperty((String)null);
        assertNull(props.getModifiedProperty().getValue());
        props.setModifiedProperty(new Nullable<>());
        assertNull(props.getModifiedProperty().getValue());
        props.setModifiedProperty(new Nullable<>(date));
        assertEquals(strDate, props.getModifiedPropertyString());
        assertEquals(date, props.getModifiedProperty().getValue());
        props.setModifiedProperty(strDate);
        assertEquals(strDate, props.getModifiedPropertyString());
        assertEquals(date, props.getModifiedProperty().getValue());
        
        // Tidy
        pkg.close();
    }

	@Test
	public void testGetPropertiesLO() throws Exception {
        // Open the package
        OPCPackage pkg1 = OPCPackage.open(OpenXML4JTestDataSamples.openSampleStream("51444.xlsx"));
        PackageProperties props1 = pkg1.getPackageProperties();
        assertEquals(null, props1.getTitleProperty().getValue());
        props1.setTitleProperty("Bug 51444 fixed");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pkg1.save(out);
        out.close();
        pkg1.close();

        OPCPackage pkg2 = OPCPackage.open(new ByteArrayInputStream(out.toByteArray()));
        PackageProperties props2 = pkg2.getPackageProperties();
        props2.setTitleProperty("Bug 51444 fixed");
        pkg2.close();
    }

	@Test
	public void testEntitiesInCoreProps_56164() throws Exception {
        InputStream is = OpenXML4JTestDataSamples.openSampleStream("CorePropertiesHasEntities.ooxml");
        OPCPackage p = OPCPackage.open(is);
        is.close();

        // Should have 3 root relationships
        boolean foundDocRel = false, foundCorePropRel = false, foundExtPropRel = false;
        for (PackageRelationship pr : p.getRelationships()) {
            if (pr.getRelationshipType().equals(PackageRelationshipTypes.CORE_DOCUMENT))
                foundDocRel = true;
            if (pr.getRelationshipType().equals(PackageRelationshipTypes.CORE_PROPERTIES))
                foundCorePropRel = true;
            if (pr.getRelationshipType().equals(PackageRelationshipTypes.EXTENDED_PROPERTIES))
                foundExtPropRel = true;
        }
        assertTrue("Core/Doc Relationship not found in " + p.getRelationships(), foundDocRel);
        assertTrue("Core Props Relationship not found in " + p.getRelationships(), foundCorePropRel);
        assertTrue("Ext Props Relationship not found in " + p.getRelationships(), foundExtPropRel);

        // Get the Core Properties
        PackagePropertiesPart props = (PackagePropertiesPart)p.getPackageProperties();
        
        // Check
        assertEquals("Stefan Kopf", props.getCreatorProperty().getValue());
        
        p.close();
    }
    
	@Test
	public void testListOfCustomProperties() throws Exception {
        File inp = POIDataSamples.getSpreadSheetInstance().getFile("ExcelWithAttachments.xlsm");
        OPCPackage pkg = OPCPackage.open(inp, PackageAccess.READ);
        XSSFWorkbook wb = new XSSFWorkbook(pkg);
        
        assertNotNull(wb.getProperties());
        assertNotNull(wb.getProperties().getCustomProperties());
        
        for (CTProperty prop : wb.getProperties().getCustomProperties().getUnderlyingProperties().getPropertyList()) {
            assertNotNull(prop);
        }
        
        wb.close();
        pkg.close();
    }
	
	@Test
	public void testAlternateCorePropertyTimezones() throws Exception {
        InputStream is = OpenXML4JTestDataSamples.openSampleStream("OPCCompliance_CoreProperties_AlternateTimezones.docx");
        OPCPackage pkg = OPCPackage.open(is);
        PackagePropertiesPart props = (PackagePropertiesPart)pkg.getPackageProperties();
        is.close();
        
        // We need predictable dates for testing!
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT);
        df.setTimeZone(LocaleUtil.TIMEZONE_UTC);

        // Check text properties first
        assertEquals("Lorem Ipsum", props.getTitleProperty().getValue());
        assertEquals("Apache POI", props.getCreatorProperty().getValue());
        
        // Created at has a +3 timezone and milliseconds
        //   2006-10-13T18:06:00.123+03:00
        // = 2006-10-13T15:06:00.123+00:00
        assertEquals("2006-10-13T15:06:00Z", props.getCreatedPropertyString());
        assertEquals("2006-10-13T15:06:00.123Z", df.format(props.getCreatedProperty().getValue()));
        
        // Modified at has a -13 timezone but no milliseconds
        //   2007-06-20T07:59:00-13:00
        // = 2007-06-20T20:59:00-13:00
        assertEquals("2007-06-20T20:59:00Z", props.getModifiedPropertyString());
        assertEquals("2007-06-20T20:59:00.000Z", df.format(props.getModifiedProperty().getValue()));
        
        
        // Ensure we can change them with other timezones and still read back OK
        props.setCreatedProperty("2007-06-20T20:57:00+13:00");
        props.setModifiedProperty("2007-06-20T20:59:00.123-13:00");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pkg.save(baos);
        pkg = OPCPackage.open(new ByteArrayInputStream(baos.toByteArray()));
        
        // Check text properties first - should be unchanged
        assertEquals("Lorem Ipsum", props.getTitleProperty().getValue());
        assertEquals("Apache POI", props.getCreatorProperty().getValue());
        
        // Check the updated times
        //   2007-06-20T20:57:00+13:00
        // = 2007-06-20T07:57:00Z
        assertEquals("2007-06-20T07:57:00.000Z", df.format(props.getCreatedProperty().getValue()));
        
        //   2007-06-20T20:59:00.123-13:00
        // = 2007-06-21T09:59:00.123Z
        assertEquals("2007-06-21T09:59:00.123Z", df.format(props.getModifiedProperty().getValue()));
	}
}
