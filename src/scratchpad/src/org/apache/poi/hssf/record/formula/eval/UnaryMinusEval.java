/*
 * Created on May 8, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.UnaryMinusPtg;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *  
 */
public class UnaryMinusEval extends NumericOperationEval {

    private UnaryMinusPtg delegate;
    private static final ValueEvalToNumericXlator NUM_XLATOR = 
        new ValueEvalToNumericXlator((short)
                ( ValueEvalToNumericXlator.BOOL_IS_PARSED 
                | ValueEvalToNumericXlator.EVALUATED_REF_BOOL_IS_PARSED
                | ValueEvalToNumericXlator.EVALUATED_REF_STRING_IS_PARSED
                | ValueEvalToNumericXlator.REF_BOOL_IS_PARSED
                | ValueEvalToNumericXlator.STRING_IS_PARSED
                ));


    public UnaryMinusEval(Ptg ptg) {
        this.delegate = (UnaryMinusPtg) ptg;
    }
    
    protected ValueEvalToNumericXlator getXlator() {
        return NUM_XLATOR;
    }

    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        ValueEval retval = null;
        double d = 0;
        
        switch (operands.length) {
        default:
            retval = ErrorEval.UNKNOWN_ERROR;
            break;
        case 1:
            ValueEval ve = singleOperandEvaluate(operands[0], srcRow, srcCol);
            if (ve instanceof NumericValueEval) {
                d = ((NumericValueEval) ve).getNumberValue();
            }
            else if (ve instanceof BlankEval) {
                // do nothing
            }
            else if (ve instanceof ErrorEval) {
                retval = ve;
            }
        }
        
        if (retval == null) {
            retval = new NumberEval(-d);
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
