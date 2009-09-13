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

import org.apache.poi.hssf.record.constant.ConstantValueParser;
import org.apache.poi.ss.formula.Formula;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.StringUtil;

/**
 * EXTERNALNAME (0x0023)<p/>
 *
 * @author Josh Micich
 */
public final class ExternalNameRecord extends StandardRecord {

	public final static short sid = 0x0023; // as per BIFF8. (some old versions used 0x223)

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
	private Formula  field_5_name_definition;

	/**
	 * 'rgoper' / 'Last received results of the DDE link'
	 * (seems to be only applicable to DDE links)<br/>
	 * Logically this is a 2-D array, which has been flattened into 1-D array here.
	 */
	private Object[] _ddeValues;
	/**
	 * (logical) number of columns in the {@link #_ddeValues} array
	 */
	private int _nColumns;
	/**
	 * (logical) number of rows in the {@link #_ddeValues} array
	 */
	private int _nRows;

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

	protected int getDataSize(){
		int result = 3 * 2  // 3 short fields
			+ 2 + field_4_name.length(); // nameLen and name
		if(hasFormula()) {
			result += field_5_name_definition.getEncodedSize();
		} else {
			if (_ddeValues != null) {
				result += 3; // byte, short
				result += ConstantValueParser.getEncodedSize(_ddeValues);
			}
		}
		return result;
	}

	public void serialize(LittleEndianOutput out) {
		out.writeShort(field_1_option_flag);
		out.writeShort(field_2_index);
		out.writeShort(field_3_not_used);
		int nameLen = field_4_name.length();
		out.writeShort(nameLen);
		StringUtil.putCompressedUnicode(field_4_name, out);
		if (hasFormula()) {
			field_5_name_definition.serialize(out);
		} else {
			if (_ddeValues != null) {
				out.writeByte(_nColumns-1);
				out.writeShort(_nRows-1);
				ConstantValueParser.encode(out, _ddeValues);
			}
		}
	}


	public ExternalNameRecord(RecordInputStream in) {
		field_1_option_flag = in.readShort();
		field_2_index       = in.readShort();
		field_3_not_used    = in.readShort();
		int nameLength = in.readUByte();
		int multibyteFlag = in.readUByte();
		if (multibyteFlag == 0) {
			field_4_name = in.readCompressedUnicode(nameLength);
		} else {
			field_4_name = in.readUnicodeLEString(nameLength);
		}
		if(!hasFormula()) {
			if (!isStdDocumentNameIdentifier() && !isOLELink() && isAutomaticLink()) {
				// both need to be incremented
				int nColumns = in.readUByte() + 1;
				int nRows = in.readShort() + 1;

				int totalCount = nRows * nColumns;
				_ddeValues = ConstantValueParser.parse(in, totalCount);
				_nColumns = nColumns;
				_nRows = nRows;
			}
			if(in.remaining() > 0) {
				throw readFail("Some unread data (is formula present?)");
			}
			field_5_name_definition = null;
			return;
		}
		int nBytesRemaining = in.available();
		if(nBytesRemaining <= 0) {
			throw readFail("Ran out of record data trying to read formula.");
		}
		int formulaLen = in.readUShort();
		nBytesRemaining -=2;
		field_5_name_definition = Formula.read(formulaLen, in, nBytesRemaining);
	}
	/*
	 * Makes better error messages (while hasFormula() is not reliable)
	 * Remove this when hasFormula() is stable.
	 */
	private RuntimeException readFail(String msg) {
		String fullMsg = msg + " fields: (option=" + field_1_option_flag + " index=" + field_2_index
		+ " not_used=" + field_3_not_used + " name='" + field_4_name + "')";
		return new RecordFormatException(fullMsg);
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
