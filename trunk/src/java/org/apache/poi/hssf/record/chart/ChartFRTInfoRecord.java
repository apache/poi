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

package org.apache.poi.hssf.record.chart;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * CHARTFRTINFO - Chart Future Record Type Info (0x0850)
 */
public final class ChartFRTInfoRecord extends StandardRecord {
	public static final short sid = 0x850;

	private short rt;
	private short grbitFrt;
	private byte verOriginator;
	private byte verWriter;
	private CFRTID[] rgCFRTID;

	private static final class CFRTID {
		public static final int ENCODED_SIZE = 4;
		private int rtFirst;
		private int rtLast;

		public CFRTID(LittleEndianInput in) {
			rtFirst = in.readShort();
			rtLast = in.readShort();
		}

		public void serialize(LittleEndianOutput out) {
			out.writeShort(rtFirst);
			out.writeShort(rtLast);
		}
	}

	public ChartFRTInfoRecord(RecordInputStream in) {
		rt = in.readShort();
		grbitFrt = in.readShort();
		verOriginator = in.readByte();
		verWriter = in.readByte();
		int cCFRTID = in.readShort();

		rgCFRTID = new CFRTID[cCFRTID];
		for (int i = 0; i < cCFRTID; i++) {
			rgCFRTID[i] = new CFRTID(in);
		}
	}

	@Override
	protected int getDataSize() {
		return 2 + 2 + 1 + 1 + 2 + rgCFRTID.length * CFRTID.ENCODED_SIZE;
	}

	@Override
	public short getSid() {
		return sid;
	}

	@Override
	public void serialize(LittleEndianOutput out) {

		out.writeShort(rt);
		out.writeShort(grbitFrt);
		out.writeByte(verOriginator);
		out.writeByte(verWriter);
		int nCFRTIDs = rgCFRTID.length;
		out.writeShort(nCFRTIDs);

		for (int i = 0; i < nCFRTIDs; i++) {
			rgCFRTID[i].serialize(out);
		}
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("[CHARTFRTINFO]\n");
		buffer.append("    .rt           =").append(HexDump.shortToHex(rt)).append('\n');
		buffer.append("    .grbitFrt     =").append(HexDump.shortToHex(grbitFrt)).append('\n');
		buffer.append("    .verOriginator=").append(HexDump.byteToHex(verOriginator)).append('\n');
		buffer.append("    .verWriter    =").append(HexDump.byteToHex(verOriginator)).append('\n');
		buffer.append("    .nCFRTIDs     =").append(HexDump.shortToHex(rgCFRTID.length)).append('\n');
		buffer.append("[/CHARTFRTINFO]\n");
		return buffer.toString();
	}
}
