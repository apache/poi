
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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


package org.apache.poi.hssf.record.formula;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

import java.io.FileOutputStream;
import java.io.File;


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
 */
public class FormulaParser {
    
    public static int FORMULA_TYPE_CELL = 0;
    public static int FORMULA_TYPE_SHARED = 1;
    public static int FORMULA_TYPE_ARRAY =2;
    public static int FORMULA_TYPE_CONDFOMRAT = 3;
    public static int FORMULA_TYPE_NAMEDRANGE = 4;
    
    private String formulaString;
    private int pointer=0;
    
    private List tokens = new java.util.Stack();
    //private Stack tokens = new java.util.Stack();
    private List result = new ArrayList();
    private int numParen;
    
    private static char TAB = '\t';
    private static char CR = '\n';
    
   private char Look;              // Lookahead Character 
    
    
    /** create the parser with the string that is to be parsed
     *    later call the parse() method to return ptg list in rpn order
     *    then call the getRPNPtg() to retrive the parse results
     *  This class is recommended only for single threaded use
     *  The parse and getPRNPtg are internally synchronized for safety, thus
     *  while it is safe to use in a multithreaded environment, you will get long lock waits.  
     */
    public FormulaParser(String formula){
        formulaString = formula;
        pointer=0;
    }
    

    /** Read New Character From Input Stream */
    private void GetChar() {
        Look=formulaString.charAt(pointer++);
        //System.out.println("Got char: "+Look);
    }
    

    /** Report an Error */
    private void Error(String s) {
        System.out.println("Error: "+s);
    }
    
    
 
    /** Report Error and Halt */
    private void Abort(String s) {
        Error(s);
        //System.exit(1);  //throw exception??
        throw new RuntimeException("Cannot Parse, sorry");
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
    
    

    /** Skip Over Leading White Space */
    private void SkipWhite() {
        while (IsWhite(Look)) {
            GetChar();
        }
    }
    
    

    /** Match a Specific Input Character */
    private void Match(char x) {
        if (Look != x) {
            Expected("" + x + "");
        }else {
            GetChar();
            SkipWhite();
        }
    }
    
    
    /** Get an Identifier */
    private String GetName() {
        StringBuffer Token = new StringBuffer();
        if (!IsAlpha(Look)) {
            Expected("Name");
        }
        while (IsAlNum(Look)) {
            Token = Token.append(Character.toUpperCase(Look));
            GetChar();
        }
        SkipWhite();
        return Token.toString();
    }
    
    
    /** Get a Number */
    private String GetNum() {
        String Value ="";
        if  (!IsDigit(Look)) Expected("Integer");
        while (IsDigit(Look)){
            Value = Value + Look;
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
    
    /** Parse and Translate a Identifier */
    private void Ident() {
        String name;
        name = GetName();
        if (Look == '('){
            //This is a function 
            Match('(');
            int numArgs = Arguments(); 
            Match(')');
            //this is the end of the function
            tokens.add(function(name,(byte)numArgs));
        } else if (Look == ':') { // this is a AreaReference
            String first = name;
            Match(':');
            String second = GetName();
            tokens.add(new AreaPtg(first+":"+second));
        } else {
            //this can be either a cell ref or a named range !!
            boolean cellRef = true ; //we should probably do it with reg exp??
            if (cellRef) {
                tokens.add(new ReferencePtg(name)); 
            }else {
                //handle after named range is integrated!!
            }
        }
    }
    
    private Ptg function(String name,byte numArgs) {
        Ptg retval = null;
        retval = new FuncVarPtg(name,numArgs);
       /** if (numArgs == 1 && name.equals("SUM")) {
            AttrPtg ptg = new AttrPtg();
            ptg.setData((short)1); //sums don't care but this is what excel does.
            ptg.setSum(true);
            retval = ptg;
        } else {
            retval = new FuncVarPtg(name,numArgs);
        }*/
        
        return retval; 
    }
    
    /** get arguments to a function */
    private int Arguments() {
        int numArgs = 0;
        if (Look != ')')  {
            numArgs++; 
            Expression();
        }
        while (Look == ','  || Look == ';') { //TODO handle EmptyArgs
            if(Look == ',') {
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
        if (Look == '(' ) {
            Match('(');
            Expression();
            Match(')');
            tokens.add(new ParenthesisPtg());
            return;
        } else if (IsAlpha(Look)){
            Ident();
        } else if(Look == '"') {
           StringLiteral();
        } else {
             
            String number = GetNum();
            if (Look=='.') { 
                Match('.');
                String decimalPart = null;
                if (IsDigit(Look)) number = number +"."+ GetNum(); //this also takes care of someone entering "1234."
                tokens.add(new NumberPtg(number));
            } else {
                tokens.add(new IntPtg(number));  //TODO:what if the number is too big to be a short? ..add factory to return Int or Number!
            }
        }
    }
    
    private void StringLiteral() {
        Match('"');
        String name= GetName();
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
        while (Look == '*' || Look == '/' || Look == '^' || Look == '&') {
            ///TODO do we need to do anything here??
            if (Look == '*') Multiply();
            if (Look == '/') Divide();
            if (Look == '^') Power();
            if (Look == '&') Concat();
        }
    }
    
    /** Recognize and Translate an Add */
    private void Add() {
        Match('+');
        Term();
        tokens.add(new AddPtg());
    }
    
    /** Recognize and Translate an Add */
    private void Concat() {
        Match('&');
        Term();
        tokens.add(new ConcatPtg());
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
        if (IsAddop(Look)) {
            EmitLn("CLR D0");  //unaryAdd ptg???
        } else {
            Term();
        }
        while (IsAddop(Look)) {
            if ( Look == '+' )  Add();
            if (Look == '-') Subtract();
            // if (Look == '*') Multiply();
           // if (Look == '/') Divide();
        }
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
    public static String toFormulaString(List lptgs) {
        String retval = null;
        if (lptgs == null || lptgs.size() == 0) return "#NAME";
        Ptg[] ptgs = new Ptg[lptgs.size()];
        ptgs = (Ptg[])lptgs.toArray(ptgs);
        retval = toFormulaString(ptgs);
        return retval;
    }
    
    /** Static method to convert an array of Ptgs in RPN order 
     *  to a human readable string format in infix mode
     *  @param ptgs - array of ptgs, can be null or empty
     */
    public static String toFormulaString(Ptg[] ptgs) {
        if (ptgs == null || ptgs.length == 0) return "#NAME";
        java.util.Stack stack = new java.util.Stack();
        int numPtgs = ptgs.length;
        OperationPtg o;
        int numOperands;
        String[] operands;
        for (int i=0;i<numPtgs;i++) {
           // Excel allows to have AttrPtg at position 0 (such as Blanks) which
           // do not have any operands. Skip them.
            if (ptgs[i] instanceof OperationPtg && i>0) {
                  o = (OperationPtg) ptgs[i];
                  numOperands = o.getNumberOfOperands();
                  operands = new String[numOperands];
                  for (int j=0;j<numOperands;j++) {
                      operands[numOperands-j-1] = (String) stack.pop(); //TODO: catch stack underflow and throw parse exception. 
                      
                  }  
                  String result = o.toFormulaString(operands);
                  stack.push(result);
            } else {
                stack.push(ptgs[i].toFormulaString());
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
        StringBuffer buf = new StringBuffer();
           for (int i=0;i<tokens.size();i++) {
            buf.append( ( (Ptg)tokens.get(i)).toFormulaString());
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
    
