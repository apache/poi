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

package org.apache.poi.hslf.usermodel;


import junit.framework.TestCase;
import org.apache.poi.hslf.*;
import org.apache.poi.hslf.record.ParentAwareRecord;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordContainer;
import org.apache.poi.POIDataSamples;

/**
 * Tests that the record setup done by SlideShow
 *  has worked correctly
 * Note: most recent record stuff has its own test
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestRecordSetup extends TestCase {
	// SlideShow primed on the test data
	private SlideShow ss;
	private HSLFSlideShow hss;

	public TestRecordSetup() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
		hss = new HSLFSlideShow(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
		ss = new SlideShow(hss);
	}

	public void testHandleParentAwareRecords() {
		Record[] records = hss.getRecords();
		for(int i=0; i<records.length; i++) {
			ensureParentAware(records[i],null);
		}
	}
	private void ensureParentAware(Record r,RecordContainer parent) {
		if(r instanceof ParentAwareRecord) {
			ParentAwareRecord pr = (ParentAwareRecord)r;
			assertEquals(parent, pr.getParentRecord());
		}
		if(r instanceof RecordContainer) {
			RecordContainer rc = (RecordContainer)r;
			Record[] children = rc.getChildRecords();
			for(int i=0; i<children.length; i++) {
				ensureParentAware(children[i], rc);
			}
		}
	}
}
