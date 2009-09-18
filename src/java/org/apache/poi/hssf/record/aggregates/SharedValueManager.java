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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.record.ArrayRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.SharedFormulaRecord;
import org.apache.poi.hssf.record.SharedValueRecordBase;
import org.apache.poi.hssf.record.TableRecord;
import org.apache.poi.hssf.record.formula.ExpPtg;
import org.apache.poi.hssf.util.CellRangeAddress8Bit;
import org.apache.poi.ss.util.CellReference;

/**
 * Manages various auxiliary records while constructing a
 * {@link RowRecordsAggregate}:
 * <ul>
 * <li>{@link SharedFormulaRecord}s</li>
 * <li>{@link ArrayRecord}s</li>
 * <li>{@link TableRecord}s</li>
 * </ul>
 *
 * @author Josh Micich
 */
public final class SharedValueManager {

	private static final class SharedFormulaGroup {
		private final SharedFormulaRecord _sfr;
		private final FormulaRecordAggregate[] _frAggs;
		private int _numberOfFormulas;
		/**
		 * Coordinates of the first cell having a formula that uses this shared formula.
		 * This is often <i>but not always</i> the top left cell in the range covered by
		 * {@link #_sfr}
		 */
		private final CellReference _firstCell;

		public SharedFormulaGroup(SharedFormulaRecord sfr, CellReference firstCell) {
			if (!sfr.isInRange(firstCell.getRow(), firstCell.getCol())) {
				throw new IllegalArgumentException("First formula cell " + firstCell.formatAsString()
						+ " is not shared formula range " + sfr.getRange().toString() + ".");
			}
			_sfr = sfr;
			_firstCell = firstCell;
			int width = sfr.getLastColumn() - sfr.getFirstColumn() + 1;
			int height = sfr.getLastRow() - sfr.getFirstRow() + 1;
			_frAggs = new FormulaRecordAggregate[width * height];
			_numberOfFormulas = 0;
		}

		public void add(FormulaRecordAggregate agg) {
			if (_numberOfFormulas == 0) {
				if (_firstCell.getRow() != agg.getRow() || _firstCell.getCol() != agg.getColumn()) {
					throw new IllegalStateException("shared formula coding error");
				}
			}
			if (_numberOfFormulas >= _frAggs.length) {
				throw new RuntimeException("Too many formula records for shared formula group");
			}
			_frAggs[_numberOfFormulas++] = agg;
		}

		public void unlinkSharedFormulas() {
			for (int i = 0; i < _numberOfFormulas; i++) {
				_frAggs[i].unlinkSharedFormula();
			}
		}

		public SharedFormulaRecord getSFR() {
			return _sfr;
		}

		public final String toString() {
			StringBuffer sb = new StringBuffer(64);
			sb.append(getClass().getName()).append(" [");
			sb.append(_sfr.getRange().toString());
			sb.append("]");
			return sb.toString();
		}

		/**
		 * Note - the 'first cell' of a shared formula group is not always the top-left cell
		 * of the enclosing range.
		 * @return <code>true</code> if the specified coordinates correspond to the 'first cell'
		 * of this shared formula group.
		 */
		public boolean isFirstCell(int row, int column) {
			return _firstCell.getRow() == row && _firstCell.getCol() == column;
		}
	}

	public static final SharedValueManager EMPTY = new SharedValueManager(
			new SharedFormulaRecord[0], new CellReference[0], new ArrayRecord[0], new TableRecord[0]);
	private final ArrayRecord[] _arrayRecords;
	private final TableRecord[] _tableRecords;
	private final Map<SharedFormulaRecord, SharedFormulaGroup> _groupsBySharedFormulaRecord;
	/** cached for optimization purposes */
	private SharedFormulaGroup[] _groups;

	private SharedValueManager(SharedFormulaRecord[] sharedFormulaRecords,
			CellReference[] firstCells, ArrayRecord[] arrayRecords, TableRecord[] tableRecords) {
		int nShF = sharedFormulaRecords.length;
		if (nShF != firstCells.length) {
			throw new IllegalArgumentException("array sizes don't match: " + nShF + "!=" + firstCells.length + ".");
		}
		_arrayRecords = arrayRecords;
		_tableRecords = tableRecords;
		Map<SharedFormulaRecord, SharedFormulaGroup> m = new HashMap<SharedFormulaRecord, SharedFormulaGroup>(nShF * 3 / 2);
		for (int i = 0; i < nShF; i++) {
			SharedFormulaRecord sfr = sharedFormulaRecords[i];
			m.put(sfr, new SharedFormulaGroup(sfr, firstCells[i]));
		}
		_groupsBySharedFormulaRecord = m;
	}

	/**
	 * @param firstCells
	 * @param recs list of sheet records (possibly contains records for other parts of the Excel file)
	 * @param startIx index of first row/cell record for current sheet
	 * @param endIx one past index of last row/cell record for current sheet.  It is important
	 * that this code does not inadvertently collect <tt>SharedFormulaRecord</tt>s from any other
	 * sheet (which could happen if endIx is chosen poorly).  (see bug 44449)
	 */
	public static SharedValueManager create(SharedFormulaRecord[] sharedFormulaRecords,
			CellReference[] firstCells, ArrayRecord[] arrayRecords, TableRecord[] tableRecords) {
		if (sharedFormulaRecords.length + firstCells.length + arrayRecords.length + tableRecords.length < 1) {
			return EMPTY;
		}
		return new SharedValueManager(sharedFormulaRecords, firstCells, arrayRecords, tableRecords);
	}


	/**
	 * @param firstCell as extracted from the {@link ExpPtg} from the cell's formula.
	 * @return never <code>null</code>
	 */
	public SharedFormulaRecord linkSharedFormulaRecord(CellReference firstCell, FormulaRecordAggregate agg) {

		SharedFormulaGroup result = findFormulaGroup(getGroups(), firstCell);
		result.add(agg);
		return result.getSFR();
	}

	private static SharedFormulaGroup findFormulaGroup(SharedFormulaGroup[] groups, CellReference firstCell) {
		int row = firstCell.getRow();
		int column = firstCell.getCol();
		// Traverse the list of shared formulas and try to find the correct one for us

		// perhaps this could be optimised to some kind of binary search
		for (int i = 0; i < groups.length; i++) {
			SharedFormulaGroup svg = groups[i];
			if (svg.isFirstCell(row, column)) {
				return svg;
			}
		}
		// TODO - fix file "15228.xls" so it opens in Excel after rewriting with POI
		throw new RuntimeException("Failed to find a matching shared formula record");
	}

	private SharedFormulaGroup[] getGroups() {
		if (_groups == null) {
			SharedFormulaGroup[] groups = new SharedFormulaGroup[_groupsBySharedFormulaRecord.size()];
			_groupsBySharedFormulaRecord.values().toArray(groups);
			Arrays.sort(groups, SVGComparator); // make search behaviour more deterministic
			_groups = groups;
		}
		return _groups;
	}

	private static final Comparator<SharedFormulaGroup> SVGComparator = new Comparator<SharedFormulaGroup>() {

		public int compare(SharedFormulaGroup a, SharedFormulaGroup b) {
			CellRangeAddress8Bit rangeA = a.getSFR().getRange();
			CellRangeAddress8Bit rangeB = b.getSFR().getRange();

			int cmp;
			cmp = rangeA.getFirstRow() - rangeB.getFirstRow();
			if (cmp != 0) {
				return cmp;
			}
			cmp = rangeA.getFirstColumn() - rangeB.getFirstColumn();
			if (cmp != 0) {
				return cmp;
			}
			return 0;
		}
	};

	/**
	 * Gets the {@link SharedValueRecordBase} record if it should be encoded immediately after the
	 * formula record contained in the specified {@link FormulaRecordAggregate} agg.  Note - the
	 * shared value record always appears after the first formula record in the group.  For arrays
	 * and tables the first formula is always the in the top left cell.  However, since shared
	 * formula groups can be sparse and/or overlap, the first formula may not actually be in the
	 * top left cell.
	 *
	 * @return the SHRFMLA, TABLE or ARRAY record for the formula cell, if it is the first cell of
	 * a table or array region. <code>null</code> if the formula cell is not shared/array/table,
	 * or if the specified formula is not the the first in the group.
	 */
	public SharedValueRecordBase getRecordForFirstCell(FormulaRecordAggregate agg) {
		CellReference firstCell = agg.getFormulaRecord().getFormula().getExpReference();
		// perhaps this could be optimised by consulting the (somewhat unreliable) isShared flag
		// and/or distinguishing between tExp and tTbl.
		if (firstCell == null) {
			// not a shared/array/table formula
			return null;
		}


		int row = firstCell.getRow();
		int column = firstCell.getCol();
		if (agg.getRow() != row || agg.getColumn() != column) {
			// not the first formula cell in the group
			return null;
		}
		SharedFormulaGroup[] groups = getGroups();
		for (int i = 0; i < groups.length; i++) {
			// note - logic for finding correct shared formula group is slightly
			// more complicated since the first cell
			SharedFormulaGroup sfg = groups[i];
			if (sfg.isFirstCell(row, column)) {
				return sfg.getSFR();
			}
		}

		// Since arrays and tables cannot be sparse (all cells in range participate)
		// The first cell will be the top left in the range.  So we can match the
		// ARRAY/TABLE record directly.

		for (int i = 0; i < _tableRecords.length; i++) {
			TableRecord tr = _tableRecords[i];
			if (tr.isFirstCell(row, column)) {
				return tr;
			}
		}
		for (int i = 0; i < _arrayRecords.length; i++) {
			ArrayRecord ar = _arrayRecords[i];
			if (ar.isFirstCell(row, column)) {
				return ar;
			}
		}
		return null;
	}

	/**
	 * Converts all {@link FormulaRecord}s handled by <tt>sharedFormulaRecord</tt>
	 * to plain unshared formulas
	 */
	public void unlink(SharedFormulaRecord sharedFormulaRecord) {
		SharedFormulaGroup svg = _groupsBySharedFormulaRecord.remove(sharedFormulaRecord);
		_groups = null; // be sure to reset cached value
		if (svg == null) {
			throw new IllegalStateException("Failed to find formulas for shared formula");
		}
		svg.unlinkSharedFormulas();
	}
}
