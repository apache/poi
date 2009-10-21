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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.record.formula.AbstractFunctionPtg;
import org.apache.poi.hssf.record.formula.AddPtg;
import org.apache.poi.hssf.record.formula.ConcatPtg;
import org.apache.poi.hssf.record.formula.DividePtg;
import org.apache.poi.hssf.record.formula.EqualPtg;
import org.apache.poi.hssf.record.formula.GreaterEqualPtg;
import org.apache.poi.hssf.record.formula.GreaterThanPtg;
import org.apache.poi.hssf.record.formula.IntersectionPtg;
import org.apache.poi.hssf.record.formula.LessEqualPtg;
import org.apache.poi.hssf.record.formula.LessThanPtg;
import org.apache.poi.hssf.record.formula.MultiplyPtg;
import org.apache.poi.hssf.record.formula.NotEqualPtg;
import org.apache.poi.hssf.record.formula.OperationPtg;
import org.apache.poi.hssf.record.formula.PercentPtg;
import org.apache.poi.hssf.record.formula.PowerPtg;
import org.apache.poi.hssf.record.formula.RangePtg;
import org.apache.poi.hssf.record.formula.SubtractPtg;
import org.apache.poi.hssf.record.formula.UnaryMinusPtg;
import org.apache.poi.hssf.record.formula.UnaryPlusPtg;
import org.apache.poi.hssf.record.formula.eval.ConcatEval;
import org.apache.poi.hssf.record.formula.eval.FunctionEval;
import org.apache.poi.hssf.record.formula.eval.IntersectionEval;
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

	private static final Map<OperationPtg, OperationEval> _instancesByPtgClass = initialiseInstancesMap2();

	private OperationEvaluatorFactory() {
		// no instances of this class
	}

	private static Map<OperationPtg, OperationEval> initialiseInstancesMap2() {
		Map<OperationPtg, OperationEval> m = new HashMap<OperationPtg, OperationEval>(32);

		put(m, 2, EqualPtg.instance, RelationalOperationEval.EqualEval);
		put(m, 2, GreaterEqualPtg.instance, RelationalOperationEval.GreaterEqualEval);
		put(m, 2, GreaterThanPtg.instance, RelationalOperationEval.GreaterThanEval);
		put(m, 2, LessEqualPtg.instance, RelationalOperationEval.LessEqualEval);
		put(m, 2, LessThanPtg.instance, RelationalOperationEval.LessThanEval);
		put(m, 2, NotEqualPtg.instance, RelationalOperationEval.NotEqualEval);

		put(m, 2, ConcatPtg.instance, ConcatEval.instance);
		put(m, 2, AddPtg.instance, TwoOperandNumericOperation.AddEval);
		put(m, 2, DividePtg.instance, TwoOperandNumericOperation.DivideEval);
		put(m, 2, MultiplyPtg.instance, TwoOperandNumericOperation.MultiplyEval);
		put(m, 1, PercentPtg.instance, PercentEval.instance);
		put(m, 2, PowerPtg.instance, TwoOperandNumericOperation.PowerEval);
		put(m, 2, SubtractPtg.instance, TwoOperandNumericOperation.SubtractEval);
		put(m, 1, UnaryMinusPtg.instance, UnaryMinusEval.instance);
		put(m, 1, UnaryPlusPtg.instance, UnaryPlusEval.instance);
		put(m, 2, RangePtg.instance, RangeEval.instance);
		put(m, 2, IntersectionPtg.instance, IntersectionEval.instance);
		return m;
	}

	private static void put(Map<OperationPtg, OperationEval> m, int argCount,
			OperationPtg ptgKey, Function instance) {
		// make sure ptg has single private constructor because map lookups assume singleton keys
		Constructor[] cc = ptgKey.getClass().getDeclaredConstructors();
		if (cc.length > 1 || !Modifier.isPrivate(cc[0].getModifiers())) {
			throw new RuntimeException("Failed to verify instance ("
					+ ptgKey.getClass().getName() + ") is a singleton.");
		}
		m.put(ptgKey, new OperationFunctionEval(instance, argCount));
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
		OperationEval result = _instancesByPtgClass.get(ptg);

		if (result != null) {
			return  result;
		}

		if (ptg instanceof AbstractFunctionPtg) {
			return new FunctionEval((AbstractFunctionPtg)ptg);
		}
		throw new RuntimeException("Unexpected operation ptg class (" + ptg.getClass().getName() + ")");
	}
}
