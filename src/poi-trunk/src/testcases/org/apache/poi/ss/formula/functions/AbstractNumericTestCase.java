/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/*
 * Created on May 29, 2005
 *
 */
package org.apache.poi.ss.formula.functions;

import junit.framework.TestCase;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public abstract class AbstractNumericTestCase extends TestCase {

    public static final double POS_ZERO = 1E-4;
    public static final double DIFF_TOLERANCE_FACTOR = 1E-8;

    @Override
    public void setUp() {
    }

    @Override
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
