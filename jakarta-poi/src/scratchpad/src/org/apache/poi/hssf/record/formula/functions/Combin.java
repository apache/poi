/*
 * Created on May 15, 2005
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
public class Combin extends NumericFunction {

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
                ve = singleOperandEvaluate(operands[1], srcRow, srcCol);
                if (ve instanceof NumericValueEval) {
                    NumericValueEval ne = (NumericValueEval) ve;
                    d1 = ne.getNumberValue();
                }
                else if (ve instanceof BlankEval) {
                    // do nothing
                }
                else {
                    retval = ErrorEval.NUM_ERROR;
                }
            }
        }

        if (retval == null) {
            if (d0 > Integer.MAX_VALUE || d1 > Integer.MAX_VALUE) {
                retval = ErrorEval.NUM_ERROR;
            }
            else {
                double d =  MathX.nChooseK((int) d0, (int) d1);
                retval = (Double.isNaN(d) || Double.isInfinite(d))
                        ? (ValueEval) ErrorEval.NUM_ERROR
                        : new NumberEval(d);
            }
        }
        return retval;
    }

}
