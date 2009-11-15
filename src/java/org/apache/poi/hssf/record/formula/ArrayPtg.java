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

import org.apache.poi.hssf.record.constant.ConstantValueParser;
import org.apache.poi.hssf.record.constant.ErrorConstant;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * ArrayPtg - handles arrays
 *
 * The ArrayPtg is a little weird, the size of the Ptg when parsing initially only
 * includes the Ptg sid and the reserved bytes. The next Ptg in the expression then follows.
 * It is only after the "size" of all the Ptgs is met, that the ArrayPtg data is actually
 * held after this. So Ptg.createParsedExpression keeps track of the number of
 * ArrayPtg elements and need to parse the data upto the FORMULA record size.
 *
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class ArrayPtg extends Ptg {
	public static final byte sid  = 0x20;

	private static final int RESERVED_FIELD_LEN = 7;
	/**
	 * The size of the plain tArray token written within the standard formula tokens
	 * (not including the data which comes after all formula tokens)
	 */
	public static final int PLAIN_TOKEN_SIZE = 1+RESERVED_FIELD_LEN;

	// 7 bytes of data (stored as an int, short and byte here)
	private final int _reserved0Int;
	private final int _reserved1Short;
	private final int _reserved2Byte;

	// data from these fields comes after the Ptg data of all tokens in current formula
	private final int  _nColumns;
	private final int _nRows;
	private final Object[] _arrayValues;

	ArrayPtg(int reserved0, int reserved1, int reserved2, int nColumns, int nRows, Object[] arrayValues) {
		_reserved0Int = reserved0;
		_reserved1Short = reserved1;
		_reserved2Byte = reserved2;
		_nColumns = nColumns;
		_nRows = nRows;
		_arrayValues = arrayValues;
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

	public String toString() {
		StringBuffer sb = new StringBuffer("[ArrayPtg]\n");

		sb.append("nRows = ").append(getRowCount()).append("\n");
		sb.append("nCols = ").append(getColumnCount()).append("\n");
		if (_arrayValues == null) {
			sb.append("  #values#uninitialised#\n");
		} else {
			sb.append("  ").append(toFormulaString());
		}
		return sb.toString();
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

	public String toFormulaString() {
		StringBuffer b = new StringBuffer();
		b.append("{");
	  	for (int y=0;y<getRowCount();y++) {
			if (y > 0) {
				b.append(";");
			}
			for (int x=0;x<getColumnCount();x++) {
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
			return "\"" + (String)o + "\"";
		}
		if (o instanceof Double) {
			return ((Double)o).toString();
		}
		if (o instanceof Boolean) {
			return ((Boolean)o).booleanValue() ? "TRUE" : "FALSE";
		}
		if (o instanceof ErrorConstant) {
			return ((ErrorConstant)o).getText();
		}
		throw new IllegalArgumentException("Unexpected constant class (" + o.getClass().getName() + ")");
	}

	public byte getDefaultOperandClass() {
		return Ptg.CLASS_ARRAY;
	}

	/**
	 * Represents the initial plain tArray token (without the constant data that trails the whole
	 * formula).  Objects of this class are only temporary and cannot be used as {@link Ptg}s.
	 * These temporary objects get converted to {@link ArrayPtg} by the
	 * {@link #finishReading(LittleEndianInput)} method.
	 */
	static final class Initial extends Ptg {
		private final int _reserved0;
		private final int _reserved1;
		private final int _reserved2;

		public Initial(LittleEndianInput in) {
			_reserved0 = in.readInt();
			_reserved1 = in.readUShort();
			_reserved2 = in.readUByte();
		}
		private static RuntimeException invalid() {
			throw new IllegalStateException("This object is a partially initialised tArray, and cannot be used as a Ptg");
		}
		public byte getDefaultOperandClass() {
			throw invalid();
		}
		public int getSize() {
			return PLAIN_TOKEN_SIZE;
		}
		public boolean isBaseToken() {
			return false;
		}
		public String toFormulaString() {
			throw invalid();
		}
		public void write(LittleEndianOutput out) {
			throw invalid();
		}
		/**
		 * Read in the actual token (array) values. This occurs
		 * AFTER the last Ptg in the expression.
		 * See page 304-305 of Excel97-2007BinaryFileFormat(xls)Specification.pdf
		 */
		public ArrayPtg finishReading(LittleEndianInput in) {
			int nColumns = in.readUByte();
			short nRows = in.readShort();
			//The token_1_columns and token_2_rows do not follow the documentation.
			//The number of physical rows and columns is actually +1 of these values.
			//Which is not explicitly documented.
			nColumns++;
			nRows++;

			int totalCount = nRows * nColumns;
			Object[] arrayValues = ConstantValueParser.parse(in, totalCount);

			ArrayPtg result = new ArrayPtg(_reserved0, _reserved1, _reserved2, nColumns, nRows, arrayValues);
			result.setClass(getPtgClass());
			return result;
		}
	}
}
