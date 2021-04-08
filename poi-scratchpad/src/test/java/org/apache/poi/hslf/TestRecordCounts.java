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

package org.apache.poi.hslf;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.record.Notes;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.Slide;
import org.apache.poi.hslf.record.SlideListWithText;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that HSLFSlideShow returns the right numbers of key records when
 * it parses the test file
 */
public final class TestRecordCounts {

	private static final POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
	// HSLFSlideShow primed on the test data
	private HSLFSlideShowImpl ss;

	@BeforeEach
	void setup() throws Exception {
		ss = new HSLFSlideShowImpl(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
	}

	@Test
	void testSheetsCount() {
		// Top level
		Record[] r = ss.getRecords();

		int count = 0;
		for (final Record rec : r) {
			if(rec instanceof Slide) {
				count++;
			}
		}
		// Currently still sees the Master Sheet, but might not in the future
		assertEquals(3,count);
	}

	@Test
	void testNotesCount() {
		// Top level
		Record[] r = ss.getRecords();

		int count = 0;
		for (final Record rec : r) {
			if (rec instanceof Notes && rec.getRecordType() == 1008L) {
				count++;
			}
		}
		// Two real sheets, plus the master sheet
		assertEquals(3,count);
	}

	@Test
	void testSlideListWithTextCount() {
		// Second level
		Record[] rt = ss.getRecords();
		Record[] r = rt[0].getChildRecords();

		int count = 0;
		for (final Record rec : r) {
			if (rec instanceof SlideListWithText && rec.getRecordType() == 4080L) {
				count++;
			}
		}
		// Two real sheets, plus the master sheet
		assertEquals(3,count);
	}
}
