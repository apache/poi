
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

package org.apache.poi.poifs.filesystem;

import java.io.*;

import java.util.*;

import junit.framework.*;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.poifs.property.DocumentProperty;
import org.apache.poi.poifs.storage.RawDataBlock;
import org.apache.poi.poifs.storage.SmallDocumentBlock;

/**
 * Class to test POIFSDocument functionality
 *
 * @author Marc Johnson
 */

public class TestDocument
    extends TestCase
{

    /**
     * Constructor TestDocument
     *
     * @param name
     */

    public TestDocument(String name)
    {
        super(name);
    }

    /**
     * Integration test -- really about all we can do
     *
     * @exception IOException
     */

    public void testPOIFSDocument()
        throws IOException
    {

        // verify correct number of blocks get created for document
        // that is exact multituple of block size
        POIFSDocument document;
        byte[]        array = new byte[ 4096 ];

        for (int j = 0; j < array.length; j++)
        {
            array[ j ] = ( byte ) j;
        }
        document = new POIFSDocument("foo", new ByteArrayInputStream(array));
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

    private POIFSDocument makeCopy(POIFSDocument document, byte [] input,
                                   byte [] data)
        throws IOException
    {
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

    private void checkDocument(final POIFSDocument document,
                               final byte [] input)
        throws IOException
    {
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

    private byte [] checkValues(int big_blocks, int small_blocks,
                                int total_output, POIFSDocument document,
                                byte [] input)
        throws IOException
    {
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

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out
            .println("Testing org.apache.poi.poifs.filesystem.POIFSDocument");
        junit.textui.TestRunner.run(TestDocument.class);
    }
}
