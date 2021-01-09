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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that Document works properly (Also tests Environment while we're at it)
 */
public final class TestDocument {
	private static final POIDataSamples slTests = POIDataSamples.getSlideShowInstance();

	// HSLFSlideShow primed on the test data
	private HSLFSlideShowImpl ss;
	// POIFS primed on the test data
	private POIFSFileSystem pfs;

	@BeforeEach
	void setup() throws Exception {
		pfs = new POIFSFileSystem(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
		ss = new HSLFSlideShowImpl(pfs);
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

	@Test
	void testRecordType() {
		Document dr = getDocRecord();
		assertEquals(1000, dr.getRecordType());
	}

	@Test
	void testChildRecords() {
		Document dr = getDocRecord();
		assertNotNull(dr.getDocumentAtom());

		assertNotNull(dr.getEnvironment());

		assertNotNull(dr.getSlideListWithTexts());
		assertEquals(3, dr.getSlideListWithTexts().length);
		assertNotNull(dr.getSlideListWithTexts()[0]);
		assertNotNull(dr.getSlideListWithTexts()[1]);
		assertNotNull(dr.getSlideListWithTexts()[2]);
	}

	@Test
	void testEnvironment() {
		Document dr = getDocRecord();
		Environment env = dr.getEnvironment();

		assertEquals(1010, env.getRecordType());
		assertNotNull(env.getFontCollection());
	}

	// No need to check re-writing - hslf.TestReWrite does all that for us
}
