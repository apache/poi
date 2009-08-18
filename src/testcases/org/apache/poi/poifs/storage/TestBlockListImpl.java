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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * Class to test BlockListImpl functionality
 *
 * @author Marc Johnson
 */
public final class TestBlockListImpl extends TestCase {
    private static final class BlockListTestImpl extends BlockListImpl {
        public BlockListTestImpl() {
            // no extra initialisation
        }
    }
    private static BlockListImpl create() {
        return new BlockListTestImpl();
    }

    public void testZap() throws IOException {
        BlockListImpl list = create();

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


    public void testRemove() throws IOException {
        BlockListImpl  list   = create();
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

    public void testSetBAT() throws IOException {
        BlockListImpl list = create();

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

    public void testFetchBlocks() throws IOException {

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
        BlockListImpl list       = create();
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
                    list.fetchBlocks(start_blocks[ j ], -1);

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
}
