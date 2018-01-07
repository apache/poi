
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

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

import java.io.*;

/**
 * A big block created from an InputStream, holding the raw data
 *
 * @author Marc Johnson (mjohnson at apache dot org
 */

public class RawDataBlock
    implements ListManagedBlock
{
    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    private byte[]  _data;
    private boolean _eof;
    private boolean _hasData;
    static POILogger log = POILogFactory.getLogger(RawDataBlock.class);

    /**
     * Constructor RawDataBlock
     *
     * @param stream the InputStream from which the data will be read
     *
     * @exception IOException on I/O errors, and if an insufficient
     *            amount of data is read (the InputStream must
     *            be an exact multiple of the block size)
     */
    public RawDataBlock(final InputStream stream)
    		throws IOException {
    	this(stream, POIFSConstants.SMALLER_BIG_BLOCK_SIZE);
    }
    /**
     * Constructor RawDataBlock
     *
     * @param stream the InputStream from which the data will be read
     * @param blockSize the size of the POIFS blocks, normally 512 bytes
     * {@link org.apache.poi.poifs.common.POIFSConstants#SMALLER_BIG_BLOCK_SIZE}
     *
     * @exception IOException on I/O errors, and if an insufficient
     *            amount of data is read (the InputStream must
     *            be an exact multiple of the block size)
     */
    public RawDataBlock(final InputStream stream, int blockSize)
    		throws IOException {
        _data = IOUtils.safelyAllocate(blockSize, MAX_RECORD_LENGTH);
        int count = IOUtils.readFully(stream, _data);
        _hasData = (count > 0);

        if (count == -1) {
            _eof = true;
        }
        else if (count != blockSize) {
        	// IOUtils.readFully will always read the
        	//  requested number of bytes, unless it hits
        	//  an EOF
            _eof = true;
            String type = " byte" + ((count == 1) ? ("")
                                                  : ("s"));

            log.log(POILogger.ERROR,
            		"Unable to read entire block; " + count
                     + type + " read before EOF; expected "
                     + blockSize + " bytes. Your document "
                     + "was either written by software that "
                     + "ignores the spec, or has been truncated!"
            );
        }
        else {
            _eof = false;
        }
    }

    /**
     * When we read the data, did we hit end of file?
     *
     * @return true if the EoF was hit during this block, or
     *  false if not. If you have a dodgy short last block, then
     *  it's possible to both have data, and also hit EoF...
     */
    public boolean eof() {
        return _eof;
    }
    /**
     * Did we actually find any data to read? It's possible,
     *  in the event of a short last block, to both have hit
     *  the EoF, but also to have data
     */
    public boolean hasData() {
    	return _hasData;
    }
    
    public String toString() {
       return "RawDataBlock of size " + _data.length; 
    }

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
        if (! hasData())
        {
            throw new IOException("Cannot return empty data");
        }
        return _data;
    }
    
    /**
     * What's the big block size?
     */
    public int getBigBlockSize() {
       return _data.length;
    }

    /* **********  END  implementation of ListManagedBlock ********** */
}   // end public class RawDataBlock

