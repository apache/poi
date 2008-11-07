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

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.record.ArrayRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.SharedFormulaRecord;
import org.apache.poi.hssf.record.SharedValueRecordBase;
import org.apache.poi.hssf.record.TableRecord;

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
		private final FormulaRecordAggregate[] _frAggs;
		private int _numberOfFormulas;

		public SharedValueGroup(SharedValueRecordBase svr) {
			_svr = svr;
			int width = svr.getLastColumn() - svr.getFirstColumn() + 1;
			int height = svr.getLastRow() - svr.getFirstRow() + 1;
			_frAggs = new FormulaRecordAggregate[width * height];
			_numberOfFormulas = 0;
		}

		public void add(FormulaRecordAggregate agg) {
			_frAggs[_numberOfFormulas++] = agg;
		}

		public void unlinkSharedFormulas() {
			for (int i = 0; i < _numberOfFormulas; i++) {
				_frAggs[i].unlinkSharedFormula();
			}
		}

		public boolean isInRange(int rowIx, int columnIx) {
			return _svr.isInRange(rowIx, columnIx);
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
		public boolean isFirstCell(int row, int column) {
			// hack for the moment, just check against the first formula that 
			// came in through the add() method.
			FormulaRecordAggregate fra = _frAggs[0];
			return fra.getRow() == row && fra.getColumn() == column;
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

	public static SharedValueManager create(SharedFormulaRecord[] sharedFormulaRecords,
			ArrayRecord[] arrayRecords, TableRecord[] tableRecords) {
		if (sharedFormulaRecords.length + arrayRecords.length + tableRecords.length < 1) {
			return EMPTY;
		}
		return new SharedValueManager(sharedFormulaRecords, arrayRecords, tableRecords);
	}


	/**
	 * @return <code>null</code> if the specified formula does not have any corresponding
	 * {@link SharedFormulaRecord}
	 */
	public SharedFormulaRecord linkSharedFormulaRecord(FormulaRecordAggregate agg) {
		FormulaRecord formula = agg.getFormulaRecord();
		int row = formula.getRow();
		int column = formula.getColumn();
		// Traverse the list of shared formulas in
		// reverse order, and try to find the correct one
		// for us
		
		SharedValueGroup[] groups = getGroups();
		for (int i = 0; i < groups.length; i++) {
			SharedValueGroup svr = groups[i];
			if (svr.isInRange(row, column)) {
				svr.add(agg);
				return (SharedFormulaRecord) svr.getSVR();
			}
		}
		return null;
	}

	private SharedValueGroup[] getGroups() {
		if (_groups == null) {
			SharedValueGroup[] groups = new SharedValueGroup[_groupsBySharedFormulaRecord.size()];
			_groupsBySharedFormulaRecord.values().toArray(groups);
			_groups = groups;
			
		}
		return _groups;
	}



	/**
	 * Note - does not return SharedFormulaRecords currently, because the corresponding formula
	 * records have been converted to 'unshared'. POI does not attempt to re-share formulas. On
	 * the other hand, there is no such conversion for array or table formulas, so this method 
	 * returns the TABLE or ARRAY record (if it should be written after the specified 
	 * formulaRecord.
	 * 
	 * @return the TABLE or ARRAY record for this formula cell, if it is the first cell of a 
	 * table or array region.
	 */
	public SharedValueRecordBase getRecordForFirstCell(FormulaRecord formulaRecord) {
		int row = formulaRecord.getRow();
		int column = formulaRecord.getColumn();
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
		SharedValueGroup[] groups = getGroups();
		for (int i = 0; i < groups.length; i++) {
			SharedValueGroup svg = groups[i];
			if (svg.isFirstCell(row, column)) {
				return svg.getSVR();
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
