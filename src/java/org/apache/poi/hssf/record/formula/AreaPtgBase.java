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

package org.apache.poi.hssf.record.formula;

import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Specifies a rectangular area of cells A1:A4 for instance.
 * @author  andy
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public abstract class AreaPtgBase extends OperandPtg implements AreaI {
	/**
	 * TODO - (May-2008) fix subclasses of AreaPtg 'AreaN~' which are used in shared formulas.
	 * see similar comment in ReferencePtg
	 */
	protected final RuntimeException notImplemented() {
		return new RuntimeException("Coding Error: This method should never be called. This ptg should be converted");
	}

	/** zero based, unsigned 16 bit */
	private int             field_1_first_row;
	/** zero based, unsigned 16 bit */
	private int             field_2_last_row;
	/** zero based, unsigned 8 bit */
	private int             field_3_first_column;
	/** zero based, unsigned 8 bit */
	private int             field_4_last_column;

	private final static BitField   rowRelative = BitFieldFactory.getInstance(0x8000);
	private final static BitField   colRelative = BitFieldFactory.getInstance(0x4000);
	private final static BitField   columnMask      = BitFieldFactory.getInstance(0x3FFF);

	protected AreaPtgBase() {
		// do nothing
	}

	protected AreaPtgBase(AreaReference ar) {
		CellReference firstCell = ar.getFirstCell();
		CellReference lastCell = ar.getLastCell();
		setFirstRow(firstCell.getRow());
		setFirstColumn(firstCell.getCol() == -1 ? 0 : firstCell.getCol());
		setLastRow(lastCell.getRow());
		setLastColumn(lastCell.getCol() == -1 ? 0xFF : lastCell.getCol());
		setFirstColRelative(!firstCell.isColAbsolute());
		setLastColRelative(!lastCell.isColAbsolute());
		setFirstRowRelative(!firstCell.isRowAbsolute());
		setLastRowRelative(!lastCell.isRowAbsolute());
	}

	protected AreaPtgBase(int firstRow, int lastRow, int firstColumn, int lastColumn,
			boolean firstRowRelative, boolean lastRowRelative, boolean firstColRelative, boolean lastColRelative) {

		if (lastRow > firstRow) {
			setFirstRow(firstRow);
			setLastRow(lastRow);
			setFirstRowRelative(firstRowRelative);
			setLastRowRelative(lastRowRelative);
		} else {
			setFirstRow(lastRow);
			setLastRow(firstRow);
			setFirstRowRelative(lastRowRelative);
			setLastRowRelative(firstRowRelative);
		}

		if (lastColumn > firstColumn) {
			setFirstColumn(firstColumn);
			setLastColumn(lastColumn);
			setFirstColRelative(firstColRelative);
			setLastColRelative(lastColRelative);
		} else {
			setFirstColumn(lastColumn);
			setLastColumn(firstColumn);
			setFirstColRelative(lastColRelative);
			setLastColRelative(firstColRelative);
		}
	}

	protected final void readCoordinates(LittleEndianInput in)  {
		field_1_first_row = in.readUShort();
		field_2_last_row = in.readUShort();
		field_3_first_column = in.readUShort();
		field_4_last_column = in.readUShort();
	}
	protected final void writeCoordinates(LittleEndianOutput out) {
		out.writeShort(field_1_first_row);
		out.writeShort(field_2_last_row);
		out.writeShort(field_3_first_column);
		out.writeShort(field_4_last_column);
	}

	/**
	 * @return the first row in the area
	 */
	public final int getFirstRow() {
		return field_1_first_row;
	}

	/**
	 * sets the first row
	 * @param rowIx number (0-based)
	 */
	public final void setFirstRow(int rowIx) {
		field_1_first_row = rowIx;
	}

	/**
	 * @return last row in the range (x2 in x1,y1-x2,y2)
	 */
	public final int getLastRow() {
		return field_2_last_row;
	}

	/**
	 * @param rowIx last row number in the area
	 */
	public final void setLastRow(int rowIx) {
		field_2_last_row = rowIx;
	}

	/**
	 * @return the first column number in the area.
	 */
	public final int getFirstColumn() {
		return columnMask.getValue(field_3_first_column);
	}

	/**
	 * @return the first column number + the options bit settings unstripped
	 */
	public final short getFirstColumnRaw() {
		return (short) field_3_first_column; // TODO
	}

	/**
	 * @return whether or not the first row is a relative reference or not.
	 */
	public final boolean isFirstRowRelative() {
		return rowRelative.isSet(field_3_first_column);
	}

	/**
	 * sets the first row to relative or not
	 * @param rel is relative or not.
	 */
	public final void setFirstRowRelative(boolean rel) {
		field_3_first_column=rowRelative.setBoolean(field_3_first_column,rel);
	}

	/**
	 * @return isrelative first column to relative or not
	 */
	public final boolean isFirstColRelative() {
		return colRelative.isSet(field_3_first_column);
	}

	/**
	 * set whether the first column is relative
	 */
	public final void setFirstColRelative(boolean rel) {
		field_3_first_column=colRelative.setBoolean(field_3_first_column,rel);
	}

	/**
	 * set the first column in the area
	 */
	public final void setFirstColumn(int colIx) {
		field_3_first_column=columnMask.setValue(field_3_first_column, colIx);
	}

	/**
	 * set the first column irrespective of the bitmasks
	 */
	public final void setFirstColumnRaw(int column) {
		field_3_first_column = column;
	}

	/**
	 * @return lastcolumn in the area
	 */
	public final int getLastColumn() {
		return columnMask.getValue(field_4_last_column);
	}

	/**
	 * @return last column and bitmask (the raw field)
	 */
	public final short getLastColumnRaw() {
		return (short) field_4_last_column;
	}

	/**
	 * @return last row relative or not
	 */
	public final boolean isLastRowRelative() {
		return rowRelative.isSet(field_4_last_column);
	}

	/**
	 * set whether the last row is relative or not
	 * @param rel <code>true</code> if the last row relative, else
	 * <code>false</code>
	 */
	public final void setLastRowRelative(boolean rel) {
		field_4_last_column=rowRelative.setBoolean(field_4_last_column,rel);
	}

	/**
	 * @return lastcol relative or not
	 */
	public final boolean isLastColRelative() {
		return colRelative.isSet(field_4_last_column);
	}

	/**
	 * set whether the last column should be relative or not
	 */
	public final void setLastColRelative(boolean rel) {
		field_4_last_column=colRelative.setBoolean(field_4_last_column,rel);
	}

	/**
	 * set the last column in the area
	 */
	public final void setLastColumn(int colIx) {
		field_4_last_column=columnMask.setValue(field_4_last_column, colIx);
	}

	/**
	 * set the last column irrespective of the bitmasks
	 */
	public final void setLastColumnRaw(short column) {
		field_4_last_column = column;
	}
	protected final String formatReferenceAsString() {
		CellReference topLeft = new CellReference(getFirstRow(),getFirstColumn(),!isFirstRowRelative(),!isFirstColRelative());
		CellReference botRight = new CellReference(getLastRow(),getLastColumn(),!isLastRowRelative(),!isLastColRelative());

		if(AreaReference.isWholeColumnReference(topLeft, botRight)) {
			return (new AreaReference(topLeft, botRight)).formatAsString();
		}
		return topLeft.formatAsString() + ":" + botRight.formatAsString();
	}

	public String toFormulaString() {
		return formatReferenceAsString();
	}

	public byte getDefaultOperandClass() {
		return Ptg.CLASS_REF;
	}
}
