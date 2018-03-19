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

package org.apache.poi.hsmf;

import org.apache.poi.hsmf.datatypes.TestChunkData;
import org.apache.poi.hsmf.datatypes.TestSorters;
import org.apache.poi.hsmf.datatypes.TestTypes;
import org.apache.poi.hsmf.extractor.TestOutlookTextExtractor;
import org.apache.poi.hsmf.parsers.TestPOIFSChunkParser;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestBasics.class,
    TestBlankFileRead.class,
    TestSimpleFileRead.class,
    TestOutlook30FileRead.class,
    TestFileWithAttachmentsRead.class,
    TestChunkData.class,
    TestTypes.class,
    TestSorters.class,
    TestOutlookTextExtractor.class,
    TestPOIFSChunkParser.class,
    TestMessageSubmissionChunkY2KRead.class,
    TestMessageSubmissionChunk.class,
    TestExtractEmbeddedMSG.class
})
public class AllHSMFTests {
}
