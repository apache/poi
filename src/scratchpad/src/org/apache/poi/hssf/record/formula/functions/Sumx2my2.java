/*
 * Created on May 15, 2005
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
public class Sumx2my2 extends XYNumericFunction {

    
    public Eval evaluate(Eval[] operands, int srcCellRow, short srcCellCol) {
        ValueEval retval = null;
        double[][] values = null;
        
        int checkLen = 0; // check to see that all array lengths are equal
        switch (operands.length) {
        default:
            retval = ErrorEval.VALUE_INVALID;
            break;
        case 2:
            values = getValues(operands, srcCellRow, srcCellCol);
            if (values==null 
                    || values[X] == null || values[Y] == null
                    || values[X].length == 0 || values[Y].length == 0
                    || values[X].length != values[Y].length) {
                retval = ErrorEval.VALUE_INVALID;
            }
        }
        
        if (retval == null) {
            double d = MathX.sumx2my2(values[X], values[Y]);
            retval = (Double.isNaN(d) || Double.isInfinite(d))
                    ? (ValueEval) ErrorEval.NUM_ERROR
                    : new NumberEval(d);
        }
        
        return retval;
    }
}
