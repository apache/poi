
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
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Arrays;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.IntegerField;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * A block of document data.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class DocumentBlock
    extends BigBlock
{
    private static final byte _default_value = ( byte ) 0xFF;
    private byte[]            _data;
    private int               _bytes_read;

    /**
     * create a document block from a raw data block
     *
     * @param block the raw data block
     *
     * @exception IOException
     */

    public DocumentBlock(final RawDataBlock block)
        throws IOException
    {
        _data       = block.getData();
        _bytes_read = _data.length;
    }

    /**
     * Create a single instance initialized with data.
     *
     * @param stream the InputStream delivering the data.
     *
     * @exception IOException
     */

    public DocumentBlock(final InputStream stream)
        throws IOException
    {
        this();
        int count = stream.read(_data);

        _bytes_read = (count == -1) ? 0
                                    : count;
    }

    /**
     * Create a single instance initialized with default values
     */

    private DocumentBlock()
    {
        _data = new byte[ POIFSConstants.BIG_BLOCK_SIZE ];
        Arrays.fill(_data, _default_value);
    }

    /**
     * Get the number of bytes read for this block
     *
     * @return bytes read into the block
     */

    public int size()
    {
        return _bytes_read;
    }

    /**
     * Was this a partially read block?
     *
     * @return true if the block was only partially filled with data
     */

    public boolean partiallyRead()
    {
        return _bytes_read != POIFSConstants.BIG_BLOCK_SIZE;
    }

    /**
     * @return the fill byte used
     */

    public static byte getFillByte()
    {
        return _default_value;
    }

    /**
     * convert a single long array into an array of DocumentBlock
     * instances
     *
     * @param array the byte array to be converted
     * @param size the intended size of the array (which may be smaller)
     *
     * @return an array of DocumentBlock instances, filled from the
     *         input array
     */

    public static DocumentBlock [] convert(final byte [] array,
                                           final int size)
    {
        DocumentBlock[] rval   =
            new DocumentBlock[ (size + POIFSConstants.BIG_BLOCK_SIZE - 1) / POIFSConstants.BIG_BLOCK_SIZE ];
        int             offset = 0;

        for (int k = 0; k < rval.length; k++)
        {
            rval[ k ] = new DocumentBlock();
            if (offset < array.length)
            {
                int length = Math.min(POIFSConstants.BIG_BLOCK_SIZE,
                                      array.length - offset);

                System.arraycopy(array, offset, rval[ k ]._data, 0, length);
                if (length != POIFSConstants.BIG_BLOCK_SIZE)
                {
                    Arrays.fill(rval[ k ]._data, length,
                                POIFSConstants.BIG_BLOCK_SIZE,
                                _default_value);
                }
            }
            else
            {
                Arrays.fill(rval[ k ]._data, _default_value);
            }
            offset += POIFSConstants.BIG_BLOCK_SIZE;
        }
        return rval;
    }

    /**
     * read data from an array of DocumentBlocks
     *
     * @param blocks the blocks to read from
     * @param buffer the buffer to write the data into
     * @param offset the offset into the array of blocks to read from
     */

    public static void read(final DocumentBlock [] blocks,
                            final byte [] buffer, final int offset)
    {
        int firstBlockIndex  = offset / POIFSConstants.BIG_BLOCK_SIZE;
        int firstBlockOffset = offset % POIFSConstants.BIG_BLOCK_SIZE;
        int lastBlockIndex   = (offset + buffer.length - 1)
                               / POIFSConstants.BIG_BLOCK_SIZE;

        if (firstBlockIndex == lastBlockIndex)
        {
            System.arraycopy(blocks[ firstBlockIndex ]._data,
                             firstBlockOffset, buffer, 0, buffer.length);
        }
        else
        {
            int buffer_offset = 0;

            System.arraycopy(blocks[ firstBlockIndex ]._data,
                             firstBlockOffset, buffer, buffer_offset,
                             POIFSConstants.BIG_BLOCK_SIZE
                             - firstBlockOffset);
            buffer_offset += POIFSConstants.BIG_BLOCK_SIZE - firstBlockOffset;
            for (int j = firstBlockIndex + 1; j < lastBlockIndex; j++)
            {
                System.arraycopy(blocks[ j ]._data, 0, buffer, buffer_offset,
                                 POIFSConstants.BIG_BLOCK_SIZE);
                buffer_offset += POIFSConstants.BIG_BLOCK_SIZE;
            }
            System.arraycopy(blocks[ lastBlockIndex ]._data, 0, buffer,
                             buffer_offset, buffer.length - buffer_offset);
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
}   // end public class DocumentBlock

