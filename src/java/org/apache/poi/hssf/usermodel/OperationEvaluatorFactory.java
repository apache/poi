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

package org.apache.poi.hssf.usermodel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
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
	private static final Class[] OPERATION_CONSTRUCTOR_CLASS_ARRAY = new Class[] { Ptg.class };
	// TODO - use singleton instances directly instead of reflection
	private static final Map _constructorsByPtgClass = initialiseConstructorsMap();
	private static final Map _instancesByPtgClass = initialiseInstancesMap();
	
	private OperationEvaluatorFactory() {
		// no instances of this class
	}
	
	private static Map initialiseConstructorsMap() {
		Map m = new HashMap(32);
		add(m, AddPtg.class, AddEval.class);
		add(m, ConcatPtg.class, ConcatEval.class);
		add(m, DividePtg.class, DivideEval.class);
		add(m, FuncPtg.class, FuncVarEval.class);
		add(m, FuncVarPtg.class, FuncVarEval.class);
		add(m, MultiplyPtg.class, MultiplyEval.class);
		add(m, PercentPtg.class, PercentEval.class);
		add(m, PowerPtg.class, PowerEval.class);
		add(m, SubtractPtg.class, SubtractEval.class);
		add(m, UnaryMinusPtg.class, UnaryMinusEval.class);
		add(m, UnaryPlusPtg.class, UnaryPlusEval.class);
		return m;
	}
	private static Map initialiseInstancesMap() {
		Map m = new HashMap(32);
		add(m, EqualPtg.class, EqualEval.instance);
		add(m, GreaterEqualPtg.class, GreaterEqualEval.instance);
		add(m, GreaterThanPtg.class, GreaterThanEval.instance);
		add(m, LessEqualPtg.class, LessEqualEval.instance);
		add(m, LessThanPtg.class, LessThanEval.instance);
		add(m, NotEqualPtg.class, NotEqualEval.instance);
		return m;
	}

	private static void add(Map m, Class ptgClass, OperationEval evalInstance) {
		if(!Ptg.class.isAssignableFrom(ptgClass)) {
			throw new IllegalArgumentException("Expected Ptg subclass");
		}
		m.put(ptgClass, evalInstance);
	}

	private static void add(Map m, Class ptgClass, Class evalClass) {
		// perform some validation now, to keep later exception handlers simple
		if(!Ptg.class.isAssignableFrom(ptgClass)) {
			throw new IllegalArgumentException("Expected Ptg subclass");
		}
		
		if(!OperationEval.class.isAssignableFrom(evalClass)) {
			throw new IllegalArgumentException("Expected OperationEval subclass");
		}
		if (!Modifier.isPublic(evalClass.getModifiers())) {
			throw new RuntimeException("Eval class must be public");
		}
		if (Modifier.isAbstract(evalClass.getModifiers())) {
			throw new RuntimeException("Eval class must not be abstract");
		}
		
		Constructor constructor;
		try {
			constructor = evalClass.getDeclaredConstructor(OPERATION_CONSTRUCTOR_CLASS_ARRAY);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Missing constructor");
		}
		if (!Modifier.isPublic(constructor.getModifiers())) {
			throw new RuntimeException("Eval constructor must be public");
		}
		m.put(ptgClass, constructor);
	}

	/**
	 * returns the OperationEval concrete impl instance corresponding
	 * to the supplied operationPtg
	 */
	public static OperationEval create(OperationPtg ptg) {
		if(ptg == null) {
			throw new IllegalArgumentException("ptg must not be null");
		}
		Object result;
		
		Class ptgClass = ptg.getClass();
		
		result = _instancesByPtgClass.get(ptgClass);
		if (result != null) {
			return (OperationEval) result;
		}
		
		
		Constructor constructor = (Constructor) _constructorsByPtgClass.get(ptgClass);
		if(constructor == null) {
			if(ptgClass == ExpPtg.class) {
				// ExpPtg is used for array formulas and shared formulas.
				// it is currently unsupported, and may not even get implemented here
				throw new RuntimeException("ExpPtg currently not supported");
			}
			throw new RuntimeException("Unexpected operation ptg class (" + ptgClass.getName() + ")");
		}
		
		Object[] initargs = { ptg };
		try {
			result = constructor.newInstance(initargs);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return (OperationEval) result;
	}
}
