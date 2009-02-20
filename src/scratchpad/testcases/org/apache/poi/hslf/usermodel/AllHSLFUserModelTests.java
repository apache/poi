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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Collects all tests from the package <tt>org.apache.poi.hslf.usermodel</tt>.
 * 
 * @author Josh Micich
 */
public class AllHSLFUserModelTests {
	
	public static Test suite() {
		TestSuite result = new TestSuite(AllHSLFUserModelTests.class.getName());
		result.addTestSuite(TestAddingSlides.class);
		result.addTestSuite(TestBugs.class);
		result.addTestSuite(TestCounts.class);
		result.addTestSuite(TestMostRecentRecords.class);
		result.addTestSuite(TestNotesText.class);
		result.addTestSuite(TestPictures.class);
		result.addTestSuite(TestReOrderingSlides.class);
		result.addTestSuite(TestRecordSetup.class);
		result.addTestSuite(TestRichTextRun.class);
		result.addTestSuite(TestSheetText.class);
		result.addTestSuite(TestSlideOrdering.class);
		result.addTestSuite(TestSoundData.class);
		return result;
	}
}
