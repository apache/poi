
/* ====================================================================
   Copyright 2003-2004   Apache Software Foundation

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

package org.apache.poi.hssf.eventusermodel;

import java.io.InputStream;
import java.io.IOException;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hssf.eventusermodel.HSSFUserException;
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
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Carey Sublette  (careysub@earthling.net)
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
	 * Processes a file into essentially record events.
	 *
	 * @param req       an Instance of HSSFRequest which has your registered listeners
	 * @param fs        a POIFS filesystem containing your workbook
	 * @return 			numeric user-specified result code.
	 */

	public short abortableProcessWorkbookEvents(HSSFRequest req, POIFSFileSystem fs)
		throws IOException, HSSFUserException
	{
		InputStream in = fs.createDocumentInputStream("Workbook");
		return abortableProcessEvents(req, in);
    }

    /**
     * Processes a DocumentInputStream into essentially Record events.
     *
     * If an <code>AbortableHSSFListener</code> causes a halt to processing during this call
     * the method will return just as with <code>abortableProcessEvents</code>, but no
     * user code or <code>HSSFUserException</code> will be passed back.
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
			genericProcessEvents(req, in);
		}
		catch (HSSFUserException hue)
		{/*If an HSSFUserException user exception is thrown, ignore it.*/ }
	}


    /**
     * Processes a DocumentInputStream into essentially Record events.
     *
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem#createDocumentInputStream(String)
     * @param req       an Instance of HSSFRequest which has your registered listeners
     * @param in        a DocumentInputStream obtained from POIFS's POIFSFileSystem object
	 * @return 			numeric user-specified result code.
     */

    public short abortableProcessEvents(HSSFRequest req, InputStream in)
        throws IOException, HSSFUserException
    {
		return genericProcessEvents(req, in);
    }

     /**
	 * Processes a DocumentInputStream into essentially Record events.
	 *
	 * @see org.apache.poi.poifs.filesystem.POIFSFileSystem#createDocumentInputStream(String)
	 * @param req       an Instance of HSSFRequest which has your registered listeners
	 * @param in        a DocumentInputStream obtained from POIFS's POIFSFileSystem object
	 * @param in        a DocumentInputStream obtained from POIFS's POIFSFileSystem object
	 * @return 			numeric user-specified result code.
	 */

	protected short genericProcessEvents(HSSFRequest req, InputStream in)
		throws IOException, HSSFUserException
	{
		short userCode = 0;

		short sid = 0;
		process:
		try
		{
			byte[] sidbytes  = new byte[ 2 ];
			int    bytesread = in.read(sidbytes);
			Record rec       = null;

			while (bytesread > 0)
			{

				sid = LittleEndian.getShort(sidbytes);
                
                //
                // for some reasons we have to make the workbook to be at least 4096 bytes
                // but if we have such workbook we fill the end of it with zeros (many zeros)
                //
                // it is not good:
                // if the length( all zero records ) % 4 = 1
                // e.g.: any zero record would be readed as  4 bytes at once ( 2 - id and 2 - size ).
                // And the last 1 byte will be readed WRONG ( the id must be 2 bytes )
                //
                // So we should better to check if the sid is zero and not to read more data
                // The zero sid shows us that rest of the stream data is a fake to make workbook 
                // certain size
                //
                if ( sid == 0 )
                    break;


				if ((rec != null) && (sid != ContinueRecord.sid))
				{
					userCode = req.processRecord(rec);
					if (userCode != 0) break process;
				}
				if (sid != ContinueRecord.sid)
				{
					short  size = LittleEndian.readShort(in);
					byte[] data = new byte[ size ];

					if (data.length > 0)
					{
						in.read(data);
					}
                                        //System.out.println("creating "+sid);
					Record[] recs = RecordFactory.createRecord(sid, size,
															   data);

					if (recs.length > 1)
					{                                // we know that the multiple
						for (int k = 0; k < (recs.length - 1); k++)
						{                            // record situations do not
							userCode = req.processRecord(
								recs[ k ]);          // contain continue records
							if (userCode != 0) break process;
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
				userCode = req.processRecord(rec);
				if (userCode != 0) break process;
			}
		}
		catch (IOException e)
		{
			throw new RecordFormatException("Error reading bytes" +
                        "while processing record sid="+sid);
		}
		return userCode;

		// Record[] retval = new Record[ records.size() ];
		// retval = ( Record [] ) records.toArray(retval);
		// return null;
    }
}
