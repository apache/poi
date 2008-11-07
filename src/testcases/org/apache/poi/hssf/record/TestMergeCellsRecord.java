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

import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.record.aggregates.MergedCellsTable;
import org.apache.poi.hssf.record.aggregates.RecordAggregate.RecordVisitor;
import org.apache.poi.ss.util.CellRangeAddress;


/**
 * Make sure the merge cells record behaves
 * @author Danny Mui (dmui at apache dot org)
 *
 */
public final class TestMergeCellsRecord extends TestCase {
   
	/**
	 * Make sure when a clone is called, we actually clone it.
	 */
	public void testCloneReferences() {
		CellRangeAddress[] cras = { new CellRangeAddress(0, 1, 0, 2), };
		MergeCellsRecord merge = new MergeCellsRecord(cras, 0, cras.length);
		MergeCellsRecord clone = (MergeCellsRecord)merge.clone();

		assertNotSame("Merged and cloned objects are the same", merge, clone);

		CellRangeAddress mergeRegion = merge.getAreaAt(0);
		CellRangeAddress cloneRegion = clone.getAreaAt(0);
		assertNotSame("Should not point to same objects when cloning", mergeRegion, cloneRegion);
		assertEquals("New Clone Row From doesnt match", mergeRegion.getFirstRow(), cloneRegion.getFirstRow());
		assertEquals("New Clone Row To doesnt match", mergeRegion.getLastRow(), cloneRegion.getLastRow());
		assertEquals("New Clone Col From doesnt match", mergeRegion.getFirstColumn(), cloneRegion.getFirstColumn());
		assertEquals("New Clone Col To doesnt match", mergeRegion.getLastColumn(), cloneRegion.getLastColumn());      
      
		assertFalse(merge.getAreaAt(0) == clone.getAreaAt(0));
	}
   
	private static final RecordVisitor dummyRecordVisitor = new RecordVisitor() {
		public void visitRecord(Record r) {
			// do nothing
		}
	};
	public void testMCTable_bug46009() {
		MergedCellsTable mct = new MergedCellsTable();
		List recList = new ArrayList();
		CellRangeAddress[] cras = new CellRangeAddress[] {
				new CellRangeAddress(0, 0, 0, 3), 
		};
		recList.add(new MergeCellsRecord(cras, 0, 1));
		RecordStream rs = new RecordStream(recList, 0);
		mct.read(rs);
		try {
			mct.visitContainedRecords(dummyRecordVisitor);
		} catch (ArrayStoreException e) {
			throw new AssertionFailedError("Identified bug 46009");
		}
	}
}
