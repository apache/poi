/*
 * Created on Nov 25, 2006
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public abstract class LogicalFunction implements Function {

    /**
     * recursively evaluate any RefEvals
     * @param reval
     * @return
     */
    protected ValueEval xlateRefEval(RefEval reval) {
        ValueEval retval = (ValueEval) reval.getInnerValueEval();
        
        if (retval instanceof RefEval) {
            RefEval re = (RefEval) retval;
            retval = xlateRefEval(re);
        }

        return retval;
    }
}
