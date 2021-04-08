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

package org.apache.poi.hssf.record.aggregates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.record.ColumnInfoRecord;
import org.apache.poi.hssf.record.RecordBase;
import org.junit.jupiter.api.Test;

final class TestColumnInfoRecordsAggregate {

	@Test
    void testGetRecordSize() {
		ColumnInfoRecordsAggregate agg = new ColumnInfoRecordsAggregate();
		agg.insertColumn(createColInfo(1, 3));
		agg.insertColumn(createColInfo(4, 7));
		agg.insertColumn(createColInfo(8, 8));
		agg.groupColumnRange((short) 2, (short) 5, true);
		assertEquals(4, agg.getNumColumns());

		confirmSerializedSize(agg);

		agg = new ColumnInfoRecordsAggregate();
		agg.groupColumnRange((short) 3, (short) 6, true);
		confirmSerializedSize(agg);
	}

	private static void confirmSerializedSize(RecordBase cirAgg) {
		int estimatedSize = cirAgg.getRecordSize();
		byte[] buf = new byte[estimatedSize];
		int serializedSize = cirAgg.serialize(0, buf);
		assertEquals(estimatedSize, serializedSize);
	}

	private static ColumnInfoRecord createColInfo(int firstCol, int lastCol) {
		ColumnInfoRecord columnInfoRecord = new ColumnInfoRecord();
		columnInfoRecord.setFirstColumn((short) firstCol);
		columnInfoRecord.setLastColumn((short) lastCol);
		return columnInfoRecord;
	}

	@Test
	void testGroupColumns_bug45639() {
		ColumnInfoRecordsAggregate agg = new ColumnInfoRecordsAggregate();
		agg.groupColumnRange( 7, 9, true);
		agg.groupColumnRange( 4, 12, true);

		// bug 45639 - ArrayIndexOutOfBoundsException
		agg.groupColumnRange( 1, 15, true);

		List<org.apache.poi.hssf.record.Record> cirs = new ArrayList<>();
		agg.visitContainedRecords(cirs::add);

		assertEquals(5, cirs.size());
		confirmCIR(cirs, 0,  1,  3, 1, false, false);
		confirmCIR(cirs, 1,  4,  6, 2, false, false);
		confirmCIR(cirs, 2,  7,  9, 3, false, false);
		confirmCIR(cirs, 3, 10, 12, 2, false, false);
		confirmCIR(cirs, 4, 13, 15, 1, false, false);
	}

	/**
	 * Check that an inner group remains hidden
	 */
	@Test
	void testHiddenAfterExpanding() {
		ColumnInfoRecordsAggregate agg = new ColumnInfoRecordsAggregate();
		agg.groupColumnRange(1, 15, true);
		agg.groupColumnRange(4, 12, true);

		List<org.apache.poi.hssf.record.Record> cirs = new ArrayList<>();

		// collapse both inner and outer groups
		agg.collapseColumn(6);
		agg.collapseColumn(3);

		agg.visitContainedRecords(cirs::add);
		assertEquals(5, cirs.size());
		confirmCIR(cirs, 0,  1,  3, 1, true, false);
		confirmCIR(cirs, 1,  4, 12, 2, true, false);
		confirmCIR(cirs, 2, 13, 13, 1, true, true);
		confirmCIR(cirs, 3, 14, 15, 1, true, false);
		confirmCIR(cirs, 4, 16, 16, 0, false, true);

		// just expand the inner group
		agg.expandColumn(6);

		cirs.clear();
		agg.visitContainedRecords(cirs::add);
		assertEquals(4, cirs.size());
		assertTrue(((ColumnInfoRecord)cirs.get(1)).getHidden(), "Inner group should still be hidden");
		confirmCIR(cirs, 0,  1,  3, 1, true, false);
		confirmCIR(cirs, 1,  4, 12, 2, true, false);
		confirmCIR(cirs, 2, 13, 15, 1, true, false);
		confirmCIR(cirs, 3, 16, 16, 0, false, true);
	}

	private static void confirmCIR(List<org.apache.poi.hssf.record.Record> cirs, int ix, int startColIx, int endColIx, int level, boolean isHidden, boolean isCollapsed) {
		assertTrue(cirs.get(ix) instanceof ColumnInfoRecord);
		ColumnInfoRecord cir = (ColumnInfoRecord)cirs.get(ix);
		assertEquals(startColIx, cir.getFirstColumn(), "startColIx");
		assertEquals(endColIx, cir.getLastColumn(), "endColIx");
		assertEquals(level, cir.getOutlineLevel(), "level");
		assertEquals(isHidden, cir.getHidden(), "hidden");
		assertEquals(isCollapsed, cir.getCollapsed(), "collapsed");
	}
}
