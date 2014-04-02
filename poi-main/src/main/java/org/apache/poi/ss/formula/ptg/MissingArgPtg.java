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

import org.apache.poi.util.LittleEndianOutput;

/**
 * Missing Function Arguments
 * 
 * Avik Sengupta &lt;avik at apache.org&gt;
 * 
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class MissingArgPtg extends ScalarConstantPtg {

	private final static int SIZE = 1;
	public final static byte sid = 0x16;

	public static final Ptg instance = new MissingArgPtg();

	private MissingArgPtg() {
		// enforce singleton
	}

	public void write(LittleEndianOutput out) {
		out.writeByte(sid + getPtgClass());
	}

	public int getSize() {
		return SIZE;
	}

	public String toFormulaString() {
		return " ";
	}
}
