/*
 * Created on May 15, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEvalToNumericXlator;

/**
 * @author 
 *
 */
public class Sign extends NumericFunction {
    
    private static final ValueEvalToNumericXlator NUM_XLATOR = 
        new ValueEvalToNumericXlator((short)
                ( ValueEvalToNumericXlator.BOOL_IS_PARSED 
                | ValueEvalToNumericXlator.EVALUATED_REF_BOOL_IS_PARSED
                | ValueEvalToNumericXlator.EVALUATED_REF_STRING_IS_PARSED
                | ValueEvalToNumericXlator.REF_BOOL_IS_PARSED
                | ValueEvalToNumericXlator.STRING_IS_PARSED
                ));

    protected ValueEvalToNumericXlator getXlator() {
        return NUM_XLATOR;
    }

    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        double d = 0;
        ValueEval retval = null;
        
        switch (operands.length) {
        default:
            retval = ErrorEval.VALUE_INVALID;
            break;
        case 1:
            ValueEval ve = singleOperandEvaluate(operands[0], srcRow, srcCol);
            if (ve instanceof NumericValueEval) {
                NumericValueEval ne = (NumericValueEval) ve;
                d = ne.getNumberValue();
            }
            else if (ve instanceof BlankEval) {
                // do nothing
            }
            else {
                retval = ErrorEval.NUM_ERROR;
            }
        }
        
        if (retval == null) {
            retval = (Double.isNaN(d) || Double.isInfinite(d)) 
                    ? (ValueEval) ErrorEval.VALUE_INVALID 
                    : (d == 0) 
                        ? new NumberEval(0)
                        : (d < 0)
                            ? new NumberEval(-1)
                            : new NumberEval(1);
        }
        return retval;
    }

}
