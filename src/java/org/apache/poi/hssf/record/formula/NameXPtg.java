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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.record.RecordInputStream;

/**
 * 
 * @author aviks
 */
public final class NameXPtg extends OperandPtg {
	public final static short sid = 0x39;
	private final static int SIZE = 7;

	/** index to REF entry in externsheet record */
	private int _sheetRefIndex;
	/** index to defined name or externname table(1 based) */
	private int _nameNumber;
	/** reserved must be 0 */
	private int _reserved;

	private NameXPtg(int sheetRefIndex, int nameNumber, int reserved) {
		_sheetRefIndex = sheetRefIndex;
		_nameNumber = nameNumber;
		_reserved = reserved;
	}

	/**
	 * @param sheetRefIndex index to REF entry in externsheet record
	 * @param nameIndex index to defined name or externname table
	 */
	public NameXPtg(int sheetRefIndex, int nameIndex) {
		this(sheetRefIndex, nameIndex + 1, 0);
	}

	public NameXPtg(RecordInputStream in) {
		this(in.readUShort(), in.readUShort(), in.readUShort());
	}

	public void writeBytes(byte[] array, int offset) {
		LittleEndian.putByte(array, offset + 0, sid + getPtgClass());
		LittleEndian.putUShort(array, offset + 1, _sheetRefIndex);
		LittleEndian.putUShort(array, offset + 3, _nameNumber);
		LittleEndian.putUShort(array, offset + 5, _reserved);
	}

	public int getSize() {
		return SIZE;
	}

	public String toFormulaString(HSSFWorkbook book) {
		// -1 to convert definedNameIndex from 1-based to zero-based
		return book.resolveNameXText(_sheetRefIndex, _nameNumber - 1);
	}

	public byte getDefaultOperandClass() {
		return Ptg.CLASS_VALUE;
	}
}
