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

package org.apache.poi.hssf.record.formula.eval;

/**
 * @author Josh Micich
 */
public final class NameXEval implements Eval {

	/** index to REF entry in externsheet record */
	private final int _sheetRefIndex;
	/** index to defined name or externname table(1 based) */
	private final int _nameNumber;

	public NameXEval(int sheetRefIndex, int nameNumber) {
		_sheetRefIndex = sheetRefIndex;
		_nameNumber = nameNumber;
	}

	public int getSheetRefIndex() {
		return _sheetRefIndex;
	}
	public int getNameNumber() {
		return _nameNumber;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(64);
		sb.append(getClass().getName()).append(" [");
		sb.append(_sheetRefIndex).append(", ").append(_nameNumber);
		sb.append("]");
		return sb.toString();
	}
}
