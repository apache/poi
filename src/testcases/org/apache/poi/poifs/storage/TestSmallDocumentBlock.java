
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.poifs.storage;

import java.io.*;

import java.util.*;

import junit.framework.*;

/**
 * Class to test SmallDocumentBlock functionality
 *
 * @author Marc Johnson
 */

public class TestSmallDocumentBlock
    extends TestCase
{
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
    ;

    /**
     * constructor
     *
     * @param name
     */

    public TestSmallDocumentBlock(String name)
    {
        super(name);
    }

    /**
     * Test conversion from DocumentBlocks
     *
     * @exception IOException
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
     *
     * @exception IOException;
     *
     * @exception IOException
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
     * Test read method
     *
     * @exception IOException
     */

    public void testRead()
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
        SmallDocumentBlock[] blocks =
            SmallDocumentBlock
                .convert(( BlockWritable [] ) documents
                    .toArray(new DocumentBlock[ 0 ]), _testdata_size);

        for (int j = 1; j <= _testdata_size; j += 38)
        {
            byte[] buffer = new byte[ j ];
            int    offset = 0;

            for (int k = 0; k < (_testdata_size / j); k++)
            {
                SmallDocumentBlock.read(blocks, buffer, offset);
                for (int n = 0; n < buffer.length; n++)
                {
                    assertEquals("checking byte " + (k * j) + n,
                                 _testdata[ (k * j) + n ], buffer[ n ]);
                }
                offset += j;
            }
        }
    }

    /**
     * test fill
     *
     * @exception IOException
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

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out.println(
            "Testing org.apache.poi.poifs.storage.SmallDocumentBlock");
        junit.textui.TestRunner.run(TestSmallDocumentBlock.class);
    }
}
