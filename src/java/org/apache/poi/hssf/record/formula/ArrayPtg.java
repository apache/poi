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

	private static final byte[] DEFAULT_RESERVED_DATA = new byte[RESERVED_FIELD_LEN];

	// TODO - fix up field visibility and subclasses
	private final byte[] field_1_reserved;

	// data from these fields comes after the Ptg data of all tokens in current formula
	private int  token_1_columns;
	private short token_2_rows;
	private Object[] token_3_arrayValues;

	public ArrayPtg(LittleEndianInput in) {
		field_1_reserved = new byte[RESERVED_FIELD_LEN];
		// TODO - add readFully method to RecordInputStream
		for(int i=0; i< RESERVED_FIELD_LEN; i++) {
			field_1_reserved[i] = in.readByte();
		}
	}
	/**
	 * @param values2d array values arranged in rows
	 */
	public ArrayPtg(Object[][] values2d) {
		int nColumns = values2d[0].length;
		int nRows = values2d.length;
		// convert 2-d to 1-d array (row by row according to getValueIndex())
		token_1_columns = (short) nColumns;
		token_2_rows = (short) nRows;

		Object[] vv = new Object[token_1_columns * token_2_rows];
		for (int r=0; r<nRows; r++) {
			Object[] rowData = values2d[r];
			for (int c=0; c<nColumns; c++) {
				vv[getValueIndex(c, r)] = rowData[c];
			}
		}

		token_3_arrayValues = vv;
		field_1_reserved = DEFAULT_RESERVED_DATA;
	}
	/**
	 * @return 2-d array (inner index is rowIx, outer index is colIx)
	 */
	public Object[][] getTokenArrayValues() {
		if (token_3_arrayValues == null) {
			throw new IllegalStateException("array values not read yet");
		}
		Object[][] result = new Object[token_2_rows][token_1_columns];
		for (int r = 0; r < token_2_rows; r++) {
			Object[] rowData = result[r];
			for (int c = 0; c < token_1_columns; c++) {
				rowData[c] = token_3_arrayValues[getValueIndex(c, r)];
			}
		}
		return result;
	}

	public boolean isBaseToken() {
		return false;
	}

	/**
	 * Read in the actual token (array) values. This occurs
	 * AFTER the last Ptg in the expression.
	 * See page 304-305 of Excel97-2007BinaryFileFormat(xls)Specification.pdf
	 */
	public void readTokenValues(LittleEndianInput in) {
		int nColumns = in.readUByte();
		short nRows = in.readShort();
		//The token_1_columns and token_2_rows do not follow the documentation.
		//The number of physical rows and columns is actually +1 of these values.
		//Which is not explicitly documented.
		nColumns++;
		nRows++;

		token_1_columns = nColumns;
		token_2_rows = nRows;

		int totalCount = nRows * nColumns;
		token_3_arrayValues = ConstantValueParser.parse(in, totalCount);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("[ArrayPtg]\n");

		sb.append("nRows = ").append(getRowCount()).append("\n");
		sb.append("nCols = ").append(getColumnCount()).append("\n");
		if (token_3_arrayValues == null) {
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
		if(colIx < 0 || colIx >= token_1_columns) {
			throw new IllegalArgumentException("Specified colIx (" + colIx
					+ ") is outside the allowed range (0.." + (token_1_columns-1) + ")");
		}
		if(rowIx < 0 || rowIx >= token_2_rows) {
			throw new IllegalArgumentException("Specified rowIx (" + rowIx
					+ ") is outside the allowed range (0.." + (token_2_rows-1) + ")");
		}
		return rowIx * token_1_columns + colIx;
	}

	public void write(LittleEndianOutput out) {
		out.writeByte(sid + getPtgClass());
		out.write(field_1_reserved);
	}

	public int writeTokenValueBytes(LittleEndianOutput out) {

		out.writeByte(token_1_columns-1);
		out.writeShort(token_2_rows-1);
		ConstantValueParser.encode(out, token_3_arrayValues);
		return 3 + ConstantValueParser.getEncodedSize(token_3_arrayValues);
	}

	public short getRowCount() {
		return token_2_rows;
	}

	public short getColumnCount() {
		return (short)token_1_columns;
	}

	/** This size includes the size of the array Ptg plus the Array Ptg Token value size*/
	public int getSize() {
		return PLAIN_TOKEN_SIZE
			// data written after the all tokens:
			+ 1 + 2 // column, row
			+ ConstantValueParser.getEncodedSize(token_3_arrayValues);
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
		  		Object o = token_3_arrayValues[getValueIndex(x, y)];
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
}
