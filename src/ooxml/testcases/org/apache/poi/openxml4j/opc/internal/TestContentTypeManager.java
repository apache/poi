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

package org.apache.poi.openxml4j.opc.internal;

import junit.framework.TestCase;

import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;

public final class TestContentTypeManager extends TestCase {

	/**
	 * Test the properties part content parsing.
	 */
	public void disabled_testContentType() throws Exception {
		String filepath =  OpenXML4JTestDataSamples.getSampleFileName("sample.docx");

		 // Retrieves core properties part
		 OPCPackage p = OPCPackage.open(filepath, PackageAccess.READ);
		 PackageRelationship corePropertiesRelationship = p
		 .getRelationshipsByType(
		 PackageRelationshipTypes.CORE_PROPERTIES)
		 .getRelationship(0);
		 PackagePart coreDocument = p.getPart(corePropertiesRelationship);

		 ContentTypeManager ctm = new ZipContentTypeManager(coreDocument.getInputStream(), p);

		 // TODO - finish writing this test
		fail();
	}

	/**
	 * Test the addition of several default and override content types.
	 */
	public void testContentTypeAddition() throws Exception {
		ContentTypeManager ctm = new ZipContentTypeManager(null, null);

		PackagePartName name1 = PackagingURIHelper.createPartName("/foo/foo.XML");
		PackagePartName name2 = PackagingURIHelper.createPartName("/foo/foo2.xml");
		PackagePartName name3 = PackagingURIHelper.createPartName("/foo/doc.rels");
		PackagePartName name4 = PackagingURIHelper.createPartName("/foo/doc.RELS");

		// Add content types
		ctm.addContentType(name1, "foo-type1");
		ctm.addContentType(name2, "foo-type2");
		ctm.addContentType(name3, "text/xml+rel");
		ctm.addContentType(name4, "text/xml+rel");

		assertEquals(ctm.getContentType(name1), "foo-type1");
		assertEquals(ctm.getContentType(name2), "foo-type2");
		assertEquals(ctm.getContentType(name3), "text/xml+rel");
		assertEquals(ctm.getContentType(name3), "text/xml+rel");
	}

	/**
	 * Test the addition then removal of content types.
	 */
	public void testContentTypeRemoval() throws Exception {
		ContentTypeManager ctm = new ZipContentTypeManager(null, null);

		PackagePartName name1 = PackagingURIHelper.createPartName("/foo/foo.xml");
		PackagePartName name2 = PackagingURIHelper.createPartName("/foo/foo2.xml");
		PackagePartName name3 = PackagingURIHelper.createPartName("/foo/doc.rels");
		PackagePartName name4 = PackagingURIHelper.createPartName("/foo/doc.RELS");

		// Add content types
		ctm.addContentType(name1, "foo-type1");
		ctm.addContentType(name2, "foo-type2");
		ctm.addContentType(name3, "text/xml+rel");
		ctm.addContentType(name4, "text/xml+rel");
		ctm.removeContentType(name2);
		ctm.removeContentType(name3);

		assertEquals(ctm.getContentType(name1), "foo-type1");
		assertEquals(ctm.getContentType(name2), "foo-type1");
		assertEquals(ctm.getContentType(name3), null);

		ctm.removeContentType(name1);
		assertEquals(ctm.getContentType(name1), null);
		assertEquals(ctm.getContentType(name2), null);
	}

	/**
	 * Test the addition then removal of content types in a package.
	 */
	public void testContentTypeRemovalPackage() {
		// TODO
	}
}
