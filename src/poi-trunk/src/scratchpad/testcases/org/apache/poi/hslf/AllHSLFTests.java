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

import org.apache.poi.hslf.extractor.TestCruddyExtractor;
import org.apache.poi.hslf.extractor.TestExtractor;
import org.apache.poi.hslf.model.AllHSLFModelTests;
import org.apache.poi.hslf.record.AllHSLFRecordTests;
import org.apache.poi.hslf.usermodel.AllHSLFUserModelTests;
import org.apache.poi.hslf.util.TestSystemTimeUtils;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Collects all tests from the package <tt>org.apache.poi.hslf</tt> and all sub-packages.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestEncryptedFile.class,
    TestRecordCounts.class,
    TestReWrite.class,
    TestReWriteSanity.class,
    TestCruddyExtractor.class,
    TestExtractor.class,
    AllHSLFModelTests.class,
    AllHSLFRecordTests.class,
    AllHSLFUserModelTests.class,
    TestSystemTimeUtils.class
})
public class AllHSLFTests {
}
