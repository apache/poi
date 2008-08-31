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

	public static final SharedValueManager EMPTY = new SharedValueManager(
			new SharedFormulaRecord[0], new ArrayRecord[0], new TableRecord[0]);
	private final SharedFormulaRecord[] _sfrs;
	private final ArrayRecord[] _arrayRecords;
	private final TableRecord[] _tableRecords;

	private SharedValueManager(SharedFormulaRecord[] sharedFormulaRecords,
			ArrayRecord[] arrayRecords, TableRecord[] tableRecords) {
		_sfrs = sharedFormulaRecords;
		_arrayRecords = arrayRecords;
		_tableRecords = tableRecords;
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

	public void convertSharedFormulaRecord(FormulaRecord formula) {
		int row = formula.getRow();
		int column = formula.getColumn();
		// Traverse the list of shared formulas in
		// reverse order, and try to find the correct one
		// for us
		for (int i = 0; i < _sfrs.length; i++) {
			SharedFormulaRecord shrd = _sfrs[i];
			if (shrd.isInRange(row, column)) {
				shrd.convertSharedFormulaRecord(formula);
				return;
			}
		}
		// not found
		handleMissingSharedFormulaRecord(formula);
	}

	/**
	 * Sometimes the shared formula flag "seems" to be erroneously set, in which case there is no 
	 * call to <tt>SharedFormulaRecord.convertSharedFormulaRecord</tt> and hence the 
	 * <tt>parsedExpression</tt> field of this <tt>FormulaRecord</tt> will not get updated.<br/>
	 * As it turns out, this is not a problem, because in these circumstances, the existing value
	 * for <tt>parsedExpression</tt> is perfectly OK.<p/>
	 * 
	 * This method may also be used for setting breakpoints to help diagnose issues regarding the
	 * abnormally-set 'shared formula' flags. 
	 * (see TestValueRecordsAggregate.testSpuriousSharedFormulaFlag()).<p/>
	 * 
	 * The method currently does nothing but do not delete it without finding a nice home for this 
	 * comment.
	 */
	private static void handleMissingSharedFormulaRecord(FormulaRecord formula) {
		// could log an info message here since this is a fairly unusual occurrence.
		formula.setSharedFormula(false); // no point leaving the flag erroneously set
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
		return null;
	}
}
