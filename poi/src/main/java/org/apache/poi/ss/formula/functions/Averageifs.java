/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Implementation for the Excel function AVERAGEIFS<p>
 *
 * Syntax : <p>
 *  AVERAGEIFS ( <b>average_range</b>, <b>criteria_range1</b>, <b>criteria1</b>,
 *  [<b>criteria_range2</b>,  <b>criteria2</b>], ...)
 *    <ul>
 *      <li><b>min_range</b> Required. One or more cells to average, including numbers or names, ranges,
 *      or cell references that contain numbers. Blank and text values are ignored.</li>
 *      <li><b>criteria1_range</b> Required. The first range in which
 *      to evaluate the associated criteria.</li>
 *      <li><b>criteria1</b> Required. The criteria in the form of a number, expression,
 *        cell reference, or text that define which cells in the criteria_range1
 *        argument will be added</li>
 *      <li><b> criteria_range2, criteria2, ...</b>    Optional. Additional ranges and their associated criteria.
 *      Up to 127 range/criteria pairs are allowed.
 *    </ul>
 */
public final class Averageifs extends Baseifs {
    /**
     * Singleton
     */
    public static final FreeRefFunction instance = new Averageifs();

    /**
     * https://support.microsoft.com/en-us/office/averageifs-function-48910c45-1fc0-4389-a028-f7c5c3001690
     * AVERAGEIFS(average_range, criteria_range1, criteria1, [criteria_range2, criteria2], ...)
     * need at least 3 arguments and need to have an odd number of arguments (average_range plus x*(criteria_range, criteria))
     */
    @Override
    protected boolean hasInitialRange() {
        return true;
    }

    @Override
    protected Aggregator createAggregator() {
        return new Aggregator() {
            Double sum = 0.0;
            Integer count = 0;
            
            @Override
            public void addValue(ValueEval value) {
                if(!(value instanceof NumberEval)) return;
                
                double d = ((NumberEval) value).getNumberValue();;
                sum += d;
                count++;
                
            }
            
            @Override
            public ValueEval getResult() {
                return count == 0 ? ErrorEval.DIV_ZERO : new NumberEval(sum / count);
            }
        };
    }
}
