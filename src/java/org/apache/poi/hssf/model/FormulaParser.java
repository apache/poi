
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003, 2003 The Apache Software Foundation.  All rights
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//import PTG's .. since we need everything, import *
import org.apache.poi.hssf.record.formula.*;

import org.apache.poi.hssf.util.SheetReferences;


/**
 * This class parses a formula string into a List of tokens in RPN order.
 * Inspired by 
 *           Lets Build a Compiler, by Jack Crenshaw
 * BNF for the formula expression is :
 * <expression> ::= <term> [<addop> <term>]*
 * <term> ::= <factor>  [ <mulop> <factor> ]*
 * <factor> ::= <number> | (<expression>) | <cellRef> | <function>
 * <function> ::= <functionName> ([expression [, expression]*])
 *
 *  @author Avik Sengupta <avik AT Avik Sengupta DOT com>
 *  @author Andrew C. oliver (acoliver at apache dot org)
 *  @author Eric Ladner (eladner at goldinc dot com)
 *  @author Cameron Riley (criley at ekmail.com)
 */
public class FormulaParser {
    
    public static int FORMULA_TYPE_CELL = 0;
    public static int FORMULA_TYPE_SHARED = 1;
    public static int FORMULA_TYPE_ARRAY =2;
    public static int FORMULA_TYPE_CONDFOMRAT = 3;
    public static int FORMULA_TYPE_NAMEDRANGE = 4;
    
    private String formulaString;
    private int pointer=0;
    private int formulaLength;
    
    private List tokens = new java.util.Stack();
    
    /**
     * Using an unsynchronized linkedlist to implement a stack since we're not multi-threaded.
     */
    private List functionTokens = new LinkedList();
    
    //private Stack tokens = new java.util.Stack();
    private List result = new ArrayList();
    private int numParen;
    
    private static char TAB = '\t';
    private static char CR = '\n';
    
   private char look;              // Lookahead Character
   private boolean inFunction = false;
   
   private Workbook book;
    
    
    /** create the parser with the string that is to be parsed
     *    later call the parse() method to return ptg list in rpn order
     *    then call the getRPNPtg() to retrive the parse results
     *  This class is recommended only for single threaded use
     */
    public FormulaParser(String formula, Workbook book){
        formulaString = formula;
        pointer=0;
        this.book = book;
    	formulaLength = formulaString.length();
    }
    

    /** Read New Character From Input Stream */
    private void GetChar() {
        // Check to see if we've walked off the end of the string.
	// Just return if so and reset Look to smoething to keep 
	// SkipWhitespace from spinning
        if (pointer == formulaLength) {
            look = (char)0;
	    return;
	}
        look=formulaString.charAt(pointer++);
        //System.out.println("Got char: "+ look);
    }
    

    /** Report an Error */
    private void Error(String s) {
        System.out.println("Error: "+s);
    }
    
    
 
    /** Report Error and Halt */
    private void Abort(String s) {
        Error(s);
        //System.exit(1);  //throw exception??
        throw new RuntimeException("Cannot Parse, sorry : "+s);
    }
    
    

    /** Report What Was Expected */
    private void Expected(String s) {
        Abort(s + " Expected");
    }
    
    
 
    /** Recognize an Alpha Character */
    private boolean IsAlpha(char c) {
        return Character.isLetter(c) || c == '$';
    }
    
    
 
    /** Recognize a Decimal Digit */
    private boolean IsDigit(char c) {
        //System.out.println("Checking digit for"+c);
        return Character.isDigit(c);
    }
    
    

    /** Recognize an Alphanumeric */
    private boolean  IsAlNum(char c) {
        return  (IsAlpha(c) || IsDigit(c));
    }
    
    

    /** Recognize an Addop */
    private boolean IsAddop( char c) {
        return (c =='+' || c =='-');
    }
    

    /** Recognize White Space */
    private boolean IsWhite( char c) {
        return  (c ==' ' || c== TAB);
    }
    
    /**
     * Determines special characters;primarily in use for definition of string literals
     * @param c
     * @return boolean
     */
    private boolean IsSpecialChar(char c) {
    	return (c == '>' || c== '<' || c== '=' || c=='&' || c=='[' || c==']');
    }
    

    /** Skip Over Leading White Space */
    private void SkipWhite() {
        while (IsWhite(look)) {
            GetChar();
        }
    }
    
    

    /** Match a Specific Input Character */
    private void Match(char x) {
        if (look != x) {
            Expected("" + x + "");
        }else {
            GetChar();
            SkipWhite();
        }
    }
    
    /** Get an Identifier */
    private String GetName() {
        StringBuffer Token = new StringBuffer();
        if (!IsAlpha(look)) {
            Expected("Name");
        }
        while (IsAlNum(look)) {
            Token = Token.append(Character.toUpperCase(look));
            GetChar();
        }
        SkipWhite();
        return Token.toString();
    }
    
    /**Get an Identifier AS IS, without stripping white spaces or 
       converting to uppercase; used for literals */
    private String GetNameAsIs() {
        StringBuffer Token = new StringBuffer();
		
		while (IsAlNum(look) || IsWhite(look) || IsSpecialChar(look)) {
            Token = Token.append(look);
            GetChar();
        }
        return Token.toString();
    }
    
    
    /** Get a Number */
    private String GetNum() {
        String Value ="";
        if  (!IsDigit(look)) Expected("Integer");
        while (IsDigit(look)){
            Value = Value + look;
            GetChar();
        }
        SkipWhite();
        return Value;
    }

    /** Output a String with Tab */
    private void  Emit(String s){
        System.out.print(TAB+s);
    }

    /** Output a String with Tab and CRLF */
    private void EmitLn(String s) {
        Emit(s);
        System.out.println();;
    }
    
    /** Parse and Translate a String Identifier */
    private void Ident() {
        String name;
        name = GetName();
        if (look == '('){
            //This is a function
            function(name);
        } else if (look == ':') { // this is a AreaReference
            String first = name;
            Match(':');
            String second = GetName();
            tokens.add(new AreaPtg(first+":"+second));
        } else if (look == '!') {
            Match('!');
            String sheetName = name;
            String first = GetName();
            short externIdx = book.checkExternSheet(book.getSheetIndex(sheetName));
            if (look == ':') {
                Match(':');
                String second=GetName();
                
                tokens.add(new Area3DPtg(first+":"+second,externIdx));
            } else {
                tokens.add(new Ref3DPtg(first,externIdx));
            }
        } else {
            //this can be either a cell ref or a named range !!
            boolean cellRef = true ; //we should probably do it with reg exp??
            boolean boolLit = (name.equals("TRUE") || name.equals("FALSE"));
            if (boolLit) {
                tokens.add(new BoolPtg(name));
            } else if (cellRef) {
                tokens.add(new ReferencePtg(name));
            }else {
                //handle after named range is integrated!!
            }
        }
    }
    
    /**
     * Adds a pointer to the last token to the latest function argument list.
     * @param obj
     */
    private void addArgumentPointer() {
		if (this.functionTokens.size() > 0) {
			//no bounds check because this method should not be called unless a token array is setup by function()
			List arguments = (List)this.functionTokens.get(0);
			arguments.add(tokens.get(tokens.size()-1));
		}
    }
    
    private void function(String name) {
    	//average 2 args per function
    	this.functionTokens.add(0, new ArrayList(2));
    	
        Match('(');
        int numArgs = Arguments();
        Match(')');
                
        Ptg functionPtg = getFunction(name,(byte)numArgs);
        
		tokens.add(functionPtg);
 
 		//remove what we just put in
		this.functionTokens.remove(0);
    }
    
    /**
     * Adds the size of all the ptgs after the provided index (inclusive).
     * <p>
     * Initially used to count a goto
     * @param index
     * @return int
     */
    private int getPtgSize(int index) {
    	int count = 0;
    	
    	Iterator ptgIterator = tokens.listIterator(index);
    	while (ptgIterator.hasNext()) {
    		Ptg ptg = (Ptg)ptgIterator.next();
    		count+=ptg.getSize();
    	}
    	
    	return count;
    }
    
    private int getPtgSize(int start, int end) {
        int count = 0;
        int index = start;
        Iterator ptgIterator = tokens.listIterator(index);
        while (ptgIterator.hasNext() && index <= end) {
            Ptg ptg = (Ptg)ptgIterator.next();
            count+=ptg.getSize();
            index++;
        }
        
        return count;
    }
    /**
     * Generates the variable function ptg for the formula.
     * <p>
     * For IF Formulas, additional PTGs are added to the tokens 
     * @param name
     * @param numArgs
     * @return Ptg a null is returned if we're in an IF formula, it needs extreme manipulation and is handled in this function
     */
    private Ptg getFunction(String name,byte numArgs) {
        Ptg retval = null;
        
        if (name.equals("IF")) {
            retval = new FuncVarPtg(AbstractFunctionPtg.ATTR_NAME, numArgs);
            
            //simulated pop, no bounds checking because this list better be populated by function()
            List argumentPointers = (List)this.functionTokens.get(0);
            
            
            AttrPtg ifPtg = new AttrPtg();
            ifPtg.setData((short)7); //mirroring excel output
            ifPtg.setOptimizedIf(true);
            
            if (argumentPointers.size() != 2  && argumentPointers.size() != 3) {
                throw new IllegalArgumentException("["+argumentPointers.size()+"] Arguments Found - An IF formula requires 2 or 3 arguments. IF(CONDITION, TRUE_VALUE, FALSE_VALUE [OPTIONAL]");
            }
            
            //Biffview of an IF formula record indicates the attr ptg goes after the condition ptgs and are
            //tracked in the argument pointers
            //The beginning first argument pointer is the last ptg of the condition
            int ifIndex = tokens.indexOf(argumentPointers.get(0))+1;
            tokens.add(ifIndex, ifPtg);
            
            //we now need a goto ptgAttr to skip to the end of the formula after a true condition
            //the true condition is should be inserted after the last ptg in the first argument
            
            int gotoIndex = tokens.indexOf(argumentPointers.get(1))+1;
            
            AttrPtg goto1Ptg = new AttrPtg();
            goto1Ptg.setGoto(true);
            
            
            tokens.add(gotoIndex, goto1Ptg);
            
            
            if (numArgs > 2) { //only add false jump if there is a false condition
                
                //second goto to skip past the function ptg
                AttrPtg goto2Ptg = new AttrPtg();
                goto2Ptg.setGoto(true);
                goto2Ptg.setData((short)(retval.getSize()-1));
                //Page 472 of the Microsoft Excel Developer's kit states that:
                //The b(or w) field specifies the number byes (or words to skip, minus 1
                
                tokens.add(goto2Ptg); //this goes after all the arguments are defined
            }
            
            //data portion of the if ptg points to the false subexpression (Page 472 of MS Excel Developer's kit)
            //count the number of bytes after the ifPtg to the False Subexpression
            //doesn't specify -1 in the documentation
            ifPtg.setData((short)(getPtgSize(ifIndex+1, gotoIndex)));
            
            //count all the additional (goto) ptgs but dont count itself
            int ptgCount = this.getPtgSize(gotoIndex)-goto1Ptg.getSize()+retval.getSize();
            if (ptgCount > (int)Short.MAX_VALUE) {
                throw new RuntimeException("Ptg Size exceeds short when being specified for a goto ptg in an if");
            }
            
            goto1Ptg.setData((short)(ptgCount-1));
            
        } else {
            
            retval = new FuncVarPtg(name,numArgs);
        }
        
        return retval;
    }
    
    /** get arguments to a function */
    private int Arguments() {
        int numArgs = 0;
        if (look != ')')  {
            numArgs++; 
            Expression();
        }
        while (look == ','  || look == ';') { //TODO handle EmptyArgs
            if(look == ',') {
              Match(',');
            }
            else {
              Match(';');
            }
            Expression();
            numArgs++;
        }
        return numArgs;
    }

   /** Parse and Translate a Math Factor  */
    private void Factor() {
        if (look == '(' ) {
            Match('(');
            Expression();
            Match(')');
            tokens.add(new ParenthesisPtg());
            return;
        } else if (IsAlpha(look)){
            Ident();
        } else if(look == '"') {
           StringLiteral();
        } else {
             
            String number = GetNum();
            if (look=='.') {
                Match('.');
                String decimalPart = null;
                if (IsDigit(look)) number = number +"."+ GetNum(); //this also takes care of someone entering "1234."
                tokens.add(new NumberPtg(number));
            } else {
                tokens.add(new IntPtg(number));  //TODO:what if the number is too big to be a short? ..add factory to return Int or Number!
            }
        }
    }
    
    private void StringLiteral() {
        Match('"');
        String name= GetNameAsIs();
        Match('"');
        tokens.add(new StringPtg(name));
    }
    
    /** Recognize and Translate a Multiply */
    private void Multiply(){
        Match('*');
        Factor();
        tokens.add(new MultiplyPtg());
  
    }
    
    
    /** Recognize and Translate a Divide */
    private void Divide() {
        Match('/');
        Factor();
        tokens.add(new DividePtg());

    }
    
    
    /** Parse and Translate a Math Term */
    private void  Term(){
        Factor();
        while (look == '*' || look == '/' || look == '^' || look == '&' || 
               look == '=' || look == '>' || look == '<' ) {
            ///TODO do we need to do anything here??
            if (look == '*') Multiply();
            if (look == '/') Divide();
            if (look == '^') Power();
            if (look == '&') Concat();
            if (look == '=') Equal();
            if (look == '>') GreaterThan();
            if (look == '<') LessThan();
        }
    }
    
    /** Recognize and Translate an Add */
    private void Add() {
        Match('+');
        Term();
        tokens.add(new AddPtg());
    }
    
    /** Recognize and Translate a Concatination */
    private void Concat() {
        Match('&');
        Term();
        tokens.add(new ConcatPtg());
    }
    
    /** Recognize and Translate a test for Equality  */
    private void Equal() {
        Match('=');
        Term();
        tokens.add(new EqualPtg());
    }
    
    /** Recognize and Translate a Subtract */
    private void Subtract() {
        Match('-');
        Term();
        tokens.add(new SubtractPtg());
    }    

    private void Power() {
        Match('^');
        Term();
        tokens.add(new PowerPtg());
    }
    
    
    /** Parse and Translate an Expression */
    private void Expression() {
        if (IsAddop(look)) {
            EmitLn("CLR D0");  //unaryAdd ptg???
        } else {
            Term();
        }
        while (IsAddop(look)) {
            if ( look == '+' )  Add();
            if (look == '-') Subtract();
            if (look == '*') Multiply();
            if (look == '/') Divide();
            if (look == '>') GreaterThan();
            if (look == '<') LessThan();
        }
        addArgumentPointer();
        
    }
    
    /** Recognize and Translate a Greater Than  */
    private void GreaterThan() {
        Match('>');
        Term();
        tokens.add(new GreaterThanPtg());
    }
    
    /** Recognize and Translate a Less Than  */
    private void LessThan() {
        Match('<');
        Term();
        tokens.add(new LessThanPtg());
    }   
    
    
    //{--------------------------------------------------------------}
    //{ Parse and Translate an Assignment Statement }
    /**
procedure Assignment;
var Name: string[8];
begin
   Name := GetName;
   Match('=');
   Expression;

end;
     **/
    
 
    /** Initialize */
    
    private void  init() {
        GetChar();
        SkipWhite();
    }
    
    /** API call to execute the parsing of the formula
     *
     */
    public void parse() {
        synchronized (tokens) {
            init();
            Expression();
        }
    }
    
    
    /*********************************
     * PARSER IMPLEMENTATION ENDS HERE
     * EXCEL SPECIFIC METHODS BELOW
     *******************************/
    
    /** API call to retrive the array of Ptgs created as 
     * a result of the parsing
     */
    public Ptg[] getRPNPtg() {
     return getRPNPtg(FORMULA_TYPE_CELL);
    }
    
    public Ptg[] getRPNPtg(int formulaType) {
        Node node = createTree();
        setRootLevelRVA(node, formulaType);
        setParameterRVA(node,formulaType);
        return (Ptg[]) tokens.toArray(new Ptg[0]);
    }
    
    private void setRootLevelRVA(Node n, int formulaType) {
        //Pg 16, excelfileformat.pdf @ openoffice.org
        Ptg p = (Ptg) n.getValue();
            if (formulaType == this.FORMULA_TYPE_NAMEDRANGE) {
                if (p.getDefaultOperandClass() == Ptg.CLASS_REF) {
                    setClass(n,Ptg.CLASS_REF);
                } else {
                    setClass(n,Ptg.CLASS_ARRAY);
                }
            } else {
                setClass(n,Ptg.CLASS_VALUE);
            }
        
    }
    
    private void setParameterRVA(Node n, int formulaType) {
        Ptg p = (Ptg) n.getValue();
        if (p instanceof AbstractFunctionPtg) {
            int numOperands = n.getNumChildren();
            for (int i =0;i<n.getNumChildren();i++) {
                setParameterRVA(n.getChild(i),((AbstractFunctionPtg)p).getParameterClass(i),formulaType);
                if (n.getChild(i).getValue() instanceof AbstractFunctionPtg) {
                    setParameterRVA(n.getChild(i),formulaType);
                }
            }  
        } else {
            for (int i =0;i<n.getNumChildren();i++) {
                setParameterRVA(n.getChild(i),formulaType);
            }
        } 
    }
    private void setParameterRVA(Node n, int expectedClass,int formulaType) {
        Ptg p = (Ptg) n.getValue();
        if (expectedClass == Ptg.CLASS_REF) { //pg 15, table 1 
            if (p.getDefaultOperandClass() == Ptg.CLASS_REF ) {
                setClass(n, Ptg.CLASS_REF);
            }
            if (p.getDefaultOperandClass() == Ptg.CLASS_VALUE) {
                if (formulaType==FORMULA_TYPE_CELL || formulaType == FORMULA_TYPE_SHARED) {
                    setClass(n,Ptg.CLASS_VALUE);
                } else {
                    setClass(n,Ptg.CLASS_ARRAY);
                }
            }
            if (p.getDefaultOperandClass() == Ptg.CLASS_ARRAY ) {
                setClass(n, Ptg.CLASS_ARRAY);
            }
        } else if (expectedClass == Ptg.CLASS_VALUE) { //pg 15, table 2
            if (formulaType == FORMULA_TYPE_NAMEDRANGE) {
                setClass(n,Ptg.CLASS_ARRAY) ;
            } else {
                setClass(n,Ptg.CLASS_VALUE);
            }
        } else { //Array class, pg 16. 
            if (p.getDefaultOperandClass() == Ptg.CLASS_VALUE &&
                 (formulaType==FORMULA_TYPE_CELL || formulaType == FORMULA_TYPE_SHARED)) {
                 setClass(n,Ptg.CLASS_VALUE);
            } else {
                setClass(n,Ptg.CLASS_ARRAY);
            }
        }
    }
    
     private void setClass(Node n, byte theClass) {
        Ptg p = (Ptg) n.getValue();
        if (p instanceof AbstractFunctionPtg || !(p instanceof OperationPtg)) {
            p.setClass(theClass);
        } else {
            for (int i =0;i<n.getNumChildren();i++) {
                setClass(n.getChild(i),theClass);
            }
        }
     }
    /**
     * Convience method which takes in a list then passes it to the other toFormulaString
     * signature. 
     * @param lptgs - list of ptgs, can be null
     */
    public static String toFormulaString(SheetReferences refs, List lptgs) {
        String retval = null;
        if (lptgs == null || lptgs.size() == 0) return "#NAME";
        Ptg[] ptgs = new Ptg[lptgs.size()];
        ptgs = (Ptg[])lptgs.toArray(ptgs);
        retval = toFormulaString(refs, ptgs);
        return retval;
    }
    
    /** Static method to convert an array of Ptgs in RPN order 
     *  to a human readable string format in infix mode
     *  @param ptgs - array of ptgs, can be null or empty
     */
    public static String toFormulaString(SheetReferences refs, Ptg[] ptgs) {
        if (ptgs == null || ptgs.length == 0) return "#NAME";
        java.util.Stack stack = new java.util.Stack();
        int numPtgs = ptgs.length;
        OperationPtg o;
        int numOperands;
        String result=null;
        String[] operands;
        AttrPtg ifptg = null;
        for (int i=0;i<numPtgs;i++) {
           // Excel allows to have AttrPtg at position 0 (such as Blanks) which
           // do not have any operands. Skip them.
            if (ptgs[i] instanceof OperationPtg && i>0) {
                  o = (OperationPtg) ptgs[i];
                  
                  if (o instanceof AttrPtg && ((AttrPtg)o).isOptimizedIf()) {
                        ifptg=(AttrPtg)o;
                  } else {
                      
                      numOperands = o.getNumberOfOperands();
                      operands = new String[numOperands];
                      
                      for (int j=0;j<numOperands;j++) {
                          operands[numOperands-j-1] = (String) stack.pop(); //TODO: catch stack underflow and throw parse exception. 
                      }  

                      if ( (o instanceof AbstractFunctionPtg) && 
                            ((AbstractFunctionPtg)o).getName().equals("specialflag") &&
                            ifptg != null
                            ) {
                             // this special case will be way different.
                             result = ifptg.toFormulaString(
                                  new String[] {(o.toFormulaString(operands))}
                                                           );
                             ifptg = null;
                      } else {                      
                        result = o.toFormulaString(operands);                                              
                      }
                      stack.push(result);                                        
                  }
                      
                  
            } else {
                stack.push(ptgs[i].toFormulaString(refs));
            }
        }
        return (String) stack.pop(); //TODO: catch stack underflow and throw parse exception. 
    }
    
    private Node createTree() {
        java.util.Stack stack = new java.util.Stack();
        int numPtgs = tokens.size();
        OperationPtg o;
        int numOperands;
        Node[] operands;
        for (int i=0;i<numPtgs;i++) {
            if (tokens.get(i) instanceof OperationPtg) {
                
                o = (OperationPtg) tokens.get(i);
                numOperands = o.getNumberOfOperands();
                operands = new Node[numOperands];
                for (int j=0;j<numOperands;j++) {
                    operands[numOperands-j-1] = (Node) stack.pop(); 
                }
                Node result = new Node(o);
                result.setChildren(operands);
                stack.push(result);
            } else {
                stack.push(new Node((Ptg)tokens.get(i)));
            }
        }
        return (Node) stack.pop();
    }
   
    /** toString on the parser instance returns the RPN ordered list of tokens
     *   Useful for testing
     */
    public String toString() {
        SheetReferences refs = null;
        if (book!=null)  book.getSheetReferences();
        StringBuffer buf = new StringBuffer();
           for (int i=0;i<tokens.size();i++) {
            buf.append( ( (Ptg)tokens.get(i)).toFormulaString(refs));
            buf.append(' ');
        } 
        return buf.toString();
    }
    
}    
    class Node {
        private Ptg value=null;
        private Node[] children=new Node[0];
        private int numChild=0;
        public Node(Ptg val) {
            value = val; 
        }
        public void setChildren(Node[] child) {children = child;numChild=child.length;}
        public int getNumChildren() {return numChild;}
        public Node getChild(int number) {return children[number];}
        public Ptg getValue() {return value;}
    }
