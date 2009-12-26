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
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.hssf.record.DVALRecord;
import org.apache.poi.hssf.record.DVRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.FeatHdrRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SelectionRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
/**
 * 
 */
public final class TestHSSFEventFactory extends TestCase {
	
	private static final InputStream openSample(String sampleFileName) {
		return HSSFTestDataSamples.openSampleFileStream(sampleFileName);
	}

	public void testWithMissingRecords() throws Exception {

		HSSFRequest req = new HSSFRequest();
		MockHSSFListener mockListen = new MockHSSFListener();
		req.addListenerForAllRecords(mockListen);
		
		POIFSFileSystem fs = new POIFSFileSystem(openSample("SimpleWithSkip.xls"));
		HSSFEventFactory factory = new HSSFEventFactory();
		factory.processWorkbookEvents(req, fs);

		Record[] recs = mockListen.getRecords();
		// Check we got the records
		assertTrue( recs.length > 100 );
		
		// Check that the last few records are as we expect
		// (Makes sure we don't accidently skip the end ones)
		int numRec = recs.length;
		assertEquals(WindowTwoRecord.class, recs[numRec-3].getClass());
		assertEquals(SelectionRecord.class, recs[numRec-2].getClass());
		assertEquals(EOFRecord.class,	   recs[numRec-1].getClass());
	}

	public void testWithCrazyContinueRecords() throws Exception {
		// Some files have crazy ordering of their continue records
		// Check that we don't break on them (bug #42844)
		
		HSSFRequest req = new HSSFRequest();
		MockHSSFListener mockListen = new MockHSSFListener();
		req.addListenerForAllRecords(mockListen);
		
		POIFSFileSystem fs = new POIFSFileSystem(openSample("ContinueRecordProblem.xls"));
		HSSFEventFactory factory = new HSSFEventFactory();
		factory.processWorkbookEvents(req, fs);

		Record[] recs = mockListen.getRecords();
		// Check we got the records
		assertTrue( recs.length > 100 );

		// And none of them are continue ones
		for(int i=0; i<recs.length; i++) {
			assertFalse( recs[i] instanceof ContinueRecord );
		}
		
		// Check that the last few records are as we expect
		// (Makes sure we don't accidently skip the end ones)
		int numRec = recs.length;
		assertEquals(DVALRecord.class,    recs[numRec-4].getClass());
		assertEquals(DVRecord.class,      recs[numRec-3].getClass());
		assertEquals(FeatHdrRecord.class, recs[numRec-2].getClass());
		assertEquals(EOFRecord.class,     recs[numRec-1].getClass());
	}

	/**
	 * Unknown records can be continued.
	 * Check that HSSFEventFactory doesn't break on them.
	 * (the test file was provided in a reopen of bug #42844)
	 */
	public void testUnknownContinueRecords() throws Exception {

		HSSFRequest req = new HSSFRequest();
		MockHSSFListener mockListen = new MockHSSFListener();
		req.addListenerForAllRecords(mockListen);

		POIFSFileSystem fs = new POIFSFileSystem(openSample("42844.xls"));
		HSSFEventFactory factory = new HSSFEventFactory();
		factory.processWorkbookEvents(req, fs);

		assertTrue("no errors while processing the file", true);
	}

	private static class MockHSSFListener implements HSSFListener {
		private final List<Record> records = new ArrayList<Record>();

		public MockHSSFListener() {}
		public Record[] getRecords() {
			Record[] result = new Record[records.size()];
			records.toArray(result);
			return result;
		}

		public void processRecord(Record record) {
			records.add(record);
		}
	}
}	
