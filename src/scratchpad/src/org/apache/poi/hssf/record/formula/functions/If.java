/*
 * Created on Nov 25, 2006
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * 
 */
public class If implements Function {

    public Eval evaluate(Eval[] evals, int srcCellRow, short srcCellCol) {
        Eval retval = null;
        Eval evalWhenFalse = BoolEval.FALSE;
        switch (evals.length) {
        case 3:
            evalWhenFalse = evals[2];
        case 2:
            BoolEval beval = (BoolEval) evals[0];
            if (beval.getBooleanValue()) {
                retval = evals[1];
            }
            else {
                retval = evalWhenFalse;
            }
            break;
        default:
            retval = ErrorEval.UNKNOWN_ERROR;
        }
        return retval;
    }


}
