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

package org.apache.poi.poifs.property;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * Class to test DocumentProperty functionality
 *
 * @author Marc Johnson
 */
public final class TestDocumentProperty extends TestCase {

    public void testConstructor() throws IOException {
        // test with short name, small file
        verifyProperty("foo", 1234);

        // test with just long enough name, small file
        verifyProperty("A.really.long.long.long.name123", 2345);

        // test with longer name, just small enough file
        verifyProperty("A.really.long.long.long.name1234", 4095);

        // test with just long enough file
        verifyProperty("A.really.long.long.long.name123", 4096);
    }

    public void testReadingConstructor() throws IOException {
        byte[] input =
        {
            ( byte ) 0x52, ( byte ) 0x00, ( byte ) 0x6F, ( byte ) 0x00,
            ( byte ) 0x6F, ( byte ) 0x00, ( byte ) 0x74, ( byte ) 0x00,
            ( byte ) 0x20, ( byte ) 0x00, ( byte ) 0x45, ( byte ) 0x00,
            ( byte ) 0x6E, ( byte ) 0x00, ( byte ) 0x74, ( byte ) 0x00,
            ( byte ) 0x72, ( byte ) 0x00, ( byte ) 0x79, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x16, ( byte ) 0x00, ( byte ) 0x05, ( byte ) 0x01,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x02, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x20, ( byte ) 0x08, ( byte ) 0x02, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xC0, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x46,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xC0, ( byte ) 0x5C, ( byte ) 0xE8, ( byte ) 0x23,
            ( byte ) 0x9E, ( byte ) 0x6B, ( byte ) 0xC1, ( byte ) 0x01,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,

            ( byte ) 0x57, ( byte ) 0x00, ( byte ) 0x6F, ( byte ) 0x00,
            ( byte ) 0x72, ( byte ) 0x00, ( byte ) 0x6B, ( byte ) 0x00,
            ( byte ) 0x62, ( byte ) 0x00, ( byte ) 0x6F, ( byte ) 0x00,
            ( byte ) 0x6F, ( byte ) 0x00, ( byte ) 0x6B, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x12, ( byte ) 0x00, ( byte ) 0x02, ( byte ) 0x01,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x10, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,

            ( byte ) 0x05, ( byte ) 0x00, ( byte ) 0x53, ( byte ) 0x00,
            ( byte ) 0x75, ( byte ) 0x00, ( byte ) 0x6D, ( byte ) 0x00,
            ( byte ) 0x6D, ( byte ) 0x00, ( byte ) 0x61, ( byte ) 0x00,
            ( byte ) 0x72, ( byte ) 0x00, ( byte ) 0x79, ( byte ) 0x00,
            ( byte ) 0x49, ( byte ) 0x00, ( byte ) 0x6E, ( byte ) 0x00,
            ( byte ) 0x66, ( byte ) 0x00, ( byte ) 0x6F, ( byte ) 0x00,
            ( byte ) 0x72, ( byte ) 0x00, ( byte ) 0x6D, ( byte ) 0x00,
            ( byte ) 0x61, ( byte ) 0x00, ( byte ) 0x74, ( byte ) 0x00,
            ( byte ) 0x69, ( byte ) 0x00, ( byte ) 0x6F, ( byte ) 0x00,
            ( byte ) 0x6E, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x28, ( byte ) 0x00, ( byte ) 0x02, ( byte ) 0x01,
            ( byte ) 0x01, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x03, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x08, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x10, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,

            ( byte ) 0x05, ( byte ) 0x00, ( byte ) 0x44, ( byte ) 0x00,
            ( byte ) 0x6F, ( byte ) 0x00, ( byte ) 0x63, ( byte ) 0x00,
            ( byte ) 0x75, ( byte ) 0x00, ( byte ) 0x6D, ( byte ) 0x00,
            ( byte ) 0x65, ( byte ) 0x00, ( byte ) 0x6E, ( byte ) 0x00,
            ( byte ) 0x74, ( byte ) 0x00, ( byte ) 0x53, ( byte ) 0x00,
            ( byte ) 0x75, ( byte ) 0x00, ( byte ) 0x6D, ( byte ) 0x00,
            ( byte ) 0x6D, ( byte ) 0x00, ( byte ) 0x61, ( byte ) 0x00,
            ( byte ) 0x72, ( byte ) 0x00, ( byte ) 0x79, ( byte ) 0x00,
            ( byte ) 0x49, ( byte ) 0x00, ( byte ) 0x6E, ( byte ) 0x00,
            ( byte ) 0x66, ( byte ) 0x00, ( byte ) 0x6F, ( byte ) 0x00,
            ( byte ) 0x72, ( byte ) 0x00, ( byte ) 0x6D, ( byte ) 0x00,
            ( byte ) 0x61, ( byte ) 0x00, ( byte ) 0x74, ( byte ) 0x00,
            ( byte ) 0x69, ( byte ) 0x00, ( byte ) 0x6F, ( byte ) 0x00,
            ( byte ) 0x6E, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x38, ( byte ) 0x00, ( byte ) 0x02, ( byte ) 0x01,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x10, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x10, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00
        };

        verifyReadingProperty(1, input, 128, "Workbook");
        verifyReadingProperty(2, input, 256, "\005SummaryInformation");
        verifyReadingProperty(3, input, 384, "\005DocumentSummaryInformation");
    }

    private void verifyReadingProperty(int index, byte[] input, int offset, String name)
			throws IOException {
        DocumentProperty      property = new DocumentProperty(index, input,
                                             offset);
        ByteArrayOutputStream stream   = new ByteArrayOutputStream(128);
        byte[]                expected = new byte[ 128 ];

        System.arraycopy(input, offset, expected, 0, 128);
        property.writeData(stream);
        byte[] output = stream.toByteArray();

        assertEquals(128, output.length);
        for (int j = 0; j < 128; j++)
        {
            assertEquals("mismatch at offset " + j, expected[ j ],
                         output[ j ]);
        }
        assertEquals(index, property.getIndex());
        assertEquals(name, property.getName());
    }

    private void verifyProperty(String name, int size) throws IOException {
        DocumentProperty property = new DocumentProperty(name, size);

        if (size >= 4096)
        {
            assertTrue(!property.shouldUseSmallBlocks());
        }
        else
        {
            assertTrue(property.shouldUseSmallBlocks());
        }
        byte[] testblock = new byte[ 128 ];
        int    index     = 0;

        for (; index < 0x40; index++)
        {
            testblock[ index ] = ( byte ) 0;
        }
        int limit = Math.min(31, name.length());

        testblock[ index++ ] = ( byte ) (2 * (limit + 1));
        testblock[ index++ ] = ( byte ) 0;
        testblock[ index++ ] = ( byte ) 2;
        testblock[ index++ ] = ( byte ) 1;
        for (; index < 0x50; index++)
        {
            testblock[ index ] = ( byte ) 0xFF;
        }
        for (; index < 0x78; index++)
        {
            testblock[ index ] = ( byte ) 0;
        }
        int sz = size;

        testblock[ index++ ] = ( byte ) sz;
        sz                   /= 256;
        testblock[ index++ ] = ( byte ) sz;
        sz                   /= 256;
        testblock[ index++ ] = ( byte ) sz;
        sz                   /= 256;
        testblock[ index++ ] = ( byte ) sz;
        for (; index < 0x80; index++)
        {
            testblock[ index ] = ( byte ) 0x0;
        }
        byte[] name_bytes = name.getBytes();

        for (index = 0; index < limit; index++)
        {
            testblock[ index * 2 ] = name_bytes[ index ];
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream(512);

        property.writeData(stream);
        byte[] output = stream.toByteArray();

        assertEquals(testblock.length, output.length);
        for (int j = 0; j < testblock.length; j++)
        {
            assertEquals("mismatch at offset " + j, testblock[ j ],
                         output[ j ]);
        }
    }
}
