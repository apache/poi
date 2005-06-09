/*
 * Created on May 22, 2005
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
public class Fact extends NumericFunction {

    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        double d = 0;
        ValueEval retval = null;

        switch (operands.length) {
        default:
            retval = ErrorEval.VALUE_INVALID;
            break;
        case 1:
            ValueEval ve = singleOperandEvaluate(operands[0], srcRow, srcCol);
            if (ve instanceof NumericValueEval) {
                NumericValueEval ne = (NumericValueEval) ve;
                d = ne.getNumberValue();
            }
            else if (ve instanceof BlankEval) {
                // do nothing
            }
            else {
                retval = ErrorEval.NUM_ERROR;
            }
        }

        if (retval == null) {
            if (d < Integer.MAX_VALUE && d >= 0) {
                d = MathX.factorial((int) d);
                retval = (Double.isNaN(d))
                ? (ValueEval) ErrorEval.VALUE_INVALID
                : (Double.isInfinite(d))
                        ? (ValueEval) ErrorEval.NUM_ERROR
                        : new NumberEval(d);
            }
            else {
                retval = ErrorEval.NUM_ERROR;
            }
        }
        return retval;
    }

}
