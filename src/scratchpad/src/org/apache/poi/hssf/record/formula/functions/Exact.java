/*
 * Created on May 15, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.StringValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public class Exact extends TextFunction {


    public Eval evaluate(Eval[] operands, int srcCellRow, short srcCellCol) {
        ValueEval retval = null;
        String s0 = null;
        String s1 = null;
        
        switch (operands.length) {
        default:
            retval = ErrorEval.VALUE_INVALID;
            break;
        case 2:
            ValueEval ve = singleOperandEvaluate(operands[0], srcCellRow, srcCellCol);
            if (ve instanceof StringValueEval) {
                StringValueEval sve = (StringValueEval) ve;
                s0 = sve.getStringValue();
            }
            else if (ve instanceof BlankEval) {
                s0 = StringEval.EMPTY_INSTANCE.getStringValue();
            }
            else {
                retval = ErrorEval.VALUE_INVALID;
                break;
            }

            if (retval == null) {
                ve = singleOperandEvaluate(operands[1], srcCellRow, srcCellCol);
                if (ve instanceof StringValueEval) {
                    StringValueEval sve = (StringValueEval) ve;
                    s1 = sve.getStringValue();
                }
                else if (ve instanceof BlankEval) {
                    s1 = StringEval.EMPTY_INSTANCE.getStringValue();
                }
                else {
                    retval = ErrorEval.VALUE_INVALID;
                    break;
                }
            }
        }
        
        if (retval == null) {
            boolean b = s0.equals(s1);
            retval = b ? BoolEval.TRUE : BoolEval.FALSE;
        }
        
        return retval;
    }
}
