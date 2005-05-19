/*
 * Created on May 8, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.GreaterThanPtg;
import org.apache.poi.hssf.record.formula.Ptg;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *  
 */
public class GreaterThanEval extends RelationalOperationEval {

    private GreaterThanPtg delegate;

    public GreaterThanEval(Ptg ptg) {
        this.delegate = (GreaterThanPtg) ptg;
    }

    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        ValueEval retval = null;
        
        RelationalValues rvs = super.doEvaluate(operands, srcRow, srcCol);
        retval = rvs.ee;
        int result = 0;
        if (retval == null) {
            result = doComparison(rvs.bs);
            if (result == 0) {
                result = doComparison(rvs.ss);
            }
            if (result == 0) {
                result = doComparison(rvs.ds);
            }

            retval = (result > 0) ? BoolEval.TRUE : BoolEval.FALSE;;
        }

        return retval;
    }

    public int getNumberOfOperands() {
        return delegate.getNumberOfOperands();
    }

    public int getType() {
        return delegate.getType();
    }

}
