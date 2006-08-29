/*
 * Created on May 9, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author
 *  
 */
public class And extends BooleanFunction {

    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        ValueEval retval = null;
        boolean b = true;
        boolean atleastOneNonBlank = false;
        
        /*
         * Note: do not abort the loop if b is false, since we could be
         * dealing with errorevals later. 
         */
        outer:
        for (int i=0, iSize=operands.length; i<iSize; i++) {
            if (operands[i] instanceof AreaEval) {
                AreaEval ae = (AreaEval) operands[i];
                ValueEval[] values = ae.getValues();
                for (int j=0, jSize=values.length; j<jSize; j++) {
                    ValueEval tempVe = singleOperandEvaluate(values[j], srcRow, srcCol, true);
                    if (tempVe instanceof BoolEval) {
                        b = b && ((BoolEval) tempVe).getBooleanValue();
                        atleastOneNonBlank = true;
                    }
                    else if (tempVe instanceof ErrorEval) {
                        retval = tempVe;
                        break outer;
                    }
                }
            }
            else {
                ValueEval tempVe = singleOperandEvaluate(operands[i], srcRow, srcCol, false);
                if (tempVe instanceof BoolEval) {
                    b = b && ((BoolEval) tempVe).getBooleanValue();
                    atleastOneNonBlank = true;
                }
                else if (tempVe instanceof StringEval) {
                    retval = ErrorEval.VALUE_INVALID;
                }
                else if (tempVe instanceof ErrorEval) {
                    retval = tempVe;
                    break outer;
                }
            }
        }
        
        if (!atleastOneNonBlank) {
            retval = ErrorEval.VALUE_INVALID;
        }
        
        if (retval == null) { // if no error
            retval = b ? BoolEval.TRUE : BoolEval.FALSE;
        }
        
        return retval;
    }

}
