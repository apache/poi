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

package org.apache.poi.hslf.record;

import junit.framework.TestCase;

import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.poifs.filesystem.*;
import org.apache.poi.POIDataSamples;

/**
 * Tests that Document works properly (Also tests Environment while we're at it)
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestDocument extends TestCase {
	// HSLFSlideShow primed on the test data
	private HSLFSlideShow ss;
	// POIFS primed on the test data
	private POIFSFileSystem pfs;

	public TestDocument() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
		pfs = new POIFSFileSystem(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
		ss = new HSLFSlideShow(pfs);
	}

	private Document getDocRecord() {
		Record[] r = ss.getRecords();
		for (int i = (r.length - 1); i >= 0; i--) {
			if (r[i] instanceof Document) {
				return (Document) r[i];
			}
		}
		throw new IllegalStateException("No Document record found");
	}

	public void testRecordType() {
		Document dr = getDocRecord();
		assertEquals(1000, dr.getRecordType());
	}

	public void testChildRecords() {
		Document dr = getDocRecord();
		assertNotNull(dr.getDocumentAtom());

		assertNotNull(dr.getEnvironment());

		assertNotNull(dr.getSlideListWithTexts());
		assertEquals(3, dr.getSlideListWithTexts().length);
		assertNotNull(dr.getSlideListWithTexts()[0]);
		assertNotNull(dr.getSlideListWithTexts()[1]);
		assertNotNull(dr.getSlideListWithTexts()[2]);
	}

	public void testEnvironment() {
		Document dr = getDocRecord();
		Environment env = dr.getEnvironment();

		assertEquals(1010, env.getRecordType());
		assertNotNull(env.getFontCollection());
	}

	// No need to check re-writing - hslf.TestReWrite does all that for us
}
