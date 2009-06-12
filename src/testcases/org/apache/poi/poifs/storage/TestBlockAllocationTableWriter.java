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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * Class to test BlockAllocationTableWriter functionality
 *
 * @author Marc Johnson
 */
public final class TestBlockAllocationTableWriter extends TestCase {

    public void testAllocateSpace() {
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

    public void testCreateBlocks() {
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
     */
    public void testProduct() throws IOException {
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

    private static void verifyBlocksCreated(BlockAllocationTableWriter table, int count){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
			table.writeBlocks(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        byte[] output = stream.toByteArray();

        assertEquals(count * 512, output.length);
    }
}
