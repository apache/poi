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
 * XCT - CRN Count <P>
 *
 * REFERENCE:  5.114<P>
 *
 * @author Josh Micich
 */
public final class CRNCountRecord extends StandardRecord {
	public final static short sid = 0x59;

	private static final short DATA_SIZE = 4;


	private int	 field_1_number_crn_records;
	private int	 field_2_sheet_table_index;

	public CRNCountRecord() {
		throw new RuntimeException("incomplete code");
	}

	public int getNumberOfCRNs() {
		return field_1_number_crn_records;
	}


	public CRNCountRecord(RecordInputStream in) {
		field_1_number_crn_records = in.readShort();
		if(field_1_number_crn_records < 0) {
			// TODO - seems like the sign bit of this field might be used for some other purpose
			// see example file for test case "TestBugs.test19599()"
			field_1_number_crn_records = (short)-field_1_number_crn_records;
		}
		field_2_sheet_table_index = in.readShort();
	 }


	public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName()).append(" [XCT");
        sb.append(" nCRNs=").append(field_1_number_crn_records);
        sb.append(" sheetIx=").append(field_2_sheet_table_index);
        sb.append("]");
        return sb.toString();
	}

	public void serialize(LittleEndianOutput out) {
		out.writeShort((short)field_1_number_crn_records);
		out.writeShort((short)field_2_sheet_table_index);
	}
	protected int getDataSize() {
		return DATA_SIZE;
	}

	/**
	 * return the non static version of the id for this record.
	 */
	public short getSid() {
		return sid;
	}
}
