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

import static org.junit.Assert.assertEquals;

import org.apache.poi.util.LittleEndianOutput;
import org.junit.Test;

public class TestAbstractFunctionPtg  {

    @Test
    public void testConstructor() {
        FunctionPtg ptg = new FunctionPtg(1, 2, null, 255);
        assertEquals(1, ptg.getFunctionIndex());
        assertEquals(2, ptg.getDefaultOperandClass());
        assertEquals(255, ptg.getNumberOfOperands());
    }

    @Test(expected=RuntimeException.class)
    public void testInvalidFunctionIndex() {
        new FunctionPtg(40000, 2, null, 255);
    }

    @Test(expected=RuntimeException.class)
    public void testInvalidRuntimeClass() {
        new FunctionPtg(1, 300, null, 255);
    }
    
    private static class FunctionPtg extends AbstractFunctionPtg {

        protected FunctionPtg(int functionIndex, int pReturnClass,
                byte[] paramTypes, int nParams) {
            super(functionIndex, pReturnClass, paramTypes, nParams);
        }

        public int getSize() {
            return 0;
        }

        public void write(LittleEndianOutput out) {
            
        }
    }
}
