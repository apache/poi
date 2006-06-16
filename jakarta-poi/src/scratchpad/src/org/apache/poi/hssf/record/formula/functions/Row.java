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
public class Row implements Function {

    public Eval evaluate(Eval[] evals, int srcCellRow, short srcCellCol) {
        ValueEval retval = null;
        int rnum = -1;
        
        switch (evals.length) {
        default:
            retval = ErrorEval.VALUE_INVALID;
        case 1:
            if (evals[0] instanceof AreaEval) {
                AreaEval ae = (AreaEval) evals[0];
                rnum = ae.getFirstRow();
            }
            else if (evals[0] instanceof RefEval) {
                RefEval re = (RefEval) evals[0];
                rnum = re.getRow();
            }
            else { // anything else is not valid argument
                retval = ErrorEval.VALUE_INVALID;
            }
            break;
        case 0:
            rnum = srcCellRow;
        }
        
        if (retval == null) {
            retval = (rnum >= 0)
                    ? new NumberEval(rnum + 1) // +1 since excel rownums are 1 based
                    : (ValueEval) ErrorEval.VALUE_INVALID;
        }
        
        return retval;
    }
    
}
