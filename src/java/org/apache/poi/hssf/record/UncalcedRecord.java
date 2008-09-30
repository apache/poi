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

import org.apache.poi.util.LittleEndian;

/**
 * Title: Uncalced Record
 * <P>
 * If this record occurs in the Worksheet Substream, it indicates that the formulas have not 
 * been recalculated before the document was saved.
 * 
 * @author Olivier Leprince
 */

public class UncalcedRecord extends Record 
{
	public final static short sid = 0x5E;

	/**
	 * Default constructor
	 */
	public UncalcedRecord() {
	}
	/**
	 * read constructor
	 */
	public UncalcedRecord(RecordInputStream in) {
		super(in);
	}

	public short getSid() {
		return sid;
	}

	protected void fillFields(RecordInputStream in) {
		short unused = in.readShort();
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[UNCALCED]\n");
		buffer.append("[/UNCALCED]\n");
		return buffer.toString();
	}

	public int serialize(int offset, byte[] data) {
		LittleEndian.putShort(data, 0 + offset, sid);
		LittleEndian.putShort(data, 2 + offset, (short) 2);
		LittleEndian.putShort(data, 4 + offset, (short) 0); // unused
		return getRecordSize();
	}

	public int getRecordSize() {
		return UncalcedRecord.getStaticRecordSize();
	}

	public static int getStaticRecordSize() {
		return 6;
	}
}
