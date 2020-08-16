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

package org.apache.poi.ss.formula.ptg;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.WorkbookDependentFormula;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Deleted Area 3D Ptg - 3D referecnce (Sheet + Area)<p>
 * Defined a area in Extern Sheet.
 *
 * @version 1.0-pre
 */
public final class DeletedArea3DPtg extends OperandPtg implements WorkbookDependentFormula {
	public final static byte sid = 0x3d;
	private final int field_1_index_extern_sheet;
	private final int unused1;
	private final int unused2;

	public DeletedArea3DPtg(int externSheetIndex) {
		field_1_index_extern_sheet = externSheetIndex;
		unused1 = 0;
		unused2 = 0;
	}

	public DeletedArea3DPtg(LittleEndianInput in)  {
		field_1_index_extern_sheet = in.readUShort();
		unused1 = in.readInt();
		unused2 = in.readInt();
	}
	public String toFormulaString(FormulaRenderingWorkbook book) {
		return ExternSheetNameResolver.prependSheetName(book, field_1_index_extern_sheet, FormulaError.REF.getString());
	}
	public String toFormulaString() {
		throw new RuntimeException("3D references need a workbook to determine formula text");
	}
	public byte getDefaultOperandClass() {
		return Ptg.CLASS_REF;
	}

	@Override
	public byte getSid() {
		return sid;
	}

	public int getSize() {
		return 11;
	}

	public int getExternSheetIndex() {
		return field_1_index_extern_sheet;
	}

	public void write(LittleEndianOutput out) {
		out.writeByte(sid + getPtgClass());
		out.writeShort(field_1_index_extern_sheet);
		out.writeInt(unused1);
		out.writeInt(unused2);
	}

	@Override
	public DeletedArea3DPtg copy() {
		// immutable
		return this;
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return GenericRecordUtil.getGenericProperties(
			"externSheetIndex", this::getExternSheetIndex,
			"unused1", () -> unused1,
			"unused2", () -> unused2
		);
	}
}
