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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.eventusermodel.AbortableHSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Tests for {@link AbortableHSSFListener}
 */
public final class TestAbortableListener extends TestCase {

	private POIFSFileSystem openSample() {
		ByteArrayInputStream is = new ByteArrayInputStream(HSSFITestDataProvider.instance
				.getTestDataFileContent("SimpleWithColours.xls"));
		try {
			return new POIFSFileSystem(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void testAbortingBasics() throws Exception {
		AbortableCountingListener l = new AbortableCountingListener(1000);

		HSSFRequest req = new HSSFRequest();
		req.addListenerForAllRecords(l);

		HSSFEventFactory f = new HSSFEventFactory();

		assertEquals(0, l.countSeen);
		assertEquals(null, l.lastRecordSeen);

		POIFSFileSystem fs = openSample();
		short res = f.abortableProcessWorkbookEvents(req, fs);

		assertEquals(0, res);
		assertEquals(175, l.countSeen);
		assertEquals(EOFRecord.sid, l.lastRecordSeen.getSid());
	}


	public void testAbortStops() throws Exception {
		AbortableCountingListener l = new AbortableCountingListener(1);

		HSSFRequest req = new HSSFRequest();
		req.addListenerForAllRecords(l);

		HSSFEventFactory f = new HSSFEventFactory();

		assertEquals(0, l.countSeen);
		assertEquals(null, l.lastRecordSeen);

		POIFSFileSystem fs = openSample();
		short res = f.abortableProcessWorkbookEvents(req, fs);

		assertEquals(1234, res);
		assertEquals(1, l.countSeen);
		assertEquals(BOFRecord.sid, l.lastRecordSeen.getSid());
	}

	private static final class AbortableCountingListener extends AbortableHSSFListener {
		private int abortAfterIndex;
		public int countSeen;
		public Record lastRecordSeen;

		public AbortableCountingListener(int abortAfter) {
			abortAfterIndex = abortAfter;
			countSeen = 0;
			lastRecordSeen = null;
		}
		public short abortableProcessRecord(Record record) {
			countSeen++;
			lastRecordSeen = record;

			if(countSeen == abortAfterIndex) {
				return 1234;
			}
			return 0;
		}
	}
}
