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

import java.io.IOException;

import java.util.*;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.IntList;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * This class manages and creates the Block Allocation Table, which is
 * basically a set of linked lists of block indices.
 * <P>
 * Each block of the filesystem has an index. The first block, the
 * header, is skipped; the first block after the header is index 0,
 * the next is index 1, and so on.
 * <P>
 * A block's index is also its index into the Block Allocation
 * Table. The entry that it finds in the Block Allocation Table is the
 * index of the next block in the linked list of blocks making up a
 * file, or it is set to -2: end of list.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */
public final class BlockAllocationTableReader {
    private IntList _entries;

    /**
     * create a BlockAllocationTableReader for an existing filesystem. Side
     * effect: when this method finishes, the BAT blocks will have
     * been removed from the raw block list, and any blocks labeled as
     * 'unused' in the block allocation table will also have been
     * removed from the raw block list.
     *
     * @param block_count the number of BAT blocks making up the block
     *                    allocation table
     * @param block_array the array of BAT block indices from the
     *                    filesystem's header
     * @param xbat_count the number of XBAT blocks
     * @param xbat_index the index of the first XBAT block
     * @param raw_block_list the list of RawDataBlocks
     *
     * @exception IOException if, in trying to create the table, we
     *            encounter logic errors
     */

    public BlockAllocationTableReader(final int block_count,
                                      final int [] block_array,
                                      final int xbat_count,
                                      final int xbat_index,
                                      final BlockList raw_block_list)
        throws IOException
    {
        this();
        if (block_count <= 0)
        {
            throw new IOException(
                "Illegal block count; minimum count is 1, got " + block_count
                + " instead");
        }

        // acquire raw data blocks containing the BAT block data
        RawDataBlock blocks[] = new RawDataBlock[ block_count ];
        int          limit    = Math.min(block_count, block_array.length);
        int          block_index;

        for (block_index = 0; block_index < limit; block_index++)
        {
            blocks[ block_index ] =
                ( RawDataBlock ) raw_block_list
                    .remove(block_array[ block_index ]);
        }
        if (block_index < block_count)
        {

            // must have extended blocks
            if (xbat_index < 0)
            {
                throw new IOException(
                    "BAT count exceeds limit, yet XBAT index indicates no valid entries");
            }
            int chain_index           = xbat_index;
            int max_entries_per_block = BATBlock.entriesPerXBATBlock();
            int chain_index_offset    = BATBlock.getXBATChainOffset();

            for (int j = 0; j < xbat_count; j++)
            {
                limit = Math.min(block_count - block_index,
                                 max_entries_per_block);
                byte[] data   = raw_block_list.remove(chain_index).getData();
                int    offset = 0;

                for (int k = 0; k < limit; k++)
                {
                    blocks[ block_index++ ] =
                        ( RawDataBlock ) raw_block_list
                            .remove(LittleEndian.getInt(data, offset));
                    offset                  += LittleEndianConsts.INT_SIZE;
                }
                chain_index = LittleEndian.getInt(data, chain_index_offset);
                if (chain_index == POIFSConstants.END_OF_CHAIN)
                {
                    break;
                }
            }
        }
        if (block_index != block_count)
        {
            throw new IOException("Could not find all blocks");
        }

        // now that we have all of the raw data blocks, go through and
        // create the indices
        setEntries(blocks, raw_block_list);
    }

    /**
     * create a BlockAllocationTableReader from an array of raw data blocks
     *
     * @param blocks the raw data
     * @param raw_block_list the list holding the managed blocks
     *
     * @exception IOException
     */

    BlockAllocationTableReader(final ListManagedBlock [] blocks,
                               final BlockList raw_block_list)
        throws IOException
    {
        this();
        setEntries(blocks, raw_block_list);
    }

    BlockAllocationTableReader()
    {
        _entries = new IntList();
    }

    /**
     * walk the entries from a specified point and return the
     * associated blocks. The associated blocks are removed from the
     * block list
     *
     * @param startBlock the first block in the chain
     * @param blockList the raw data block list
     *
     * @return array of ListManagedBlocks, in their correct order
     *
     * @exception IOException if there is a problem acquiring the blocks
     */
    ListManagedBlock [] fetchBlocks(final int startBlock,
                                    final int headerPropertiesStartBlock,
                                    final BlockList blockList)
        throws IOException
    {
        List<ListManagedBlock> blocks = new ArrayList<ListManagedBlock>();
        int  currentBlock = startBlock;
        boolean firstPass = true;
        ListManagedBlock dataBlock = null;

        // Process the chain from the start to the end
        // Normally we have header, data, end
        // Sometimes we have data, header, end
        // For those cases, stop at the header, not the end
        while (currentBlock != POIFSConstants.END_OF_CHAIN) {
        	try {
        		// Grab the data at the current block offset
        		dataBlock = blockList.remove(currentBlock);
        		blocks.add(dataBlock);
        		// Now figure out which block we go to next
                currentBlock = _entries.get(currentBlock);
                firstPass = false;
        	} catch(IOException e) {
        		if(currentBlock == headerPropertiesStartBlock) {
        			// Special case where things are in the wrong order
        			System.err.println("Warning, header block comes after data blocks in POIFS block listing");
        			currentBlock = POIFSConstants.END_OF_CHAIN;
        		} else if(currentBlock == 0 && firstPass) {
        			// Special case where the termination isn't done right
        			//  on an empty set
        			System.err.println("Warning, incorrectly terminated empty data blocks in POIFS block listing (should end at -2, ended at 0)");
        			currentBlock = POIFSConstants.END_OF_CHAIN;
        		} else {
        			// Ripple up
        			throw e;
        		}
        	}
        }

        return blocks.toArray(new ListManagedBlock[blocks.size()]);
    }

    // methods for debugging reader

    /**
     * determine whether the block specified by index is used or not
     *
     * @param index index of block in question
     *
     * @return true if the specific block is used, else false
     */
    boolean isUsed(final int index)
    {
        boolean rval = false;

        try
        {
            rval = _entries.get(index) != -1;
        } catch (IndexOutOfBoundsException e) {
            // ignored
        }
        return rval;
    }

    /**
     * return the next block index
     *
     * @param index of the current block
     *
     * @return index of the next block (may be
     *         POIFSConstants.END_OF_CHAIN, indicating end of chain
     *         (duh))
     *
     * @exception IOException if the current block is unused
     */
    int getNextBlockIndex(final int index)
        throws IOException
    {
        if (isUsed(index))
        {
            return _entries.get(index);
        }
        throw new IOException("index " + index + " is unused");
    }

    /**
     * Convert an array of blocks into a set of integer indices
     *
     * @param blocks the array of blocks containing the indices
     * @param raw_blocks the list of blocks being managed. Unused
     *                   blocks will be eliminated from the list
     */
    private void setEntries(final ListManagedBlock [] blocks,
                            final BlockList raw_blocks)
        throws IOException
    {
        int limit = BATBlock.entriesPerBlock();

        for (int block_index = 0; block_index < blocks.length; block_index++)
        {
            byte[] data   = blocks[ block_index ].getData();
            int    offset = 0;

            for (int k = 0; k < limit; k++)
            {
                int entry = LittleEndian.getInt(data, offset);

                if (entry == POIFSConstants.UNUSED_BLOCK)
                {
                    raw_blocks.zap(_entries.size());
                }
                _entries.add(entry);
                offset += LittleEndianConsts.INT_SIZE;
            }

            // discard block
            blocks[ block_index ] = null;
        }
        raw_blocks.setBAT(this);
    }
}
