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

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Test cases for SUMPRODUCT()
 *
 * @author Josh Micich
 */
public final class TestSumproduct extends TestCase {

	private static ValueEval invokeSumproduct(ValueEval[] args) {
		// srcCellRow and srcCellColumn are ignored by SUMPRODUCT
		return new Sumproduct().evaluate(args, -1, (short)-1);
	}
	private static void confirmDouble(double expected, ValueEval actualEval) {
		if(!(actualEval instanceof NumericValueEval)) {
			fail("Expected numeric result");
		}
		NumericValueEval nve = (NumericValueEval)actualEval;
		assertEquals(expected, nve.getNumberValue(), 0);
	}

	public void testScalarSimple() {

		RefEval refEval = EvalFactory.createRefEval("A1", new NumberEval(3));
		ValueEval[] args = {
			refEval,
			new NumberEval(2),
		};
		ValueEval result = invokeSumproduct(args);
		confirmDouble(6D, result);
	}

	public void testAreaSimple() {
		ValueEval[] aValues = {
			new NumberEval(2),
			new NumberEval(4),
			new NumberEval(5),
		};
		ValueEval[] bValues = {
			new NumberEval(3),
			new NumberEval(6),
			new NumberEval(7),
		};
		AreaEval aeA = EvalFactory.createAreaEval("A1:A3", aValues);
		AreaEval aeB = EvalFactory.createAreaEval("B1:B3", bValues);

		ValueEval[] args = { aeA, aeB, };
		ValueEval result = invokeSumproduct(args);
		confirmDouble(65D, result);
	}

	/**
	 * For scalar products, the terms may be 1x1 area refs
	 */
	public void testOneByOneArea() {

		AreaEval ae = EvalFactory.createAreaEval("A1:A1", new ValueEval[] { new NumberEval(7), });

		ValueEval[] args = {
				ae,
				new NumberEval(2),
			};
		ValueEval result = invokeSumproduct(args);
		confirmDouble(14D, result);
	}

	public void testMismatchAreaDimensions() {

		AreaEval aeA = EvalFactory.createAreaEval("A1:A3", new ValueEval[3]);
		AreaEval aeB = EvalFactory.createAreaEval("B1:D1", new ValueEval[3]);

		ValueEval[] args;
		args = new ValueEval[] { aeA, aeB, };
		assertEquals(ErrorEval.VALUE_INVALID, invokeSumproduct(args));

		args = new ValueEval[] { aeA, new NumberEval(5), };
		assertEquals(ErrorEval.VALUE_INVALID, invokeSumproduct(args));
	}

	public void testAreaWithErrorCell() {
		ValueEval[] aValues = {
			ErrorEval.REF_INVALID,
			null,
		};
		AreaEval aeA = EvalFactory.createAreaEval("A1:A2", aValues);
		AreaEval aeB = EvalFactory.createAreaEval("B1:B2", new ValueEval[2]);

		ValueEval[] args = { aeA, aeB, };
		assertEquals(ErrorEval.REF_INVALID, invokeSumproduct(args));
	}
}
