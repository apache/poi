
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

import java.io.IOException;
import java.io.OutputStream;

import java.util.*;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.filesystem.BATManaged;
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

public class BlockAllocationTableWriter
    implements BlockWritable, BATManaged
{
    private IntList    _entries;
    private BATBlock[] _blocks;
    private int        _start_block;

    /**
     * create a BlockAllocationTableWriter
     */

    public BlockAllocationTableWriter()
    {
        _start_block = POIFSConstants.END_OF_CHAIN;
        _entries     = new IntList();
        _blocks      = new BATBlock[ 0 ];
    }

    /**
     * Create the BATBlocks we need
     *
     * @return start block index of BAT blocks
     */

    public int createBlocks()
    {
        int xbat_blocks = 0;
        int bat_blocks  = 0;

        while (true)
        {
            int calculated_bat_blocks  =
                BATBlock.calculateStorageRequirements(bat_blocks
                                                      + xbat_blocks
                                                      + _entries.size());
            int calculated_xbat_blocks =
                HeaderBlockWriter
                    .calculateXBATStorageRequirements(calculated_bat_blocks);

            if ((bat_blocks == calculated_bat_blocks)
                    && (xbat_blocks == calculated_xbat_blocks))
            {

                // stable ... we're OK
                break;
            }
            else
            {
                bat_blocks  = calculated_bat_blocks;
                xbat_blocks = calculated_xbat_blocks;
            }
        }
        int startBlock = allocateSpace(bat_blocks);

        allocateSpace(xbat_blocks);
        simpleCreateBlocks();
        return startBlock;
    }

    /**
     * Allocate space for a block of indices
     *
     * @param blockCount the number of blocks to allocate space for
     *
     * @return the starting index of the blocks
     */

    public int allocateSpace(final int blockCount)
    {
        int startBlock = _entries.size();

        if (blockCount > 0)
        {
            int limit = blockCount - 1;
            int index = startBlock + 1;

            for (int k = 0; k < limit; k++)
            {
                _entries.add(index++);
            }
            _entries.add(POIFSConstants.END_OF_CHAIN);
        }
        return startBlock;
    }

    /**
     * get the starting block
     *
     * @return the starting block index
     */

    public int getStartBlock()
    {
        return _start_block;
    }

    /**
     * create the BATBlocks
     */

    void simpleCreateBlocks()
    {
        _blocks = BATBlock.createBATBlocks(_entries.toArray());
    }

    /* ********** START implementation of BlockWritable ********** */

    /**
     * Write the storage to an OutputStream
     *
     * @param stream the OutputStream to which the stored data should
     *               be written
     *
     * @exception IOException on problems writing to the specified
     *            stream
     */

    public void writeBlocks(final OutputStream stream)
        throws IOException
    {
        for (int j = 0; j < _blocks.length; j++)
        {
            _blocks[ j ].writeBlocks(stream);
        }
    }

    /* **********  END  implementation of BlockWritable ********** */
    /* ********** START implementation of BATManaged ********** */

    /**
     * Return the number of BigBlock's this instance uses
     *
     * @return count of BigBlock instances
     */

    public int countBlocks()
    {
        return _blocks.length;
    }

    /**
     * Set the start block for this instance
     *
     * @param index index into the array of BigBlock instances making
     *              up the the filesystem
     *
     * @param start_block
     */

    public void setStartBlock(int start_block)
    {
        _start_block = start_block;
    }

    /* **********  END  implementation of BATManaged ********** */
}   // end class BlockAllocationTableWriter

