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

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordFactory;

/**
 * An HSSFRequest object should be constructed registering an instance or multiple
 * instances of HSSFListener with each Record.sid you wish to listen for.
 *
 * @see org.apache.poi.hssf.eventusermodel.HSSFEventFactory
 * @see org.apache.poi.hssf.eventusermodel.HSSFListener
 * @see org.apache.poi.hssf.dev.EFHSSF
 * @see org.apache.poi.hssf.eventusermodel.HSSFUserException
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author Carey Sublette (careysub@earthling.net)
 */
public class HSSFRequest {
	private final Map<Short, List<HSSFListener>> _records;

	/** Creates a new instance of HSSFRequest */
	public HSSFRequest() {
		_records = new HashMap<Short, List<HSSFListener>>(50); // most folks won't listen for too many of these
	}

	/**
	 * add an event listener for a particular record type.  The trick is you have to know
	 * what the records are for or just start with our examples and build on them.  Alternatively,
	 * you CAN call addListenerForAllRecords and you'll receive ALL record events in one listener,
	 * but if you like to squeeze every last byte of efficiency out of life you my not like this.
	 * (its sure as heck what I plan to do)
	 *
	 * @see #addListenerForAllRecords(HSSFListener)
	 *
	 * @param lsnr for the event
	 * @param sid identifier for the record type this is the .sid static member on the individual records
	 *        for example req.addListener(myListener, BOFRecord.sid)
	 */
	public void addListener(HSSFListener lsnr, short sid) {
		List<HSSFListener> list = _records.get(Short.valueOf(sid));

		if (list == null) {
			list = new ArrayList<HSSFListener>(1); // probably most people will use one listener
			_records.put(Short.valueOf(sid), list);
		}
		list.add(lsnr);
	}

	/**
	 * This is the equivalent of calling addListener(myListener, sid) for EVERY
	 * record in the org.apache.poi.hssf.record package. This is for lazy
	 * people like me. You can call this more than once with more than one listener, but
	 * that seems like a bad thing to do from a practice-perspective unless you have a
	 * compelling reason to do so (like maybe you send the event two places or log it or
	 * something?).
	 *
	 * @param lsnr a single listener to associate with ALL records
	 */
	public void addListenerForAllRecords(HSSFListener lsnr) {
		short[] rectypes = RecordFactory.getAllKnownRecordSIDs();

		for (int k = 0; k < rectypes.length; k++) {
			addListener(lsnr, rectypes[k]);
		}
	}

	/**
	 * Called by HSSFEventFactory, passes the Record to each listener associated with
	 * a record.sid.
	 *
	 * Exception and return value added 2002-04-19 by Carey Sublette
	 *
	 * @return numeric user-specified result code. If zero continue processing.
	 * @throws HSSFUserException User exception condition
	 */
	protected short processRecord(Record rec) throws HSSFUserException {
		Object obj = _records.get(Short.valueOf(rec.getSid()));
		short userCode = 0;

		if (obj != null) {
			List listeners = (List) obj;

			for (int k = 0; k < listeners.size(); k++) {
				Object listenObj = listeners.get(k);
				if (listenObj instanceof AbortableHSSFListener) {
					AbortableHSSFListener listener = (AbortableHSSFListener) listenObj;
					userCode = listener.abortableProcessRecord(rec);
					if (userCode != 0)
						break;
				} else {
					HSSFListener listener = (HSSFListener) listenObj;
					listener.processRecord(rec);
				}
			}
		}
		return userCode;
	}
}
