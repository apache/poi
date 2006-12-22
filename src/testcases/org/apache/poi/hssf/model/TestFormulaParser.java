
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        
package org.apache.poi.hssf.model;

import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.AbstractFunctionPtg;
import org.apache.poi.hssf.record.formula.AddPtg;
import org.apache.poi.hssf.record.formula.AttrPtg;
import org.apache.poi.hssf.record.formula.BoolPtg;
import org.apache.poi.hssf.record.formula.DividePtg;
import org.apache.poi.hssf.record.formula.EqualPtg;
import org.apache.poi.hssf.record.formula.FuncVarPtg;
import org.apache.poi.hssf.record.formula.IntPtg;
import org.apache.poi.hssf.record.formula.LessEqualPtg;
import org.apache.poi.hssf.record.formula.LessThanPtg;
import org.apache.poi.hssf.record.formula.NamePtg;
import org.apache.poi.hssf.record.formula.NotEqualPtg;
import org.apache.poi.hssf.record.formula.NumberPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.ReferencePtg;
import org.apache.poi.hssf.record.formula.StringPtg;
import org.apache.poi.hssf.record.formula.UnaryMinusPtg;
import org.apache.poi.hssf.record.formula.UnaryPlusPtg;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

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
        assertEquals("IF", funif.toFormulaString((Workbook) null));
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
	 * Refers to Bug <a href="http://issues.apache.org/bugzilla/show_bug.cgi?id=17582">#17582</a>
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
        
	public void testSimpleLogical() {
		FormulaParser fp=new FormulaParser("IF(A1<A2,B1,B2)",null);
		fp.parse();
      Ptg[] ptgs = fp.getRPNPtg();
      assertTrue("Ptg array should not be null", ptgs !=null);
      assertEquals("Ptg array length", 9, ptgs.length);
      assertEquals("3rd Ptg is less than",LessThanPtg.class,ptgs[2].getClass());
            
           
	}
	 
	public void testParenIf() {
		FormulaParser fp=new FormulaParser("IF((A1+A2)<=3,\"yes\",\"no\")",null);
		fp.parse();
		Ptg[] ptgs = fp.getRPNPtg();
		assertTrue("Ptg array should not be null", ptgs !=null);
		assertEquals("Ptg array length", 12, ptgs.length);
		assertEquals("6th Ptg is less than equal",LessEqualPtg.class,ptgs[5].getClass());
		assertEquals("11th Ptg is not a goto (Attr) ptg",AttrPtg.class,ptgs[10].getClass());
	}
	
	public void testEmbeddedIf() {
		FormulaParser fp=new FormulaParser("IF(3>=1,\"*\",IF(4<>1,\"first\",\"second\"))",null);
		fp.parse();
		Ptg[] ptgs = fp.getRPNPtg();
		assertTrue("Ptg array should not be null", ptgs !=null);
		assertEquals("Ptg array length", 17, ptgs.length);
		
		assertEquals("6th Ptg is not a goto (Attr) ptg",AttrPtg.class,ptgs[5].getClass());
		assertEquals("9th Ptg is not a not equal ptg",NotEqualPtg.class,ptgs[8].getClass());
		assertEquals("15th Ptg is not the inner IF variable function ptg",FuncVarPtg.class,ptgs[14].getClass());
		
	}
	    
    public void testMacroFunction() {
        Workbook w = new Workbook();
        FormulaParser fp = new FormulaParser("FOO()", w);
        fp.parse();
        Ptg[] ptg = fp.getRPNPtg();

        AbstractFunctionPtg tfunc = (AbstractFunctionPtg) ptg[0];
        assertEquals("externalflag", tfunc.getName());

        NamePtg tname = (NamePtg) ptg[1];
        assertEquals("FOO", tname.toFormulaString(w));
    }

    public void testEmbeddedSlash() {
        FormulaParser fp = new FormulaParser("HYPERLINK(\"http://www.jakarta.org\",\"Jakarta\");",null);
        fp.parse();
        Ptg[] ptg = fp.getRPNPtg();
        assertTrue("first ptg is string",ptg[0] instanceof StringPtg);
        assertTrue("second ptg is string",ptg[1] instanceof StringPtg);
        
    }
    
    public void testConcatenate(){
         FormulaParser fp = new FormulaParser("CONCATENATE(\"first\",\"second\")",null);
         fp.parse();
         Ptg[] ptg = fp.getRPNPtg();
        assertTrue("first ptg is string",ptg[0] instanceof StringPtg);
        assertTrue("second ptg is string",ptg[1] instanceof StringPtg);
    }
    
    public void testWorksheetReferences()
    {
    	HSSFWorkbook wb = new HSSFWorkbook();
    	
    	wb.createSheet("NoQuotesNeeded");
    	wb.createSheet("Quotes Needed Here &#$@");
    	
    	HSSFSheet sheet = wb.createSheet("Test");
    	HSSFRow row = sheet.createRow(0);
    	HSSFCell cell;
    	
    	cell = row.createCell((short)0);
    	cell.setCellFormula("NoQuotesNeeded!A1");
    	
    	cell = row.createCell((short)1);
    	cell.setCellFormula("'Quotes Needed Here &#$@'!A1");
    }
    
    public void testUnaryMinus()
    {
		FormulaParser fp = new FormulaParser("-A1", null);
		fp.parse();
		Ptg[] ptg = fp.getRPNPtg();
		assertTrue("got 2 ptgs", ptg.length == 2);
		assertTrue("first ptg is reference",ptg[0] instanceof ReferencePtg);
		assertTrue("second ptg is Minus",ptg[1] instanceof UnaryMinusPtg);
     }
    
    public void testUnaryPlus()
    {
		FormulaParser fp = new FormulaParser("+A1", null);
		fp.parse();
		Ptg[] ptg = fp.getRPNPtg();
		assertTrue("got 2 ptgs", ptg.length == 2);
		assertTrue("first ptg is reference",ptg[0] instanceof ReferencePtg);
		assertTrue("second ptg is Plus",ptg[1] instanceof UnaryPlusPtg);
     }
    
	public void testLeadingSpaceInString()
	{
		String value = "  hi  ";
		FormulaParser fp = new FormulaParser("\"" + value + "\"", null);
		fp.parse();
		Ptg[] ptg = fp.getRPNPtg();
    
		assertTrue("got 1 ptg", ptg.length == 1);
		assertTrue("ptg0 is a StringPtg", ptg[0] instanceof StringPtg);
		assertTrue("ptg0 contains exact value", ((StringPtg)ptg[0]).getValue().equals(value));
	}

	public void testLookupAndMatchFunctionArgs()
	{
		FormulaParser fp = new FormulaParser("lookup(A1, A3:A52, B3:B52)", null);
		fp.parse();
		Ptg[] ptg = fp.getRPNPtg();
    
		assertTrue("got 4 ptg", ptg.length == 4);
		assertTrue("ptg0 has Value class", ptg[0].getPtgClass() == Ptg.CLASS_VALUE);
		
		fp = new FormulaParser("match(A1, A3:A52)", null);
		fp.parse();
		ptg = fp.getRPNPtg();
    
		assertTrue("got 3 ptg", ptg.length == 3);
		assertTrue("ptg0 has Value class", ptg[0].getPtgClass() == Ptg.CLASS_VALUE);
	}
	
	/** bug 33160*/
	public void testLargeInt() {
		FormulaParser fp = new FormulaParser("40", null);
		fp.parse();
		Ptg[] ptg=fp.getRPNPtg();
		assertTrue("ptg is Int, is "+ptg[0].getClass(),ptg[0] instanceof IntPtg);
		
		fp = new FormulaParser("40000", null);
		fp.parse();
		ptg=fp.getRPNPtg();
		assertTrue("ptg should be  Number, is "+ptg[0].getClass(), ptg[0] instanceof NumberPtg);
	}
	/** bug 33160, testcase by Amol Deshmukh*/
	public void testSimpleLongFormula() {
		        FormulaParser fp = new FormulaParser("40000/2", null);
		        fp.parse();
		        Ptg[] ptgs = fp.getRPNPtg();
		        assertTrue("three tokens expected, got "+ptgs.length,ptgs.length == 3);
		        assertTrue("NumberPtg",(ptgs[0] instanceof NumberPtg));
		        assertTrue("IntPtg",(ptgs[1] instanceof IntPtg));
		        assertTrue("DividePtg",(ptgs[2] instanceof DividePtg));
	}
	
	/** bug 35027, underscore in sheet name*/
	public void testUnderscore() {
		HSSFWorkbook wb = new HSSFWorkbook();
    	
    	wb.createSheet("Cash_Flow");;
    	
    	HSSFSheet sheet = wb.createSheet("Test");
    	HSSFRow row = sheet.createRow(0);
    	HSSFCell cell;
    	
    	cell = row.createCell((short)0);
    	cell.setCellFormula("Cash_Flow!A1");
		
	}

    // bug 38396 : Formula with exponential numbers not parsed correctly.
    public void testExponentialParsing() {
        FormulaParser fp = new FormulaParser("1.3E21/2", null);
        fp.parse();
        Ptg[] ptgs = fp.getRPNPtg();
        assertTrue("three tokens expected, got " + ptgs.length, ptgs.length == 3);
        assertTrue("NumberPtg", (ptgs[0] instanceof NumberPtg));
        assertTrue("IntPtg", (ptgs[1] instanceof IntPtg));
        assertTrue("DividePtg", (ptgs[2] instanceof DividePtg));

        fp = new FormulaParser("1322E21/2", null);
        fp.parse();
        ptgs = fp.getRPNPtg();
        assertTrue("three tokens expected, got " + ptgs.length, ptgs.length == 3);
        assertTrue("NumberPtg", (ptgs[0] instanceof NumberPtg));
        assertTrue("IntPtg", (ptgs[1] instanceof IntPtg));
        assertTrue("DividePtg", (ptgs[2] instanceof DividePtg));

        fp = new FormulaParser("1.3E1/2", null);
        fp.parse();
        ptgs = fp.getRPNPtg();
        assertTrue("three tokens expected, got " + ptgs.length, ptgs.length == 3);
        assertTrue("NumberPtg", (ptgs[0] instanceof NumberPtg));
        assertTrue("IntPtg", (ptgs[1] instanceof IntPtg));
        assertTrue("DividePtg", (ptgs[2] instanceof DividePtg));

    }
    public void testExponentialInSheet() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();

        wb.createSheet("Cash_Flow");;

        HSSFSheet sheet = wb.createSheet("Test");
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell((short)0);
        String formula = null;

        cell.setCellFormula("1.3E21/3");
        formula = cell.getCellFormula();
        assertEquals("Exponential formula string", "1.3E21/3", formula);

        cell.setCellFormula("-1.3E21/3");
        formula = cell.getCellFormula();
        assertEquals("Exponential formula string", "-1.3E21/3", formula);

        cell.setCellFormula("1322E21/3");
        formula = cell.getCellFormula();
        assertEquals("Exponential formula string", "1.322E24/3", formula);

        cell.setCellFormula("-1322E21/3");
        formula = cell.getCellFormula();
        assertEquals("Exponential formula string", "-1.322E24/3", formula);

        cell.setCellFormula("1.3E1/3");
        formula = cell.getCellFormula();
        assertEquals("Exponential formula string", "13.0/3", formula);

        cell.setCellFormula("-1.3E1/3");
        formula = cell.getCellFormula();
        assertEquals("Exponential formula string", "-13.0/3", formula);

        cell.setCellFormula("1.3E-4/3");
        formula = cell.getCellFormula();
        assertEquals("Exponential formula string", "1.3E-4/3", formula);

        cell.setCellFormula("-1.3E-4/3");
        formula = cell.getCellFormula();
        assertEquals("Exponential formula string", "-1.3E-4/3", formula);

        cell.setCellFormula("13E-15/3");
        formula = cell.getCellFormula();
        assertEquals("Exponential formula string", "1.3E-14/3", formula);

        cell.setCellFormula("-13E-15/3");
        formula = cell.getCellFormula();
        assertEquals("Exponential formula string", "-1.3E-14/3", formula);

        cell.setCellFormula("1.3E3/3");
        formula = cell.getCellFormula();
        assertEquals("Exponential formula string", "1300.0/3", formula);

        cell.setCellFormula("-1.3E3/3");
        formula = cell.getCellFormula();
        assertEquals("Exponential formula string", "-1300.0/3", formula);

        cell.setCellFormula("1300000000000000/3");
        formula = cell.getCellFormula();
        assertEquals("Exponential formula string", "1.3E15/3", formula);

        cell.setCellFormula("-1300000000000000/3");
        formula = cell.getCellFormula();
        assertEquals("Exponential formula string", "-1.3E15/3", formula);

        cell.setCellFormula("-10E-1/3.1E2*4E3/3E4");
        formula = cell.getCellFormula();
        assertEquals("Exponential formula string", "-1.0/310.0*4000.0/30000.0", formula);
    }

     public static void main(String [] args) {
        System.out.println("Testing org.apache.poi.hssf.record.formula.FormulaParser");
        junit.textui.TestRunner.run(TestFormulaParser.class);
    }
}
