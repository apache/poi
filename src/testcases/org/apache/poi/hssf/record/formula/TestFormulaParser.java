package org.apache.poi.hssf.record.formula;

import junit.framework.TestCase;

/**
 * Test the low level formula parser functionality. High level tests are to 
 * be done via usermodel/HSSFCell.setFormulaValue() . 
 */
public class TestFormulaParser extends TestCase {

    public TestFormulaParser(String name) {
        super(name);
    }
    public void setUp(){
        
    }
    
    public void tearDown() {
        
    }
    
    public void testSimpleFormula() {
        FormulaParser fp = new FormulaParser("2+2;");
        fp.parse();
        Ptg[] ptgs = fp.getRPNPtg();
        assertTrue("three tokens expected, got "+ptgs.length,ptgs.length == 3);
    }
    public void testFormulaWithSpace1() {
        FormulaParser fp = new FormulaParser(" 2 + 2 ;");
        fp.parse();
        Ptg[] ptgs = fp.getRPNPtg();
        assertTrue("three tokens expected, got "+ptgs.length,ptgs.length == 3);
        assertTrue("",(ptgs[0] instanceof IntPtg));
        assertTrue("",(ptgs[1] instanceof IntPtg));
        assertTrue("",(ptgs[2] instanceof AddPtg));
        
    }
    
    public void testFormulaWithSpace2() {
        Ptg[] ptgs;
        FormulaParser fp;
        fp = new FormulaParser("2+ sum( 3 , 4) ;");
        fp.parse();
        ptgs = fp.getRPNPtg();
        assertTrue("five tokens expected, got "+ptgs.length,ptgs.length == 5);
    }
    
     public void testFormulaWithSpaceNRef() {
        Ptg[] ptgs;
        FormulaParser fp;
        fp = new FormulaParser("sum( A2:A3 );");
        fp.parse();
        ptgs = fp.getRPNPtg();
        assertTrue("two tokens expected, got "+ptgs.length,ptgs.length == 2);
    }
    
     public static void main(String [] args) {
        System.out.println("Testing org.apache.poi.hssf.record.formula.FormulaParser");
        junit.textui.TestRunner.run(TestFormulaParser.class);
    }
}



