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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.apache.poi.openxml4j.opc.Package;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;

import org.apache.poi.openxml4j.TestCore;
import junit.framework.TestCase;

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
 * @version 1.0
 */
public class OPCCompliance_CoreProperties extends TestCase {

	TestCore testCore = new TestCore(this.getClass());

	public void testCorePropertiesPart() {
		Package pkg = null;
		try {
			String filepath = System.getProperty("openxml4j.compliance.input")
					+ File.separator
					+ "OPCCompliance_CoreProperties_OnlyOneCorePropertiesPart.docx";
			pkg = Package.open(filepath);
			// Normally must thrown an InvalidFormatException exception.
		} catch (InvalidFormatException e) {
			fail("OPC compliance failure: the core properties is considered as invalid than it's not !");
		} finally {
			pkg.revert();
		}
	}

	/**
	 * Test M4.1 rule.
	 */
	public void testOnlyOneCorePropertiesPart() {
		Package pkg = null;
		try {
			String filepath = System.getProperty("openxml4j.compliance.input")
					+ File.separator
					+ "OPCCompliance_CoreProperties_OnlyOneCorePropertiesPartFAIL.docx";
			pkg = Package.open(filepath);
			// Normally must thrown an InvalidFormatException exception.
			fail("OPC compliance failure: M4.1 -> A format consumer shall consider more than one core properties relationship for a package to be an error.");
		} catch (InvalidFormatException e) {
			// DO nothing, it's the normal behavior
		} finally {
			if (pkg != null)
				pkg.revert();
		}
	}

	/**
	 * Test M4.1 rule.
	 */
	public void testOnlyOneCorePropertiesPart_AddRelationship() {
		Package pkg = null;
		try {
			String filepath = System.getProperty("openxml4j.testdata.input")
					+ File.separator
					+ "OPCCompliance_CoreProperties_OnlyOneCorePropertiesPart.docx";
			pkg = Package.open(filepath);
			pkg.addRelationship(PackagingURIHelper.createPartName(new URI(
					"/docProps/core2.xml")), TargetMode.INTERNAL,
					PackageRelationshipTypes.CORE_PROPERTIES);
			// Normally must thrown an InvalidFormatException exception.
			fail("OPC compliance failure: M4.1 -> A format consumer shall consider more than one core properties relationship for a package to be an error.");
		} catch (InvalidOperationException e) {
			// Do nothing, it's the normal behavior
		} catch (InvalidFormatException e) {
			// Do nothing, it's the normal behavior
		} catch (URISyntaxException e) {
			// Should never happen
		} finally {
			if (pkg != null)
				pkg.revert();
		}
	}

	/**
	 * Test M4.1 rule.
	 */
	public void testOnlyOneCorePropertiesPart_AddPart() {
		Package pkg = null;
		try {
			String filepath = System.getProperty("openxml4j.testdata.input")
					+ File.separator
					+ "OPCCompliance_CoreProperties_OnlyOneCorePropertiesPart.docx";
			pkg = Package.open(filepath);
			pkg.createPart(PackagingURIHelper.createPartName(new URI(
					"/docProps/core2.xml")), ContentTypes.CORE_PROPERTIES_PART);
			// Normally must thrown an InvalidFormatException exception.
			fail("OPC compliance failure: M4.1 -> A format consumer shall consider more than one core properties relationship for a package to be an error.");
		} catch (InvalidFormatException e) {
			// Do nothing, it's the normal behavior
		} catch (InvalidOperationException e) {
			// Do nothing, it's the normal behavior
		} catch (URISyntaxException e) {
			// Should never happen
		} finally {
			if (pkg != null)
				pkg.revert();
		}
	}

	/**
	 * Test M4.2 rule.
	 */
	public void testDoNotUseCompatibilityMarkup() {
		Package pkg = null;
		try {
			String filepath = System.getProperty("openxml4j.compliance.input")
					+ File.separator
					+ "OPCCompliance_CoreProperties_DoNotUseCompatibilityMarkupFAIL.docx";
			pkg = Package.open(filepath);
			// Normally must thrown an InvalidFormatException exception.
			fail("OPC compliance failure: M4.2 -> A format consumer shall consider the use of the Markup Compatibility namespace to be an error.");
		} catch (InvalidFormatException e) {
			// Do nothing, it's the normal behavior
		} finally {
			if (pkg != null)
				pkg.revert();
		}
	}

	/**
	 * Test M4.3 rule.
	 */
	public void testDCTermsNamespaceLimitedUse() {
		Package pkg = null;
		try {
			String filepath = System.getProperty("openxml4j.compliance.input")
					+ File.separator
					+ "OPCCompliance_CoreProperties_DCTermsNamespaceLimitedUseFAIL.docx";
			pkg = Package.open(filepath);
			// Normally must thrown an InvalidFormatException exception.
			fail("OPC compliance failure: M4.3 -> Producers shall not create a document element that contains refinements to the Dublin Core elements, except for the two specified in the schema: <dcterms:created> and <dcterms:modified> Consumers shall consider a document element that violates this constraint to be an error.");
		} catch (InvalidFormatException e) {
			// Do nothing, it's the normal behavior
		} finally {
			if (pkg != null)
				pkg.revert();
		}
	}

	/**
	 * Test M4.4 rule.
	 */
	public void testUnauthorizedXMLLangAttribute() {
		Package pkg = null;
		try {
			String filepath = System.getProperty("openxml4j.compliance.input")
					+ File.separator
					+ "OPCCompliance_CoreProperties_UnauthorizedXMLLangAttributeFAIL.docx";
			pkg = Package.open(filepath);
			// Normally must thrown an InvalidFormatException exception.
			fail("OPC compliance failure: M4.4 -> Producers shall not create a document element that contains refinements to the Dublin Core elements, except for the two specified in the schema: <dcterms:created> and <dcterms:modified> Consumers shall consider a document element that violates this constraint to be an error.");
		} catch (InvalidFormatException e) {
			// Do nothing, it's the normal behavior
		} finally {
			if (pkg != null)
				pkg.revert();
		}
	}

	/**
	 * Test M4.5 rule.
	 */
	public void testLimitedXSITypeAttribute_NotPresent() {
		Package pkg = null;
		try {
			String filepath = System.getProperty("openxml4j.compliance.input")
					+ File.separator
					+ "OPCCompliance_CoreProperties_LimitedXSITypeAttribute_NotPresentFAIL.docx";
			pkg = Package.open(filepath);
			// Normally must thrown an InvalidFormatException exception.
			fail("OPC compliance failure: M4.5 -> Producers shall not create a document element that contains refinements to the Dublin Core elements, except for the two specified in the schema: <dcterms:created> and <dcterms:modified> Consumers shall consider a document element that violates this constraint to be an error.");
		} catch (InvalidFormatException e) {
			// Do nothing, it's the normal behavior
		} finally {
			if (pkg != null)
				pkg.revert();
		}
	}

	/**
	 * Test M4.5 rule.
	 */
	public void testLimitedXSITypeAttribute_PresentWithUnauthorizedValue() {
		Package pkg = null;
		try {
			String filepath = System.getProperty("openxml4j.compliance.input")
					+ File.separator
					+ "OPCCompliance_CoreProperties_LimitedXSITypeAttribute_PresentWithUnauthorizedValueFAIL.docx";
			pkg = Package.open(filepath);
			// Normally must thrown an InvalidFormatException exception.
			fail("OPC compliance failure: M4.5 -> Producers shall not create a document element that contains refinements to the Dublin Core elements, except for the two specified in the schema: <dcterms:created> and <dcterms:modified> Consumers shall consider a document element that violates this constraint to be an error.");
		} catch (InvalidFormatException e) {
			// Do nothing, it's the normal behavior
		} finally {
			if (pkg != null)
				pkg.revert();
		}
	}
}
