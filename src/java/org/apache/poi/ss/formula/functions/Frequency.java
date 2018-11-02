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

import org.apache.poi.ss.formula.CacheAreaEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;

import java.util.Arrays;

/**
 * Implementation of Excel 'Analysis ToolPak' function FREQUENCY()<br>
 * Returns a frequency distribution as a vertical array<p>
 * <p>
 * <b>Syntax</b><br>
 * <b>FREQUENCY</b>(<b>data_array</b>, <b>bins_array</b>)<p>
 * <p>
 * <b>data_array</b> Required. An array of or reference to a set of values for which you want to count frequencies.
 * If data_array contains no values, FREQUENCY returns an array of zeros.<br>
 * <b>bins_array</b> Required. An array of or reference to intervals into which you want to group the values in data_array.
 * If bins_array contains no values, FREQUENCY returns the number of elements in data_array.<br>
 *
 * @author Yegor Kozlov
 */
public class Frequency extends Fixed2ArgFunction {
    public static final Function instance = new Frequency();

    private Frequency() {
        // enforce singleton
    }

    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
        MatrixFunction.MutableValueCollector collector = new MatrixFunction.MutableValueCollector(false, false);

        double[] values;
        double[] bins;
        try {
            values = collector.collectValues(arg0);
            bins = collector.collectValues(arg1);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }

        // can bins be not sorted?
        //bins = Arrays.stream(bins).sorted().distinct().toArray();

        int[] histogram = histogram(values, bins);
        NumberEval[] result = Arrays.stream(histogram).boxed().map(NumberEval::new).toArray(NumberEval[]::new);
        return new CacheAreaEval(srcRowIndex, srcColumnIndex,
                srcRowIndex + result.length - 1, srcColumnIndex, result);
    }

    static int findBin(double value, double[] bins) {
        int idx = Arrays.binarySearch(bins, value);
        return idx >= 0 ? idx + 1 : -idx;
    }

    static int[] histogram(double[] values, double[] bins) {
        int[] histogram = new int[bins.length + 1];
        for (double val : values) {
            histogram[findBin(val, bins) - 1]++;
        }
        return histogram;
    }
}
