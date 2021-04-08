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

package org.apache.poi.ss.formula.functions;

import static org.apache.poi.ss.formula.functions.AggregateFunction.subtotalInstance;

import org.apache.poi.ss.formula.LazyRefEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.formula.eval.NotImplementedFunctionException;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation for the Excel function SUBTOTAL<p>
 *
 * <b>Syntax :</b> <br>
 *  SUBTOTAL ( <b>functionCode</b>, <b>ref1</b>, ref2 ... ) <br>
 *    <table border="1" cellpadding="1" cellspacing="0" summary="Parameter descriptions">
 *      <tr><td><b>functionCode</b></td><td>(1-11) Selects the underlying aggregate function to be used (see table below)</td></tr>
 *      <tr><td><b>ref1</b>, ref2 ...</td><td>Arguments to be passed to the underlying aggregate function</td></tr>
 *    </table><br>
 * </p>
 *
 *  <table border="1" cellpadding="1" cellspacing="0" summary="Parameter descriptions">
 *      <tr><th>functionCode</th><th>Aggregate Function</th></tr>
 *      <tr align='center'><td>1</td><td>AVERAGE</td></tr>
 *      <tr align='center'><td>2</td><td>COUNT</td></tr>
 *      <tr align='center'><td>3</td><td>COUNTA</td></tr>
 *      <tr align='center'><td>4</td><td>MAX</td></tr>
 *      <tr align='center'><td>5</td><td>MIN</td></tr>
 *      <tr align='center'><td>6</td><td>PRODUCT</td></tr>
 *      <tr align='center'><td>7</td><td>STDEV</td></tr>
 *      <tr align='center'><td>8</td><td>STDEVP *</td></tr>
 *      <tr align='center'><td>9</td><td>SUM</td></tr>
 *      <tr align='center'><td>10</td><td>VAR *</td></tr>
 *      <tr align='center'><td>11</td><td>VARP *</td></tr>
 *      <tr align='center'><td>101</td><td>AVERAGE</td></tr>
 *      <tr align='center'><td>102</td><td>COUNT</td></tr>
 *      <tr align='center'><td>103</td><td>COUNTA</td></tr>
 *      <tr align='center'><td>104</td><td>MAX</td></tr>
 *      <tr align='center'><td>105</td><td>MIN</td></tr>
 *      <tr align='center'><td>106</td><td>PRODUCT</td></tr>
 *      <tr align='center'><td>107</td><td>STDEV</td></tr>
 *      <tr align='center'><td>108</td><td>STDEVP *</td></tr>
 *      <tr align='center'><td>109</td><td>SUM</td></tr>
 *      <tr align='center'><td>110</td><td>VAR *</td></tr>
 *      <tr align='center'><td>111</td><td>VARP *</td></tr>
 *  </table><br>
 * * Not implemented in POI yet. Functions 101-111 are the same as functions 1-11 but with
 * the option 'ignore hidden values'.
 * <p>
 *
 * @author Paul Tomlin &lt; pault at bulk sms dot com &gt;
 */
public class Subtotal implements Function {

	private static Function findFunction(int functionCode) throws EvaluationException {
        switch (functionCode) {
			case 1: return subtotalInstance(AggregateFunction.AVERAGE, true);
			case 2: return Count.subtotalInstance(true);
			case 3: return Counta.subtotalInstance(true);
			case 4: return subtotalInstance(AggregateFunction.MAX, true);
			case 5: return subtotalInstance(AggregateFunction.MIN, true);
			case 6: return subtotalInstance(AggregateFunction.PRODUCT, true);
			case 7: return subtotalInstance(AggregateFunction.STDEV, true);
			case 8: throw new NotImplementedFunctionException("STDEVP");
			case 9: return subtotalInstance(AggregateFunction.SUM, true);
			case 10: throw new NotImplementedFunctionException("VAR");
			case 11: throw new NotImplementedFunctionException("VARP");
			case 101: return subtotalInstance(AggregateFunction.AVERAGE, false);
			case 102: return Count.subtotalInstance(false);
			case 103: return Counta.subtotalInstance(false);
			case 104: return subtotalInstance(AggregateFunction.MAX, false);
			case 105: return subtotalInstance(AggregateFunction.MIN, false);
			case 106: return subtotalInstance(AggregateFunction.PRODUCT, false);
			case 107: return subtotalInstance(AggregateFunction.STDEV, false);
			case 108: throw new NotImplementedFunctionException("STDEVP SUBTOTAL with 'exclude hidden values' option");
			case 109: return subtotalInstance(AggregateFunction.SUM, false);
			case 110: throw new NotImplementedFunctionException("VAR SUBTOTAL with 'exclude hidden values' option");
			case 111: throw new NotImplementedFunctionException("VARP SUBTOTAL with 'exclude hidden values' option");
		}
		throw EvaluationException.invalidValue();
	}

	public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
		int nInnerArgs = args.length-1; // -1: first arg is used to select from a basic aggregate function
		if (nInnerArgs < 1) {
			return ErrorEval.VALUE_INVALID;
		}

		final Function innerFunc;
		int functionCode = 0;
		try {
			ValueEval ve = OperandResolver.getSingleValue(args[0], srcRowIndex, srcColumnIndex);
            functionCode = OperandResolver.coerceValueToInt(ve);
			innerFunc = findFunction(functionCode);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}

		// ignore the first arg, this is the function-type, we check for the length above
		final List<ValueEval> list = new ArrayList<>(Arrays.asList(args).subList(1, args.length));

		Iterator<ValueEval> it = list.iterator();

		// See https://support.office.com/en-us/article/SUBTOTAL-function-7b027003-f060-4ade-9040-e478765b9939
		// "If there are other subtotals within ref1, ref2,... (or nested subtotals), these nested subtotals are ignored to avoid double counting."
		// For array references it is handled in other evaluation steps, but we need to handle this here for references to subtotal-functions
		while(it.hasNext()) {
			ValueEval eval = it.next();
			if(eval instanceof LazyRefEval) {
				LazyRefEval lazyRefEval = (LazyRefEval) eval;
				if(lazyRefEval.isSubTotal()) {
					it.remove();
				}
				if (functionCode > 100 && lazyRefEval.isRowHidden()) {
				    it.remove();
				}
			}
		}

		return innerFunc.evaluate(list.toArray(new ValueEval[0]), srcRowIndex, srcColumnIndex);
	}
}
