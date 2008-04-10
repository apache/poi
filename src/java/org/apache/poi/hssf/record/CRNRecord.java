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

import org.apache.poi.hssf.record.constant.ConstantValueParser;
import org.apache.poi.util.LittleEndian;

/**
 * Title:       CRN  <P>
 * Description: This record stores the contents of an external cell or cell range <P>
 * REFERENCE:  5.23<P>
 *
 * @author josh micich
 */
public final class CRNRecord extends Record {
	public final static short sid = 0x5A;

	private int	 field_1_last_column_index;
	private int	 field_2_first_column_index;
	private int	 field_3_row_index;
	private Object[] field_4_constant_values;

	public CRNRecord() {
		throw new RuntimeException("incomplete code");
	}

	public CRNRecord(RecordInputStream in) {
		super(in);
	}

	protected void validateSid(short id) {
		if (id != sid) {
			throw new RecordFormatException("NOT An XCT RECORD");
		}
	}

	public int getNumberOfCRNs() {
		return field_1_last_column_index;
	}


	protected void fillFields(RecordInputStream in) {
		field_1_last_column_index = in.readByte() & 0x00FF;
		field_2_first_column_index = in.readByte() & 0x00FF;
		field_3_row_index = in.readShort();
		int nValues = field_1_last_column_index - field_2_first_column_index + 1;
		field_4_constant_values = ConstantValueParser.parse(in, nValues);
	}


	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName()).append(" [CRN");
		sb.append(" rowIx=").append(field_3_row_index);
		sb.append(" firstColIx=").append(field_2_first_column_index);
		sb.append(" lastColIx=").append(field_1_last_column_index);
		sb.append("]");
		return sb.toString();
	}
	private int getDataSize() {
		return 4 + ConstantValueParser.getEncodedSize(field_4_constant_values);
	}

	public int serialize(int offset, byte [] data) {
		int dataSize = getDataSize();
		LittleEndian.putShort(data, 0 + offset, sid);
		LittleEndian.putShort(data, 2 + offset, (short) dataSize);
		LittleEndian.putByte(data, 4 + offset, field_1_last_column_index);
		LittleEndian.putByte(data, 5 + offset, field_2_first_column_index);
		LittleEndian.putShort(data, 6 + offset, (short) field_3_row_index);
		ConstantValueParser.encode(data, 8 + offset, field_4_constant_values);
		return getRecordSize();
	}

	public int getRecordSize() {
		return getDataSize() + 4;
	}

	/**
	 * return the non static version of the id for this record.
	 */
	public short getSid() {
		return sid;
	}
}
