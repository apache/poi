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

import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;

public final class TestListParts extends TestCase {
    private static final POILogger logger = POILogFactory.getLogger(TestListParts.class);

	private TreeMap<PackagePartName, String> expectedValues;

	private TreeMap<PackagePartName, String> values;

	@Override
	protected void setUp() throws Exception {
		values = new TreeMap<PackagePartName, String>();

		// Expected values
		expectedValues = new TreeMap<PackagePartName, String>();
		expectedValues.put(PackagingURIHelper.createPartName("/_rels/.rels"),
				"application/vnd.openxmlformats-package.relationships+xml");

		expectedValues
				.put(PackagingURIHelper.createPartName("/docProps/app.xml"),
						"application/vnd.openxmlformats-officedocument.extended-properties+xml");
		expectedValues.put(PackagingURIHelper
				.createPartName("/docProps/core.xml"),
				"application/vnd.openxmlformats-package.core-properties+xml");
		expectedValues.put(PackagingURIHelper
				.createPartName("/word/_rels/document.xml.rels"),
				"application/vnd.openxmlformats-package.relationships+xml");
		expectedValues
				.put(
						PackagingURIHelper.createPartName("/word/document.xml"),
						"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml");
		expectedValues
				.put(PackagingURIHelper.createPartName("/word/fontTable.xml"),
						"application/vnd.openxmlformats-officedocument.wordprocessingml.fontTable+xml");
		expectedValues.put(PackagingURIHelper
				.createPartName("/word/media/image1.gif"), "image/gif");
		expectedValues
				.put(PackagingURIHelper.createPartName("/word/settings.xml"),
						"application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml");
		expectedValues
				.put(PackagingURIHelper.createPartName("/word/styles.xml"),
						"application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml");
		expectedValues.put(PackagingURIHelper
				.createPartName("/word/theme/theme1.xml"),
				"application/vnd.openxmlformats-officedocument.theme+xml");
		expectedValues
				.put(
						PackagingURIHelper
								.createPartName("/word/webSettings.xml"),
						"application/vnd.openxmlformats-officedocument.wordprocessingml.webSettings+xml");
	}

	/**
	 * List all parts of a package.
	 */
	public void testListParts() throws InvalidFormatException {
		InputStream is = OpenXML4JTestDataSamples.openSampleStream("sample.docx");

		OPCPackage p;
		try {
			p = OPCPackage.open(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		for (PackagePart part : p.getParts()) {
			values.put(part.getPartName(), part.getContentType());
			logger.log(POILogger.DEBUG, part.getPartName());
		}

		// Compare expected values with values return by the package
		for (PackagePartName partName : expectedValues.keySet()) {
			assertNotNull(values.get(partName));
			assertEquals(expectedValues.get(partName), values.get(partName));
		}
	}
}
