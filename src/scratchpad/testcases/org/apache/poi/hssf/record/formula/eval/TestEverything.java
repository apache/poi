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
 * Created on May 11, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

import junit.framework.TestSuite;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public class TestEverything extends TestSuite {

    public static TestSuite suite() throws Exception {
        TestSuite suite = new TestSuite("Tests for OperationEval concrete implementation classes.");
        suite.addTest(new GenericFormulaTestCase("D23")); // Add
        suite.addTest(new GenericFormulaTestCase("D27")); // ConcatEval
        suite.addTest(new GenericFormulaTestCase("D31")); // DivideEval
        suite.addTest(new GenericFormulaTestCase("D35")); // EqualEval
        suite.addTest(new GenericFormulaTestCase("D39")); // GreaterEqualEval
        suite.addTest(new GenericFormulaTestCase("D43")); // GreaterThanEval
        suite.addTest(new GenericFormulaTestCase("D47")); // LessEqualEval
        suite.addTest(new GenericFormulaTestCase("D51")); // LessThanEval
        suite.addTest(new GenericFormulaTestCase("D55")); // MultiplyEval
        suite.addTest(new GenericFormulaTestCase("D59")); // NotEqualEval
        suite.addTest(new GenericFormulaTestCase("D63")); // PowerEval
        suite.addTest(new GenericFormulaTestCase("D67")); // SubtractEval
        suite.addTest(new GenericFormulaTestCase("D71")); // UnaryMinusEval
        suite.addTest(new GenericFormulaTestCase("D75")); // UnaryPlusEval
        
        suite.addTest(new GenericFormulaTestCase("D249")); // Concatenate
        suite.addTest(new GenericFormulaTestCase("D741")); // Int
        suite.addTest(new GenericFormulaTestCase("D1393")); // Trim
        suite.addTest(new GenericFormulaTestCase("D1421")); // Upper
        
        // Add newly implemented formula functions here 
        
        return suite;
    }
}
