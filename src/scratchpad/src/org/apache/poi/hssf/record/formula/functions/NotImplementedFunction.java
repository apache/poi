/*
 * Created on May 6, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;

/**
 * 
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * This is the default implementation of a Function class. 
 * The default behaviour is to return a non-standard ErrorEval
 * "ErrorEval.FUNCTION_NOT_IMPLEMENTED". This error should alert 
 * the user that the formula contained a function that is not
 * yet implemented.
 */
public class NotImplementedFunction implements Function {

    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        return ErrorEval.FUNCTION_NOT_IMPLEMENTED;
    }

}
