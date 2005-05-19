/*
 * Created on May 11, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.GenericFormulaTestCase;

import junit.framework.TestSuite;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public class TestEverything extends TestSuite {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite("Tests for individual function classes");
        String s;
        for(int i=80; i<1481;i=i+4) {
        	s = "D"+Integer.toString(i).trim();
        	suite.addTest(new GenericFormulaTestCase(s));
        }
        return suite;
    }
}
