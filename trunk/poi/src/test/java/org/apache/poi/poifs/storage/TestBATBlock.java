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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.apache.poi.poifs.common.POIFSConstants;
import org.junit.jupiter.api.Test;

/**
 * Class to test BATBlock functionality
 */
final class TestBATBlock {


    @Test
    void testEntriesPerBlock() {
        assertEquals(128, POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS.getBATEntriesPerBlock());
    }

    @Test
    void testEntriesPerXBATBlock() {
        assertEquals(127, POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS.getXBATEntriesPerBlock());
    }

    @Test
    void testGetXBATChainOffset() {
        assertEquals(508, POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS.getNextXBATChainOffset());
    }

    @Test
    void testCalculateMaximumSize() {
        // Zero fat blocks isn't technically valid, but it'd be header only
        assertEquals(
                512,
                BATBlock.calculateMaximumSize(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, 0)
        );
        assertEquals(
                4096,
                BATBlock.calculateMaximumSize(POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS, 0)
        );

        // A single FAT block can address 128/1024 blocks
        assertEquals(
                512 + 512 * 128,
                BATBlock.calculateMaximumSize(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, 1)
        );
        assertEquals(
                4096 + 4096 * 1024,
                BATBlock.calculateMaximumSize(POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS, 1)
        );

        assertEquals(
                512 + 4 * 512 * 128,
                BATBlock.calculateMaximumSize(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, 4)
        );
        assertEquals(
                4096 + 4 * 4096 * 1024,
                BATBlock.calculateMaximumSize(POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS, 4)
        );

        // One XBAT block holds 127/1023 individual BAT blocks, so they can address
        //  a fairly hefty amount of space themselves
        // However, the BATs continue as before
        assertEquals(
                512 + 109 * 512 * 128,
                BATBlock.calculateMaximumSize(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, 109)
        );
        assertEquals(
                4096 + 109 * 4096 * 1024,
                BATBlock.calculateMaximumSize(POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS, 109)
        );

        assertEquals(
                512 + 110 * 512 * 128,
                BATBlock.calculateMaximumSize(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, 110)
        );
        assertEquals(
                4096 + 110 * 4096 * 1024,
                BATBlock.calculateMaximumSize(POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS, 110)
        );

        assertEquals(
                512 + 112 * 512 * 128,
                BATBlock.calculateMaximumSize(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, 112)
        );
        assertEquals(
                4096 + 112 * 4096 * 1024,
                BATBlock.calculateMaximumSize(POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS, 112)
        );

        // Check for >2gb, which we only support via a File
        assertEquals(
                512 + 8030L * 512 * 128,
                BATBlock.calculateMaximumSize(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, 8030)
        );
        assertEquals(
                4096 + 8030L * 4096 * 1024,
                BATBlock.calculateMaximumSize(POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS, 8030)
        );
    }

    @Test
    void testUsedSectors() {
        POIFSBigBlockSize b512 = POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS;
        POIFSBigBlockSize b4096 = POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS;

        // Try first with 512 block sizes, which can hold 128 entries
        BATBlock block512 = BATBlock.createEmptyBATBlock(b512, false);
        assertTrue(block512.hasFreeSectors());
        assertEquals(0, block512.getUsedSectors(false));

        // Allocate a few
        block512.setValueAt(0, 42);
        block512.setValueAt(10, 42);
        block512.setValueAt(20, 42);
        assertTrue(block512.hasFreeSectors());
        assertEquals(3, block512.getUsedSectors(false));

        // Allocate all
        for (int i = 0; i < b512.getBATEntriesPerBlock(); i++) {
            block512.setValueAt(i, 82);
        }
        // Check
        assertFalse(block512.hasFreeSectors());
        assertEquals(128, block512.getUsedSectors(false));
        assertEquals(127, block512.getUsedSectors(true));

        // Release one
        block512.setValueAt(10, POIFSConstants.UNUSED_BLOCK);
        assertTrue(block512.hasFreeSectors());
        assertEquals(127, block512.getUsedSectors(false));
        assertEquals(126, block512.getUsedSectors(true));


        // Now repeat with 4096 block sizes
        BATBlock block4096 = BATBlock.createEmptyBATBlock(b4096, false);
        assertTrue(block4096.hasFreeSectors());
        assertEquals(0, block4096.getUsedSectors(false));

        block4096.setValueAt(0, 42);
        block4096.setValueAt(10, 42);
        block4096.setValueAt(20, 42);
        assertTrue(block4096.hasFreeSectors());
        assertEquals(3, block4096.getUsedSectors(false));

        // Allocate all
        for (int i = 0; i < b4096.getBATEntriesPerBlock(); i++) {
            block4096.setValueAt(i, 82);
        }
        // Check
        assertFalse(block4096.hasFreeSectors());
        assertEquals(1024, block4096.getUsedSectors(false));
        assertEquals(1023, block4096.getUsedSectors(true));
    }

    @Test
    void testOccupiedSize() {
        POIFSBigBlockSize b512 = POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS;
        POIFSBigBlockSize b4096 = POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS;

        // Try first with 512 block sizes, which can hold 128 entries
        BATBlock block512 = BATBlock.createEmptyBATBlock(b512, false);
        assertTrue(block512.hasFreeSectors());
        assertEquals(0, block512.getUsedSectors(false));

        // Allocate a few
        block512.setValueAt(0, 42);
        block512.setValueAt(10, 42);
        block512.setValueAt(20, 42);
        assertTrue(block512.hasFreeSectors());
        assertEquals(21, block512.getOccupiedSize());

        // Release one in the middle should not lower size
        block512.setValueAt(10, POIFSConstants.UNUSED_BLOCK);
        assertTrue(block512.hasFreeSectors());
        assertEquals(21, block512.getOccupiedSize());

        // Release the last one should lower the size
        block512.setValueAt(20, POIFSConstants.UNUSED_BLOCK);
        assertTrue(block512.hasFreeSectors());
        assertEquals(1, block512.getOccupiedSize());

        // Release first one should lower the size
        block512.setValueAt(0, POIFSConstants.UNUSED_BLOCK);
        assertTrue(block512.hasFreeSectors());
        assertEquals(0, block512.getOccupiedSize());

        // Set the last one
        block512.setValueAt(127, 42);
        assertTrue(block512.hasFreeSectors());
        assertEquals(128, block512.getOccupiedSize());

        block512.setValueAt(126, 42);
        assertTrue(block512.hasFreeSectors());
        assertEquals(128, block512.getOccupiedSize());

        block512.setValueAt(127, POIFSConstants.UNUSED_BLOCK);
        assertTrue(block512.hasFreeSectors());
        assertEquals(127, block512.getOccupiedSize());

        // Allocate all
        for (int i = 0; i < b512.getBATEntriesPerBlock(); i++) {
            block512.setValueAt(i, 82);
        }
        // Check
        assertFalse(block512.hasFreeSectors());
        assertEquals(128, block512.getOccupiedSize());

        // Release some in the beginning should not lower size
        block512.setValueAt(0, POIFSConstants.UNUSED_BLOCK);
        block512.setValueAt(1, POIFSConstants.UNUSED_BLOCK);
        block512.setValueAt(13, POIFSConstants.UNUSED_BLOCK);
        assertTrue(block512.hasFreeSectors());
        assertEquals(128, block512.getOccupiedSize());
    }

    @Test
    void testGetBATBlockAndIndex() {
        HeaderBlock header = new HeaderBlock(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS);
        List<BATBlock> blocks = new ArrayList<>();
        int offset;


        // First, try a one BAT block file
        header.setBATCount(1);
        blocks.add(
                BATBlock.createBATBlock(header.getBigBlockSize(), ByteBuffer.allocate(512))
        );

        offset = 0;
        assertEquals(0, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(0, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));

        offset = 1;
        assertEquals(1, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(0, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));

        offset = 127;
        assertEquals(127, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(0, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));


        // Now go for one with multiple BAT blocks
        header.setBATCount(2);
        blocks.add(
                BATBlock.createBATBlock(header.getBigBlockSize(), ByteBuffer.allocate(512))
        );

        offset = 0;
        assertEquals(0, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(0, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));

        offset = 127;
        assertEquals(127, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(0, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));

        offset = 128;
        assertEquals(0, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(1, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));

        offset = 129;
        assertEquals(1, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(1, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));


        // The XBAT count makes no difference, as we flatten in memory
        header.setBATCount(1);
        header.setXBATCount(1);
        offset = 0;
        assertEquals(0, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(0, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));

        offset = 126;
        assertEquals(126, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(0, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));

        offset = 127;
        assertEquals(127, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(0, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));

        offset = 128;
        assertEquals(0, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(1, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));

        offset = 129;
        assertEquals(1, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(1, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));


        // Check with the bigger block size too
        header = new HeaderBlock(POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS);

        offset = 0;
        assertEquals(0, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(0, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));

        offset = 1022;
        assertEquals(1022, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(0, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));

        offset = 1023;
        assertEquals(1023, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(0, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));

        offset = 1024;
        assertEquals(0, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(1, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));

        // Biggr block size, back to real BATs
        header.setBATCount(2);

        offset = 0;
        assertEquals(0, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(0, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));

        offset = 1022;
        assertEquals(1022, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(0, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));

        offset = 1023;
        assertEquals(1023, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(0, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));

        offset = 1024;
        assertEquals(0, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
        assertEquals(1, blocks.indexOf(BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock()));
    }
}
