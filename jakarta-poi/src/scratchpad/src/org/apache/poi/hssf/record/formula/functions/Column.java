/*
 * Created on May 15, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author 
 *
 */
public class Column implements Function {
    public Eval evaluate(Eval[] evals, int srcCellRow, short srcCellCol) {
        ValueEval retval = null;
        int cnum = -1;
        
        switch (evals.length) {
        default:
            retval = ErrorEval.VALUE_INVALID;
        case 1:
            if (evals[0] instanceof AreaEval) {
                AreaEval ae = (AreaEval) evals[0];
                cnum = ae.getFirstColumn();
            }
            else if (evals[0] instanceof RefEval) {
                RefEval re = (RefEval) evals[0];
                cnum = re.getColumn();
            }
            else { // anything else is not valid argument
                retval = ErrorEval.VALUE_INVALID;
            }
            break;
        case 0:
            cnum = srcCellCol;
        }
        
        if (retval == null) {
            retval = (cnum >= 0)
                    ? new NumberEval(cnum + 1) // +1 since excel colnums are 1 based
                    : (ValueEval) ErrorEval.VALUE_INVALID;
        }
        
        return retval;
    }
    

}
