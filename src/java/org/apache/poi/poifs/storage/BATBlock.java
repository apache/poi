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
import java.io.OutputStream;

import java.util.Arrays;

import org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.IntegerField;
import org.apache.poi.util.LittleEndianConsts;

/**
 * A block of block allocation table entries. BATBlocks are created
 * only through a static factory method: createBATBlocks.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */
public final class BATBlock extends BigBlock {
    private static final byte _default_value          = ( byte ) 0xFF;
    private IntegerField[]    _fields;
    private byte[]            _data;

    /**
     * Create a single instance initialized with default values
     */

    private BATBlock(POIFSBigBlockSize bigBlockSize)
    {
        super(bigBlockSize);
        
        int _entries_per_block = bigBlockSize.getBATEntriesPerBlock(); 
        
        _data = new byte[ bigBlockSize.getBigBlockSize() ];
        Arrays.fill(_data, _default_value);
        _fields = new IntegerField[ _entries_per_block ];
        int offset = 0;

        for (int j = 0; j < _entries_per_block; j++)
        {
            _fields[ j ] = new IntegerField(offset);
            offset       += LittleEndianConsts.INT_SIZE;
        }
    }

    /**
     * Create an array of BATBlocks from an array of int block
     * allocation table entries
     *
     * @param entries the array of int entries
     *
     * @return the newly created array of BATBlocks
     */

    public static BATBlock [] createBATBlocks(final POIFSBigBlockSize bigBlockSize, final int [] entries)
    {
        int        block_count = calculateStorageRequirements(bigBlockSize, entries.length);
        BATBlock[] blocks      = new BATBlock[ block_count ];
        int        index       = 0;
        int        remaining   = entries.length;

        int _entries_per_block = bigBlockSize.getBATEntriesPerBlock();
        for (int j = 0; j < entries.length; j += _entries_per_block)
        {
            blocks[ index++ ] = new BATBlock(bigBlockSize, entries, j,
                                             (remaining > _entries_per_block)
                                             ? j + _entries_per_block
                                             : entries.length);
            remaining         -= _entries_per_block;
        }
        return blocks;
    }

    /**
     * Create an array of XBATBlocks from an array of int block
     * allocation table entries
     *
     * @param entries the array of int entries
     * @param startBlock the start block of the array of XBAT blocks
     *
     * @return the newly created array of BATBlocks
     */

    public static BATBlock [] createXBATBlocks(final POIFSBigBlockSize bigBlockSize,
                                               final int [] entries,
                                               final int startBlock)
    {
        int        block_count =
            calculateXBATStorageRequirements(bigBlockSize, entries.length);
        BATBlock[] blocks      = new BATBlock[ block_count ];
        int        index       = 0;
        int        remaining   = entries.length;

        int _entries_per_xbat_block = bigBlockSize.getXBATEntriesPerBlock();
        if (block_count != 0)
        {
            for (int j = 0; j < entries.length; j += _entries_per_xbat_block)
            {
                blocks[ index++ ] =
                    new BATBlock(bigBlockSize, entries, j,
                                 (remaining > _entries_per_xbat_block)
                                 ? j + _entries_per_xbat_block
                                 : entries.length);
                remaining         -= _entries_per_xbat_block;
            }
            for (index = 0; index < blocks.length - 1; index++)
            {
                blocks[ index ].setXBATChain(bigBlockSize, startBlock + index + 1);
            }
            blocks[ index ].setXBATChain(bigBlockSize, POIFSConstants.END_OF_CHAIN);
        }
        return blocks;
    }

    /**
     * Calculate how many BATBlocks are needed to hold a specified
     * number of BAT entries.
     *
     * @param entryCount the number of entries
     *
     * @return the number of BATBlocks needed
     */

    public static int calculateStorageRequirements(final POIFSBigBlockSize bigBlockSize, final int entryCount)
    {
        int _entries_per_block = bigBlockSize.getBATEntriesPerBlock();
        return (entryCount + _entries_per_block - 1) / _entries_per_block;
    }

    /**
     * Calculate how many XBATBlocks are needed to hold a specified
     * number of BAT entries.
     *
     * @param entryCount the number of entries
     *
     * @return the number of XBATBlocks needed
     */

    public static int calculateXBATStorageRequirements(final POIFSBigBlockSize bigBlockSize, final int entryCount)
    {
        int _entries_per_xbat_block = bigBlockSize.getXBATEntriesPerBlock();
        return (entryCount + _entries_per_xbat_block - 1)
               / _entries_per_xbat_block;
    }

    private void setXBATChain(final POIFSBigBlockSize bigBlockSize, int chainIndex)
    {
        int _entries_per_xbat_block = bigBlockSize.getXBATEntriesPerBlock();
        _fields[ _entries_per_xbat_block ].set(chainIndex, _data);
    }

    /**
     * Create a single instance initialized (perhaps partially) with entries
     *
     * @param entries the array of block allocation table entries
     * @param start_index the index of the first entry to be written
     *                    to the block
     * @param end_index the index, plus one, of the last entry to be
     *                  written to the block (writing is for all index
     *                  k, start_index <= k < end_index)
     */

    private BATBlock(POIFSBigBlockSize bigBlockSize, final int [] entries,
                     final int start_index, final int end_index)
    {
        this(bigBlockSize);
        for (int k = start_index; k < end_index; k++)
        {
            _fields[ k - start_index ].set(entries[ k ], _data);
        }
    }

    /* ********** START extension of BigBlock ********** */

    /**
     * Write the block's data to an OutputStream
     *
     * @param stream the OutputStream to which the stored data should
     *               be written
     *
     * @exception IOException on problems writing to the specified
     *            stream
     */

    void writeData(final OutputStream stream)
        throws IOException
    {
        doWriteData(stream, _data);
    }

    /* **********  END  extension of BigBlock ********** */
}   // end public class BATBlock

