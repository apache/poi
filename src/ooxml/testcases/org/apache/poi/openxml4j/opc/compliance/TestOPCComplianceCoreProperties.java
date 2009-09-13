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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.POIDataSamples;

/**
 * Test core properties Open Packaging Convention compliance.
 * 
 * M4.1: The format designer shall specify and the format producer shall create
 * at most one core properties relationship for a package. A format consumer
 * shall consider more than one core properties relationship for a package to be
 * an error. If present, the relationship shall target the Core Properties part.
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
public final class TestOPCComplianceCoreProperties extends TestCase {

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
			// expected during successful test
			return e.getMessage();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		pkg.revert();
		// Normally must thrown an InvalidFormatException exception.
		throw new AssertionFailedError("expected OPC compliance exception was not thrown");
	}
	
	/**
	 * Test M4.1 rule.
	 */
	public void testOnlyOneCorePropertiesPart() {
		String msg = extractInvalidFormatMessage("OnlyOneCorePropertiesPartFAIL.docx");
		assertEquals("OPC Compliance error [M4.1]: there is more than one core properties relationship in the package !", msg);
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
			fail("expected OPC compliance exception was not thrown");
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
	public void testOnlyOneCorePropertiesPart_AddPart() {
		String sampleFileName = "OPCCompliance_CoreProperties_OnlyOneCorePropertiesPart.docx";
		OPCPackage pkg = null;
		try {
			pkg = OPCPackage.open(POIDataSamples.getOpenXML4JInstance().getFile(sampleFileName).getPath());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		URI partUri = createURI("/docProps/core2.xml");
		try {
			pkg.createPart(PackagingURIHelper.createPartName(partUri),
					ContentTypes.CORE_PROPERTIES_PART);
			fail("expected OPC compliance exception was not thrown");
		} catch (InvalidFormatException e) {
			throw new RuntimeException(e);
		} catch (InvalidOperationException e) {
			// expected during successful test
			assertEquals("OPC Compliance error [M4.1]: you try to add more than one core properties relationship in the package !", e.getMessage());
		}
		pkg.revert();
	}

	/**
	 * Test M4.2 rule.
	 */
	public void testDoNotUseCompatibilityMarkup() {
		String msg = extractInvalidFormatMessage("DoNotUseCompatibilityMarkupFAIL.docx");
		assertEquals("OPC Compliance error [M4.2]: A format consumer shall consider the use of the Markup Compatibility namespace to be an error.", msg);
	}

	/**
	 * Test M4.3 rule.
	 */
	public void testDCTermsNamespaceLimitedUse() {
		String msg = extractInvalidFormatMessage("DCTermsNamespaceLimitedUseFAIL.docx");
		assertEquals("OPC Compliance error [M4.3]: Producers shall not create a document element that contains refinements to the Dublin Core elements, except for the two specified in the schema: <dcterms:created> and <dcterms:modified> Consumers shall consider a document element that violates this constraint to be an error.", msg);
	}

	/**
	 * Test M4.4 rule.
	 */
	public void testUnauthorizedXMLLangAttribute() {
		String msg = extractInvalidFormatMessage("UnauthorizedXMLLangAttributeFAIL.docx");
		assertEquals("OPC Compliance error [M4.4]: Producers shall not create a document element that contains the xml:lang attribute. Consumers shall consider a document element that violates this constraint to be an error.", msg);
	}

	/**
	 * Test M4.5 rule.
	 */
	public void testLimitedXSITypeAttribute_NotPresent() {
		String msg = extractInvalidFormatMessage("LimitedXSITypeAttribute_NotPresentFAIL.docx");
		assertEquals("The element 'created' must have the 'xsi:type' attribute present !", msg);
	}

	/**
	 * Test M4.5 rule.
	 */
	public void testLimitedXSITypeAttribute_PresentWithUnauthorizedValue() {
		String msg = extractInvalidFormatMessage("LimitedXSITypeAttribute_PresentWithUnauthorizedValueFAIL.docx");
		assertEquals("The element 'modified' must have the 'xsi:type' attribute with the value 'dcterms:W3CDTF' !", msg);
	}
}
