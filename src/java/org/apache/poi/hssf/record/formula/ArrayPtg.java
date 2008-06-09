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

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.UnicodeString;
import org.apache.poi.hssf.record.constant.ConstantValueParser;
import org.apache.poi.hssf.record.constant.ErrorConstant;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.LittleEndian;

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
	// TODO - fix up field visibility and subclasses
	private byte[] field_1_reserved;
	
	// data from these fields comes after the Ptg data of all tokens in current formula
	private short  token_1_columns;
	private short token_2_rows;
	private Object[] token_3_arrayValues;

	public ArrayPtg(RecordInputStream in) {
		field_1_reserved = new byte[RESERVED_FIELD_LEN];
		// TODO - add readFully method to RecordInputStream
		for(int i=0; i< RESERVED_FIELD_LEN; i++) {
			field_1_reserved[i] = in.readByte();
		}
	}
	public Object[] getTokenArrayValues() {
		return (Object[]) token_3_arrayValues.clone();
	}
	
	public boolean isBaseToken() {
		return false;
	}
	
	/** 
	 * Read in the actual token (array) values. This occurs 
	 * AFTER the last Ptg in the expression.
	 * See page 304-305 of Excel97-2007BinaryFileFormat(xls)Specification.pdf
	 */
	public void readTokenValues(RecordInputStream in) {
		short nColumns = in.readUByte();
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

	public String toString()
	{
		StringBuffer buffer = new StringBuffer("[ArrayPtg]\n");

		buffer.append("columns = ").append(getColumnCount()).append("\n");
		buffer.append("rows = ").append(getRowCount()).append("\n");
		for (int x=0;x<getColumnCount();x++) {
			for (int y=0;y<getRowCount();y++) {
				Object o = token_3_arrayValues[getValueIndex(x, y)];
	   			buffer.append("[").append(x).append("][").append(y).append("] = ").append(o).append("\n"); 
			}
		}
		return buffer.toString();
	}

	/**
	 * Note - (2D) array elements are stored column by column 
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
		return rowIx + token_2_rows * colIx;
	}

	public void writeBytes(byte[] data, int offset) {
		
		LittleEndian.putByte(data, offset + 0, sid + getPtgClass());
		System.arraycopy(field_1_reserved, 0, data, offset+1, RESERVED_FIELD_LEN);
	}

	public int writeTokenValueBytes(byte[] data, int offset) {

		LittleEndian.putByte(data,  offset + 0, token_1_columns-1);
		LittleEndian.putShort(data, offset + 1, (short)(token_2_rows-1));
		ConstantValueParser.encode(data, offset + 3, token_3_arrayValues);
		return 3 + ConstantValueParser.getEncodedSize(token_3_arrayValues);
	}

	public short getRowCount() {
		return token_2_rows;
	}

	public short getColumnCount() {
		return token_1_columns;
	}

	/** This size includes the size of the array Ptg plus the Array Ptg Token value size*/
	public int getSize()
	{
		int size = 1+7+1+2;
		size += ConstantValueParser.getEncodedSize(token_3_arrayValues);
		return size;
	}

	public String toFormulaString(Workbook book)
	{
		StringBuffer b = new StringBuffer();
		b.append("{");
		for (int x=0;x<getColumnCount();x++) {
		  	if (x > 0) {
				b.append(";");
			}
		  	for (int y=0;y<getRowCount();y++) {
				if (y > 0) {
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
			return ""; // TODO - how is 'empty value' represented in formulas?
		}
		if (o instanceof UnicodeString) {
			return "\"" + ((UnicodeString)o).getString() + "\"";
		}
		if (o instanceof Double) {
			return ((Double)o).toString();
		}
		if (o instanceof Boolean) {
			((Boolean)o).toString();
		}
		if (o instanceof ErrorConstant) {
			return ((ErrorConstant)o).getText();
		}
		throw new IllegalArgumentException("Unexpected constant class (" + o.getClass().getName() + ")");
	}
	
	public byte getDefaultOperandClass() {
		return Ptg.CLASS_ARRAY;
	}
	
	public Object clone() {
	  ArrayPtg ptg = (ArrayPtg) super.clone();
	  ptg.field_1_reserved = (byte[]) field_1_reserved.clone();
	  ptg.token_3_arrayValues = (Object[]) token_3_arrayValues.clone();
	  return ptg;
	}
}
