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
import org.apache.poi.util.StringUtil;

/**
 * NOTE: Comment Associated with a Cell (0x001C)<p/>
 *
 * @author Yegor Kozlov
 */
public final class NoteRecord extends StandardRecord {
	public final static short sid = 0x001C;

	public static final NoteRecord[] EMPTY_ARRAY = { };

	/**
	 * Flag indicating that the comment is hidden (default)
	 */
	public final static short NOTE_HIDDEN = 0x0;

	/**
	 * Flag indicating that the comment is visible
	 */
	public final static short NOTE_VISIBLE = 0x2;

	private static final Byte DEFAULT_PADDING = Byte.valueOf((byte)0);

	private int field_1_row;
	private int field_2_col;
	private short field_3_flags;
	private int field_4_shapeid;
	private boolean field_5_hasMultibyte;
	private String field_6_author;
	/**
	 * Saves padding byte value to reduce delta during round-trip serialization.<br/>
	 *
	 * The documentation is not clear about how padding should work.  In any case
	 * Excel(2007) does something different.
	 */
	private Byte field_7_padding;

	/**
	 * Construct a new <code>NoteRecord</code> and
	 * fill its data with the default values
	 */
	public NoteRecord() {
		field_6_author = "";
		field_3_flags = 0;
		field_7_padding = DEFAULT_PADDING; // seems to be always present regardless of author text
	}

	/**
	 * @return id of this record.
	 */
	public short getSid() {
		return sid;
	}

	/**
	 * Read the record data from the supplied <code>RecordInputStream</code>
	 */
	public NoteRecord(RecordInputStream in) {
		field_1_row = in.readUShort();
		field_2_col = in.readShort();
		field_3_flags = in.readShort();
		field_4_shapeid = in.readUShort();
		int length = in.readShort();
		field_5_hasMultibyte = in.readByte() != 0x00;
		if (field_5_hasMultibyte) {
			field_6_author = StringUtil.readUnicodeLE(in, length);
		} else {
			field_6_author = StringUtil.readCompressedUnicode(in, length);
		}
 		if (in.available() == 1) {
			field_7_padding = Byte.valueOf(in.readByte());
		}
	}

	public void serialize(LittleEndianOutput out) {
		out.writeShort(field_1_row);
		out.writeShort(field_2_col);
		out.writeShort(field_3_flags);
		out.writeShort(field_4_shapeid);
		out.writeShort(field_6_author.length());
		out.writeByte(field_5_hasMultibyte ? 0x01 : 0x00);
		if (field_5_hasMultibyte) {
			StringUtil.putUnicodeLE(field_6_author, out);
		} else {
			StringUtil.putCompressedUnicode(field_6_author, out);
		}
		if (field_7_padding != null) {
			out.writeByte(field_7_padding.intValue());
		}
	}

	protected int getDataSize() {
		return 11 // 5 shorts + 1 byte
			+ field_6_author.length() * (field_5_hasMultibyte ? 2 : 1)
			+ (field_7_padding == null ? 0 : 1);
	}

	/**
	 * Convert this record to string.
	 * Used by BiffViewer and other utilities.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("[NOTE]\n");
		buffer.append("    .row    = ").append(field_1_row).append("\n");
		buffer.append("    .col    = ").append(field_2_col).append("\n");
		buffer.append("    .flags  = ").append(field_3_flags).append("\n");
		buffer.append("    .shapeid= ").append(field_4_shapeid).append("\n");
		buffer.append("    .author = ").append(field_6_author).append("\n");
		buffer.append("[/NOTE]\n");
		return buffer.toString();
	}

	/**
	 * Return the row that contains the comment
	 *
	 * @return the row that contains the comment
	 */
	public int getRow() {
		return field_1_row;
	}

	/**
	 * Specify the row that contains the comment
	 *
	 * @param row the row that contains the comment
	 */
	public void setRow(int row) {
		field_1_row = row;
	}

	/**
	 * Return the column that contains the comment
	 *
	 * @return the column that contains the comment
	 */
	public int getColumn() {
		return field_2_col;
	}

	/**
	 * Specify the column that contains the comment
	 *
	 * @param col the column that contains the comment
	 */
	public void setColumn(int col) {
		field_2_col = col;
	}

	/**
	 * Options flags.
	 *
	 * @return the options flag
	 * @see #NOTE_VISIBLE
	 * @see #NOTE_HIDDEN
	 */
	public short getFlags() {
		return field_3_flags;
	}

	/**
	 * Options flag
	 *
	 * @param flags the options flag
	 * @see #NOTE_VISIBLE
	 * @see #NOTE_HIDDEN
	 */
	public void setFlags(short flags) {
		field_3_flags = flags;
	}
	
	/**
	 * For unit testing only!
	 */
	protected boolean authorIsMultibyte() {
	   return field_5_hasMultibyte;
	}

	/**
	 * Object id for OBJ record that contains the comment
	 */
	public int getShapeId() {
		return field_4_shapeid;
	}

	/**
	 * Object id for OBJ record that contains the comment
	 */
	public void setShapeId(int id) {
		field_4_shapeid = id;
	}

	/**
	 * Name of the original comment author
	 *
	 * @return the name of the original author of the comment
	 */
	public String getAuthor() {
		return field_6_author;
	}

	/**
	 * Name of the original comment author
	 *
	 * @param author the name of the original author of the comment
	 */
	public void setAuthor(String author) {
		field_6_author = author;
      field_5_hasMultibyte = StringUtil.hasMultibyte(author);
	}

	public Object clone() {
		NoteRecord rec = new NoteRecord();
		rec.field_1_row = field_1_row;
		rec.field_2_col = field_2_col;
		rec.field_3_flags = field_3_flags;
		rec.field_4_shapeid = field_4_shapeid;
		rec.field_6_author = field_6_author;
		return rec;
	}
}
