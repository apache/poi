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
    private static final int  _entries_per_block      =
        POIFSConstants.BIG_BLOCK_SIZE / LittleEndianConsts.INT_SIZE;
    private static final int  _entries_per_xbat_block = _entries_per_block
                                                            - 1;
    private static final int  _xbat_chain_offset      =
        _entries_per_xbat_block * LittleEndianConsts.INT_SIZE;
    private static final byte _default_value          = ( byte ) 0xFF;
    private IntegerField[]    _fields;
    private byte[]            _data;

    /**
     * Create a single instance initialized with default values
     */

    private BATBlock()
    {
        _data = new byte[ POIFSConstants.BIG_BLOCK_SIZE ];
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

    public static BATBlock [] createBATBlocks(final int [] entries)
    {
        int        block_count = calculateStorageRequirements(entries.length);
        BATBlock[] blocks      = new BATBlock[ block_count ];
        int        index       = 0;
        int        remaining   = entries.length;

        for (int j = 0; j < entries.length; j += _entries_per_block)
        {
            blocks[ index++ ] = new BATBlock(entries, j,
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

    public static BATBlock [] createXBATBlocks(final int [] entries,
                                               final int startBlock)
    {
        int        block_count =
            calculateXBATStorageRequirements(entries.length);
        BATBlock[] blocks      = new BATBlock[ block_count ];
        int        index       = 0;
        int        remaining   = entries.length;

        if (block_count != 0)
        {
            for (int j = 0; j < entries.length; j += _entries_per_xbat_block)
            {
                blocks[ index++ ] =
                    new BATBlock(entries, j,
                                 (remaining > _entries_per_xbat_block)
                                 ? j + _entries_per_xbat_block
                                 : entries.length);
                remaining         -= _entries_per_xbat_block;
            }
            for (index = 0; index < blocks.length - 1; index++)
            {
                blocks[ index ].setXBATChain(startBlock + index + 1);
            }
            blocks[ index ].setXBATChain(POIFSConstants.END_OF_CHAIN);
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

    public static int calculateStorageRequirements(final int entryCount)
    {
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

    public static int calculateXBATStorageRequirements(final int entryCount)
    {
        return (entryCount + _entries_per_xbat_block - 1)
               / _entries_per_xbat_block;
    }

    /**
     * @return number of entries per block
     */

    public static final int entriesPerBlock()
    {
        return _entries_per_block;
    }

    /**
     * @return number of entries per XBAT block
     */

    public static final int entriesPerXBATBlock()
    {
        return _entries_per_xbat_block;
    }

    /**
     * @return offset of chain index of XBAT block
     */

    public static final int getXBATChainOffset()
    {
        return _xbat_chain_offset;
    }

    private void setXBATChain(int chainIndex)
    {
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

    private BATBlock(final int [] entries, final int start_index,
                     final int end_index)
    {
        this();
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

