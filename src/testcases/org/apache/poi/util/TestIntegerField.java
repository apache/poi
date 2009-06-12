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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * Test IntegerField code
 *
 * @author  Marc Johnson (mjohnson at apache dot org)
 */
public final class TestIntegerField extends TestCase {

    private static final int[] _test_array =
    {
        Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE
    };

    public void testConstructors() {
        try
        {
            new IntegerField(-1);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        IntegerField field = new IntegerField(2);

        assertEquals(0, field.get());
        try
        {
            new IntegerField(-1, 1);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new IntegerField(2, 0x12345678);
        assertEquals(0x12345678, field.get());
        byte[] array = new byte[ 6 ];

        try
        {
            new IntegerField(-1, 1, array);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new IntegerField(2, 0x12345678, array);
        assertEquals(0x12345678, field.get());
        assertEquals(( byte ) 0x78, array[ 2 ]);
        assertEquals(( byte ) 0x56, array[ 3 ]);
        assertEquals(( byte ) 0x34, array[ 4 ]);
        assertEquals(( byte ) 0x12, array[ 5 ]);
        array = new byte[ 5 ];
        try
        {
            new IntegerField(2, 5, array);
            fail("should have gotten ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        for (int j = 0; j < _test_array.length; j++)
        {
            array = new byte[ 4 ];
            new IntegerField(0, _test_array[ j ], array);
            assertEquals(_test_array[ j ], new IntegerField(0, array).get());
        }
    }

    public void testSet() {
        IntegerField field = new IntegerField(0);
        byte[]       array = new byte[ 4 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            field.set(_test_array[ j ]);
            assertEquals("testing _1 " + j, _test_array[ j ], field.get());
            field = new IntegerField(0);
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
        }
    }

    public void testReadFromBytes() {
        IntegerField field = new IntegerField(1);
        byte[]       array = new byte[ 4 ];

        try
        {
            field.readFromBytes(array);
            fail("should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new IntegerField(0);
        for (int j = 0; j < _test_array.length; j++)
        {
            array[ 0 ] = ( byte ) (_test_array[ j ] % 256);
            array[ 1 ] = ( byte ) ((_test_array[ j ] >> 8) % 256);
            array[ 2 ] = ( byte ) ((_test_array[ j ] >> 16) % 256);
            array[ 3 ] = ( byte ) ((_test_array[ j ] >> 24) % 256);
            field.readFromBytes(array);
            assertEquals("testing " + j, _test_array[ j ], field.get());
        }
    }

    public void testReadFromStream() throws IOException {
        IntegerField field  = new IntegerField(0);
        byte[]       buffer = new byte[ _test_array.length * 4 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            buffer[ (j * 4) + 0 ] = ( byte ) (_test_array[ j ] % 256);
            buffer[ (j * 4) + 1 ] = ( byte ) ((_test_array[ j ] >> 8) % 256);
            buffer[ (j * 4) + 2 ] = ( byte ) ((_test_array[ j ] >> 16) % 256);
            buffer[ (j * 4) + 3 ] = ( byte ) ((_test_array[ j ] >> 24) % 256);
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);

        for (int j = 0; j < buffer.length / 4; j++)
        {
            field.readFromStream(stream);
            assertEquals("Testing " + j, _test_array[ j ], field.get());
        }
    }

    public void testWriteToBytes() {
        IntegerField field = new IntegerField(0);
        byte[]       array = new byte[ 4 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            field.set(_test_array[ j ]);
            field.writeToBytes(array);
            int val = array[ 3 ] << 24;

            val &= 0xFF000000;
            val += (array[ 2 ] << 16) & 0x00FF0000;
            val += (array[ 1 ] << 8) & 0x0000FF00;
            val += (array[ 0 ] & 0x000000FF);
            assertEquals("testing ", _test_array[ j ], val);
        }
    }
}
