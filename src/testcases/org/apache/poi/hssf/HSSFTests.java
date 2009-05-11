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

package org.apache.poi.hssf;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.poi.hssf.eventmodel.TestEventRecordFactory;
import org.apache.poi.hssf.eventmodel.TestModelFactory;
import org.apache.poi.hssf.eventusermodel.AllEventUserModelTests;
import org.apache.poi.hssf.extractor.TestExcelExtractor;
import org.apache.poi.hssf.model.AllModelTests;
import org.apache.poi.hssf.record.AllRecordTests;
import org.apache.poi.hssf.usermodel.AllUserModelTests;
import org.apache.poi.hssf.util.AllHSSFUtilTests;
import org.apache.poi.ss.formula.AllSSFormulaTests;
import org.apache.poi.ss.util.AllSSUtilTests;

/**
 * Test Suite for all sub-packages of org.apache.poi.hssf<br/>
 * 
 * Mostly this is for my convenience.
 * 
 * @author Andrew C. Oliver acoliver@apache.org
 */
public final class HSSFTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(HSSFTests.class.getName());

        suite.addTest(AllEventUserModelTests.suite());
        suite.addTest(AllModelTests.suite());
        suite.addTest(AllUserModelTests.suite());
        suite.addTest(AllRecordTests.suite());
        suite.addTest(AllHSSFUtilTests.suite());
        suite.addTest(new TestSuite(TestExcelExtractor.class));
        suite.addTest(new TestSuite(TestEventRecordFactory.class));
        suite.addTest(new TestSuite(TestModelFactory.class));
        suite.addTest(AllSSFormulaTests.suite());
        suite.addTest(AllSSUtilTests.suite());
        return suite;
    }
}
