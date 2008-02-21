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

package org.apache.poi;

import org.apache.poi.ddf.AllPOIDDFTests;
import org.apache.poi.hpsf.basic.AllPOIHPSFBasicTests;
import org.apache.poi.hssf.HSSFTests;
import org.apache.poi.poifs.AllPOIFSTests;
import org.apache.poi.util.AllPOIUtilTests;

import junit.framework.Test;
import junit.framework.TestSuite;
/**
 * Root Test Suite for entire POI project.  (Includes all sub-packages of org.apache.poi)<br/>
 * 
 * @author Josh Micich
 */
public final class AllPOITests {
    public static Test suite() {
        TestSuite result = new TestSuite("Tests for org.apache.poi");
        result.addTestSuite(TestPOIDocumentMain.class);
        result.addTest(AllPOIDDFTests.suite());
        result.addTest(AllPOIHPSFBasicTests.suite());
        result.addTest(HSSFTests.suite());
        result.addTest(AllPOIFSTests.suite());
        result.addTest(AllPOIUtilTests.suite());
        return result;
    }
}
