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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Test ShortField code
 */
final class TestShortField {

    private static final short[] _test_array = {Short.MIN_VALUE, -1, 0, 1, Short.MAX_VALUE};

    @Test
    void testConstructors() {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new ShortField(-1));
        ShortField field = new ShortField(2);

        assertEquals(0, field.get());
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new ShortField(-1, ( short ) 1));
        field = new ShortField(2, ( short ) 0x1234);
        assertEquals(0x1234, field.get());

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new ShortField(-1, ( short ) 1, new byte[ 4 ]));

        byte[] array = new byte[ 4 ];
        field = new ShortField(2, ( short ) 0x1234, array);
        assertEquals(( short ) 0x1234, field.get());
        assertEquals(( byte ) 0x34, array[ 2 ]);
        assertEquals(( byte ) 0x12, array[ 3 ]);

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new ShortField(2, ( short ) 5, new byte[ 3 ]));

        for (short element : _test_array) {
            array = new byte[ 2 ];
            new ShortField(0, element, array);
            assertEquals(element, new ShortField(0, array).get());
        }
    }

    @Test
    void testSet() {
        ShortField field = new ShortField(0);
        byte[]     array = new byte[ 2 ];

        for (int j = 0; j < _test_array.length; j++) {
            field.set(_test_array[ j ]);
            assertEquals(_test_array[ j ], field.get(), "testing _1 " + j);
            field = new ShortField(0);
            field.set(_test_array[ j ], array);
            assertEquals(_test_array[ j ], field.get(), "testing _2 ");
            assertEquals(( byte ) (_test_array[ j ] % 256), array[ 0 ], "testing _3.0 " + _test_array[ j ]);
            assertEquals(( byte ) ((_test_array[ j ] >> 8) % 256), array[ 1 ], "testing _3.1 " + _test_array[ j ]);
        }
    }

    @Test
    void testReadFromBytes() {
        ShortField field1 = new ShortField(1);
        byte[]     array = new byte[ 2 ];

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> field1.readFromBytes(array));

        ShortField field2 = new ShortField(0);
        for (int j = 0; j < _test_array.length; j++)
        {
            array[ 0 ] = ( byte ) (_test_array[ j ] % 256);
            array[ 1 ] = ( byte ) ((_test_array[ j ] >> 8) % 256);
            field2.readFromBytes(array);
            assertEquals(_test_array[ j ], field2.get(), "testing " + j);
        }
    }

    @Test
    void testReadFromStream() throws IOException {
        ShortField field  = new ShortField(0);
        byte[]     buffer = new byte[ _test_array.length * 2 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            buffer[ (j * 2)     ] = ( byte ) (_test_array[ j ]        % 256);
            buffer[ (j * 2) + 1 ] = ( byte ) ((_test_array[ j ] >> 8) % 256);
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);

        for (int j = 0; j < buffer.length / 2; j++)
        {
            field.readFromStream(stream);
            assertEquals(_test_array[ j ], field.get(), "Testing " + j);
        }
    }

    @Test
    void testWriteToBytes() {
        ShortField field = new ShortField(0);
        byte[]     array = new byte[ 2 ];

        for (short element : _test_array) {
            field.set(element);
            field.writeToBytes(array);
            short val = ( short ) (array[ 1 ] << 8);

            val &= ( short ) 0xFF00;
            val += ( short ) (array[ 0 ] & 0x00FF);
            assertEquals(element, val, "testing ");
        }
    }
}
