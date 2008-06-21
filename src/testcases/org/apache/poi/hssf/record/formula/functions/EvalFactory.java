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
import org.apache.poi.hssf.record.formula.RefPtg;
import org.apache.poi.hssf.record.formula.eval.Area2DEval;
import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.Ref2DEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Test helper class for creating mock <code>Eval</code> objects
 * 
 * @author Josh Micich
 */
final class EvalFactory {
	private static final NumberEval ZERO = new NumberEval(0);

	private EvalFactory() {
		// no instances of this class
	}

	/**
	 * Creates a dummy AreaEval (filled with zeros)
	 * <p/>
	 * nCols and nRows could have been derived
	 */
	public static AreaEval createAreaEval(String areaRefStr, int nCols, int nRows) {
		int nValues = nCols * nRows;
		ValueEval[] values = new ValueEval[nValues];
		for (int i = 0; i < nValues; i++) {
			values[i] = ZERO;
		}
		
		return new Area2DEval(new AreaPtg(areaRefStr), values);
	}

	/**
	 * Creates a single RefEval (with value zero)
	 */
	public static RefEval createRefEval(String refStr) {
		return new Ref2DEval(new RefPtg(refStr), ZERO);
	}
}
