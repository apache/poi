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

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

public final class MemErrPtg extends OperandPtg {
	public final static short sid = 0x27;
	private final static int SIZE = 7;
	private int field_1_reserved;
	private short field_2_subex_len;

	public MemErrPtg(MemErrPtg other) {
		super(other);
		field_1_reserved = other.field_1_reserved;
		field_2_subex_len = other.field_2_subex_len;
	}

	public MemErrPtg(LittleEndianInput in)  {
		field_1_reserved = in.readInt();
		field_2_subex_len = in.readShort();
	}

	public void write(LittleEndianOutput out) {
		out.writeByte(sid + getPtgClass());
		out.writeInt(field_1_reserved);
		out.writeShort(field_2_subex_len);
	}

	@Override
	public byte getSid() {
		return sid;
	}

	public int getSize() {
		return SIZE;
	}

	public String toFormulaString() {
		return "ERR#";
	}

	public byte getDefaultOperandClass() {
		return Ptg.CLASS_VALUE;
	}

	public int getLenRefSubexpression() {
		return field_2_subex_len;
	}

	@Override
	public MemErrPtg copy() {
		return new MemErrPtg(this);
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return GenericRecordUtil.getGenericProperties("lenRefSubexpression", this::getLenRefSubexpression);
	}
}
