
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

import java.io.*;

import java.util.*;

import org.apache.poi.poifs.common.POIFSConstants;

/**
 * Storage for documents that are too small to use regular
 * DocumentBlocks for their data
 *
 * @author  Marc Johnson (mjohnson at apache dot org)
 */

public class SmallDocumentBlock
    implements BlockWritable, ListManagedBlock
{
    private byte[]            _data;
    private static final byte _default_fill         = ( byte ) 0xff;
    private static final int  _block_size           = 64;
    private static final int  _blocks_per_big_block =
        POIFSConstants.BIG_BLOCK_SIZE / _block_size;

    private SmallDocumentBlock(final byte [] data, final int index)
    {
        this();
        System.arraycopy(data, index * _block_size, _data, 0, _block_size);
    }

    private SmallDocumentBlock()
    {
        _data = new byte[ _block_size ];
    }

    /**
     * convert a single long array into an array of SmallDocumentBlock
     * instances
     *
     * @param array the byte array to be converted
     * @param size the intended size of the array (which may be smaller)
     *
     * @return an array of SmallDocumentBlock instances, filled from
     *         the array
     */

    public static SmallDocumentBlock [] convert(final byte [] array,
                                                final int size)
    {
        SmallDocumentBlock[] rval   =
            new SmallDocumentBlock[ (size + _block_size - 1) / _block_size ];
        int                  offset = 0;

        for (int k = 0; k < rval.length; k++)
        {
            rval[ k ] = new SmallDocumentBlock();
            if (offset < array.length)
            {
                int length = Math.min(_block_size, array.length - offset);

                System.arraycopy(array, offset, rval[ k ]._data, 0, length);
                if (length != _block_size)
                {
                    Arrays.fill(rval[ k ]._data, length, _block_size,
                                _default_fill);
                }
            }
            else
            {
                Arrays.fill(rval[ k ]._data, _default_fill);
            }
            offset += _block_size;
        }
        return rval;
    }

    /**
     * fill out a List of SmallDocumentBlocks so that it fully occupies
     * a set of big blocks
     *
     * @param blocks the List to be filled out
     *
     * @return number of big blocks the list encompasses
     */

    public static int fill(final List blocks)
    {
        int count           = blocks.size();
        int big_block_count = (count + _blocks_per_big_block - 1)
                              / _blocks_per_big_block;
        int full_count      = big_block_count * _blocks_per_big_block;

        for (; count < full_count; count++)
        {
            blocks.add(makeEmptySmallDocumentBlock());
        }
        return big_block_count;
    }

    /**
     * Factory for creating SmallDocumentBlocks from DocumentBlocks
     *
     * @param store the original DocumentBlocks
     * @param size the total document size
     *
     * @return an array of new SmallDocumentBlocks instances
     *
     * @exception IOException on errors reading from the DocumentBlocks
     * @exception ArrayIndexOutOfBoundsException if, somehow, the store
     *            contains less data than size indicates
     */

    public static SmallDocumentBlock [] convert(final BlockWritable [] store,
                                                final int size)
        throws IOException, ArrayIndexOutOfBoundsException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        for (int j = 0; j < store.length; j++)
        {
            store[ j ].writeBlocks(stream);
        }
        byte[]               data = stream.toByteArray();
        SmallDocumentBlock[] rval =
            new SmallDocumentBlock[ convertToBlockCount(size) ];

        for (int index = 0; index < rval.length; index++)
        {
            rval[ index ] = new SmallDocumentBlock(data, index);
        }
        return rval;
    }

    /**
     * create a list of SmallDocumentBlock's from raw data
     *
     * @param blocks the raw data containing the SmallDocumentBlock
     *               data
     *
     * @return a List of SmallDocumentBlock's extracted from the input
     *
     * @exception IOException
     */

    public static List extract(ListManagedBlock [] blocks)
        throws IOException
    {
        List sdbs = new ArrayList();

        for (int j = 0; j < blocks.length; j++)
        {
            byte[] data = blocks[ j ].getData();

            for (int k = 0; k < _blocks_per_big_block; k++)
            {
                sdbs.add(new SmallDocumentBlock(data, k));
            }
        }
        return sdbs;
    }

    /**
     * read data from an array of SmallDocumentBlocks
     *
     * @param blocks the blocks to read from
     * @param buffer the buffer to write the data into
     * @param offset the offset into the array of blocks to read from
     */

    public static void read(final BlockWritable [] blocks,
                            final byte [] buffer, final int offset)
    {
        int firstBlockIndex  = offset / _block_size;
        int firstBlockOffset = offset % _block_size;
        int lastBlockIndex   = (offset + buffer.length - 1) / _block_size;

        if (firstBlockIndex == lastBlockIndex)
        {
            System.arraycopy(
                (( SmallDocumentBlock ) blocks[ firstBlockIndex ])._data,
                firstBlockOffset, buffer, 0, buffer.length);
        }
        else
        {
            int buffer_offset = 0;

            System.arraycopy(
                (( SmallDocumentBlock ) blocks[ firstBlockIndex ])._data,
                firstBlockOffset, buffer, buffer_offset,
                _block_size - firstBlockOffset);
            buffer_offset += _block_size - firstBlockOffset;
            for (int j = firstBlockIndex + 1; j < lastBlockIndex; j++)
            {
                System.arraycopy((( SmallDocumentBlock ) blocks[ j ])._data,
                                 0, buffer, buffer_offset, _block_size);
                buffer_offset += _block_size;
            }
            System.arraycopy(
                (( SmallDocumentBlock ) blocks[ lastBlockIndex ])._data, 0,
                buffer, buffer_offset, buffer.length - buffer_offset);
        }
    }

    /**
     * Calculate the storage size of a set of SmallDocumentBlocks
     *
     * @param size number of SmallDocumentBlocks
     *
     * @return total size
     */

    public static int calcSize(int size)
    {
        return size * _block_size;
    }

    private static SmallDocumentBlock makeEmptySmallDocumentBlock()
    {
        SmallDocumentBlock block = new SmallDocumentBlock();

        Arrays.fill(block._data, _default_fill);
        return block;
    }

    private static int convertToBlockCount(final int size)
    {
        return (size + _block_size - 1) / _block_size;
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
        stream.write(_data);
    }

    /* **********  END  implementation of BlockWritable ********** */
    /* ********** START implementation of ListManagedBlock ********** */

    /**
     * Get the data from the block
     *
     * @return the block's data as a byte array
     *
     * @exception IOException if there is no data
     */

    public byte [] getData()
        throws IOException
    {
        return _data;
    }

    /* **********  END  implementation of ListManagedBlock ********** */
}   // end public class SmallDocumentBlock

