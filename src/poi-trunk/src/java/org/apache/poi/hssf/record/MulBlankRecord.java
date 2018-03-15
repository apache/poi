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

import org.apache.poi.util.LittleEndianOutput;

/**
 * Title:        Multiple Blank cell record(0x00BE)<p>
 * Description:  Represents a  set of columns in a row with no value but with styling.<p>
 *
 * REFERENCE:  PG 329 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)
 * 
 * @see BlankRecord
 */
public final class MulBlankRecord extends StandardRecord {
	public final static short sid = 0x00BE;

	private final int _row;
	private final int _firstCol;
	private final short[] _xfs;
	private final int _lastCol;

	public MulBlankRecord(int row, int firstCol, short[] xfs) {
		_row = row;
		_firstCol = firstCol;
		_xfs = xfs;
		_lastCol = firstCol + xfs.length - 1;
	}

	/**
	 * @return the row number of the cells this represents
	 */
	public int getRow() {
		return _row;
	}

	/**
	 * @return starting column (first cell this holds in the row). Zero based
	 */
	public int getFirstColumn() {
		return _firstCol;
	}
	
	/**
	 * @return ending column (last cell this holds in the row). Zero based
	 */
	public int getLastColumn() {
		return _lastCol;
	}

	/**
	 * get the number of columns this contains (last-first +1)
	 * @return number of columns (last - first +1)
	 */
	public int getNumColumns() {
		return _lastCol - _firstCol + 1;
	}

	/**
	 * returns the xf index for column (coffset = column - field_2_first_col)
	 * @param coffset  the column (coffset = column - field_2_first_col)
	 * @return the XF index for the column
	 */
	public short getXFAt(int coffset) {
		return _xfs[coffset];
	}

	/**
	 * @param in the RecordInputstream to read the record from
	 */
	public MulBlankRecord(RecordInputStream in) {
		_row	   = in.readUShort();
		_firstCol = in.readShort();
		_xfs	   = parseXFs(in);
		_lastCol  = in.readShort();
	}

	private static short [] parseXFs(RecordInputStream in) {
		short[] retval = new short[(in.remaining() - 2) / 2];

		for (int idx = 0; idx < retval.length;idx++) {
		  retval[idx] = in.readShort();
		}
		return retval;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("[MULBLANK]\n");
		buffer.append("row  = ").append(Integer.toHexString(getRow())).append("\n");
		buffer.append("firstcol  = ").append(Integer.toHexString(getFirstColumn())).append("\n");
		buffer.append(" lastcol  = ").append(Integer.toHexString(_lastCol)).append("\n");
		for (int k = 0; k < getNumColumns(); k++) {
			buffer.append("xf").append(k).append("		= ").append(
					Integer.toHexString(getXFAt(k))).append("\n");
		}
		buffer.append("[/MULBLANK]\n");
		return buffer.toString();
	}

	public short getSid() {
		return sid;
	}

	public void serialize(LittleEndianOutput out) {
		out.writeShort(_row);
		out.writeShort(_firstCol);
		int nItems = _xfs.length;
		for (int i = 0; i < nItems; i++) {
			out.writeShort(_xfs[i]);
		}
		out.writeShort(_lastCol);
	}

	protected int getDataSize() {
		// 3 short fields + array of shorts
		return 6 + _xfs.length * 2;
	}

	@Override
	public MulBlankRecord clone() {
		// immutable - so OK to return this
		return this;
	}
}
