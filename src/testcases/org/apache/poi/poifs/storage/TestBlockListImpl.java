
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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * Class to test BlockListImpl functionality
 *
 * @author Marc Johnson
 */

public class TestBlockListImpl
    extends TestCase
{

    /**
     * Constructor TestBlockListImpl
     *
     * @param name
     */

    public TestBlockListImpl(String name)
    {
        super(name);
    }

    /**
     * test zap method
     *
     * @exception IOException
     */

    public void testZap()
        throws IOException
    {
        BlockListImpl list = new BlockListImpl();

        // verify that you can zap anything
        for (int j = -2; j < 10; j++)
        {
            list.zap(j);
        }
        RawDataBlock[] blocks = new RawDataBlock[ 5 ];

        for (int j = 0; j < 5; j++)
        {
            blocks[ j ] =
                new RawDataBlock(new ByteArrayInputStream(new byte[ 512 ]));
        }
        list.setBlocks(blocks);
        for (int j = -2; j < 10; j++)
        {
            list.zap(j);
        }

        // verify that all blocks are gone
        for (int j = 0; j < 5; j++)
        {
            try
            {
                list.remove(j);
                fail("removing item " + j + " should not have succeeded");
            }
            catch (IOException ignored)
            {
            }
        }
    }

    /**
     * test remove method
     *
     * @exception IOException
     */

    public void testRemove()
        throws IOException
    {
        BlockListImpl  list   = new BlockListImpl();
        RawDataBlock[] blocks = new RawDataBlock[ 5 ];
        byte[]         data   = new byte[ 512 * 5 ];

        for (int j = 0; j < 5; j++)
        {
            Arrays.fill(data, j * 512, (j * 512) + 512, ( byte ) j);
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(data);

        for (int j = 0; j < 5; j++)
        {
            blocks[ j ] = new RawDataBlock(stream);
        }
        list.setBlocks(blocks);

        // verify that you can't remove illegal indices
        for (int j = -2; j < 10; j++)
        {
            if ((j < 0) || (j >= 5))
            {
                try
                {
                    list.remove(j);
                    fail("removing item " + j + " should have failed");
                }
                catch (IOException ignored)
                {
                }
            }
        }

        // verify we can safely and correctly remove all blocks
        for (int j = 0; j < 5; j++)
        {
            byte[] output = list.remove(j).getData();

            for (int k = 0; k < 512; k++)
            {
                assertEquals("testing block " + j + ", index " + k,
                             data[ (j * 512) + k ], output[ k ]);
            }
        }

        // verify that all blocks are gone
        for (int j = 0; j < 5; j++)
        {
            try
            {
                list.remove(j);
                fail("removing item " + j + " should not have succeeded");
            }
            catch (IOException ignored)
            {
            }
        }
    }

    /**
     * test setBAT
     *
     * @exception IOException
     */

    public void testSetBAT()
        throws IOException
    {
        BlockListImpl list = new BlockListImpl();

        list.setBAT(null);
        list.setBAT(new BlockAllocationTableReader());
        try
        {
            list.setBAT(new BlockAllocationTableReader());
            fail("second attempt should have failed");
        }
        catch (IOException ignored)
        {
        }
    }

    /**
     * Test fetchBlocks
     *
     * @exception IOException
     */

    public void testFetchBlocks()
        throws IOException
    {

        // strategy:
        // 
        // 1. set up a single BAT block from which to construct a
        // BAT. create nonsense blocks in the raw data block list
        // corresponding to the indices in the BAT block.
        // 2. The indices will include very short documents (0 and 1
        // block in length), longer documents, and some screwed up
        // documents (one with a loop, one that will peek into
        // another document's data, one that includes an unused
        // document, one that includes a reserved (BAT) block, one
        // that includes a reserved (XBAT) block, and one that
        // points off into space somewhere
        BlockListImpl list       = new BlockListImpl();
        List          raw_blocks = new ArrayList();
        byte[]        data       = new byte[ 512 ];
        int           offset     = 0;

        LittleEndian.putInt(data, offset, -3);   // for the BAT block itself
        offset += LittleEndianConsts.INT_SIZE;

        // document 1: is at end of file already; start block = -2
        // document 2: has only one block; start block = 1
        LittleEndian.putInt(data, offset, -2);
        offset += LittleEndianConsts.INT_SIZE;

        // document 3: has a loop in it; start block = 2
        LittleEndian.putInt(data, offset, 2);
        offset += LittleEndianConsts.INT_SIZE;

        // document 4: peeks into document 2's data; start block = 3
        LittleEndian.putInt(data, offset, 4);
        offset += LittleEndianConsts.INT_SIZE;
        LittleEndian.putInt(data, offset, 1);
        offset += LittleEndianConsts.INT_SIZE;

        // document 5: includes an unused block; start block = 5
        LittleEndian.putInt(data, offset, 6);
        offset += LittleEndianConsts.INT_SIZE;
        LittleEndian.putInt(data, offset, -1);
        offset += LittleEndianConsts.INT_SIZE;

        // document 6: includes a BAT block; start block = 7
        LittleEndian.putInt(data, offset, 8);
        offset += LittleEndianConsts.INT_SIZE;
        LittleEndian.putInt(data, offset, 0);
        offset += LittleEndianConsts.INT_SIZE;

        // document 7: includes an XBAT block; start block = 9
        LittleEndian.putInt(data, offset, 10);
        offset += LittleEndianConsts.INT_SIZE;
        LittleEndian.putInt(data, offset, -4);
        offset += LittleEndianConsts.INT_SIZE;

        // document 8: goes off into space; start block = 11;
        LittleEndian.putInt(data, offset, 1000);
        offset += LittleEndianConsts.INT_SIZE;

        // document 9: no screw ups; start block = 12;
        int index = 13;

        for (; offset < 508; offset += LittleEndianConsts.INT_SIZE)
        {
            LittleEndian.putInt(data, offset, index++);
        }
        LittleEndian.putInt(data, offset, -2);
        raw_blocks.add(new RawDataBlock(new ByteArrayInputStream(data)));
        for (int j = raw_blocks.size(); j < 128; j++)
        {
            raw_blocks.add(
                new RawDataBlock(new ByteArrayInputStream(new byte[ 0 ])));
        }
        list.setBlocks(( RawDataBlock [] ) raw_blocks
            .toArray(new RawDataBlock[ 0 ]));
        int[]                      blocks          =
        {
            0
        };
        BlockAllocationTableReader table           =
            new BlockAllocationTableReader(1, blocks, 0, -2, list);
        int[]                      start_blocks    =
        {
            -2, 1, 2, 3, 5, 7, 9, 11, 12
        };
        int[]                      expected_length =
        {
            0, 1, -1, -1, -1, -1, -1, -1, 116
        };

        for (int j = 0; j < start_blocks.length; j++)
        {
            try
            {
                ListManagedBlock[] dataBlocks =
                    list.fetchBlocks(start_blocks[ j ]);

                if (expected_length[ j ] == -1)
                {
                    fail("document " + j + " should have failed");
                }
                else
                {
                    assertEquals(expected_length[ j ], dataBlocks.length);
                }
            }
            catch (IOException e)
            {
                if (expected_length[ j ] == -1)
                {

                    // no problem, we expected a failure here
                }
                else
                {
                    throw e;
                }
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
        System.out
            .println("Testing org.apache.poi.poifs.storage.BlockListImpl");
        junit.textui.TestRunner.run(TestBlockListImpl.class);
    }
}
