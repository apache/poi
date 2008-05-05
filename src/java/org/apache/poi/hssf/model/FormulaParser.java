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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

//import PTG's .. since we need everything, import *
import org.apache.poi.hssf.record.formula.*;
import org.apache.poi.hssf.record.formula.function.FunctionMetadata;
import org.apache.poi.hssf.record.formula.function.FunctionMetadataRegistry;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

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
 *  @author Avik Sengupta <avik at apache dot org>
 *  @author Andrew C. oliver (acoliver at apache dot org)
 *  @author Eric Ladner (eladner at goldinc dot com)
 *  @author Cameron Riley (criley at ekmail.com)
 *  @author Peter M. Murray (pete at quantrix dot com)
 *  @author Pavel Krupets (pkrupets at palmtreebusiness dot com)
 */
public final class FormulaParser {

    /**
     * Specific exception thrown when a supplied formula does not parse properly.<br/>
     * Primarily used by test cases when testing for specific parsing exceptions.</p>
     *
     */
    static final class FormulaParseException extends RuntimeException {
        // This class was given package scope until it would become clear that it is useful to
        // general client code. 
        public FormulaParseException(String msg) {
            super(msg);
        }
    }

    public static int FORMULA_TYPE_CELL = 0;
    public static int FORMULA_TYPE_SHARED = 1;
    public static int FORMULA_TYPE_ARRAY =2;
    public static int FORMULA_TYPE_CONDFOMRAT = 3;
    public static int FORMULA_TYPE_NAMEDRANGE = 4;

    private final String formulaString;
    private final int formulaLength;
    private int pointer;

    private final List tokens = new Stack();

    /**
     * Used for spotting if we have a cell reference,
     *  or a named range
     */
    private final static Pattern CELL_REFERENCE_PATTERN = Pattern.compile("(?:('?)[^:\\\\/\\?\\*\\[\\]]+\\1!)?\\$?[A-Za-z]+\\$?[\\d]+");

    private static char TAB = '\t';

    /**
     * Lookahead Character.
     * gets value '\0' when the input string is exhausted
     */
    private char look;

    private HSSFWorkbook book;


    /**
     * Create the formula parser, with the string that is to be
     *  parsed against the supplied workbook.
     * A later call the parse() method to return ptg list in
     *  rpn order, then call the getRPNPtg() to retrive the
     *  parse results.
     * This class is recommended only for single threaded use.
     *
     * If you only have a usermodel.HSSFWorkbook, and not a
     *  model.Workbook, then use the convenience method on
     *  usermodel.HSSFFormulaEvaluator
     */
    public FormulaParser(String formula, HSSFWorkbook book){
        formulaString = formula;
        pointer=0;
        this.book = book;
        formulaLength = formulaString.length();
    }

    public static Ptg[] parse(String formula, HSSFWorkbook book) {
        FormulaParser fp = new FormulaParser(formula, book);
        fp.parse();
        return fp.getRPNPtg();
    }

    /** Read New Character From Input Stream */
    private void GetChar() {
        // Check to see if we've walked off the end of the string.
        if (pointer > formulaLength) {
            throw new RuntimeException("too far");
        }
        if (pointer < formulaLength) {
            look=formulaString.charAt(pointer);
        } else {
            // Just return if so and reset 'look' to something to keep
            // SkipWhitespace from spinning
            look = (char)0;
        }    
        pointer++;
        //System.out.println("Got char: "+ look);
    }

    /** Report What Was Expected */
    private RuntimeException expected(String s) {
        return new FormulaParseException(s + " Expected");
    }



    /** Recognize an Alpha Character */
    private boolean IsAlpha(char c) {
        return Character.isLetter(c) || c == '$' || c=='_';
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


    /** Recognize White Space */
    private boolean IsWhite( char c) {
        return  (c ==' ' || c== TAB);
    }

    /** Skip Over Leading White Space */
    private void SkipWhite() {
        while (IsWhite(look)) {
            GetChar();
        }
    }

    /**
     *  Consumes the next input character if it is equal to the one specified otherwise throws an
     *  unchecked exception. This method does <b>not</b> consume whitespace (before or after the
     *  matched character). 
     */
    private void Match(char x) {
        if (look != x) {
            throw expected("'" + x + "'");
        }
        GetChar();
    }

    /** Get an Identifier */
    private String GetName() {
        StringBuffer Token = new StringBuffer();
        if (!IsAlpha(look) && look != '\'') {
            throw expected("Name");
        }
        if(look == '\'')
        {
            Match('\'');
            boolean done = look == '\'';
            while(!done)
            {
                Token.append(look);
                GetChar();
                if(look == '\'')
                {
                    Match('\'');
                    done = look != '\'';
                }
            }
        }
        else
        {
            while (IsAlNum(look)) {
                Token.append(look);
                GetChar();
            }
        }
        return Token.toString();
    }


    /** Get a Number */
    private String GetNum() {
        StringBuffer value = new StringBuffer();

        while (IsDigit(this.look)){
            value.append(this.look);
            GetChar();
        }
        return value.length() == 0 ? null : value.toString();
    }

    /** Parse and Translate a String Identifier */
    private Ptg parseIdent() {
        String name;
        name = GetName();
        if (look == '('){
            //This is a function
            return function(name);
        }

        if (look == ':' || look == '.') { // this is a AreaReference
            GetChar();

            while (look == '.') { // formulas can have . or .. or ... instead of :
                GetChar();
            }

            String first = name;
            String second = GetName();
            return new AreaPtg(first+":"+second);
        }

        if (look == '!') {
            Match('!');
            String sheetName = name;
            String first = GetName();
            short externIdx = book.getExternalSheetIndex(book.getSheetIndex(sheetName));
            if (look == ':') {
                Match(':');
                String second=GetName();
                if (look == '!') {
                    //The sheet name was included in both of the areas. Only really
                    //need it once
                    Match('!');
                    String third=GetName();

                    if (!sheetName.equals(second))
                        throw new RuntimeException("Unhandled double sheet reference.");

                    return new Area3DPtg(first+":"+third,externIdx);
                }
                return new Area3DPtg(first+":"+second,externIdx);
            }
            return new Ref3DPtg(first,externIdx);
        }
        if (name.equalsIgnoreCase("TRUE") || name.equalsIgnoreCase("FALSE")) {
            return new BoolPtg(name.toUpperCase());
        }

        // This can be either a cell ref or a named range
        // Try to spot which it is
        boolean cellRef = CELL_REFERENCE_PATTERN.matcher(name).matches();
 
        if (cellRef) {
            return new ReferencePtg(name);
        }

        for(int i = 0; i < book.getNumberOfNames(); i++) {
            // named range name matching is case insensitive
        	if(book.getNameAt(i).getNameName().equalsIgnoreCase(name)) {
                return new NamePtg(name, book);
            }
        }
        throw new FormulaParseException("Found reference to named range \"" 
                    + name + "\", but that named range wasn't defined!");
    }

    /**
     * Adds a pointer to the last token to the latest function argument list.
     * @param obj
     */
    private void addArgumentPointer(List argumentPointers) {
        argumentPointers.add(tokens.get(tokens.size()-1));
    }

    /**
     * Note - Excel function names are 'case aware but not case sensitive'.  This method may end
     * up creating a defined name record in the workbook if the specified name is not an internal
     * Excel function, and has not been encountered before. 
     * 
     * @param name case preserved function name (as it was entered/appeared in the formula). 
     */
    private Ptg function(String name) {
        int numArgs =0 ;
        // Note regarding parameter - 
        if(!AbstractFunctionPtg.isInternalFunctionName(name)) {
            // external functions get a Name token which points to a defined name record
            NamePtg nameToken = new NamePtg(name, this.book);
            
            // in the token tree, the name is more or less the first argument
            numArgs++;  
            tokens.add(nameToken);
        }
        //average 2 args per function
        List argumentPointers = new ArrayList(2);

        Match('(');
        numArgs += Arguments(argumentPointers);
        Match(')');

        return getFunction(name, numArgs, argumentPointers);
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
    private AbstractFunctionPtg getFunction(String name, int numArgs, List argumentPointers) {

        boolean isVarArgs;
        int funcIx;
        FunctionMetadata fm = FunctionMetadataRegistry.getFunctionByName(name.toUpperCase());
        if(fm == null) {
            // must be external function
            isVarArgs = true;
            funcIx = FunctionMetadataRegistry.FUNCTION_INDEX_EXTERNAL;
        } else {
            isVarArgs = !fm.hasFixedArgsLength();
            funcIx = fm.getIndex();
            validateNumArgs(numArgs, fm);
        }
        AbstractFunctionPtg retval;
        if(isVarArgs) {
            retval = new FuncVarPtg(name, (byte)numArgs);
        } else {
            retval = new FuncPtg(funcIx);
        }
        if (!name.equals(AbstractFunctionPtg.FUNCTION_NAME_IF)) {
            // early return for everything else besides IF()
            return retval;
        }


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
        if (ptgCount > Short.MAX_VALUE) {
            throw new RuntimeException("Ptg Size exceeds short when being specified for a goto ptg in an if");
        }

        goto1Ptg.setData((short)(ptgCount-1));

        return retval;
    }

    private void validateNumArgs(int numArgs, FunctionMetadata fm) {
        if(numArgs < fm.getMinParams()) {
            String msg = "Too few arguments to function '" + fm.getName() + "'. ";
            if(fm.hasFixedArgsLength()) {
                msg += "Expected " + fm.getMinParams();
            } else {
                msg += "At least " + fm.getMinParams() + " were expected";
            }
            msg += " but got " + numArgs + ".";
            throw new FormulaParseException(msg);
         }
        if(numArgs > fm.getMaxParams()) {
            String msg = "Too many arguments to function '" + fm.getName() + "'. ";
            if(fm.hasFixedArgsLength()) {
                msg += "Expected " + fm.getMaxParams();
            } else {
                msg += "At most " + fm.getMaxParams() + " were expected";
            }
            msg += " but got " + numArgs + ".";
            throw new FormulaParseException(msg);
       }
    }

    private static boolean isArgumentDelimiter(char ch) {
        return ch ==  ',' || ch == ')';
    }
    
    /** get arguments to a function */
    private int Arguments(List argumentPointers) {
        SkipWhite();
        if(look == ')') {
            return 0;
        }
        
        boolean missedPrevArg = true;
        
        int numArgs = 0;
        while(true) {
            SkipWhite();
            if(isArgumentDelimiter(look)) {
                if(missedPrevArg) {
                    tokens.add(new MissingArgPtg());
                    addArgumentPointer(argumentPointers);
                    numArgs++;
                }
                if(look == ')') {
                    break;
                }
                Match(',');
                missedPrevArg = true;
                continue;
            }
            comparisonExpression();
            addArgumentPointer(argumentPointers);
            numArgs++;
            missedPrevArg = false;
        }
        return numArgs;
    }

   /** Parse and Translate a Math Factor  */
    private void powerFactor() {
        percentFactor();
        while(true) {
            SkipWhite();
            if(look != '^') {
                return;
            }
            Match('^');
            percentFactor();
            tokens.add(new PowerPtg());
        }
    }
    
    private void percentFactor() {
        tokens.add(parseSimpleFactor());
        while(true) {
            SkipWhite();
            if(look != '%') {
                return;
            }
            Match('%');
            tokens.add(new PercentPtg());
        }
    }
    
    
    /**
     * factors (without ^ or % )
     */
    private Ptg parseSimpleFactor() {
        SkipWhite();
        switch(look) {
            case '#':
                return parseErrorLiteral();
            case '-':
                Match('-');
                powerFactor();
                return new UnaryMinusPtg();
            case '+':
                Match('+');
                powerFactor();
                return new UnaryPlusPtg();
            case '(':
                Match('(');
                comparisonExpression();
                Match(')');
                return new ParenthesisPtg();
            case '"':
                return parseStringLiteral();
            case ',':
            case ')':
                return new MissingArgPtg(); // TODO - not quite the right place to recognise a missing arg
        }
        if (IsAlpha(look) || look == '\''){
            return parseIdent();
        }
        // else - assume number
        return parseNumber();
    }


    private Ptg parseNumber() {
        String number2 = null;
        String exponent = null;
        String number1 = GetNum();

        if (look == '.') {
            GetChar();
            number2 = GetNum();
        }

        if (look == 'E') {
            GetChar();

            String sign = "";
            if (look == '+') {
                GetChar();
            } else if (look == '-') {
                GetChar();
                sign = "-";
            }

            String number = GetNum();
            if (number == null) {
                throw expected("Integer");
            }
            exponent = sign + number;
        }

        if (number1 == null && number2 == null) {
            throw expected("Integer");
        }

        return getNumberPtgFromString(number1, number2, exponent);
    }


    private ErrPtg parseErrorLiteral() {
        Match('#');
        String part1 = GetName().toUpperCase();

        switch(part1.charAt(0)) {
            case 'V':
                if(part1.equals("VALUE")) {
                    Match('!');
                    return ErrPtg.VALUE_INVALID;
                }
                throw expected("#VALUE!");
            case 'R':
                if(part1.equals("REF")) {
                    Match('!');
                    return ErrPtg.REF_INVALID;
                }
                throw expected("#REF!");
            case 'D':
                if(part1.equals("DIV")) {
                    Match('/');
                    Match('0');
                    Match('!');
                    return ErrPtg.DIV_ZERO;
                }
                throw expected("#DIV/0!");
            case 'N':
                if(part1.equals("NAME")) {
                    Match('?');  // only one that ends in '?'
                    return ErrPtg.NAME_INVALID;
                }
                if(part1.equals("NUM")) {
                    Match('!');
                    return ErrPtg.NUM_ERROR;
                }
                if(part1.equals("NULL")) {
                    Match('!');
                    return ErrPtg.NULL_INTERSECTION;
                }
                if(part1.equals("N")) {
                    Match('/');
                    if(look != 'A' && look != 'a') {
                        throw expected("#N/A");
                    }
                    Match(look);
                    // Note - no '!' or '?' suffix
                    return ErrPtg.N_A;
                }
                throw expected("#NAME?, #NUM!, #NULL! or #N/A");

        }
        throw expected("#VALUE!, #REF!, #DIV/0!, #NAME?, #NUM!, #NULL! or #N/A");
    }


    /**
     * Get a PTG for an integer from its string representation.
     * return Int or Number Ptg based on size of input
     */
    private static Ptg getNumberPtgFromString(String number1, String number2, String exponent) {
        StringBuffer number = new StringBuffer();

        if (number2 == null) {
            number.append(number1);

            if (exponent != null) {
                number.append('E');
                number.append(exponent);
            }

            String numberStr = number.toString();
            int intVal;
            try {
                intVal = Integer.parseInt(numberStr);
            } catch (NumberFormatException e) {
                return new NumberPtg(numberStr);
            }
            if (IntPtg.isInRange(intVal)) {
                return new IntPtg(intVal);
            }
            return new NumberPtg(numberStr);
        }

        if (number1 != null) {
            number.append(number1);
        }

        number.append('.');
        number.append(number2);

        if (exponent != null) {
            number.append('E');
            number.append(exponent);
        }

        return new NumberPtg(number.toString());
    }


    private StringPtg parseStringLiteral()
    {
        Match('"');
        
        StringBuffer token = new StringBuffer();
        while (true) {
            if (look == '"') {
                GetChar();
                if (look != '"') {
                    break;
                }
             }
            token.append(look);
            GetChar();
        }
        return new StringPtg(token.toString());
    }

    /** Parse and Translate a Math Term */
    private void  Term() {
        powerFactor();
        while(true) {
            SkipWhite();
            switch(look) {
                case '*':
                    Match('*');
                    powerFactor();
                    tokens.add(new MultiplyPtg());
                    continue;
                case '/':
                    Match('/');
                    powerFactor();
                    tokens.add(new DividePtg());
                    continue;
            }
            return; // finished with Term
        }
    }
    
    private void comparisonExpression() {
        concatExpression();
        while (true) {
            SkipWhite();
            switch(look) {
                case '=':
                case '>':
                case '<':
                    Ptg comparisonToken = getComparisonToken();
                    concatExpression();
                    tokens.add(comparisonToken);
                    continue;
            }
            return; // finished with predicate expression
        }
    }

    private Ptg getComparisonToken() {
        if(look == '=') {
            Match(look);
            return new EqualPtg();
        }
        boolean isGreater = look == '>';
        Match(look);
        if(isGreater) {
            if(look == '=') {
                Match('=');
                return new GreaterEqualPtg();
            }
            return new GreaterThanPtg();
        }
        switch(look) {
            case '=':
                Match('=');
                return new LessEqualPtg();
            case '>':
                Match('>');
                return new NotEqualPtg();
        }
        return new LessThanPtg();
    }
    

    private void concatExpression() {
        additiveExpression();
        while (true) {
            SkipWhite();
            if(look != '&') {
                break; // finished with concat expression
            }
            Match('&');
            additiveExpression();
            tokens.add(new ConcatPtg());
        }
    }
    

    /** Parse and Translate an Expression */
    private void additiveExpression() {
        Term();
        while (true) {
            SkipWhite();
            switch(look) {
                case '+':
                    Match('+');
                    Term();
                    tokens.add(new AddPtg());
                    continue;
                case '-':
                    Match('-');
                    Term();
                    tokens.add(new SubtractPtg());
                    continue;
            }
            return; // finished with additive expression
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


    /** API call to execute the parsing of the formula
     *
     */
    public void parse() {
        pointer=0;
        GetChar();
        comparisonExpression();

        if(pointer <= formulaLength) {
            String msg = "Unused input [" + formulaString.substring(pointer-1) 
                + "] after attempting to parse the formula [" + formulaString + "]"; 
            throw new FormulaParseException(msg);
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
        Ptg p = n.getValue();
            if (formulaType == FormulaParser.FORMULA_TYPE_NAMEDRANGE) {
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
        Ptg p = n.getValue();
        int numOperands = n.getNumChildren();
        if (p instanceof AbstractFunctionPtg) {
            for (int i =0;i<numOperands;i++) {
                setParameterRVA(n.getChild(i),((AbstractFunctionPtg)p).getParameterClass(i),formulaType);
//                if (n.getChild(i).getValue() instanceof AbstractFunctionPtg) {
//                    setParameterRVA(n.getChild(i),formulaType);
//                }
                setParameterRVA(n.getChild(i),formulaType);
            }
        } else {
            for (int i =0;i<numOperands;i++) {
                setParameterRVA(n.getChild(i),formulaType);
            }
        }
    }
    private void setParameterRVA(Node n, int expectedClass,int formulaType) {
        Ptg p = n.getValue();
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
        Ptg p = n.getValue();
        if (p instanceof AbstractFunctionPtg || !(p instanceof OperationPtg)) {
            p.setClass(theClass);
        } else {
            for (int i =0;i<n.getNumChildren();i++) {
                setClass(n.getChild(i),theClass);
            }
        }
     }
    /**
     * Convience method which takes in a list then passes it to the
     *  other toFormulaString signature.
     * @param book   workbook for 3D and named references
     * @param lptgs  list of Ptg, can be null or empty
     * @return a human readable String
     */
    public static String toFormulaString(HSSFWorkbook book, List lptgs) {
        String retval = null;
        if (lptgs == null || lptgs.size() == 0) return "#NAME";
        Ptg[] ptgs = new Ptg[lptgs.size()];
        ptgs = (Ptg[])lptgs.toArray(ptgs);
        retval = toFormulaString(book, ptgs);
        return retval;
    }
    /**
     * Convience method which takes in a list then passes it to the
     *  other toFormulaString signature. Works on the current
     *  workbook for 3D and named references
     * @param lptgs  list of Ptg, can be null or empty
     * @return a human readable String
     */
    public String toFormulaString(List lptgs) {
        return toFormulaString(book, lptgs);
    }

    /**
     * Static method to convert an array of Ptgs in RPN order
     * to a human readable string format in infix mode.
     * @param book  workbook for named and 3D references
     * @param ptgs  array of Ptg, can be null or empty
     * @return a human readable String
     */
    public static String toFormulaString(HSSFWorkbook book, Ptg[] ptgs) {
        if (ptgs == null || ptgs.length == 0) {
            // TODO - what is the justification for returning "#NAME" (which is not "#NAME?", btw)
            return "#NAME";
        }
        Stack stack = new Stack();

        for (int i=0 ; i < ptgs.length; i++) {
            Ptg ptg = ptgs[i];
            // TODO - what about MemNoMemPtg?
            if(ptg instanceof MemAreaPtg || ptg instanceof MemFuncPtg || ptg instanceof MemErrPtg) {
                // marks the start of a list of area expressions which will be naturally combined
                // by their trailing operators (e.g. UnionPtg)
                // TODO - put comment and throw exception in toFormulaString() of these classes
                continue;
            }
            if (! (ptg instanceof OperationPtg)) {
                stack.push(ptg.toFormulaString(book));
                continue;
            }

            if (ptg instanceof AttrPtg) {
                AttrPtg attrPtg = ((AttrPtg) ptg);
                if (attrPtg.isOptimizedIf()) {
                    continue;
                }
                if (attrPtg.isSpace()) {
                    // POI currently doesn't render spaces in formulas
                    continue;
                    // but if it ever did, care must be taken:
                    // tAttrSpace comes *before* the operand it applies to, which may be consistent
                    // with how the formula text appears but is against the RPN ordering assumed here 
                }
                if (attrPtg.isSemiVolatile()) {
                    // similar to tAttrSpace - RPN is violated
                    continue;
                }
            }

            final OperationPtg o = (OperationPtg) ptg;
            int nOperands = o.getNumberOfOperands();
            final String[] operands = new String[nOperands];

            for (int j = nOperands-1; j >= 0; j--) { // reverse iteration because args were pushed in-order
                if(stack.isEmpty()) {
                   String msg = "Too few arguments suppled to operation token ("
                        + o.getClass().getName() + "). Expected (" + nOperands
                        + ") operands but got (" + (nOperands - j - 1) + ")";
                    throw new IllegalStateException(msg);
                }
                operands[j] = (String) stack.pop();
            }
            stack.push(o.toFormulaString(operands));
        }
        if(stack.isEmpty()) {
            // inspection of the code above reveals that every stack.pop() is followed by a 
            // stack.push(). So this is either an internal error or impossible.
            throw new IllegalStateException("Stack underflow");
        }
        String result = (String) stack.pop();
        if(!stack.isEmpty()) {
            // Might be caused by some tokens like AttrPtg and Mem*Ptg, which really shouldn't
            // put anything on the stack
            throw new IllegalStateException("too much stuff left on the stack");
        }
        return result;
    }
    /**
     * Static method to convert an array of Ptgs in RPN order
     *  to a human readable string format in infix mode. Works
     *  on the current workbook for named and 3D references.
     * @param ptgs  array of Ptg, can be null or empty
     * @return a human readable String
     */
    public String toFormulaString(Ptg[] ptgs) {
        return toFormulaString(book, ptgs);
    }


    /** Create a tree representation of the RPN token array
     *used to run the class(RVA) change algo
     */
    private Node createTree() {
        Stack stack = new Stack();
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
            buf.append( ( (Ptg)tokens.get(i)).toFormulaString(book));
            buf.append(' ');
        }
        return buf.toString();
    }

    /** Private helper class, used to create a tree representation of the formula*/
    private static final class Node {
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
}
