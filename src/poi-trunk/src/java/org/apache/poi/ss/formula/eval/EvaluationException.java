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

package org.apache.poi.ss.formula.eval;

/**
 * This class is used to simplify error handling logic <i>within</i> operator and function
 * implementations.   Note - <tt>OperationEval.evaluate()</tt> and <tt>Function.evaluate()</tt>
 * method signatures do not throw this exception so it cannot propagate outside.<p>
 * 
 * Here is an example coded without <tt>EvaluationException</tt>, to show how it can help:
 * <pre>
 * public Eval evaluate(Eval[] args, int srcRow, short srcCol) {
 *	// ...
 *	Eval arg0 = args[0];
 *	if(arg0 instanceof ErrorEval) {
 *		return arg0;
 *	}
 *	if(!(arg0 instanceof AreaEval)) {
 *		return ErrorEval.VALUE_INVALID;
 *	}
 *	double temp = 0;
 *	AreaEval area = (AreaEval)arg0;
 *	ValueEval[] values = area.getValues();
 *	for (int i = 0; i < values.length; i++) {
 *		ValueEval ve = values[i];
 *		if(ve instanceof ErrorEval) {
 *			return ve;
 *		}
 *		if(!(ve instanceof NumericValueEval)) {
 *			return ErrorEval.VALUE_INVALID;
 *		}
 *		temp += ((NumericValueEval)ve).getNumberValue();
 *	}
 *	// ...
 * }	 
 * </pre>
 * In this example, if any error is encountered while processing the arguments, an error is 
 * returned immediately. This code is difficult to refactor due to all the points where errors
 * are returned.<br>
 * Using <tt>EvaluationException</tt> allows the error returning code to be consolidated to one
 * place.<p>
 * <pre>
 * public Eval evaluate(Eval[] args, int srcRow, short srcCol) {
 *	try {
 *		// ...
 *		AreaEval area = getAreaArg(args[0]);
 *		double temp = sumValues(area.getValues());
 *		// ...
 *	} catch (EvaluationException e) {
 *		return e.getErrorEval();
 *	}
 *}
 *
 *private static AreaEval getAreaArg(Eval arg0) throws EvaluationException {
 *	if (arg0 instanceof ErrorEval) {
 *		throw new EvaluationException((ErrorEval) arg0);
 *	}
 *	if (arg0 instanceof AreaEval) {
 *		return (AreaEval) arg0;
 *	}
 *	throw EvaluationException.invalidValue();
 *}
 *
 *private double sumValues(ValueEval[] values) throws EvaluationException {
 *	double temp = 0;
 *	for (int i = 0; i < values.length; i++) {
 *		ValueEval ve = values[i];
 *		if (ve instanceof ErrorEval) {
 *			throw new EvaluationException((ErrorEval) ve);
 *		}
 *		if (!(ve instanceof NumericValueEval)) {
 *			throw EvaluationException.invalidValue();
 *		}
 *		temp += ((NumericValueEval) ve).getNumberValue();
 *	}
 *	return temp;
 *}
 * </pre>   
 * It is not mandatory to use EvaluationException, doing so might give the following advantages:<br>
 *  - Methods can more easily be extracted, allowing for re-use.<br>
 *  - Type management (typecasting etc) is simpler because error conditions have been separated from
 * intermediate calculation values.<br>
 *  - Fewer local variables are required. Local variables can have stronger types.<br>
 *  - It is easier to mimic common Excel error handling behaviour (exit upon encountering first 
 *  error), because exceptions conveniently propagate up the call stack regardless of execution 
 *  points or the number of levels of nested calls.<p>
 *  
 * <b>Note</b> - Only standard evaluation errors are represented by <tt>EvaluationException</tt> (
 * i.e. conditions expected to be encountered when evaluating arbitrary Excel formulas). Conditions
 * that could never occur in an Excel spreadsheet should result in runtime exceptions. Care should
 * be taken to not translate any POI internal error into an Excel evaluation error code.   
 * 
 * @author Josh Micich
 */
public final class EvaluationException extends Exception {
	private final ErrorEval _errorEval;

	public EvaluationException(ErrorEval errorEval) {
		_errorEval = errorEval;
	}
	// some convenience factory methods

    /** <b>#VALUE!</b> - Wrong type of operand */
	public static EvaluationException invalidValue() {
		return new EvaluationException(ErrorEval.VALUE_INVALID);
	}
    /** <b>#REF!</b> - Illegal or deleted cell reference */
	public static EvaluationException invalidRef() {
		return new EvaluationException(ErrorEval.REF_INVALID);
	}
    /** <b>#NUM!</b> - Value range overflow */
	public static EvaluationException numberError() {
		return new EvaluationException(ErrorEval.NUM_ERROR);
	}
	
	public ErrorEval getErrorEval() {
		return _errorEval;
	}
}
