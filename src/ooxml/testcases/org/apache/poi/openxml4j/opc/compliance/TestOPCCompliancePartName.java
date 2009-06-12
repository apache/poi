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

import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;

/**
 * Test part name Open Packaging Convention compliance.
 *
 * (Open Packaging Convention 8.1.1 Part names) :
 *
 * The part name grammar is defined as follows:
 *
 * part_name = 1*( "/" segment )
 *
 * segment = 1*( pchar )
 *
 * pchar is defined in RFC 3986.
 *
 * The part name grammar implies the following constraints. The package
 * implementer shall neither create any part that violates these constraints nor
 * retrieve any data from a package as a part if the purported part name
 * violates these constraints.
 *
 * A part name shall not be empty. [M1.1]
 *
 * A part name shall not have empty segments. [M1.3]
 *
 * A part name shall start with a forward slash ("/") character. [M1.4]
 *
 * A part name shall not have a forward slash as the last character. [M1.5]
 *
 * A segment shall not hold any characters other than pchar characters. [M1.6]
 *
 * Part segments have the following additional constraints. The package
 * implementer shall neither create any part with a part name comprised of a
 * segment that violates these constraints nor retrieve any data from a package
 * as a part if the purported part name contains a segment that violates these
 * constraints.
 *
 * A segment shall not contain percent-encoded forward slash ("/"), or backward
 * slash ("\") characters. [M1.7]
 *
 * A segment shall not contain percent-encoded unreserved characters. [M1.8]
 *
 * A segment shall not end with a dot (".") character. [M1.9]
 *
 * A segment shall include at least one non-dot character. [M1.10]
 *
 * A package implementer shall neither create nor recognize a part with a part
 * name derived from another part name by appending segments to it. [M1.11]
 *
 * Part name equivalence is determined by comparing part names as
 * case-insensitive ASCII strings. [M1.12]
 *
 * @author Julien Chable
 */
public final class TestOPCCompliancePartName extends TestCase {

	/**
	 * Test some common invalid names.
	 *
	 * A segment shall not contain percent-encoded unreserved characters. [M1.8]
	 */
	public void testInvalidPartNames() {
		String[] invalidNames = { "/", "/xml./doc.xml", "[Content_Types].xml", "//xml/." };
		for (String s : invalidNames) {
			URI uri = null;
			try {
				uri = new URI(s);
			} catch (URISyntaxException e) {
				assertTrue(s == "[Content_Types].xml");
				continue;
			}
			assertFalse("This part name SHOULD NOT be valid: " + s,
					PackagingURIHelper.isValidPartName(uri));
		}
	}

	/**
	 * Test some common valid names.
	 */
	public void testValidPartNames() throws URISyntaxException {
		String[] validNames = { "/xml/item1.xml", "/document.xml",
				"/a/%D1%86.xml" };
		for (String s : validNames)
			assertTrue("This part name SHOULD be valid: " + s,
					PackagingURIHelper.isValidPartName(new URI(s)));
	}

	/**
	 * A part name shall not be empty. [M1.1]
	 */
	public void testEmptyPartNameFailure() throws URISyntaxException {
		try {
			PackagingURIHelper.createPartName(new URI(""));
			fail("A part name shall not be empty. [M1.1]");
		} catch (InvalidFormatException e) {
			// Normal behaviour
		}
	}

	/**
	 * A part name shall not have empty segments. [M1.3]
	 *
	 * A segment shall not end with a dot ('.') character. [M1.9]
	 *
	 * A segment shall include at least one non-dot character. [M1.10]
	 */
	public void testPartNameWithInvalidSegmentsFailure() {
		String[] invalidNames = { "//document.xml", "//word/document.xml",
				"/word//document.rels", "/word//rels//document.rels",
				"/xml./doc.xml", "/document.", "/./document.xml",
				"/word/./doc.rels", "/%2F/document.xml" };
		try {
			for (String s : invalidNames)
				assertFalse(
						"A part name shall not have empty segments. [M1.3]",
						PackagingURIHelper.isValidPartName(new URI(s)));
		} catch (URISyntaxException e) {
			fail();
		}
	}

	/**
	 * A segment shall not hold any characters other than ipchar (RFC 3987) characters.
	 * [M1.6].
	 */
	public void testPartNameWithNonPCharCharacters() {
		String[] validNames = { "/doc&.xml" };
		try {
			for (String s : validNames)
				assertTrue(
						"A segment shall not contain non pchar characters [M1.6] : "
								+ s, PackagingURIHelper
								.isValidPartName(new URI(s)));
		} catch (URISyntaxException e) {
			fail();
		}
	}

	/**
	 * A segment shall not contain percent-encoded unreserved characters [M1.8].
	 */
	public void testPartNameWithUnreservedEncodedCharactersFailure() {
		String[] invalidNames = { "/a/docum%65nt.xml" };
		try {
			for (String s : invalidNames)
				assertFalse(
						"A segment shall not contain percent-encoded unreserved characters [M1.8] : "
								+ s, PackagingURIHelper
								.isValidPartName(new URI(s)));
		} catch (URISyntaxException e) {
			fail();
		}
	}

	/**
	 * A part name shall start with a forward slash ('/') character. [M1.4]
	 */
	public void testPartNameStartsWithAForwardSlashFailure()
			throws URISyntaxException {
		try {
			PackagingURIHelper.createPartName(new URI("document.xml"));
			fail("A part name shall start with a forward slash ('/') character. [M1.4]");
		} catch (InvalidFormatException e) {
			// Normal behaviour
		}
	}

	/**
	 * A part name shall not have a forward slash as the last character. [M1.5]
	 */
	public void testPartNameEndsWithAForwardSlashFailure()
			throws URISyntaxException {
		try {
			PackagingURIHelper.createPartName(new URI("/document.xml/"));
			fail("A part name shall not have a forward slash as the last character. [M1.5]");
		} catch (InvalidFormatException e) {
			// Normal behaviour
		}
	}

	/**
	 * Part name equivalence is determined by comparing part names as
	 * case-insensitive ASCII strings. [M1.12]
	 */
	public void testPartNameComparaison() throws Exception {
		String[] partName1 = { "/word/document.xml", "/docProps/core.xml", "/rels/.rels" };
		String[] partName2 = { "/WORD/DocUment.XML", "/docProps/core.xml", "/rels/.rels" };
		for (int i = 0; i < partName1.length || i < partName2.length; ++i) {
			PackagePartName p1 = PackagingURIHelper.createPartName(partName1[i]);
			PackagePartName p2 = PackagingURIHelper.createPartName(partName2[i]);
			assertTrue(p1.equals(p2));
			assertTrue(p1.compareTo(p2) == 0);
			assertTrue(p1.hashCode() == p2.hashCode());
		}
	}

	/**
	 * Part name equivalence is determined by comparing part names as
	 * case-insensitive ASCII strings. [M1.12].
	 *
	 * All the comparisons MUST FAIL !
	 */
	public void testPartNameComparaisonFailure() throws Exception {
		String[] partName1 = { "/word/document.xml", "/docProps/core.xml", "/rels/.rels" };
		String[] partName2 = { "/WORD/DocUment.XML2", "/docProp/core.xml", "/rels/rels" };
		for (int i = 0; i < partName1.length || i < partName2.length; ++i) {
			PackagePartName p1 = PackagingURIHelper.createPartName(partName1[i]);
			PackagePartName p2 = PackagingURIHelper.createPartName(partName2[i]);
			assertFalse(p1.equals(p2));
			assertFalse(p1.compareTo(p2) == 0);
			assertFalse(p1.hashCode() == p2.hashCode());
		}
	}
}
