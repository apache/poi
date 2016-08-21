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

import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * AreaErr - handles deleted cell area references.
 *
 * @author Daniel Noll (daniel at nuix dot com dot au)
 */
public final class AreaErrPtg extends OperandPtg {
	public final static byte sid = 0x2B;
	private final int unused1;
	private final int unused2;

	public AreaErrPtg() {
		unused1 = 0;
		unused2 = 0;
	}

	public AreaErrPtg(LittleEndianInput in)  {
		// 8 bytes unused:
		unused1 = in.readInt();
		unused2 = in.readInt();
	}

	public void write(LittleEndianOutput out) {
		out.writeByte(sid + getPtgClass());
		out.writeInt(unused1);
		out.writeInt(unused2);
	}

	public String toFormulaString() {
		return FormulaError.REF.getString();
	}

	public byte getDefaultOperandClass() {
		return Ptg.CLASS_REF;
	}

	public int getSize() {
		return 9;
	}
}

