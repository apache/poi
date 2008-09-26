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

import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.AreaErrPtg;
import org.apache.poi.hssf.record.formula.AttrPtg;
import org.apache.poi.hssf.record.formula.DeletedArea3DPtg;
import org.apache.poi.hssf.record.formula.DeletedRef3DPtg;
import org.apache.poi.hssf.record.formula.IntPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.RefErrorPtg;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Tests {@link WorkbookEvaluator}. 
 *
 * @author Josh Micich
 */
public class TestWorkbookEvaluator extends TestCase {
	
	/**
	 * Make sure that the evaluator can directly handle tAttrSum (instead of relying on re-parsing
	 * the whole formula which converts tAttrSum to tFuncVar("SUM") ) 
	 */
	public void testAttrSum() {
		
		Ptg[] ptgs = {
			new IntPtg(42),
			AttrPtg.SUM,
		};
		
		ValueEval result = new WorkbookEvaluator(null).evaluateFormula(0, 0, 0, ptgs, null);
		assertEquals(42, ((NumberEval)result).getNumberValue(), 0.0);
	}
	
	/**
	 * Make sure that the evaluator can directly handle (deleted) ref error tokens
	 * (instead of relying on re-parsing the whole formula which converts these 
	 * to the error constant #REF! ) 
	 */
	public void testRefErr() {
		
		confirmRefErr(new RefErrorPtg());
		confirmRefErr(new AreaErrPtg());
		confirmRefErr(new DeletedRef3DPtg(0));
		confirmRefErr(new DeletedArea3DPtg(0));
	}
	private static void confirmRefErr(Ptg ptg) {
		Ptg[] ptgs = {
			ptg,
		};
		
		ValueEval result = new WorkbookEvaluator(null).evaluateFormula(0, 0, 0, ptgs, null);
		assertEquals(ErrorEval.REF_INVALID, result);
	}
	
	/**
	 * Make sure that the evaluator can directly handle tAttrSum (instead of relying on re-parsing
	 * the whole formula which converts tAttrSum to tFuncVar("SUM") ) 
	 */
	public void testMemFunc() {
		
		Ptg[] ptgs = {
			new IntPtg(42),
			AttrPtg.SUM,
		};
		
		ValueEval result = new WorkbookEvaluator(null).evaluateFormula(0, 0, 0, ptgs, null);
		assertEquals(42, ((NumberEval)result).getNumberValue(), 0.0);
	}
	
	
}
