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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.apache.poi.poifs.common.POIFSConstants;

/**
 * Class to test BATBlock functionality
 *
 * @author Marc Johnson
 */
public final class TestBATBlock extends TestCase {

    /**
     * Test the createBATBlocks method. The test involves setting up
     * various arrays of int's and ensuring that the correct number of
     * BATBlocks is created for each array, and that the data from
     * each array is correctly written to the BATBlocks.
     */
    public void testCreateBATBlocks() throws IOException {

        // test 0 length array (basic sanity)
        BATBlock[] rvalue = BATBlock.createBATBlocks(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, createTestArray(0));

        assertEquals(0, rvalue.length);

        // test array of length 1
        rvalue = BATBlock.createBATBlocks(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, createTestArray(1));
        assertEquals(1, rvalue.length);
        verifyContents(rvalue, 1);

        // test array of length 127
        rvalue = BATBlock.createBATBlocks(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, createTestArray(127));
        assertEquals(1, rvalue.length);
        verifyContents(rvalue, 127);

        // test array of length 128
        rvalue = BATBlock.createBATBlocks(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, createTestArray(128));
        assertEquals(1, rvalue.length);
        verifyContents(rvalue, 128);

        // test array of length 129
        rvalue = BATBlock.createBATBlocks(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, createTestArray(129));
        assertEquals(2, rvalue.length);
        verifyContents(rvalue, 129);
    }

    private static int[] createTestArray(int count) {
        int[] rvalue = new int[ count ];

        for (int j = 0; j < count; j++)
        {
            rvalue[ j ] = j;
        }
        return rvalue;
    }

    private static void verifyContents(BATBlock[] blocks, int entries) throws IOException {
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

        for (BATBlock block : blocks) {
            block.writeBlocks(stream);
        }
        byte[] actual = stream.toByteArray();

        assertEquals(expected.length, actual.length);
        for (int j = 0; j < expected.length; j++)
        {
            assertEquals(expected[ j ], actual[ j ]);
        }
    }

    public void testCreateXBATBlocks() throws IOException {
        // test 0 length array (basic sanity)
        BATBlock[] rvalue = BATBlock.createXBATBlocks(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, createTestArray(0), 1);

        assertEquals(0, rvalue.length);

        // test array of length 1
        rvalue = BATBlock.createXBATBlocks(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, createTestArray(1), 1);
        assertEquals(1, rvalue.length);
        verifyXBATContents(rvalue, 1, 1);

        // test array of length 127
        rvalue = BATBlock.createXBATBlocks(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, createTestArray(127), 1);
        assertEquals(1, rvalue.length);
        verifyXBATContents(rvalue, 127, 1);

        // test array of length 128
        rvalue = BATBlock.createXBATBlocks(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, createTestArray(128), 1);
        assertEquals(2, rvalue.length);
        verifyXBATContents(rvalue, 128, 1);

        // test array of length 254
        rvalue = BATBlock.createXBATBlocks(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, createTestArray(254), 1);
        assertEquals(2, rvalue.length);
        verifyXBATContents(rvalue, 254, 1);

        // test array of length 255
        rvalue = BATBlock.createXBATBlocks(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, createTestArray(255), 1);
        assertEquals(3, rvalue.length);
        verifyXBATContents(rvalue, 255, 1);
    }

    private static void verifyXBATContents(BATBlock[] blocks, int entries, int start_block)
			throws IOException {
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

        for (BATBlock block : blocks) {
            block.writeBlocks(stream);
        }
        byte[] actual = stream.toByteArray();

        assertEquals(expected.length, actual.length);
        for (int j = 0; j < expected.length; j++)
        {
            assertEquals("offset " + j, expected[ j ], actual[ j ]);
        }
    }

    public void testCalculateXBATStorageRequirements() {
        int[] blockCounts = { 0, 1, 127, 128 };
        int[] requirements = { 0, 1, 1, 2 };

        for (int j = 0; j < blockCounts.length; j++)
        {
            assertEquals(
                "requirement for " + blockCounts[ j ], requirements[ j ],
                BATBlock.calculateXBATStorageRequirements(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, blockCounts[ j ]));
        }
    }

    public void testEntriesPerBlock() {
        assertEquals(128, POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS.getBATEntriesPerBlock()); 
    }
    public void testEntriesPerXBATBlock() {
        assertEquals(127, POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS.getXBATEntriesPerBlock());
    }
    public void testGetXBATChainOffset() {
        assertEquals(508, POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS.getNextXBATChainOffset());
    }
    
    public void testCalculateMaximumSize() throws Exception {
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
             512 + 512*128, 
             BATBlock.calculateMaximumSize(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, 1)
       );
       assertEquals(
             4096 + 4096*1024, 
             BATBlock.calculateMaximumSize(POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS, 1)
       );
       
       assertEquals(
             512 + 4*512*128, 
             BATBlock.calculateMaximumSize(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, 4)
       );
       assertEquals(
             4096 + 4*4096*1024, 
             BATBlock.calculateMaximumSize(POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS, 4)
       );
       
       // One XBAT block holds 127/1023 individual BAT blocks, so they can address
       //  a fairly hefty amount of space themselves
       // However, the BATs continue as before
       assertEquals(
             512 + 109*512*128, 
             BATBlock.calculateMaximumSize(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, 109)
       );
       assertEquals(
             4096 + 109*4096*1024, 
             BATBlock.calculateMaximumSize(POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS, 109)
       );
             
       assertEquals(
             512 + 110*512*128, 
             BATBlock.calculateMaximumSize(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, 110)
       );
       assertEquals(
             4096 + 110*4096*1024, 
             BATBlock.calculateMaximumSize(POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS, 110)
       );
       
       assertEquals(
             512 + 112*512*128, 
             BATBlock.calculateMaximumSize(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, 112)
       );
       assertEquals(
             4096 + 112*4096*1024, 
             BATBlock.calculateMaximumSize(POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS, 112)
       );
       
       // Check for >2gb, which we only support via a File
       assertEquals(
               512 + 8030l*512*128, 
               BATBlock.calculateMaximumSize(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS, 8030)
       );
       assertEquals(
               4096 + 8030l*4096*1024, 
               BATBlock.calculateMaximumSize(POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS, 8030)
       );
    }
    
    public void testUsedSectors() throws Exception {
        POIFSBigBlockSize b512 = POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS;
        POIFSBigBlockSize b4096 = POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS;
        
        // Try first with 512 block sizes, which can hold 128 entries
        BATBlock block512 = BATBlock.createEmptyBATBlock(b512, false);
        assertEquals(true, block512.hasFreeSectors());
        assertEquals(0, block512.getUsedSectors(false));

        // Allocate a few
        block512.setValueAt(0, 42);
        block512.setValueAt(10, 42);
        block512.setValueAt(20, 42);
        assertEquals(true, block512.hasFreeSectors());
        assertEquals(3, block512.getUsedSectors(false));
        
        // Allocate all
        for (int i=0; i<b512.getBATEntriesPerBlock(); i++) {
            block512.setValueAt(i, 82);
        }
        // Check
        assertEquals(false, block512.hasFreeSectors());
        assertEquals(128, block512.getUsedSectors(false));
        assertEquals(127, block512.getUsedSectors(true));
        
        // Release one
        block512.setValueAt(10, POIFSConstants.UNUSED_BLOCK);
        assertEquals(true, block512.hasFreeSectors());
        assertEquals(127, block512.getUsedSectors(false));
        assertEquals(126, block512.getUsedSectors(true));
        
        
        // Now repeat with 4096 block sizes
        BATBlock block4096 = BATBlock.createEmptyBATBlock(b4096, false);
        assertEquals(true, block4096.hasFreeSectors());
        assertEquals(0, block4096.getUsedSectors(false));
        
        block4096.setValueAt(0, 42);
        block4096.setValueAt(10, 42);
        block4096.setValueAt(20, 42);
        assertEquals(true, block4096.hasFreeSectors());
        assertEquals(3, block4096.getUsedSectors(false));
        
        // Allocate all
        for (int i=0; i<b4096.getBATEntriesPerBlock(); i++) {
            block4096.setValueAt(i, 82);
        }
        // Check
        assertEquals(false, block4096.hasFreeSectors());
        assertEquals(1024, block4096.getUsedSectors(false));
        assertEquals(1023, block4096.getUsedSectors(true));
    }
    
    public void testGetBATBlockAndIndex() throws Exception {
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
       assertEquals(0, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));
       
       offset = 1;
       assertEquals(1, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
       assertEquals(0, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));
       
       offset = 127;
       assertEquals(127, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
       assertEquals(0, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));
       
       
       // Now go for one with multiple BAT blocks
       header.setBATCount(2);
       blocks.add(
             BATBlock.createBATBlock(header.getBigBlockSize(), ByteBuffer.allocate(512))
       );
       
       offset = 0;
       assertEquals(0, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
       assertEquals(0, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));
       
       offset = 127;
       assertEquals(127, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
       assertEquals(0, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));
       
       offset = 128;
       assertEquals(0, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
       assertEquals(1, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));
       
       offset = 129;
       assertEquals(1, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
       assertEquals(1, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));
       
       
       // The XBAT count makes no difference, as we flatten in memory
       header.setBATCount(1);
       header.setXBATCount(1);
       offset = 0;
       assertEquals(0, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
       assertEquals(0, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));
       
       offset = 126;
       assertEquals(126, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
       assertEquals(0, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));
       
       offset = 127;
       assertEquals(127, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
       assertEquals(0, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));
       
       offset = 128;
       assertEquals(0, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
       assertEquals(1, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));
       
       offset = 129;
       assertEquals(1, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
       assertEquals(1, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));
       
       
       // Check with the bigger block size too
       header = new HeaderBlock(POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS);
       
       offset = 0;
       assertEquals(0, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
       assertEquals(0, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));
       
       offset = 1022;
       assertEquals(1022, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
       assertEquals(0, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));
       
       offset = 1023;
       assertEquals(1023, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
       assertEquals(0, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));
       
       offset = 1024;
       assertEquals(0, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
       assertEquals(1, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));

       // Biggr block size, back to real BATs
       header.setBATCount(2);
       
       offset = 0;
       assertEquals(0, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
       assertEquals(0, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));
       
       offset = 1022;
       assertEquals(1022, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
       assertEquals(0, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));
       
       offset = 1023;
       assertEquals(1023, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
       assertEquals(0, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));
       
       offset = 1024;
       assertEquals(0, BATBlock.getBATBlockAndIndex(offset, header, blocks).getIndex());
       assertEquals(1, blocks.indexOf( BATBlock.getBATBlockAndIndex(offset, header, blocks).getBlock() ));
    }
}
