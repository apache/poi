
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
        

package org.apache.poi.hssf.record;

import junit.framework.TestCase;

/**
 * Tests BoundSheetRecord.
 *
 * @see BoundSheetRecord
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestBoundSheetRecord
        extends TestCase
{
    public TestBoundSheetRecord( String s )
    {
        super( s );
    }

    public void testRecordLength()
            throws Exception
    {
        BoundSheetRecord record = new BoundSheetRecord();
        record.setCompressedUnicodeFlag((byte)0x00);
        record.setSheetname("Sheet1");
        record.setSheetnameLength((byte)6);

        assertEquals(" 2  +  2  +  4  +   2   +    1     +    1    + len(str)", 18, record.getRecordSize());
    }

    public void testWideRecordLength()
            throws Exception
    {
        BoundSheetRecord record = new BoundSheetRecord();        
        record.setSheetname("Sheet\u20ac");
        record.setSheetnameLength((byte)6);

        assertEquals(" 2  +  2  +  4  +   2   +    1     +    1    + len(str) * 2", 24, record.getRecordSize());
    }
    
    public void testName() {
        BoundSheetRecord record = new BoundSheetRecord();
        record.setSheetname("1234567890223456789032345678904");
        assertTrue("Success", true);
        try {
            record.setSheetname("12345678902234567890323456789042");
            assertTrue("Should have thrown IllegalArgumentException, but didnt", false);
        } catch (IllegalArgumentException e) {
            assertTrue("succefully threw exception",true);
        }
        
        try {
            record.setSheetname("s//*s");
            assertTrue("Should have thrown IllegalArgumentException, but didnt", false);
        } catch (IllegalArgumentException e) {
            assertTrue("succefully threw exception",true);
        }
            
    }

}
