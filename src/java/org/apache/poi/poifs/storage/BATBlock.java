
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

import java.util.Arrays;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.IntegerField;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * A block of block allocation table entries. BATBlocks are created
 * only through a static factory method: createBATBlocks.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class BATBlock
    extends BigBlock
{
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

