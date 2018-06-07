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

package org.apache.poi.openxml4j.opc.compliance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.junit.Test;

import junit.framework.AssertionFailedError;

import static org.junit.Assert.*;

/**
 * Test core properties Open Packaging Convention compliance.
 * 
 * M4.1: The format designer shall specify and the format producer shall create
 * at most one core properties relationship for a package. A format consumer
 * shall consider more than one core properties relationship for a package to be
 * an error. If present, the relationship shall target the Core Properties part.
 * (POI relaxes this on reading, as Office sometimes breaks this)
 * 
 * M4.2: The format designer shall not specify and the format producer shall not
 * create Core Properties that use the Markup Compatibility namespace as defined
 * in Annex F, "Standard Namespaces and Content Types". A format consumer shall
 * consider the use of the Markup Compatibility namespace to be an error.
 * 
 * M4.3: Producers shall not create a document element that contains refinements
 * to the Dublin Core elements, except for the two specified in the schema:
 * <dcterms:created> and <dcterms:modified> Consumers shall consider a document
 * element that violates this constraint to be an error.
 * 
 * M4.4: Producers shall not create a document element that contains the
 * xml:lang attribute. Consumers shall consider a document element that violates
 * this constraint to be an error.
 * 
 * M4.5: Producers shall not create a document element that contains the
 * xsi:type attribute, except for a <dcterms:created> or <dcterms:modified>
 * element where the xsi:type attribute shall be present and shall hold the
 * value dcterms:W3CDTF, where dcterms is the namespace prefix of the Dublin
 * Core namespace. Consumers shall consider a document element that violates
 * this constraint to be an error.
 * 
 * @author Julien Chable
 */
public final class TestOPCComplianceCoreProperties {

    @Test
    public void testCorePropertiesPart() {
        OPCPackage pkg;
        try {
            InputStream is = OpenXML4JTestDataSamples.openComplianceSampleStream("OPCCompliance_CoreProperties_OnlyOneCorePropertiesPart.docx");
            pkg = OPCPackage.open(is);
        } catch (InvalidFormatException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pkg.revert();
    }

    private static String extractInvalidFormatMessage(String sampleNameSuffix) {
        InputStream is = OpenXML4JTestDataSamples.openComplianceSampleStream("OPCCompliance_CoreProperties_" + sampleNameSuffix);
        OPCPackage pkg;
        try {
            pkg = OPCPackage.open(is);
        } catch (InvalidFormatException e) {
            // no longer required for successful test
            return e.getMessage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pkg.revert();
        throw new AssertionFailedError("expected OPC compliance exception was not thrown");
    }
    
    /**
     * Test M4.1 rule.
     */
    @Test
    public void testOnlyOneCorePropertiesPart() throws Exception {
       // We have relaxed this check, so we can read the file anyway
       try {
          extractInvalidFormatMessage("OnlyOneCorePropertiesPartFAIL.docx");
          fail("M4.1 should be being relaxed");
       } catch (AssertionFailedError e) {
           // expected here
       }
       
       // We will use the first core properties, and ignore the others
      InputStream is = OpenXML4JTestDataSamples.openSampleStream("MultipleCoreProperties.docx");
      OPCPackage pkg = OPCPackage.open(is);
      
      // We can see 2 by type
      assertEquals(2, pkg.getPartsByContentType(ContentTypes.CORE_PROPERTIES_PART).size());
      // But only the first one by relationship
      assertEquals(1, pkg.getPartsByRelationshipType(PackageRelationshipTypes.CORE_PROPERTIES).size());
      // It should be core.xml not the older core1.xml
      assertEquals(
            "/docProps/core.xml",
            pkg.getPartsByRelationshipType(PackageRelationshipTypes.CORE_PROPERTIES).get(0).getPartName().toString()
      );
    }
    
    private static URI createURI(String text) {
        try {
            return new URI(text);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Test M4.1 rule.
     */
    @Test
    public void testOnlyOneCorePropertiesPart_AddRelationship() {
        InputStream is = OpenXML4JTestDataSamples.openComplianceSampleStream("OPCCompliance_CoreProperties_OnlyOneCorePropertiesPart.docx");
        OPCPackage pkg;
        try {
            pkg = OPCPackage.open(is);
        } catch (InvalidFormatException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        URI partUri = createURI("/docProps/core2.xml");
        try {
            pkg.addRelationship(PackagingURIHelper.createPartName(partUri), TargetMode.INTERNAL,
                    PackageRelationshipTypes.CORE_PROPERTIES);
            // no longer fail on compliance error
            //fail("expected OPC compliance exception was not thrown");
        } catch (InvalidFormatException e) {
            throw new RuntimeException(e);
        } catch (InvalidOperationException e) {
            // expected during successful test
            assertEquals("OPC Compliance error [M4.1]: can't add another core properties part ! Use the built-in package method instead.", e.getMessage());
        }
        pkg.revert();
    }

    /**
     * Test M4.1 rule.
     */
    @Test
    public void testOnlyOneCorePropertiesPart_AddPart() throws InvalidFormatException {
        String sampleFileName = "OPCCompliance_CoreProperties_OnlyOneCorePropertiesPart.docx";
        OPCPackage pkg = OPCPackage.open(POIDataSamples.getOpenXML4JInstance().getFile(sampleFileName).getPath());

        URI partUri = createURI("/docProps/core2.xml");
        try {
            pkg.createPart(PackagingURIHelper.createPartName(partUri),
                    ContentTypes.CORE_PROPERTIES_PART);
            // no longer fail on compliance error
            //fail("expected OPC compliance exception was not thrown");
        } catch (InvalidOperationException e) {
            // expected during successful test
            assertEquals("OPC Compliance error [M4.1]: you try to add more than one core properties relationship in the package !", e.getMessage());
        }
        pkg.revert();
    }

    /**
     * Test M4.2 rule.
     */
    @Test
    public void testDoNotUseCompatibilityMarkup() {
        String msg = extractInvalidFormatMessage("DoNotUseCompatibilityMarkupFAIL.docx");
        assertEquals("OPC Compliance error [M4.2]: A format consumer shall consider the use of the Markup Compatibility namespace to be an error.", msg);
    }

    /**
     * Test M4.3 rule.
     */
    @Test
    public void testDCTermsNamespaceLimitedUse() {
        String msg = extractInvalidFormatMessage("DCTermsNamespaceLimitedUseFAIL.docx");
        assertEquals("OPC Compliance error [M4.3]: Producers shall not create a document element that contains refinements to the Dublin Core elements, except for the two specified in the schema: <dcterms:created> and <dcterms:modified> Consumers shall consider a document element that violates this constraint to be an error.", msg);
    }

    /**
     * Test M4.4 rule.
     */
    @Test
    public void testUnauthorizedXMLLangAttribute() {
        String msg = extractInvalidFormatMessage("UnauthorizedXMLLangAttributeFAIL.docx");
        assertEquals("OPC Compliance error [M4.4]: Producers shall not create a document element that contains the xml:lang attribute. Consumers shall consider a document element that violates this constraint to be an error.", msg);
    }

    /**
     * Test M4.5 rule.
     */
    @Test
    public void testLimitedXSITypeAttribute_NotPresent() {
        String msg = extractInvalidFormatMessage("LimitedXSITypeAttribute_NotPresentFAIL.docx");
        assertEquals("The element 'created' must have the 'xsi:type' attribute present !", msg);
    }

    /**
     * Test M4.5 rule.
     */
    @Test
    public void testLimitedXSITypeAttribute_PresentWithUnauthorizedValue() {
        String msg = extractInvalidFormatMessage("LimitedXSITypeAttribute_PresentWithUnauthorizedValueFAIL.docx");
        assertEquals("The element 'modified' must have the 'xsi:type' attribute with the value 'dcterms:W3CDTF', but had 'W3CDTF' !", msg);
    }
    
    /**
     * Document with no core properties - testing at the OPC level,
     *  saving into a new stream
     */
    @Test
    public void testNoCoreProperties_saveNew() throws Exception {
        String sampleFileName = "OPCCompliance_NoCoreProperties.xlsx";
        OPCPackage pkg = OPCPackage.open(POIDataSamples.getOpenXML4JInstance().getFile(sampleFileName).getPath());

        // Verify it has empty properties
        assertEquals(0, pkg.getPartsByContentType(ContentTypes.CORE_PROPERTIES_PART).size());
        assertNotNull(pkg.getPackageProperties());
        assertNotNull(pkg.getPackageProperties().getLanguageProperty());
        assertFalse(pkg.getPackageProperties().getLanguageProperty().isPresent());

        // Save and re-load
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pkg.save(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        pkg.revert();

        pkg = OPCPackage.open(bais);

        // An Empty Properties part has been added in the save/load
        assertEquals(1, pkg.getPartsByContentType(ContentTypes.CORE_PROPERTIES_PART).size());
        assertNotNull(pkg.getPackageProperties());
        assertNotNull(pkg.getPackageProperties().getLanguageProperty());
        assertFalse(pkg.getPackageProperties().getLanguageProperty().isPresent());
        pkg.close();
        
        // Open a new copy of it
        pkg = OPCPackage.open(POIDataSamples.getOpenXML4JInstance().getFile(sampleFileName).getPath());
        
        // Save and re-load, without having touched the properties yet
        baos = new ByteArrayOutputStream();
        pkg.save(baos);
        pkg.revert();

        bais = new ByteArrayInputStream(baos.toByteArray());
        pkg = OPCPackage.open(bais);
        
        // Check that this too added empty properties without error
        assertEquals(1, pkg.getPartsByContentType(ContentTypes.CORE_PROPERTIES_PART).size());
        assertNotNull(pkg.getPackageProperties());
        assertNotNull(pkg.getPackageProperties().getLanguageProperty());
        assertFalse(pkg.getPackageProperties().getLanguageProperty().isPresent());
    }

    /**
     * Document with no core properties - testing at the OPC level,
     *  from a temp-file, saving in-place
     */
    @Test
    public void testNoCoreProperties_saveInPlace() throws Exception {
        String sampleFileName = "OPCCompliance_NoCoreProperties.xlsx";

        // Copy this into a temp file, so we can play with it
        File tmp = TempFile.createTempFile("poi-test", ".opc");
        FileOutputStream out = new FileOutputStream(tmp);
        InputStream in = POIDataSamples.getOpenXML4JInstance().openResourceAsStream(sampleFileName);
        IOUtils.copy(
                in,
                out);
        out.close();
        in.close();

        // Open it from that temp file
        OPCPackage pkg = OPCPackage.open(tmp);

        // Empty properties
        assertEquals(0, pkg.getPartsByContentType(ContentTypes.CORE_PROPERTIES_PART).size());
        assertNotNull(pkg.getPackageProperties());
        assertNotNull(pkg.getPackageProperties().getLanguageProperty());
        assertFalse(pkg.getPackageProperties().getLanguageProperty().isPresent());

        // Save and close
        pkg.close();

        // Re-open and check
        pkg = OPCPackage.open(tmp);

        // An Empty Properties part has been added in the save/load
        assertEquals(1, pkg.getPartsByContentType(ContentTypes.CORE_PROPERTIES_PART).size());
        assertNotNull(pkg.getPackageProperties());
        assertNotNull(pkg.getPackageProperties().getLanguageProperty());
        assertFalse(pkg.getPackageProperties().getLanguageProperty().isPresent());

        // Finish and tidy
        pkg.revert();
        assertTrue(tmp.delete());
    }
}
