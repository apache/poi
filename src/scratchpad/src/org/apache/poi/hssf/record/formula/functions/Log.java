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
import org.apache.poi.hssf.record.formula.eval.ValueEvalToNumericXlator;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * Log: LOG(number,[base])
 */
public class Log extends NumericFunction {
    
    private static final ValueEvalToNumericXlator NUM_XLATOR = 
        new ValueEvalToNumericXlator((short)
                ( ValueEvalToNumericXlator.BOOL_IS_PARSED 
                | ValueEvalToNumericXlator.EVALUATED_REF_BOOL_IS_PARSED
                | ValueEvalToNumericXlator.EVALUATED_REF_STRING_IS_PARSED
                | ValueEvalToNumericXlator.REF_BOOL_IS_PARSED
                | ValueEvalToNumericXlator.STRING_IS_PARSED
                ));
    
    private static final double DEFAULT_BASE = 10;

    protected ValueEvalToNumericXlator getXlator() {
        return NUM_XLATOR;
    }

    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        double d = 0;
        double base = DEFAULT_BASE;
        double num = 0;
        ValueEval retval = null;
        
        switch (operands.length) {
        default:
            break;
        case 2: // second arg is base 
            ValueEval ve = singleOperandEvaluate(operands[1], srcRow, srcCol);
            if (ve instanceof NumericValueEval) {
                NumericValueEval ne = (NumericValueEval) ve;
                base = ne.getNumberValue();
            }
            else if (ve instanceof BlankEval) {
                // do nothing
            }
            else {
                retval = ErrorEval.NUM_ERROR;
            }
            
        case 1: // first arg is number
            if (retval == null) {
                ValueEval vev = singleOperandEvaluate(operands[0], srcRow, srcCol);
                if (vev instanceof NumericValueEval) {
                    NumericValueEval ne = (NumericValueEval) vev;
                    num = ne.getNumberValue();
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
            d = (base == E) 
                ? Math.log(num)
                : Math.log(num) / Math.log(base);
            retval = (Double.isNaN(d) || Double.isInfinite(d)) ? (ValueEval) ErrorEval.VALUE_INVALID : new NumberEval(d);
        }
        return retval;
    }

}
