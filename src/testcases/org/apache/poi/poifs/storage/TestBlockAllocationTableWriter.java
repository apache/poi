
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

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * Class to test BlockAllocationTableWriter functionality
 *
 * @author Marc Johnson
 */

public class TestBlockAllocationTableWriter
    extends TestCase
{

    /**
     * Constructor TestBlockAllocationTableWriter
     *
     * @param name
     */

    public TestBlockAllocationTableWriter(String name)
    {
        super(name);
    }

    /**
     * Test the allocateSpace method.
     */

    public void testAllocateSpace()
    {
        BlockAllocationTableWriter table         =
            new BlockAllocationTableWriter();
        int[]                      blockSizes    =
        {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9
        };
        int                        expectedIndex = 0;

        for (int j = 0; j < blockSizes.length; j++)
        {
            assertEquals(expectedIndex, table.allocateSpace(blockSizes[ j ]));
            expectedIndex += blockSizes[ j ];
        }
    }

    /**
     * Test the createBlocks method
     *
     * @exception IOException
     */

    public void testCreateBlocks()
        throws IOException
    {
        BlockAllocationTableWriter table = new BlockAllocationTableWriter();

        table.allocateSpace(127);
        table.createBlocks();
        verifyBlocksCreated(table, 1);
        table = new BlockAllocationTableWriter();
        table.allocateSpace(128);
        table.createBlocks();
        verifyBlocksCreated(table, 2);
        table = new BlockAllocationTableWriter();
        table.allocateSpace(254);
        table.createBlocks();
        verifyBlocksCreated(table, 2);
        table = new BlockAllocationTableWriter();
        table.allocateSpace(255);
        table.createBlocks();
        verifyBlocksCreated(table, 3);
        table = new BlockAllocationTableWriter();
        table.allocateSpace(13843);
        table.createBlocks();
        verifyBlocksCreated(table, 109);
        table = new BlockAllocationTableWriter();
        table.allocateSpace(13844);
        table.createBlocks();
        verifyBlocksCreated(table, 110);
        table = new BlockAllocationTableWriter();
        table.allocateSpace(13969);
        table.createBlocks();
        verifyBlocksCreated(table, 110);
        table = new BlockAllocationTableWriter();
        table.allocateSpace(13970);
        table.createBlocks();
        verifyBlocksCreated(table, 111);
    }

    /**
     * Test content produced by BlockAllocationTableWriter
     *
     * @exception IOException
     */

    public void testProduct()
        throws IOException
    {
        BlockAllocationTableWriter table = new BlockAllocationTableWriter();

        for (int k = 1; k <= 22; k++)
        {
            table.allocateSpace(k);
        }
        table.createBlocks();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        table.writeBlocks(stream);
        byte[] output = stream.toByteArray();

        assertEquals(1024, output.length);
        byte[] expected = new byte[ 1024 ];

        Arrays.fill(expected, ( byte ) 0xFF);
        int offset      = 0;
        int block_index = 1;

        for (int k = 1; k <= 22; k++)
        {
            int limit = k - 1;

            for (int j = 0; j < limit; j++)
            {
                LittleEndian.putInt(expected, offset, block_index++);
                offset += LittleEndianConsts.INT_SIZE;
            }
            LittleEndian.putInt(expected, offset,
                                POIFSConstants.END_OF_CHAIN);
            offset += 4;
            block_index++;
        }

        // add BAT block indices
        LittleEndian.putInt(expected, offset, block_index++);
        offset += LittleEndianConsts.INT_SIZE;
        LittleEndian.putInt(expected, offset, POIFSConstants.END_OF_CHAIN);
        for (int k = 0; k < expected.length; k++)
        {
            assertEquals("At offset " + k, expected[ k ], output[ k ]);
        }
    }

    private void verifyBlocksCreated(BlockAllocationTableWriter table,
                                     int count)
        throws IOException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        table.writeBlocks(stream);
        byte[] output = stream.toByteArray();

        assertEquals(count * 512, output.length);
    }

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out.println(
            "Testing org.apache.poi.poifs.storage.BlockAllocationTableWriter");
        junit.textui.TestRunner.run(TestBlockAllocationTableWriter.class);
    }
}
