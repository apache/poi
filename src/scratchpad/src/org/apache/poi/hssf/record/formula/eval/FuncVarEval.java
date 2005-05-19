/*
 * Created on May 8, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.AbstractFunctionPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.functions.Function;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *  
 */
public class FuncVarEval extends FunctionEval {

    private AbstractFunctionPtg delegate;

    public FuncVarEval(Ptg funcPtg) {
        delegate = (AbstractFunctionPtg) funcPtg;
    }

    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        Eval retval = null;
        Function f = getFunction();
        if (f != null)
            retval = f.evaluate(operands, srcRow, srcCol);
        else
            retval = ErrorEval.FUNCTION_NOT_IMPLEMENTED;
        return retval;
    }

    public int getNumberOfOperands() {
        return delegate.getNumberOfOperands();
    }

    public int getType() {
        return delegate.getType();
    }

    public short getFunctionIndex() {
        return delegate.getFunctionIndex();
    }
}
