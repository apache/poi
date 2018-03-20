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
import org.apache.poi.util.LittleEndianOutput;

/**
 * CATLAB - Category Labels (0x0856)<br>
 * 
 * @author Patrick Cheng
 */
public final class CatLabRecord extends StandardRecord {
	public static final short sid = 0x0856;
	
	private short rt;
	private short grbitFrt;
	private short wOffset;
	private short at;
	private short grbit;
	private Short unused;
	
	public CatLabRecord(RecordInputStream in) {
		rt = in.readShort();
		grbitFrt = in.readShort();
		wOffset = in.readShort();
		at = in.readShort();
		grbit = in.readShort();
		
		// Often, but not always has an unused short at the end
		if(in.available() == 0) {
			unused = null;
		} else {
			unused = in.readShort();
		}
	}
	
	@Override
	protected int getDataSize() {
		return 2 + 2 + 2 + 2 + 2 + (unused==null? 0:2);
	}

	@Override
	public short getSid() {
		return sid;
	}

	@Override
	public void serialize(LittleEndianOutput out) {
		out.writeShort(rt);
		out.writeShort(grbitFrt);
		out.writeShort(wOffset);
		out.writeShort(at);
		out.writeShort(grbit);
		if(unused != null)
			out.writeShort(unused);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("[CATLAB]\n");
		buffer.append("    .rt      =").append(HexDump.shortToHex(rt)).append('\n');
		buffer.append("    .grbitFrt=").append(HexDump.shortToHex(grbitFrt)).append('\n');
		buffer.append("    .wOffset =").append(HexDump.shortToHex(wOffset)).append('\n');
		buffer.append("    .at      =").append(HexDump.shortToHex(at)).append('\n');
		buffer.append("    .grbit   =").append(HexDump.shortToHex(grbit)).append('\n');
		if(unused != null)
			buffer.append("    .unused  =").append(HexDump.shortToHex(unused)).append('\n');

		buffer.append("[/CATLAB]\n");
		return buffer.toString();
	}
}
