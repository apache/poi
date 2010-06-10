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
	private static final int OPT_STD_DOCUMENT_NAME     = 0x0008; //fOle
	private static final int OPT_OLE_LINK              = 0x0010; //fOleLink
//	private static final int OPT_CLIP_FORMAT_MASK      = 0x7FE0;
	private static final int OPT_ICONIFIED_PICTURE_LINK= 0x8000;


	private short  field_1_option_flag;
	private short  field_2_ixals;
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
	
	/**
	 * If this is a local name, then this is the (1 based)
	 *  index of the name of the Sheet this refers to, as
	 *  defined in the preceeding {@link SupBookRecord}.
	 * If it isn't a local name, then it must be zero.
	 */
	public short getIx() {
	   return field_2_ixals;
	}

	protected int getDataSize(){
		int result = 2 + 4;  // short and int
        result += StringUtil.getEncodedSize(field_4_name) - 1; //size is byte, not short 

        if(!isOLELink() && !isStdDocumentNameIdentifier()){
            if(isAutomaticLink()){
                result += 3; // byte, short
                result += ConstantValueParser.getEncodedSize(_ddeValues);
            } else {
                result += field_5_name_definition.getEncodedSize();
            }
        }
		return result;
	}

	public void serialize(LittleEndianOutput out) {
		out.writeShort(field_1_option_flag);
		out.writeShort(field_2_ixals);
		out.writeShort(field_3_not_used);

		out.writeByte(field_4_name.length());
		StringUtil.writeUnicodeStringFlagAndData(out, field_4_name);

        if(!isOLELink() && !isStdDocumentNameIdentifier()){
            if(isAutomaticLink()){
                out.writeByte(_nColumns-1);
                out.writeShort(_nRows-1);
                ConstantValueParser.encode(out, _ddeValues);
            } else {
                field_5_name_definition.serialize(out);
            }
        }
	}


	public ExternalNameRecord(RecordInputStream in) {
		field_1_option_flag = in.readShort();
		field_2_ixals       = in.readShort();
      field_3_not_used    = in.readShort();

        int numChars = in.readUByte();
        field_4_name = StringUtil.readUnicodeString(in, numChars);

        // the record body can take different forms.
        // The form is dictated by the values of 3-th and 4-th bits in field_1_option_flag
        if(!isOLELink() && !isStdDocumentNameIdentifier()){
            // another switch: the fWantAdvise bit specifies whether the body describes
            // an external defined name or a DDE data item
            if(isAutomaticLink()){
                //body specifies DDE data item
                int nColumns = in.readUByte() + 1;
                int nRows = in.readShort() + 1;

                int totalCount = nRows * nColumns;
                _ddeValues = ConstantValueParser.parse(in, totalCount);
                _nColumns = nColumns;
                _nRows = nRows;
            } else {
                //body specifies an external defined name
                int formulaLen = in.readUShort();
                field_5_name_definition = Formula.read(formulaLen, in);
            }
        }
    }

	public short getSid() {
		return sid;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[EXTERNALNAME]\n");
		sb.append("    .ix      = ").append(field_2_ixals).append("\n");
		sb.append("    .name    = ").append(field_4_name).append("\n");
		if(field_5_name_definition != null) {
		sb.append("    .formula = ").append(field_5_name_definition).append("\n");
		}
		sb.append("[/EXTERNALNAME]\n");
		return sb.toString();
	}
}
