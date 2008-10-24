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
 * 
 * @author andy
 * @author Jason Height (jheight at chariot dot net dot au)
 * @author Daniel Noll (daniel at nuix dot com dot au)
 */
public final class MemErrPtg extends OperandPtg {
	public final static short sid = 0x27;
	private final static int SIZE = 7;
	private int field_1_reserved;
	private short field_2_subex_len;

	public MemErrPtg(LittleEndianInput in)  {
		field_1_reserved = in.readInt();
		field_2_subex_len = in.readShort();
	}

	public void write(LittleEndianOutput out) {
		out.writeByte(sid + getPtgClass());
		out.writeInt(field_1_reserved);
		out.writeShort(field_2_subex_len);
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
}
