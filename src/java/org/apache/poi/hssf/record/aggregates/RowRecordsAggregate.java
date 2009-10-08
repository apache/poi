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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.record.ArrayRecord;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.hssf.record.DBCellRecord;
import org.apache.poi.hssf.record.DimensionsRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.IndexRecord;
import org.apache.poi.hssf.record.MergeCellsRecord;
import org.apache.poi.hssf.record.MulBlankRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SharedFormulaRecord;
import org.apache.poi.hssf.record.TableRecord;
import org.apache.poi.hssf.record.UnknownRecord;
import org.apache.poi.hssf.record.formula.FormulaShifter;
import org.apache.poi.ss.SpreadsheetVersion;

/**
 *
 * @author  andy
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class RowRecordsAggregate extends RecordAggregate {
	private int _firstrow = -1;
	private int _lastrow  = -1;
	private final Map<Integer, RowRecord> _rowRecords;
	private final ValueRecordsAggregate _valuesAgg;
	private final List<Record> _unknownRecords;
	private final SharedValueManager _sharedValueManager;

	/** Creates a new instance of ValueRecordsAggregate */
	public RowRecordsAggregate() {
		this(SharedValueManager.EMPTY);
	}
	private RowRecordsAggregate(SharedValueManager svm) {
		_rowRecords = new TreeMap<Integer, RowRecord>();
		_valuesAgg = new ValueRecordsAggregate();
		_unknownRecords = new ArrayList<Record>();
		_sharedValueManager = svm;
	}

	/**
	 * @param rs record stream with all {@link SharedFormulaRecord}
	 * {@link ArrayRecord}, {@link TableRecord} {@link MergeCellsRecord} Records removed
	 */
	public RowRecordsAggregate(RecordStream rs, SharedValueManager svm) {
		this(svm);
		while(rs.hasNext()) {
			Record rec = rs.getNext();
			switch (rec.getSid()) {
				case RowRecord.sid:
					insertRow((RowRecord) rec);
					continue;
				case DBCellRecord.sid:
					// end of 'Row Block'.  Should only occur after cell records
					// ignore DBCELL records because POI generates them upon re-serialization
					continue;
			}
			if (rec instanceof UnknownRecord) {
				// might need to keep track of where exactly these belong
				addUnknownRecord(rec);
				while (rs.peekNextSid() == ContinueRecord.sid) {
					addUnknownRecord(rs.getNext());
				}
				continue;
			}
			if (rec instanceof MulBlankRecord) {
				_valuesAgg.addMultipleBlanks((MulBlankRecord) rec);
				continue;
			}
			if (!(rec instanceof CellValueRecordInterface)) {
				throw new RuntimeException("Unexpected record type (" + rec.getClass().getName() + ")");
			}
			_valuesAgg.construct((CellValueRecordInterface)rec, rs, svm);
		}
	}
	/**
	 * Handles UnknownRecords which appear within the row/cell records
	 */
	private void addUnknownRecord(Record rec) {
		// ony a few distinct record IDs are encountered by the existing POI test cases:
		// 0x1065 // many
		// 0x01C2 // several
		// 0x0034 // few
		// No documentation could be found for these

		// keep the unknown records for re-serialization
		_unknownRecords.add(rec);
	}
	public void insertRow(RowRecord row) {
		// Integer integer = Integer.valueOf(row.getRowNumber());
		_rowRecords.put(Integer.valueOf(row.getRowNumber()), row);
		if ((row.getRowNumber() < _firstrow) || (_firstrow == -1)) {
			_firstrow = row.getRowNumber();
		}
		if ((row.getRowNumber() > _lastrow) || (_lastrow == -1)) {
			_lastrow = row.getRowNumber();
		}
	}

	public void removeRow(RowRecord row) {
		int rowIndex = row.getRowNumber();
		_valuesAgg.removeAllCellsValuesForRow(rowIndex);
		Integer key = Integer.valueOf(rowIndex);
		RowRecord rr = _rowRecords.remove(key);
		if (rr == null) {
			throw new RuntimeException("Invalid row index (" + key.intValue() + ")");
		}
		if (row != rr) {
			_rowRecords.put(key, rr);
			throw new RuntimeException("Attempt to remove row that does not belong to this sheet");
		}
	}

	public RowRecord getRow(int rowIndex) {
        int maxrow = SpreadsheetVersion.EXCEL97.getLastRowIndex();
        if (rowIndex < 0 || rowIndex > maxrow) {
			throw new IllegalArgumentException("The row number must be between 0 and " + maxrow);
		}
		return _rowRecords.get(Integer.valueOf(rowIndex));
	}

	public int getPhysicalNumberOfRows()
	{
		return _rowRecords.size();
	}

	public int getFirstRowNum()
	{
		return _firstrow;
	}

	public int getLastRowNum()
	{
		return _lastrow;
	}

	/** Returns the number of row blocks.
	 * <p/>The row blocks are goupings of rows that contain the DBCell record
	 * after them
	 */
	public int getRowBlockCount() {
	  int size = _rowRecords.size()/DBCellRecord.BLOCK_SIZE;
	  if ((_rowRecords.size() % DBCellRecord.BLOCK_SIZE) != 0)
		  size++;
	  return size;
	}

	private int getRowBlockSize(int block) {
	  return RowRecord.ENCODED_SIZE * getRowCountForBlock(block);
	}

	/** Returns the number of physical rows within a block*/
	public int getRowCountForBlock(int block) {
	  int startIndex = block * DBCellRecord.BLOCK_SIZE;
	  int endIndex = startIndex + DBCellRecord.BLOCK_SIZE - 1;
	  if (endIndex >= _rowRecords.size())
		endIndex = _rowRecords.size()-1;

	  return endIndex-startIndex+1;
	}

	/** Returns the physical row number of the first row in a block*/
	private int getStartRowNumberForBlock(int block) {
	  //Given that we basically iterate through the rows in order,
	  // TODO - For a performance improvement, it would be better to return an instance of
	  //an iterator and use that instance throughout, rather than recreating one and
	  //having to move it to the right position.
	  int startIndex = block * DBCellRecord.BLOCK_SIZE;
	  Iterator rowIter = _rowRecords.values().iterator();
	  RowRecord row = null;
	  //Position the iterator at the start of the block
	  for (int i=0; i<=startIndex;i++) {
		row = (RowRecord)rowIter.next();
	  }
	  if (row == null) {
		  throw new RuntimeException("Did not find start row for block " + block);
	  }

	  return row.getRowNumber();
	}

	/** Returns the physical row number of the end row in a block*/
	private int getEndRowNumberForBlock(int block) {
	  int endIndex = ((block + 1)*DBCellRecord.BLOCK_SIZE)-1;
	  if (endIndex >= _rowRecords.size())
		endIndex = _rowRecords.size()-1;

	  Iterator rowIter = _rowRecords.values().iterator();
	  RowRecord row = null;
	  for (int i=0; i<=endIndex;i++) {
		row = (RowRecord)rowIter.next();
	  }
	  if (row == null) {
		  throw new RuntimeException("Did not find start row for block " + block);
	  }
	  return row.getRowNumber();
	}

	private int visitRowRecordsForBlock(int blockIndex, RecordVisitor rv) {
		final int startIndex = blockIndex*DBCellRecord.BLOCK_SIZE;
		final int endIndex = startIndex + DBCellRecord.BLOCK_SIZE;

		Iterator rowIterator = _rowRecords.values().iterator();

		//Given that we basically iterate through the rows in order,
		//For a performance improvement, it would be better to return an instance of
		//an iterator and use that instance throughout, rather than recreating one and
		//having to move it to the right position.
		int i=0;
		for (;i<startIndex;i++)
		  rowIterator.next();
		int result = 0;
		while(rowIterator.hasNext() && (i++ < endIndex)) {
		  Record rec = (Record)rowIterator.next();
		  result += rec.getRecordSize();
		  rv.visitRecord(rec);
		}
		return result;
	}

	public void visitContainedRecords(RecordVisitor rv) {

		PositionTrackingVisitor stv = new PositionTrackingVisitor(rv, 0);
		//DBCells are serialized before row records.
		final int blockCount = getRowBlockCount();
		for (int blockIndex = 0; blockIndex < blockCount; blockIndex++) {
			// Serialize a block of rows.
			// Hold onto the position of the first row in the block
			int pos=0;
			// Hold onto the size of this block that was serialized
			final int rowBlockSize = visitRowRecordsForBlock(blockIndex, rv);
			pos += rowBlockSize;
			// Serialize a block of cells for those rows
			final int startRowNumber = getStartRowNumberForBlock(blockIndex);
			final int endRowNumber = getEndRowNumberForBlock(blockIndex);
			DBCellRecord.Builder dbcrBuilder = new DBCellRecord.Builder();
			// Note: Cell references start from the second row...
			int cellRefOffset = (rowBlockSize - RowRecord.ENCODED_SIZE);
			for (int row = startRowNumber; row <= endRowNumber; row++) {
				if (_valuesAgg.rowHasCells(row)) {
					stv.setPosition(0);
					_valuesAgg.visitCellsForRow(row, stv);
					int rowCellSize = stv.getPosition();
					pos += rowCellSize;
					// Add the offset to the first cell for the row into the
					// DBCellRecord.
					dbcrBuilder.addCellOffset(cellRefOffset);
					cellRefOffset = rowCellSize;
				}
			}
			// Calculate Offset from the start of a DBCellRecord to the first Row
			rv.visitRecord(dbcrBuilder.build(pos));
		}
		for (int i=0; i< _unknownRecords.size(); i++) {
			// Potentially breaking the file here since we don't know exactly where to write these records
			rv.visitRecord(_unknownRecords.get(i));
		}
	}

	public Iterator getIterator() {
		return _rowRecords.values().iterator();
	}

	public int findStartOfRowOutlineGroup(int row) {
		// Find the start of the group.
		RowRecord rowRecord = this.getRow( row );
		int level = rowRecord.getOutlineLevel();
		int currentRow = row;
		while (this.getRow( currentRow ) != null) {
			rowRecord = this.getRow( currentRow );
			if (rowRecord.getOutlineLevel() < level) {
				return currentRow + 1;
			}
			currentRow--;
		}

		return currentRow + 1;
	}

	public int findEndOfRowOutlineGroup(int row) {
		int level = getRow( row ).getOutlineLevel();
		int currentRow;
		for (currentRow = row; currentRow < getLastRowNum(); currentRow++) {
			if (getRow(currentRow) == null || getRow(currentRow).getOutlineLevel() < level) {
				break;
			}
		}

		return currentRow-1;
	}

	/**
	 * Hide all rows at or below the current outline level
	 * @return index of the <em>next<em> row after the last row that gets hidden
	 */
	private int writeHidden(RowRecord pRowRecord, int row) {
		int rowIx = row;
		RowRecord rowRecord = pRowRecord;
		int level = rowRecord.getOutlineLevel();
		while (rowRecord != null && getRow(rowIx).getOutlineLevel() >= level) {
			rowRecord.setZeroHeight(true);
			rowIx++;
			rowRecord = getRow(rowIx);
		}
		return rowIx;
	}

	public void collapseRow(int rowNumber) {

		// Find the start of the group.
		int startRow = findStartOfRowOutlineGroup(rowNumber);
		RowRecord rowRecord = getRow(startRow);

		// Hide all the columns until the end of the group
		int nextRowIx = writeHidden(rowRecord, startRow);

		RowRecord row = getRow(nextRowIx);
		if (row == null) {
			row = createRow(nextRowIx);
			insertRow(row);
		}
		// Write collapse field
		row.setColapsed(true);
	}

	/**
	 * Create a row record.
	 *
	 * @param rowNumber row number
	 * @return RowRecord created for the passed in row number
	 * @see org.apache.poi.hssf.record.RowRecord
	 */
	public static RowRecord createRow(int rowNumber) {
		return new RowRecord(rowNumber);
	}

	public boolean isRowGroupCollapsed(int row) {
		int collapseRow = findEndOfRowOutlineGroup(row) + 1;

		if (getRow(collapseRow) == null) {
			return false;
		}
		return getRow( collapseRow ).getColapsed();
	}

	public void expandRow(int rowNumber) {
		int idx = rowNumber;
		if (idx == -1)
			return;

		// If it is already expanded do nothing.
		if (!isRowGroupCollapsed(idx)) {
			return;
		}

		// Find the start of the group.
		int startIdx = findStartOfRowOutlineGroup(idx);
		RowRecord row = getRow(startIdx);

		// Find the end of the group.
		int endIdx = findEndOfRowOutlineGroup(idx);

		// expand:
		// collapsed bit must be unset
		// hidden bit gets unset _if_ surrounding groups are expanded you can determine
		//   this by looking at the hidden bit of the enclosing group.  You will have
		//   to look at the start and the end of the current group to determine which
		//   is the enclosing group
		// hidden bit only is altered for this outline level.  ie.  don't un-collapse contained groups
		if (!isRowGroupHiddenByParent(idx)) {
			for (int i = startIdx; i <= endIdx; i++) {
				RowRecord otherRow = getRow(i);
				if (row.getOutlineLevel() == otherRow.getOutlineLevel() || !isRowGroupCollapsed(i)) {
					otherRow.setZeroHeight(false);
				}
			}
		}

		// Write collapse field
		getRow(endIdx + 1).setColapsed(false);
	}

	public boolean isRowGroupHiddenByParent(int row) {
		// Look out outline details of end
		int endLevel;
		boolean endHidden;
		int endOfOutlineGroupIdx = findEndOfRowOutlineGroup(row);
		if (getRow(endOfOutlineGroupIdx + 1) == null) {
			endLevel = 0;
			endHidden = false;
		} else {
			endLevel = getRow(endOfOutlineGroupIdx + 1).getOutlineLevel();
			endHidden = getRow(endOfOutlineGroupIdx + 1).getZeroHeight();
		}

		// Look out outline details of start
		int startLevel;
		boolean startHidden;
		int startOfOutlineGroupIdx = findStartOfRowOutlineGroup( row );
		if (startOfOutlineGroupIdx - 1 < 0 || getRow(startOfOutlineGroupIdx - 1) == null) {
			startLevel = 0;
			startHidden = false;
		} else {
			startLevel = getRow(startOfOutlineGroupIdx - 1).getOutlineLevel();
			startHidden = getRow(startOfOutlineGroupIdx - 1).getZeroHeight();
		}

		if (endLevel > startLevel) {
			return endHidden;
		}

		return startHidden;
	}

	public CellValueRecordInterface[] getValueRecords() {
		return _valuesAgg.getValueRecords();
	}

	public IndexRecord createIndexRecord(int indexRecordOffset, int sizeOfInitialSheetRecords) {
		IndexRecord result = new IndexRecord();
		result.setFirstRow(_firstrow);
		result.setLastRowAdd1(_lastrow + 1);
		// Calculate the size of the records from the end of the BOF
		// and up to the RowRecordsAggregate...

		// Add the references to the DBCells in the IndexRecord (one for each block)
		// Note: The offsets are relative to the Workbook BOF. Assume that this is
		// 0 for now.....

		int blockCount = getRowBlockCount();
		// Calculate the size of this IndexRecord
		int indexRecSize = IndexRecord.getRecordSizeForBlockCount(blockCount);

		int currentOffset = indexRecordOffset + indexRecSize + sizeOfInitialSheetRecords;

		for (int block = 0; block < blockCount; block++) {
			// each row-block has a DBCELL record.
			// The offset of each DBCELL record needs to be updated in the INDEX record

			// account for row records in this row-block
			currentOffset += getRowBlockSize(block);
			// account for cell value records after those
			currentOffset += _valuesAgg.getRowCellBlockSize(
					getStartRowNumberForBlock(block), getEndRowNumberForBlock(block));

			// currentOffset is now the location of the DBCELL record for this row-block
			result.addDbcell(currentOffset);
			// Add space required to write the DBCELL record (whose reference was just added).
			currentOffset += (8 + (getRowCountForBlock(block) * 2));
		}
		return result;
	}
	public void insertCell(CellValueRecordInterface cvRec) {
		_valuesAgg.insertCell(cvRec);
	}
	public void removeCell(CellValueRecordInterface cvRec) {
		if (cvRec instanceof FormulaRecordAggregate) {
			((FormulaRecordAggregate)cvRec).notifyFormulaChanging();
		}
		_valuesAgg.removeCell(cvRec);
	}
	public FormulaRecordAggregate createFormula(int row, int col) {
		FormulaRecord fr = new FormulaRecord();
		fr.setRow(row);
		fr.setColumn((short) col);
		return new FormulaRecordAggregate(fr, null, _sharedValueManager);
	}
	public void updateFormulasAfterRowShift(FormulaShifter formulaShifter, int currentExternSheetIndex) {
		_valuesAgg.updateFormulasAfterRowShift(formulaShifter, currentExternSheetIndex);
	}
	public DimensionsRecord createDimensions() {
		DimensionsRecord result = new DimensionsRecord();
		result.setFirstRow(_firstrow);
		result.setLastRow(_lastrow);
		result.setFirstCol((short) _valuesAgg.getFirstCellNum());
		result.setLastCol((short) _valuesAgg.getLastCellNum());
		return result;
	}
}
