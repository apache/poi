/*
 * Created on May 6, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *  
 */
public class Power extends NumericFunction {
   
    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        double d0 = 0;
        double d1 = 0;
        ValueEval retval = null;
        
        switch (operands.length) {
        default:
            retval = ErrorEval.VALUE_INVALID;
            break;
        case 2:
            ValueEval ve = singleOperandEvaluate(operands[0], srcRow, srcCol);
            if (ve instanceof NumericValueEval) {
                NumericValueEval ne = (NumericValueEval) ve;
                d0 = ne.getNumberValue();
            }
            else if (ve instanceof BlankEval) {
                // do nothing
            }
            else {
                retval = ErrorEval.NUM_ERROR;
            }
            
            if (retval == null) {
                ValueEval vev = singleOperandEvaluate(operands[1], srcRow, srcCol);
                if (vev instanceof NumericValueEval) {
                    NumericValueEval ne = (NumericValueEval) vev;
                    d1 = ne.getNumberValue();
                }
                else if (vev instanceof BlankEval) {
                    // do nothing
                }
                else {
                    retval = ErrorEval.NUM_ERROR;
                }
            }
        }
        
        if (retval == null) {
            double d = Math.pow(d0, d1);
            retval = (Double.isNaN(d) || Double.isNaN(d)) ? (ValueEval) ErrorEval.VALUE_INVALID : new NumberEval(d);
        }
        return retval;
    }

}
