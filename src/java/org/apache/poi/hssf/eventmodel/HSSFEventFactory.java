
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

package org.apache.poi.hssf.eventmodel;

import java.io.InputStream;
import java.io.IOException;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hssf.record.RecordFormatException;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordFactory;
import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Low level event based HSSF reader.  Pass either a DocumentInputStream to
 * process events along with a request object or pass a POIFS POIFSFileSystem to
 * processWorkbookEvents along with a request.
 *
 * This will cause your file to be processed a record at a time.  Each record with
 * a static id matching one that you have registed in your HSSFRequest will be passed
 * to your associated HSSFListener.
 *
 * @see org.apache.poi.hssf.dev.EFHSSF
 *
 * @author  andy
 */

public class HSSFEventFactory
{

    /** Creates a new instance of HSSFEventFactory */

    public HSSFEventFactory()
    {
    }

    /**
     * Processes a file into essentially record events.
     *
     * @param req       an Instance of HSSFRequest which has your registered listeners
     * @param fs        a POIFS filesystem containing your workbook
     */

    public void processWorkbookEvents(HSSFRequest req, POIFSFileSystem fs)
        throws IOException
    {
        InputStream in = fs.createDocumentInputStream("Workbook");

        processEvents(req, in);
    }

    /**
     * Processes a DocumentInputStream into essentially Record events.
     *
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem#createDocumentInputStream(String)
     * @param req       an Instance of HSSFRequest which has your registered listeners
     * @param in        a DocumentInputStream obtained from POIFS's POIFSFileSystem object
     */

    public void processEvents(HSSFRequest req, InputStream in)
        throws IOException
    {
        try
        {
            byte[] sidbytes  = new byte[ 2 ];
            int    bytesread = in.read(sidbytes);
            Record rec       = null;

            while (bytesread > 0)
            {
                short sid = 0;

                sid = LittleEndian.getShort(sidbytes);
                if ((rec != null) && (sid != ContinueRecord.sid))
                {
                    req.processRecord(rec);
                }
                if (sid != ContinueRecord.sid)
                {
                    short  size = LittleEndian.readShort(in);
                    byte[] data = new byte[ size ];

                    if (data.length > 0)
                    {
                        in.read(data);
                    }
                    Record[] recs = RecordFactory.createRecord(sid, size,
                                                               data);

                    if (recs.length > 1)
                    {                                // we know that the multiple
                        for (int k = 0; k < (recs.length - 1); k++)
                        {                            // record situations do not
                            req.processRecord(
                                recs[ k ]);          // contain continue records
                        }
                    }
                    rec = recs[ recs.length - 1 ];   // regardless we'll process

                    // the last record as though
                    // it might be continued
                    // if there is only one
                    // records, it will go here too.
                }
                else
                {                                    // we do have a continue record
                    short  size = LittleEndian.readShort(in);
                    byte[] data = new byte[ size ];

                    if (data.length > 0)
                    {
                        in.read(data);
                    }
                    rec.processContinueRecord(data);
                }
                bytesread = in.read(sidbytes);       // read next record sid
            }
            if (rec != null)
            {
                req.processRecord(rec);
            }
        }
        catch (IOException e)
        {
            throw new RecordFormatException("Error reading bytes");
        }

        // Record[] retval = new Record[ records.size() ];
        // retval = ( Record [] ) records.toArray(retval);
        // return null;
    }
}
