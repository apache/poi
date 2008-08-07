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

import junit.framework.TestCase;
import org.apache.poi.hssf.record.ColumnInfoRecord;
import org.apache.poi.hssf.record.RecordBase;

/**
 * @author Glen Stampoultzis
 */
public final class TestColumnInfoRecordsAggregate extends TestCase {

	public void testGetRecordSize() {
		ColumnInfoRecordsAggregate agg = new ColumnInfoRecordsAggregate();
		agg.insertColumn(createColumn(1, 3));
		agg.insertColumn(createColumn(4, 7));
		agg.insertColumn(createColumn(8, 8));
		agg.groupColumnRange((short) 2, (short) 5, true);
		assertEquals(6, agg.getNumColumns());

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

	private static ColumnInfoRecord createColumn(int firstCol, int lastCol) {
		ColumnInfoRecord columnInfoRecord = new ColumnInfoRecord();
		columnInfoRecord.setFirstColumn((short) firstCol);
		columnInfoRecord.setLastColumn((short) lastCol);
		return columnInfoRecord;
	}
}