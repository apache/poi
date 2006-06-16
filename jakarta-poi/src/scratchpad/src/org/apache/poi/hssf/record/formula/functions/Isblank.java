/*
 * Created on May 15, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public class Isblank implements Function {

    public Eval evaluate(Eval[] operands, int srcCellRow, short srcCellCol) {
        ValueEval retval = null;
        boolean b = false;
        
        switch (operands.length) {
        default:
            retval = ErrorEval.VALUE_INVALID;
            break;
        case 1:
            if (operands[0] instanceof BlankEval) {
                b = true;
            }
            else if (operands[0] instanceof AreaEval) {
                AreaEval ae = (AreaEval) operands[0];
                if (ae.contains(srcCellRow, srcCellCol)) { // circular ref!
                    retval = ErrorEval.CIRCULAR_REF_ERROR;
                }
                else if (ae.isRow()) {
                    if (ae.containsColumn(srcCellCol)) {
                        ValueEval ve = ae.getValueAt(ae.getFirstRow(), srcCellCol);
                        b = (ve instanceof BlankEval);
                    }
                    else {
                        b = false;
                    }
                }
                else if (ae.isColumn()) {
                    if (ae.containsRow(srcCellRow)) {
                        ValueEval ve = ae.getValueAt(srcCellRow, ae.getFirstColumn());
                        b = (ve instanceof BlankEval);
                    }
                    else {
                        b = false;
                    }
                }
                else {
                    b = false;
                }
            }
            else if (operands[0] instanceof RefEval) {
                RefEval re = (RefEval) operands[0];
                b = (!re.isEvaluated()) && re.getInnerValueEval() instanceof BlankEval; 
            }
            else {
                b = false;
            }
        }
        
        if (retval == null) {
            retval = b
                    ? BoolEval.TRUE
                    : BoolEval.FALSE;
        }
        return retval;
    }
}
