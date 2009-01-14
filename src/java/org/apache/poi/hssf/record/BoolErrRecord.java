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

import org.apache.poi.ss.usermodel.ErrorConstants;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Creates new BoolErrRecord. (0x0205) <P>
 * REFERENCE:  PG ??? Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Michael P. Harhen
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class BoolErrRecord extends StandardRecord implements CellValueRecordInterface {
	public final static short sid = 0x0205;
	private int field_1_row;
	private short field_2_column;
	private short field_3_xf_index;
	private byte field_4_bBoolErr;
	private byte field_5_fError;

	/** Creates new BoolErrRecord */
	public BoolErrRecord() {
 
	}

	/**
	 * @param in the RecordInputstream to read the record from
	 */
	public BoolErrRecord(RecordInputStream in) {
		field_1_row      = in.readUShort();
		field_2_column   = in.readShort();
		field_3_xf_index = in.readShort();
		field_4_bBoolErr = in.readByte();
		field_5_fError   = in.readByte();
	}

	public void setRow(int row) {
		field_1_row = row;
	}

	public void setColumn(short col) {
		field_2_column = col;
	}

	/**
	 * set the index to the ExtendedFormat
	 * @see org.apache.poi.hssf.record.ExtendedFormatRecord
	 * @param xf    index to the XF record
	 */
	public void setXFIndex(short xf) {
		field_3_xf_index = xf;
	}

	/**
	 * set the boolean value for the cell
	 *
	 * @param value   representing the boolean value
	 */
	public void setValue(boolean value) {
		field_4_bBoolErr = value ? ( byte ) 1 : ( byte ) 0;
		field_5_fError   = ( byte ) 0;
	}

	/**
	 * set the error value for the cell
	 *
	 * @param value     error representing the error value
	 *                  this value can only be 0,7,15,23,29,36 or 42
	 *                  see bugzilla bug 16560 for an explanation
	 */
	public void setValue(byte value) {
		switch(value) {
			case ErrorConstants.ERROR_NULL:
			case ErrorConstants.ERROR_DIV_0:
			case ErrorConstants.ERROR_VALUE:
			case ErrorConstants.ERROR_REF:
			case ErrorConstants.ERROR_NAME:
			case ErrorConstants.ERROR_NUM:
			case ErrorConstants.ERROR_NA:
				field_4_bBoolErr = value;
				field_5_fError   = ( byte ) 1;
				return;
		}
		throw new IllegalArgumentException("Error Value can only be 0,7,15,23,29,36 or 42. It cannot be "+value);
	}

	public int getRow() {
		return field_1_row;
	}

	public short getColumn() {
		return field_2_column;
	}

	/**
	 * get the index to the ExtendedFormat
	 * @see org.apache.poi.hssf.record.ExtendedFormatRecord
	 * @return index to the XF record
	 */
	public short getXFIndex() {
		return field_3_xf_index;
	}

	/**
	 * get the value for the cell
	 *
	 * @return boolean representing the boolean value
	 */
	public boolean getBooleanValue() {
		return (field_4_bBoolErr != 0);
	}

	/**
	 * get the error value for the cell
	 *
	 * @return byte representing the error value
	 */
	public byte getErrorValue() {
		return field_4_bBoolErr;
	}

	/**
	 * Indicates whether the call holds a boolean value
	 *
	 * @return boolean true if the cell holds a boolean value
	 */
	public boolean isBoolean() {
		return (field_5_fError == ( byte ) 0);
	}

	/**
	 * manually indicate this is an error rather than a boolean
	 */
	public void setError(boolean val) {
		field_5_fError = (byte) (val == false ? 0 : 1);
	}

	/**
	 * Indicates whether the call holds an error value
	 *
	 * @return boolean true if the cell holds an error value
	 */

	public boolean isError() {
		return field_5_fError != 0;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("[BOOLERR]\n");
		sb.append("    .row    = ").append(HexDump.shortToHex(getRow())).append("\n");
		sb.append("    .col    = ").append(HexDump.shortToHex(getColumn())).append("\n");
		sb.append("    .xfindex= ").append(HexDump.shortToHex(getXFIndex())).append("\n");
		if (isBoolean()) {
			sb.append("    .booleanValue   = ").append(getBooleanValue()).append("\n");
		} else {
			sb.append("    .errorValue     = ").append(getErrorValue()).append("\n");
		}
		sb.append("[/BOOLERR]\n");
		return sb.toString();
	}

	public void serialize(LittleEndianOutput out) {
		out.writeShort(getRow());
		out.writeShort(getColumn());
		out.writeShort(getXFIndex());
		out.writeByte(field_4_bBoolErr);
		out.writeByte(field_5_fError);
	}

	protected int getDataSize() {
		return 8;
	}

	public short getSid()
	{
		return sid;
	}

	public Object clone() {
	  BoolErrRecord rec = new BoolErrRecord();
	  rec.field_1_row = field_1_row;
	  rec.field_2_column = field_2_column;
	  rec.field_3_xf_index = field_3_xf_index;
	  rec.field_4_bBoolErr = field_4_bBoolErr;
	  rec.field_5_fError = field_5_fError;
	  return rec;
	}
}
