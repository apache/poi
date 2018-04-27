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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.util.LittleEndian.BufferUnderrunException;
import org.junit.Test;

/**
 * Class to test LittleEndian functionality
 */
public final class TestLittleEndian {

    /**
     * test the getShort() method
     */
    @Test
    public void testGetShort() {
        byte[] testdata = new byte[ LittleEndianConsts.SHORT_SIZE + 1 ];

        testdata[0] = 0x01;
        testdata[1] = (byte) 0xFF;
        testdata[2] = 0x02;
        short expected[] = new short[2];

        expected[0] = ( short ) 0xFF01;
        expected[1] = 0x02FF;
        assertEquals(expected[0], LittleEndian.getShort(testdata));
        assertEquals(expected[1], LittleEndian.getShort(testdata, 1));
    }

    @Test
    public void testGetUShort() {
        byte[] testdata = {
            (byte) 0x01,
            (byte) 0xFF,
            (byte) 0x02,
        };
        byte[] testdata2 = {
            (byte) 0x0D,
            (byte) 0x93,
            (byte) 0xFF,
        };

        int expected0 = 0xFF01;
        int expected1 = 0x02FF;
        int expected2 = 0x930D;
        int expected3 = 0xFF93;
        assertEquals(expected0, LittleEndian.getUShort(testdata));
        assertEquals(expected1, LittleEndian.getUShort(testdata, 1));
        assertEquals(expected2, LittleEndian.getUShort(testdata2));
        assertEquals(expected3, LittleEndian.getUShort(testdata2, 1));

        byte[] testdata3 = new byte[ LittleEndianConsts.SHORT_SIZE + 1 ];
        LittleEndian.putUShort(testdata3, 0, expected2);
        LittleEndian.putUShort(testdata3, 1, expected3);
        assertEquals(testdata3[0], 0x0D);
        assertEquals(testdata3[1], (byte)0x93);
        assertEquals(testdata3[2], (byte)0xFF);
        assertEquals(expected2, LittleEndian.getUShort(testdata3));
        assertEquals(expected3, LittleEndian.getUShort(testdata3, 1));
        
    }

    private static final byte[]   _double_array =
    {
        56, 50, -113, -4, -63, -64, -13, 63, 76, -32, -42, -35, 60, -43, 3, 64
    };
	/** 0x7ff8000000000000 encoded in little endian order */
	private static final byte[] _nan_double_array = HexRead.readFromString("00 00 00 00 00 00 F8 7F");
    private static final double[] _doubles      =
    {
        1.23456, 2.47912, Double.NaN
    };

    /**
     * test the getDouble() method
     */
    @Test
    public void testGetDouble() {
        assertEquals(_doubles[0], LittleEndian.getDouble(_double_array, 0), 0.000001 );
        assertEquals(_doubles[1], LittleEndian.getDouble( _double_array, LittleEndianConsts.DOUBLE_SIZE), 0.000001);
        assertTrue(Double.isNaN(LittleEndian.getDouble(_nan_double_array, 0)));

        double nan = LittleEndian.getDouble(_nan_double_array, 0);
        byte[] data = new byte[8];
        LittleEndian.putDouble(data, 0, nan);
        for ( int i = 0; i < data.length; i++ ) {
            assertEquals(data[i], _nan_double_array[i]);
        }
    }

    /**
     * test the getInt() method
     */
    @Test
    public void testGetInt() {
        // reading 4 byte data from a 5 byte buffer
        byte[] testdata = {
                (byte) 0x01,
                (byte) 0xFF,
                (byte) 0xFF,
                (byte) 0xFF,
                (byte) 0x02,
        };

        assertEquals(0xFFFFFF01, LittleEndian.getInt(testdata));
        assertEquals(0x02FFFFFF, LittleEndian.getInt(testdata, 1));
    }

    /**
     * test the getLong method
     */
    @Test
    public void testGetLong() {

        // reading 8 byte values from a 9 byte buffer
        byte[] testdata = {
            (byte) 0x01,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0x02,
        };

        assertEquals(0xFFFFFFFFFFFFFF01L, LittleEndian.getLong(testdata, 0));
        assertEquals(0x02FFFFFFFFFFFFFFL, LittleEndian.getLong(testdata, 1));
    }

    /**
     * test the PutShort method
     */
    @Test
    public void testPutShort() {
        byte[] expected = new byte[ LittleEndianConsts.SHORT_SIZE + 1 ];

        expected[0] = 0x01;
        expected[1] = (byte) 0xFF;
        expected[2] = 0x02;
        byte[] received   = new byte[ LittleEndianConsts.SHORT_SIZE + 1 ];
        short  testdata[] = new short[2];

        testdata[0] = ( short ) 0xFF01;
        testdata[1] = 0x02FF;
        LittleEndian.putShort(received, 0, testdata[0]);
        assertTrue(compareByteArrays(received, expected, 0, LittleEndianConsts.SHORT_SIZE));
        LittleEndian.putShort(received, 1, testdata[1]);
        assertTrue(compareByteArrays(received, expected, 1, LittleEndianConsts.SHORT_SIZE));
    }

    /**
     * test the putInt method
     */
    @Test
    public void testPutInt() {
        // writing 4 byte data to a 5 byte buffer
        byte[] expected = {
                (byte) 0x01,
                (byte) 0xFF,
                (byte) 0xFF,
                (byte) 0xFF,
                (byte) 0x02,
        };
        byte[] received = new byte[ LittleEndianConsts.INT_SIZE + 1 ];

        LittleEndian.putInt(received, 0, 0xFFFFFF01);
        assertTrue(compareByteArrays(received, expected, 0, LittleEndianConsts.INT_SIZE));
        LittleEndian.putInt(received, 1, 0x02FFFFFF);
        assertTrue(compareByteArrays(received, expected, 1, LittleEndianConsts.INT_SIZE));
    }

    /**
     * test the putDouble methods
     */
    @Test
    public void testPutDouble() {
        byte[] received = new byte[ LittleEndianConsts.DOUBLE_SIZE + 1 ];

        LittleEndian.putDouble(received, 0, _doubles[0]);
        assertTrue(compareByteArrays(received, _double_array, 0, LittleEndianConsts.DOUBLE_SIZE));
        LittleEndian.putDouble(received, 1, _doubles[1]);
        byte[] expected = new byte[ LittleEndianConsts.DOUBLE_SIZE + 1 ];

        System.arraycopy(_double_array, LittleEndianConsts.DOUBLE_SIZE, expected,
                         1, LittleEndianConsts.DOUBLE_SIZE);
        assertTrue(compareByteArrays(received, expected, 1, LittleEndianConsts.DOUBLE_SIZE));
    }

    /**
     * test the putLong method
     */
    @Test
    public void testPutLong() {
        // writing 8 byte values to a 9 byte buffer
        byte[] expected = {
            (byte) 0x01,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0x02,
        };
        byte[] received   = new byte[ LittleEndianConsts.LONG_SIZE + 1 ];

        long testdata0 = 0xFFFFFFFFFFFFFF01L;
        long testdata1 = 0x02FFFFFFFFFFFFFFL;
        LittleEndian.putLong(received, 0, testdata0);
        assertTrue(compareByteArrays(received, expected, 0, LittleEndianConsts.LONG_SIZE));
        LittleEndian.putLong(received, 1, testdata1);
        assertTrue(compareByteArrays(received, expected, 1, LittleEndianConsts.LONG_SIZE));
    }

    private static byte[] _good_array = {
        0x01, 0x02, 0x01, 0x02, 
        0x01, 0x02, 0x01, 0x02, 
        0x01, 0x02, 0x01, 0x02,
        0x01, 0x02, 0x01, 0x02,
    };
    private static byte[] _bad_array  = {
        0x01
    };

    /**
     * test the readShort method
     */
    @Test
    public void testReadShort() throws IOException {
        short       expected_value = 0x0201;
        InputStream stream         = new ByteArrayInputStream(_good_array);
        int         count          = 0;

        while (true) {
            try {
                short value = LittleEndian.readShort(stream);
                assertEquals(value, expected_value);
                count++;
            } catch (BufferUnderrunException e) {
                break;
            }
        }
        assertEquals(count,
                     _good_array.length / LittleEndianConsts.SHORT_SIZE);
        stream = new ByteArrayInputStream(_bad_array);
        try {
            LittleEndian.readShort(stream);
            fail("Should have caught BufferUnderrunException");
        } catch (BufferUnderrunException ignored) {
            // as expected
        }
    }

    /**
     * test the readInt method
     */
    @Test
    public void testReadInt() throws IOException {
        int         expected_value = 0x02010201;
        InputStream stream         = new ByteArrayInputStream(_good_array);
        int         count          = 0;

        while (true) {
            try {
                int value = LittleEndian.readInt(stream);
                assertEquals(value, expected_value);
                count++;
            } catch (BufferUnderrunException e) {
                break;
            }
        }
        assertEquals(count, _good_array.length / LittleEndianConsts.INT_SIZE);
        stream = new ByteArrayInputStream(_bad_array);
        try {
            LittleEndian.readInt(stream);
            fail("Should have caught BufferUnderrunException");
        } catch (BufferUnderrunException ignored) {

            // as expected
        }
    }

    /**
     * test the readLong method
     */
    @Test
    public void testReadLong() throws IOException {
        long        expected_value = 0x0201020102010201L;
        InputStream stream         = new ByteArrayInputStream(_good_array);
        int         count          = 0;

        while (true) {
            try {
                long value = LittleEndian.readLong(stream);
                assertEquals(value, expected_value);
                count++;
            } catch (BufferUnderrunException e) {
                break;
            }
        }
        assertEquals(count,
                     _good_array.length / LittleEndianConsts.LONG_SIZE);
        stream = new ByteArrayInputStream(_bad_array);
        try {
            LittleEndian.readLong(stream);
            fail("Should have caught BufferUnderrunException");
        } catch (BufferUnderrunException ignored) {
            // as expected
        }
    }

    @Test(expected = BufferUnderrunException.class)
    public void testReadFromStream() throws IOException {
        int actual;
        actual = LittleEndian.readUShort(new ByteArrayInputStream(new byte[] { 5, -128, }));
        assertEquals(32773, actual);

        actual = LittleEndian.readUShort(new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, }));
        assertEquals(513, actual);

        LittleEndian.readInt(new ByteArrayInputStream(new byte[] { 1, 2, 3, }));
    }

    @Test
    public void testUnsignedByteToInt() {
        assertEquals(255, LittleEndian.ubyteToInt((byte)255));
    }

    private static boolean compareByteArrays(byte [] received, byte [] expected,
                                  int offset, int size) {

        for (int j = offset; j < offset + size; j++) {
            if (received[j] != expected[j]) {
                System.out.println("difference at index " + j);
                return false;
            }
        }
        return true;
    }

    @Test
    public void testUnsignedShort() {
        assertEquals(0xffff, LittleEndian.getUShort(new byte[] { (byte)0xff, (byte)0xff }, 0));
    }
}
