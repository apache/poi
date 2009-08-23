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

package org.apache.poi.hslf.record;


import junit.framework.TestCase;

import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.POIDataSamples;

/**
 * Tests that the helper methods on RecordContainer work properly
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestRecordContainer extends TestCase {
	private RecordContainer recordContainer;

	public void testIsAnAtom() {
		assertFalse( recordContainer.isAnAtom() );
	}

	public void testAppendChildRecord() {
		// Grab records for testing with
		Record r = recordContainer.getChildRecords()[0];
		Record rb = recordContainer.getChildRecords()[1];
		Record rc = recordContainer.getChildRecords()[2];
		Record rd = recordContainer.getChildRecords()[3];

		// Start with an empty set
		Record[] rs = new Record[0];
		recordContainer._children = rs;
		recordContainer.appendChildRecord(r);
		Record[] nrs = recordContainer.getChildRecords();

		assertEquals(1, nrs.length);
		assertEquals(r, nrs[0]);

		// Now start with one with 3 entries
		rs = new Record[3];
		recordContainer._children = rs;
		rs[0] = rb;
		rs[1] = rc;
		rs[2] = rd;

		recordContainer.appendChildRecord(r);
		nrs = recordContainer.getChildRecords();

		assertEquals(4, nrs.length);
		assertEquals(rb, nrs[0]);
		assertEquals(rc, nrs[1]);
		assertEquals(rd, nrs[2]);
		assertEquals(r, nrs[3]);
	}

	public void testAddChildAfter() {
		// Working with new StyleTextPropAtom
		Record newRecord = new StyleTextPropAtom(0);

		// Try to add after a mid-record
		Record[] cr = recordContainer.getChildRecords();
		Record after = cr[2];
		Record before = cr[3];

		recordContainer.addChildAfter(newRecord, after);
		Record[] ncr = recordContainer.getChildRecords();

		assertEquals(cr.length+1, ncr.length);
		assertEquals(after, ncr[2]);
		assertEquals(newRecord, ncr[3]);
		assertEquals(before, ncr[4]);

		// Try again at the end
		recordContainer._children = cr;
		after = cr[cr.length-1];

		recordContainer.addChildAfter(newRecord, after);
		ncr = recordContainer.getChildRecords();

		assertEquals(cr.length+1, ncr.length);
		assertEquals(after, ncr[cr.length-1]);
		assertEquals(newRecord, ncr[cr.length]);
	}

	public void testAddChildBefore() {
		// Working with new StyleTextPropAtom
		Record newRecord = new StyleTextPropAtom(0);

		// Try to add before a mid-record
		Record[] cr = recordContainer.getChildRecords();
		Record before = cr[2];

		recordContainer.addChildBefore(newRecord, before);
		Record[] ncr = recordContainer.getChildRecords();

		assertEquals(cr.length+1, ncr.length);
		assertEquals(newRecord, ncr[2]);
		assertEquals(before, ncr[3]);


		// Try again at the end
		recordContainer._children = cr;
		before = cr[cr.length-1];

		recordContainer.addChildBefore(newRecord, before);
		ncr = recordContainer.getChildRecords();

		assertEquals(cr.length+1, ncr.length);
		assertEquals(newRecord, ncr[cr.length-1]);
		assertEquals(before, ncr[cr.length]);


		// And at the start
		recordContainer._children = cr;
		before = cr[0];

		recordContainer.addChildBefore(newRecord, before);
		ncr = recordContainer.getChildRecords();

		assertEquals(cr.length+1, ncr.length);
		assertEquals(newRecord, ncr[0]);
		assertEquals(before, ncr[1]);
	}

    public void testRemove() {
        Record[] ch = recordContainer.getChildRecords();
        Record removeRecord = recordContainer.removeChild(ch[0]);
        assertSame(ch[0], removeRecord);
        assertEquals(ch.length-1, recordContainer.getChildRecords().length);
    }

    protected void setUp() throws Exception {
		super.setUp();

		// Find a real RecordContainer record
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
		HSLFSlideShow hss = new HSLFSlideShow(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));

		Record[] r = hss.getRecords();
		for(int i=0; i<r.length; i++) {
			if(r[i] instanceof RecordContainer) {
				recordContainer = (RecordContainer)r[i];
				return;
			}
		}
	}
}
