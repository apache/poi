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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestHexDump {

    private static PrintStream SYSTEM_OUT;

    @BeforeAll
    public static void setUp() throws UnsupportedEncodingException {
        SYSTEM_OUT = System.out;
        System.setOut(new NullPrintStream());
    }

    @AfterAll
    public static void tearDown() {
        System.setOut(SYSTEM_OUT);
    }

    @Test
    void testDump() throws IOException {
        byte[] testArray = testArray();
        ByteArrayOutputStream streamAct = new ByteArrayOutputStream();
        HexDump.dump(testArray, 0, streamAct, 0);
        byte[] bytesAct = streamAct.toByteArray();
        byte[] bytesExp = toHexDump(0, 0);

        assertEquals(bytesExp.length, bytesAct.length, "array size mismatch");
        assertArrayEquals(bytesExp, bytesAct, "array mismatch");

        // verify proper behavior with non-zero offset
        streamAct.reset();
        HexDump.dump(testArray, 0x10000000L, streamAct, 0);
        bytesAct = streamAct.toByteArray();
        bytesExp = toHexDump(0x10000000L,0);

        assertEquals(bytesExp.length, bytesAct.length, "array size mismatch");
        assertArrayEquals(bytesExp, bytesAct, "array mismatch");

        // verify proper behavior with negative offset
        streamAct.reset();
        HexDump.dump(testArray, 0xFF000000L, streamAct, 0);
        bytesAct = streamAct.toByteArray();
        bytesExp = toHexDump(0xFF000000L,0);

        assertEquals(bytesExp.length, bytesAct.length, "array size mismatch");
        assertArrayEquals(bytesExp, bytesAct, "array mismatch");

        // verify proper behavior with non-zero index
        streamAct.reset();
        HexDump.dump(testArray, 0xFF000000L, streamAct, 0x81);
        bytesAct = streamAct.toByteArray();
        bytesExp = toHexDump(0xFF000000L,0x81);

        assertEquals(bytesExp.length, bytesAct.length, "array size mismatch");
        assertArrayEquals(bytesExp, bytesAct, "array mismatch");


        // verify proper behavior with negative index
        streamAct.reset();
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> HexDump.dump(testArray, 0x10000000L, streamAct, -1));

        // verify proper behavior with index that is too large
        streamAct.reset();
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> HexDump.dump(testArray, 0x10000000L, streamAct, testArray.length));

        // verify proper behavior with null stream
        assertThrows(IllegalArgumentException.class, () -> HexDump.dump(testArray, 0x10000000L, null, 0));

        // verify proper behaviour with empty byte array
        streamAct.reset();
        HexDump.dump( new byte[0], 0, streamAct, 0 );
        assertEquals( "No Data" + System.getProperty( "line.separator"), streamAct.toString(LocaleUtil.CHARSET_1252.name()) );

    }

    private byte[] toHexDump(long offset, int index) {
        StringBuilder strExp = new StringBuilder(), chrs = new StringBuilder();
        Object[] obj = new Object[33];
        StringBuilder format = new StringBuilder();

        for (int j = 0; j < 16 && (index + j*16) < 256; j++) {
            obj[0] = offset+index+j*16L;
            chrs.setLength(0);
            format.setLength(0);
            format.append("%08X ");
            for (int k = 0; k < 16; k++) {
                if (index+j*16+k < 256){
                    obj[k+1] = index+j*16+k;
                    chrs.append(HexDump.toAscii(index+j*16+k));
                    format.append("%02X ");
                } else {
                    format.append("   ");
                }
            }
            obj[17] = chrs.toString();
            format.append("%18$s").append(HexDump.EOL);

            String str = String.format(LocaleUtil.getUserLocale(), format.toString(), obj);
            strExp.append(str);
        }
        return strExp.toString().getBytes(HexDump.UTF8);
    }

    @Test
    void testToHex() {
        assertEquals("000A", HexDump.toHex((short)0xA));

        assertEquals("0A", HexDump.toHex((byte)0xA));
        assertEquals("0000000A", HexDump.toHex(0xA));

        assertEquals("[]", HexDump.toHex(new byte[] { }));
        assertEquals("[0A]", HexDump.toHex(new byte[] { 0xA }));
        assertEquals("[0A, 0B]", HexDump.toHex(new byte[] { 0xA, 0xB }));

        assertEquals("FFFF", HexDump.toHex((short)0xFFFF));

        assertEquals("00000000000004D2", HexDump.toHex(1234L));

        assertEquals("0xFE", HexDump.byteToHex(-2));
        assertEquals("0x25", HexDump.byteToHex(37));
        assertEquals("0xFFFE", HexDump.shortToHex(-2));
        assertEquals("0x0005", HexDump.shortToHex(5));
        assertEquals("0xFFFFFF9C", HexDump.intToHex(-100));
        assertEquals("0x00001001", HexDump.intToHex(4097));
        assertEquals("0xFFFFFFFFFFFF0006", HexDump.longToHex(-65530));
        assertEquals("0x0000000000003FCD", HexDump.longToHex(16333));
    }

	@Test
    void testDumpToString() {
        byte[] testArray = testArray();
        String dump = HexDump.dump(testArray, 0, 0);
        //System.out.println("Hex: \n" + dump);
        assertTrue(dump.contains("0123456789:;<=>?"), "Had: \n" + dump);

        dump = HexDump.dump(testArray, 2, 1);
        //System.out.println("Hex: \n" + dump);
        assertTrue(dump.contains("123456789:;<=>?@"), "Had: \n" + dump);
    }

    @Test
    void testDumpToStringOutOfIndex1() {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> HexDump.dump(new byte[1], 0, -1));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> HexDump.dump(new byte[1], 0, 2));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> HexDump.dump(new byte[1], 0, 1));
    }

    @Test
    void testDumpToStringNoDataEOL1() {
        HexDump.dump(new byte[0], 0, 1);
    }

    @Test
    void testDumpToStringNoDataEOL2() {
        HexDump.dump(new byte[0], 0, 0);
    }

    private static byte[] testArray() {
        byte[] testArray = new byte[ 256 ];

        for (int j = 0; j < 256; j++) {
            testArray[ j ] = ( byte ) j;
        }

        return testArray;
    }
}
