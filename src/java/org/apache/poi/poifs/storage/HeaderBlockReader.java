
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
import org.apache.poi.util.IntegerField;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LongField;
import org.apache.poi.util.ShortField;

/**
 * The block containing the archive header
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class HeaderBlockReader
    implements HeaderBlockConstants
{

    // number of big block allocation table blocks (int)
    private IntegerField _bat_count;

    // start of the property set block (int index of the property set
    // chain's first big block)
    private IntegerField _property_start;

    // start of the small block allocation table (int index of small
    // block allocation table's first big block)
    private IntegerField _sbat_start;

    // big block index for extension to the big block allocation table
    private IntegerField _xbat_start;
    private IntegerField _xbat_count;
    private byte[]       _data;

    /**
     * create a new HeaderBlockReader from an InputStream
     *
     * @param stream the source InputStream
     *
     * @exception IOException on errors or bad data
     */

    public HeaderBlockReader(final InputStream stream)
        throws IOException
    {
        _data = new byte[ POIFSConstants.BIG_BLOCK_SIZE ];
        int byte_count = stream.read(_data);

        if (byte_count != POIFSConstants.BIG_BLOCK_SIZE)
        {
            String type = " byte" + ((byte_count == 1) ? ("")
                                                       : ("s"));

            throw new IOException("Unable to read entire header; "
                                  + byte_count + type + " read; expected "
                                  + POIFSConstants.BIG_BLOCK_SIZE + " bytes");
        }

        // verify signature
        LongField signature = new LongField(_signature_offset, _data);

        if (signature.get() != _signature)
        {
            throw new IOException("Invalid header signature; read "
                                  + signature.get() + ", expected "
                                  + _signature);
        }
        _bat_count      = new IntegerField(_bat_count_offset, _data);
        _property_start = new IntegerField(_property_start_offset, _data);
        _sbat_start     = new IntegerField(_sbat_start_offset, _data);
        _xbat_start     = new IntegerField(_xbat_start_offset, _data);
        _xbat_count     = new IntegerField(_xbat_count_offset, _data);
    }

    /**
     * get start of Property Table
     *
     * @return the index of the first block of the Property Table
     */

    public int getPropertyStart()
    {
        return _property_start.get();
    }

    /**
     * @return start of small block allocation table
     */

    public int getSBATStart()
    {
        return _sbat_start.get();
    }

    /**
     * @return number of BAT blocks
     */

    public int getBATCount()
    {
        return _bat_count.get();
    }

    /**
     * @return BAT array
     */

    public int [] getBATArray()
    {
        int[] result = new int[ _max_bats_in_header ];
        int   offset = _bat_array_offset;

        for (int j = 0; j < _max_bats_in_header; j++)
        {
            result[ j ] = LittleEndian.getInt(_data, offset);
            offset      += LittleEndianConsts.INT_SIZE;
        }
        return result;
    }

    /**
     * @return XBAT count
     */

    public int getXBATCount()
    {
        return _xbat_count.get();
    }

    /**
     * @return XBAT index
     */

    public int getXBATIndex()
    {
        return _xbat_start.get();
    }
}   // end public class HeaderBlockReader

