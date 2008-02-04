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
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.apache.poi.hssf.record.DVALRecord;
import org.apache.poi.hssf.record.DVRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.hssf.record.SelectionRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import junit.framework.TestCase;

public class TestHSSFEventFactory extends TestCase {
	private String dirname;
	
	public TestHSSFEventFactory() {
		dirname = System.getProperty("HSSF.testdata.path");
	}

	public void testWithMissingRecords() throws Exception {
		File f = new File(dirname + "/SimpleWithSkip.xls");

		HSSFRequest req = new HSSFRequest();
		MockHSSFListener mockListen = new MockHSSFListener();
		req.addListenerForAllRecords(mockListen);
		
		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(f));
		HSSFEventFactory factory = new HSSFEventFactory();
		factory.processWorkbookEvents(req, fs);

		// Check we got the records
		System.out.println("Processed, found " + mockListen.records.size() + " records");
		assertTrue( mockListen.records.size() > 100 );
		
		// Check that the last few records are as we expect
		// (Makes sure we don't accidently skip the end ones)
		int numRec = mockListen.records.size();
		assertEquals(WindowTwoRecord.class, mockListen.records.get(numRec-3).getClass());
		assertEquals(SelectionRecord.class, mockListen.records.get(numRec-2).getClass());
		assertEquals(EOFRecord.class,       mockListen.records.get(numRec-1).getClass());
	}

	public void testWithCrazyContinueRecords() throws Exception {
		// Some files have crazy ordering of their continue records
		// Check that we don't break on them (bug #42844)

		File f = new File(dirname + "/ContinueRecordProblem.xls");
		
		HSSFRequest req = new HSSFRequest();
		MockHSSFListener mockListen = new MockHSSFListener();
		req.addListenerForAllRecords(mockListen);
		
		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(f));
		HSSFEventFactory factory = new HSSFEventFactory();
		factory.processWorkbookEvents(req, fs);

		// Check we got the records
		System.out.println("Processed, found " + mockListen.records.size() + " records");
		assertTrue( mockListen.records.size() > 100 );

		// And none of them are continue ones
		Record[] r = (Record[])mockListen.records.toArray( 
							new Record[mockListen.records.size()] );
		for(int i=0; i<r.length; i++) {
			assertFalse( r[i] instanceof ContinueRecord );
		}
		
		// Check that the last few records are as we expect
		// (Makes sure we don't accidently skip the end ones)
		int numRec = mockListen.records.size();
		assertEquals(DVALRecord.class, mockListen.records.get(numRec-3).getClass());
		assertEquals(DVRecord.class, mockListen.records.get(numRec-2).getClass());
		assertEquals(EOFRecord.class,       mockListen.records.get(numRec-1).getClass());
	}

    /**
     * Unknown records can be continued.
     * Check that HSSFEventFactory doesn't break on them.
     * (the test file was provided in a reopen of bug #42844)
     */
    public void testUnknownContinueRecords() throws Exception {
         File f = new File(dirname + "/42844.xls");

        HSSFRequest req = new HSSFRequest();
        MockHSSFListener mockListen = new MockHSSFListener();
        req.addListenerForAllRecords(mockListen);

        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(f));
        HSSFEventFactory factory = new HSSFEventFactory();
        factory.processWorkbookEvents(req, fs);

        assertTrue("no errors while processing the file", true);
    }

	private static class MockHSSFListener implements HSSFListener {
		private MockHSSFListener() {}
		private ArrayList records = new ArrayList();

		public void processRecord(Record record) {
			records.add(record);
		}
	}
}	
