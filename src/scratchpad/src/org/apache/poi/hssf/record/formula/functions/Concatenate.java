/*
 * Created on May 15, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.StringValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public class Concatenate extends TextFunction {


    public Eval evaluate(Eval[] operands, int srcCellRow, short srcCellCol) {
        ValueEval retval = null;
        StringBuffer sb = new StringBuffer();
        
        for (int i=0, iSize=operands.length; i<iSize; i++) {
            ValueEval ve = singleOperandEvaluate(operands[i], srcCellRow, srcCellCol);
            if (ve instanceof StringValueEval) {
                StringValueEval sve = (StringValueEval) ve;
                sb.append(sve.getStringValue());
            }
            else if (ve instanceof BlankEval) {}
            else {
                retval = ErrorEval.VALUE_INVALID;
                break;
            }
        }
        
        if (retval == null) {
            retval = new StringEval(sb.toString());
        }
        
        return retval;
    }
}
