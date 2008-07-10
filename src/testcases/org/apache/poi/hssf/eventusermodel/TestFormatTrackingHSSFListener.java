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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
/**
 * Tests for FormatTrackingHSSFListener
 */
public final class TestFormatTrackingHSSFListener extends TestCase {
	private FormatTrackingHSSFListener listener;
	private MockHSSFListener mockListen;

	private void processFile(String filename) throws Exception {
		HSSFRequest req = new HSSFRequest();
		mockListen = new MockHSSFListener();
		listener = new FormatTrackingHSSFListener(mockListen);
		req.addListenerForAllRecords(listener);
		
		HSSFEventFactory factory = new HSSFEventFactory();
		try {
			InputStream is = HSSFTestDataSamples.openSampleFileStream(filename);
			POIFSFileSystem fs = new POIFSFileSystem(is);
			factory.processWorkbookEvents(req, fs);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	} 
	
	public void testFormats() throws Exception {
		processFile("MissingBits.xls");
		
		assertEquals("_(*#,##0_);_(*(#,##0);_(* \"-\"_);_(@_)", listener.getFormatString(41));
		assertEquals("_($*#,##0_);_($*(#,##0);_($* \"-\"_);_(@_)", listener.getFormatString(42));
		assertEquals("_(*#,##0.00_);_(*(#,##0.00);_(*\"-\"??_);_(@_)", listener.getFormatString(43));
	}
	
	/**
	 * Ensure that all number and formula records can be
	 *  turned into strings without problems
	 */
	public void testTurnToString() throws Exception {
		processFile("45365.xls");
		
		for(int i=0; i<mockListen._records.size(); i++) {
			Record r = (Record)mockListen._records.get(i);
			CellValueRecordInterface cvr = null;
			
			if(r instanceof NumberRecord) {
				cvr = (CellValueRecordInterface)r;
			}
			if(r instanceof FormulaRecord) {
				cvr = (CellValueRecordInterface)r;
			}
			
			if(cvr != null) {
				// Should always give us a string 
				String s = listener.formatNumberDateCell(cvr);
				assertNotNull(s);
				assertTrue(s.length() > 0);
			}
		}
		
		// TODO - test some specific format strings
	}
	
	private static final class MockHSSFListener implements HSSFListener {
		public MockHSSFListener() {}
		private final List _records = new ArrayList();

		public void processRecord(Record record) {
			_records.add(record);
		}
	}
}