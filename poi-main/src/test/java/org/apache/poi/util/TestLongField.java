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

import junit.framework.*;

import java.io.*;

/**
 * Test LongField code
 *
 * @author  Marc Johnson (mjohnson at apache dot org)
 */
public final class TestLongField extends TestCase {

    static private final long[] _test_array =
    {
        Long.MIN_VALUE, -1L, 0L, 1L, Long.MAX_VALUE
    };

    public void testConstructors()
    {
        try
        {
            new LongField(-1);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        LongField field = new LongField(2);

        assertEquals(0L, field.get());
        try
        {
            new LongField(-1, 1L);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new LongField(2, 0x123456789ABCDEF0L);
        assertEquals(0x123456789ABCDEF0L, field.get());
        byte[] array = new byte[ 10 ];

        try
        {
            new LongField(-1, 1L, array);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new LongField(2, 0x123456789ABCDEF0L, array);
        assertEquals(0x123456789ABCDEF0L, field.get());
        assertEquals(( byte ) 0xF0, array[ 2 ]);
        assertEquals(( byte ) 0xDE, array[ 3 ]);
        assertEquals(( byte ) 0xBC, array[ 4 ]);
        assertEquals(( byte ) 0x9A, array[ 5 ]);
        assertEquals(( byte ) 0x78, array[ 6 ]);
        assertEquals(( byte ) 0x56, array[ 7 ]);
        assertEquals(( byte ) 0x34, array[ 8 ]);
        assertEquals(( byte ) 0x12, array[ 9 ]);
        array = new byte[ 9 ];
        try
        {
            new LongField(2, 5L, array);
            fail("should have gotten ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        for (int j = 0; j < _test_array.length; j++)
        {
            array = new byte[ 8 ];
            new LongField(0, _test_array[ j ], array);
            assertEquals(_test_array[ j ], new LongField(0, array).get());
        }
    }

    public void testSet()
    {
        LongField field = new LongField(0);
        byte[]    array = new byte[ 8 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            field.set(_test_array[ j ]);
            assertEquals("testing _1 " + j, _test_array[ j ], field.get());
            field = new LongField(0);
            field.set(_test_array[ j ], array);
            assertEquals("testing _2 ", _test_array[ j ], field.get());
            assertEquals("testing _3.0 " + _test_array[ j ],
                         ( byte ) (_test_array[ j ] % 256), array[ 0 ]);
            assertEquals("testing _3.1 " + _test_array[ j ],
                         ( byte ) ((_test_array[ j ] >> 8) % 256),
                         array[ 1 ]);
            assertEquals("testing _3.2 " + _test_array[ j ],
                         ( byte ) ((_test_array[ j ] >> 16) % 256),
                         array[ 2 ]);
            assertEquals("testing _3.3 " + _test_array[ j ],
                         ( byte ) ((_test_array[ j ] >> 24) % 256),
                         array[ 3 ]);
            assertEquals("testing _3.4 " + _test_array[ j ],
                         ( byte ) ((_test_array[ j ] >> 32) % 256),
                         array[ 4 ]);
            assertEquals("testing _3.5 " + _test_array[ j ],
                         ( byte ) ((_test_array[ j ] >> 40) % 256),
                         array[ 5 ]);
            assertEquals("testing _3.6 " + _test_array[ j ],
                         ( byte ) ((_test_array[ j ] >> 48) % 256),
                         array[ 6 ]);
            assertEquals("testing _3.7 " + _test_array[ j ],
                         ( byte ) ((_test_array[ j ] >> 56) % 256),
                         array[ 7 ]);
        }
    }

    public void testReadFromBytes()
    {
        LongField field = new LongField(1);
        byte[]    array = new byte[ 8 ];

        try
        {
            field.readFromBytes(array);
            fail("should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new LongField(0);
        for (int j = 0; j < _test_array.length; j++)
        {
            array[ 0 ] = ( byte ) (_test_array[ j ] % 256);
            array[ 1 ] = ( byte ) ((_test_array[ j ] >> 8) % 256);
            array[ 2 ] = ( byte ) ((_test_array[ j ] >> 16) % 256);
            array[ 3 ] = ( byte ) ((_test_array[ j ] >> 24) % 256);
            array[ 4 ] = ( byte ) ((_test_array[ j ] >> 32) % 256);
            array[ 5 ] = ( byte ) ((_test_array[ j ] >> 40) % 256);
            array[ 6 ] = ( byte ) ((_test_array[ j ] >> 48) % 256);
            array[ 7 ] = ( byte ) ((_test_array[ j ] >> 56) % 256);
            field.readFromBytes(array);
            assertEquals("testing " + j, _test_array[ j ], field.get());
        }
    }

    public void testReadFromStream()
        throws IOException
    {
        LongField field  = new LongField(0);
        byte[]    buffer = new byte[ _test_array.length * 8 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            buffer[ (j * 8) + 0 ] = ( byte ) ((_test_array[ j ] >>  0) % 256);
            buffer[ (j * 8) + 1 ] = ( byte ) ((_test_array[ j ] >>  8) % 256);
            buffer[ (j * 8) + 2 ] = ( byte ) ((_test_array[ j ] >> 16) % 256);
            buffer[ (j * 8) + 3 ] = ( byte ) ((_test_array[ j ] >> 24) % 256);
            buffer[ (j * 8) + 4 ] = ( byte ) ((_test_array[ j ] >> 32) % 256);
            buffer[ (j * 8) + 5 ] = ( byte ) ((_test_array[ j ] >> 40) % 256);
            buffer[ (j * 8) + 6 ] = ( byte ) ((_test_array[ j ] >> 48) % 256);
            buffer[ (j * 8) + 7 ] = ( byte ) ((_test_array[ j ] >> 56) % 256);
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);

        for (int j = 0; j < buffer.length / 8; j++)
        {
            field.readFromStream(stream);
            assertEquals("Testing " + j, _test_array[ j ], field.get());
        }
    }

    public void testWriteToBytes()
    {
        LongField field = new LongField(0);
        byte[]    array = new byte[ 8 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            field.set(_test_array[ j ]);
            field.writeToBytes(array);
            long val = (( long ) array[ 7 ]) << 56;

            val &= 0xFF00000000000000L;
            val += ((( long ) array[ 6 ]) << 48) & 0x00FF000000000000L;
            val += ((( long ) array[ 5 ]) << 40) & 0x0000FF0000000000L;
            val += ((( long ) array[ 4 ]) << 32) & 0x000000FF00000000L;
            val += ((( long ) array[ 3 ]) << 24) & 0x00000000FF000000L;
            val += ((( long ) array[ 2 ]) << 16) & 0x0000000000FF0000L;
            val += ((( long ) array[ 1 ]) << 8) & 0x000000000000FF00L;
            val += (array[ 0 ] & 0x00000000000000FFL);
            assertEquals("testing ", _test_array[ j ], val);
        }
    }
}
