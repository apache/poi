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


import junit.framework.TestCase;
import org.apache.poi.hslf.record.*;
import org.apache.poi.POIDataSamples;

/**
 * Tests that HSLFSlideShow returns the right numbers of key records when
 * it parses the test file
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestRecordCounts extends TestCase {
	// HSLFSlideShow primed on the test data
	private HSLFSlideShow ss;

	public TestRecordCounts() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
		ss = new HSLFSlideShow(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
	}

	public void testSheetsCount() {
		// Top level
		Record[] r = ss.getRecords();

		int count = 0;
		for(int i=0; i<r.length; i++) {
			if(r[i] instanceof Slide) {
				count++;
			}
		}
		// Currently still sees the Master Sheet, but might not in the future
		assertEquals(3,count);
	}

	public void testNotesCount() {
		// Top level
		Record[] r = ss.getRecords();

		int count = 0;
		for(int i=0; i<r.length; i++) {
			if(r[i] instanceof Notes &&
			r[i].getRecordType() == 1008l) {
				count++;
			}
		}
		// Two real sheets, plus the master sheet
		assertEquals(3,count);
	}

	public void testSlideListWithTextCount() {
		// Second level
		Record[] rt = ss.getRecords();
		Record[] r = rt[0].getChildRecords();

		int count = 0;
		for(int i=0; i<r.length; i++) {
			if(r[i] instanceof SlideListWithText &&
			r[i].getRecordType() == 4080l) {
				count++;
			}
		}
		// Two real sheets, plus the master sheet
		assertEquals(3,count);
	}
}
