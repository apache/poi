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
        suite.addTest(new GenericFormulaTestCase("D23"));
        suite.addTest(new GenericFormulaTestCase("D27"));
        suite.addTest(new GenericFormulaTestCase("D31"));
        suite.addTest(new GenericFormulaTestCase("D35"));
        suite.addTest(new GenericFormulaTestCase("D39"));
        suite.addTest(new GenericFormulaTestCase("D43"));
        suite.addTest(new GenericFormulaTestCase("D47"));
        suite.addTest(new GenericFormulaTestCase("D51"));
        suite.addTest(new GenericFormulaTestCase("D55"));
        suite.addTest(new GenericFormulaTestCase("D59"));
        suite.addTest(new GenericFormulaTestCase("D63"));
        suite.addTest(new GenericFormulaTestCase("D67"));
        suite.addTest(new GenericFormulaTestCase("D71"));
        suite.addTest(new GenericFormulaTestCase("D75"));
        return suite;
    }
}
