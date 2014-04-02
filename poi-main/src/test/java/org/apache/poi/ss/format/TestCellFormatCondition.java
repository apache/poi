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
package org.apache.poi.ss.format;

import junit.framework.TestCase;
import org.apache.poi.ss.format.CellFormatCondition;

public class TestCellFormatCondition extends TestCase {
    public void testSVConditions() {
        CellFormatCondition lt = CellFormatCondition.getInstance("<", "1.5");
        assertTrue(lt.pass(1.4));
        assertFalse(lt.pass(1.5));
        assertFalse(lt.pass(1.6));

        CellFormatCondition le = CellFormatCondition.getInstance("<=", "1.5");
        assertTrue(le.pass(1.4));
        assertTrue(le.pass(1.5));
        assertFalse(le.pass(1.6));

        CellFormatCondition gt = CellFormatCondition.getInstance(">", "1.5");
        assertFalse(gt.pass(1.4));
        assertFalse(gt.pass(1.5));
        assertTrue(gt.pass(1.6));

        CellFormatCondition ge = CellFormatCondition.getInstance(">=", "1.5");
        assertFalse(ge.pass(1.4));
        assertTrue(ge.pass(1.5));
        assertTrue(ge.pass(1.6));

        CellFormatCondition eqs = CellFormatCondition.getInstance("=", "1.5");
        assertFalse(eqs.pass(1.4));
        assertTrue(eqs.pass(1.5));
        assertFalse(eqs.pass(1.6));

        CellFormatCondition eql = CellFormatCondition.getInstance("==", "1.5");
        assertFalse(eql.pass(1.4));
        assertTrue(eql.pass(1.5));
        assertFalse(eql.pass(1.6));

        CellFormatCondition neo = CellFormatCondition.getInstance("<>", "1.5");
        assertTrue(neo.pass(1.4));
        assertFalse(neo.pass(1.5));
        assertTrue(neo.pass(1.6));

        CellFormatCondition nen = CellFormatCondition.getInstance("!=", "1.5");
        assertTrue(nen.pass(1.4));
        assertFalse(nen.pass(1.5));
        assertTrue(nen.pass(1.6));
    }
}