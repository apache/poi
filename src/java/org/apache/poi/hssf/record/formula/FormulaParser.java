
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

import org.apache.poi.hssf.usermodel.*;
import java.io.FileOutputStream;
import java.io.File;


/**
 * EXPERIMENTAL 
 *
 *
 * This class parses a formula string into a List of tokens in RPN order
 * Inspired by 
 *           Lets Build a Compiler, by Jack Crenshaw
 * BNF for the formula expression is :
 * <expression> ::= <term> [<addop> <term>]*
 * <term> ::= <factor>  [ <mulop> <factor ]*
 * <factor> ::= <number> | (<expression>) | <cellRef> 
 *
 *  @author Avik Sengupta <avik AT Avik Sengupta DOT com>
 *  @author Andrew C. oliver (acoliver at apache dot org)
 */
public class FormulaParser {
    
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
        return Character.isLetter(c);
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
        String Token;
        Token = "";
        if (!IsAlpha(Look)) {
            Expected("Name");
        }
        while (IsAlNum(Look)) {
            Token = Token + Character.toUpperCase(Look);
            GetChar();
        }
        
        SkipWhite();
        return Token;
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
            tokens.add(new FunctionPtg(name,(byte)numArgs));
        } else if (Look == ':') { // this is a AreaReference
            String first = name;
            GetChar();
            String second = GetName();
                tokens.add(new AreaPtg(first+":"+second));
            //String second = ;
        } else {
            //this can be either a cell ref or a named range !!
            
            boolean cellRef = true ; //we should probably do it with reg exp??
            if (cellRef) {
                tokens.add(new ReferencePtg(name)); //TODO we need to pass in Name somewhere??
            }else {
                //handle after named range is integrated!!
            }
        }
    }
    
    /** get arguments to a function */
    private int Arguments() {
        int numArgs = 0;
        if (Look != ')')  {
            numArgs++; 
            Expression();
        }
        while (Look == ',') {
            Match(',');
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
        }else{

            IntPtg p = new IntPtg();
            p.setValue(Short.parseShort(GetNum()));
            tokens.add(p);
        }
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
    
    /** API call to retrive the array of Ptgs created as 
     * a result of the parsing
     */
    public Ptg[] getRPNPtg() {
       synchronized (tokens) {
            if (tokens == null) throw new IllegalStateException("Please parse a string before trying to access the parse result");
            Ptg[] retval = new Ptg[tokens.size()];
            return (Ptg[]) tokens.toArray(retval);
       }
    }
  
    /**
     * Convience method which takes in a list then passes it to the other toFormulaString
     * signature
     */
    public static String toFormulaString(List lptgs) {
        String retval = null;
        Ptg[] ptgs = new Ptg[lptgs.size()];
        ptgs = (Ptg[])lptgs.toArray(ptgs);
        retval = toFormulaString(ptgs);
        return retval;
    }
    
    /** Static method to convert an array of Ptgs in RPN order 
     *  to a human readable string format in infix mode
     *  TODO - extra brackets might appear, but string will be semantically correct. 
     */
    public static String toFormulaString(Ptg[] ptgs) {
        java.util.Stack stack = new java.util.Stack();
        int numPtgs = ptgs.length;
        OperationPtg o;
        int numOperands;
        String[] operands;
        for (int i=0;i<numPtgs;i++) {
            if (ptgs[i] instanceof OperationPtg) {
                o = (OperationPtg) ptgs[i];
                numOperands = o.getNumberOfOperands();
                operands = new String[numOperands];
                for (int j=0;j<numOperands;j++) {
                    operands[numOperands-j-1] = (String) stack.pop(); //TODO: catch stack underflow and throw parse exception. 
                    
                }  
                String result = o.toFormulaString(operands);
                //if (! (o instanceof DummyFunctionPtg) ) result = "("+result+")" ;
                stack.push(result);
            } else {
                stack.push(ptgs[i].toFormulaString());
            }
        }
        return (String) stack.pop(); //TODO: catch stack underflow and throw parse exception. 
    }
   
    public String toString() {
        StringBuffer buf = new StringBuffer();
           for (int i=0;i<tokens.size();i++) {
            buf.append( ( (Ptg)tokens.get(i)).toFormulaString());
            buf.append(' ');
        } 
        return buf.toString();
    }
    
    
    /** Main Program for testing*/
    public static void main(String[] argv) {
        FormulaParser fp = new FormulaParser(argv[0]+";");
        System.out.println("\nFormula is: ");
        fp.parse();
        System.out.println("RPN Form is: " +fp.toString());
        
        System.out.println("Converted Text form is : "+fp.toFormulaString(fp.getRPNPtg()));
        try {
        short            rownum = 0;
        File file = File.createTempFile("testFormula",".xls");
        FileOutputStream out    = new FileOutputStream(file);
        HSSFWorkbook     wb     = new HSSFWorkbook();
        HSSFSheet        s      = wb.createSheet();
        HSSFRow          r      = null;
        HSSFCell         c      = null;
        
        r = s.createRow((short) 0);
        c = r.createCell((short) 0);
        c.setCellFormula(argv[0]);
        
        wb.write(out);
        out.close();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }
        //If Look <> CR then Expected('NewLine');
    }
    
} 