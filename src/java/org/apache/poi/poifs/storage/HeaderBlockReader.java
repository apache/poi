
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

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.IntegerField;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LongField;

/**
 * The block containing the archive header
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class HeaderBlockReader
    implements HeaderBlockConstants
{
    /**
     * What big block size the file uses. Most files
     *  use 512 bytes, but a few use 4096
     */
    private int bigBlockSize = POIFSConstants.BIG_BLOCK_SIZE;

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
    	// At this point, we don't know how big our
    	//  block sizes are
    	// So, read the first 32 bytes to check, then
    	//  read the rest of the block
    	byte[] blockStart = new byte[32];
    	int bsCount = IOUtils.readFully(stream, blockStart);
    	if(bsCount != 32) {
    		alertShortRead(bsCount);
    	}
    	
    	// Figure out our block size
    	if(blockStart[30] == 12) {
    		bigBlockSize = POIFSConstants.LARGER_BIG_BLOCK_SIZE;
    	}
        _data = new byte[ bigBlockSize ];
        System.arraycopy(blockStart, 0, _data, 0, blockStart.length);
    	
    	// Now we can read the rest of our header
        int byte_count = IOUtils.readFully(stream, _data, blockStart.length, _data.length - blockStart.length);
        if (byte_count+bsCount != bigBlockSize) {
    		alertShortRead(byte_count);
        }

        // verify signature
        LongField signature = new LongField(_signature_offset, _data);

        if (signature.get() != _signature)
        {
			// Is it one of the usual suspects?
        	byte[] OOXML_FILE_HEADER = POIFSConstants.OOXML_FILE_HEADER;
			if(_data[0] == OOXML_FILE_HEADER[0] && 
					_data[1] == OOXML_FILE_HEADER[1] && 
					_data[2] == OOXML_FILE_HEADER[2] &&
					_data[3] == OOXML_FILE_HEADER[3]) {
				throw new OfficeXmlFileException("The supplied data appears to be in the Office 2007+ XML. You are calling the part of POI that deals with OLE2 Office Documents. You need to call a different part of POI to process this data (eg XSSF instead of HSSF)");
			}

			// Give a generic error
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
    
    private void alertShortRead(int read) throws IOException {
    	if (read == -1)
    		//Cant have -1 bytes read in the error message!
    		read = 0;
        String type = " byte" + ((read == 1) ? ("")
                                                   : ("s"));

        throw new IOException("Unable to read entire header; "
                              + read + type + " read; expected "
                              + bigBlockSize + " bytes");
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
    
    /**
     * @return The Big Block size, normally 512 bytes, sometimes 4096 bytes
     */
    public int getBigBlockSize() {
    	return bigBlockSize;
    }
}   // end public class HeaderBlockReader

