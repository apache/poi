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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Test LongField code
 */
final class TestLongField {

    static private final long[] _test_array =
    {
        Long.MIN_VALUE, -1L, 0L, 1L, Long.MAX_VALUE
    };

    @Test
    void testConstructors() {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new LongField(-1));

        LongField field1 = new LongField(2);
        assertEquals(0L, field1.get());

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new LongField(-1, 1L));

        LongField  field2 = new LongField(2, 0x123456789ABCDEF0L);
        assertEquals(0x123456789ABCDEF0L, field2.get());

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new LongField(-1, 1L, new byte[ 10 ]));


        byte[] array = new byte[ 10 ];
        LongField field = new LongField(2, 0x123456789ABCDEF0L, array);
        assertEquals(0x123456789ABCDEF0L, field.get());
        assertEquals(( byte ) 0xF0, array[ 2 ]);
        assertEquals(( byte ) 0xDE, array[ 3 ]);
        assertEquals(( byte ) 0xBC, array[ 4 ]);
        assertEquals(( byte ) 0x9A, array[ 5 ]);
        assertEquals(( byte ) 0x78, array[ 6 ]);
        assertEquals(( byte ) 0x56, array[ 7 ]);
        assertEquals(( byte ) 0x34, array[ 8 ]);
        assertEquals(( byte ) 0x12, array[ 9 ]);

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new LongField(2, 5L, new byte[ 9 ]));

        for (long element : _test_array) {
            array = new byte[ 8 ];
            new LongField(0, element, array);
            assertEquals(element, new LongField(0, array).get());
        }
    }

    @Test
    void testSet() {
        LongField field = new LongField(0);
        byte[]    array = new byte[ 8 ];

        for (int j = 0; j < _test_array.length; j++) {
            field.set(_test_array[ j ]);
            assertEquals(_test_array[ j ], field.get(), "testing _1 " + j);
            field = new LongField(0);
            field.set(_test_array[ j ], array);
            assertEquals(_test_array[ j ], field.get(), "testing _2 ");
            assertEquals(( byte ) (_test_array[ j ] % 256), array[ 0 ], "testing _3.0 " + _test_array[ j ]);
            assertEquals(( byte ) ((_test_array[ j ] >> 8) % 256), array[ 1 ], "testing _3.1 " + _test_array[ j ]);
            assertEquals(( byte ) ((_test_array[ j ] >> 16) % 256), array[ 2 ], "testing _3.2 " + _test_array[ j ]);
            assertEquals(( byte ) ((_test_array[ j ] >> 24) % 256), array[ 3 ], "testing _3.3 " + _test_array[ j ]);
            assertEquals(( byte ) ((_test_array[ j ] >> 32) % 256), array[ 4 ], "testing _3.4 " + _test_array[ j ]);
            assertEquals(( byte ) ((_test_array[ j ] >> 40) % 256), array[ 5 ], "testing _3.5 " + _test_array[ j ]);
            assertEquals(( byte ) ((_test_array[ j ] >> 48) % 256), array[ 6 ], "testing _3.6 " + _test_array[ j ]);
            assertEquals(( byte ) ((_test_array[ j ] >> 56) % 256), array[ 7 ], "testing _3.7 " + _test_array[ j ]);
        }
    }

    @Test
    void testReadFromBytes() {
        LongField field = new LongField(1);
        byte[]    array = new byte[ 8 ];

        try {
            field.readFromBytes(array);
            fail("should have caught ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException ignored_e) {
            // as expected
        }
        field = new LongField(0);
        for (int j = 0; j < _test_array.length; j++) {
            array[ 0 ] = ( byte ) (_test_array[ j ] % 256);
            array[ 1 ] = ( byte ) ((_test_array[ j ] >> 8) % 256);
            array[ 2 ] = ( byte ) ((_test_array[ j ] >> 16) % 256);
            array[ 3 ] = ( byte ) ((_test_array[ j ] >> 24) % 256);
            array[ 4 ] = ( byte ) ((_test_array[ j ] >> 32) % 256);
            array[ 5 ] = ( byte ) ((_test_array[ j ] >> 40) % 256);
            array[ 6 ] = ( byte ) ((_test_array[ j ] >> 48) % 256);
            array[ 7 ] = ( byte ) ((_test_array[ j ] >> 56) % 256);
            field.readFromBytes(array);
            assertEquals(_test_array[ j ], field.get(), "testing " + j);
        }
    }

    @Test
    void testReadFromStream() throws IOException {
        LongField field  = new LongField(0);
        byte[]    buffer = new byte[ _test_array.length * 8 ];

        for (int j = 0; j < _test_array.length; j++) {
            buffer[ (j * 8)     ] = ( byte ) ((_test_array[ j ]      ) % 256);
            buffer[ (j * 8) + 1 ] = ( byte ) ((_test_array[ j ] >>  8) % 256);
            buffer[ (j * 8) + 2 ] = ( byte ) ((_test_array[ j ] >> 16) % 256);
            buffer[ (j * 8) + 3 ] = ( byte ) ((_test_array[ j ] >> 24) % 256);
            buffer[ (j * 8) + 4 ] = ( byte ) ((_test_array[ j ] >> 32) % 256);
            buffer[ (j * 8) + 5 ] = ( byte ) ((_test_array[ j ] >> 40) % 256);
            buffer[ (j * 8) + 6 ] = ( byte ) ((_test_array[ j ] >> 48) % 256);
            buffer[ (j * 8) + 7 ] = ( byte ) ((_test_array[ j ] >> 56) % 256);
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);

        for (int j = 0; j < buffer.length / 8; j++) {
            field.readFromStream(stream);
            assertEquals(_test_array[ j ], field.get(), "Testing " + j);
        }
    }

    @Test
    void testWriteToBytes() {
        LongField field = new LongField(0);
        byte[]    array = new byte[ 8 ];

        for (long element : _test_array) {
            field.set(element);
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
            assertEquals(element, val, "testing ");
        }
    }
}
