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

import org.apache.poi.hssf.record.formula.AddPtg;
import org.apache.poi.hssf.record.formula.ConcatPtg;
import org.apache.poi.hssf.record.formula.DividePtg;
import org.apache.poi.hssf.record.formula.EqualPtg;
import org.apache.poi.hssf.record.formula.ExpPtg;
import org.apache.poi.hssf.record.formula.FuncPtg;
import org.apache.poi.hssf.record.formula.FuncVarPtg;
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
import org.apache.poi.hssf.record.formula.eval.AddEval;
import org.apache.poi.hssf.record.formula.eval.ConcatEval;
import org.apache.poi.hssf.record.formula.eval.DivideEval;
import org.apache.poi.hssf.record.formula.eval.EqualEval;
import org.apache.poi.hssf.record.formula.eval.FuncVarEval;
import org.apache.poi.hssf.record.formula.eval.GreaterEqualEval;
import org.apache.poi.hssf.record.formula.eval.GreaterThanEval;
import org.apache.poi.hssf.record.formula.eval.LessEqualEval;
import org.apache.poi.hssf.record.formula.eval.LessThanEval;
import org.apache.poi.hssf.record.formula.eval.MultiplyEval;
import org.apache.poi.hssf.record.formula.eval.NotEqualEval;
import org.apache.poi.hssf.record.formula.eval.OperationEval;
import org.apache.poi.hssf.record.formula.eval.PercentEval;
import org.apache.poi.hssf.record.formula.eval.PowerEval;
import org.apache.poi.hssf.record.formula.eval.RangeEval;
import org.apache.poi.hssf.record.formula.eval.SubtractEval;
import org.apache.poi.hssf.record.formula.eval.UnaryMinusEval;
import org.apache.poi.hssf.record.formula.eval.UnaryPlusEval;

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
		m.put(EqualPtg.class, EqualEval.instance);

		m.put(EqualPtg.class, EqualEval.instance);
		m.put(GreaterEqualPtg.class, GreaterEqualEval.instance);
		m.put(GreaterThanPtg.class, GreaterThanEval.instance);
		m.put(LessEqualPtg.class, LessEqualEval.instance);
		m.put(LessThanPtg.class, LessThanEval.instance);
		m.put(NotEqualPtg.class, NotEqualEval.instance);

		m.put(AddPtg.class, AddEval.instance);
		m.put(DividePtg.class, DivideEval.instance);
		m.put(MultiplyPtg.class, MultiplyEval.instance);
		m.put(PercentPtg.class, PercentEval.instance);
		m.put(PowerPtg.class, PowerEval.instance);
		m.put(SubtractPtg.class, SubtractEval.instance);
		m.put(UnaryMinusPtg.class, UnaryMinusEval.instance);
		m.put(UnaryPlusPtg.class, UnaryPlusEval.instance);
		m.put(RangePtg.class, RangeEval.instance);
		return m;
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

		Class ptgClass = ptg.getClass();

		result = _instancesByPtgClass.get(ptgClass);
		if (result != null) {
			return  result;
		}
		
		if (ptgClass == FuncPtg.class) {
			return new FuncVarEval((FuncPtg)ptg);
		}
		if (ptgClass == FuncVarPtg.class) {
			return new FuncVarEval((FuncVarPtg)ptg);
		}
		if (ptgClass == ConcatPtg.class) {
			return new ConcatEval((ConcatPtg)ptg);
		}
		if(ptgClass == ExpPtg.class) {
			// ExpPtg is used for array formulas and shared formulas.
			// it is currently unsupported, and may not even get implemented here
			throw new RuntimeException("ExpPtg currently not supported");
		}
		throw new RuntimeException("Unexpected operation ptg class (" + ptgClass.getName() + ")");
	}
}
