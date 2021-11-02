
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
 * Unit test for ByteField class
 */
final class TestByteField {

    private static final byte[] _test_array = {
        Byte.MIN_VALUE, ( byte ) -1, ( byte ) 0, ( byte ) 1, Byte.MAX_VALUE
    };

    @Test
    void testConstructors() {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new ByteField(-1));
        ByteField field = new ByteField(2);

        assertEquals(( byte ) 0, field.get());
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new ByteField(-1, (byte)1));

        field = new ByteField(2, ( byte ) 3);
        assertEquals(( byte ) 3, field.get());

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new ByteField(-1, (byte)1, new byte[3]));

        byte[] array = new byte[ 3 ];
        field = new ByteField(2, ( byte ) 4, array);
        assertEquals(( byte ) 4, field.get());
        assertEquals(( byte ) 4, array[ 2 ]);

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new ByteField(2, (byte)5, new byte[2]));

        for (byte b : _test_array) {
            array = new byte[ 1 ];
            new ByteField(0, b, array);
            assertEquals(b, new ByteField(0, array).get());
        }
    }

    @Test
    void testSet() {
        ByteField field = new ByteField(0);
        byte[]    array = new byte[ 1 ];

        for (int j = 0; j < _test_array.length; j++) {
            field.set(_test_array[ j ]);
            assertEquals(_test_array[ j ], field.get(), "testing _1 " + j);
            field = new ByteField(0);
            field.set(_test_array[ j ], array);
            assertEquals(_test_array[ j ], field.get(), "testing _2 ");
            assertEquals(_test_array[ j ], array[ 0 ], "testing _3 ");
        }
    }

    @Test
    void testReadFromBytes() {
        ByteField field1 = new ByteField(1);
        byte[]    array = new byte[ 1 ];

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> field1.readFromBytes(array));

        ByteField field2 = new ByteField(0);
        for (int j = 0; j < _test_array.length; j++) {
            array[ 0 ] = _test_array[ j ];
            field2.readFromBytes(array);
            assertEquals(_test_array[ j ], field2.get(), "testing " + j);
        }
    }

    @Test
    void testReadFromStream() throws IOException {
        ByteField field  = new ByteField(0);
        ByteArrayInputStream stream = new ByteArrayInputStream(_test_array.clone());

        for (int j = 0; j < _test_array.length; j++) {
            field.readFromStream(stream);
            assertEquals(_test_array[ j ], field.get(), "Testing " + j);
        }
    }

    @Test
    void testWriteToBytes() {
        ByteField field = new ByteField(0);
        byte[]    array = new byte[ 1 ];

        for (byte b : _test_array) {
            field.set(b);
            field.writeToBytes(array);
            assertEquals(b, array[ 0 ], "testing ");
        }
    }
}
