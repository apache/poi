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

import java.util.Stack;

import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * EXTERNALNAME<p/>
 * 
 * @author Josh Micich
 */
public final class ExternalNameRecord extends Record {

	private static final Ptg[] EMPTY_PTG_ARRAY = { };

	public final static short sid = 0x23; // as per BIFF8. (some old versions used 0x223)

	private static final int OPT_BUILTIN_NAME          = 0x0001;
	private static final int OPT_AUTOMATIC_LINK        = 0x0002; // m$ doc calls this fWantAdvise 
	private static final int OPT_PICTURE_LINK          = 0x0004;
	private static final int OPT_STD_DOCUMENT_NAME     = 0x0008;
	private static final int OPT_OLE_LINK              = 0x0010;
//	private static final int OPT_CLIP_FORMAT_MASK      = 0x7FE0;
	private static final int OPT_ICONIFIED_PICTURE_LINK= 0x8000;


	private short  field_1_option_flag;
	private short  field_2_index;
	private short  field_3_not_used;
	private String field_4_name;
	private Ptg[]  field_5_name_definition; // TODO - junits for name definition field

	public ExternalNameRecord(RecordInputStream in) {
		super(in);
	}

	/**
	 * Convenience Function to determine if the name is a built-in name
	 */
	public boolean isBuiltInName() {
		return (field_1_option_flag & OPT_BUILTIN_NAME) != 0;
	}
	/**
	 * For OLE and DDE, links can be either 'automatic' or 'manual'
	 */
	public boolean isAutomaticLink() {
		return (field_1_option_flag & OPT_AUTOMATIC_LINK) != 0;
	}
	/**
	 * only for OLE and DDE
	 */
	public boolean isPicureLink() {
		return (field_1_option_flag & OPT_PICTURE_LINK) != 0;
	}
	/**
	 * DDE links only. If <code>true</code>, this denotes the 'StdDocumentName'
	 */
	public boolean isStdDocumentNameIdentifier() {
		return (field_1_option_flag & OPT_STD_DOCUMENT_NAME) != 0;
	}
	public boolean isOLELink() {
		return (field_1_option_flag & OPT_OLE_LINK) != 0;
	}
	public boolean isIconifiedPictureLink() {
		return (field_1_option_flag & OPT_ICONIFIED_PICTURE_LINK) != 0;
	}
	/**
	 * @return the standard String representation of this name
	 */
	public String getText() {
		return field_4_name;
	}


	/**
	 * called by constructor, should throw runtime exception in the event of a
	 * record passed with a differing ID.
	 *
	 * @param id alleged id for this record
	 */
	protected void validateSid(short id) {
		if (id != sid) {
			throw new RecordFormatException("NOT A valid ExternalName RECORD");
		}
	}

	private int getDataSize(){
		int result = 3 * 2  // 3 short fields
			+ 2 + field_4_name.length(); // nameLen and name
		if(hasFormula()) {
			result += 2 + getNameDefinitionSize(); // nameDefLen and nameDef
		}
		return result;
	}

	/**
	 * called by the class that is responsible for writing this sucker.
	 * Subclasses should implement this so that their data is passed back in a
	 * byte array.
	 *
	 * @param offset to begin writing at
	 * @param data byte array containing instance data
	 * @return number of bytes written
	 */
	public int serialize( int offset, byte[] data ) {
		int dataSize = getDataSize();

		LittleEndian.putShort( data, 0 + offset, sid );
		LittleEndian.putShort( data, 2 + offset, (short) dataSize );
		LittleEndian.putShort( data, 4 + offset, field_1_option_flag );
		LittleEndian.putShort( data, 6 + offset, field_2_index );
		LittleEndian.putShort( data, 8 + offset, field_3_not_used );
		short nameLen = (short) field_4_name.length();
		LittleEndian.putShort( data, 10 + offset, nameLen );
		StringUtil.putCompressedUnicode( field_4_name, data, 12 + offset );
		if(hasFormula()) {
			short defLen = (short) getNameDefinitionSize();
			LittleEndian.putShort( data, 12 + nameLen + offset, defLen );
			Ptg.serializePtgStack(toStack(field_5_name_definition), data, 14 + nameLen + offset );
		}
		return dataSize + 4;
	}

	private int getNameDefinitionSize() {
		int result = 0;
		for (int i = 0; i < field_5_name_definition.length; i++) {
			result += field_5_name_definition[i].getSize();
		}
		return result;
	}


	public int getRecordSize(){
		return 4 + getDataSize();
	}


	protected void fillFields(RecordInputStream in) {
		field_1_option_flag = in.readShort();
		field_2_index       = in.readShort();
		field_3_not_used    = in.readShort();
		short nameLength    = in.readShort();
		field_4_name = in.readCompressedUnicode(nameLength);
		if(!hasFormula()) {
			if(in.remaining() > 0) {
				throw readFail("Some unread data (is formula present?)");
			}
			field_5_name_definition = EMPTY_PTG_ARRAY;
			return;
		}
		if(in.remaining() <= 0) {
			throw readFail("Ran out of record data trying to read formula.");
		}
		short formulaLen = in.readShort();
		field_5_name_definition = toPtgArray(Ptg.createParsedExpressionTokens(formulaLen, in));
	}
	/*
	 * Makes better error messages (while hasFormula() is not reliable) 
	 * Remove this when hasFormula() is stable.
	 */
	private RuntimeException readFail(String msg) {
		String fullMsg = msg + " fields: (option=" + field_1_option_flag + " index=" + field_2_index 
		+ " not_used=" + field_3_not_used + " name='" + field_4_name + "')";
		return new RuntimeException(fullMsg);
	}

	private boolean hasFormula() {
		// TODO - determine exact conditions when formula is present
		if (false) {
			// "Microsoft Office Excel 97-2007 Binary File Format (.xls) Specification"
			// m$'s document suggests logic like this, but bugzilla 44774 att 21790 seems to disagree
			if (isStdDocumentNameIdentifier()) {
				if (isOLELink()) {
					// seems to be not possible according to m$ document
					throw new IllegalStateException(
							"flags (std-doc-name and ole-link) cannot be true at the same time");
				}
				return false;
			}
			if (isOLELink()) {
				return false;
			}
			return true;
		}

		// This was derived by trial and error, but doesn't seem quite right
		if (isAutomaticLink()) {
			return false;
		}
		return true;
	}

	private static Ptg[] toPtgArray(Stack s) {
		Ptg[] result = new Ptg[s.size()];
		s.toArray(result);
		return result;
	}
	private static Stack toStack(Ptg[] ptgs) {
		Stack result = new Stack();
		for (int i = 0; i < ptgs.length; i++) {
			result.push(ptgs[i]);
		}
		return result;
	}

	public short getSid() {
		return sid;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName()).append(" [EXTERNALNAME ");
		sb.append(" ").append(field_4_name);
		sb.append(" ix=").append(field_2_index);
		sb.append("]");
		return sb.toString();
	}
}
