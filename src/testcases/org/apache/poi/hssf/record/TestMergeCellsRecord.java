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

package org.apache.poi.hssf.record;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.record.aggregates.MergedCellsTable;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Test;


/**
 * Make sure the merge cells record behaves
 */
public final class TestMergeCellsRecord {

	/**
	 * Make sure when a clone is called, we actually clone it.
	 */
	@Test
	public void testCloneReferences() {
		CellRangeAddress[] cras = { new CellRangeAddress(0, 1, 0, 2), };
		MergeCellsRecord merge = new MergeCellsRecord(cras, 0, cras.length);
		MergeCellsRecord clone = merge.copy();

		assertNotSame(merge, clone, "Merged and cloned objects are the same");

		CellRangeAddress mergeRegion = merge.getAreaAt(0);
		CellRangeAddress cloneRegion = clone.getAreaAt(0);
		assertNotSame(mergeRegion, cloneRegion, "Should not point to same objects when cloning");
		assertEquals(mergeRegion.getFirstRow(), cloneRegion.getFirstRow(), "New Clone Row From doesnt match");
		assertEquals(mergeRegion.getLastRow(), cloneRegion.getLastRow(), "New Clone Row To doesnt match");
		assertEquals(mergeRegion.getFirstColumn(), cloneRegion.getFirstColumn(), "New Clone Col From doesnt match");
		assertEquals(mergeRegion.getLastColumn(), cloneRegion.getLastColumn(), "New Clone Col To doesnt match");

        assertNotSame(merge.getAreaAt(0), clone.getAreaAt(0));
	}

	@Test
	public void testMCTable_bug46009() {
		MergedCellsTable mct = new MergedCellsTable();
		List<org.apache.poi.hssf.record.Record> recList = new ArrayList<>();
		CellRangeAddress[] cras = new CellRangeAddress[] {
				new CellRangeAddress(0, 0, 0, 3),
		};
		recList.add(new MergeCellsRecord(cras, 0, 1));
		RecordStream rs = new RecordStream(recList, 0);
		mct.read(rs);
		mct.visitContainedRecords(r -> {});
	}
}
