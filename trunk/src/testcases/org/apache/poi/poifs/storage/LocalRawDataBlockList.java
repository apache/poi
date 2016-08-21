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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * Class LocalRawDataBlockList
 *
 * @author Marc Johnson(mjohnson at apache dot org)
 */
public final class LocalRawDataBlockList extends RawDataBlockList {
    private List<RawDataBlock> _list;
    private RawDataBlock[] _array;

    public LocalRawDataBlockList()
        throws IOException
    {
        super(new ByteArrayInputStream(new byte[ 0 ]), POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS);
        _list  = new ArrayList<RawDataBlock>();
        _array = null;
    }

    /**
     * create and a new XBAT block
     *
     * @param start index of first BAT block
     * @param end index of last BAT block
     * @param chain index of next XBAT block
     */
    public void createNewXBATBlock(final int start, final int end,
                                   final int chain)
        throws IOException
    {
        byte[] data   = new byte[ 512 ];
        int    offset = 0;

        for (int k = start; k <= end; k++)
        {
            LittleEndian.putInt(data, offset, k);
            offset += LittleEndianConsts.INT_SIZE;
        }
        while (offset != 508)
        {
            LittleEndian.putInt(data, offset, -1);
            offset += LittleEndianConsts.INT_SIZE;
        }
        LittleEndian.putInt(data, offset, chain);
        add(new RawDataBlock(new ByteArrayInputStream(data)));
    }

    /**
     * create a BAT block and add it to the list
     *
     * @param start_index initial index for the block list
     */
    public void createNewBATBlock(final int start_index)
        throws IOException
    {
        byte[] data   = new byte[ 512 ];
        int    offset = 0;

        for (int j = 0; j < 128; j++)
        {
            int index = start_index + j;

            if (index % 256 == 0)
            {
                LittleEndian.putInt(data, offset, -1);
            }
            else if (index % 256 == 255)
            {
                LittleEndian.putInt(data, offset, -2);
            }
            else
            {
                LittleEndian.putInt(data, offset, index + 1);
            }
            offset += LittleEndianConsts.INT_SIZE;
        }
        add(new RawDataBlock(new ByteArrayInputStream(data)));
    }

    /**
     * fill the list with dummy blocks
     *
     * @param count of blocks
     */
    public void fill(final int count)
        throws IOException
    {
        int limit = 128 * count;

        for (int j = _list.size(); j < limit; j++)
        {
            add(new RawDataBlock(new ByteArrayInputStream(new byte[ 0 ])));
        }
    }

    /**
     * add a new block
     *
     * @param block new block to add
     */
    public void add(RawDataBlock block)
    {
        _list.add(block);
    }

    /**
     * override of remove method
     *
     * @param index of block to be removed
     *
     * @return desired block
     */
    public ListManagedBlock remove(final int index)
        throws IOException
    {
        ensureArrayExists();
        RawDataBlock rvalue = null;

        try
        {
            rvalue = _array[ index ];
            if (rvalue == null)
            {
                throw new IOException("index " + index + " is null");
            }
            _array[ index ] = null;
        }
        catch (ArrayIndexOutOfBoundsException ignored)
        {
            throw new IOException("Cannot remove block[ " + index
                                  + " ]; out of range");
        }
        return rvalue;
    }

    /**
     * remove the specified block from the list
     *
     * @param index the index of the specified block; if the index is
     *              out of range, that's ok
     */
    public void zap(final int index)
    {
        ensureArrayExists();
        if ((index >= 0) && (index < _array.length))
        {
            _array[ index ] = null;
        }
    }

    private void ensureArrayExists()
    {
        if (_array == null)
        {
            _array = _list.toArray(new RawDataBlock[ 0 ]);
        }
    }
    
    public int blockCount() {
       return _list.size();
    }
}
