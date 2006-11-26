/*
 * Created on May 15, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public class Isnumber extends LogicalFunction {
    public Eval evaluate(Eval[] operands, int srcCellRow, short srcCellCol) {
        Eval retval = BoolEval.FALSE;
        
        switch (operands.length) {
        default:
            retval = ErrorEval.VALUE_INVALID;
            break;
        case 1:
            Eval eval = operands[0];
            if (eval instanceof NumberEval) {
                retval = BoolEval.TRUE;
            }
            else if (eval instanceof RefEval) {
                Eval xlatedEval = xlateRefEval((RefEval) eval);
                if (xlatedEval instanceof NumberEval) {
                    retval = BoolEval.TRUE;
                }
            }
        }
        
        return retval;
    }
}
