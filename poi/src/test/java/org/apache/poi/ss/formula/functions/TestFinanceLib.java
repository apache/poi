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


import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.Utils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.ss.util.Utils.addRow;

class TestFinanceLib extends BaseTestNumeric {

    @Test
    void testFv() {
        double f, r, y, p, x;
        int n;
        boolean t;

        r = 0; n = 1; y = 1; p = 1; t = true;
        f = FinanceLib.fv(r, n, y, p, t);
        x = -2;
        assertDouble("fv ", x, f);

        r = 0.12/12; n = 12; y = -1000; p = 0; t = false;
        f = FinanceLib.fv(r, n, y, p, t);
        x = 12682.50301319;
        assertDouble("fv ", x, f);

        r = 0.06/12; n = 10; y = -200; p = -500; t = true;
        f = FinanceLib.fv(r, n, y, p, t);
        x = 2581.4033740;
        assertDouble("fv ", x, f);

        r = 0; n = 3; y = 2; p = 7; t = true;
        f = FinanceLib.fv(r, n, y, p, t);
        x = -13;
        assertDouble("fv ", x, f);

        r = 1; n = 10; y = 100; p = 10000; t = false;
        f = FinanceLib.fv(r, n, y, p, t);
        x = -10342300;
        assertDouble("fv ", x, f);

        r = 1; n = 10; y = 100; p = 10000; t = true;
        f = FinanceLib.fv(r, n, y, p, t);
        x = -10444600;
        assertDouble("fv ", x, f);

        r = 2; n = 12; y = 120; p = 12000; t = false;
        f = FinanceLib.fv(r, n, y, p, t);
        x = -6409178400d;
        assertDouble("fv ", x, f);

        r = 2; n = 12; y = 120; p = 12000; t = true;
        f = FinanceLib.fv(r, n, y, p, t);
        x = -6472951200d;
        assertDouble("fv ", x, f);

        // cross tests with pv
        r = 2.95; n = 13; y = 13000; p = -4406.78544294496; t = false;
        f = FinanceLib.fv(r, n, y, p, t);
        x = 333891.230010986; // as returned by excel
        assertDouble("fv ", x, f);

        r = 2.95; n = 13; y = 13000; p = -17406.7852148156; t = true;
        f = FinanceLib.fv(r, n, y, p, t);
        x = 333891.230102539; // as returned by excel
        assertDouble("fv ", x, f);

    }

    @Test
    void testNpv() {
        double r;
        double[] v;
        double npv;
        double x;

        r = 1; v = new double[]{100, 200, 300, 400};
        npv = FinanceLib.npv(r, v);
        x = 162.5;
        assertDouble("npv ", x, npv);

        r = 2.5; v = new double[]{1000, 666.66666, 333.33, 12.2768416};
        npv = FinanceLib.npv(r, v);
        x = 347.99232604144827;
        assertDouble("npv ", x, npv);

        r = 12.33333; v = new double[]{1000, 0, -900, -7777.5765};
        npv = FinanceLib.npv(r, v);
        x = 74.3742433377061;
        assertDouble("npv ", x, npv);

        r = 0.05; v = new double[]{200000, 300000.55, 400000, 1000000, 6000000, 7000000, -300000};
        npv = FinanceLib.npv(r, v);
        x = 11342283.4233124;
        assertDouble("npv ", x, npv);

        r = 0.08; v = new double[]{-1000, 500, 300, 800};
        npv = FinanceLib.npv(r, v);
        x = 328.9170387681793;
        assertDouble("npv ", x, npv);

        //https://support.microsoft.com/en-us/office/npv-function-8672cb67-2576-4d07-b67b-ac28acf2a568
        r = 0.1; v = new double[]{-10000, 3000, 4200, 6800};
        npv = FinanceLib.npv(r, v);
        x = 1188.4434123352207;
        assertDouble("npv ", x, npv);

        r = 0.08; v = new double[]{-40000, 8000, 9200, 10000, 12000, 14500};
        npv = FinanceLib.npv(r, v);
        x = 1779.6866249373716;
        assertDouble("npv ", x, npv);
    }

    @Test
    void testMicrosoftNpvExample2() throws IOException {
        //https://support.microsoft.com/en-us/office/npv-function-8672cb67-2576-4d07-b67b-ac28acf2a568
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            addRow(sheet, 0, "Data");
            addRow(sheet, 1, 0.08);
            addRow(sheet, 2, -40000);
            addRow(sheet, 3, 8000);
            addRow(sheet, 4, 9200);
            addRow(sheet, 5, 10000);
            addRow(sheet, 6, 12000);
            addRow(sheet, 7, 14500);
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            HSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            Utils.assertDouble(fe, cell, "NPV(A2, A4:A8)+A3", 1922.061554932363, 0.00000000001);
            Utils.assertDouble(fe, cell, "NPV(A2, A4:A8, -9000)+A3", -3749.4650870155747, 0.00000000001);
        }
    }

    @Test
    void testPmt() {
        double f, r, y, p, x;
        int n;
        boolean t;

        r = 0; n = 3; p = 2; f = 7; t = true;
        y = FinanceLib.pmt(r, n, p, f, t);
        x = -3;
        assertDouble("pmt ", x, y);

        // cross check with pv
        r = 1; n = 10; p = -109.66796875; f = 10000; t = false;
        y = FinanceLib.pmt(r, n, p, f, t);
        x = 100;
        assertDouble("pmt ", x, y);

        r = 1; n = 10; p = -209.5703125; f = 10000; t = true;
        y = FinanceLib.pmt(r, n, p, f, t);
        x = 100;
        assertDouble("pmt ", x, y);

        // cross check with fv
        r = 2; n = 12; f = -6409178400d; p = 12000; t = false;
        y = FinanceLib.pmt(r, n, p, f, t);
        x = 120;
        assertDouble("pmt ", x, y);

        r = 2; n = 12; f = -6472951200d; p = 12000; t = true;
        y = FinanceLib.pmt(r, n, p, f, t);
        x = 120;
        assertDouble("pmt ", x, y);
    }

    @Test
    void testPv() {
        double f, r, y, p, x;
        int n;
        boolean t;

        r = 0; n = 3; y = 2; f = 7; t = true;
        f = FinanceLib.pv(r, n, y, f, t);
        x = -13;
        assertDouble("pv ", x, f);

        r = 1; n = 10; y = 100; f = 10000; t = false;
        p = FinanceLib.pv(r, n, y, f, t);
        x = -109.66796875;
        assertDouble("pv ", x, p);

        r = 1; n = 10; y = 100; f = 10000; t = true;
        p = FinanceLib.pv(r, n, y, f, t);
        x = -209.5703125;
        assertDouble("pv ", x, p);

        r = 2.95; n = 13; y = 13000; f = 333891.23; t = false;
        p = FinanceLib.pv(r, n, y, f, t);
        x = -4406.78544294496;
        assertDouble("pv ", x, p);

        r = 2.95; n = 13; y = 13000; f = 333891.23; t = true;
        p = FinanceLib.pv(r, n, y, f, t);
        x = -17406.7852148156;
        assertDouble("pv ", x, p);

        // cross tests with fv
        r = 2; n = 12; y = 120; f = -6409178400d; t = false;
        p = FinanceLib.pv(r, n, y, f, t);
        x = 12000;
        assertDouble("pv ", x, p);

        r = 2; n = 12; y = 120; f = -6472951200d; t = true;
        p = FinanceLib.pv(r, n, y, f, t);
        x = 12000;
        assertDouble("pv ", x, p);

    }

    @Test
    void testNper() {
        double f, r, y, p, x, n;
        boolean t;

        r = 0; y = 7; p = 2; f = 3; t = false;
        n = FinanceLib.nper(r, y, p, f, t);
        x = -0.71428571429; // can you believe it? excel returns nper as a fraction!??
        assertDouble("nper ", x, n);

        // cross check with pv
        r = 1; y = 100; p = -109.66796875; f = 10000; t = false;
        n = FinanceLib.nper(r, y, p, f, t);
        x = 10;
        assertDouble("nper ", x, n);

        r = 1; y = 100; p = -209.5703125; f = 10000; t = true;
        n = FinanceLib.nper(r, y, p, f, t);
        x = 10;
        assertDouble("nper ", x, n);

        // cross check with fv
        r = 2; y = 120; f = -6409178400d; p = 12000; t = false;
        n = FinanceLib.nper(r, y, p, f, t);
        x = 12;
        assertDouble("nper ", x, n);

        r = 2; y = 120; f = -6472951200d; p = 12000; t = true;
        n = FinanceLib.nper(r, y, p, f, t);
        x = 12;
        assertDouble("nper ", x, n);
    }
}
