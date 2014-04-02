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
 * Test ShortField code
 *
 * @author  Marc Johnson (mjohnson at apache dot org)
 */
public final class TestShortField extends TestCase {

    private static final short[] _test_array =
    {
        Short.MIN_VALUE, ( short ) -1, ( short ) 0, ( short ) 1,
        Short.MAX_VALUE
    };

    public void testConstructors() {
        try
        {
            new ShortField(-1);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        ShortField field = new ShortField(2);

        assertEquals(0, field.get());
        try
        {
            new ShortField(-1, ( short ) 1);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new ShortField(2, ( short ) 0x1234);
        assertEquals(0x1234, field.get());
        byte[] array = new byte[ 4 ];

        try
        {
            new ShortField(-1, ( short ) 1, array);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new ShortField(2, ( short ) 0x1234, array);
        assertEquals(( short ) 0x1234, field.get());
        assertEquals(( byte ) 0x34, array[ 2 ]);
        assertEquals(( byte ) 0x12, array[ 3 ]);
        array = new byte[ 3 ];
        try
        {
            new ShortField(2, ( short ) 5, array);
            fail("should have gotten ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        for (int j = 0; j < _test_array.length; j++)
        {
            array = new byte[ 2 ];
            new ShortField(0, _test_array[ j ], array);
            assertEquals(_test_array[ j ], new ShortField(0, array).get());
        }
    }

    public void testSet() {
        ShortField field = new ShortField(0);
        byte[]     array = new byte[ 2 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            field.set(_test_array[ j ]);
            assertEquals("testing _1 " + j, _test_array[ j ], field.get());
            field = new ShortField(0);
            field.set(_test_array[ j ], array);
            assertEquals("testing _2 ", _test_array[ j ], field.get());
            assertEquals("testing _3.0 " + _test_array[ j ],
                         ( byte ) (_test_array[ j ] % 256), array[ 0 ]);
            assertEquals("testing _3.1 " + _test_array[ j ],
                         ( byte ) ((_test_array[ j ] >> 8) % 256),
                         array[ 1 ]);
        }
    }

    public void testReadFromBytes() {
        ShortField field = new ShortField(1);
        byte[]     array = new byte[ 2 ];

        try
        {
            field.readFromBytes(array);
            fail("should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new ShortField(0);
        for (int j = 0; j < _test_array.length; j++)
        {
            array[ 0 ] = ( byte ) (_test_array[ j ] % 256);
            array[ 1 ] = ( byte ) ((_test_array[ j ] >> 8) % 256);
            field.readFromBytes(array);
            assertEquals("testing " + j, _test_array[ j ], field.get());
        }
    }

    public void testReadFromStream() throws IOException {
        ShortField field  = new ShortField(0);
        byte[]     buffer = new byte[ _test_array.length * 2 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            buffer[ (j * 2) + 0 ] = ( byte ) (_test_array[ j ] % 256);
            buffer[ (j * 2) + 1 ] = ( byte ) ((_test_array[ j ] >> 8) % 256);
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);

        for (int j = 0; j < buffer.length / 2; j++)
        {
            field.readFromStream(stream);
            assertEquals("Testing " + j, _test_array[ j ], field.get());
        }
    }

    public void testWriteToBytes() {
        ShortField field = new ShortField(0);
        byte[]     array = new byte[ 2 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            field.set(_test_array[ j ]);
            field.writeToBytes(array);
            short val = ( short ) (array[ 1 ] << 8);

            val &= ( short ) 0xFF00;
            val += ( short ) (array[ 0 ] & 0x00FF);
            assertEquals("testing ", _test_array[ j ], val);
        }
    }
}
