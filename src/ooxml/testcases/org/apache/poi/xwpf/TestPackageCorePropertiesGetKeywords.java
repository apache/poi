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

package org.apache.poi.xwpf;

import junit.framework.TestCase;

import org.apache.poi.POIXMLProperties.CoreProperties;
import org.apache.poi.openxml4j.opc.PackageProperties;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

/**
 * Tests if the {@link CoreProperties#getKeywords()} method. This test has been
 * submitted because even though the
 * {@link PackageProperties#getKeywordsProperty()} had been present before, the
 * {@link CoreProperties#getKeywords()} had been missing.
 * 
 * The author of this has added {@link CoreProperties#getKeywords()} and
 * {@link CoreProperties#setKeywords(String)} and this test is supposed to test
 * them.
 * 
 * @author Antoni Mylka
 * 
 */
public final class TestPackageCorePropertiesGetKeywords extends TestCase {
	public void testGetSetKeywords() {
		XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("TestPoiXMLDocumentCorePropertiesGetKeywords.docx");
		String keywords = doc.getProperties().getCoreProperties().getKeywords();
		assertEquals("extractor, test, rdf", keywords);
		
		doc.getProperties().getCoreProperties().setKeywords("test, keywords");
		doc = XWPFTestDataSamples.writeOutAndReadBack(doc);
		keywords = doc.getProperties().getCoreProperties().getKeywords();
		assertEquals("test, keywords",keywords);
	}
}
