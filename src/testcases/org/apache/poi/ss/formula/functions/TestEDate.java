package org.apache.poi.ss.formula.functions;

import junit.framework.TestCase;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.ErrorConstants;

import java.util.Calendar;
import java.util.Date;

public class TestEDate extends TestCase{

    public void testEDateProperValues() {
        EDate eDate = new EDate();
        NumberEval result = (NumberEval) eDate.evaluate(new ValueEval[]{new NumberEval(1000), new NumberEval(0)}, null);
        assertEquals(1000d, result.getNumberValue());
    }

    public void testEDateInvalidValues() {
        EDate eDate = new EDate();
        ErrorEval result = (ErrorEval) eDate.evaluate(new ValueEval[]{new NumberEval(1000)}, null);
        assertEquals(ErrorConstants.ERROR_VALUE, result.getErrorCode());
    }

    public void testEDateIncrease() {
        EDate eDate = new EDate();
        Date startDate = new Date();
        int offset = 2;
        NumberEval result = (NumberEval) eDate.evaluate(new ValueEval[]{new NumberEval(DateUtil.getExcelDate(startDate)), new NumberEval(offset)}, null);
        Date resultDate = DateUtil.getJavaDate(result.getNumberValue());
        Calendar instance = Calendar.getInstance();
        instance.setTime(startDate);
        instance.add(Calendar.MONTH, offset);
        assertEquals(resultDate, instance.getTime());

    }

    public void testEDateDecrease() {
        EDate eDate = new EDate();
        Date startDate = new Date();
        int offset = -2;
        NumberEval result = (NumberEval) eDate.evaluate(new ValueEval[]{new NumberEval(DateUtil.getExcelDate(startDate)), new NumberEval(offset)}, null);
        Date resultDate = DateUtil.getJavaDate(result.getNumberValue());
        Calendar instance = Calendar.getInstance();
        instance.setTime(startDate);
        instance.add(Calendar.MONTH, offset);
        assertEquals(resultDate, instance.getTime());
    }
}
