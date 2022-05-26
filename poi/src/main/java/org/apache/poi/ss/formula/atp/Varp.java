package org.apache.poi.ss.formula.atp;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.AggregateFunction;
import org.apache.poi.ss.formula.functions.FreeRefFunction;

public class Varp implements FreeRefFunction {

    public static final Varp instance = new Varp();

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        return AggregateFunction.VARP.evaluate(args, ec.getRowIndex(), ec.getColumnIndex());
    }
}
