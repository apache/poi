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
import org.apache.poi.util.StringUtil;

/**
 * SXVD - View Fields (0x00B1)<br/>
 * 
 * @author Patrick Cheng
 */
public final class ViewFieldsRecord extends StandardRecord {
	public static final short sid = 0x00B1;

	/** the value of the <tt>cchName</tt> field when the {@link #_name} is not present */
	private static final int STRING_NOT_PRESENT_LEN = 0xFFFF;
	/** 5 shorts */
	private static final int BASE_SIZE = 10;

	private int _sxaxis;
	private int _cSub;
	private int _grbitSub;
	private int _cItm;
	
	private String _name;
	
	/**
	 * values for the {@link ViewFieldsRecord#_sxaxis} field
	 */
	private static final class Axis {
		public static final int NO_AXIS = 0;
		public static final int ROW = 1;
		public static final int COLUMN = 2;
		public static final int PAGE = 4;
		public static final int DATA = 8;
	}

	public ViewFieldsRecord(RecordInputStream in) {
		_sxaxis = in.readShort();
		_cSub = in.readShort();
		_grbitSub = in.readShort();
		_cItm = in.readShort();
		
		int cchName = in.readUShort();
		if (cchName != STRING_NOT_PRESENT_LEN) {
			int flag = in.readByte();
			if ((flag & 0x01) != 0) {
				_name = in.readUnicodeLEString(cchName);
			} else {
				_name = in.readCompressedUnicode(cchName);
			}
		}
	}
	
	@Override
	protected void serialize(LittleEndianOutput out) {
		
		out.writeShort(_sxaxis);
		out.writeShort(_cSub);
		out.writeShort(_grbitSub);
		out.writeShort(_cItm);
		
		if (_name != null) {
			StringUtil.writeUnicodeString(out, _name);
		} else {
			out.writeShort(STRING_NOT_PRESENT_LEN);
		}
	}

	@Override
	protected int getDataSize() {
		if (_name == null) {
			return BASE_SIZE;
		}
		return BASE_SIZE 
			+ 1 // unicode flag 
			+ _name.length() * (StringUtil.hasMultibyte(_name) ? 2 : 1);
	}

	@Override
	public short getSid() {
		return sid;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[SXVD]\n");
		buffer.append("    .sxaxis    = ").append(HexDump.shortToHex(_sxaxis)).append('\n');
		buffer.append("    .cSub      = ").append(HexDump.shortToHex(_cSub)).append('\n');
		buffer.append("    .grbitSub  = ").append(HexDump.shortToHex(_grbitSub)).append('\n');
		buffer.append("    .cItm      = ").append(HexDump.shortToHex(_cItm)).append('\n');
		buffer.append("    .name      = ").append(_name).append('\n');
		
		buffer.append("[/SXVD]\n");
		return buffer.toString();
	}
}
