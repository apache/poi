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

import org.apache.poi.hssf.record.formula.RefPtg;
import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;
import org.apache.poi.hssf.record.formula.eval.Ref2DEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

import junit.framework.TestCase;

/**
 * Test cases for SUMPRODUCT()
 * 
 * @author Josh Micich
 */
public final class TestSumproduct extends TestCase {
	
	private static Eval invokeSumproduct(Eval[] args) {
		// srcCellRow and srcCellColumn are ignored by SUMPRODUCT
		return new Sumproduct().evaluate(args, -1, (short)-1);
	}
	private static void confirmDouble(double expected, Eval actualEval) {
		if(!(actualEval instanceof NumericValueEval)) {
			fail("Expected numeric result");
		}
		NumericValueEval nve = (NumericValueEval)actualEval;
		assertEquals(expected, nve.getNumberValue(), 0);
	}

	public void testScalarSimple() {
		
		RefEval refEval = new Ref2DEval(new RefPtg("A1"), new NumberEval(3));
		Eval[] args = {
			refEval, 
			new NumberEval(2),
		};
		Eval result = invokeSumproduct(args);
		confirmDouble(6D, result);
	}


	public void testAreaSimple() {
		
		AreaEval aeA = EvalFactory.createAreaEval("A1:A3", 1, 3);
		AreaEval aeB = EvalFactory.createAreaEval("B1:B3", 1, 3);
		ValueEval[] aValues = aeA.getValues();
		ValueEval[] bValues = aeB.getValues();
		aValues[0] = new NumberEval(2);
		aValues[1] = new NumberEval(4);
		aValues[2] = new NumberEval(5);
		bValues[0] = new NumberEval(3);
		bValues[1] = new NumberEval(6);
		bValues[2] = new NumberEval(7);
		
		Eval[] args = { aeA, aeB, };
		Eval result = invokeSumproduct(args);
		confirmDouble(65D, result);
	}

	/**
	 * For scalar products, the terms may be 1x1 area refs
	 */
	public void testOneByOneArea() {
		
		AreaEval ae = EvalFactory.createAreaEval("A1:A1", 1, 1);
		ae.getValues()[0] = new NumberEval(7);
		
		Eval[] args = {
				ae, 
				new NumberEval(2),
			};
		Eval result = invokeSumproduct(args);
		confirmDouble(14D, result);
	}

	
	public void testMismatchAreaDimensions() {
		
		AreaEval aeA = EvalFactory.createAreaEval("A1:A3", 1, 3);
		AreaEval aeB = EvalFactory.createAreaEval("B1:D1", 3, 1);
		
		Eval[] args;
		args = new Eval[] { aeA, aeB, };
		assertEquals(ErrorEval.VALUE_INVALID, invokeSumproduct(args));
		
		args = new Eval[] { aeA, new NumberEval(5), };
		assertEquals(ErrorEval.VALUE_INVALID, invokeSumproduct(args));
	}
	
	public void testAreaWithErrorCell() {
		AreaEval aeA = EvalFactory.createAreaEval("A1:A2", 1, 2);
		AreaEval aeB = EvalFactory.createAreaEval("B1:B2", 1, 2);
		aeB.getValues()[1] = ErrorEval.REF_INVALID;
		
		Eval[] args = { aeA, aeB, };
		assertEquals(ErrorEval.REF_INVALID, invokeSumproduct(args));
	}
	
}
