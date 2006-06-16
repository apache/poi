
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.poifs.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Arrays;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.IOUtils;
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
        int count = IOUtils.readFully(stream, _data);

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

