/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002, 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.poi.hssf.model;

import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.*;
import org.apache.poi.hssf.util.SheetReferences;

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
        FormulaParser fp = new FormulaParser("2+2;",null);
        fp.parse();
        Ptg[] ptgs = fp.getRPNPtg();
        assertTrue("three tokens expected, got "+ptgs.length,ptgs.length == 3);
    }
    public void testFormulaWithSpace1() {
        FormulaParser fp = new FormulaParser(" 2 + 2 ;",null);
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
        fp = new FormulaParser("2+ sum( 3 , 4) ;",null);
        fp.parse();
        ptgs = fp.getRPNPtg();
        assertTrue("five tokens expected, got "+ptgs.length,ptgs.length == 5);
    }
    
     public void testFormulaWithSpaceNRef() {
        Ptg[] ptgs;
        FormulaParser fp;
        fp = new FormulaParser("sum( A2:A3 );",null);
        fp.parse();
        ptgs = fp.getRPNPtg();
        assertTrue("two tokens expected, got "+ptgs.length,ptgs.length == 2);
    }
    
    public void testFormulaWithString() {
        Ptg[] ptgs;
        FormulaParser fp;
        fp = new FormulaParser("\"hello\" & \"world\" ;",null);
        fp.parse();
        ptgs = fp.getRPNPtg();
        assertTrue("three token expected, got " + ptgs.length, ptgs.length == 3);
    }

    public void testTRUE() throws Exception {
        FormulaParser fp = new FormulaParser("TRUE", null);
        fp.parse();
        Ptg[] asts = fp.getRPNPtg();
        assertEquals(1, asts.length);
        BoolPtg flag  = (BoolPtg) asts[0];
        assertEquals(true, flag.getValue());
    }

    public void testYN() throws Exception {
        final String yn = "IF(TRUE,\"Y\",\"N\")";
        FormulaParser fp = new FormulaParser(yn, null);
        fp.parse();
        Ptg[] asts = fp.getRPNPtg();
        assertEquals(7, asts.length);

        BoolPtg flag  = (BoolPtg) asts[0];
		AttrPtg funif = (AttrPtg) asts[1];
        StringPtg y = (StringPtg) asts[2];
		AttrPtg goto1 = (AttrPtg) asts[3];
        StringPtg n = (StringPtg) asts[4];


        assertEquals(true, flag.getValue());
        assertEquals("Y", y.getValue());
        assertEquals("N", n.getValue());
        assertEquals("IF", funif.toFormulaString(new SheetReferences()));
        assertTrue("Goto ptg exists", goto1.isGoto());
    }

	public void testSimpleIf() throws Exception {
		final String simpleif = "IF(1=1,0,1)";
		FormulaParser fp = new FormulaParser(simpleif, null);
		fp.parse();
		Ptg[] asts = fp.getRPNPtg();
		assertEquals(9, asts.length);
		
		IntPtg op1 = (IntPtg) asts[0];
		IntPtg op2 = (IntPtg) asts[1];
		EqualPtg eq = (EqualPtg) asts[2];		
		AttrPtg ifPtg = (AttrPtg) asts[3];
		IntPtg res1 = (IntPtg) asts[4];
				
		AttrPtg ptgGoto= (AttrPtg) asts[5];		
		assertEquals("Goto 1 Length", (short)10, ptgGoto.getData());
		
		IntPtg res2 = (IntPtg) asts[6];		
		AttrPtg ptgGoto2 = (AttrPtg) asts[7];		
		assertEquals("Goto 2 Length", (short)3, ptgGoto2.getData());
		
		assertEquals("If FALSE offset", (short)7, ifPtg.getData());
		
		FuncVarPtg funcPtg = (FuncVarPtg)asts[8];
		
		
	}
    
	/**
	 * Make sure the ptgs are generated properly with two functions embedded
	 *
	 */
	public void testNestedFunctionIf() {
		String function = "IF(A1=B1,AVERAGE(A1:B1),AVERAGE(A2:B2))";

		FormulaParser fp = new FormulaParser(function, null);
		fp.parse();
		Ptg[] asts = fp.getRPNPtg();
		assertEquals("11 Ptgs expected", 11, asts.length);

		assertTrue("IF Attr set correctly", (asts[3] instanceof AttrPtg));
		AttrPtg ifFunc = (AttrPtg)asts[3];
		assertTrue("It is not an if", ifFunc.isOptimizedIf());
		
		assertTrue("Average Function set correctly", (asts[5] instanceof FuncVarPtg));
		
				    	
	}
	
	public void testIfSingleCondition(){
		String function = "IF(1=1,10)";

		FormulaParser fp = new FormulaParser(function, null);
		fp.parse();
		Ptg[] asts = fp.getRPNPtg();
		assertEquals("7 Ptgs expected", 7, asts.length);

		assertTrue("IF Attr set correctly", (asts[3] instanceof AttrPtg));
		AttrPtg ifFunc = (AttrPtg)asts[3];
		assertTrue("It is not an if", ifFunc.isOptimizedIf());
		
		assertTrue("Single Value is not an IntPtg", (asts[4] instanceof IntPtg));
		IntPtg intPtg = (IntPtg)asts[4];
		assertEquals("Result", (short)10, intPtg.getValue());
		
		assertTrue("Ptg is not a Variable Function", (asts[6] instanceof FuncVarPtg));
		FuncVarPtg funcPtg = (FuncVarPtg)asts[6];
		assertEquals("Arguments", 2, funcPtg.getNumberOfOperands());
		
		
	}

	public void testSumIf() {
		String function ="SUMIF(A1:A5,\">4000\",B1:B5)";
		FormulaParser fp = new FormulaParser(function, null);
		fp.parse();
		Ptg[] asts = fp.getRPNPtg();
		assertEquals("4 Ptgs expected", 4, asts.length);
		
	}
	 
	/**
	 * Bug Reported by xt-jens.riis@nokia.com (Jens Riis)
	 * Refers to Bug <a href="http://nagoya.apache.org/bugzilla/show_bug.cgi?id=17582">#17582</a>
	 *
	 */
	public void testNonAlphaFormula(){
		String currencyCell = "F3";
		String function="\"TOTAL[\"&"+currencyCell+"&\"]\"";

		FormulaParser fp = new FormulaParser(function, null);
		fp.parse();
		Ptg[] asts = fp.getRPNPtg();
		assertEquals("5 ptgs expected", 5, asts.length);
		assertTrue ("Ptg[0] is a string", (asts[0] instanceof StringPtg));
		StringPtg firstString = (StringPtg)asts[0];		
		
		assertEquals("TOTAL[", firstString.getValue());
		//the PTG order isn't 100% correct but it still works - dmui
		
					
	}	 
	    
     public static void main(String [] args) {
        System.out.println("Testing org.apache.poi.hssf.record.formula.FormulaParser");
        junit.textui.TestRunner.run(TestFormulaParser.class);
    }
}
