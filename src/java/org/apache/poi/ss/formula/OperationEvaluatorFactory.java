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

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.record.formula.AbstractFunctionPtg;
import org.apache.poi.hssf.record.formula.AddPtg;
import org.apache.poi.hssf.record.formula.ConcatPtg;
import org.apache.poi.hssf.record.formula.DividePtg;
import org.apache.poi.hssf.record.formula.EqualPtg;
import org.apache.poi.hssf.record.formula.GreaterEqualPtg;
import org.apache.poi.hssf.record.formula.GreaterThanPtg;
import org.apache.poi.hssf.record.formula.LessEqualPtg;
import org.apache.poi.hssf.record.formula.LessThanPtg;
import org.apache.poi.hssf.record.formula.MultiplyPtg;
import org.apache.poi.hssf.record.formula.NotEqualPtg;
import org.apache.poi.hssf.record.formula.OperationPtg;
import org.apache.poi.hssf.record.formula.PercentPtg;
import org.apache.poi.hssf.record.formula.PowerPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.RangePtg;
import org.apache.poi.hssf.record.formula.SubtractPtg;
import org.apache.poi.hssf.record.formula.UnaryMinusPtg;
import org.apache.poi.hssf.record.formula.UnaryPlusPtg;
import org.apache.poi.hssf.record.formula.eval.ConcatEval;
import org.apache.poi.hssf.record.formula.eval.FunctionEval;
import org.apache.poi.hssf.record.formula.eval.OperationEval;
import org.apache.poi.hssf.record.formula.eval.PercentEval;
import org.apache.poi.hssf.record.formula.eval.RangeEval;
import org.apache.poi.hssf.record.formula.eval.RelationalOperationEval;
import org.apache.poi.hssf.record.formula.eval.TwoOperandNumericOperation;
import org.apache.poi.hssf.record.formula.eval.UnaryMinusEval;
import org.apache.poi.hssf.record.formula.eval.UnaryPlusEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.functions.Function;

/**
 * This class creates <tt>OperationEval</tt> instances to help evaluate <tt>OperationPtg</tt>
 * formula tokens.
 *
 * @author Josh Micich
 */
final class OperationEvaluatorFactory {

	private static final Map<Class<? extends Ptg>, OperationEval> _instancesByPtgClass = initialiseInstancesMap();

	private OperationEvaluatorFactory() {
		// no instances of this class
	}

	private static Map<Class<? extends Ptg>, OperationEval> initialiseInstancesMap() {
		Map<Class<? extends Ptg>, OperationEval> m = new HashMap<Class<? extends Ptg>, OperationEval>(32);

		put(m, 2, EqualPtg.class, RelationalOperationEval.EqualEval);
		put(m, 2, GreaterEqualPtg.class, RelationalOperationEval.GreaterEqualEval);
		put(m, 2, GreaterThanPtg.class, RelationalOperationEval.GreaterThanEval);
		put(m, 2, LessEqualPtg.class, RelationalOperationEval.LessEqualEval);
		put(m, 2, LessThanPtg.class, RelationalOperationEval.LessThanEval);
		put(m, 2, NotEqualPtg.class, RelationalOperationEval.NotEqualEval);

		put(m, 2, ConcatPtg.class, ConcatEval.instance);
		put(m, 2, AddPtg.class, TwoOperandNumericOperation.AddEval);
		put(m, 2, DividePtg.class, TwoOperandNumericOperation.DivideEval);
		put(m, 2, MultiplyPtg.class, TwoOperandNumericOperation.MultiplyEval);
		put(m, 1, PercentPtg.class, PercentEval.instance);
		put(m, 2, PowerPtg.class, TwoOperandNumericOperation.PowerEval);
		put(m, 2, SubtractPtg.class, TwoOperandNumericOperation.SubtractEval);
		put(m, 1, UnaryMinusPtg.class, UnaryMinusEval.instance);
		put(m, 1, UnaryPlusPtg.class, UnaryPlusEval.instance);
		put(m, 2, RangePtg.class, RangeEval.instance);
		return m;
	}

	private static void put(Map<Class<? extends Ptg>, OperationEval> m, int argCount,
			Class<? extends Ptg> ptgClass, Function instance) {
		m.put(ptgClass, new OperationFunctionEval(instance, argCount));
	}

	/**
	 * Simple adapter from {@link OperationEval} to {@link Function}
	 */
	private static final class OperationFunctionEval implements OperationEval {

		private final Function _function;
		private final int _numberOfOperands;

		public OperationFunctionEval(Function function, int argCount) {
			_function = function;
			_numberOfOperands = argCount;
		}

		public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
			return _function.evaluate(args, ec.getRowIndex(), (short) ec.getColumnIndex());
		}

		public int getNumberOfOperands() {
			return _numberOfOperands;
		}
	}

	/**
	 * returns the OperationEval concrete impl instance corresponding
	 * to the supplied operationPtg
	 */
	public static OperationEval create(OperationPtg ptg) {
		if(ptg == null) {
			throw new IllegalArgumentException("ptg must not be null");
		}
		OperationEval result;

		Class<? extends OperationPtg> ptgClass = ptg.getClass();

		result = _instancesByPtgClass.get(ptgClass);
		if (result != null) {
			return  result;
		}

		if (ptg instanceof AbstractFunctionPtg) {
			return new FunctionEval((AbstractFunctionPtg)ptg);
		}
		throw new RuntimeException("Unexpected operation ptg class (" + ptgClass.getName() + ")");
	}
}
