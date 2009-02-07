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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.MulBlankRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordBase;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.hssf.record.aggregates.RecordAggregate.RecordVisitor;
import org.apache.poi.hssf.record.formula.FormulaShifter;
import org.apache.poi.hssf.record.formula.Ptg;

/**
 *
 * Aggregate value records together.  Things are easier to handle that way.
 *
 * @author  andy
 * @author  Glen Stampoultzis (glens at apache.org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class ValueRecordsAggregate {
	private static final int MAX_ROW_INDEX = 0XFFFF;
	private static final int INDEX_NOT_SET = -1;
	private int firstcell = INDEX_NOT_SET;
	private int lastcell  = INDEX_NOT_SET;
	private CellValueRecordInterface[][] records;

	/** Creates a new instance of ValueRecordsAggregate */

	public ValueRecordsAggregate() {
		this(INDEX_NOT_SET, INDEX_NOT_SET, new CellValueRecordInterface[30][]); // We start with 30 Rows.
	}
	private ValueRecordsAggregate(int firstCellIx, int lastCellIx, CellValueRecordInterface[][] pRecords) {
		firstcell = firstCellIx;
		lastcell = lastCellIx;
		records = pRecords;
	}

	public void insertCell(CellValueRecordInterface cell) {
		short column = cell.getColumn();
		int row = cell.getRow();
		if (row >= records.length) {
			CellValueRecordInterface[][] oldRecords = records;
			int newSize = oldRecords.length * 2;
			if (newSize < row + 1)
				newSize = row + 1;
			records = new CellValueRecordInterface[newSize][];
			System.arraycopy(oldRecords, 0, records, 0, oldRecords.length);
		}
		CellValueRecordInterface[] rowCells = records[row];
		if (rowCells == null) {
			int newSize = column + 1;
			if (newSize < 10)
				newSize = 10;
			rowCells = new CellValueRecordInterface[newSize];
			records[row] = rowCells;
		}
		if (column >= rowCells.length) {
			CellValueRecordInterface[] oldRowCells = rowCells;
			int newSize = oldRowCells.length * 2;
			if (newSize < column + 1)
				newSize = column + 1;
			// if(newSize>257) newSize=257; // activate?
			rowCells = new CellValueRecordInterface[newSize];
			System.arraycopy(oldRowCells, 0, rowCells, 0, oldRowCells.length);
			records[row] = rowCells;
		}
		rowCells[column] = cell;

		if (column < firstcell || firstcell == INDEX_NOT_SET) {
			firstcell = column;
		}
		if (column > lastcell || lastcell == INDEX_NOT_SET) {
			lastcell = column;
		}
	}

	public void removeCell(CellValueRecordInterface cell) {
		if (cell == null) {
			throw new IllegalArgumentException("cell must not be null");
		}
		int row = cell.getRow();
		if (row >= records.length) {
			throw new RuntimeException("cell row is out of range");
		}
		CellValueRecordInterface[] rowCells = records[row];
		if (rowCells == null) {
			throw new RuntimeException("cell row is already empty");
		}
		short column = cell.getColumn();
		if (column >= rowCells.length) {
			throw new RuntimeException("cell column is out of range");
		}
		rowCells[column] = null;
	}

	public void removeAllCellsValuesForRow(int rowIndex) {
		if (rowIndex < 0 || rowIndex > MAX_ROW_INDEX) {
			throw new IllegalArgumentException("Specified rowIndex " + rowIndex
					+ " is outside the allowable range (0.." +MAX_ROW_INDEX + ")");
		}
		if (rowIndex >= records.length) {
			// this can happen when the client code has created a row,
			// and then removes/replaces it before adding any cells. (see bug 46312)
			return;
		}

		records[rowIndex] = null;
	}


	public int getPhysicalNumberOfCells() {
		int count = 0;
		for (int r = 0; r < records.length; r++) {
			CellValueRecordInterface[] rowCells = records[r];
			if (rowCells != null) {
				for (int c = 0; c < rowCells.length; c++) {
					if (rowCells[c] != null)
						count++;
				}
			}
		}
		return count;
	}

	public int getFirstCellNum() {
		return firstcell;
	}

	public int getLastCellNum() {
		return lastcell;
	}

	public void addMultipleBlanks(MulBlankRecord mbr) {
		for (int j = 0; j < mbr.getNumColumns(); j++) {
			BlankRecord br = new BlankRecord();

			br.setColumn(( short ) (j + mbr.getFirstColumn()));
			br.setRow(mbr.getRow());
			br.setXFIndex(mbr.getXFAt(j));
			insertCell(br);
		}
	}

	/**
	 * Processes a single cell value record
	 * @param sfh used to resolve any shared-formulas/arrays/tables for the current sheet
	 */
	public void construct(CellValueRecordInterface rec, RecordStream rs, SharedValueManager sfh) {
		if (rec instanceof FormulaRecord) {
			FormulaRecord formulaRec = (FormulaRecord)rec;
			// read optional cached text value
			StringRecord cachedText;
			Class<? extends Record> nextClass = rs.peekNextClass();
			if (nextClass == StringRecord.class) {
				cachedText = (StringRecord) rs.getNext();
			} else {
				cachedText = null;
			}
			insertCell(new FormulaRecordAggregate(formulaRec, cachedText, sfh));
		} else {
			insertCell(rec);
		}
	}

	/** Tallies a count of the size of the cell records
	 *  that are attached to the rows in the range specified.
	 */
	public int getRowCellBlockSize(int startRow, int endRow) {
		int result = 0;
		for(int rowIx=startRow; rowIx<=endRow && rowIx<records.length; rowIx++) {
			result += getRowSerializedSize(records[rowIx]);
		}
		return result;
	}

	/** Returns true if the row has cells attached to it */
	public boolean rowHasCells(int row) {
		if (row >= records.length) {
			return false;
		}
		CellValueRecordInterface[] rowCells=records[row];
		if(rowCells==null) return false;
		for(int col=0;col<rowCells.length;col++) {
			if(rowCells[col]!=null) return true;
		}
		return false;
	}

	private static int getRowSerializedSize(CellValueRecordInterface[] rowCells) {
		if(rowCells == null) {
			return 0;
		}
		int result = 0;
		for (int i = 0; i < rowCells.length; i++) {
			RecordBase cvr = (RecordBase) rowCells[i];
			if(cvr == null) {
				continue;
			}
			int nBlank = countBlanks(rowCells, i);
			if (nBlank > 1) {
				result += (10 + 2*nBlank);
				i+=nBlank-1;
			} else {
				result += cvr.getRecordSize();
			}
		}
		return result;
	}

	public void visitCellsForRow(int rowIndex, RecordVisitor rv) {

		CellValueRecordInterface[] rowCells = records[rowIndex];
		if(rowCells == null) {
			throw new IllegalArgumentException("Row [" + rowIndex + "] is empty");
		}


		for (int i = 0; i < rowCells.length; i++) {
			RecordBase cvr = (RecordBase) rowCells[i];
			if(cvr == null) {
				continue;
			}
			int nBlank = countBlanks(rowCells, i);
			if (nBlank > 1) {
				rv.visitRecord(createMBR(rowCells, i, nBlank));
				i+=nBlank-1;
			} else if (cvr instanceof RecordAggregate) {
				RecordAggregate agg = (RecordAggregate) cvr;
				agg.visitContainedRecords(rv);
			} else {
				rv.visitRecord((Record) cvr);
			}
		}
	}

	/**
	 * @return the number of <em>consecutive</em> {@link BlankRecord}s in the specified row
	 * starting from startIx.
	 */
	private static int countBlanks(CellValueRecordInterface[] rowCellValues, int startIx) {
		int i = startIx;
		while(i < rowCellValues.length) {
			CellValueRecordInterface cvr = rowCellValues[i];
			if (!(cvr instanceof BlankRecord)) {
				break;
			}
			i++;
		}
		return i - startIx;
	}

	private MulBlankRecord createMBR(CellValueRecordInterface[] cellValues, int startIx, int nBlank) {

		short[] xfs = new short[nBlank];
		for (int i = 0; i < xfs.length; i++) {
			xfs[i] = ((BlankRecord)cellValues[startIx + i]).getXFIndex();
		}
		int rowIx = cellValues[startIx].getRow();
		return new MulBlankRecord(rowIx, startIx, xfs);
	}

	public void updateFormulasAfterRowShift(FormulaShifter shifter, int currentExternSheetIndex) {
		for (int i = 0; i < records.length; i++) {
			CellValueRecordInterface[] rowCells = records[i];
			if (rowCells == null) {
				continue;
			}
			for (int j = 0; j < rowCells.length; j++) {
				CellValueRecordInterface cell = rowCells[j];
				if (cell instanceof FormulaRecordAggregate) {
					FormulaRecord fr = ((FormulaRecordAggregate)cell).getFormulaRecord();
					Ptg[] ptgs = fr.getParsedExpression(); // needs clone() inside this getter?
					if (shifter.adjustFormula(ptgs, currentExternSheetIndex)) {
						fr.setParsedExpression(ptgs);
					}
				}
			}
		}
	}

	/**
	 * Gets all the cell records contained in this aggregate. 
	 * Note {@link BlankRecord}s appear separate (not in {@link MulBlankRecord}s).
	 */
	public CellValueRecordInterface[] getValueRecords() {
		List<CellValueRecordInterface> temp = new ArrayList<CellValueRecordInterface>();

		for (int rowIx = 0; rowIx < records.length; rowIx++) {
			CellValueRecordInterface[] rowCells = records[rowIx];
			if (rowCells == null) {
				continue;
			}
			for (int colIx = 0; colIx < rowCells.length; colIx++) {
				CellValueRecordInterface cell = rowCells[colIx];
				if (cell != null) {
					temp.add(cell);
				}
			}
		}

		CellValueRecordInterface[] result = new CellValueRecordInterface[temp.size()];
		temp.toArray(result);
		return result;
	}

	public Object clone() {
		throw new RuntimeException("clone() should not be called.  ValueRecordsAggregate should be copied via Sheet.cloneSheet()");
	}
}
