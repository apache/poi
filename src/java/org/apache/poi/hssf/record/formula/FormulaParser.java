
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

/**
 * EXPERIMENTAL code to parse formulas back and forth between RPN and not
 *
 * @author Avik Sengupta <lists@aviksengupta.com>
 */
public class FormulaParser {
    
    private String formulaString;
    private int pointer=0;
    
    private Stack operationsList = new java.util.Stack();
    private Stack operandsList = new java.util.Stack();
    private List result = new ArrayList();
    private int numParen;
    
    //{--------------------------------------------------------------}
    //{ Constant Declarations }
    
    private static char TAB = '\t';
    private static char CR = '\n';
    
    //{--------------------------------------------------------------}
    //{ Variable Declarations }
    
    private char Look;              //{ Lookahead Character }
    
    public FormulaParser(String formula){
        formulaString = formula;
        pointer=0;
    }
    
    //{--------------------------------------------------------------}
    //{ Read New Character From Input Stream }
    
    private void GetChar() {
        Look=formulaString.charAt(pointer++);
        System.out.println("Got char: "+Look);
    }
    
    //{--------------------------------------------------------------}
    //{ Report an Error }
    
    private void Error(String s) {
        System.out.println("Error: "+s);
    }
    
    
    //{--------------------------------------------------------------}
    //{ Report Error and Halt }
    
    private void Abort(String s) {
        Error(s);
        //System.exit(1);  //throw exception??
        throw new RuntimeException("Cannot Parse, sorry");
    }
    
    
    //{--------------------------------------------------------------}
    //{ Report What Was Expected }
    
    private void Expected(String s) {
        Abort(s + " Expected");
    }
    
    
    //{--------------------------------------------------------------}
    //{ Recognize an Alpha Character }
    
    private boolean IsAlpha(char c) {
        return Character.isLetter(c);
        //return  UpCase(c) in ['A'..'Z'];
    }
    
    
    //{--------------------------------------------------------------}
    //{ Recognize a Decimal Digit }
    
    private boolean IsDigit(char c) {
        System.out.println("Checking digit for"+c);
        return Character.isDigit(c);
        
        //return ("0123456789".indexOf( (int) c) != 0)//c in ['0'..'9'];
    }
    
    
    //{--------------------------------------------------------------}
    //{ Recognize an Alphanumeric }
    
    private boolean  IsAlNum(char c) {
        return  (IsAlpha(c) || IsDigit(c));
    }
    
    
    //{--------------------------------------------------------------}
    //{ Recognize an Addop }
    
    private boolean IsAddop( char c) {
        return (c =='+' || c =='-');
    }
    
    
    //{--------------------------------------------------------------}
    //{ Recognize White Space }
    
    private boolean IsWhite( char c) {
        return  (c ==' ' || c== TAB);
    }
    
    
    //{--------------------------------------------------------------}
    //{ Skip Over Leading White Space }
    
    private void SkipWhite() {
        while (IsWhite(Look)) {
            GetChar();
        }
    }
    
    
    //{--------------------------------------------------------------}
    //{ Match a Specific Input Character }
    
    private void Match(char x) {
        if (Look != x) {
            Expected("" + x + "");
        }else {
            GetChar();
            SkipWhite();
        }
    }
    
    
    //{--------------------------------------------------------------}
    //{ Get an Identifier }
    
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
    
    
    //{--------------------------------------------------------------}
    //{ Get a Number }
    
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
    
    
    //{--------------------------------------------------------------}
    //{ Output a String with Tab }
    
    private void  Emit(String s){
        System.out.print(TAB+s);
    }
    
    
    //{--------------------------------------------------------------}
    //{ Output a String with Tab and CRLF }
    
    private void EmitLn(String s) {
        Emit(s);
        System.out.println();;
    }
    
    
    //{---------------------------------------------------------------}
    //{ Parse and Translate a Identifier }
    
    private void Ident() {
        String Name;
        Name = GetName();
        if (Look == '('){
            Match('(');
            //Expression() -- add this!
            Match(')');
            //this is the end of the function
            //EmitLn("BSR " + Name);
        } else {
            //EmitLn("MOVE " + Name + "(PC),D0b");
            //this can be either a cell ref or a named range !!
            
            boolean cellRef = true ; //we should probably do it with reg exp??
            if (cellRef) {
                operationsList.add(new ValueReferencePtg()); //TODO we need to pass in Name somewhere
            }else {
                //handle after named range is integrated!!
            }
        }
    }

    
    //{---------------------------------------------------------------}
    //{ Parse and Translate a Math Factor }
    
    //procedure Expression; Forward;
    
    private void Factor() {
        if (Look == '(' ) {
            Match('(');
            operationsList.add(new ParenthesisPtg());
            Expression();
            Match(')');
            operationsList.add(new ParenthesisPtg());
            return;
        } else if (IsAlpha(Look)){
            Ident();
        }else{
            //EmitLn("MOVE #" + GetNum() + ",D0");
            IntPtg p = new IntPtg();
            p.setValue(Short.parseShort(GetNum()));
            operandsList.add(p);
        }
    }

    
    //{--------------------------------------------------------------}
    //{ Recognize and Translate a Multiply }
    
    private void Multiply(){
        Match('*');
        Factor();
        operationsList.add(new MultiplyPtg());
        //EmitLn("MULS (SP)+,D0");
    }
    
    
    //{-------------------------------------------------------------}
    //{ Recognize and Translate a Divide }
    
    private void Divide() {
        Match('/');
        Factor();
        operationsList.add(new DividePtg());
        //EmitLn("MOVE (SP)+,D1");
        //EmitLn("EXS.L D0");
        //EmitLn("DIVS D1,D0");
    }
    
    
    //{---------------------------------------------------------------}
    //{ Parse and Translate a Math Term }
    
    private void  Term(){
        Factor();
        while (Look == '*' || Look == '/' ) {
            //EmitLn("MOVE D0,-(SP)");
            ///TODO do we need to do anything here??
            if (Look == '*') Multiply();
            if (Look == '/') Divide();
        }
    }
    
    
    //{--------------------------------------------------------------}
    //{ Recognize and Translate an Add }
    
    private void Add() {
        Match('+');
        Term();
        //EmitLn("ADD (SP)+,D0");
        operationsList.add(new AddPtg());
    }
    
    
    //{-------------------------------------------------------------}
    //{ Recognize and Translate a Subtract }
    
    private void Subtract() {
        Match('-');
        Term();
        operationsList.add(new SubtractPtg());
        //EmitLn("SUB (SP)+,D0");
        //EmitLn("NEG D0");
    }
    
    
    //{---------------------------------------------------------------}
    //{ Parse and Translate an Expression }
    
    private void Expression() {
        if (IsAddop(Look)) {
            EmitLn("CLR D0");  //unaryAdd ptg???
        } else {
            Term();
        }
        while (IsAddop(Look)) {
            EmitLn("MOVE D0,-(SP)");
            if ( Look == '+' )  Add();
            if (Look == '-') Subtract();
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
   EmitLn('LEA ' + Name + '(PC),A0');
   EmitLn('MOVE D0,(A0)')
end;
     **/
    
    //{--------------------------------------------------------------}
    //{ Initialize }
    
    private void  Init() {
        GetChar();
        SkipWhite();
    }
    
    public void parse() {
        Init();
        Expression();
        //now tokenisation is done .. convert to RPN!!
        tokenToRPN();
    }
    
    private void tokenToRPN() {
        OperationPtg op;
        Ptg operand;
        int numOper = 0;
        int numOnStack = 0;
        result.add(operandsList.pop()); numOnStack++;
        
        while (!operationsList.isEmpty()) {
            op = (OperationPtg) operationsList.pop();
            if (op instanceof ParenthesisPtg) {
                // do something smart
            }
            
            
            for (numOper = op.getNumberOfOperands();numOper>0;numOper--) {
                if (numOnStack==0) {
                    result.add(operandsList.pop());//numOnStack++;
                } else {
                    numOnStack--;
                }
            }
            result.add(op);
            numOnStack++;
        }
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
           for (int i=0;i<result.size();i++) {
            buf.append( ( (Ptg)result.get(i)).toFormulaString());
            buf.append(' ');
        } 
        return buf.toString();
    }
    
    
    //{--------------------------------------------------------------}
    //{ Main Program for testing}
    public static void main(String[] argv) {
        FormulaParser fp = new FormulaParser(argv[0]+";");
        fp.parse();
        System.out.println(fp.toString());
        
        //If Look <> CR then Expected('NewLine');
    }
    //{--------------------------------------------------------------}
    
} 