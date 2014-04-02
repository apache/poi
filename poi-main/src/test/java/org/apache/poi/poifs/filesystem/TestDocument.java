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

package org.apache.poi.poifs.filesystem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.poifs.property.DocumentProperty;
import org.apache.poi.poifs.storage.RawDataBlock;
import org.apache.poi.poifs.storage.SmallDocumentBlock;

/**
 * Class to test POIFSDocument functionality
 *
 * @author Marc Johnson
 */
public final class TestDocument extends TestCase {

    /**
     * Integration test -- really about all we can do
     */
    public void testPOIFSDocument() throws IOException {

        // verify correct number of blocks get created for document
        // that is exact multituple of block size
        POIFSDocument document;
        byte[]        array = new byte[ 4096 ];

        for (int j = 0; j < array.length; j++)
        {
            array[ j ] = ( byte ) j;
        }
        document = new POIFSDocument("foo", new SlowInputStream(new ByteArrayInputStream(array)));
        checkDocument(document, array);

        // verify correct number of blocks get created for document
        // that is not an exact multiple of block size
        array = new byte[ 4097 ];
        for (int j = 0; j < array.length; j++)
        {
            array[ j ] = ( byte ) j;
        }
        document = new POIFSDocument("bar", new ByteArrayInputStream(array));
        checkDocument(document, array);

        // verify correct number of blocks get created for document
        // that is small
        array = new byte[ 4095 ];
        for (int j = 0; j < array.length; j++)
        {
            array[ j ] = ( byte ) j;
        }
        document = new POIFSDocument("_bar", new ByteArrayInputStream(array));
        checkDocument(document, array);

        // verify correct number of blocks get created for document
        // that is rather small
        array = new byte[ 199 ];
        for (int j = 0; j < array.length; j++)
        {
            array[ j ] = ( byte ) j;
        }
        document = new POIFSDocument("_bar2",
                                     new ByteArrayInputStream(array));
        checkDocument(document, array);

        // verify that output is correct
        array = new byte[ 4097 ];
        for (int j = 0; j < array.length; j++)
        {
            array[ j ] = ( byte ) j;
        }
        document = new POIFSDocument("foobar",
                                     new ByteArrayInputStream(array));
        checkDocument(document, array);
        document.setStartBlock(0x12345678);   // what a big file!!
        DocumentProperty      property = document.getDocumentProperty();
        ByteArrayOutputStream stream   = new ByteArrayOutputStream();

        property.writeData(stream);
        byte[] output = stream.toByteArray();
        byte[] array2 =
        {
            ( byte ) 'f', ( byte ) 0, ( byte ) 'o', ( byte ) 0, ( byte ) 'o',
            ( byte ) 0, ( byte ) 'b', ( byte ) 0, ( byte ) 'a', ( byte ) 0,
            ( byte ) 'r', ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
            ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
            ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
            ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
            ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
            ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
            ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
            ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
            ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
            ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
            ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 14,
            ( byte ) 0, ( byte ) 2, ( byte ) 1, ( byte ) -1, ( byte ) -1,
            ( byte ) -1, ( byte ) -1, ( byte ) -1, ( byte ) -1, ( byte ) -1,
            ( byte ) -1, ( byte ) -1, ( byte ) -1, ( byte ) -1, ( byte ) -1,
            ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
            ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
            ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
            ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
            ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
            ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
            ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
            ( byte ) 0, ( byte ) 0x78, ( byte ) 0x56, ( byte ) 0x34,
            ( byte ) 0x12, ( byte ) 1, ( byte ) 16, ( byte ) 0, ( byte ) 0,
            ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0
        };

        assertEquals(array2.length, output.length);
        for (int j = 0; j < output.length; j++)
        {
            assertEquals("Checking property offset " + j, array2[ j ],
                         output[ j ]);
        }
    }

    private static POIFSDocument makeCopy(POIFSDocument document, byte[] input, byte[] data)
            throws IOException {
        POIFSDocument copy = null;

        if (input.length >= 4096)
        {
            RawDataBlock[]       blocks =
                new RawDataBlock[ (input.length + 511) / 512 ];
            ByteArrayInputStream stream = new ByteArrayInputStream(data);
            int                  index  = 0;

            while (true)
            {
                RawDataBlock block = new RawDataBlock(stream);

                if (block.eof())
                {
                    break;
                }
                blocks[ index++ ] = block;
            }
            copy = new POIFSDocument("test" + input.length, blocks,
                                     input.length);
        }
        else
        {
            copy = new POIFSDocument(
                "test" + input.length,
                ( SmallDocumentBlock [] ) document.getSmallBlocks(),
                input.length);
        }
        return copy;
    }

    private static void checkDocument(final POIFSDocument document, final byte[] input)
            throws IOException {
        int big_blocks   = 0;
        int small_blocks = 0;
        int total_output = 0;

        if (input.length >= 4096)
        {
            big_blocks   = (input.length + 511) / 512;
            total_output = big_blocks * 512;
        }
        else
        {
            small_blocks = (input.length + 63) / 64;
            total_output = 0;
        }
        checkValues(
            big_blocks, small_blocks, total_output,
            makeCopy(
            document, input,
            checkValues(
                big_blocks, small_blocks, total_output, document,
                input)), input);
    }

    private static byte[] checkValues(int big_blocks, int small_blocks, int total_output,
            POIFSDocument document, byte[] input) throws IOException {
        assertEquals(document, document.getDocumentProperty().getDocument());
        int increment = ( int ) Math.sqrt(input.length);

        for (int j = 1; j <= input.length; j += increment)
        {
            byte[] buffer = new byte[ j ];
            int    offset = 0;

            for (int k = 0; k < (input.length / j); k++)
            {
                document.read(buffer, offset);
                for (int n = 0; n < buffer.length; n++)
                {
                    assertEquals("checking byte " + (k * j) + n,
                                 input[ (k * j) + n ], buffer[ n ]);
                }
                offset += j;
            }
        }
        assertEquals(big_blocks, document.countBlocks());
        assertEquals(small_blocks, document.getSmallBlocks().length);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        document.writeBlocks(stream);
        byte[] output = stream.toByteArray();

        assertEquals(total_output, output.length);
        int limit = Math.min(total_output, input.length);

        for (int j = 0; j < limit; j++)
        {
            assertEquals("Checking document offset " + j, input[ j ],
                         output[ j ]);
        }
        for (int j = limit; j < output.length; j++)
        {
            assertEquals("Checking document offset " + j, ( byte ) -1,
                         output[ j ]);
        }
        return output;
    }
}
