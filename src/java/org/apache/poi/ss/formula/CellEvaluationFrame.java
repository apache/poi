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

package org.apache.poi.ss.formula;

/**
 * Stores the parameters that identify the evaluation of one cell.<br/>
 */
final class CellEvaluationFrame {

	private final int _sheetIndex;
	private final int _srcRowNum;
	private final int _srcColNum;
	private final int _hashCode;

	public CellEvaluationFrame(int sheetIndex, int srcRowNum, int srcColNum) {
		if (sheetIndex < 0) {
			throw new IllegalArgumentException("sheetIndex must not be negative");
		}
		_sheetIndex = sheetIndex;
		_srcRowNum = srcRowNum;
		_srcColNum = srcColNum;
		_hashCode = sheetIndex + 17 * (srcRowNum + 17 * srcColNum);
	}

	public boolean equals(Object obj) {
		CellEvaluationFrame other = (CellEvaluationFrame) obj;
		if (_sheetIndex != other._sheetIndex) {
			return false;
		}
		if (_srcRowNum != other._srcRowNum) {
			return false;
		}
		if (_srcColNum != other._srcColNum) {
			return false;
		}
		return true;
	}
	public int hashCode() {
		return _hashCode;
	}

	/**
	 * @return human readable string for debug purposes
	 */
	public String formatAsString() {
		return "R=" + _srcRowNum + " C=" + _srcColNum + " ShIx=" + _sheetIndex;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(64);
		sb.append(getClass().getName()).append(" [");
		sb.append(formatAsString());
		sb.append("]");
		return sb.toString();
	}
}