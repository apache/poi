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
package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.IntPtg;
import org.apache.poi.hssf.record.formula.NumberPtg;
import org.apache.poi.hssf.record.formula.Ptg;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *  
 */
public class NumberEval implements NumericValueEval, StringValueEval {
    
    public static final NumberEval ZERO = new NumberEval(0);

    private double value;
    private String stringValue;

    public NumberEval(Ptg ptg) {
        if (ptg instanceof IntPtg) {
            this.value = ((IntPtg) ptg).getValue();
        }
        else if (ptg instanceof NumberPtg) {
            this.value = ((NumberPtg) ptg).getValue();
        }
    }

    public NumberEval(double value) {
        this.value = value;
    }

    public double getNumberValue() {
        return value;
    }

    public String getStringValue() { // TODO: limit to 15 decimal places
        if (stringValue == null)
            makeString();
        return stringValue;
    }
    
    protected void makeString() {
        if (!Double.isNaN(value)) {
            long lvalue = Math.round(value);
            if (lvalue == value) {
                stringValue = String.valueOf(lvalue);
            }
            else {
                stringValue = String.valueOf(value);
            }
        }
    }
    public final String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append(getClass().getName()).append(" [");
        sb.append(getStringValue());
        sb.append("]");
        return sb.toString();
    }
}
