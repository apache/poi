
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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

import java.io.*;

import java.util.*;

/**
 * Class LocalRawDataBlockList
 *
 * @author Marc Johnson(mjohnson at apache dot org)
 */

public class LocalRawDataBlockList
    extends RawDataBlockList
{
    private List           _list;
    private RawDataBlock[] _array;

    /**
     * Constructor LocalRawDataBlockList
     *
     * @exception IOException
     */

    public LocalRawDataBlockList()
        throws IOException
    {
        super(new ByteArrayInputStream(new byte[ 0 ]));
        _list  = new ArrayList();
        _array = null;
    }

    /**
     * create and a new XBAT block
     *
     * @param start index of first BAT block
     * @param end index of last BAT block
     * @param chain index of next XBAT block
     *
     * @exception IOException
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
     *
     * @exception IOException
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
     *
     * @exception IOException
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
     *
     * @exception IOException
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
            _array = ( RawDataBlock [] ) _list.toArray(new RawDataBlock[ 0 ]);
        }
    }
}
