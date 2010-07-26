package org.apache.poi.hssf.record.formula.functions;

import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;


/**
 * Tests for Excel function POISSON(x,mean,cumulative)
 * @author Kalpesh Parmar
 */
public class TestPoisson extends TestCase {

    private static final double DELTA = 1E-15;

    private static ValueEval invokePoisson(double x, double mean, boolean cumulative)
    {

        ValueEval[] valueEvals = new ValueEval[3];
        valueEvals[0] = new NumberEval(x);
        valueEvals[1] = new NumberEval(mean);
        valueEvals[2] = BoolEval.valueOf(cumulative);

        return NumericFunction.POISSON.evaluate(valueEvals,-1,-1);
	}

    public void testCumulativeProbability()
    {
        double x = 1;
        double mean = 0.2;
        double result = 0.9824769036935787; // known result

        NumberEval myResult = (NumberEval)invokePoisson(x,mean,true);

        assertEquals(myResult.getNumberValue(), result, DELTA);
    }

    public void testNonCumulativeProbability()
    {
        double x = 0;
        double mean = 0.2;
        double result = 0.8187307530779818; // known result
        
        NumberEval myResult = (NumberEval)invokePoisson(x,mean,false);

        assertEquals(myResult.getNumberValue(), result, DELTA);
    }

    public void testNegativeMean()
    {
        double x = 0;
        double mean = -0.2;

        ErrorEval myResult = (ErrorEval)invokePoisson(x,mean,false);

        assertEquals(ErrorEval.NUM_ERROR.getErrorCode(), myResult.getErrorCode());
    }

    public void testNegativeX()
    {
        double x = -1;
        double mean = 0.2;

        ErrorEval myResult = (ErrorEval)invokePoisson(x,mean,false);

        assertEquals(ErrorEval.NUM_ERROR.getErrorCode(), myResult.getErrorCode());    
    }



    public void testXAsDecimalNumber()
    {
        double x = 1.1;
        double mean = 0.2;
        double result = 0.9824769036935787; // known result

        NumberEval myResult = (NumberEval)invokePoisson(x,mean,true);

        assertEquals(myResult.getNumberValue(), result, DELTA);
    }

    public void testXZeroMeanZero()
    {
        double x = 0;
        double mean = 0;
        double result = 1; // known result in excel

        NumberEval myResult = (NumberEval)invokePoisson(x,mean,true);

        assertEquals(myResult.getNumberValue(), result, DELTA);
    }
}
