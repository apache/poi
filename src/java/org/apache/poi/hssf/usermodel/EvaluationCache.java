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

package org.apache.poi.hssf.usermodel;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Performance optimisation for {@link HSSFFormulaEvaluator}. This class stores previously
 * calculated values of already visited cells, to avoid unnecessary re-calculation when the 
 * same cells are referenced multiple times
 * 
 * 
 * @author Josh Micich
 */
final class EvaluationCache {
	private static final class Key {

		private final int _sheetIndex;
		private final int _srcRowNum;
		private final int _srcColNum;
		private final int _hashCode;

		public Key(int sheetIndex, int srcRowNum, int srcColNum) {
			_sheetIndex = sheetIndex;
			_srcRowNum = srcRowNum;
			_srcColNum = srcColNum;
			_hashCode = sheetIndex + srcRowNum + srcColNum;
		}

		public int hashCode() {
			return _hashCode;
		}

		public boolean equals(Object obj) {
			Key other = (Key) obj;
			if (_hashCode != other._hashCode) {
				return false;
			}
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
	}

	private final Map _valuesByKey;

	/* package */EvaluationCache() {
		_valuesByKey = new HashMap();
	}

	public ValueEval getValue(int sheetIndex, int srcRowNum, int srcColNum) {
		Key key = new Key(sheetIndex, srcRowNum, srcColNum);
		return (ValueEval) _valuesByKey.get(key);
	}

	public void setValue(int sheetIndex, int srcRowNum, int srcColNum, ValueEval value) {
		Key key = new Key(sheetIndex, srcRowNum, srcColNum);
		if (_valuesByKey.containsKey(key)) {
			throw new RuntimeException("Already have cached value for this cell");
		}
		_valuesByKey.put(key, value);
	}

	/**
	 * Should be called whenever there are changes to input cells in the evaluated workbook.
	 */
	public void clear() {
		_valuesByKey.clear();
	}
}
