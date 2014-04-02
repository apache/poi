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

package org.apache.poi.hslf.record;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Tests TextSpecInfoAtom
 *
 * @author Yegor Kozlov
 */
public final class TestTextSpecInfoAtom extends TestCase {

    //from a real file
    private byte[] data_1 = new byte[] {
        0x00, 0x00, (byte)0xAA, 0x0F, 0x2C, 0x00, 0x00, 0x00,
        0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,
        0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x46, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x09, 0x00, 0x00, 0x00, 0x01,
        0x00, 0x00, 0x00, 0x03, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };


    public void testRead() {
        TextSpecInfoAtom spec = new TextSpecInfoAtom(data_1, 0, data_1.length);
        TextSpecInfoAtom.TextSpecInfoRun[] run = spec.getTextSpecInfoRuns();
        assertEquals(5, run.length);

        assertEquals(10, run[0].length());
        assertEquals(1, run[1].length());
        assertEquals(70, run[2].length());
        assertEquals(9, run[3].length());
        assertEquals(32, run[4].length());

    }

    public void testWrite() throws Exception {
        TextSpecInfoAtom spec = new TextSpecInfoAtom(data_1, 0, data_1.length);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        spec.writeOut(out);

        byte[] result = out.toByteArray();
        assertTrue(Arrays.equals(result, data_1));
	}

    public void testReset() throws Exception {
        TextSpecInfoAtom spec = new TextSpecInfoAtom(data_1, 0, data_1.length);
        spec.reset(32);  //length of the parent text

        TextSpecInfoAtom.TextSpecInfoRun[] run = spec.getTextSpecInfoRuns();
        assertEquals(1, run.length);

        assertEquals(32, run[0].length());

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        spec.writeOut(out);

        byte[] result = out.toByteArray();
        TextSpecInfoAtom spec2 = new TextSpecInfoAtom(result, 0, result.length);
        TextSpecInfoAtom.TextSpecInfoRun[] run2 = spec2.getTextSpecInfoRuns();
        assertEquals(1, run2.length);

        assertEquals(32, run2[0].length());
    }
}
