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

package org.apache.poi.ss.formula.ptg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.junit.jupiter.api.Test;

/**
 * Make sure the FuncPtg performs as expected
 */
final class TestFuncPtg {

    @Test
    void testRead() {
    	// This function index represents the LEN() function
        byte[] fakeData = { 0x20, 0x00,};

        FuncPtg ptg = FuncPtg.create(TestcaseRecordInputStream.createLittleEndian(fakeData) );
        assertEquals( 0x20, ptg.getFunctionIndex(), "Len formula index is not 32(20H)" );
        assertEquals( 1, ptg.getNumberOfOperands(), "Number of operands in the len formula" );
        assertEquals( "LEN", ptg.getName(), "Function Name" );
        assertEquals( 3, ptg.getSize(), "Ptg Size" );
    }

    @Test
    void testNumberOfOperands() {
        FuncPtg funcPtg = FuncPtg.create(27); // ROUND() - takes 2 args
        assertEquals(2, funcPtg.getNumberOfOperands());
        assertEquals("ROUND", funcPtg.getName());
    }
}
