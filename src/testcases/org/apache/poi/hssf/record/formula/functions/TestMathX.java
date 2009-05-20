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
 * Created on May 23, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.functions.XYNumericFunction.Accumulator;


/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *  
 */
public class TestMathX extends AbstractNumericTestCase {

    public void testAcosh() {
        double d = 0;

        d = MathX.acosh(0);
        assertTrue("Acosh 0 is NaN", Double.isNaN(d));

        d = MathX.acosh(1);
        assertEquals("Acosh 1 ", 0, d);

        d = MathX.acosh(-1);
        assertTrue("Acosh -1 is NaN", Double.isNaN(d));

        d = MathX.acosh(100);
        assertEquals("Acosh 100 ", 5.298292366d, d);

        d = MathX.acosh(101.001);
        assertEquals("Acosh 101.001 ", 5.308253091d, d);

        d = MathX.acosh(200000);
        assertEquals("Acosh 200000 ", 12.89921983d, d);

    }

    public void testAsinh() {
        double d = 0;

        d = MathX.asinh(0);
        assertEquals("asinh 0", d, 0);

        d = MathX.asinh(1);
        assertEquals("asinh 1 ", 0.881373587, d);

        d = MathX.asinh(-1);
        assertEquals("asinh -1 ", -0.881373587, d);

        d = MathX.asinh(-100);
        assertEquals("asinh -100 ", -5.298342366, d);

        d = MathX.asinh(100);
        assertEquals("asinh 100 ", 5.298342366, d);

        d = MathX.asinh(200000);
        assertEquals("asinh 200000", 12.899219826096400, d);

        d = MathX.asinh(-200000);
        assertEquals("asinh -200000 ", -12.899223853137, d);

    }

    public void testAtanh() {
        double d = 0;
        d = MathX.atanh(0);
        assertEquals("atanh 0", d, 0);

        d = MathX.atanh(1);
        assertEquals("atanh 1 ", Double.POSITIVE_INFINITY, d);

        d = MathX.atanh(-1);
        assertEquals("atanh -1 ", Double.NEGATIVE_INFINITY, d);

        d = MathX.atanh(-100);
        assertEquals("atanh -100 ", Double.NaN, d);

        d = MathX.atanh(100);
        assertEquals("atanh 100 ", Double.NaN, d);

        d = MathX.atanh(200000);
        assertEquals("atanh 200000", Double.NaN, d);

        d = MathX.atanh(-200000);
        assertEquals("atanh -200000 ", Double.NaN, d);

        d = MathX.atanh(0.1);
        assertEquals("atanh 0.1", 0.100335348, d);

        d = MathX.atanh(-0.1);
        assertEquals("atanh -0.1 ", -0.100335348, d);

    }

    public void testCosh() {
        double d = 0;
        d = MathX.cosh(0);
        assertEquals("cosh 0", 1, d);

        d = MathX.cosh(1);
        assertEquals("cosh 1 ", 1.543080635, d);

        d = MathX.cosh(-1);
        assertEquals("cosh -1 ", 1.543080635, d);

        d = MathX.cosh(-100);
        assertEquals("cosh -100 ", 1.344058570908070E+43, d);

        d = MathX.cosh(100);
        assertEquals("cosh 100 ", 1.344058570908070E+43, d);

        d = MathX.cosh(15);
        assertEquals("cosh 15", 1634508.686, d);

        d = MathX.cosh(-15);
        assertEquals("cosh -15 ", 1634508.686, d);

        d = MathX.cosh(0.1);
        assertEquals("cosh 0.1", 1.005004168, d);

        d = MathX.cosh(-0.1);
        assertEquals("cosh -0.1 ", 1.005004168, d);

    }

    public void testTanh() {
        double d = 0;
        d = MathX.tanh(0);
        assertEquals("tanh 0", 0, d);

        d = MathX.tanh(1);
        assertEquals("tanh 1 ", 0.761594156, d);

        d = MathX.tanh(-1);
        assertEquals("tanh -1 ", -0.761594156, d);

        d = MathX.tanh(-100);
        assertEquals("tanh -100 ", -1, d);

        d = MathX.tanh(100);
        assertEquals("tanh 100 ", 1, d);

        d = MathX.tanh(15);
        assertEquals("tanh 15", 1, d);

        d = MathX.tanh(-15);
        assertEquals("tanh -15 ", -1, d);

        d = MathX.tanh(0.1);
        assertEquals("tanh 0.1", 0.099667995, d);

        d = MathX.tanh(-0.1);
        assertEquals("tanh -0.1 ", -0.099667995, d);

    }

    public void testMax() {
        double[] d = new double[100];
        d[0] = 1.1;     d[1] = 2.1;     d[2] = 3.1;     d[3] = 4.1; 
        d[4] = 5.1;     d[5] = 6.1;     d[6] = 7.1;     d[7] = 8.1;
        d[8] = 9.1;     d[9] = 10.1;    d[10] = 11.1;   d[11] = 12.1;
        d[12] = 13.1;   d[13] = 14.1;   d[14] = 15.1;   d[15] = 16.1;
        d[16] = 17.1;   d[17] = 18.1;   d[18] = 19.1;   d[19] = 20.1; 
        
        double m = MathX.max(d);
        assertEquals("Max ", 20.1, m);
        
        d = new double[1000];
        m = MathX.max(d);
        assertEquals("Max ", 0, m);
        
        d[0] = -1.1;     d[1] = 2.1;     d[2] = -3.1;     d[3] = 4.1; 
        d[4] = -5.1;     d[5] = 6.1;     d[6] = -7.1;     d[7] = 8.1;
        d[8] = -9.1;     d[9] = 10.1;    d[10] = -11.1;   d[11] = 12.1;
        d[12] = -13.1;   d[13] = 14.1;   d[14] = -15.1;   d[15] = 16.1;
        d[16] = -17.1;   d[17] = 18.1;   d[18] = -19.1;   d[19] = 20.1; 
        m = MathX.max(d);
        assertEquals("Max ", 20.1, m);
        
        d = new double[20];
        d[0] = -1.1;     d[1] = -2.1;     d[2] = -3.1;     d[3] = -4.1; 
        d[4] = -5.1;     d[5] = -6.1;     d[6] = -7.1;     d[7] = -8.1;
        d[8] = -9.1;     d[9] = -10.1;    d[10] = -11.1;   d[11] = -12.1;
        d[12] = -13.1;   d[13] = -14.1;   d[14] = -15.1;   d[15] = -16.1;
        d[16] = -17.1;   d[17] = -18.1;   d[18] = -19.1;   d[19] = -20.1; 
        m = MathX.max(d);
        assertEquals("Max ", -1.1, m);
        
    }

    public void testMin() {
        double[] d = new double[100];
        d[0] = 1.1;     d[1] = 2.1;     d[2] = 3.1;     d[3] = 4.1; 
        d[4] = 5.1;     d[5] = 6.1;     d[6] = 7.1;     d[7] = 8.1;
        d[8] = 9.1;     d[9] = 10.1;    d[10] = 11.1;   d[11] = 12.1;
        d[12] = 13.1;   d[13] = 14.1;   d[14] = 15.1;   d[15] = 16.1;
        d[16] = 17.1;   d[17] = 18.1;   d[18] = 19.1;   d[19] = 20.1; 
        
        double m = MathX.min(d);
        assertEquals("Min ", 0, m);
        
        d = new double[20];
        d[0] = 1.1;     d[1] = 2.1;     d[2] = 3.1;     d[3] = 4.1; 
        d[4] = 5.1;     d[5] = 6.1;     d[6] = 7.1;     d[7] = 8.1;
        d[8] = 9.1;     d[9] = 10.1;    d[10] = 11.1;   d[11] = 12.1;
        d[12] = 13.1;   d[13] = 14.1;   d[14] = 15.1;   d[15] = 16.1;
        d[16] = 17.1;   d[17] = 18.1;   d[18] = 19.1;   d[19] = 20.1; 
        
        m = MathX.min(d);
        assertEquals("Min ", 1.1, m);
        
        d = new double[1000];
        m = MathX.min(d);
        assertEquals("Min ", 0, m);
        
        d[0] = -1.1;     d[1] = 2.1;     d[2] = -3.1;     d[3] = 4.1; 
        d[4] = -5.1;     d[5] = 6.1;     d[6] = -7.1;     d[7] = 8.1;
        d[8] = -9.1;     d[9] = 10.1;    d[10] = -11.1;   d[11] = 12.1;
        d[12] = -13.1;   d[13] = 14.1;   d[14] = -15.1;   d[15] = 16.1;
        d[16] = -17.1;   d[17] = 18.1;   d[18] = -19.1;   d[19] = 20.1; 
        m = MathX.min(d);
        assertEquals("Min ", -19.1, m);
        
        d = new double[20];
        d[0] = -1.1;     d[1] = -2.1;     d[2] = -3.1;     d[3] = -4.1; 
        d[4] = -5.1;     d[5] = -6.1;     d[6] = -7.1;     d[7] = -8.1;
        d[8] = -9.1;     d[9] = -10.1;    d[10] = -11.1;   d[11] = -12.1;
        d[12] = -13.1;   d[13] = -14.1;   d[14] = -15.1;   d[15] = -16.1;
        d[16] = -17.1;   d[17] = -18.1;   d[18] = -19.1;   d[19] = -20.1; 
        m = MathX.min(d);
        assertEquals("Min ", -20.1, m);
    }

    public void testProduct() {
        double[] d = new double[100];
        d[0] = 1.1;     d[1] = 2.1;     d[2] = 3.1;     d[3] = 4.1; 
        d[4] = 5.1;     d[5] = 6.1;     d[6] = 7.1;     d[7] = 8.1;
        d[8] = 9.1;     d[9] = 10.1;    d[10] = 11.1;   d[11] = 12.1;
        d[12] = 13.1;   d[13] = 14.1;   d[14] = 15.1;   d[15] = 16.1;
        d[16] = 17.1;   d[17] = 18.1;   d[18] = 19.1;   d[19] = 20.1; 
        
        double m = MathX.min(d);
        assertEquals("Min ", 0, m);
        
        d = new double[20];
        d[0] = 1.1;     d[1] = 2.1;     d[2] = 3.1;     d[3] = 4.1; 
        d[4] = 5.1;     d[5] = 6.1;     d[6] = 7.1;     d[7] = 8.1;
        d[8] = 9.1;     d[9] = 10.1;    d[10] = 11.1;   d[11] = 12.1;
        d[12] = 13.1;   d[13] = 14.1;   d[14] = 15.1;   d[15] = 16.1;
        d[16] = 17.1;   d[17] = 18.1;   d[18] = 19.1;   d[19] = 20.1; 
        
        m = MathX.min(d);
        assertEquals("Min ", 1.1, m);
        
        d = new double[1000];
        m = MathX.min(d);
        assertEquals("Min ", 0, m);
        
        d[0] = -1.1;     d[1] = 2.1;     d[2] = -3.1;     d[3] = 4.1; 
        d[4] = -5.1;     d[5] = 6.1;     d[6] = -7.1;     d[7] = 8.1;
        d[8] = -9.1;     d[9] = 10.1;    d[10] = -11.1;   d[11] = 12.1;
        d[12] = -13.1;   d[13] = 14.1;   d[14] = -15.1;   d[15] = 16.1;
        d[16] = -17.1;   d[17] = 18.1;   d[18] = -19.1;   d[19] = 20.1; 
        m = MathX.min(d);
        assertEquals("Min ", -19.1, m);
        
        d = new double[20];
        d[0] = -1.1;     d[1] = -2.1;     d[2] = -3.1;     d[3] = -4.1; 
        d[4] = -5.1;     d[5] = -6.1;     d[6] = -7.1;     d[7] = -8.1;
        d[8] = -9.1;     d[9] = -10.1;    d[10] = -11.1;   d[11] = -12.1;
        d[12] = -13.1;   d[13] = -14.1;   d[14] = -15.1;   d[15] = -16.1;
        d[16] = -17.1;   d[17] = -18.1;   d[18] = -19.1;   d[19] = -20.1; 
        m = MathX.min(d);
        assertEquals("Min ", -20.1, m);
    }

    public void testMod() {
    }

    public void testNChooseK() {
        int n=100;
        int k=50;
        double d = MathX.nChooseK(n, k);
        assertEquals("NChooseK ", 1.00891344545564E29, d);
        
        n = -1; k = 1;
        d = MathX.nChooseK(n, k);
        assertEquals("NChooseK ", Double.NaN, d);
        
        n = 1; k = -1;
        d = MathX.nChooseK(n, k);
        assertEquals("NChooseK ", Double.NaN, d);
        
        n = 0; k = 1;
        d = MathX.nChooseK(n, k);
        assertEquals("NChooseK ", Double.NaN, d);
        
        n = 1; k = 0;
        d = MathX.nChooseK(n, k);
        assertEquals("NChooseK ", 1, d);
        
        n = 10; k = 9;
        d = MathX.nChooseK(n, k);
        assertEquals("NChooseK ", 10, d);
        
        n = 10; k = 10;
        d = MathX.nChooseK(n, k);
        assertEquals("NChooseK ", 1, d);
        
        n = 10; k = 1;
        d = MathX.nChooseK(n, k);
        assertEquals("NChooseK ", 10, d);
        
        n = 1000; k = 1;
        d = MathX.nChooseK(n, k);
        assertEquals("NChooseK ", 1000, d); // awesome ;)
        
        n = 1000; k = 2;
        d = MathX.nChooseK(n, k);
        assertEquals("NChooseK ", 499500, d); // awesome ;)
        
        n = 13; k = 7;
        d = MathX.nChooseK(n, k);
        assertEquals("NChooseK ", 1716, d);
        
    }

    public void testSign() {
        final short minus = -1;
        final short zero = 0;
        final short plus = 1;
        double d = 0;
        
        
        assertEquals("Sign ", minus, MathX.sign(minus));
        assertEquals("Sign ", plus, MathX.sign(plus));
        assertEquals("Sign ", zero, MathX.sign(zero));
        
        d = 0;
        assertEquals("Sign ", zero, MathX.sign(d));
        
        d = -1.000001;
        assertEquals("Sign ", minus, MathX.sign(d));
        
        d = -.000001;
        assertEquals("Sign ", minus, MathX.sign(d));
        
        d = -1E-200;
        assertEquals("Sign ", minus, MathX.sign(d));
        
        d = Double.NEGATIVE_INFINITY;
        assertEquals("Sign ", minus, MathX.sign(d));
        
        d = -200.11;
        assertEquals("Sign ", minus, MathX.sign(d));
        
        d = -2000000000000.11;
        assertEquals("Sign ", minus, MathX.sign(d));
        
        d = 1.000001;
        assertEquals("Sign ", plus, MathX.sign(d));
        
        d = .000001;
        assertEquals("Sign ", plus, MathX.sign(d));
        
        d = 1E-200;
        assertEquals("Sign ", plus, MathX.sign(d));
        
        d = Double.POSITIVE_INFINITY;
        assertEquals("Sign ", plus, MathX.sign(d));
        
        d = 200.11;
        assertEquals("Sign ", plus, MathX.sign(d));
        
        d = 2000000000000.11;
        assertEquals("Sign ", plus, MathX.sign(d));
        
    }

    public void testSinh() {
        double d = 0;
        d = MathX.sinh(0);
        assertEquals("sinh 0", 0, d);

        d = MathX.sinh(1);
        assertEquals("sinh 1 ", 1.175201194, d);

        d = MathX.sinh(-1);
        assertEquals("sinh -1 ", -1.175201194, d);

        d = MathX.sinh(-100);
        assertEquals("sinh -100 ", -1.344058570908070E+43, d);

        d = MathX.sinh(100);
        assertEquals("sinh 100 ", 1.344058570908070E+43, d);

        d = MathX.sinh(15);
        assertEquals("sinh 15", 1634508.686, d);

        d = MathX.sinh(-15);
        assertEquals("sinh -15 ", -1634508.686, d);

        d = MathX.sinh(0.1);
        assertEquals("sinh 0.1", 0.10016675, d);

        d = MathX.sinh(-0.1);
        assertEquals("sinh -0.1 ", -0.10016675, d);

    }

    public void testSum() {
        double[] d = new double[100];
        d[0] = 1.1;     d[1] = 2.1;     d[2] = 3.1;     d[3] = 4.1; 
        d[4] = 5.1;     d[5] = 6.1;     d[6] = 7.1;     d[7] = 8.1;
        d[8] = 9.1;     d[9] = 10.1;    d[10] = 11.1;   d[11] = 12.1;
        d[12] = 13.1;   d[13] = 14.1;   d[14] = 15.1;   d[15] = 16.1;
        d[16] = 17.1;   d[17] = 18.1;   d[18] = 19.1;   d[19] = 20.1; 
        
        double s = MathX.sum(d);
        assertEquals("Sum ", 212, s);
        
        d = new double[1000];
        s = MathX.sum(d);
        assertEquals("Sum ", 0, s);
        
        d[0] = -1.1;     d[1] = 2.1;     d[2] = -3.1;     d[3] = 4.1; 
        d[4] = -5.1;     d[5] = 6.1;     d[6] = -7.1;     d[7] = 8.1;
        d[8] = -9.1;     d[9] = 10.1;    d[10] = -11.1;   d[11] = 12.1;
        d[12] = -13.1;   d[13] = 14.1;   d[14] = -15.1;   d[15] = 16.1;
        d[16] = -17.1;   d[17] = 18.1;   d[18] = -19.1;   d[19] = 20.1; 
        s = MathX.sum(d);
        assertEquals("Sum ", 10, s);
        
        d[0] = -1.1;     d[1] = -2.1;     d[2] = -3.1;     d[3] = -4.1; 
        d[4] = -5.1;     d[5] = -6.1;     d[6] = -7.1;     d[7] = -8.1;
        d[8] = -9.1;     d[9] = -10.1;    d[10] = -11.1;   d[11] = -12.1;
        d[12] = -13.1;   d[13] = -14.1;   d[14] = -15.1;   d[15] = -16.1;
        d[16] = -17.1;   d[17] = -18.1;   d[18] = -19.1;   d[19] = -20.1; 
        s = MathX.sum(d);
        assertEquals("Sum ", -212, s);
        
    }

    public void testSumsq() {
        double[] d = new double[100];
        d[0] = 1.1;     d[1] = 2.1;     d[2] = 3.1;     d[3] = 4.1; 
        d[4] = 5.1;     d[5] = 6.1;     d[6] = 7.1;     d[7] = 8.1;
        d[8] = 9.1;     d[9] = 10.1;    d[10] = 11.1;   d[11] = 12.1;
        d[12] = 13.1;   d[13] = 14.1;   d[14] = 15.1;   d[15] = 16.1;
        d[16] = 17.1;   d[17] = 18.1;   d[18] = 19.1;   d[19] = 20.1; 
        
        double s = MathX.sumsq(d);
        assertEquals("Sumsq ", 2912.2, s);
        
        d = new double[1000];
        s = MathX.sumsq(d);
        assertEquals("Sumsq ", 0, s);
        
        d[0] = -1.1;     d[1] = 2.1;     d[2] = -3.1;     d[3] = 4.1; 
        d[4] = -5.1;     d[5] = 6.1;     d[6] = -7.1;     d[7] = 8.1;
        d[8] = -9.1;     d[9] = 10.1;    d[10] = -11.1;   d[11] = 12.1;
        d[12] = -13.1;   d[13] = 14.1;   d[14] = -15.1;   d[15] = 16.1;
        d[16] = -17.1;   d[17] = 18.1;   d[18] = -19.1;   d[19] = 20.1; 
        s = MathX.sumsq(d);
        assertEquals("Sumsq ", 2912.2, s);
        
        d[0] = -1.1;     d[1] = -2.1;     d[2] = -3.1;     d[3] = -4.1; 
        d[4] = -5.1;     d[5] = -6.1;     d[6] = -7.1;     d[7] = -8.1;
        d[8] = -9.1;     d[9] = -10.1;    d[10] = -11.1;   d[11] = -12.1;
        d[12] = -13.1;   d[13] = -14.1;   d[14] = -15.1;   d[15] = -16.1;
        d[16] = -17.1;   d[17] = -18.1;   d[18] = -19.1;   d[19] = -20.1; 
        s = MathX.sumsq(d);
        assertEquals("Sumsq ", 2912.2, s);
    }

    public void testFactorial() {
        int n = 0;
        double s = 0;
        
        n = 0;
        s = MathX.factorial(n);
        assertEquals("Factorial ", 1, s);
        
        n = 1;
        s = MathX.factorial(n);
        assertEquals("Factorial ", 1, s);
        
        n = 10;
        s = MathX.factorial(n);
        assertEquals("Factorial ", 3628800, s);
        
        n = 99;
        s = MathX.factorial(n);
        assertEquals("Factorial ", 9.33262154439E+155, s);
        
        n = -1;
        s = MathX.factorial(n);
        assertEquals("Factorial ", Double.NaN, s);
        
        n = Integer.MAX_VALUE;
        s = MathX.factorial(n);
        assertEquals("Factorial ", Double.POSITIVE_INFINITY, s);
    }

    public void testSumx2my2() {
        double[] xarr = null;
        double[] yarr = null;
        
        xarr = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        yarr = new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        confirmSumx2my2(xarr, yarr, 100);
        
        xarr = new double[]{-1, -2, -3, -4, -5, -6, -7, -8, -9, -10};
        yarr = new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        confirmSumx2my2(xarr, yarr, 100);
        
        xarr = new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        yarr = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        confirmSumx2my2(xarr, yarr, -100);
        
        xarr = new double[]{10};
        yarr = new double[]{9};
        confirmSumx2my2(xarr, yarr, 19);
        
        xarr = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        yarr = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        confirmSumx2my2(xarr, yarr, 0);
    }

    public void testSumx2py2() {
        double[] xarr = null;
        double[] yarr = null;
        
        xarr = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        yarr = new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        confirmSumx2py2(xarr, yarr, 670);
        
        xarr = new double[]{-1, -2, -3, -4, -5, -6, -7, -8, -9, -10};
        yarr = new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        confirmSumx2py2(xarr, yarr, 670);
        
        xarr = new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        yarr = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        confirmSumx2py2(xarr, yarr, 670);
        
        xarr = new double[]{10};
        yarr = new double[]{9};
        confirmSumx2py2(xarr, yarr, 181);
        
        xarr = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        yarr = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        confirmSumx2py2(xarr, yarr, 770);
    }

    public void testSumxmy2() {
        double[] xarr = null;
        double[] yarr = null;
        
        xarr = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        yarr = new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        confirmSumxmy2(xarr, yarr, 10);
        
        xarr = new double[]{-1, -2, -3, -4, -5, -6, -7, -8, -9, -10};
        yarr = new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        confirmSumxmy2(xarr, yarr, 1330);
        
        xarr = new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        yarr = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        confirmSumxmy2(xarr, yarr, 10);
        
        xarr = new double[]{10};
        yarr = new double[]{9};
        confirmSumxmy2(xarr, yarr, 1);
        
        xarr = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        yarr = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        confirmSumxmy2(xarr, yarr, 0);
    }

    private static void confirmSumx2my2(double[] xarr, double[] yarr, double expectedResult) {
        confirmXY(new Sumx2my2().createAccumulator(), xarr, yarr, expectedResult);
    }
    private static void confirmSumx2py2(double[] xarr, double[] yarr, double expectedResult) {
        confirmXY(new Sumx2py2().createAccumulator(), xarr, yarr, expectedResult);
    }
    private static void confirmSumxmy2(double[] xarr, double[] yarr, double expectedResult) {
        confirmXY(new Sumxmy2().createAccumulator(), xarr, yarr, expectedResult);
    }

    private static void confirmXY(Accumulator acc, double[] xarr, double[] yarr,
            double expectedResult) {
        double result = 0.0;
        for (int i = 0; i < xarr.length; i++) {
            result += acc.accumulate(xarr[i], yarr[i]);
        }
        assertEquals(expectedResult, result, 0.0);
    }
    
    public void testRound() {
        double d = 0;
        int p = 0;
        
        d = 0; p = 0;
        assertEquals("round ", 0, MathX.round(d, p));
        
        d = 10; p = 0;
        assertEquals("round ", 10, MathX.round(d, p));
        
        d = 123.23; p = 0;
        assertEquals("round ", 123, MathX.round(d, p));
        
        d = -123.23; p = 0;
        assertEquals("round ", -123, MathX.round(d, p));
        
        d = 123.12; p = 2;
        assertEquals("round ", 123.12, MathX.round(d, p));
        
        d = 88.123459; p = 5;
        assertEquals("round ", 88.12346, MathX.round(d, p));
        
        d = 0; p = 2;
        assertEquals("round ", 0, MathX.round(d, p));
        
        d = 0; p = -1;
        assertEquals("round ", 0, MathX.round(d, p));
        
        d = 0.01; p = -1;
        assertEquals("round ", 0, MathX.round(d, p));

        d = 123.12; p = -2;
        assertEquals("round ", 100, MathX.round(d, p));
        
        d = 88.123459; p = -3;
        assertEquals("round ", 0, MathX.round(d, p));
        
        d = 49.00000001; p = -1;
        assertEquals("round ", 50, MathX.round(d, p));
        
        d = 149.999999; p = -2;
        assertEquals("round ", 100, MathX.round(d, p));
        
        d = 150.0; p = -2;
        assertEquals("round ", 200, MathX.round(d, p));
    }

    public void testRoundDown() {
        double d = 0;
        int p = 0;
        
        d = 0; p = 0;
        assertEquals("roundDown ", 0, MathX.roundDown(d, p));
        
        d = 10; p = 0;
        assertEquals("roundDown ", 10, MathX.roundDown(d, p));
        
        d = 123.99; p = 0;
        assertEquals("roundDown ", 123, MathX.roundDown(d, p));
        
        d = -123.99; p = 0;
        assertEquals("roundDown ", -123, MathX.roundDown(d, p));
        
        d = 123.99; p = 2;
        assertEquals("roundDown ", 123.99, MathX.roundDown(d, p));
        
        d = 88.123459; p = 5;
        assertEquals("roundDown ", 88.12345, MathX.roundDown(d, p));
        
        d = 0; p = 2;
        assertEquals("roundDown ", 0, MathX.roundDown(d, p));
        
        d = 0; p = -1;
        assertEquals("roundDown ", 0, MathX.roundDown(d, p));
        
        d = 0.01; p = -1;
        assertEquals("roundDown ", 0, MathX.roundDown(d, p));

        d = 199.12; p = -2;
        assertEquals("roundDown ", 100, MathX.roundDown(d, p));
        
        d = 88.123459; p = -3;
        assertEquals("roundDown ", 0, MathX.roundDown(d, p));
        
        d = 99.00000001; p = -1;
        assertEquals("roundDown ", 90, MathX.roundDown(d, p));
        
        d = 100.00001; p = -2;
        assertEquals("roundDown ", 100, MathX.roundDown(d, p));
        
        d = 150.0; p = -2;
        assertEquals("roundDown ", 100, MathX.roundDown(d, p));
    }

    public void testRoundUp() {
        double d = 0;
        int p = 0;
        
        d = 0; p = 0;
        assertEquals("roundUp ", 0, MathX.roundUp(d, p));
        
        d = 10; p = 0;
        assertEquals("roundUp ", 10, MathX.roundUp(d, p));
        
        d = 123.23; p = 0;
        assertEquals("roundUp ", 124, MathX.roundUp(d, p));
        
        d = -123.23; p = 0;
        assertEquals("roundUp ", -124, MathX.roundUp(d, p));
        
        d = 123.12; p = 2;
        assertEquals("roundUp ", 123.12, MathX.roundUp(d, p));
        
        d = 88.123459; p = 5;
        assertEquals("roundUp ", 88.12346, MathX.roundUp(d, p));
        
        d = 0; p = 2;
        assertEquals("roundUp ", 0, MathX.roundUp(d, p));
        
        d = 0; p = -1;
        assertEquals("roundUp ", 0, MathX.roundUp(d, p));
        
        d = 0.01; p = -1;
        assertEquals("roundUp ", 10, MathX.roundUp(d, p));

        d = 123.12; p = -2;
        assertEquals("roundUp ", 200, MathX.roundUp(d, p));
        
        d = 88.123459; p = -3;
        assertEquals("roundUp ", 1000, MathX.roundUp(d, p));
        
        d = 49.00000001; p = -1;
        assertEquals("roundUp ", 50, MathX.roundUp(d, p));
        
        d = 149.999999; p = -2;
        assertEquals("roundUp ", 200, MathX.roundUp(d, p));
        
        d = 150.0; p = -2;
        assertEquals("roundUp ", 200, MathX.roundUp(d, p));
    }

    public void testCeiling() {
        double d = 0;
        double s = 0;
        
        d = 0; s = 0;
        assertEquals("ceiling ", 0, MathX.ceiling(d, s));
        
        d = 1; s = 0;
        assertEquals("ceiling ", 0, MathX.ceiling(d, s));
        
        d = 0; s = 1;
        assertEquals("ceiling ", 0, MathX.ceiling(d, s));
        
        d = -1; s = 0;
        assertEquals("ceiling ", 0, MathX.ceiling(d, s));
        
        d = 0; s = -1;
        assertEquals("ceiling ", 0, MathX.ceiling(d, s));
        
        d = 10; s = 1.11;
        assertEquals("ceiling ", 11.1, MathX.ceiling(d, s));
        
        d = 11.12333; s = 0.03499;
        assertEquals("ceiling ", 11.12682, MathX.ceiling(d, s));
        
        d = -11.12333; s = 0.03499;
        assertEquals("ceiling ", Double.NaN, MathX.ceiling(d, s));
        
        d = 11.12333; s = -0.03499;
        assertEquals("ceiling ", Double.NaN, MathX.ceiling(d, s));
        
        d = -11.12333; s = -0.03499;
        assertEquals("ceiling ", -11.12682, MathX.ceiling(d, s));
        
        d = 100; s = 0.001;
        assertEquals("ceiling ", 100, MathX.ceiling(d, s));
        
        d = -0.001; s = -9.99;
        assertEquals("ceiling ", -9.99, MathX.ceiling(d, s));
        
        d = 4.42; s = 0.05;
        assertEquals("ceiling ", 4.45, MathX.ceiling(d, s));
        
        d = 0.05; s = 4.42;
        assertEquals("ceiling ", 4.42, MathX.ceiling(d, s));
        
        d = 0.6666; s = 3.33;
        assertEquals("ceiling ", 3.33, MathX.ceiling(d, s));
        
        d = 2d/3; s = 3.33;
        assertEquals("ceiling ", 3.33, MathX.ceiling(d, s));
    }

    public void testFloor() {
        double d = 0;
        double s = 0;
        
        d = 0; s = 0;
        assertEquals("floor ", 0, MathX.floor(d, s));
        
        d = 1; s = 0;
        assertEquals("floor ", Double.NaN, MathX.floor(d, s));
        
        d = 0; s = 1;
        assertEquals("floor ", 0, MathX.floor(d, s));
        
        d = -1; s = 0;
        assertEquals("floor ", Double.NaN, MathX.floor(d, s));
        
        d = 0; s = -1;
        assertEquals("floor ", 0, MathX.floor(d, s));
        
        d = 10; s = 1.11;
        assertEquals("floor ", 9.99, MathX.floor(d, s));
        
        d = 11.12333; s = 0.03499;
        assertEquals("floor ", 11.09183, MathX.floor(d, s));
        
        d = -11.12333; s = 0.03499;
        assertEquals("floor ", Double.NaN, MathX.floor(d, s));
        
        d = 11.12333; s = -0.03499;
        assertEquals("floor ", Double.NaN, MathX.floor(d, s));
        
        d = -11.12333; s = -0.03499;
        assertEquals("floor ", -11.09183, MathX.floor(d, s));
        
        d = 100; s = 0.001;
        assertEquals("floor ", 100, MathX.floor(d, s));
        
        d = -0.001; s = -9.99;
        assertEquals("floor ", 0, MathX.floor(d, s));
        
        d = 4.42; s = 0.05;
        assertEquals("floor ", 4.4, MathX.floor(d, s));
        
        d = 0.05; s = 4.42;
        assertEquals("floor ", 0, MathX.floor(d, s));
        
        d = 0.6666; s = 3.33;
        assertEquals("floor ", 0, MathX.floor(d, s));
        
        d = 2d/3; s = 3.33;
        assertEquals("floor ", 0, MathX.floor(d, s));
    }

}
