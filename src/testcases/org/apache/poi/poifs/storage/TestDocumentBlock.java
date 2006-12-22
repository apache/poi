
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
