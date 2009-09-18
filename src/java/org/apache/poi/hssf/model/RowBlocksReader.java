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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.record.ArrayRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.MergeCellsRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SharedFormulaRecord;
import org.apache.poi.hssf.record.TableRecord;
import org.apache.poi.hssf.record.aggregates.MergedCellsTable;
import org.apache.poi.hssf.record.aggregates.SharedValueManager;
import org.apache.poi.ss.util.CellReference;

/**
 * Segregates the 'Row Blocks' section of a single sheet into plain row/cell records and
 * shared formula records.
 *
 * @author Josh Micich
 */
public final class RowBlocksReader {

	private final List _plainRecords;
	private final SharedValueManager _sfm;
	private final MergeCellsRecord[] _mergedCellsRecords;

	/**
	 * Also collects any loose MergeCellRecords and puts them in the supplied
	 * mergedCellsTable
	 */
	public RowBlocksReader(RecordStream rs) {
		List<Record> plainRecords = new ArrayList<Record>();
		List<Record> shFrmRecords = new ArrayList<Record>();
		List<CellReference> firstCellRefs = new ArrayList<CellReference>();
		List<Record> arrayRecords = new ArrayList<Record>();
		List<Record> tableRecords = new ArrayList<Record>();
		List<Record> mergeCellRecords = new ArrayList<Record>();

		Record prevRec = null;
		while(!RecordOrderer.isEndOfRowBlock(rs.peekNextSid())) {
			// End of row/cell records for the current sheet
			// Note - It is important that this code does not inadvertently add any sheet
			// records from a subsequent sheet.  For example, if SharedFormulaRecords
			// are taken from the wrong sheet, this could cause bug 44449.
			if (!rs.hasNext()) {
				throw new RuntimeException("Failed to find end of row/cell records");

			}
			Record rec = rs.getNext();
			List<Record> dest;
			switch (rec.getSid()) {
				case MergeCellsRecord.sid:    dest = mergeCellRecords; break;
				case SharedFormulaRecord.sid: dest = shFrmRecords;
					if (!(prevRec instanceof FormulaRecord)) {
						throw new RuntimeException("Shared formula record should follow a FormulaRecord");
					}
					FormulaRecord fr = (FormulaRecord)prevRec;
					firstCellRefs.add(new CellReference(fr.getRow(), fr.getColumn()));
					break;
				case ArrayRecord.sid:         dest = arrayRecords;     break;
				case TableRecord.sid:         dest = tableRecords;     break;
				default:                      dest = plainRecords;
			}
			dest.add(rec);
			prevRec = rec;
		}
		SharedFormulaRecord[] sharedFormulaRecs = new SharedFormulaRecord[shFrmRecords.size()];
		CellReference[] firstCells = new CellReference[firstCellRefs.size()];
		ArrayRecord[] arrayRecs = new ArrayRecord[arrayRecords.size()];
		TableRecord[] tableRecs = new TableRecord[tableRecords.size()];
		shFrmRecords.toArray(sharedFormulaRecs);
		firstCellRefs.toArray(firstCells);
		arrayRecords.toArray(arrayRecs);
		tableRecords.toArray(tableRecs);

		_plainRecords = plainRecords;
		_sfm = SharedValueManager.create(sharedFormulaRecs, firstCells, arrayRecs, tableRecs);
		_mergedCellsRecords = new MergeCellsRecord[mergeCellRecords.size()];
		mergeCellRecords.toArray(_mergedCellsRecords);
	}

	/**
	 * Some unconventional apps place {@link MergeCellsRecord}s within the row block.  They
	 * actually should be in the {@link MergedCellsTable} which is much later (see bug 45699).
	 * @return any loose  <tt>MergeCellsRecord</tt>s found
	 */
	public MergeCellsRecord[] getLooseMergedCells() {
		return _mergedCellsRecords;
	}

	public SharedValueManager getSharedFormulaManager() {
		return _sfm;
	}
	/**
	 * @return a {@link RecordStream} containing all the non-{@link SharedFormulaRecord}
	 * non-{@link ArrayRecord} and non-{@link TableRecord} Records.
	 */
	public RecordStream getPlainRecordStream() {
		return new RecordStream(_plainRecords, 0);
	}
}
