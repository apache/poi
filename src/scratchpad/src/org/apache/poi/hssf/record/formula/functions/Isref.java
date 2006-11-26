/*
 * Created on May 15, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.RefEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public class Isref implements Function {
    public Eval evaluate(Eval[] operands, int srcCellRow, short srcCellCol) {
        Eval retval = BoolEval.FALSE;
        
        switch (operands.length) {
        default:
            retval = ErrorEval.VALUE_INVALID;
            break;
        case 1:
            Eval eval = operands[0];
            if (eval instanceof RefEval || eval instanceof AreaEval) {
                retval = BoolEval.TRUE;
            }
        }
        
        return retval;
    }
}
