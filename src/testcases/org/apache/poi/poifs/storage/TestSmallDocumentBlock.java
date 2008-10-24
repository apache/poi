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

package org.apache.poi.poifs.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/**
 * Class to test SmallDocumentBlock functionality
 *
 * @author Marc Johnson
 */
public final class TestSmallDocumentBlock extends TestCase {
    static final private byte[] _testdata;
    static final private int    _testdata_size = 2999;

    static
    {
        _testdata = new byte[ _testdata_size ];
        for (int j = 0; j < _testdata.length; j++)
        {
            _testdata[ j ] = ( byte ) j;
        }
    }

    /**
     * Test conversion from DocumentBlocks
     */
    public void testConvert1()
        throws IOException
    {
        ByteArrayInputStream stream    = new ByteArrayInputStream(_testdata);
        List                 documents = new ArrayList();

        while (true)
        {
            DocumentBlock block = new DocumentBlock(stream);

            documents.add(block);
            if (block.partiallyRead())
            {
                break;
            }
        }
        SmallDocumentBlock[] results =
            SmallDocumentBlock
                .convert(( BlockWritable [] ) documents
                    .toArray(new DocumentBlock[ 0 ]), _testdata_size);

        assertEquals("checking correct result size: ",
                     (_testdata_size + 63) / 64, results.length);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        for (int j = 0; j < results.length; j++)
        {
            results[ j ].writeBlocks(output);
        }
        byte[] output_array = output.toByteArray();

        assertEquals("checking correct output size: ", 64 * results.length,
                     output_array.length);
        int index = 0;

        for (; index < _testdata_size; index++)
        {
            assertEquals("checking output " + index, _testdata[ index ],
                         output_array[ index ]);
        }
        for (; index < output_array.length; index++)
        {
            assertEquals("checking output " + index, ( byte ) 0xff,
                         output_array[ index ]);
        }
    }

    /**
     * Test conversion from byte array
     */
    public void testConvert2()
        throws IOException
    {
        for (int j = 0; j < 320; j++)
        {
            byte[] array = new byte[ j ];

            for (int k = 0; k < j; k++)
            {
                array[ k ] = ( byte ) k;
            }
            SmallDocumentBlock[] blocks = SmallDocumentBlock.convert(array,
                                              319);

            assertEquals(5, blocks.length);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            for (int k = 0; k < blocks.length; k++)
            {
                blocks[ k ].writeBlocks(stream);
            }
            stream.close();
            byte[] output = stream.toByteArray();

            for (int k = 0; k < array.length; k++)
            {
                assertEquals(String.valueOf(k), array[ k ], output[ k ]);
            }
            for (int k = array.length; k < 320; k++)
            {
                assertEquals(String.valueOf(k), ( byte ) 0xFF, output[ k ]);
            }
        }
    }

    /**
     * test fill
     */
    public void testFill()
        throws IOException
    {
        for (int j = 0; j <= 8; j++)
        {
            List foo = new ArrayList();

            for (int k = 0; k < j; k++)
            {
                foo.add(new Object());
            }
            int result = SmallDocumentBlock.fill(foo);

            assertEquals("correct big block count: ", (j + 7) / 8, result);
            assertEquals("correct small block count: ", 8 * result,
                         foo.size());
            for (int m = j; m < foo.size(); m++)
            {
                BlockWritable         block  = ( BlockWritable ) foo.get(m);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                block.writeBlocks(stream);
                byte[] output = stream.toByteArray();

                assertEquals("correct output size (block[ " + m + " ]): ",
                             64, output.length);
                for (int n = 0; n < 64; n++)
                {
                    assertEquals("correct value (block[ " + m + " ][ " + n
                                 + " ]): ", ( byte ) 0xff, output[ n ]);
                }
            }
        }
    }

    /**
     * test calcSize
     */

    public void testCalcSize()
    {
        for (int j = 0; j < 10; j++)
        {
            assertEquals("testing " + j, j * 64,
                         SmallDocumentBlock.calcSize(j));
        }
    }

    /**
     * test extract method
     *
     * @exception IOException
     */

    public void testExtract()
        throws IOException
    {
        byte[] data   = new byte[ 512 ];
        int    offset = 0;

        for (int j = 0; j < 8; j++)
        {
            for (int k = 0; k < 64; k++)
            {
                data[ offset++ ] = ( byte ) (k + j);
            }
        }
        RawDataBlock[] blocks =
        {
            new RawDataBlock(new ByteArrayInputStream(data))
        };
        List           output = SmallDocumentBlock.extract(blocks);
        Iterator       iter   = output.iterator();

        offset = 0;
        while (iter.hasNext())
        {
            byte[] out_data = (( SmallDocumentBlock ) iter.next()).getData();

            assertEquals("testing block at offset " + offset, 64,
                         out_data.length);
            for (int j = 0; j < out_data.length; j++)
            {
                assertEquals("testing byte at offset " + offset,
                             data[ offset ], out_data[ j ]);
                offset++;
            }
        }
    }
}
