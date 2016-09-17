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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Collects all tests from the package <tt>org.apache.poi.hslf.record</tt>.
 * 
 * @author Josh Micich
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestAnimationInfoAtom.class,
    TestCString.class,
    TestColorSchemeAtom.class,
    TestComment2000.class,
    TestComment2000Atom.class,
    TestCurrentUserAtom.class,
    TestDocument.class,
    TestDocumentAtom.class,
    TestDocumentEncryptionAtom.class,
    TestExControl.class,
    TestExHyperlink.class,
    TestExHyperlinkAtom.class,
    TestExMediaAtom.class,
    TestExObjList.class,
    TestExObjListAtom.class,
    TestExOleObjAtom.class,
    TestExOleObjStg.class,
    TestExVideoContainer.class,
    TestFontCollection.class,
    TestHeadersFootersAtom.class,
    TestHeadersFootersContainer.class,
    TestInteractiveInfo.class,
    TestInteractiveInfoAtom.class,
    TestNotesAtom.class,
    TestRecordContainer.class,
    TestRecordTypes.class,
    TestSlideAtom.class,
    TestSlidePersistAtom.class,
    TestSound.class,
    TestStyleTextPropAtom.class,
    TestTextBytesAtom.class,
    TestTextCharsAtom.class,
    TestTextHeaderAtom.class,
    TestTextRulerAtom.class,
    TestTextSpecInfoAtom.class,
    TestTxInteractiveInfoAtom.class,
    TestTxMasterStyleAtom.class,
    TestUserEditAtom.class
})
public class AllHSLFRecordTests {
}
