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

package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.hssf.record.formula.Ref3DPtg;
import org.apache.poi.hssf.record.formula.RefPtg;
import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.AreaEvalBase;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.RefEvalBase;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Test helper class for creating mock <code>Eval</code> objects
 *
 * @author Josh Micich
 */
public final class EvalFactory {

	private EvalFactory() {
		// no instances of this class
	}

	/**
	 * Creates a dummy AreaEval
	 * @param values empty (<code>null</code>) entries in this array will be converted to NumberEval.ZERO
	 */
	public static AreaEval createAreaEval(String areaRefStr, ValueEval[] values) {
		AreaPtg areaPtg = new AreaPtg(areaRefStr);
		return createAreaEval(areaPtg, values);
	}

	/**
	 * Creates a dummy AreaEval
	 * @param values empty (<code>null</code>) entries in this array will be converted to NumberEval.ZERO
	 */
	public static AreaEval createAreaEval(AreaPtg areaPtg, ValueEval[] values) {
		int nCols = areaPtg.getLastColumn() - areaPtg.getFirstColumn() + 1;
		int nRows = areaPtg.getLastRow() - areaPtg.getFirstRow() + 1;
		int nExpected = nRows * nCols;
		if (values.length != nExpected) {
			throw new RuntimeException("Expected " + nExpected + " values but got " + values.length);
		}
		for (int i = 0; i < nExpected; i++) {
			if (values[i] == null) {
				values[i] = NumberEval.ZERO;
			}
		}
		return new MockAreaEval(areaPtg, values);
	}

	/**
	 * Creates a single RefEval (with value zero)
	 */
	public static RefEval createRefEval(String refStr) {
		return createRefEval(refStr, NumberEval.ZERO);
	}
	public static RefEval createRefEval(String refStr, ValueEval value) {
		return new MockRefEval(new RefPtg(refStr), value);
	}

	private static final class MockAreaEval extends AreaEvalBase {
		private final ValueEval[] _values;
		public MockAreaEval(AreaPtg areaPtg, ValueEval[] values) {
			super(areaPtg);
			_values = values;
		}
		public ValueEval getRelativeValue(int relativeRowIndex, int relativeColumnIndex) {
			if (relativeRowIndex < 0 || relativeRowIndex >=getHeight()) {
				throw new IllegalArgumentException("row index out of range");
			}
			int width = getWidth();
			if (relativeColumnIndex < 0 || relativeColumnIndex >=width) {
				throw new IllegalArgumentException("column index out of range");
			}
			int oneDimensionalIndex = relativeRowIndex * width + relativeColumnIndex;
			return _values[oneDimensionalIndex];
		}
		public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx, int relLastColIx) {
			if (relFirstRowIx == 0 && relFirstColIx == 0
					&& relLastRowIx == getHeight()-1 && relLastColIx == getWidth()-1) {
				return this;
			}
			throw new RuntimeException("Operation not implemented on this mock object");
		}
	}

	private static final class MockRefEval extends RefEvalBase {
		private final ValueEval _value;
		public MockRefEval(RefPtg ptg, ValueEval value) {
			super(ptg.getRow(), ptg.getColumn());
			_value = value;
		}
		public MockRefEval(Ref3DPtg ptg, ValueEval value) {
			super(ptg.getRow(), ptg.getColumn());
			_value = value;
		}
		public ValueEval getInnerValueEval() {
			return _value;
		}
		public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx, int relLastColIx) {
			throw new RuntimeException("Operation not implemented on this mock object");
		}
	}
}
