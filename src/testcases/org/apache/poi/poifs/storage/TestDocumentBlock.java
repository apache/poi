
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
 * Class to test DocumentBlock functionality
 *
 * @author Marc Johnson
 */

public class TestDocumentBlock
    extends TestCase
{
    static final private byte[] _testdata;

    static
    {
        _testdata = new byte[ 2000 ];
        for (int j = 0; j < _testdata.length; j++)
        {
            _testdata[ j ] = ( byte ) j;
        }
    }
    ;

    /**
     * Constructor TestDocumentBlock
     *
     * @param name
     */

    public TestDocumentBlock(String name)
    {
        super(name);
    }

    /**
     * Test the writing DocumentBlock constructor.
     *
     * @exception IOException
     */

    public void testConstructor()
        throws IOException
    {
        ByteArrayInputStream input = new ByteArrayInputStream(_testdata);
        int                  index = 0;
        int                  size  = 0;

        while (true)
        {
            byte[] data = new byte[ Math.min(_testdata.length - index, 512) ];

            System.arraycopy(_testdata, index, data, 0, data.length);
            DocumentBlock block = new DocumentBlock(input);

            verifyOutput(block, data);
            size += block.size();
            if (block.partiallyRead())
            {
                break;
            }
            index += 512;
        }
        assertEquals(_testdata.length, size);
    }

    /**
     * test static read method
     *
     * @exception IOException
     */

    public void testRead()
        throws IOException
    {
        DocumentBlock[]      blocks = new DocumentBlock[ 4 ];
        ByteArrayInputStream input  = new ByteArrayInputStream(_testdata);

        for (int j = 0; j < 4; j++)
        {
            blocks[ j ] = new DocumentBlock(input);
        }
        for (int j = 1; j <= 2000; j += 17)
        {
            byte[] buffer = new byte[ j ];
            int    offset = 0;

            for (int k = 0; k < (2000 / j); k++)
            {
                DocumentBlock.read(blocks, buffer, offset);
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
     * Test 'reading' constructor
     *
     * @exception IOException
     */

    public void testReadingConstructor()
        throws IOException
    {
        RawDataBlock input =
            new RawDataBlock(new ByteArrayInputStream(_testdata));

        verifyOutput(new DocumentBlock(input), input.getData());
    }

    private void verifyOutput(DocumentBlock block, byte [] input)
        throws IOException
    {
        assertEquals(input.length, block.size());
        if (input.length < 512)
        {
            assertTrue(block.partiallyRead());
        }
        else
        {
            assertTrue(!block.partiallyRead());
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream(512);

        block.writeBlocks(output);
        byte[] copy = output.toByteArray();
        int    j    = 0;

        for (; j < input.length; j++)
        {
            assertEquals(input[ j ], copy[ j ]);
        }
        for (; j < 512; j++)
        {
            assertEquals(( byte ) 0xFF, copy[ j ]);
        }
    }

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out
            .println("Testing org.apache.poi.poifs.storage.DocumentBlock");
        junit.textui.TestRunner.run(TestDocumentBlock.class);
    }
}
