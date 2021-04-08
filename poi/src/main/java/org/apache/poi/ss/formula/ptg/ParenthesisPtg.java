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

import org.apache.poi.util.LittleEndianOutput;

/**
 * While formula tokens are stored in RPN order and thus do not need parenthesis
 * for precedence reasons, Parenthesis tokens ARE written to ensure that user
 * entered parenthesis are displayed as-is on reading back
 */
public final class ParenthesisPtg extends ControlPtg {

	private static final int SIZE = 1;
	public static final byte sid = 0x15;

	public static final ParenthesisPtg instance = new ParenthesisPtg();

	private ParenthesisPtg() {
		// enforce singleton
	}

	public void write(LittleEndianOutput out) {
		out.writeByte(sid + getPtgClass());
	}

	@Override
	public byte getSid() {
		return sid;
	}

	public int getSize() {
		return SIZE;
	}

	public String toFormulaString() {
		return "()";
	}

	public String toFormulaString(String[] operands) {
		return "(" + operands[0] + ")";
	}

	@Override
	public ParenthesisPtg copy() {
		return instance;
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return null;
	}
}
