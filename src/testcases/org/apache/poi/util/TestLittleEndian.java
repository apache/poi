
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.util;

import org.apache.poi.util.LittleEndian.BufferUnderrunException;

import java.io.*;

import junit.framework.*;

/**
 * Class to test LittleEndian functionality
 *
 * @author Marc Johnson
 */

public class TestLittleEndian
    extends TestCase
{

    /**
     * Constructor TestLittleEndian
     *
     * @param name
     */

    public TestLittleEndian(String name)
    {
        super(name);
    }

    /**
     * test the getShort() method
     */

    public void testGetShort()
    {
        byte[] testdata = new byte[ LittleEndian.SHORT_SIZE + 1 ];

        testdata[ 0 ] = 0x01;
        testdata[ 1 ] = ( byte ) 0xFF;
        testdata[ 2 ] = 0x02;
        short expected[] = new short[ 2 ];

        expected[ 0 ] = ( short ) 0xFF01;
        expected[ 1 ] = 0x02FF;
        assertEquals(expected[ 0 ], LittleEndian.getShort(testdata));
        assertEquals(expected[ 1 ], LittleEndian.getShort(testdata, 1));
    }

    private static final byte[]   _double_array =
    {
        56, 50, -113, -4, -63, -64, -13, 63, 76, -32, -42, -35, 60, -43, 3, 64
    };
    private static final double[] _doubles      =
    {
        1.23456, 2.47912
    };

    /**
     * test the getDouble() method
     */

    public void testGetDouble()
    {
        assertEquals(_doubles[ 0 ], LittleEndian.getDouble(_double_array),
                     0.000001);
        assertEquals(_doubles[ 1 ], LittleEndian
            .getDouble(_double_array, LittleEndian.DOUBLE_SIZE), 0.000001);
    }

    /**
     * test the getInt() method
     */

    public void testGetInt()
    {
        byte[] testdata = new byte[ LittleEndian.INT_SIZE + 1 ];

        testdata[ 0 ] = 0x01;
        testdata[ 1 ] = ( byte ) 0xFF;
        testdata[ 2 ] = ( byte ) 0xFF;
        testdata[ 3 ] = ( byte ) 0xFF;
        testdata[ 4 ] = 0x02;
        int expected[] = new int[ 2 ];

        expected[ 0 ] = 0xFFFFFF01;
        expected[ 1 ] = 0x02FFFFFF;
        assertEquals(expected[ 0 ], LittleEndian.getInt(testdata));
        assertEquals(expected[ 1 ], LittleEndian.getInt(testdata, 1));
    }

    /**
     * test the getLong method
     */

    public void testGetLong()
    {
        byte[] testdata = new byte[ LittleEndian.LONG_SIZE + 1 ];

        testdata[ 0 ] = 0x01;
        testdata[ 1 ] = ( byte ) 0xFF;
        testdata[ 2 ] = ( byte ) 0xFF;
        testdata[ 3 ] = ( byte ) 0xFF;
        testdata[ 4 ] = ( byte ) 0xFF;
        testdata[ 5 ] = ( byte ) 0xFF;
        testdata[ 6 ] = ( byte ) 0xFF;
        testdata[ 7 ] = ( byte ) 0xFF;
        testdata[ 8 ] = 0x02;
        long expected[] = new long[ 2 ];

        expected[ 0 ] = 0xFFFFFFFFFFFFFF01L;
        expected[ 1 ] = 0x02FFFFFFFFFFFFFFL;
        assertEquals(expected[ 0 ], LittleEndian.getLong(testdata));
        assertEquals(expected[ 1 ], LittleEndian.getLong(testdata, 1));
    }

    /**
     * test the PutShort method
     */

    public void testPutShort()
    {
        byte[] expected = new byte[ LittleEndian.SHORT_SIZE + 1 ];

        expected[ 0 ] = 0x01;
        expected[ 1 ] = ( byte ) 0xFF;
        expected[ 2 ] = 0x02;
        byte[] received   = new byte[ LittleEndian.SHORT_SIZE + 1 ];
        short  testdata[] = new short[ 2 ];

        testdata[ 0 ] = ( short ) 0xFF01;
        testdata[ 1 ] = 0x02FF;
        LittleEndian.putShort(received, testdata[ 0 ]);
        assertTrue(ba_equivalent(received, expected, 0,
                                 LittleEndian.SHORT_SIZE));
        LittleEndian.putShort(received, 1, testdata[ 1 ]);
        assertTrue(ba_equivalent(received, expected, 1,
                                 LittleEndian.SHORT_SIZE));
    }

    /**
     * test the putInt method
     */

    public void testPutInt()
    {
        byte[] expected = new byte[ LittleEndian.INT_SIZE + 1 ];

        expected[ 0 ] = 0x01;
        expected[ 1 ] = ( byte ) 0xFF;
        expected[ 2 ] = ( byte ) 0xFF;
        expected[ 3 ] = ( byte ) 0xFF;
        expected[ 4 ] = 0x02;
        byte[] received   = new byte[ LittleEndian.INT_SIZE + 1 ];
        int    testdata[] = new int[ 2 ];

        testdata[ 0 ] = 0xFFFFFF01;
        testdata[ 1 ] = 0x02FFFFFF;
        LittleEndian.putInt(received, testdata[ 0 ]);
        assertTrue(ba_equivalent(received, expected, 0,
                                 LittleEndian.INT_SIZE));
        LittleEndian.putInt(received, 1, testdata[ 1 ]);
        assertTrue(ba_equivalent(received, expected, 1,
                                 LittleEndian.INT_SIZE));
    }

    /**
     * test the putDouble methods
     */

    public void testPutDouble()
    {
        byte[] received = new byte[ LittleEndian.DOUBLE_SIZE + 1 ];

        LittleEndian.putDouble(received, _doubles[ 0 ]);
        assertTrue(ba_equivalent(received, _double_array, 0,
                                 LittleEndian.DOUBLE_SIZE));
        LittleEndian.putDouble(received, 1, _doubles[ 1 ]);
        byte[] expected = new byte[ LittleEndian.DOUBLE_SIZE + 1 ];

        System.arraycopy(_double_array, LittleEndian.DOUBLE_SIZE, expected,
                         1, LittleEndian.DOUBLE_SIZE);
        assertTrue(ba_equivalent(received, expected, 1,
                                 LittleEndian.DOUBLE_SIZE));
    }

    /**
     * test the putLong method
     */

    public void testPutLong()
    {
        byte[] expected = new byte[ LittleEndian.LONG_SIZE + 1 ];

        expected[ 0 ] = 0x01;
        expected[ 1 ] = ( byte ) 0xFF;
        expected[ 2 ] = ( byte ) 0xFF;
        expected[ 3 ] = ( byte ) 0xFF;
        expected[ 4 ] = ( byte ) 0xFF;
        expected[ 5 ] = ( byte ) 0xFF;
        expected[ 6 ] = ( byte ) 0xFF;
        expected[ 7 ] = ( byte ) 0xFF;
        expected[ 8 ] = 0x02;
        byte[] received   = new byte[ LittleEndian.LONG_SIZE + 1 ];
        long   testdata[] = new long[ 2 ];

        testdata[ 0 ] = 0xFFFFFFFFFFFFFF01L;
        testdata[ 1 ] = 0x02FFFFFFFFFFFFFFL;
        LittleEndian.putLong(received, testdata[ 0 ]);
        assertTrue(ba_equivalent(received, expected, 0,
                                 LittleEndian.LONG_SIZE));
        LittleEndian.putLong(received, 1, testdata[ 1 ]);
        assertTrue(ba_equivalent(received, expected, 1,
                                 LittleEndian.LONG_SIZE));
    }

    private static byte[] _good_array =
    {
        0x01, 0x02, 0x01, 0x02, 0x01, 0x02, 0x01, 0x02, 0x01, 0x02, 0x01,
        0x02, 0x01, 0x02, 0x01, 0x02
    };
    private static byte[] _bad_array  =
    {
        0x01
    };

    /**
     * test the readShort method
     */

    public void testReadShort()
        throws IOException
    {
        short       expected_value = 0x0201;
        InputStream stream         = new ByteArrayInputStream(_good_array);
        int         count          = 0;

        while (true)
        {
            short value = LittleEndian.readShort(stream);

            if (value == 0)
            {
                break;
            }
            assertEquals(value, expected_value);
            count++;
        }
        assertEquals(count,
                     _good_array.length / LittleEndianConsts.SHORT_SIZE);
        stream = new ByteArrayInputStream(_bad_array);
        try
        {
            LittleEndian.readShort(stream);
            fail("Should have caught BufferUnderrunException");
        }
        catch (BufferUnderrunException ignored)
        {

            // as expected
        }
    }

    /**
     * test the readInt method
     */

    public void testReadInt()
        throws IOException
    {
        int         expected_value = 0x02010201;
        InputStream stream         = new ByteArrayInputStream(_good_array);
        int         count          = 0;

        while (true)
        {
            int value = LittleEndian.readInt(stream);

            if (value == 0)
            {
                break;
            }
            assertEquals(value, expected_value);
            count++;
        }
        assertEquals(count, _good_array.length / LittleEndianConsts.INT_SIZE);
        stream = new ByteArrayInputStream(_bad_array);
        try
        {
            LittleEndian.readInt(stream);
            fail("Should have caught BufferUnderrunException");
        }
        catch (BufferUnderrunException ignored)
        {

            // as expected
        }
    }

    /**
     * test the readLong method
     */

    public void testReadLong()
        throws IOException
    {
        long        expected_value = 0x0201020102010201L;
        InputStream stream         = new ByteArrayInputStream(_good_array);
        int         count          = 0;

        while (true)
        {
            long value = LittleEndian.readLong(stream);

            if (value == 0)
            {
                break;
            }
            assertEquals(value, expected_value);
            count++;
        }
        assertEquals(count,
                     _good_array.length / LittleEndianConsts.LONG_SIZE);
        stream = new ByteArrayInputStream(_bad_array);
        try
        {
            LittleEndian.readLong(stream);
            fail("Should have caught BufferUnderrunException");
        }
        catch (BufferUnderrunException ignored)
        {

            // as expected
        }
    }

    /**
     * test the readFromStream method
     */

    public void testReadFromStream()
        throws IOException
    {
        InputStream stream = new ByteArrayInputStream(_good_array);
        byte[]      value  = LittleEndian.readFromStream(stream,
                                 _good_array.length);

        assertTrue(ba_equivalent(value, _good_array, 0, _good_array.length));
        stream = new ByteArrayInputStream(_good_array);
        try
        {
            value = LittleEndian.readFromStream(stream,
                                                _good_array.length + 1);
            fail("Should have caught BufferUnderrunException");
        }
        catch (BufferUnderrunException ignored)
        {

            // as expected
        }
    }

    public void testUnsignedByteToInt()
            throws Exception
    {
        assertEquals(255, LittleEndian.ubyteToInt((byte)255));
    }

    private boolean ba_equivalent(byte [] received, byte [] expected,
                                  int offset, int size)
    {
        boolean result = true;

        for (int j = offset; j < offset + size; j++)
        {
            if (received[ j ] != expected[ j ])
            {
                System.out.println("difference at index " + j);
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out.println("Testing util.LittleEndian functionality");
        junit.textui.TestRunner.run(TestLittleEndian.class);
    }
}
