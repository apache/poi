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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Collects all tests from the package <tt>org.apache.poi.hslf.record</tt>.
 * 
 * @author Josh Micich
 */
public class AllHSLFRecordTests {
	
	public static Test suite() {
		TestSuite result = new TestSuite(AllHSLFRecordTests.class.getName());
		result.addTestSuite(TestAnimationInfoAtom.class);
		result.addTestSuite(TestCString.class);
		result.addTestSuite(TestColorSchemeAtom.class);
		result.addTestSuite(TestComment2000.class);
		result.addTestSuite(TestComment2000Atom.class);
		result.addTestSuite(TestCurrentUserAtom.class);
		result.addTestSuite(TestDocument.class);
		result.addTestSuite(TestDocumentAtom.class);
		result.addTestSuite(TestDocumentEncryptionAtom.class);
		result.addTestSuite(TestExControl.class);
		result.addTestSuite(TestExHyperlink.class);
		result.addTestSuite(TestExHyperlinkAtom.class);
		result.addTestSuite(TestExMediaAtom.class);
		result.addTestSuite(TestExObjList.class);
		result.addTestSuite(TestExObjListAtom.class);
		result.addTestSuite(TestExOleObjAtom.class);
		result.addTestSuite(TestExOleObjStg.class);
		result.addTestSuite(TestExVideoContainer.class);
		result.addTestSuite(TestFontCollection.class);
		result.addTestSuite(TestHeadersFootersAtom.class);
		result.addTestSuite(TestHeadersFootersContainer.class);
		result.addTestSuite(TestInteractiveInfo.class);
		result.addTestSuite(TestInteractiveInfoAtom.class);
		result.addTestSuite(TestNotesAtom.class);
		result.addTestSuite(TestRecordContainer.class);
		result.addTestSuite(TestRecordTypes.class);
		result.addTestSuite(TestSlideAtom.class);
		result.addTestSuite(TestSlidePersistAtom.class);
		result.addTestSuite(TestSound.class);
		result.addTestSuite(TestStyleTextPropAtom.class);
		result.addTestSuite(TestTextBytesAtom.class);
		result.addTestSuite(TestTextCharsAtom.class);
		result.addTestSuite(TestTextHeaderAtom.class);
		result.addTestSuite(TestTextRulerAtom.class);
		result.addTestSuite(TestTextSpecInfoAtom.class);
		result.addTestSuite(TestTxInteractiveInfoAtom.class);
		result.addTestSuite(TestTxMasterStyleAtom.class);
		result.addTestSuite(TestUserEditAtom.class);
		return result;
	}
}
