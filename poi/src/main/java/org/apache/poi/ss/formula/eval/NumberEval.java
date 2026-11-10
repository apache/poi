/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/*
 * Created on May 8, 2005
 *
 */
package org.apache.poi.ss.formula.eval;

import org.apache.poi.ss.formula.ptg.IntPtg;
import org.apache.poi.ss.formula.ptg.NumberPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.util.NumberToTextConverter;

public final class NumberEval implements NumericValueEval, StringValueEval {

    public static final NumberEval ZERO = new NumberEval(0);

    private final double _value;
    private String _stringValue;

    public NumberEval(Ptg ptg) {
        if (ptg == null) {
            throw new IllegalArgumentException("ptg must not be null");
        }
        if (ptg instanceof IntPtg ip) {
            _value = ip.getValue();
        } else if (ptg instanceof NumberPtg np) {
            _value = flushSubnormal(np.getValue());
        } else {
            throw new IllegalArgumentException("bad argument type (" + ptg.getClass().getName() + ")");
        }
    }

    public NumberEval(double value) {
        _value = flushSubnormal(value);
    }

    /**
     * Excel has no subnormal numbers: anything closer to zero than {@link Double#MIN_NORMAL}
     * (about 2.2E-308) is zero to it, whether typed in, read from a cell or produced by a
     * function. Doing this here covers every path that puts a number into a formula result.
     * The sign of zero is kept as it is.
     */
    private static double flushSubnormal(double value) {
        return value != 0.0 && Math.abs(value) < Double.MIN_NORMAL ? 0.0 : value;
    }

    public double getNumberValue() {
        return _value;
    }

    public String getStringValue() {
        if (_stringValue == null) {
            _stringValue = NumberToTextConverter.toText(_value);
        }
        return _stringValue;
    }
    public final String toString() {
        return getClass().getName() + " [" +
                getStringValue() +
                "]";
    }
}
