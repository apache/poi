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

package org.apache.poi.hssf.model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Collects all tests for <tt>org.apache.poi.hssf.model</tt>.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestDrawingManager2.class,
    TestFormulaParser.class,
    TestFormulaParserEval.class,
    TestFormulaParserIf.class,
    TestLinkTable.class,
    TestOperandClassTransformer.class,
    TestRowBlocksReader.class,
    TestRVA.class,
    TestSheet.class,
    TestSheetAdditional.class,
    TestWorkbook.class
})
public final class AllModelTests {
}
