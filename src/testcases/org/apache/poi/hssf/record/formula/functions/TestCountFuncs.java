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

import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.hssf.record.formula.RefPtg;
import org.apache.poi.hssf.record.formula.eval.Area2DEval;
import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.Ref2DEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Test cases for COUNT(), COUNTA() COUNTIF(), COUNTBLANK()
 * 
 * @author Josh Micich
 */
public final class TestCountFuncs extends TestCase {

	public TestCountFuncs(String testName) {
		super(testName);
	}
	
	public void testCountA() {
		
		Eval[] args;
		
		args = new Eval[] {
			new NumberEval(0),	 
		};
		confirmCountA(1, args);
		
		args = new Eval[] {
			new NumberEval(0),	
			new NumberEval(0),
			new StringEval(""),
		};
		confirmCountA(3, args);
		
		args = new Eval[] {
			EvalFactory.createAreaEval("D2:F5", 3, 4),	
		};
		confirmCountA(12, args);
		
		args = new Eval[] {
			EvalFactory.createAreaEval("D1:F5", 3, 5),	// 15
			EvalFactory.createRefEval("A1"),	
			EvalFactory.createAreaEval("A1:G6", 7, 6),	// 42
			new NumberEval(0),
		};
		confirmCountA(59, args);
	}

	public void testCountIf() {
		
		AreaEval range;
		ValueEval[] values;
		
		// when criteria is a boolean value
		values = new ValueEval[] {
				new NumberEval(0),	
				new StringEval("TRUE"),	// note - does not match boolean TRUE
				BoolEval.TRUE,
				BoolEval.FALSE,
				BoolEval.TRUE,
				BlankEval.INSTANCE,
		};
		range = createAreaEval("A1:B3", values);
		confirmCountIf(2, range, BoolEval.TRUE);
		
		// when criteria is numeric
		values = new ValueEval[] {
				new NumberEval(0),	
				new StringEval("2"),	
				new StringEval("2.001"),	
				new NumberEval(2),	
				new NumberEval(2),	
				BoolEval.TRUE,
		};
		range = createAreaEval("A1:B3", values);
		confirmCountIf(3, range, new NumberEval(2));
		// note - same results when criteria is a string that parses as the number with the same value
		confirmCountIf(3, range, new StringEval("2.00"));
		
		if (false) { // not supported yet: 
			// when criteria is an expression (starting with a comparison operator)
			confirmCountIf(4, range, new StringEval(">1"));
		}
	}
	/**
	 * special case where the criteria argument is a cell reference
	 */
	public void testCountIfWithCriteriaReference() {

		ValueEval[] values = { 
				new NumberEval(22),
				new NumberEval(25),
				new NumberEval(21),
				new NumberEval(25),
				new NumberEval(25),
				new NumberEval(25),
		};
		Area2DEval arg0 = new Area2DEval(new AreaPtg("C1:C6"), values);
		
		Ref2DEval criteriaArg = new Ref2DEval(new RefPtg("A1"), new NumberEval(25));
		Eval[] args=  { arg0, criteriaArg, };
		
		double actual = NumericFunctionInvoker.invoke(new Countif(), args);
		assertEquals(4, actual, 0D);
	}
	

	private static AreaEval createAreaEval(String areaRefStr, ValueEval[] values) {
		return new Area2DEval(new AreaPtg(areaRefStr), values);
	}

	private static void confirmCountA(int expected, Eval[] args) {
		double result = NumericFunctionInvoker.invoke(new Counta(), args);
		assertEquals(expected, result, 0);
	}
	private static void confirmCountIf(int expected, AreaEval range, Eval criteria) {
		
		Eval[] args = { range, criteria, };
		double result = NumericFunctionInvoker.invoke(new Countif(), args);
		assertEquals(expected, result, 0);
	}
}
