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

package org.apache.poi.hssf.eventmodel;

import java.io.InputStream;
import java.util.Arrays;

import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordFactory;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.util.RecordFormatException;

/**
 * Event-based record factory.  As opposed to RecordFactory
 * this version sends {@link ERFListener#processRecord(Record) } messages to
 * the supplied listener.  Record notifications are sent one record behind
 * to ensure that {@link ContinueRecord}s are processed first.
 */
public final class EventRecordFactory {

	private final ERFListener _listener;
	private final short[] _sids;

	/**
	 * Create an EventRecordFactory
	 *
	 * @param listener the listener to be informed about events
	 * @param sids an array of Record.sid values identifying the records
	 * the listener will work with.  Alternatively if this is "null" then
	 * all records are passed. For all 'known' record types use {@link RecordFactory#getAllKnownRecordSIDs()}
	 */
	public EventRecordFactory(ERFListener listener, short[] sids) {
		_listener = listener;
		if (sids == null) {
			_sids = null;
		} else {
			_sids = sids.clone();
			Arrays.sort(_sids); // for faster binary search
		}
	}
	private boolean isSidIncluded(short sid) {
		if (_sids == null) {
			return true;
		}
		return Arrays.binarySearch(_sids, sid) >= 0;
	}


	/**
	 * sends the record event to all registered listeners.
	 * @param record the record to be thrown.
	 * @return <code>false</code> to abort.  This aborts
	 * out of the event loop should the listener return false
	 */
	private boolean processRecord(Record record) {
		if (!isSidIncluded(record.getSid())) {
			return true;
		}
		return _listener.processRecord(record);
	}

	/**
	 * Create an array of records from an input stream
	 *
	 * @param in the InputStream from which the records will be
	 *		   obtained
	 *
	 * @exception RecordFormatException on error processing the
	 *			InputStream
	 */
	public void processRecords(InputStream in) throws RecordFormatException {
		Record last_record = null;

		RecordInputStream recStream = new RecordInputStream(in);

		while (recStream.hasNextRecord()) {
			recStream.nextRecord();
			Record[] recs = RecordFactory.createRecord(recStream);   // handle MulRK records
			if (recs.length > 1) {
				for (Record rec : recs) {
					if ( last_record != null ) {
						if (!processRecord(last_record)) {
							return;
						}
					}
					last_record = rec; // do to keep the algorithm homogeneous...you can't
				}							// actually continue a number record anyhow.
			} else {
				Record record = recs[ 0 ];

				if (record != null) {
					if (last_record != null) {
						if (!processRecord(last_record)) {
							return;
						}
					}
					 last_record = record;
				}
			}
		}

		if (last_record != null) {
			processRecord(last_record);
		}
	}
}
