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

import org.apache.poi.hssf.record.formula.functions.Function;

/**
 * Collects eval instances for easy access by tests in this package
 *
 * @author Josh Micich
 */
final class EvalInstances {
	private EvalInstances() {
		// no instances of this class
	}

	public static final Function Add = TwoOperandNumericOperation.AddEval;
	public static final Function Subtract = TwoOperandNumericOperation.SubtractEval;
	public static final Function Multiply = TwoOperandNumericOperation.MultiplyEval;
	public static final Function Divide = TwoOperandNumericOperation.DivideEval;

	public static final Function Power = TwoOperandNumericOperation.PowerEval;

	public static final Function Percent = PercentEval.instance;

	public static final Function UnaryMinus = UnaryMinusEval.instance;
	public static final Function UnaryPlus = UnaryPlusEval.instance;

	public static final Function Equal = RelationalOperationEval.EqualEval;
	public static final Function LessThan = RelationalOperationEval.LessThanEval;
	public static final Function LessEqual = RelationalOperationEval.LessEqualEval;
	public static final Function GreaterThan = RelationalOperationEval.GreaterThanEval;
	public static final Function GreaterEqual = RelationalOperationEval.GreaterEqualEval;
	public static final Function NotEqual = RelationalOperationEval.NotEqualEval;

	public static final Function Range = RangeEval.instance;
	public static final Function Concat = ConcatEval.instance;
}
