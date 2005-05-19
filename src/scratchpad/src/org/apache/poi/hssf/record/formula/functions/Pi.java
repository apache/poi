/*
 * Created on May 6, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *  
 */
public class Pi implements Function {

    private static final NumberEval PI_EVAL = new NumberEval(Math.PI);

    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        ValueEval retval;
        switch (operands.length) {
        default:
            retval = ErrorEval.VALUE_INVALID;
            break;
        case 0:
            retval = PI_EVAL;
        }
        return retval;
    }

}
