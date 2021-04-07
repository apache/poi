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

package org.apache.poi.ss.formula.ptg;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.ss.formula.constant.ConstantValueParser;
import org.apache.poi.ss.formula.constant.ErrorConstant;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * ArrayPtg - handles arrays
 *
 * The ArrayPtg is a little weird, the size of the Ptg when parsing initially only
 * includes the Ptg sid and the reserved bytes. The next Ptg in the expression then follows.
 * It is only after the "size" of all the Ptgs is met, that the ArrayPtg data is actually
 * held after this. So Ptg.createParsedExpression keeps track of the number of
 * ArrayPtg elements and need to parse the data upto the FORMULA record size.
 */
public final class ArrayPtg extends Ptg {
	public static final byte sid = 0x20;

	private static final int RESERVED_FIELD_LEN = 7;
	/**
	 * The size of the plain tArray token written within the standard formula tokens
	 * (not including the data which comes after all formula tokens)
	 */
	public static final int PLAIN_TOKEN_SIZE = 1 + RESERVED_FIELD_LEN;

	// 7 bytes of data (stored as an int, short and byte here)
	private final int _reserved0Int;
	private final int _reserved1Short;
	private final int _reserved2Byte;

	// data from these fields comes after the Ptg data of all tokens in current formula
	private final int _nColumns;
	private final int _nRows;
	private final Object[] _arrayValues;

	ArrayPtg(int reserved0, int reserved1, int reserved2, int nColumns, int nRows, Object[] arrayValues) {
		_reserved0Int = reserved0;
		_reserved1Short = reserved1;
		_reserved2Byte = reserved2;
		_nColumns = nColumns;
		_nRows = nRows;
		_arrayValues = arrayValues.clone();
	}

	public ArrayPtg(ArrayPtg other) {
		_reserved0Int = other._reserved0Int;
		_reserved1Short = other._reserved1Short;
		_reserved2Byte = other._reserved2Byte;
		_nColumns = other._nColumns;
		_nRows = other._nRows;
		_arrayValues = (other._arrayValues == null) ? null : other._arrayValues.clone();
	}

	/**
	 * @param values2d array values arranged in rows
	 */
	public ArrayPtg(Object[][] values2d) {
		int nColumns = values2d[0].length;
		int nRows = values2d.length;
		// convert 2-d to 1-d array (row by row according to getValueIndex())
		_nColumns = (short) nColumns;
		_nRows = (short) nRows;

		Object[] vv = new Object[_nColumns * _nRows];
		for (int r=0; r<nRows; r++) {
			Object[] rowData = values2d[r];
			for (int c=0; c<nColumns; c++) {
				vv[getValueIndex(c, r)] = rowData[c];
			}
		}

		_arrayValues = vv;
		_reserved0Int = 0;
		_reserved1Short = 0;
		_reserved2Byte = 0;
	}
	/**
	 * @return 2-d array (inner index is rowIx, outer index is colIx)
	 */
	public Object[][] getTokenArrayValues() {
		if (_arrayValues == null) {
			throw new IllegalStateException("array values not read yet");
		}
		Object[][] result = new Object[_nRows][_nColumns];
		for (int r = 0; r < _nRows; r++) {
			Object[] rowData = result[r];
			for (int c = 0; c < _nColumns; c++) {
				rowData[c] = _arrayValues[getValueIndex(c, r)];
			}
		}
		return result;
	}

	public boolean isBaseToken() {
		return false;
	}

	/**
	 * Note - (2D) array elements are stored row by row
	 * @return the index into the internal 1D array for the specified column and row
	 */
	/* package */ int getValueIndex(int colIx, int rowIx) {
		if(colIx < 0 || colIx >= _nColumns) {
			throw new IllegalArgumentException("Specified colIx (" + colIx
					+ ") is outside the allowed range (0.." + (_nColumns-1) + ")");
		}
		if(rowIx < 0 || rowIx >= _nRows) {
			throw new IllegalArgumentException("Specified rowIx (" + rowIx
					+ ") is outside the allowed range (0.." + (_nRows-1) + ")");
		}
		return rowIx * _nColumns + colIx;
	}

	public void write(LittleEndianOutput out) {
		out.writeByte(sid + getPtgClass());
		out.writeInt(_reserved0Int);
		out.writeShort(_reserved1Short);
		out.writeByte(_reserved2Byte);
	}

	public int writeTokenValueBytes(LittleEndianOutput out) {

		out.writeByte(_nColumns-1);
		out.writeShort(_nRows-1);
		ConstantValueParser.encode(out, _arrayValues);
		return 3 + ConstantValueParser.getEncodedSize(_arrayValues);
	}

	public int getRowCount() {
		return _nRows;
	}

	public int getColumnCount() {
		return _nColumns;
	}

	/** This size includes the size of the array Ptg plus the Array Ptg Token value size*/
	public int getSize() {
		return PLAIN_TOKEN_SIZE
			// data written after the all tokens:
			+ 1 + 2 // column, row
			+ ConstantValueParser.getEncodedSize(_arrayValues);
	}

	@Override
	public byte getSid() {
		return sid;
	}

	public String toFormulaString() {
		StringBuilder b = new StringBuilder();
		b.append("{");
		for (int y = 0; y < _nRows; y++) {
			if (y > 0) {
				b.append(";");
			}
			for (int x = 0; x < _nColumns; x++) {
				if (x > 0) {
					b.append(",");
				}
				Object o = _arrayValues[getValueIndex(x, y)];
				b.append(getConstantText(o));
			}
		}
		b.append("}");
		return b.toString();
	}

	private static String getConstantText(Object o) {

		if (o == null) {
			throw new RuntimeException("Array item cannot be null");
		}
		if (o instanceof String) {
			return "\"" + o + "\"";
		}
		if (o instanceof Double) {
			return NumberToTextConverter.toText((Double) o);
		}
		if (o instanceof Boolean) {
			return (Boolean) o ? "TRUE" : "FALSE";
		}
		if (o instanceof ErrorConstant) {
			return ((ErrorConstant)o).getText();
		}
		throw new IllegalArgumentException("Unexpected constant class (" + o.getClass().getName() + ")");
	}

	public byte getDefaultOperandClass() {
		return Ptg.CLASS_ARRAY;
	}

	@Override
	public ArrayPtg copy() {
		return new ArrayPtg(this);
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return GenericRecordUtil.getGenericProperties(
			"reserved0", () -> _reserved0Int,
			"reserved1", () -> _reserved1Short,
			"reserved2", () -> _reserved2Byte,
			"columnCount", this::getColumnCount,
			"rowCount", this::getRowCount,
			"arrayValues", () -> _arrayValues == null ? "#values#uninitialised#" : toFormulaString()
		);
	}
}
