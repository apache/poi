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
 * Tests the NameRecord serializes/deserializes correctly
 *
 * @author Danny Mui (dmui at apache dot org)
 */
public final class TestNameRecord extends TestCase {

    /**
     * Makes sure that additional name information is parsed properly such as menu/description
     */
    public void testFillExtras()
    {

        byte[] examples = {
            (byte) 0x88, (byte) 0x03, (byte) 0x67, (byte) 0x06,
            (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x23,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x4D,
            (byte) 0x61, (byte) 0x63, (byte) 0x72, (byte) 0x6F,
            (byte) 0x31, (byte) 0x3A, (byte) 0x01, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x11, (byte) 0x00,
            (byte) 0x00, (byte) 0x4D, (byte) 0x61, (byte) 0x63,
            (byte) 0x72, (byte) 0x6F, (byte) 0x20, (byte) 0x72,
            (byte) 0x65, (byte) 0x63, (byte) 0x6F, (byte) 0x72,
            (byte) 0x64, (byte) 0x65, (byte) 0x64, (byte) 0x20,
            (byte) 0x32, (byte) 0x37, (byte) 0x2D, (byte) 0x53,
            (byte) 0x65, (byte) 0x70, (byte) 0x2D, (byte) 0x39,
            (byte) 0x33, (byte) 0x20, (byte) 0x62, (byte) 0x79,
            (byte) 0x20, (byte) 0x41, (byte) 0x4C, (byte) 0x4C,
            (byte) 0x57, (byte) 0x4F, (byte) 0x52
        };


        NameRecord name = new NameRecord(TestcaseRecordInputStream.create(NameRecord.sid, examples));
        String description = name.getDescriptionText();
        assertNotNull( description );
        assertTrue( "text contains ALLWOR", description.indexOf( "ALLWOR" ) > 0 );
    }
}


