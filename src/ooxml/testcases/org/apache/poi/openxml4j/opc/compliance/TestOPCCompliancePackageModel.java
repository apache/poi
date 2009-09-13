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
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.POIDataSamples;

/**
 * Test Open Packaging Convention package model compliance.
 *
 * M1.11 : A package implementer shall neither create nor recognize a part with
 * a part name derived from another part name by appending segments to it.
 *
 * @author Julien Chable
 */
public class TestOPCCompliancePackageModel extends TestCase {

	/**
	 * A package implementer shall neither create nor recognize a part with a
	 * part name derived from another part name by appending segments to it.
	 * [M1.11]
	 */
	public void testPartNameDerivationAdditionFailure() {
		OPCPackage pkg = OPCPackage.create("TODELETEIFEXIST.docx");
		try {
			PackagePartName name = PackagingURIHelper
					.createPartName("/word/document.xml");
			PackagePartName nameDerived = PackagingURIHelper
					.createPartName("/word/document.xml/image1.gif");
			pkg.createPart(name, ContentTypes.XML);
			pkg.createPart(nameDerived, ContentTypes.EXTENSION_GIF);
		} catch (InvalidOperationException e) {
			pkg.revert();
			return;
		} catch (InvalidFormatException e) {
			fail(e.getMessage());
		}
		fail("A package implementer shall neither create nor recognize a part with a"
				+ " part name derived from another part name by appending segments to it."
				+ " [M1.11]");
	}

	/**
	 * A package implementer shall neither create nor recognize a part with a
	 * part name derived from another part name by appending segments to it.
	 * [M1.11]
	 */
	public void testPartNameDerivationReadingFailure() throws IOException {
		String filename = "OPCCompliance_DerivedPartNameFAIL.docx";
		try {
			OPCPackage.open(POIDataSamples.getOpenXML4JInstance().openResourceAsStream(filename));
		} catch (InvalidFormatException e) {
			return;
		}
		fail("A package implementer shall neither create nor recognize a part with a"
				+ " part name derived from another part name by appending segments to it."
				+ " [M1.11]");
	}

	/**
	 * Rule M1.12 : Packages shall not contain equivalent part names and package
	 * implementers shall neither create nor recognize packages with equivalent
	 * part names.
	 */
	public void testAddPackageAlreadyAddFailure() throws Exception {
		OPCPackage pkg = OPCPackage.create("DELETEIFEXISTS.docx");
		PackagePartName name1 = null;
		PackagePartName name2 = null;
		try {
			name1 = PackagingURIHelper.createPartName("/word/document.xml");
			name2 = PackagingURIHelper.createPartName("/word/document.xml");
		} catch (InvalidFormatException e) {
			throw new Exception(e.getMessage());
		}
		pkg.createPart(name1, ContentTypes.XML);
		try {
			pkg.createPart(name2, ContentTypes.XML);
		} catch (InvalidOperationException e) {
			return;
		}
		fail("Packages shall not contain equivalent part names and package implementers shall neither create nor recognize packages with equivalent part names. [M1.12]");
	}

	/**
	 * Rule M1.12 : Packages shall not contain equivalent part names and package
	 * implementers shall neither create nor recognize packages with equivalent
	 * part names.
	 */
	public void testAddPackageAlreadyAddFailure2() throws Exception {
		OPCPackage pkg = OPCPackage.create("DELETEIFEXISTS.docx");
		PackagePartName partName = null;
		try {
			partName = PackagingURIHelper.createPartName("/word/document.xml");
		} catch (InvalidFormatException e) {
			throw new Exception(e.getMessage());
		}
		pkg.createPart(partName, ContentTypes.XML);
		try {
			pkg.createPart(partName, ContentTypes.XML);
		} catch (InvalidOperationException e) {
			return;
		}
		fail("Packages shall not contain equivalent part names and package implementers shall neither create nor recognize packages with equivalent part names. [M1.12]");
	}

	/**
	 * Try to add a relationship to a relationship part.
	 *
	 * Check rule M1.25: The Relationships part shall not have relationships to
	 * any other part. Package implementers shall enforce this requirement upon
	 * the attempt to create such a relationship and shall treat any such
	 * relationship as invalid.
	 */
	public void testAddRelationshipRelationshipsPartFailure() {
		OPCPackage pkg = OPCPackage.create("DELETEIFEXISTS.docx");
		PackagePartName name1 = null;
		try {
			name1 = PackagingURIHelper
					.createPartName("/test/_rels/document.xml.rels");
		} catch (InvalidFormatException e) {
			fail("This exception should never happen !");
		}

		try {
			pkg.addRelationship(name1, TargetMode.INTERNAL,
					PackageRelationshipTypes.CORE_DOCUMENT);
		} catch (InvalidOperationException e) {
			return;
		}
		fail("Fail test -> M1.25: The Relationships part shall not have relationships to any other part");
	}
}
