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

import org.apache.poi.ss.formula.constant.ConstantValueParser;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title:       CRN(0x005A)<p>
 * Description: This record stores the contents of an external cell or cell range<p>
 * REFERENCE:   OOO 5.23
 */
public final class CRNRecord extends StandardRecord {
	public final static short sid = 0x005A;

	private int	 field_1_last_column_index;
	private int	 field_2_first_column_index;
	private int	 field_3_row_index;
	private Object[] field_4_constant_values;

	public CRNRecord() {
		throw new RuntimeException("incomplete code");
	}

	public int getNumberOfCRNs() {
		return field_1_last_column_index;
	}


	public CRNRecord(RecordInputStream in) {
		field_1_last_column_index = in.readUByte();
		field_2_first_column_index = in.readUByte();
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
	protected int getDataSize() {
		return 4 + ConstantValueParser.getEncodedSize(field_4_constant_values);
	}

	public void serialize(LittleEndianOutput out) {
		out.writeByte(field_1_last_column_index);
		out.writeByte(field_2_first_column_index);
		out.writeShort(field_3_row_index);
		ConstantValueParser.encode(out, field_4_constant_values);
	}

	/**
	 * return the non static version of the id for this record.
	 */
	public short getSid() {
		return sid;
	}
}
