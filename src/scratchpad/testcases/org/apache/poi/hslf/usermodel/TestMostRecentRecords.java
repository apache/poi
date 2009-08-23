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

package org.apache.poi.hslf.usermodel;


import junit.framework.TestCase;
import org.apache.poi.hslf.*;
import org.apache.poi.hslf.record.*;
import org.apache.poi.POIDataSamples;

/**
 * Tests that SlideShow finds the right records as its most recent ones
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestMostRecentRecords extends TestCase {
	// HSLFSlideShow primed on the test data
	private HSLFSlideShow hss;
	// SlideShow primed on the test data
	private SlideShow ss;

	public TestMostRecentRecords() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
		hss = new HSLFSlideShow(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
		ss = new SlideShow(hss);
	}

	public void testCount() {
		// Most recent core records
		Record[] mrcr = ss.getMostRecentCoreRecords();

		// Master sheet + master notes + 2 slides + 2 notes + document
		assertEquals(7, mrcr.length);
	}

	public void testRightRecordTypes() {
		// Most recent core records
		Record[] mrcr = ss.getMostRecentCoreRecords();

		// Document
		assertEquals(1000, mrcr[0].getRecordType());
		// Notes of master
		assertEquals(1008, mrcr[1].getRecordType());
		// Master
		assertEquals(1016, mrcr[2].getRecordType());

		// Slide
		assertEquals(1006, mrcr[3].getRecordType());
		// Notes
		assertEquals(1008, mrcr[4].getRecordType());
		// Slide
		assertEquals(1006, mrcr[5].getRecordType());
		// Notes
		assertEquals(1008, mrcr[6].getRecordType());
	}

	public void testCorrectRecords() {
		// Most recent core records
		Record[] mrcr = ss.getMostRecentCoreRecords();

		// All records
		Record[] allr = hss.getRecords();

		// Ensure they are the right (latest) version of each

		// Document - late version
		assertEquals(allr[12], mrcr[0]);
		// Notes of master - unchanged
		assertEquals(allr[2], mrcr[1]);
		// Master - unchanged
		assertEquals(allr[1], mrcr[2]);

		// Slide - added at start
		assertEquals(allr[3], mrcr[3]);
		// Notes - added at start
		assertEquals(allr[4], mrcr[4]);
		// Slide - added later and then changed
		assertEquals(allr[13], mrcr[5]);
		// Notes - added later but not changed
		assertEquals(allr[9], mrcr[6]);
	}
}
