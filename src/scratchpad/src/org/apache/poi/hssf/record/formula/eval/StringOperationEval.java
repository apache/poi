/*
 * Created on May 14, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public abstract class StringOperationEval implements OperationEval {



    /**
     * Returns an instanceof StringValueEval or ErrorEval or BlankEval
     * 
     * @param eval
     * @param srcRow
     * @param srcCol
     * @return
     */
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
                    retval = internalResolveEval(eval);
                }
                else {
                    retval = ErrorEval.NAME_INVALID;
                }
            }
            else if (ae.isColumn()) {
                if (ae.containsRow(srcRow)) {
                    ValueEval ve = ae.getValueAt(srcRow, ae.getFirstColumn());
                    retval = internalResolveEval(eval);
                }
                else {
                    retval = ErrorEval.NAME_INVALID;
                }
            }
            else {
                retval = ErrorEval.NAME_INVALID;
            }
        }
        else {
            retval = internalResolveEval(eval);
        }
        return retval;
    }

    private ValueEval internalResolveEval(Eval eval) {
        ValueEval retval;
        if (eval instanceof StringValueEval) {
            retval = (StringValueEval) eval;
        }
        else if (eval instanceof RefEval) {
            RefEval re = (RefEval) eval;
            ValueEval tve = re.getInnerValueEval();
            if (tve instanceof StringValueEval || tve instanceof BlankEval) {
                retval = tve;
            }
            else {
                retval = ErrorEval.NAME_INVALID;
            }
        }
        else if (eval instanceof BlankEval) {
            retval = (BlankEval) eval;
        }
        else {
            retval = ErrorEval.NAME_INVALID;
        }
        return retval;
    }
}
