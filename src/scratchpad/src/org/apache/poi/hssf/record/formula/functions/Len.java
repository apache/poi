/*
 * Created on May 15, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.StringValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public class Len extends TextFunction {


    public Eval evaluate(Eval[] operands, int srcCellRow, short srcCellCol) {
        ValueEval retval = null;
        String s = null;
        
        switch (operands.length) {
        default:
            retval = ErrorEval.VALUE_INVALID;
            break;
        case 1:
            ValueEval ve = singleOperandEvaluate(operands[0], srcCellRow, srcCellCol);
            if (ve instanceof StringValueEval) {
                StringValueEval sve = (StringValueEval) ve;
                s = sve.getStringValue();
            }
            else if (ve instanceof RefEval) {
                RefEval re = (RefEval) ve;
                ValueEval ive = re.getInnerValueEval();
                if (ive instanceof BlankEval) {
                    s = re.isEvaluated() ? "0" : null;
                }
                else if (ive instanceof StringValueEval) {
                    s = ((StringValueEval) ive).getStringValue();
                }
                else if (ive instanceof BlankEval) {}
                else {
                    retval = ErrorEval.VALUE_INVALID;
                }
            }
            else if (ve instanceof BlankEval) {}
            else {
                retval = ErrorEval.VALUE_INVALID;
                break;
            }
        }
        
        if (retval == null) {
            s = (s == null) ? EMPTY_STRING : s;
            retval = new NumberEval(s.length());
        }
        
        return retval;
    }
    
    
    protected ValueEval singleOperandEvaluate(Eval eval, int srcRow, short srcCol) {
        ValueEval retval;
        if (eval instanceof AreaEval) {
            AreaEval ae = (AreaEval) eval;
            if (ae.contains(srcRow, srcCol)) { // circular ref!
                retval = ErrorEval.CIRCULAR_REF_ERROR;
            }
            else if (ae.isRow()) {
                if (ae.containsColumn(srcCol)) {
                    ValueEval ve = ae.getValueAt(ae.getFirstRow(), srcCol);
                    retval = attemptXlateToText(ve);
                }
                else {
                    retval = ErrorEval.VALUE_INVALID;
                }
            }
            else if (ae.isColumn()) {
                if (ae.containsRow(srcRow)) {
                    ValueEval ve = ae.getValueAt(srcRow, ae.getFirstColumn());
                    retval = attemptXlateToText(ve);
                }
                else {
                    retval = ErrorEval.VALUE_INVALID;
                }
            }
            else {
                retval = ErrorEval.VALUE_INVALID;
            }
        }
        else {
            retval = attemptXlateToText((ValueEval) eval);
        }
        return retval;
    }

    
    /**
     * converts from Different ValueEval types to StringEval.
     * Note: AreaEvals are not handled, if arg is an AreaEval,
     * the returned value is ErrorEval.VALUE_INVALID
     * @param ve
     * @return
     */
    protected ValueEval attemptXlateToText(ValueEval ve) {
        ValueEval retval;
        if (ve instanceof StringValueEval || ve instanceof RefEval) {
            retval = ve;
        }
        else if (ve instanceof BlankEval) {
            retval = ve;
        }
        else {
            retval = ErrorEval.VALUE_INVALID;
        }
        return retval;
    }
}
