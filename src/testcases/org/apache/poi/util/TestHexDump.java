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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestHexDump {

    private static PrintStream SYSTEM_OUT;

    @BeforeClass
    public static void setUp() throws UnsupportedEncodingException {
        SYSTEM_OUT = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        }, false, "UTF-8"));
    }

    @AfterClass
    public static void tearDown() {
        System.setOut(SYSTEM_OUT);
    }

    @Test
    public void testDump() throws IOException {
        byte[] testArray = testArray();
        ByteArrayOutputStream streamAct = new ByteArrayOutputStream();
        HexDump.dump(testArray, 0, streamAct, 0);
        byte bytesAct[] = streamAct.toByteArray();
        byte bytesExp[] = toHexDump(0,0);

        assertEquals("array size mismatch", bytesExp.length, bytesAct.length);
        assertArrayEquals("array mismatch", bytesExp, bytesAct);

        // verify proper behavior with non-zero offset
        streamAct.reset();
        HexDump.dump(testArray, 0x10000000L, streamAct, 0);
        bytesAct = streamAct.toByteArray();
        bytesExp = toHexDump(0x10000000L,0);

        assertEquals("array size mismatch", bytesExp.length, bytesAct.length);
        assertArrayEquals("array mismatch", bytesExp, bytesAct);

        // verify proper behavior with negative offset
        streamAct.reset();
        HexDump.dump(testArray, 0xFF000000L, streamAct, 0);
        bytesAct = streamAct.toByteArray();
        bytesExp = toHexDump(0xFF000000L,0);

        assertEquals("array size mismatch", bytesExp.length, bytesAct.length);
        assertArrayEquals("array mismatch", bytesExp, bytesAct);

        // verify proper behavior with non-zero index
        streamAct.reset();
        HexDump.dump(testArray, 0xFF000000L, streamAct, 0x81);
        bytesAct = streamAct.toByteArray();
        bytesExp = toHexDump(0xFF000000L,0x81);

        assertEquals("array size mismatch", bytesExp.length, bytesAct.length);
        assertArrayEquals("array mismatch", bytesExp, bytesAct);


        // verify proper behavior with negative index
        try {
            streamAct.reset();
            HexDump.dump(testArray, 0x10000000L, streamAct, -1);
            fail("should have caught ArrayIndexOutOfBoundsException on negative index");
        } catch (ArrayIndexOutOfBoundsException ignored_exception) {
            // as expected
        }

        // verify proper behavior with index that is too large
        try {
            streamAct.reset();
            HexDump.dump(testArray, 0x10000000L, streamAct, testArray.length);
            fail("should have caught ArrayIndexOutOfBoundsException on large index");
        } catch (ArrayIndexOutOfBoundsException ignored_exception) {
            // as expected
        }

        // verify proper behavior with null stream
        try {
            HexDump.dump(testArray, 0x10000000L, null, 0);
            fail("should have caught IllegalArgumentException on negative index");
        } catch (IllegalArgumentException ignored_exception) {

            // as expected
        }

        // verify proper behaviour with empty byte array
        streamAct.reset();
        HexDump.dump( new byte[0], 0, streamAct, 0 );
        assertEquals( "No Data" + System.getProperty( "line.separator"), streamAct.toString(LocaleUtil.CHARSET_1252.name()) );

    }

    private byte[] toHexDump(long offset, int index) {
        StringBuilder strExp = new StringBuilder(), chrs = new StringBuilder();
        Object obj[] = new Object[33];
        StringBuilder format = new StringBuilder();

        for (int j = 0; j < 16 && (index + j*16) < 256; j++) {
            obj[0] = offset+index+j*16;
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
    public void testToHex() {
        assertEquals("000A", HexDump.toHex((short)0xA));

        assertEquals("[]", HexDump.toHex(new short[] { }));
        assertEquals("[000A]", HexDump.toHex(new short[] { 0xA }));
        assertEquals("[000A, 000B]", HexDump.toHex(new short[] { 0xA, 0xB }));

        assertEquals("0A", HexDump.toHex((byte)0xA));
        assertEquals("0000000A", HexDump.toHex(0xA));

        assertEquals("[]", HexDump.toHex(new byte[] { }));
        assertEquals("[0A]", HexDump.toHex(new byte[] { 0xA }));
        assertEquals("[0A, 0B]", HexDump.toHex(new byte[] { 0xA, 0xB }));

        assertEquals(": 0", HexDump.toHex(new byte[] { }, 10));
        assertEquals("0: 0A", HexDump.toHex(new byte[] { 0xA }, 10));
        assertEquals("0: 0A, 0B", HexDump.toHex(new byte[] { 0xA, 0xB }, 10));
        assertEquals("0: 0A, 0B\n2: 0C, 0D", HexDump.toHex(new byte[] { 0xA, 0xB, 0xC, 0xD }, 2));
        assertEquals("0: 0A, 0B\n2: 0C, 0D\n4: 0E, 0F", HexDump.toHex(new byte[] { 0xA, 0xB, 0xC, 0xD, 0xE, 0xF }, 2));

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
    public void testDumpToString() throws Exception {
        byte[] testArray = testArray();
        String dump = HexDump.dump(testArray, 0, 0);
        //System.out.println("Hex: \n" + dump);
        assertTrue("Had: \n" + dump,
                dump.contains("0123456789:;<=>?"));

        dump = HexDump.dump(testArray, 2, 1);
        //System.out.println("Hex: \n" + dump);
        assertTrue("Had: \n" + dump,
                dump.contains("123456789:;<=>?@"));
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testDumpToStringOutOfIndex1() throws Exception {
        HexDump.dump(new byte[1], 0, -1);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testDumpToStringOutOfIndex2() throws Exception {
        HexDump.dump(new byte[1], 0, 2);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testDumpToStringOutOfIndex3() throws Exception {
        HexDump.dump(new byte[1], 0, 1);
    }

    @Test
    public void testDumpToStringNoDataEOL1() throws Exception {
        HexDump.dump(new byte[0], 0, 1);
    }

    @Test
    public void testDumpToStringNoDataEOL2() throws Exception {
        HexDump.dump(new byte[0], 0, 0);
    }

    @Test
    public void testDumpToPrintStream() throws IOException {
        byte[] testArray = testArray();
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(byteOut,true,LocaleUtil.CHARSET_1252.name());
        ByteArrayInputStream byteIn = new ByteArrayInputStream(testArray);
        byteIn.mark(256);
        String str;

        byteIn.reset();
        byteOut.reset();
        HexDump.dump(byteIn, out, 0, 256);
        str = new String(byteOut.toByteArray(), LocaleUtil.CHARSET_1252);
        assertTrue("Had: \n" + str, str.contains("0123456789:;<=>?"));

        // test with more than we have
        byteIn.reset();
        byteOut.reset();
        HexDump.dump(byteIn, out, 0, 1000);
        str = new String(byteOut.toByteArray(), LocaleUtil.CHARSET_1252);
        assertTrue("Had: \n" + str, str.contains("0123456789:;<=>?"));

        // test with -1
        byteIn.reset();
        byteOut.reset();
        HexDump.dump(byteIn, out, 0, -1);
        str = new String(byteOut.toByteArray(), LocaleUtil.CHARSET_1252);
        assertTrue("Had: \n" + str, str.contains("0123456789:;<=>?"));

        byteIn.reset();
        byteOut.reset();
        HexDump.dump(byteIn, out, 1, 235);
        str = new String(byteOut.toByteArray(), LocaleUtil.CHARSET_1252);
        assertTrue("Line contents should be moved by one now, but Had: \n" + str,
                    str.contains("123456789:;<=>?@"));

        byteIn.close();
        byteOut.close();
    }

    @Test
    public void testMain() throws Exception {
        File file = TempFile.createTempFile("HexDump", ".dat");
        try {
            try (FileOutputStream out = new FileOutputStream(file)) {
                IOUtils.copy(new ByteArrayInputStream("teststring".getBytes(LocaleUtil.CHARSET_1252)), out);
            }
            assertTrue(file.exists());
            assertTrue(file.length() > 0);

            HexDump.main(new String[] { file.getAbsolutePath() });
        } finally {
            assertTrue(file.exists() && file.delete());
        }
    }

    private static byte[] testArray() {
        byte[] testArray = new byte[ 256 ];

        for (int j = 0; j < 256; j++) {
            testArray[ j ] = ( byte ) j;
        }
        
        return testArray;
    }
}
