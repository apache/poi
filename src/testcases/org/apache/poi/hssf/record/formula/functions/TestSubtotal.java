package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

import junit.framework.TestCase;

/**
 * Tests for {@link Subtotal}
 *
 * @author Paul Tomlin
 */
public final class TestSubtotal extends TestCase {
	private static final int FUNCTION_AVERAGE = 1;
	private static final int FUNCTION_COUNT = 2;
	private static final int FUNCTION_MAX = 4;
	private static final int FUNCTION_MIN = 5;
	private static final int FUNCTION_PRODUCT = 6;
	private static final int FUNCTION_STDEV = 7;
	private static final int FUNCTION_SUM = 9;

	private static final double[] TEST_VALUES0 = {
		1, 2,
		3, 4,
		5, 6,
		7, 8,
		9, 10
	};

	private static void confirmSubtotal(int function, double expected) {
		ValueEval[] values = new ValueEval[TEST_VALUES0.length];
		for (int i = 0; i < TEST_VALUES0.length; i++) {
			values[i] = new NumberEval(TEST_VALUES0[i]);
		}

		AreaEval arg1 = EvalFactory.createAreaEval("C1:D5", values);
		ValueEval args[] = { new NumberEval(function), arg1 };

		ValueEval result = new Subtotal().evaluate(args, 0, 0);

		assertEquals(NumberEval.class, result.getClass());
		assertEquals(expected, ((NumberEval) result).getNumberValue(), 0.0);
	}

	public void testBasics() {
		confirmSubtotal(FUNCTION_SUM, 55.0);
		confirmSubtotal(FUNCTION_AVERAGE, 5.5);
		confirmSubtotal(FUNCTION_COUNT, 10.0);
		confirmSubtotal(FUNCTION_MAX, 10.0);
		confirmSubtotal(FUNCTION_MIN, 1.0);
		confirmSubtotal(FUNCTION_PRODUCT, 3628800.0);
		confirmSubtotal(FUNCTION_STDEV, 3.0276503540974917);
	}
}
