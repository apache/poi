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

package org.apache.poi.ddf;

import junit.framework.Test;
import junit.framework.TestSuite;
/**
 * Tests for org.apache.poi.ddf<br/>
 * 
 * @author Josh Micich
 */
public final class AllPOIDDFTests {
    public static Test suite() {
        TestSuite result = new TestSuite("Tests for org.apache.poi.ddf");
        result.addTestSuite(TestEscherBlipWMFRecord.class);
        result.addTestSuite(TestEscherBoolProperty.class);
        result.addTestSuite(TestEscherBSERecord.class);
        result.addTestSuite(TestEscherChildAnchorRecord.class);
        result.addTestSuite(TestEscherClientAnchorRecord.class);
        result.addTestSuite(TestEscherClientDataRecord.class);
        result.addTestSuite(TestEscherContainerRecord.class);
        result.addTestSuite(TestEscherDggRecord.class);
        result.addTestSuite(TestEscherDgRecord.class);
        result.addTestSuite(TestEscherOptRecord.class);
        result.addTestSuite(TestEscherPropertyFactory.class);
        result.addTestSuite(TestEscherSpgrRecord.class);
        result.addTestSuite(TestEscherSplitMenuColorsRecord.class);
        result.addTestSuite(TestEscherSpRecord.class);
        result.addTestSuite(TestUnknownEscherRecord.class);
        result.addTestSuite(TestEscherBlipRecord.class);
        return result;
    }
}
