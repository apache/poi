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

package org.apache.poi.hssf.model;

import java.util.Arrays;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.UnknownRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.hssf.record.pivottable.ViewDefinitionRecord;

/**
 * Tests for {@link RowBlocksReader}
 *
 * @author Josh Micich
 */
public final class TestRowBlocksReader extends TestCase {
	public void testAbnormalPivotTableRecords_bug46280() {
		int SXVIEW_SID = ViewDefinitionRecord.sid;
		Record[] inRecs = {
			new RowRecord(0),
			new NumberRecord(),
			// normally MSODRAWING(0x00EC) would come here before SXVIEW
			new UnknownRecord(SXVIEW_SID, "dummydata (SXVIEW: View Definition)".getBytes()),
			new WindowTwoRecord(),
		};
		RecordStream rs = new RecordStream(Arrays.asList(inRecs), 0);
		RowBlocksReader rbr = new RowBlocksReader(rs);
		if (rs.peekNextClass() == WindowTwoRecord.class) {
			// Should have stopped at the SXVIEW record
			throw new AssertionFailedError("Identified bug 46280b");
		}
		RecordStream rbStream = rbr.getPlainRecordStream();
		assertEquals(inRecs[0], rbStream.getNext());
		assertEquals(inRecs[1], rbStream.getNext());
		assertFalse(rbStream.hasNext());
		assertTrue(rs.hasNext());
		assertEquals(inRecs[2], rs.getNext());
		assertEquals(inRecs[3], rs.getNext());
	}
}
