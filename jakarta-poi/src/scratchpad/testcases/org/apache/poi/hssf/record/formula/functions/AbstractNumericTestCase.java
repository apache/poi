/*
 * Created on May 29, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import junit.framework.TestCase;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public abstract class AbstractNumericTestCase extends TestCase {

    public static final double POS_ZERO = 1E-4;
    public static final double DIFF_TOLERANCE_FACTOR = 1E-8;

    public void setUp() {
    }

    public void tearDown() {
    }

    /**
     * Why doesnt JUnit have a method like this for doubles? 
     * The current impl (3.8.1) of Junit has a retar*** method
     * for comparing doubles. DO NOT use that.
     * TODO: This class should really be in an abstract super class
     * to avoid code duplication across this project.
     * @param message
     * @param baseval
     * @param checkval
     */
    public static void assertEquals(String message, double baseval, double checkval, double almostZero, double diffToleranceFactor) {
        double posZero = Math.abs(almostZero);
        double negZero = -1 * posZero;
        if (Double.isNaN(baseval)) {
            assertTrue(message+": Expected " + baseval + " but was " + checkval
                    , Double.isNaN(baseval));
        }
        else if (Double.isInfinite(baseval)) {
            assertTrue(message+": Expected " + baseval + " but was " + checkval
                    , Double.isInfinite(baseval) && ((baseval<0) == (checkval<0)));
        }
        else {
            assertTrue(message+": Expected " + baseval + " but was " + checkval
                ,baseval != 0
                    ? Math.abs(baseval - checkval) <= Math.abs(diffToleranceFactor * baseval)
                    : checkval < posZero && checkval > negZero);
        }
    }

    public static void assertEquals(String msg, double baseval, double checkval) {
        assertEquals(msg, baseval, checkval, POS_ZERO, DIFF_TOLERANCE_FACTOR);
    }

}
