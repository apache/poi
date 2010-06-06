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

package org.apache.poi.hssf.eventusermodel;

import java.io.InputStream;
import java.io.IOException;

import org.apache.poi.hssf.eventusermodel.HSSFUserException;
import org.apache.poi.hssf.record.*;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Low level event based HSSF reader.  Pass either a DocumentInputStream to
 * process events along with a request object or pass a POIFS POIFSFileSystem to
 * processWorkbookEvents along with a request.
 *
 * This will cause your file to be processed a record at a time.  Each record with
 * a static id matching one that you have registered in your HSSFRequest will be passed
 * to your associated HSSFListener.
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Carey Sublette  (careysub@earthling.net)
 */
public class HSSFEventFactory {
	/** Creates a new instance of HSSFEventFactory */
	public HSSFEventFactory() {
		// no instance fields
	}

	/**
	 * Processes a file into essentially record events.
	 *
	 * @param req an Instance of HSSFRequest which has your registered listeners
	 * @param fs  a POIFS filesystem containing your workbook
	 */
	public void processWorkbookEvents(HSSFRequest req, POIFSFileSystem fs) throws IOException {
	   processWorkbookEvents(req, fs.getRoot());
	}

   /**
    * Processes a file into essentially record events.
    *
    * @param req an Instance of HSSFRequest which has your registered listeners
    * @param dir  a DirectoryNode containing your workbook
    */
   public void processWorkbookEvents(HSSFRequest req, DirectoryNode dir) throws IOException {
      InputStream in = dir.createDocumentInputStream("Workbook");

      processEvents(req, in);
   }

   /**
    * Processes a file into essentially record events.
    *
    * @param req an Instance of HSSFRequest which has your registered listeners
    * @param fs  a POIFS filesystem containing your workbook
    * @return    numeric user-specified result code.
    */
   public short abortableProcessWorkbookEvents(HSSFRequest req, POIFSFileSystem fs)
      throws IOException, HSSFUserException {
      return abortableProcessWorkbookEvents(req, fs.getRoot());
   }

	/**
	 * Processes a file into essentially record events.
	 *
	 * @param req an Instance of HSSFRequest which has your registered listeners
	 * @param dir  a DirectoryNode containing your workbook
	 * @return    numeric user-specified result code.
	 */
	public short abortableProcessWorkbookEvents(HSSFRequest req, DirectoryNode dir)
		throws IOException, HSSFUserException {
		InputStream in = dir.createDocumentInputStream("Workbook");
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
	 * @param req an Instance of HSSFRequest which has your registered listeners
	 * @param in  a DocumentInputStream obtained from POIFS's POIFSFileSystem object
	 */
	public void processEvents(HSSFRequest req, InputStream in) {
		try {
			genericProcessEvents(req, in);
		} catch (HSSFUserException hue) {
			/*If an HSSFUserException user exception is thrown, ignore it.*/
		}
	}


	/**
	 * Processes a DocumentInputStream into essentially Record events.
	 *
	 * @see org.apache.poi.poifs.filesystem.POIFSFileSystem#createDocumentInputStream(String)
	 * @param req an Instance of HSSFRequest which has your registered listeners
	 * @param in  a DocumentInputStream obtained from POIFS's POIFSFileSystem object
	 * @return    numeric user-specified result code.
	 */
	public short abortableProcessEvents(HSSFRequest req, InputStream in)
		throws HSSFUserException {
		return genericProcessEvents(req, in);
	}

	/**
	 * Processes a DocumentInputStream into essentially Record events.
	 *
	 * @see org.apache.poi.poifs.filesystem.POIFSFileSystem#createDocumentInputStream(String)
	 * @param req an Instance of HSSFRequest which has your registered listeners
	 * @param in  a DocumentInputStream obtained from POIFS's POIFSFileSystem object
	 * @return    numeric user-specified result code.
	 */
	private short genericProcessEvents(HSSFRequest req, InputStream in)
		throws HSSFUserException {
		short userCode = 0;

		// Create a new RecordStream and use that
		RecordFactoryInputStream recordStream = new RecordFactoryInputStream(in, false);

		// Process each record as they come in
		while(true) {
			Record r = recordStream.nextRecord();
			if(r == null) {
				break;
			}
			userCode = req.processRecord(r);
			if (userCode != 0) {
				break;
			}
		}

		// All done, return our last code
		return userCode;
	}
}
