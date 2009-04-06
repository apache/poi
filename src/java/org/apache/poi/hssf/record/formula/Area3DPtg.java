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

import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.formula.ExternSheetReferenceToken;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.WorkbookDependentFormula;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title:        Area 3D Ptg - 3D reference (Sheet + Area)<P>
 * Description:  Defined a area in Extern Sheet. <P>
 * REFERENCE:  <P>
 * @author Libin Roman (Vista Portal LDT. Developer)
 * @author avik
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class Area3DPtg extends AreaPtgBase implements WorkbookDependentFormula, ExternSheetReferenceToken {
	public final static byte sid = 0x3b;
	private final static int SIZE = 11; // 10 + 1 for Ptg
	
	private int field_1_index_extern_sheet;


	public Area3DPtg(String arearef, int externIdx) {
		super(new AreaReference(arearef));
		setExternSheetIndex(externIdx);
	}

	public Area3DPtg(LittleEndianInput in)  {
		field_1_index_extern_sheet = in.readShort();
		readCoordinates(in);
	}

	public Area3DPtg(int firstRow, int lastRow, int firstColumn, int lastColumn,
			boolean firstRowRelative, boolean lastRowRelative, boolean firstColRelative, boolean lastColRelative,
			int externalSheetIndex) {
		super(firstRow, lastRow, firstColumn, lastColumn, firstRowRelative, lastRowRelative, firstColRelative, lastColRelative);
		setExternSheetIndex(externalSheetIndex);
	}

	public Area3DPtg(AreaReference arearef, int externIdx) {
		super(arearef);
		setExternSheetIndex(externIdx);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName());
		sb.append(" [");
		sb.append("sheetIx=").append(getExternSheetIndex());
		sb.append(" ! ");
		sb.append(formatReferenceAsString());
		sb.append("]");
		return sb.toString();
	}

	public void write(LittleEndianOutput out) {
		out.writeByte(sid + getPtgClass());
		out.writeShort(field_1_index_extern_sheet);
		writeCoordinates(out);
	}

	public int getSize() {
		return SIZE;
	}

	public int getExternSheetIndex() {
		return field_1_index_extern_sheet;
	}

	public void setExternSheetIndex(int index) {
		field_1_index_extern_sheet = index;
	}

	/**
	 * @return text representation of this area reference that can be used in text
	 *  formulas. The sheet name will get properly delimited if required.
	 */
	public String toFormulaString(FormulaRenderingWorkbook book) {
		return ExternSheetNameResolver.prependSheetName(book, field_1_index_extern_sheet, formatReferenceAsString());
	}
	public String toFormulaString() {
		throw new RuntimeException("3D references need a workbook to determine formula text");
	}
}
