
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

import org.apache.poi.poifs.common.POIFSConstants;

import java.io.*;

/**
 * A big block created from an InputStream, holding the raw data
 *
 * @author Marc Johnson (mjohnson at apache dot org
 */

public class RawDataBlock
    implements ListManagedBlock
{
    private byte[]  _data;
    private boolean _eof;

    /**
     * Constructor RawDataBlock
     *
     * @param stream the InputStream from which the data will be read
     *
     * @exception IOException on I/O errors, and if an insufficient
     *            amount of data is read
     */

    public RawDataBlock(final InputStream stream)
        throws IOException
    {
        _data = new byte[ POIFSConstants.BIG_BLOCK_SIZE ];
        int count = stream.read(_data);

        if (count == -1)
        {
            _eof = true;
        }
        else if (count != POIFSConstants.BIG_BLOCK_SIZE)
        {
            String type = " byte" + ((count == 1) ? ("")
                                                  : ("s"));

            throw new IOException("Unable to read entire block; " + count
                                  + type + " read; expected "
                                  + POIFSConstants.BIG_BLOCK_SIZE + " bytes");
        }
        else
        {
            _eof = false;
        }
    }

    /**
     * When we read the data, did we hit end of file?
     *
     * @return true if no data was read because we were at the end of
     *         the file, else false
     *
     * @exception IOException
     */

    public boolean eof()
        throws IOException
    {
        return _eof;
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
        if (eof())
        {
            throw new IOException("Cannot return empty data");
        }
        return _data;
    }

    /* **********  END  implementation of ListManagedBlock ********** */
}   // end public class RawDataBlock

