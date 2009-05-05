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

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.eventusermodel.AbortableHSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class TestAbortableListener extends TestCase {
    protected HSSFITestDataProvider getTestDataProvider(){
        return HSSFITestDataProvider.getInstance();
    }
    
	public void testAbortingBasics() throws Exception {
		AbortableCountingListener l = new AbortableCountingListener(1000);
		
		HSSFRequest req = new HSSFRequest();
		req.addListenerForAllRecords(l);
		
		HSSFEventFactory f = new HSSFEventFactory();
		
		assertEquals(0, l.seen);
		assertEquals(null, l.lastseen);
		
		POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(
				getTestDataProvider().getTestDataFileContent("SimpleWithColours.xls")
		));
		short res = f.abortableProcessWorkbookEvents(req, fs);
		
		assertEquals(0, res);
		assertEquals(175, l.seen);
		assertEquals(EOFRecord.sid, l.lastseen.getSid());
	}
	
	public void testAbortStops() throws Exception {
		AbortableCountingListener l = new AbortableCountingListener(1);
		
		HSSFRequest req = new HSSFRequest();
		req.addListenerForAllRecords(l);
		
		HSSFEventFactory f = new HSSFEventFactory();
		
		assertEquals(0, l.seen);
		assertEquals(null, l.lastseen);
		
		POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(
				getTestDataProvider().getTestDataFileContent("SimpleWithColours.xls")
		));
		short res = f.abortableProcessWorkbookEvents(req, fs);
		
		assertEquals(1234, res);
		assertEquals(1, l.seen);
		assertEquals(BOFRecord.sid, l.lastseen.getSid());
	}
	
	public static class AbortableCountingListener extends AbortableHSSFListener {
		private int abortAfter;
		private int seen;
		private Record lastseen;
		
		public AbortableCountingListener(int abortAfter) {
			this.abortAfter = abortAfter;
			this.seen = 0;
			this.lastseen = null;
		}
		public short abortableProcessRecord(Record record) {
			seen++;
			lastseen = record;
			
			if(seen == abortAfter)
				return 1234;
			return 0;
		}
	}
}
