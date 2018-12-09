package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.Test;

import static org.junit.Assert.*;

public class GEOMEANTest {
    @Test
    public void GEOMEAN0arg() {
        Function geomean = AggregateFunction.GEOMEAN;

        final ValueEval result = geomean.evaluate(new ValueEval[0], 0, 0);
        assertTrue(result instanceof ErrorEval);
        assertEquals(ErrorEval.NUM_ERROR.getErrorCode(), ((ErrorEval)result).getErrorCode());
    }

    @Test
    public void GEOMEAN1arg() {
        Function geomean = AggregateFunction.GEOMEAN;

        final ValueEval result = geomean.evaluate(new ValueEval[]{new NumberEval(2)}, 0, 0);
        assertTrue(result instanceof NumberEval);
        assertEquals(2, ((NumberEval)result).getNumberValue(), 0);
    }

    @Test
    public void GEOMEAN() {
        Function geomean = AggregateFunction.GEOMEAN;

        final ValueEval result = geomean.evaluate(new ValueEval[]{new NumberEval(2), new NumberEval(3)}, 0, 0);
        assertTrue(result instanceof NumberEval);
        assertEquals(2.449489742783178, ((NumberEval)result).getNumberValue(), 1e-15);
    }
}
