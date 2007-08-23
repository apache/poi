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

import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.ContinueRecord;
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
		assertTrue( mockListen.records.size() > 100 );
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
		assertTrue( mockListen.records.size() > 100 );

		// And none of them are continue ones
		Record[] r = (Record[])mockListen.records.toArray( 
							new Record[mockListen.records.size()] );
		for(int i=0; i<r.length; i++) {
			assertFalse( r[i] instanceof ContinueRecord );
		}
	}


	private static class MockHSSFListener implements HSSFListener {
		private MockHSSFListener() {}
		private ArrayList records = new ArrayList();

		public void processRecord(Record record) {
			records.add(record);
		}
	}
}	
