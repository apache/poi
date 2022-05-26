package org.apache.poi.ss.formula.atp;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.AggregateFunction;
import org.apache.poi.ss.formula.functions.FreeRefFunction;

public class Vars implements FreeRefFunction {

    public static final Vars instance = new Vars();

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        return AggregateFunction.VAR.evaluate(args, ec.getRowIndex(), ec.getColumnIndex());
    }
}
