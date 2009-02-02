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

import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class MemFuncPtg extends OperandPtg {

	public final static byte sid = 0x29;
	private final int field_1_len_ref_subexpression;

	/**
	 * Creates new function pointer from a byte array usually called while
	 * reading an excel file.
	 */
	public MemFuncPtg(LittleEndianInput in)  {
		this(in.readUShort());
	}

	public MemFuncPtg(int subExprLen) {
		field_1_len_ref_subexpression = subExprLen;
	}

	public int getSize() {
		return 3;
	}

	public void write(LittleEndianOutput out) {
		out.writeByte(sid + getPtgClass());
		out.writeShort(field_1_len_ref_subexpression);
	}

	public String toFormulaString() {
		return "";
	}

	public byte getDefaultOperandClass() {
		return Ptg.CLASS_REF;
	}

	public int getNumberOfOperands() {
		return field_1_len_ref_subexpression;
	}

	public int getLenRefSubexpression() {
		return field_1_len_ref_subexpression;
	}
	@Override
	public final String toString() {
		StringBuffer sb = new StringBuffer(64);
		sb.append(getClass().getName()).append(" [len=");
		sb.append(field_1_len_ref_subexpression);
		sb.append("]");
		return sb.toString();
	}
}