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

package org.apache.poi.hslf.extractor;


import junit.framework.TestCase;
import java.util.Vector;

import org.apache.poi.POIDataSamples;

/**
 * Tests that the QuickButCruddyTextExtractor works correctly
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestCruddyExtractor extends TestCase {
	// Extractor primed on the test data
	private QuickButCruddyTextExtractor te;
	// All the text to be found in the file
	String[] allTheText = new String[] {
		"This is a test title",
		"This is a test subtitle\nThis is on page 1",
		"Click to edit Master title style",
		"Click to edit Master text styles\nSecond level\nThird level\nFourth level\nFifth level",
		"*",
		"*",
		"*",
		"*",
		"*",
		"Click to edit Master text styles\nSecond level\nThird level\nFourth level\nFifth level",
		"*",
		"*",
		"These are the notes for page 1",
		"This is a test title",
		"This is a test subtitle\nThis is on page 1",
		"This is the title on page 2",
		"This is page two\nIt has several blocks of text\nNone of them have formattingT",
		"These are the notes on page two, again lacking formatting",
		"This is a test title",
		"This is a test subtitle\nThis is on page 1",
		"This is the title on page 2",
		"This is page two\nIt has several blocks of text\nNone of them have formatting",
	};

    public TestCruddyExtractor() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
		te = new QuickButCruddyTextExtractor(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
    }

    public void testReadAsVector() {
		// Extract the text from the file as a vector
		Vector foundTextV = te.getTextAsVector();

		// Ensure they match
		assertEquals(allTheText.length,foundTextV.size());
		for(int i=0; i<allTheText.length; i++) {
			String foundText = (String)foundTextV.get(i);
			assertEquals(allTheText[i],foundText);
		}
	}

	public void testReadAsString() {
		// Extract the text as a String
		String foundText = te.getTextAsString();

		// Turn the string array into a single string
		StringBuffer expectTextSB = new StringBuffer();
		for(int i=0; i<allTheText.length; i++) {
			expectTextSB.append(allTheText[i]);
			expectTextSB.append('\n');
		}
		String expectText = expectTextSB.toString();

		// Ensure they match
		assertEquals(expectText,foundText);
	}
}
