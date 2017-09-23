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

package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.TwoDEval;
import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.AreaEvalBase;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.RefEvalBase;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.ptg.AreaI;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.Ref3DPtg;
import org.apache.poi.ss.formula.ptg.RefPtg;
import org.apache.poi.ss.util.AreaReference;

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
		AreaPtg areaPtg = new AreaPtg(new AreaReference(areaRefStr, SpreadsheetVersion.EXCEL2007));
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
		public MockAreaEval(AreaI areaPtg, ValueEval[] values) {
			super(areaPtg);
			_values = values;
		}
		private MockAreaEval(int firstRow, int firstColumn, int lastRow, int lastColumn, ValueEval[] values) {
			super(firstRow, firstColumn, lastRow, lastColumn);
			_values = values;
		}
		@Override
        public ValueEval getRelativeValue(int relativeRowIndex, int relativeColumnIndex) {
		    return getRelativeValue(-1, relativeRowIndex, relativeColumnIndex);
		}
        @Override
        public ValueEval getRelativeValue(int sheetIndex, int relativeRowIndex, int relativeColumnIndex) {
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
		@Override
        public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx, int relLastColIx) {
			if (relFirstRowIx < 0 || relFirstColIx < 0
					|| relLastRowIx >= getHeight() || relLastColIx >= getWidth()) {
				throw new RuntimeException("Operation not implemented on this mock object");
			}

			if (relFirstRowIx == 0 && relFirstColIx == 0
					&& relLastRowIx == getHeight()-1 && relLastColIx == getWidth()-1) {
				return this;
			}
			ValueEval[] values = transpose(_values, getWidth(), relFirstRowIx, relLastRowIx, relFirstColIx, relLastColIx);
			return new MockAreaEval(getFirstRow() + relFirstRowIx, getFirstColumn() + relFirstColIx,
					getFirstRow() + relLastRowIx, getFirstColumn() + relLastColIx, values);
		}
		private static ValueEval[] transpose(ValueEval[] srcValues, int srcWidth,
				int relFirstRowIx, int relLastRowIx,
				int relFirstColIx, int relLastColIx) {
			int height = relLastRowIx - relFirstRowIx + 1;
			int width = relLastColIx - relFirstColIx + 1;
			ValueEval[] result = new ValueEval[height * width];
			for (int r=0; r<height; r++) {
				int srcRowIx = r + relFirstRowIx;
				for (int c=0; c<width; c++) {
					int srcColIx = c + relFirstColIx;
					int destIx = r * width + c;
					int srcIx = srcRowIx * srcWidth + srcColIx;
					result[destIx] = srcValues[srcIx];
				}
			}
			return result;
		}
		@Override
        public TwoDEval getRow(int rowIndex) {
			if (rowIndex >= getHeight()) {
				throw new IllegalArgumentException("Invalid rowIndex " + rowIndex
						+ ".  Allowable range is (0.." + getHeight() + ").");
			}
			ValueEval[] values = new ValueEval[getWidth()];
			for (int i = 0; i < values.length; i++) {
				values[i] = getRelativeValue(rowIndex, i);
			}
			return new MockAreaEval(rowIndex, getFirstColumn(), rowIndex, getLastColumn(), values);
		}
		@Override
        public TwoDEval getColumn(int columnIndex) {
			if (columnIndex >= getWidth()) {
				throw new IllegalArgumentException("Invalid columnIndex " + columnIndex
						+ ".  Allowable range is (0.." + getWidth() + ").");
			}
			ValueEval[] values = new ValueEval[getHeight()];
			for (int i = 0; i < values.length; i++) {
				values[i] = getRelativeValue(i, columnIndex);
			}
			return new MockAreaEval(getFirstRow(), columnIndex, getLastRow(), columnIndex, values);
		}
	}

	private static final class MockRefEval extends RefEvalBase {
		private final ValueEval _value;
		public MockRefEval(RefPtg ptg, ValueEval value) {
			super(-1, -1, ptg.getRow(), ptg.getColumn());
			_value = value;
		}
		public MockRefEval(Ref3DPtg ptg, ValueEval value) {
			super(-1, -1, ptg.getRow(), ptg.getColumn());
			_value = value;
		}
		@Override
        public ValueEval getInnerValueEval(int sheetIndex) {
			return _value;
		}
		@Override
        public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx, int relLastColIx) {
			throw new RuntimeException("Operation not implemented on this mock object");
		}
	}
}
