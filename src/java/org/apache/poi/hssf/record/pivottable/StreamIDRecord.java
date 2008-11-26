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

package org.apache.poi.hssf.record.pivottable;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianOutput;

/**
 * SXIDSTM - Stream ID (0x00D5)<br/>
 * 
 * @author Patrick Cheng
 */
public final class StreamIDRecord extends StandardRecord {
	public static final short sid = 0x00D5;

	private int idstm;
	
	public StreamIDRecord(RecordInputStream in) {
		idstm = in.readShort();
	}
	
	@Override
	protected void serialize(LittleEndianOutput out) {
		out.writeShort(idstm);
	}

	@Override
	protected int getDataSize() {
		return 2;
	}

	@Override
	public short getSid() {
		return sid;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("[SXIDSTM]\n");
		buffer.append("    .idstm      =").append(HexDump.shortToHex(idstm)).append('\n');

		buffer.append("[/SXIDSTM]\n");
		return buffer.toString();
	}
}
