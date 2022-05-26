package org.apache.poi.ss.formula.atp;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.AggregateFunction;
import org.apache.poi.ss.formula.functions.FreeRefFunction;

public class Stdevp implements FreeRefFunction {

    public static final Stdevp instance = new Stdevp();

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        return AggregateFunction.STDEVP.evaluate(args, ec.getRowIndex(), ec.getColumnIndex());
    }
}
