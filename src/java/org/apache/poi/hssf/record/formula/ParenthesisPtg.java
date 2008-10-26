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

import org.apache.poi.util.LittleEndianOutput;

/**
 * While formula tokens are stored in RPN order and thus do not need parenthesis
 * for precedence reasons, Parenthesis tokens ARE written to ensure that user
 * entered parenthesis are displayed as-is on reading back
 * 
 * Avik Sengupta &lt;lists@aviksengupta.com&gt; Andrew C. Oliver (acoliver at
 * apache dot org)
 * 
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class ParenthesisPtg extends ControlPtg {

	private final static int SIZE = 1;
	public final static byte sid = 0x15;

	public static final ControlPtg instance = new ParenthesisPtg();

	private ParenthesisPtg() {
		// enforce singleton
	}

	public void write(LittleEndianOutput out) {
		out.writeByte(sid + getPtgClass());
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
}
