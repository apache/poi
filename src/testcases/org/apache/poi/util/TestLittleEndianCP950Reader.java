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

package org.apache.poi.util;


import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;

import org.junit.Test;

public class TestLittleEndianCP950Reader {

    @Test
    public void testPersonalUseMappings() throws Exception {
        //ftp://ftp.unicode.org/Public/MAPPINGS/VENDORS/MICSFT/WindowsBestFit/bestfit950.txt
        byte[] data = new byte[2];
        data[1] = (byte) 0xfe;
        data[0] = (byte) 0xd3;
        assertCharEquals('\uE2E5', data);

        data[1] = (byte) 0x90;
        data[0] = (byte) 0xb6;
        assertCharEquals('\uE49F', data);

        //actually found in document
        //but this disagrees with file above
        data[1] = (byte) 0x8E;
        data[0] = (byte) 0xA8;
        assertCharEquals('\uE357', data);

        data[1] = (byte) 0x8E;
        data[0] = (byte) 0xE6;
        assertCharEquals('\uE395', data);

    /*
        //TODO: figure out why this isn't working
        data[0] = (byte)0xF9;
        data[1] = (byte)0xD8;
        assertCharEquals('\u88CF', data);
     */

    }


    private void assertCharEquals(char expected, byte[] data) throws IOException {
        Reader reader = new LittleEndianCP950Reader(data);
        int c = reader.read();
        assertEquals((int) expected, c);
        int eof = reader.read();
        assertEquals("should be end of stream", -1, eof);
    }
}
