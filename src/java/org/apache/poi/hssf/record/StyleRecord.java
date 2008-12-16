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

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.StringUtil;

/**
 * Title:        Style Record (0x0293)<p/>
 * Description:  Describes a builtin to the gui or user defined style<P>
 * REFERENCE:  PG 390 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author aviks : string fixes for UserDefined Style
 */
public final class StyleRecord extends StandardRecord {
	public final static short sid = 0x0293;

	private static final BitField styleIndexMask = BitFieldFactory.getInstance(0x0FFF);
	private static final BitField isBuiltinFlag  = BitFieldFactory.getInstance(0x8000);

	/** shared by both user defined and built-in styles */
	private int field_1_xf_index;

	// only for built in styles
	private int field_2_builtin_style;
	private int field_3_outline_style_level;

	// only for user defined styles
	private boolean field_3_stringHasMultibyte;
	private String field_4_name;

	/**
	 * creates a new style record, initially set to 'built-in'
	 */
	public StyleRecord() {
		field_1_xf_index = isBuiltinFlag.set(field_1_xf_index);
	}

	public StyleRecord(RecordInputStream in) {
		field_1_xf_index = in.readShort();
		if (isBuiltin()) {
			field_2_builtin_style	   = in.readByte();
			field_3_outline_style_level = in.readByte();
		} else {
			int field_2_name_length = in.readShort();
			
			if(in.remaining() < 1) {
				// Some files from Crystal Reports lack the is16BitUnicode byte
				//  the remaining fields, which is naughty
				if (field_2_name_length != 0) {
					throw new RecordFormatException("Ran out of data reading style record");
				}
				// guess this is OK if the string length is zero
				field_4_name = "";
			} else {
				
				field_3_stringHasMultibyte = in.readByte() != 0x00;
				if (field_3_stringHasMultibyte) {
					field_4_name = StringUtil.readUnicodeLE(in, field_2_name_length);
				} else {
					field_4_name = StringUtil.readCompressedUnicode(in, field_2_name_length);
				}
			}
		}
	}

	/**
	 * set the actual index of the style extended format record
	 * @param xfIndex of the xf record
	 */
	public void setXFIndex(int xfIndex) {
		field_1_xf_index = styleIndexMask.setValue(field_1_xf_index, xfIndex);
	}

	/**
	 * get the actual index of the style extended format record
	 * @see #getXFIndex() 
	 * @return index of the xf record
	 */
	public int getXFIndex() {
		return styleIndexMask.getValue(field_1_xf_index);
	}

	/**
	 * set the style's name
	 * @param name of the style
	 */
	public void setName(String name) {
		field_4_name = name;
		field_3_stringHasMultibyte = StringUtil.hasMultibyte(name);
		field_1_xf_index = isBuiltinFlag.clear(field_1_xf_index);
	}

	/**
	 * if this is a builtin style set the number of the built in style
	 * @param  builtinStyleId style number (0-7)
	 *
	 */
	public void setBuiltinStyle(int builtinStyleId) {
		field_1_xf_index = isBuiltinFlag.set(field_1_xf_index);
		field_2_builtin_style = builtinStyleId;
	}

	/**
	 * set the row or column level of the style (if builtin 1||2)
	 */
	public void setOutlineStyleLevel(int level) {
		field_3_outline_style_level = level & 0x00FF;
	}

	public boolean isBuiltin(){
		return isBuiltinFlag.isSet(field_1_xf_index);
	}

	/**
	 * get the style's name
	 * @return name of the style
	 */
	public String getName() {
		return field_4_name;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("[STYLE]\n");
		sb.append("    .xf_index_raw =").append(HexDump.shortToHex(field_1_xf_index)).append("\n");
		sb.append("        .type     =").append(isBuiltin() ? "built-in" : "user-defined").append("\n");
		sb.append("        .xf_index =").append(HexDump.shortToHex(getXFIndex())).append("\n");
		if (isBuiltin()){
			sb.append("    .builtin_style=").append(HexDump.byteToHex(field_2_builtin_style)).append("\n");
			sb.append("    .outline_level=").append(HexDump.byteToHex(field_3_outline_style_level)).append("\n");
		} else {
			 sb.append("    .name        =").append(getName()).append("\n");
		}
		sb.append("[/STYLE]\n");
		return sb.toString();
	}

	
	protected int getDataSize() {
		if (isBuiltin()) {
			return 4; // short, byte, byte
		}
		return 2 // short xf index 
			+ 3 // str len + flag 
			+ field_4_name.length() * (field_3_stringHasMultibyte ? 2 : 1);
	}

	public void serialize(LittleEndianOutput out) {
		out.writeShort(field_1_xf_index);
		if (isBuiltin()) {
			out.writeByte(field_2_builtin_style);
			out.writeByte(field_3_outline_style_level);
		} else {
			out.writeShort(field_4_name.length());
			out.writeByte(field_3_stringHasMultibyte ? 0x01 : 0x00);
			if (field_3_stringHasMultibyte) {
				StringUtil.putUnicodeLE(getName(), out);
			} else {
				StringUtil.putCompressedUnicode(getName(), out);
			}
		}
	}

	public short getSid() {
		return sid;
	}
}
