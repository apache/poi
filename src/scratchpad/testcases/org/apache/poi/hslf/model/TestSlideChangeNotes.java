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

package org.apache.poi.hslf.model;


import junit.framework.TestCase;

import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.record.SlideAtom;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.POIDataSamples;

/**
 * Tests that changing a slide's idea of what notes sheet is its works right
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestSlideChangeNotes extends TestCase {
	// SlideShow primed on the test data
	private SlideShow ss;

	public TestSlideChangeNotes() throws Exception {
        POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();
		HSLFSlideShow hss = new HSLFSlideShow(_slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
		ss = new SlideShow(hss);
	}

	public void testSetToNone() {
		Slide slideOne = ss.getSlides()[0];
		SlideAtom sa = slideOne.getSlideRecord().getSlideAtom();

		slideOne.setNotes(null);

		assertEquals(0, sa.getNotesID());
	}

	public void testSetToSomething() {
		Slide slideOne = ss.getSlides()[0];
		Notes notesOne = ss.getNotes()[1];
		SlideAtom sa = slideOne.getSlideRecord().getSlideAtom();

		slideOne.setNotes(notesOne);

		assertEquals(notesOne._getSheetNumber(), sa.getNotesID());
	}
}
