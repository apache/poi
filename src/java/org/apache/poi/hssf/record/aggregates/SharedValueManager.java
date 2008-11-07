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
import org.apache.poi.hssf.record.formula.TblPtg;
import org.apache.poi.hssf.util.CellRangeAddress8Bit;
import org.apache.poi.hssf.util.CellReference;

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

	// This class should probably be generalised to handle array and table groups too
	private static final class SharedValueGroup {
		private final SharedValueRecordBase _svr;
		private FormulaRecordAggregate[] _frAggs;
		private int _numberOfFormulas;

		public SharedValueGroup(SharedValueRecordBase svr) {
			_svr = svr;
			int width = svr.getLastColumn() - svr.getFirstColumn() + 1;
			int height = svr.getLastRow() - svr.getFirstRow() + 1;
			_frAggs = new FormulaRecordAggregate[width * height];
			_numberOfFormulas = 0;
		}

		public void add(FormulaRecordAggregate agg) {
			if (_numberOfFormulas >= _frAggs.length) {
				// this probably shouldn't occur - problems with sample file "15228.xls"
				FormulaRecordAggregate[] temp = new FormulaRecordAggregate[_numberOfFormulas*2];
				System.arraycopy(_frAggs, 0, temp, 0, _frAggs.length);
				_frAggs = temp;
			}
			_frAggs[_numberOfFormulas++] = agg;
		}

		public void unlinkSharedFormulas() {
			for (int i = 0; i < _numberOfFormulas; i++) {
				_frAggs[i].unlinkSharedFormula();
			}
		}

		public SharedValueRecordBase getSVR() {
			return _svr;
		}

		/**
		 * Note - Sometimes the first formula in a group is not present (because the range
		 * is sparsely populated), so this method can return <code>true</code> for a cell
		 * that is not the top-left corner of the range.
		 * @return <code>true</code> if this is the first formula cell in the group
		 */
		public boolean isFirstMember(FormulaRecordAggregate agg) {
			return agg == _frAggs[0];
		}
		public final String toString() {
			StringBuffer sb = new StringBuffer(64);
			sb.append(getClass().getName()).append(" [");
			sb.append(_svr.getRange().toString());
			sb.append("]");
			return sb.toString();
		}
	}

	public static final SharedValueManager EMPTY = new SharedValueManager(
			new SharedFormulaRecord[0], new ArrayRecord[0], new TableRecord[0]);
	private final ArrayRecord[] _arrayRecords;
	private final TableRecord[] _tableRecords;
	private final Map _groupsBySharedFormulaRecord;
	/** cached for optimization purposes */
	private SharedValueGroup[] _groups;

	private SharedValueManager(SharedFormulaRecord[] sharedFormulaRecords,
			ArrayRecord[] arrayRecords, TableRecord[] tableRecords) {
		_arrayRecords = arrayRecords;
		_tableRecords = tableRecords;
		Map m = new HashMap(sharedFormulaRecords.length * 3 / 2);
		for (int i = 0; i < sharedFormulaRecords.length; i++) {
			SharedFormulaRecord sfr = sharedFormulaRecords[i];
			m.put(sfr, new SharedValueGroup(sfr));
		}
		_groupsBySharedFormulaRecord = m;
	}

	/**
	 * @param recs list of sheet records (possibly contains records for other parts of the Excel file)
	 * @param startIx index of first row/cell record for current sheet
	 * @param endIx one past index of last row/cell record for current sheet.  It is important
	 * that this code does not inadvertently collect <tt>SharedFormulaRecord</tt>s from any other
	 * sheet (which could happen if endIx is chosen poorly).  (see bug 44449)
	 */
	public static SharedValueManager create(SharedFormulaRecord[] sharedFormulaRecords,
			ArrayRecord[] arrayRecords, TableRecord[] tableRecords) {
		if (sharedFormulaRecords.length + arrayRecords.length + tableRecords.length < 1) {
			return EMPTY;
		}
		return new SharedValueManager(sharedFormulaRecords, arrayRecords, tableRecords);
	}


	/**
	 * @param firstCell as extracted from the {@link ExpPtg} from the cell's formula.
	 * @return never <code>null</code>
	 */
	public SharedFormulaRecord linkSharedFormulaRecord(CellReference firstCell, FormulaRecordAggregate agg) {

		SharedValueGroup result = findGroup(getGroups(), firstCell);
		result.add(agg);
		return (SharedFormulaRecord) result.getSVR();
	}

	private static SharedValueGroup findGroup(SharedValueGroup[] groups, CellReference firstCell) {
		int row = firstCell.getRow();
		int column = firstCell.getCol();
		// Traverse the list of shared formulas and try to find the correct one for us

		// perhaps this could be optimised to some kind of binary search
		for (int i = 0; i < groups.length; i++) {
			SharedValueGroup svg = groups[i];
			if (svg.getSVR().isFirstCell(row, column)) {
				return svg;
			}
		}
		// else - no SharedFormulaRecord was found with the specified firstCell.
		// This is unusual, but one sample file exhibits the anomaly: "ex45046-21984.xls"
		// Excel seems to handle the problem OK, and doesn't even correct it.  Perhaps POI should.

		// search for shared formula by range
		SharedValueGroup result = null;
		for (int i = 0; i < groups.length; i++) {
			SharedValueGroup svg = groups[i];
			if (svg.getSVR().isInRange(row, column)) {
				if (result != null) {
					// This happens in sample file "15228.xls"
					if (sharedFormulasAreSame(result, svg)) {
						// hopefully this is OK - just use the first one since they are the same
						// not quite
						// TODO - fix file "15228.xls" so it opens in Excel after rewriting with POI
					} else {
						throw new RuntimeException("This cell is in the range of more than one distinct shared formula");
					}
				} else {
					result = svg;
				}
			}
		}
		if (result == null) {
			throw new RuntimeException("Failed to find a matching shared formula record");
		}
		return result;
	}

	/**
	 * Handles the ugly situation (seen in example "15228.xls") where a shared formula cell is
	 * covered by more than one shared formula range, but the formula cell's {@link ExpPtg}
	 * doesn't identify any of them.
	 * @return <code>true</code> if the underlying shared formulas are the same
	 */
	private static boolean sharedFormulasAreSame(SharedValueGroup grpA, SharedValueGroup grpB) {
		// safe to cast here because this findGroup() is never called for ARRAY or TABLE formulas
		SharedFormulaRecord sfrA = (SharedFormulaRecord) grpA.getSVR();
		SharedFormulaRecord sfrB = (SharedFormulaRecord) grpB.getSVR();
		return sfrA.isFormulaSame(sfrB);
	}

	private SharedValueGroup[] getGroups() {
		if (_groups == null) {
			SharedValueGroup[] groups = new SharedValueGroup[_groupsBySharedFormulaRecord.size()];
			_groupsBySharedFormulaRecord.values().toArray(groups);
			Arrays.sort(groups, SVGComparator); // make search behaviour more deterministic
			_groups = groups;
		}
		return _groups;
	}

	private static final Comparator SVGComparator = new Comparator() {

		public int compare(Object a, Object b) {
			CellRangeAddress8Bit rangeA = ((SharedValueGroup)a).getSVR().getRange();
			CellRangeAddress8Bit rangeB = ((SharedValueGroup)b).getSVR().getRange();

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
	 * The {@link SharedValueRecordBase} record returned by this method
	 * @param firstCell the cell coordinates as read from the {@link ExpPtg} or {@link TblPtg}
	 * of the current formula.  Note - this is usually not the same as the cell coordinates
	 * of the formula's cell.
	 *
	 * @return the SHRFMLA, TABLE or ARRAY record for this formula cell, if it is the first cell of a
	 * table or array region. <code>null</code> if
	 */
	public SharedValueRecordBase getRecordForFirstCell(CellReference firstCell, FormulaRecordAggregate agg) {
		int row = firstCell.getRow();
		int column = firstCell.getCol();
		boolean isTopLeft = agg.getRow() == row && agg.getColumn() == column;
		if (isTopLeft) {
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
		} else {
			// Since arrays and tables cannot be sparse (all cells in range participate)
			// no need to search arrays and tables if agg is not the top left cell
		}
		SharedValueGroup[] groups = getGroups();
		for (int i = 0; i < groups.length; i++) {
			SharedValueGroup svg = groups[i];
			SharedValueRecordBase svr = svg.getSVR();
			if (svr.isFirstCell(row, column)) {
				if (svg.isFirstMember(agg)) {
					return svr;
				}
				return null;
			}
		}
		return null;
	}

	/**
	 * Converts all {@link FormulaRecord}s handled by <tt>sharedFormulaRecord</tt>
	 * to plain unshared formulas
	 */
	public void unlink(SharedFormulaRecord sharedFormulaRecord) {
		SharedValueGroup svg = (SharedValueGroup) _groupsBySharedFormulaRecord.remove(sharedFormulaRecord);
		_groups = null; // be sure to reset cached value
		if (svg == null) {
			throw new IllegalStateException("Failed to find formulas for shared formula");
		}
		svg.unlinkSharedFormulas();
	}
}
