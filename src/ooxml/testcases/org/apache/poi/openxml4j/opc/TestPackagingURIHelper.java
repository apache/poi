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

import java.net.URI;

import junit.framework.TestCase;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;

public class TestPackagingURIHelper extends TestCase {

	/**
	 * Test relativizePartName() method.
	 */
	public void testRelativizeURI() throws Exception {
		URI uri1 = new URI("/word/document.xml");
		URI uri2 = new URI("/word/media/image1.gif");
		
		// Document to image is down a directory
		URI retURI1to2 = PackagingURIHelper.relativizeURI(uri1, uri2);
		assertEquals("media/image1.gif", retURI1to2.getPath());
		// Image to document is up a directory
		URI retURI2to1 = PackagingURIHelper.relativizeURI(uri2, uri1);
		assertEquals("../document.xml", retURI2to1.getPath());
		
		// Document and CustomXML parts totally different [Julien C.]
		URI uriCustomXml = new URI("/customXml/item1.xml");
		URI uriRes = PackagingURIHelper.relativizeURI(uri1, uriCustomXml);
		assertEquals("../customXml/item1.xml", uriRes.toString());

		// Document to itself is the same place (empty URI)
		URI retURI2 = PackagingURIHelper.relativizeURI(uri1, uri1);
		assertEquals("", retURI2.getPath());

		// relativization against root
		URI root = new URI("/");
        uriRes = PackagingURIHelper.relativizeURI(root, uri1);
        assertEquals("/word/document.xml", uriRes.toString());

        //URI compatible with MS Office and OpenOffice: leading slash is removed
        uriRes = PackagingURIHelper.relativizeURI(root, uri1, true);
        assertEquals("word/document.xml", uriRes.toString());
    }

	/**
	 * Test createPartName(String, y)
	 */
	public void testCreatePartNameRelativeString()
			throws InvalidFormatException {
		PackagePartName partNameToValid = PackagingURIHelper
				.createPartName("/word/media/image1.gif");

		OPCPackage pkg = OPCPackage.create("DELETEIFEXISTS.docx");
		// Base part
		PackagePartName nameBase = PackagingURIHelper
				.createPartName("/word/document.xml");
		PackagePart partBase = pkg.createPart(nameBase, ContentTypes.XML);
		// Relative part name
		PackagePartName relativeName = PackagingURIHelper.createPartName(
				"media/image1.gif", partBase);
		assertTrue("The part name must be equal to "
				+ partNameToValid.getName(), partNameToValid
				.equals(relativeName));
		pkg.revert();
	}

	/**
	 * Test createPartName(URI, y)
	 */
	public void testCreatePartNameRelativeURI() throws Exception {
		PackagePartName partNameToValid = PackagingURIHelper
				.createPartName("/word/media/image1.gif");

		OPCPackage pkg = OPCPackage.create("DELETEIFEXISTS.docx");
		// Base part
		PackagePartName nameBase = PackagingURIHelper
				.createPartName("/word/document.xml");
		PackagePart partBase = pkg.createPart(nameBase, ContentTypes.XML);
		// Relative part name
		PackagePartName relativeName = PackagingURIHelper.createPartName(
				new URI("media/image1.gif"), partBase);
		assertTrue("The part name must be equal to "
				+ partNameToValid.getName(), partNameToValid
				.equals(relativeName));
		pkg.revert();
	}
}
