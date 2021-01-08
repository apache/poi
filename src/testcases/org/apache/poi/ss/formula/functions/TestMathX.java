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
package org.apache.poi.ss.formula.functions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ss.formula.functions.XYNumericFunction.Accumulator;
import org.junit.jupiter.api.Test;


/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public class TestMathX extends BaseTestNumeric {

    @Test
    void testAcosh() {
        double d;

        d = MathX.acosh(0);
        assertTrue( Double.isNaN(d), "Acosh 0 is NaN" );

        d = MathX.acosh(1);
        assertDouble("Acosh 1 ", 0, d);

        d = MathX.acosh(-1);
        assertTrue( Double.isNaN(d), "Acosh -1 is NaN" );

        d = MathX.acosh(100);
        assertDouble("Acosh 100 ", 5.298292366d, d);

        d = MathX.acosh(101.001);
        assertDouble("Acosh 101.001 ", 5.308253091d, d);

        d = MathX.acosh(200000);
        assertDouble("Acosh 200000 ", 12.89921983d, d);

    }

    @Test
    void testAsinh() {
        double d;

        d = MathX.asinh(0);
        assertDouble("asinh 0", d, 0);

        d = MathX.asinh(1);
        assertDouble("asinh 1 ", 0.881373587, d);

        d = MathX.asinh(-1);
        assertDouble("asinh -1 ", -0.881373587, d);

        d = MathX.asinh(-100);
        assertDouble("asinh -100 ", -5.298342366, d);

        d = MathX.asinh(100);
        assertDouble("asinh 100 ", 5.298342366, d);

        d = MathX.asinh(200000);
        assertDouble("asinh 200000", 12.899219826096400, d);

        d = MathX.asinh(-200000);
        assertDouble("asinh -200000 ", -12.899223853137, d);

    }

    @Test
    void testAtanh() {
        double d;
        d = MathX.atanh(0);
        assertDouble("atanh 0", d, 0);

        d = MathX.atanh(1);
        assertDouble("atanh 1 ", Double.POSITIVE_INFINITY, d);

        d = MathX.atanh(-1);
        assertDouble("atanh -1 ", Double.NEGATIVE_INFINITY, d);

        d = MathX.atanh(-100);
        assertDouble("atanh -100 ", Double.NaN, d);

        d = MathX.atanh(100);
        assertDouble("atanh 100 ", Double.NaN, d);

        d = MathX.atanh(200000);
        assertDouble("atanh 200000", Double.NaN, d);

        d = MathX.atanh(-200000);
        assertDouble("atanh -200000 ", Double.NaN, d);

        d = MathX.atanh(0.1);
        assertDouble("atanh 0.1", 0.100335348, d);

        d = MathX.atanh(-0.1);
        assertDouble("atanh -0.1 ", -0.100335348, d);

    }

    @Test
    void testCosh() {
        double d;
        d = MathX.cosh(0);
        assertDouble("cosh 0", 1, d);

        d = MathX.cosh(1);
        assertDouble("cosh 1 ", 1.543080635, d);

        d = MathX.cosh(-1);
        assertDouble("cosh -1 ", 1.543080635, d);

        d = MathX.cosh(-100);
        assertDouble("cosh -100 ", 1.344058570908070E+43, d);

        d = MathX.cosh(100);
        assertDouble("cosh 100 ", 1.344058570908070E+43, d);

        d = MathX.cosh(15);
        assertDouble("cosh 15", 1634508.686, d);

        d = MathX.cosh(-15);
        assertDouble("cosh -15 ", 1634508.686, d);

        d = MathX.cosh(0.1);
        assertDouble("cosh 0.1", 1.005004168, d);

        d = MathX.cosh(-0.1);
        assertDouble("cosh -0.1 ", 1.005004168, d);

    }

    @Test
    void testTanh() {
        double d;
        d = MathX.tanh(0);
        assertDouble("tanh 0", 0, d);

        d = MathX.tanh(1);
        assertDouble("tanh 1 ", 0.761594156, d);

        d = MathX.tanh(-1);
        assertDouble("tanh -1 ", -0.761594156, d);

        d = MathX.tanh(-100);
        assertDouble("tanh -100 ", -1, d);

        d = MathX.tanh(100);
        assertDouble("tanh 100 ", 1, d);

        d = MathX.tanh(15);
        assertDouble("tanh 15", 1, d);

        d = MathX.tanh(-15);
        assertDouble("tanh -15 ", -1, d);

        d = MathX.tanh(0.1);
        assertDouble("tanh 0.1", 0.099667995, d);

        d = MathX.tanh(-0.1);
        assertDouble("tanh -0.1 ", -0.099667995, d);

    }

    @Test
    void testMax() {
        double[] d = new double[100];
        d[0] = 1.1;     d[1] = 2.1;     d[2] = 3.1;     d[3] = 4.1;
        d[4] = 5.1;     d[5] = 6.1;     d[6] = 7.1;     d[7] = 8.1;
        d[8] = 9.1;     d[9] = 10.1;    d[10] = 11.1;   d[11] = 12.1;
        d[12] = 13.1;   d[13] = 14.1;   d[14] = 15.1;   d[15] = 16.1;
        d[16] = 17.1;   d[17] = 18.1;   d[18] = 19.1;   d[19] = 20.1;

        double m = MathX.max(d);
        assertDouble("Max ", 20.1, m);

        d = new double[1000];
        m = MathX.max(d);
        assertDouble("Max ", 0, m);

        d[0] = -1.1;     d[1] = 2.1;     d[2] = -3.1;     d[3] = 4.1;
        d[4] = -5.1;     d[5] = 6.1;     d[6] = -7.1;     d[7] = 8.1;
        d[8] = -9.1;     d[9] = 10.1;    d[10] = -11.1;   d[11] = 12.1;
        d[12] = -13.1;   d[13] = 14.1;   d[14] = -15.1;   d[15] = 16.1;
        d[16] = -17.1;   d[17] = 18.1;   d[18] = -19.1;   d[19] = 20.1;
        m = MathX.max(d);
        assertDouble("Max ", 20.1, m);

        d = new double[20];
        d[0] = -1.1;     d[1] = -2.1;     d[2] = -3.1;     d[3] = -4.1;
        d[4] = -5.1;     d[5] = -6.1;     d[6] = -7.1;     d[7] = -8.1;
        d[8] = -9.1;     d[9] = -10.1;    d[10] = -11.1;   d[11] = -12.1;
        d[12] = -13.1;   d[13] = -14.1;   d[14] = -15.1;   d[15] = -16.1;
        d[16] = -17.1;   d[17] = -18.1;   d[18] = -19.1;   d[19] = -20.1;
        m = MathX.max(d);
        assertDouble("Max ", -1.1, m);

    }

    @Test
    void testMin() {
        double[] d = new double[100];
        d[0] = 1.1;     d[1] = 2.1;     d[2] = 3.1;     d[3] = 4.1;
        d[4] = 5.1;     d[5] = 6.1;     d[6] = 7.1;     d[7] = 8.1;
        d[8] = 9.1;     d[9] = 10.1;    d[10] = 11.1;   d[11] = 12.1;
        d[12] = 13.1;   d[13] = 14.1;   d[14] = 15.1;   d[15] = 16.1;
        d[16] = 17.1;   d[17] = 18.1;   d[18] = 19.1;   d[19] = 20.1;

        double m = MathX.min(d);
        assertDouble("Min ", 0, m);

        d = new double[20];
        d[0] = 1.1;     d[1] = 2.1;     d[2] = 3.1;     d[3] = 4.1;
        d[4] = 5.1;     d[5] = 6.1;     d[6] = 7.1;     d[7] = 8.1;
        d[8] = 9.1;     d[9] = 10.1;    d[10] = 11.1;   d[11] = 12.1;
        d[12] = 13.1;   d[13] = 14.1;   d[14] = 15.1;   d[15] = 16.1;
        d[16] = 17.1;   d[17] = 18.1;   d[18] = 19.1;   d[19] = 20.1;

        m = MathX.min(d);
        assertDouble("Min ", 1.1, m);

        d = new double[1000];
        m = MathX.min(d);
        assertDouble("Min ", 0, m);

        d[0] = -1.1;     d[1] = 2.1;     d[2] = -3.1;     d[3] = 4.1;
        d[4] = -5.1;     d[5] = 6.1;     d[6] = -7.1;     d[7] = 8.1;
        d[8] = -9.1;     d[9] = 10.1;    d[10] = -11.1;   d[11] = 12.1;
        d[12] = -13.1;   d[13] = 14.1;   d[14] = -15.1;   d[15] = 16.1;
        d[16] = -17.1;   d[17] = 18.1;   d[18] = -19.1;   d[19] = 20.1;
        m = MathX.min(d);
        assertDouble("Min ", -19.1, m);

        d = new double[20];
        d[0] = -1.1;     d[1] = -2.1;     d[2] = -3.1;     d[3] = -4.1;
        d[4] = -5.1;     d[5] = -6.1;     d[6] = -7.1;     d[7] = -8.1;
        d[8] = -9.1;     d[9] = -10.1;    d[10] = -11.1;   d[11] = -12.1;
        d[12] = -13.1;   d[13] = -14.1;   d[14] = -15.1;   d[15] = -16.1;
        d[16] = -17.1;   d[17] = -18.1;   d[18] = -19.1;   d[19] = -20.1;
        m = MathX.min(d);
        assertDouble("Min ", -20.1, m);
    }

    @Test
    void testProduct() {
        assertDouble("Product ", 0, MathX.product(null));
        assertDouble("Product ", 0, MathX.product(new double[] {}));
        assertDouble("Product ", 0, MathX.product(new double[] {1, 0}));

        assertDouble("Product ", 1, MathX.product(new double[] { 1 }));
        assertDouble("Product ", 1, MathX.product(new double[] { 1, 1 }));
        assertDouble("Product ", 10, MathX.product(new double[] { 10, 1 }));
        assertDouble("Product ", -2, MathX.product(new double[] { 2, -1 }));
        assertDouble("Product ", 99988000209999d, MathX.product(new double[] { 99999, 99999, 9999 }));

        double[] d = new double[100];
        d[0] = 1.1;     d[1] = 2.1;     d[2] = 3.1;     d[3] = 4.1;
        d[4] = 5.1;     d[5] = 6.1;     d[6] = 7.1;     d[7] = 8.1;
        d[8] = 9.1;     d[9] = 10.1;    d[10] = 11.1;   d[11] = 12.1;
        d[12] = 13.1;   d[13] = 14.1;   d[14] = 15.1;   d[15] = 16.1;
        d[16] = 17.1;   d[17] = 18.1;   d[18] = 19.1;   d[19] = 20.1;

        double m = MathX.product(d);
        assertDouble("Product ", 0, m);

        d = new double[20];
        d[0] = 1.1;     d[1] = 2.1;     d[2] = 3.1;     d[3] = 4.1;
        d[4] = 5.1;     d[5] = 6.1;     d[6] = 7.1;     d[7] = 8.1;
        d[8] = 9.1;     d[9] = 10.1;    d[10] = 11.1;   d[11] = 12.1;
        d[12] = 13.1;   d[13] = 14.1;   d[14] = 15.1;   d[15] = 16.1;
        d[16] = 17.1;   d[17] = 18.1;   d[18] = 19.1;   d[19] = 20.1;

        m = MathX.product(d);
        assertDouble("Product ", 3459946360003355534d, m);

        d = new double[1000];
        m = MathX.product(d);
        assertDouble("Product ", 0, m);

        d = new double[20];
        d[0] = -1.1;     d[1] = -2.1;     d[2] = -3.1;     d[3] = -4.1;
        d[4] = -5.1;     d[5] = -6.1;     d[6] = -7.1;     d[7] = -8.1;
        d[8] = -9.1;     d[9] = -10.1;    d[10] = -11.1;   d[11] = -12.1;
        d[12] = -13.1;   d[13] = -14.1;   d[14] = -15.1;   d[15] = -16.1;
        d[16] = -17.1;   d[17] = -18.1;   d[18] = -19.1;   d[19] = -20.1;
        m = MathX.product(d);
        assertDouble("Product ", 3459946360003355534d, m);
    }

    @Test
    void testMod() {

        //example from Excel help
        assertDouble(1.0, MathX.mod(3, 2));
        assertDouble(1.0, MathX.mod(-3, 2));
        assertDouble(-1.0, MathX.mod(3, -2));
        assertDouble(-1.0, MathX.mod(-3, -2));

        assertDouble(0.0, MathX.mod(0, 2));
        assertDouble(Double.NaN, MathX.mod(3, 0));
        assertDouble(1.4, MathX.mod(3.4, 2));
        assertDouble(-1.4, MathX.mod(-3.4, -2));
        assertDouble(0.6000000000000001, MathX.mod(-3.4, 2.0));// should actually be 0.6
        assertDouble(-0.6000000000000001, MathX.mod(3.4, -2.0));// should actually be -0.6
        assertDouble(3.0, MathX.mod(3, Double.MAX_VALUE));
        assertDouble(2.0, MathX.mod(Double.MAX_VALUE, 3));

        // Bugzilla 50033
        assertDouble(1.0, MathX.mod(13, 12));
    }

    @Test
    void testNChooseK() {
        int n=100;
        int k=50;
        double d = MathX.nChooseK(n, k);
        assertDouble("NChooseK ", 1.00891344545564E29, d);

        n = -1; k = 1;
        d = MathX.nChooseK(n, k);
        assertDouble("NChooseK ", Double.NaN, d);

        n = 1; k = -1;
        d = MathX.nChooseK(n, k);
        assertDouble("NChooseK ", Double.NaN, d);

        n = 0; k = 1;
        d = MathX.nChooseK(n, k);
        assertDouble("NChooseK ", Double.NaN, d);

        n = 1; k = 0;
        d = MathX.nChooseK(n, k);
        assertDouble("NChooseK ", 1, d);

        n = 10; k = 9;
        d = MathX.nChooseK(n, k);
        assertDouble("NChooseK ", 10, d);

        n = 10; k = 10;
        d = MathX.nChooseK(n, k);
        assertDouble("NChooseK ", 1, d);

        n = 10; k = 1;
        d = MathX.nChooseK(n, k);
        assertDouble("NChooseK ", 10, d);

        n = 1000; k = 1;
        d = MathX.nChooseK(n, k);
        assertDouble("NChooseK ", 1000, d); // awesome ;)

        n = 1000; k = 2;
        d = MathX.nChooseK(n, k);
        assertDouble("NChooseK ", 499500, d); // awesome ;)

        n = 13; k = 7;
        d = MathX.nChooseK(n, k);
        assertDouble("NChooseK ", 1716, d);

    }

    @Test
    void testSign() {
        final short minus = -1;
        final short zero = 0;
        final short plus = 1;
        double d;


        assertDouble("Sign ", minus, MathX.sign(minus));
        assertDouble("Sign ", plus, MathX.sign(plus));
        assertDouble("Sign ", zero, MathX.sign(zero));

        d = 0;
        assertDouble("Sign ", zero, MathX.sign(d));

        d = -1.000001;
        assertDouble("Sign ", minus, MathX.sign(d));

        d = -.000001;
        assertDouble("Sign ", minus, MathX.sign(d));

        d = -1E-200;
        assertDouble("Sign ", minus, MathX.sign(d));

        d = Double.NEGATIVE_INFINITY;
        assertDouble("Sign ", minus, MathX.sign(d));

        d = -200.11;
        assertDouble("Sign ", minus, MathX.sign(d));

        d = -2000000000000.11;
        assertDouble("Sign ", minus, MathX.sign(d));

        d = 1.000001;
        assertDouble("Sign ", plus, MathX.sign(d));

        d = .000001;
        assertDouble("Sign ", plus, MathX.sign(d));

        d = 1E-200;
        assertDouble("Sign ", plus, MathX.sign(d));

        d = Double.POSITIVE_INFINITY;
        assertDouble("Sign ", plus, MathX.sign(d));

        d = 200.11;
        assertDouble("Sign ", plus, MathX.sign(d));

        d = 2000000000000.11;
        assertDouble("Sign ", plus, MathX.sign(d));

    }

    @Test
    void testSinh() {
        double d;
        d = MathX.sinh(0);
        assertDouble("sinh 0", 0, d);

        d = MathX.sinh(1);
        assertDouble("sinh 1 ", 1.175201194, d);

        d = MathX.sinh(-1);
        assertDouble("sinh -1 ", -1.175201194, d);

        d = MathX.sinh(-100);
        assertDouble("sinh -100 ", -1.344058570908070E+43, d);

        d = MathX.sinh(100);
        assertDouble("sinh 100 ", 1.344058570908070E+43, d);

        d = MathX.sinh(15);
        assertDouble("sinh 15", 1634508.686, d);

        d = MathX.sinh(-15);
        assertDouble("sinh -15 ", -1634508.686, d);

        d = MathX.sinh(0.1);
        assertDouble("sinh 0.1", 0.10016675, d);

        d = MathX.sinh(-0.1);
        assertDouble("sinh -0.1 ", -0.10016675, d);

    }

    @Test
    void testSum() {
        double[] d = new double[100];
        d[0] = 1.1;     d[1] = 2.1;     d[2] = 3.1;     d[3] = 4.1;
        d[4] = 5.1;     d[5] = 6.1;     d[6] = 7.1;     d[7] = 8.1;
        d[8] = 9.1;     d[9] = 10.1;    d[10] = 11.1;   d[11] = 12.1;
        d[12] = 13.1;   d[13] = 14.1;   d[14] = 15.1;   d[15] = 16.1;
        d[16] = 17.1;   d[17] = 18.1;   d[18] = 19.1;   d[19] = 20.1;

        double s = MathX.sum(d);
        assertDouble("Sum ", 212, s);

        d = new double[1000];
        s = MathX.sum(d);
        assertDouble("Sum ", 0, s);

        d[0] = -1.1;     d[1] = 2.1;     d[2] = -3.1;     d[3] = 4.1;
        d[4] = -5.1;     d[5] = 6.1;     d[6] = -7.1;     d[7] = 8.1;
        d[8] = -9.1;     d[9] = 10.1;    d[10] = -11.1;   d[11] = 12.1;
        d[12] = -13.1;   d[13] = 14.1;   d[14] = -15.1;   d[15] = 16.1;
        d[16] = -17.1;   d[17] = 18.1;   d[18] = -19.1;   d[19] = 20.1;
        s = MathX.sum(d);
        assertDouble("Sum ", 10, s);

        d[0] = -1.1;     d[1] = -2.1;     d[2] = -3.1;     d[3] = -4.1;
        d[4] = -5.1;     d[5] = -6.1;     d[6] = -7.1;     d[7] = -8.1;
        d[8] = -9.1;     d[9] = -10.1;    d[10] = -11.1;   d[11] = -12.1;
        d[12] = -13.1;   d[13] = -14.1;   d[14] = -15.1;   d[15] = -16.1;
        d[16] = -17.1;   d[17] = -18.1;   d[18] = -19.1;   d[19] = -20.1;
        s = MathX.sum(d);
        assertDouble("Sum ", -212, s);

    }

    @Test
    void testSumsq() {
        double[] d = new double[100];
        d[0] = 1.1;     d[1] = 2.1;     d[2] = 3.1;     d[3] = 4.1;
        d[4] = 5.1;     d[5] = 6.1;     d[6] = 7.1;     d[7] = 8.1;
        d[8] = 9.1;     d[9] = 10.1;    d[10] = 11.1;   d[11] = 12.1;
        d[12] = 13.1;   d[13] = 14.1;   d[14] = 15.1;   d[15] = 16.1;
        d[16] = 17.1;   d[17] = 18.1;   d[18] = 19.1;   d[19] = 20.1;

        double s = MathX.sumsq(d);
        assertDouble("Sumsq ", 2912.2, s);

        d = new double[1000];
        s = MathX.sumsq(d);
        assertDouble("Sumsq ", 0, s);

        d[0] = -1.1;     d[1] = 2.1;     d[2] = -3.1;     d[3] = 4.1;
        d[4] = -5.1;     d[5] = 6.1;     d[6] = -7.1;     d[7] = 8.1;
        d[8] = -9.1;     d[9] = 10.1;    d[10] = -11.1;   d[11] = 12.1;
        d[12] = -13.1;   d[13] = 14.1;   d[14] = -15.1;   d[15] = 16.1;
        d[16] = -17.1;   d[17] = 18.1;   d[18] = -19.1;   d[19] = 20.1;
        s = MathX.sumsq(d);
        assertDouble("Sumsq ", 2912.2, s);

        d[0] = -1.1;     d[1] = -2.1;     d[2] = -3.1;     d[3] = -4.1;
        d[4] = -5.1;     d[5] = -6.1;     d[6] = -7.1;     d[7] = -8.1;
        d[8] = -9.1;     d[9] = -10.1;    d[10] = -11.1;   d[11] = -12.1;
        d[12] = -13.1;   d[13] = -14.1;   d[14] = -15.1;   d[15] = -16.1;
        d[16] = -17.1;   d[17] = -18.1;   d[18] = -19.1;   d[19] = -20.1;
        s = MathX.sumsq(d);
        assertDouble("Sumsq ", 2912.2, s);
    }

    @Test
    void testFactorial() {
        int n;
        double s;

        n = 0;
        s = MathX.factorial(n);
        assertDouble("Factorial ", 1, s);

        n = 1;
        s = MathX.factorial(n);
        assertDouble("Factorial ", 1, s);

        n = 10;
        s = MathX.factorial(n);
        assertDouble("Factorial ", 3628800, s);

        n = 99;
        s = MathX.factorial(n);
        assertDouble("Factorial ", 9.33262154439E+155, s);

        n = -1;
        s = MathX.factorial(n);
        assertDouble("Factorial ", Double.NaN, s);

        n = Integer.MAX_VALUE;
        s = MathX.factorial(n);
        assertDouble("Factorial ", Double.POSITIVE_INFINITY, s);
    }

    @Test
    void testSumx2my2() {
        double[] xarr;
        double[] yarr;

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

    @Test
    void testSumx2py2() {
        double[] xarr;
        double[] yarr;

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

    @Test
    void testSumxmy2() {
        double[] xarr;
        double[] yarr;

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
        assertDouble(expectedResult, result);
    }

    @Test
    void testRound() {
        double d;
        int p;

        d = 0; p = 0;
        assertDouble("round ", 0, MathX.round(d, p));

        d = 10; p = 0;
        assertDouble("round ", 10, MathX.round(d, p));

        d = 123.23; p = 0;
        assertDouble("round ", 123, MathX.round(d, p));

        d = -123.23; p = 0;
        assertDouble("round ", -123, MathX.round(d, p));

        d = 123.12; p = 2;
        assertDouble("round ", 123.12, MathX.round(d, p));

        d = 88.123459; p = 5;
        assertDouble("round ", 88.12346, MathX.round(d, p));

        d = 0; p = 2;
        assertDouble("round ", 0, MathX.round(d, p));

        d = 0; p = -1;
        assertDouble("round ", 0, MathX.round(d, p));

        d = 0.01; p = -1;
        assertDouble("round ", 0, MathX.round(d, p));

        d = 123.12; p = -2;
        assertDouble("round ", 100, MathX.round(d, p));

        d = 88.123459; p = -3;
        assertDouble("round ", 0, MathX.round(d, p));

        d = 49.00000001; p = -1;
        assertDouble("round ", 50, MathX.round(d, p));

        d = 149.999999; p = -2;
        assertDouble("round ", 100, MathX.round(d, p));

        d = 150.0; p = -2;
        assertDouble("round ", 200, MathX.round(d, p));

        d = 2162.615d; p = 2;
        assertDouble("round ", 2162.62d, MathX.round(d, p));

        d = 0.049999999999999975d; p = 2;
        assertDouble("round ", 0.05d, MathX.round(d, p));

        d = 0.049999999999999975d; p = 1;
        assertDouble("round ", 0.1d, MathX.round(d, p));

        d = Double.NaN; p = 1;
        assertDouble("round ", Double.NaN, MathX.round(d, p));

        d = Double.POSITIVE_INFINITY; p = 1;
        assertDouble("round ", Double.NaN, MathX.round(d, p));

        d = Double.NEGATIVE_INFINITY; p = 1;
        assertDouble("round ", Double.NaN, MathX.round(d, p));

        d = Double.MAX_VALUE; p = 1;
        assertDouble("round ", Double.MAX_VALUE, MathX.round(d, p));

        d = Double.MIN_VALUE; p = 1;
        assertDouble("round ", 0.0d, MathX.round(d, p));
    }

    @Test
    void testRoundDown() {
        double d;
        int p;

        d = 0; p = 0;
        assertDouble("roundDown ", 0, MathX.roundDown(d, p));

        d = 10; p = 0;
        assertDouble("roundDown ", 10, MathX.roundDown(d, p));

        d = 123.99; p = 0;
        assertDouble("roundDown ", 123, MathX.roundDown(d, p));

        d = -123.99; p = 0;
        assertDouble("roundDown ", -123, MathX.roundDown(d, p));

        d = 123.99; p = 2;
        assertDouble("roundDown ", 123.99, MathX.roundDown(d, p));

        d = 88.123459; p = 5;
        assertDouble("roundDown ", 88.12345, MathX.roundDown(d, p));

        d = 0; p = 2;
        assertDouble("roundDown ", 0, MathX.roundDown(d, p));

        d = 0; p = -1;
        assertDouble("roundDown ", 0, MathX.roundDown(d, p));

        d = 0.01; p = -1;
        assertDouble("roundDown ", 0, MathX.roundDown(d, p));

        d = 199.12; p = -2;
        assertDouble("roundDown ", 100, MathX.roundDown(d, p));

        d = 88.123459; p = -3;
        assertDouble("roundDown ", 0, MathX.roundDown(d, p));

        d = 99.00000001; p = -1;
        assertDouble("roundDown ", 90, MathX.roundDown(d, p));

        d = 100.00001; p = -2;
        assertDouble("roundDown ", 100, MathX.roundDown(d, p));

        d = 150.0; p = -2;
        assertDouble("roundDown ", 100, MathX.roundDown(d, p));

        d = 0.0499999999999975d; p = 2;
        assertDouble("roundDown ", 0.04d, MathX.roundDown(d, p));

        d = 0.049999999999999975d; p = 1;
        assertDouble("roundDown ", 0.0d, MathX.roundDown(d, p));

        d = Double.NaN; p = 1;
        assertDouble("roundDown ", Double.NaN, MathX.roundDown(d, p));

        d = Double.POSITIVE_INFINITY; p = 1;
        assertDouble("roundDown ", Double.NaN, MathX.roundDown(d, p));

        d = Double.NEGATIVE_INFINITY; p = 1;
        assertDouble("roundDown ", Double.NaN, MathX.roundDown(d, p));

        d = Double.MAX_VALUE; p = 1;
        assertDouble("roundDown ", Double.MAX_VALUE, MathX.roundDown(d, p));

        d = Double.MIN_VALUE; p = 1;
        assertDouble("roundDown ", 0.0d, MathX.roundDown(d, p));

        d = 3987 * 0.2; p = 2;
        assertDouble("roundDown ", 797.40, MathX.round(d, p));
    }

    @Test
    void testRoundUp() {
        double d;
        int p;

        d = 0; p = 0;
        assertDouble("roundUp ", 0, MathX.roundUp(d, p));

        d = 10; p = 0;
        assertDouble("roundUp ", 10, MathX.roundUp(d, p));

        d = 123.23; p = 0;
        assertDouble("roundUp ", 124, MathX.roundUp(d, p));

        d = -123.23; p = 0;
        assertDouble("roundUp ", -124, MathX.roundUp(d, p));

        d = 123.12; p = 2;
        assertDouble("roundUp ", 123.12, MathX.roundUp(d, p));

        d = 88.123459; p = 5;
        assertDouble("roundUp ", 88.12346, MathX.roundUp(d, p));

        d = 0; p = 2;
        assertDouble("roundUp ", 0, MathX.roundUp(d, p));

        d = 0; p = -1;
        assertDouble("roundUp ", 0, MathX.roundUp(d, p));

        d = 0.01; p = -1;
        assertDouble("roundUp ", 10, MathX.roundUp(d, p));

        d = 123.12; p = -2;
        assertDouble("roundUp ", 200, MathX.roundUp(d, p));

        d = 88.123459; p = -3;
        assertDouble("roundUp ", 1000, MathX.roundUp(d, p));

        d = 49.00000001; p = -1;
        assertDouble("roundUp ", 50, MathX.roundUp(d, p));

        d = 149.999999; p = -2;
        assertDouble("roundUp ", 200, MathX.roundUp(d, p));

        d = 150.0; p = -2;
        assertDouble("roundUp ", 200, MathX.roundUp(d, p));

        d = 0.049999999999999975d; p = 2;
        assertDouble("roundUp ", 0.05d, MathX.roundUp(d, p));

        d = 0.049999999999999975d; p = 1;
        assertDouble("roundUp ", 0.1d, MathX.roundUp(d, p));

        d = Double.NaN; p = 1;
        assertDouble("roundUp ", Double.NaN, MathX.roundUp(d, p));

        d = Double.POSITIVE_INFINITY; p = 1;
        assertDouble("roundUp ", Double.NaN, MathX.roundUp(d, p));

        d = Double.NEGATIVE_INFINITY; p = 1;
        assertDouble("roundUp ", Double.NaN, MathX.roundUp(d, p));

        d = Double.MAX_VALUE; p = 1;
        assertDouble("roundUp ", Double.MAX_VALUE, MathX.roundUp(d, p));

        // Excel's min positive value is several orders of magnitude larger than Java's (Java 8: 4.9e-324, Excel 2016 on Windows 10: 2.2259157957E-308)
        d = Double.MIN_VALUE; p = 1;
        assertDouble("roundUp ", 0.0d, MathX.roundUp(d, p));
        d = 2.2259157957E-308; p = 1;
        assertDouble("roundUp ", 0.1d, MathX.roundUp(d, p));

        //github-43: https://github.com/apache/poi/pull/43
        // "ROUNDUP(3987*0.2, 2) currently fails by returning 797.41")
        d = 3987 * 0.2; p = 2;
        assertDouble("roundUp ", 797.40, MathX.roundUp(d, p));
    }

    @Test
    void testCeiling() {
        double d;
        double s;

        d = 0; s = 0;
        assertDouble("ceiling ", 0, MathX.ceiling(d, s));

        d = 1; s = 0;
        assertDouble("ceiling ", 0, MathX.ceiling(d, s));

        d = 0; s = 1;
        assertDouble("ceiling ", 0, MathX.ceiling(d, s));

        d = -1; s = 0;
        assertDouble("ceiling ", 0, MathX.ceiling(d, s));

        d = 0; s = -1;
        assertDouble("ceiling ", 0, MathX.ceiling(d, s));

        d = 10; s = 1.11;
        assertDouble("ceiling ", 11.1, MathX.ceiling(d, s));

        d = 11.12333; s = 0.03499;
        assertDouble("ceiling ", 11.12682, MathX.ceiling(d, s));

        d = -11.12333; s = 0.03499;
        assertDouble("ceiling ", Double.NaN, MathX.ceiling(d, s));

        d = 11.12333; s = -0.03499;
        assertDouble("ceiling ", Double.NaN, MathX.ceiling(d, s));

        d = -11.12333; s = -0.03499;
        assertDouble("ceiling ", -11.12682, MathX.ceiling(d, s));

        d = 100; s = 0.001;
        assertDouble("ceiling ", 100, MathX.ceiling(d, s));

        d = -0.001; s = -9.99;
        assertDouble("ceiling ", -9.99, MathX.ceiling(d, s));

        d = 4.42; s = 0.05;
        assertDouble("ceiling ", 4.45, MathX.ceiling(d, s));

        d = 0.05; s = 4.42;
        assertDouble("ceiling ", 4.42, MathX.ceiling(d, s));

        d = 0.6666; s = 3.33;
        assertDouble("ceiling ", 3.33, MathX.ceiling(d, s));

        d = 2d/3; s = 3.33;
        assertDouble("ceiling ", 3.33, MathX.ceiling(d, s));

        // samples from http://www.excelfunctions.net/Excel-Ceiling-Function.html
        // and https://support.office.com/en-us/article/CEILING-function-0a5cd7c8-0720-4f0a-bd2c-c943e510899f
        d = 22.25; s = 0.1;
        assertDouble("ceiling ", 22.3, MathX.ceiling(d, s));
        d = 22.25; s = 0.5;
        assertDouble("ceiling ", 22.5, MathX.ceiling(d, s));
        d = 22.25; s = 1;
        assertDouble("ceiling ", 23, MathX.ceiling(d, s));
        d = 22.25; s = 10;
        assertDouble("ceiling ", 30, MathX.ceiling(d, s));
        d = 22.25; s = 20;
        assertDouble("ceiling ", 40, MathX.ceiling(d, s));
        d = -22.25; s = -0.1;
        assertDouble("ceiling ", -22.3, MathX.ceiling(d, s));
        d = -22.25; s = -1;
        assertDouble("ceiling ", -23, MathX.ceiling(d, s));
        d = -22.25; s = -5;
        assertDouble("ceiling ", -25, MathX.ceiling(d, s));

        d = 22.25; s = 1;
        assertDouble("ceiling ", 23, MathX.ceiling(d, s));
        d = 22.25; s = -1;
        assertDouble("ceiling ", Double.NaN, MathX.ceiling(d, s));
        d = -22.25; s = 1;
        assertDouble("ceiling ", -22, MathX.ceiling(d, s)); // returns an error in Excel 2007 & earlier
        d = -22.25; s = -1;
        assertDouble("ceiling ", -23, MathX.ceiling(d, s));

        // test cases for newer versions of Excel where d can be negative for
        d = -11.12333; s = 0.03499;
        assertDouble("ceiling ", -11.09183, MathX.ceiling(d, s));
    }

    @Test
    void testFloor() {
        double d;
        double s;

        d = 0; s = 0;
        assertDouble("floor ", 0, MathX.floor(d, s));

        d = 1; s = 0;
        assertDouble("floor ", Double.NaN, MathX.floor(d, s));

        d = 0; s = 1;
        assertDouble("floor ", 0, MathX.floor(d, s));

        d = -1; s = 0;
        assertDouble("floor ", Double.NaN, MathX.floor(d, s));

        d = 0; s = -1;
        assertDouble("floor ", 0, MathX.floor(d, s));

        d = 10; s = 1.11;
        assertDouble("floor ", 9.99, MathX.floor(d, s));

        d = 11.12333; s = 0.03499;
        assertDouble("floor ", 11.09183, MathX.floor(d, s));

        d = -11.12333; s = 0.03499;
        assertDouble("floor ", Double.NaN, MathX.floor(d, s));

        d = 11.12333; s = -0.03499;
        assertDouble("floor ", Double.NaN, MathX.floor(d, s));

        d = -11.12333; s = -0.03499;
        assertDouble("floor ", -11.09183, MathX.floor(d, s));

        d = 100; s = 0.001;
        assertDouble("floor ", 100, MathX.floor(d, s));

        d = -0.001; s = -9.99;
        assertDouble("floor ", 0, MathX.floor(d, s));

        d = 4.42; s = 0.05;
        assertDouble("floor ", 4.4, MathX.floor(d, s));

        d = 0.05; s = 4.42;
        assertDouble("floor ", 0, MathX.floor(d, s));

        d = 0.6666; s = 3.33;
        assertDouble("floor ", 0, MathX.floor(d, s));

        d = 2d/3; s = 3.33;
        assertDouble("floor ", 0, MathX.floor(d, s));

        // samples from http://www.excelfunctions.net/Excel-Ceiling-Function.html
        // and https://support.office.com/en-us/article/CEILING-function-0a5cd7c8-0720-4f0a-bd2c-c943e510899f
        d = 3.7; s = 2;
        assertDouble("floor ", 2, MathX.floor(d, s));
        d = -2.5; s = -2;
        assertDouble("floor ", -2, MathX.floor(d, s));
        d = 2.5; s = -2;
        assertDouble("floor ", Double.NaN, MathX.floor(d, s));
        d = 1.58; s = 0.1;
        assertDouble("floor ", 1.5, MathX.floor(d, s));
        d = 0.234; s = 0.01;
        assertDouble("floor ", 0.23, MathX.floor(d, s));

        // see bug 62839
        d = -123;
        s = 10;
        assertDouble("floor ", -130, MathX.floor(d, s));
    }
}
