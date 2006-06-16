/*
 * Created on May 8, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.BoolPtg;
import org.apache.poi.hssf.record.formula.Ptg;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *  
 */
public class BoolEval implements NumericValueEval, StringValueEval {

    private boolean value;
    
    public static final BoolEval FALSE = new BoolEval(false);
    
    public static final BoolEval TRUE = new BoolEval(true);

    public BoolEval(Ptg ptg) {
        this.value = ((BoolPtg) ptg).getValue();
    }

    private BoolEval(boolean value) {
        this.value = value;
    }

    public boolean getBooleanValue() {
        return value;
    }

    public double getNumberValue() {
        return value ? (short) 1 : (short) 0;
    }

    public String getStringValue() {
        return value ? "TRUE" : "FALSE";
    }
}
