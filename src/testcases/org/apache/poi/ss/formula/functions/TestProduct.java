package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TestProduct {
    @Test
    public void missingArgsAreIgnored() {
        ValueEval result = getInstance().evaluate(new ValueEval[]{new NumberEval(2.0), MissingArgEval.instance}, 0, 0);
        assertTrue(result instanceof NumberEval);
        assertEquals(2, ((NumberEval)result).getNumberValue(), 0);
    }

    @Test
    public void acceptanceTest() {
        final ValueEval[] args = {
                new NumberEval(2.0),
                MissingArgEval.instance,
                new StringEval("6"),
                BoolEval.TRUE};
        ValueEval result = getInstance().evaluate(args, 0, 0);
        assertTrue(result instanceof NumberEval);
        assertEquals(12, ((NumberEval)result).getNumberValue(), 0);
    }

    private static Function getInstance() {
        return AggregateFunction.PRODUCT;
    }
}
