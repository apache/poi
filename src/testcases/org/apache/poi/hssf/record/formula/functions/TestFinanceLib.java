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


/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public class TestFinanceLib extends AbstractNumericTestCase {

    public void testFv() {
        double f, r, y, p, x;
        int n;
        boolean t = false;
        
        r = 0; n = 3; y = 2; p = 7; t = true;
        f = FinanceLib.fv(r, n, y, p, t);
        x = -13;
        assertEquals("fv ", x, f);
        
        r = 1; n = 10; y = 100; p = 10000; t = false;
        f = FinanceLib.fv(r, n, y, p, t);
        x = -10342300;
        assertEquals("fv ", x, f);
        
        r = 1; n = 10; y = 100; p = 10000; t = true;
        f = FinanceLib.fv(r, n, y, p, t);
        x = -10444600;
        assertEquals("fv ", x, f);
        
        r = 2; n = 12; y = 120; p = 12000; t = false;
        f = FinanceLib.fv(r, n, y, p, t);
        x = -6409178400d;
        assertEquals("fv ", x, f);
        
        r = 2; n = 12; y = 120; p = 12000; t = true;
        f = FinanceLib.fv(r, n, y, p, t);
        x = -6472951200d;
        assertEquals("fv ", x, f);
        
        // cross tests with pv
        r = 2.95; n = 13; y = 13000; p = -4406.78544294496; t = false;
        f = FinanceLib.fv(r, n, y, p, t);
        x = 333891.230010986; // as returned by excel
        assertEquals("fv ", x, f);
        
        r = 2.95; n = 13; y = 13000; p = -17406.7852148156; t = true;
        f = FinanceLib.fv(r, n, y, p, t);
        x = 333891.230102539; // as returned by excel
        assertEquals("fv ", x, f);
        
    }
    public void testNpv() {
        double r, v[], npv, x;
        
        r = 1; v = new double[]{100, 200, 300, 400};
        npv = FinanceLib.npv(r, v);
        x = 162.5;
        assertEquals("npv ", x, npv);
        
        r = 2.5; v = new double[]{1000, 666.66666, 333.33, 12.2768416};
        npv = FinanceLib.npv(r, v);
        x = 347.99232604144827;
        assertEquals("npv ", x, npv);
        
        r = 12.33333; v = new double[]{1000, 0, -900, -7777.5765};
        npv = FinanceLib.npv(r, v);
        x = 74.3742433377061;
        assertEquals("npv ", x, npv);
        
        r = 0.05; v = new double[]{200000, 300000.55, 400000, 1000000, 6000000, 7000000, -300000};
        npv = FinanceLib.npv(r, v);
        x = 11342283.4233124;
        assertEquals("npv ", x, npv);
    }
    public void testPmt() {
        double f, r, y, p, x;
        int n;
        boolean t = false;

        r = 0; n = 3; p = 2; f = 7; t = true;
        y = FinanceLib.pmt(r, n, p, f, t);
        x = -3;
        assertEquals("pmt ", x, y);    
        
        // cross check with pv
        r = 1; n = 10; p = -109.66796875; f = 10000; t = false;
        y = FinanceLib.pmt(r, n, p, f, t);
        x = 100;
        assertEquals("pmt ", x, y);    
        
        r = 1; n = 10; p = -209.5703125; f = 10000; t = true;
        y = FinanceLib.pmt(r, n, p, f, t);
        x = 100;
        assertEquals("pmt ", x, y);
        
        // cross check with fv
        r = 2; n = 12; f = -6409178400d; p = 12000; t = false;
        y = FinanceLib.pmt(r, n, p, f, t);
        x = 120;
        assertEquals("pmt ", x, y);    
        
        r = 2; n = 12; f = -6472951200d; p = 12000; t = true;
        y = FinanceLib.pmt(r, n, p, f, t);
        x = 120;
        assertEquals("pmt ", x, y);
    }
    
    public void testPv() {
        double f, r, y, p, x;
        int n;
        boolean t = false;

        r = 0; n = 3; y = 2; f = 7; t = true;
        f = FinanceLib.pv(r, n, y, f, t);
        x = -13;
        assertEquals("pv ", x, f);

        r = 1; n = 10; y = 100; f = 10000; t = false;
        p = FinanceLib.pv(r, n, y, f, t);
        x = -109.66796875;
        assertEquals("pv ", x, p);    
        
        r = 1; n = 10; y = 100; f = 10000; t = true;
        p = FinanceLib.pv(r, n, y, f, t);
        x = -209.5703125;
        assertEquals("pv ", x, p);    
        
        r = 2.95; n = 13; y = 13000; f = 333891.23; t = false;
        p = FinanceLib.pv(r, n, y, f, t);
        x = -4406.78544294496;
        assertEquals("pv ", x, p);
        
        r = 2.95; n = 13; y = 13000; f = 333891.23; t = true;
        p = FinanceLib.pv(r, n, y, f, t);
        x = -17406.7852148156;
        assertEquals("pv ", x, p);
        
        // cross tests with fv
        r = 2; n = 12; y = 120; f = -6409178400d; t = false;
        p = FinanceLib.pv(r, n, y, f, t);
        x = 12000;
        assertEquals("pv ", x, p);
        
        r = 2; n = 12; y = 120; f = -6472951200d; t = true;
        p = FinanceLib.pv(r, n, y, f, t);
        x = 12000; 
        assertEquals("pv ", x, p);
        
    }
    
    public void testNper() {
        double f, r, y, p, x, n;
        boolean t = false;
        
        r = 0; y = 7; p = 2; f = 3; t = false;
        n = FinanceLib.nper(r, y, p, f, t);
        x = -0.71428571429; // can you believe it? excel returns nper as a fraction!??
        assertEquals("nper ", x, n);    
        
        // cross check with pv
        r = 1; y = 100; p = -109.66796875; f = 10000; t = false;
        n = FinanceLib.nper(r, y, p, f, t);
        x = 10;
        assertEquals("nper ", x, n);    
        
        r = 1; y = 100; p = -209.5703125; f = 10000; t = true;
        n = FinanceLib.nper(r, y, p, f, t);
        x = 10;
        assertEquals("nper ", x, n);
        
        // cross check with fv
        r = 2; y = 120; f = -6409178400d; p = 12000; t = false;
        n = FinanceLib.nper(r, y, p, f, t);
        x = 12;
        assertEquals("nper ", x, n);    
        
        r = 2; y = 120; f = -6472951200d; p = 12000; t = true;
        n = FinanceLib.nper(r, y, p, f, t);
        x = 12; 
        assertEquals("nper ", x, n);
    }
}
