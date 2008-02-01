        
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

package org.apache.poi.hssf.record.formula;

import junit.framework.TestCase;
import org.apache.poi.hssf.record.TestcaseRecordInputStream;

/**
 * Make sure the FuncPtg performs as expected
 *
 * @author Danny Mui (dmui at apache dot org)
 */

public class TestFuncPtg extends TestCase
{

    public TestFuncPtg( String name )
    {
        super( name );
    }


    public static void main( java.lang.String[] args )
    {
        junit.textui.TestRunner.run( TestFuncPtg.class );
    }

    /**
     * Make sure the left overs are re-serialized on excel file reads to avoid
     * the "Warning: Data may have been lost" prompt in excel.
     * <p/>
     * This ptg represents a LEN function extracted from excel
     */

    public void testLeftOvers()
    {
        byte[] fakeData = new byte[4];

        //fakeData[0] = (byte) 0x41;
        fakeData[0] = (byte) 0x20;  //function index
        fakeData[1] = (byte) 0;
        fakeData[2] = (byte) 8;

        FuncPtg ptg = new FuncPtg( new TestcaseRecordInputStream((short)0, (short)fakeData.length, fakeData) );
        assertEquals( "Len formula index is not 32(20H)", (int) 0x20, ptg.getFunctionIndex() );
        assertEquals( "Number of operands in the len formula", 1, ptg.getNumberOfOperands() );
        assertEquals( "Function Name", "LEN", ptg.getName() );
        assertEquals( "Ptg Size", 3, ptg.getSize() );
        //assertEquals("first leftover byte is not 0", (byte)0, ptg.leftOvers[0]);
        //assertEquals("second leftover byte is not 8", (byte)8, ptg.leftOvers[1]);

    }
}


