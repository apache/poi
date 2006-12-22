
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

import junit.framework.TestCase;
import org.apache.poi.util.LittleEndian.BufferUnderrunException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

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

    public void testGetUShort()
    {
        byte[] testdata = new byte[ LittleEndian.SHORT_SIZE + 1 ];

        testdata[ 0 ] = 0x01;
        testdata[ 1 ] = ( byte ) 0xFF;
        testdata[ 2 ] = 0x02;

        byte[] testdata2 = new byte[ LittleEndian.SHORT_SIZE + 1 ];
        
        testdata2[ 0 ] = 0x0D;
        testdata2[ 1 ] = ( byte )0x93;
        testdata2[ 2 ] = ( byte )0xFF;

        int expected[] = new int[ 4 ];

        expected[ 0 ] = 0xFF01;
        expected[ 1 ] = 0x02FF;
        expected[ 2 ] = 0x930D;
        expected[ 3 ] = 0xFF93;
        assertEquals(expected[ 0 ], LittleEndian.getUShort(testdata));
        assertEquals(expected[ 1 ], LittleEndian.getUShort(testdata, 1));
        assertEquals(expected[ 2 ], LittleEndian.getUShort(testdata2));
        assertEquals(expected[ 3 ], LittleEndian.getUShort(testdata2, 1));

        byte[] testdata3 = new byte[ LittleEndian.SHORT_SIZE + 1 ];
        LittleEndian.putShort(testdata3, 0, ( short ) expected[2] );
        LittleEndian.putShort(testdata3, 1, ( short ) expected[3] );
        assertEquals(testdata3[ 0 ], 0x0D);
        assertEquals(testdata3[ 1 ], (byte)0x93);
        assertEquals(testdata3[ 2 ], (byte)0xFF);
        assertEquals(expected[ 2 ], LittleEndian.getUShort(testdata3));
        assertEquals(expected[ 3 ], LittleEndian.getUShort(testdata3, 1));
        //System.out.println("TD[1][0]: "+LittleEndian.getUShort(testdata)+" expecting 65281");
        //System.out.println("TD[1][1]: "+LittleEndian.getUShort(testdata, 1)+" expecting 767");
        //System.out.println("TD[2][0]: "+LittleEndian.getUShort(testdata2)+" expecting 37645");
        //System.out.println("TD[2][1]: "+LittleEndian.getUShort(testdata2, 1)+" expecting 65427");
        //System.out.println("TD[3][0]: "+LittleEndian.getUShort(testdata3)+" expecting 37645");
        //System.out.println("TD[3][1]: "+LittleEndian.getUShort(testdata3, 1)+" expecting 65427");
        
    }

    private static final byte[]   _double_array =
    {
        56, 50, -113, -4, -63, -64, -13, 63, 76, -32, -42, -35, 60, -43, 3, 64
    };
    private static final byte[]   _nan_double_array =
    {
        (byte)0x00, (byte)0x00, (byte)0x3C, (byte)0x00, (byte)0x20, (byte)0x04, (byte)0xFF, (byte)0xFF
    };
    private static final double[] _doubles      =
    {
        1.23456, 2.47912, Double.NaN
    };

    /**
     * test the getDouble() method
     */

    public void testGetDouble()
    {
        assertEquals(_doubles[ 0 ], LittleEndian.getDouble(_double_array), 0.000001 );
        assertEquals(_doubles[ 1 ], LittleEndian.getDouble( _double_array, LittleEndian.DOUBLE_SIZE), 0.000001);
        assertTrue(Double.isNaN(LittleEndian.getDouble(_nan_double_array)));

        double nan = LittleEndian.getDouble(_nan_double_array);
        byte[] data = new byte[8];
        LittleEndian.putDouble(data, nan);
        for ( int i = 0; i < data.length; i++ )
        {
            byte b = data[i];
            assertEquals(data[i], _nan_double_array[i]);
        }
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

    public void testUnsignedShort()
            throws Exception
    {
        assertEquals(0xffff, LittleEndian.getUShort(new byte[] { (byte)0xff, (byte)0xff }, 0));
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
