
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
 * Class to test BATBlock functionality
 *
 * @author Marc Johnson
 */

public class TestBATBlock
    extends TestCase
{

    /**
     * Constructor TestBATBlock
     *
     * @param name
     */

    public TestBATBlock(String name)
    {
        super(name);
    }

    /**
     * Test the createBATBlocks method. The test involves setting up
     * various arrays of int's and ensuring that the correct number of
     * BATBlocks is created for each array, and that the data from
     * each array is correctly written to the BATBlocks.
     *
     * @exception IOException
     */

    public void testCreateBATBlocks()
        throws IOException
    {

        // test 0 length array (basic sanity)
        BATBlock[] rvalue = BATBlock.createBATBlocks(createTestArray(0));

        assertEquals(0, rvalue.length);

        // test array of length 1
        rvalue = BATBlock.createBATBlocks(createTestArray(1));
        assertEquals(1, rvalue.length);
        verifyContents(rvalue, 1);

        // test array of length 127
        rvalue = BATBlock.createBATBlocks(createTestArray(127));
        assertEquals(1, rvalue.length);
        verifyContents(rvalue, 127);

        // test array of length 128
        rvalue = BATBlock.createBATBlocks(createTestArray(128));
        assertEquals(1, rvalue.length);
        verifyContents(rvalue, 128);

        // test array of length 129
        rvalue = BATBlock.createBATBlocks(createTestArray(129));
        assertEquals(2, rvalue.length);
        verifyContents(rvalue, 129);
    }

    private int [] createTestArray(int count)
    {
        int[] rvalue = new int[ count ];

        for (int j = 0; j < count; j++)
        {
            rvalue[ j ] = j;
        }
        return rvalue;
    }

    private void verifyContents(BATBlock [] blocks, int entries)
        throws IOException
    {
        byte[] expected = new byte[ 512 * blocks.length ];

        Arrays.fill(expected, ( byte ) 0xFF);
        int offset = 0;

        for (int j = 0; j < entries; j++)
        {
            expected[ offset++ ] = ( byte ) j;
            expected[ offset++ ] = 0;
            expected[ offset++ ] = 0;
            expected[ offset++ ] = 0;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream(512
                                           * blocks.length);

        for (int j = 0; j < blocks.length; j++)
        {
            blocks[ j ].writeBlocks(stream);
        }
        byte[] actual = stream.toByteArray();

        assertEquals(expected.length, actual.length);
        for (int j = 0; j < expected.length; j++)
        {
            assertEquals(expected[ j ], actual[ j ]);
        }
    }

    /**
     * test createXBATBlocks
     *
     * @exception IOException
     */

    public void testCreateXBATBlocks()
        throws IOException
    {

        // test 0 length array (basic sanity)
        BATBlock[] rvalue = BATBlock.createXBATBlocks(createTestArray(0), 1);

        assertEquals(0, rvalue.length);

        // test array of length 1
        rvalue = BATBlock.createXBATBlocks(createTestArray(1), 1);
        assertEquals(1, rvalue.length);
        verifyXBATContents(rvalue, 1, 1);

        // test array of length 127
        rvalue = BATBlock.createXBATBlocks(createTestArray(127), 1);
        assertEquals(1, rvalue.length);
        verifyXBATContents(rvalue, 127, 1);

        // test array of length 128
        rvalue = BATBlock.createXBATBlocks(createTestArray(128), 1);
        assertEquals(2, rvalue.length);
        verifyXBATContents(rvalue, 128, 1);

        // test array of length 254
        rvalue = BATBlock.createXBATBlocks(createTestArray(254), 1);
        assertEquals(2, rvalue.length);
        verifyXBATContents(rvalue, 254, 1);

        // test array of length 255
        rvalue = BATBlock.createXBATBlocks(createTestArray(255), 1);
        assertEquals(3, rvalue.length);
        verifyXBATContents(rvalue, 255, 1);
    }

    private void verifyXBATContents(BATBlock [] blocks, int entries,
                                    int start_block)
        throws IOException
    {
        byte[] expected = new byte[ 512 * blocks.length ];

        Arrays.fill(expected, ( byte ) 0xFF);
        int offset = 0;

        for (int j = 0; j < entries; j++)
        {
            if ((j % 127) == 0)
            {
                if (j != 0)
                {
                    offset += 4;
                }
            }
            expected[ offset++ ] = ( byte ) j;
            expected[ offset++ ] = 0;
            expected[ offset++ ] = 0;
            expected[ offset++ ] = 0;
        }
        for (int j = 0; j < (blocks.length - 1); j++)
        {
            offset               = 508 + (j * 512);
            expected[ offset++ ] = ( byte ) (start_block + j + 1);
            expected[ offset++ ] = 0;
            expected[ offset++ ] = 0;
            expected[ offset++ ] = 0;
        }
        offset               = (blocks.length * 512) - 4;
        expected[ offset++ ] = ( byte ) -2;
        expected[ offset++ ] = ( byte ) -1;
        expected[ offset++ ] = ( byte ) -1;
        expected[ offset++ ] = ( byte ) -1;
        ByteArrayOutputStream stream = new ByteArrayOutputStream(512
                                           * blocks.length);

        for (int j = 0; j < blocks.length; j++)
        {
            blocks[ j ].writeBlocks(stream);
        }
        byte[] actual = stream.toByteArray();

        assertEquals(expected.length, actual.length);
        for (int j = 0; j < expected.length; j++)
        {
            assertEquals("offset " + j, expected[ j ], actual[ j ]);
        }
    }

    /**
     * test calculateXBATStorageRequirements
     */

    public void testCalculateXBATStorageRequirements()
    {
        int[] blockCounts  =
        {
            0, 1, 127, 128
        };
        int[] requirements =
        {
            0, 1, 1, 2
        };

        for (int j = 0; j < blockCounts.length; j++)
        {
            assertEquals(
                "requirement for " + blockCounts[ j ], requirements[ j ],
                BATBlock.calculateXBATStorageRequirements(blockCounts[ j ]));
        }
    }

    /**
     * test entriesPerBlock
     */

    public void testEntriesPerBlock()
    {
        assertEquals(128, BATBlock.entriesPerBlock());
    }

    /**
     * test entriesPerXBATBlock
     */

    public void testEntriesPerXBATBlock()
    {
        assertEquals(127, BATBlock.entriesPerXBATBlock());
    }

    /**
     * test getXBATChainOffset
     */

    public void testGetXBATChainOffset()
    {
        assertEquals(508, BATBlock.getXBATChainOffset());
    }

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out.println("Testing org.apache.poi.poifs.storage.BATBlock");
        junit.textui.TestRunner.run(TestBATBlock.class);
    }
}
