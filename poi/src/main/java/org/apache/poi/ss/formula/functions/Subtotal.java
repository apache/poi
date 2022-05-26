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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.formula.LazyRefEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Implementation for the Excel function SUBTOTAL<p>
 *
 * <b>Syntax :</b> <br>
 *  SUBTOTAL ( <b>functionCode</b>, <b>ref1</b>, ref2 ... ) <br>
 *    <table>
 *      <caption>Parameter descriptions</caption>
 *      <tr><td><b>functionCode</b></td><td>(1-11) Selects the underlying aggregate function to be used (see table below)</td></tr>
 *      <tr><td><b>ref1</b>, ref2 ...</td><td>Arguments to be passed to the underlying aggregate function</td></tr>
 *    </table><br>
 *
 *  <table>
 *      <caption>Parameter descriptions</caption>
 *      <tr><th>functionCode</th><th>Aggregate Function</th></tr>
 *      <tr><td>1</td><td>AVERAGE</td></tr>
 *      <tr><td>2</td><td>COUNT</td></tr>
 *      <tr><td>3</td><td>COUNTA</td></tr>
 *      <tr><td>4</td><td>MAX</td></tr>
 *      <tr><td>5</td><td>MIN</td></tr>
 *      <tr><td>6</td><td>PRODUCT</td></tr>
 *      <tr><td>7</td><td>STDEV</td></tr>
 *      <tr><td>8</td><td>STDEVP</td></tr>
 *      <tr><td>9</td><td>SUM</td></tr>
 *      <tr><td>10</td><td>VAR</td></tr>
 *      <tr><td>11</td><td>VARP</td></tr>
 *      <tr><td>101</td><td>AVERAGE</td></tr>
 *      <tr><td>102</td><td>COUNT</td></tr>
 *      <tr><td>103</td><td>COUNTA</td></tr>
 *      <tr><td>104</td><td>MAX</td></tr>
 *      <tr><td>105</td><td>MIN</td></tr>
 *      <tr><td>106</td><td>PRODUCT</td></tr>
 *      <tr><td>107</td><td>STDEV</td></tr>
 *      <tr><td>108</td><td>STDEVP</td></tr>
 *      <tr><td>109</td><td>SUM</td></tr>
 *      <tr><td>110</td><td>VAR</td></tr>
 *      <tr><td>111</td><td>VARP</td></tr>
 *  </table><br>
 * * Functions 101-111 are the same as functions 1-11 but with
 * the option 'ignore hidden values'.
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
            case 8: return subtotalInstance(AggregateFunction.STDEVP, true);
            case 9: return subtotalInstance(AggregateFunction.SUM, true);
            case 10: return subtotalInstance(AggregateFunction.VAR, true);
            case 11: return subtotalInstance(AggregateFunction.VARP, true);
            case 101: return subtotalInstance(AggregateFunction.AVERAGE, false);
            case 102: return Count.subtotalInstance(false);
            case 103: return Counta.subtotalInstance(false);
            case 104: return subtotalInstance(AggregateFunction.MAX, false);
            case 105: return subtotalInstance(AggregateFunction.MIN, false);
            case 106: return subtotalInstance(AggregateFunction.PRODUCT, false);
            case 107: return subtotalInstance(AggregateFunction.STDEV, false);
            case 108: return subtotalInstance(AggregateFunction.STDEVP, false);
            case 109: return subtotalInstance(AggregateFunction.SUM, false);
            case 110: return subtotalInstance(AggregateFunction.VAR, false);
            case 111: return subtotalInstance(AggregateFunction.VARP, false);
        }
        throw EvaluationException.invalidValue();
    }

    @Override
    public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        int nInnerArgs = args.length-1; // -1: first arg is used to select from a basic aggregate function
        if (nInnerArgs < 1) {
            return ErrorEval.VALUE_INVALID;
        }

        final Function innerFunc;
        int functionCode;
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
