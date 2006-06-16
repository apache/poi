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
    
}
