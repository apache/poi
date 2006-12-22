
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

import java.io.*;

import java.util.*;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.IntegerField;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LongField;
import org.apache.poi.util.ShortField;

/**
 * The block containing the archive header
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class HeaderBlockWriter
    extends BigBlock
    implements HeaderBlockConstants
{
    private static final byte _default_value = ( byte ) 0xFF;

    // number of big block allocation table blocks (int)
    private IntegerField      _bat_count;

    // start of the property set block (int index of the property set
    // chain's first big block)
    private IntegerField      _property_start;

    // start of the small block allocation table (int index of small
    // block allocation table's first big block)
    private IntegerField      _sbat_start;

    // number of big blocks holding the small block allocation table
    private IntegerField      _sbat_block_count;

    // big block index for extension to the big block allocation table
    private IntegerField      _xbat_start;
    private IntegerField      _xbat_count;
    private byte[]            _data;

    /**
     * Create a single instance initialized with default values
     */

    public HeaderBlockWriter()
    {
        _data = new byte[ POIFSConstants.BIG_BLOCK_SIZE ];
        Arrays.fill(_data, _default_value);
        new LongField(_signature_offset, _signature, _data);
        new IntegerField(0x08, 0, _data);
        new IntegerField(0x0c, 0, _data);
        new IntegerField(0x10, 0, _data);
        new IntegerField(0x14, 0, _data);
        new ShortField(0x18, ( short ) 0x3b, _data);
        new ShortField(0x1a, ( short ) 0x3, _data);
        new ShortField(0x1c, ( short ) -2, _data);
        new ShortField(0x1e, ( short ) 0x9, _data);
        new IntegerField(0x20, 0x6, _data);
        new IntegerField(0x24, 0, _data);
        new IntegerField(0x28, 0, _data);
        _bat_count      = new IntegerField(_bat_count_offset, 0, _data);
        _property_start = new IntegerField(_property_start_offset,
                                           POIFSConstants.END_OF_CHAIN,
                                           _data);
        new IntegerField(0x34, 0, _data);
        new IntegerField(0x38, 0x1000, _data);
        _sbat_start = new IntegerField(_sbat_start_offset,
                                       POIFSConstants.END_OF_CHAIN, _data);
        _sbat_block_count = new IntegerField(_sbat_block_count_offset, 0,
					     _data);
        _xbat_start = new IntegerField(_xbat_start_offset,
                                       POIFSConstants.END_OF_CHAIN, _data);
        _xbat_count = new IntegerField(_xbat_count_offset, 0, _data);
    }

    /**
     * Set BAT block parameters. Assumes that all BAT blocks are
     * contiguous. Will construct XBAT blocks if necessary and return
     * the array of newly constructed XBAT blocks.
     *
     * @param blockCount count of BAT blocks
     * @param startBlock index of first BAT block
     *
     * @return array of XBAT blocks; may be zero length, will not be
     *         null
     */

    public BATBlock [] setBATBlocks(final int blockCount,
                                    final int startBlock)
    {
        BATBlock[] rvalue;

        _bat_count.set(blockCount, _data);
        int limit  = Math.min(blockCount, _max_bats_in_header);
        int offset = _bat_array_offset;

        for (int j = 0; j < limit; j++)
        {
            new IntegerField(offset, startBlock + j, _data);
            offset += LittleEndianConsts.INT_SIZE;
        }
        if (blockCount > _max_bats_in_header)
        {
            int   excess_blocks      = blockCount - _max_bats_in_header;
            int[] excess_block_array = new int[ excess_blocks ];

            for (int j = 0; j < excess_blocks; j++)
            {
                excess_block_array[ j ] = startBlock + j
                                          + _max_bats_in_header;
            }
            rvalue = BATBlock.createXBATBlocks(excess_block_array,
                                               startBlock + blockCount);
            _xbat_start.set(startBlock + blockCount, _data);
        }
        else
        {
            rvalue = BATBlock.createXBATBlocks(new int[ 0 ], 0);
            _xbat_start.set(POIFSConstants.END_OF_CHAIN, _data);
        }
        _xbat_count.set(rvalue.length, _data);
        return rvalue;
    }

    /**
     * Set start of Property Table
     *
     * @param startBlock the index of the first block of the Property
     *                   Table
     */

    public void setPropertyStart(final int startBlock)
    {
        _property_start.set(startBlock, _data);
    }

    /**
     * Set start of small block allocation table
     *
     * @param startBlock the index of the first big block of the small
     *                   block allocation table
     */

    public void setSBATStart(final int startBlock)
    {
        _sbat_start.set(startBlock, _data);
    }

    /**
     * Set count of SBAT blocks
     *
     * @param count the number of SBAT blocks
     */

    public void setSBATBlockCount(final int count)
    {
	_sbat_block_count.set(count, _data);
    }

    /**
     * For a given number of BAT blocks, calculate how many XBAT
     * blocks will be needed
     *
     * @param blockCount number of BAT blocks
     *
     * @return number of XBAT blocks needed
     */

    static int calculateXBATStorageRequirements(final int blockCount)
    {
        return (blockCount > _max_bats_in_header)
               ? BATBlock.calculateXBATStorageRequirements(blockCount
                   - _max_bats_in_header)
               : 0;
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
}   // end public class HeaderBlockWriter

