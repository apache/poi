
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

import java.io.*;

import java.util.*;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.IOUtils;
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
        int byte_count = IOUtils.readFully(stream, _data);

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

