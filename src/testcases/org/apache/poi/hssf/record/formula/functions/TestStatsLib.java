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
 * Created on May 30, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import junit.framework.AssertionFailedError;

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;


/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public class TestStatsLib extends AbstractNumericTestCase {

    public void testDevsq() {
        double[] v = null;
        double d, x = 0;
        
        v = new double[] {1,2,3,4,5,6,7,8,9,10};
        d = StatsLib.devsq(v);
        x = 82.5;
        assertEquals("devsq ", x, d);
        
        v = new double[] {1,1,1,1,1,1,1,1,1,1};
        d = StatsLib.devsq(v);
        x = 0;
        assertEquals("devsq ", x, d);
        
        v = new double[] {0,0,0,0,0,0,0,0,0,0};
        d = StatsLib.devsq(v);
        x = 0;
        assertEquals("devsq ", x, d);
        
        v = new double[] {1,2,1,2,1,2,1,2,1,2};
        d = StatsLib.devsq(v);
        x = 2.5;
        assertEquals("devsq ", x, d);
        
        v = new double[] {123.12,33.3333,2d/3d,5.37828,0.999};
        d = StatsLib.devsq(v);
        x = 10953.7416965767;
        assertEquals("devsq ", x, d);
        
        v = new double[] {-1,-2,-3,-4,-5,-6,-7,-8,-9,-10};
        d = StatsLib.devsq(v);
        x = 82.5;
        assertEquals("devsq ", x, d);
    }

    public void testKthLargest() {
        double[] v = null;
        double d, x = 0;
        
        v = new double[] {1,2,3,4,5,6,7,8,9,10};
        d = StatsLib.kthLargest(v, 3);
        x = 8;
        assertEquals("kthLargest ", x, d);
        
        v = new double[] {1,1,1,1,1,1,1,1,1,1};
        d = StatsLib.kthLargest(v, 3);
        x = 1;
        assertEquals("kthLargest ", x, d);
        
        v = new double[] {0,0,0,0,0,0,0,0,0,0};
        d = StatsLib.kthLargest(v, 3);
        x = 0;
        assertEquals("kthLargest ", x, d);
        
        v = new double[] {1,2,1,2,1,2,1,2,1,2};
        d = StatsLib.kthLargest(v, 3);
        x = 2;
        assertEquals("kthLargest ", x, d);
        
        v = new double[] {123.12,33.3333,2d/3d,5.37828,0.999};
        d = StatsLib.kthLargest(v, 3);
        x = 5.37828;
        assertEquals("kthLargest ", x, d);
        
        v = new double[] {-1,-2,-3,-4,-5,-6,-7,-8,-9,-10};
        d = StatsLib.kthLargest(v, 3);
        x = -3;
        assertEquals("kthLargest ", x, d);
    }

    public void testKthSmallest() {
    }

    public void testAvedev() {
        double[] v = null;
        double d, x = 0;
        
        v = new double[] {1,2,3,4,5,6,7,8,9,10};
        d = StatsLib.avedev(v);
        x = 2.5;
        assertEquals("avedev ", x, d);
        
        v = new double[] {1,1,1,1,1,1,1,1,1,1};
        d = StatsLib.avedev(v);
        x = 0;
        assertEquals("avedev ", x, d);
        
        v = new double[] {0,0,0,0,0,0,0,0,0,0};
        d = StatsLib.avedev(v);
        x = 0;
        assertEquals("avedev ", x, d);
        
        v = new double[] {1,2,1,2,1,2,1,2,1,2};
        d = StatsLib.avedev(v);
        x = 0.5;
        assertEquals("avedev ", x, d);
        
        v = new double[] {123.12,33.3333,2d/3d,5.37828,0.999};
        d = StatsLib.avedev(v);
        x = 36.42176053333;
        assertEquals("avedev ", x, d);
        
        v = new double[] {-1,-2,-3,-4,-5,-6,-7,-8,-9,-10};
        d = StatsLib.avedev(v);
        x = 2.5;
        assertEquals("avedev ", x, d);
    }

    public void testMedian() {
        double[] v = null;
        double d, x = 0;
        
        v = new double[] {1,2,3,4,5,6,7,8,9,10};
        d = StatsLib.median(v);
        x = 5.5;
        assertEquals("median ", x, d);
        
        v = new double[] {1,1,1,1,1,1,1,1,1,1};
        d = StatsLib.median(v);
        x = 1;
        assertEquals("median ", x, d);
        
        v = new double[] {0,0,0,0,0,0,0,0,0,0};
        d = StatsLib.median(v);
        x = 0;
        assertEquals("median ", x, d);
        
        v = new double[] {1,2,1,2,1,2,1,2,1,2};
        d = StatsLib.median(v);
        x = 1.5;
        assertEquals("median ", x, d);
        
        v = new double[] {123.12,33.3333,2d/3d,5.37828,0.999};
        d = StatsLib.median(v);
        x = 5.37828;
        assertEquals("median ", x, d);
        
        v = new double[] {-1,-2,-3,-4,-5,-6,-7,-8,-9,-10};
        d = StatsLib.median(v);
        x = -5.5;
        assertEquals("median ", x, d);
        
        v = new double[] {-2,-3,-4,-5,-6,-7,-8,-9,-10};
        d = StatsLib.median(v);
        x = -6;
        assertEquals("median ", x, d);
        
        v = new double[] {1,2,3,4,5,6,7,8,9};
        d = StatsLib.median(v);
        x = 5;
        assertEquals("median ", x, d);
    }

    public void testMode() {
        double[] v;
        double d, x = 0;
        
        v = new double[] {1,2,3,4,5,6,7,8,9,10};
        confirmMode(v, null);
        
        v = new double[] {1,1,1,1,1,1,1,1,1,1};
        confirmMode(v, 1.0);
        
        v = new double[] {0,0,0,0,0,0,0,0,0,0};
        confirmMode(v, 0.0);
        
        v = new double[] {1,2,1,2,1,2,1,2,1,2};
        confirmMode(v, 1.0);
        
        v = new double[] {123.12,33.3333,2d/3d,5.37828,0.999};
        confirmMode(v, null);
        
        v = new double[] {-1,-2,-3,-4,-5,-6,-7,-8,-9,-10};
        confirmMode(v, null);
        
        v = new double[] {1,2,3,4,1,1,1,1,0,0,0,0,0};
        confirmMode(v, 1.0);
        
        v = new double[] {0,1,2,3,4,1,1,1,0,0,0,0,1};
        confirmMode(v, 0.0);
    }
    private static void confirmMode(double[] v, double expectedResult) {
    	confirmMode(v, new Double(expectedResult));
    }
    private static void confirmMode(double[] v, Double expectedResult) {
    	double actual;
		try {
			actual = Mode.evaluate(v);
			if (expectedResult == null) {
				throw new AssertionFailedError("Expected N/A exception was not thrown");
			}
		} catch (EvaluationException e) {
			if (expectedResult == null) {
				assertEquals(ErrorEval.NA, e.getErrorEval());
				return;
			}
			throw new RuntimeException(e);
		}
    	assertEquals("mode", expectedResult.doubleValue(), actual);
    }
    

    public void testStddev() {
        double[] v = null;
        double d, x = 0;
        
        v = new double[] {1,2,3,4,5,6,7,8,9,10};
        d = StatsLib.stdev(v);
        x = 3.02765035410;
        assertEquals("stdev ", x, d);
        
        v = new double[] {1,1,1,1,1,1,1,1,1,1};
        d = StatsLib.stdev(v);
        x = 0;
        assertEquals("stdev ", x, d);
        
        v = new double[] {0,0,0,0,0,0,0,0,0,0};
        d = StatsLib.stdev(v);
        x = 0;
        assertEquals("stdev ", x, d);
        
        v = new double[] {1,2,1,2,1,2,1,2,1,2};
        d = StatsLib.stdev(v);
        x = 0.52704627669;
        assertEquals("stdev ", x, d);
        
        v = new double[] {123.12,33.3333,2d/3d,5.37828,0.999};
        d = StatsLib.stdev(v);
        x = 52.33006233652;
        assertEquals("stdev ", x, d);
        
        v = new double[] {-1,-2,-3,-4,-5,-6,-7,-8,-9,-10};
        d = StatsLib.stdev(v);
        x = 3.02765035410;
        assertEquals("stdev ", x, d);
    }
}
