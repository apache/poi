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

package org.apache.poi.hssf.record.formula;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.usermodel.HSSFErrorConstants;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.LittleEndian;

/**
 * Title:        Deleted Area 3D Ptg - 3D referecnce (Sheet + Area)<P>
 * Description:  Defined a area in Extern Sheet. <P>
 * REFERENCE:  <P>
 * @author Patrick Luby
 * @version 1.0-pre
 */
public final class DeletedArea3DPtg extends OperandPtg {
	public final static byte sid = 0x3d;
	private final int field_1_index_extern_sheet;
	private final int unused1;
	private final int unused2;

	public DeletedArea3DPtg( RecordInputStream in) {
		field_1_index_extern_sheet = in.readUShort();
		unused1 = in.readInt();
		unused2 = in.readInt();
	}
	public String toFormulaString(HSSFWorkbook book) {
		return HSSFErrorConstants.getText(HSSFErrorConstants.ERROR_REF);
	}
	public byte getDefaultOperandClass() {
		return Ptg.CLASS_REF;
	}
	public int getSize() {
		return 11;
	}
	public void writeBytes(byte[] data, int offset) {
		LittleEndian.putByte(data, 0 + offset, sid + getPtgClass());
		LittleEndian.putUShort(data, 1 + offset, field_1_index_extern_sheet);
		LittleEndian.putInt(data, 3 + offset, unused1);
		LittleEndian.putInt(data, 7 + offset, unused2);
	}
}
