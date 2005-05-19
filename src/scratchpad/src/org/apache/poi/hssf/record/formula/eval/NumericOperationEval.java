/*
 * Created on May 14, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public abstract class NumericOperationEval implements OperationEval {

    protected abstract ValueEvalToNumericXlator getXlator();
    
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
                    ve = getXlator().attemptXlateToNumeric(ve);
                    retval = getXlator().attemptXlateToNumeric(ve);
                }
                else {
                    retval = ErrorEval.VALUE_INVALID;
                }
            }
            else if (ae.isColumn()) {
                if (ae.containsRow(srcRow)) {
                    ValueEval ve = ae.getValueAt(srcRow, ae.getFirstColumn());
                    retval = getXlator().attemptXlateToNumeric(ve);
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
            retval = getXlator().attemptXlateToNumeric((ValueEval) eval);
        }
        return retval;
    }
}
