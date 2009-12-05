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
 * Boolean (boolean) Stores a (java) boolean value in a formula.
 *
 * @author Paul Krause (pkrause at soundbite dot com)
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class BoolPtg extends ScalarConstantPtg {
	public static final int SIZE = 2;
	public static final byte sid = 0x1D;

	private static final BoolPtg FALSE = new BoolPtg(false);
	private static final BoolPtg TRUE = new BoolPtg(true);

	private final boolean _value;

	private BoolPtg(boolean b) {
		_value = b;
	}

	public static BoolPtg valueOf(boolean b) {
		return b ? TRUE : FALSE;
	}
	public static BoolPtg read(LittleEndianInput in)  {
		return valueOf(in.readByte() == 1);
	}

	public boolean getValue() {
		return _value;
	}

	public void write(LittleEndianOutput out) {
		out.writeByte(sid + getPtgClass());
		out.writeByte(_value ? 1 : 0);
	}

	public int getSize() {
		return SIZE;
	}

	public String toFormulaString() {
		return _value ? "TRUE" : "FALSE";
	}
}
