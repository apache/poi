package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.usermodel.DateUtil;

import java.util.Calendar;
import java.util.Date;

public class EDate implements FreeRefFunction {
    public static final FreeRefFunction instance = new EDate();

    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length != 2) {
            return ErrorEval.VALUE_INVALID;
        }
        try {
            double startDateAsNumber = getValue(args[0]);
            NumberEval offsetInYearsValue = (NumberEval) args[1];
            int offsetInMonthAsNumber = (int) offsetInYearsValue.getNumberValue();

            Date startDate = DateUtil.getJavaDate(startDateAsNumber);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.MONTH, offsetInMonthAsNumber);
            return new NumberEval(DateUtil.getExcelDate(calendar.getTime()));
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    private double getValue(ValueEval arg) throws EvaluationException {
        if (arg instanceof NumberEval) {
            return ((NumberEval) arg).getNumberValue();
        }
        if (arg instanceof RefEval) {
            ValueEval innerValueEval = ((RefEval) arg).getInnerValueEval();
            return ((NumberEval) innerValueEval).getNumberValue();
        }
        throw new EvaluationException(ErrorEval.REF_INVALID);
    }
}
