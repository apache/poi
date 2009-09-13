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

import java.io.ByteArrayInputStream;

import org.apache.poi.util.LittleEndian;

import junit.framework.TestCase;

/**
 * 
 * @author Josh Micich
 */
public final class TestDVALRecord extends TestCase {
    public void testRead() {
        
        byte[] data = new byte[22];
        LittleEndian.putShort(data, 0, DVALRecord.sid);
        LittleEndian.putShort(data, 2, (short)18);
        LittleEndian.putShort(data, 4, (short)55);
        LittleEndian.putInt(data, 6, 56);
        LittleEndian.putInt(data, 10, 57);
        LittleEndian.putInt(data, 14, 58);
        LittleEndian.putInt(data, 18, 59);
       
        RecordInputStream in = new RecordInputStream(new ByteArrayInputStream(data));
        in.nextRecord();
        DVALRecord dv = new DVALRecord(in);
        
        assertEquals(55, dv.getOptions());
        assertEquals(56, dv.getHorizontalPos());
        assertEquals(57, dv.getVerticalPos());
        assertEquals(58, dv.getObjectID());
        if(dv.getDVRecNo() == 0) {
            fail("Identified bug 44510");
        }
        assertEquals(59, dv.getDVRecNo());
    }
}
