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
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

//import PTGs .. since we need everything, import *
import org.apache.poi.hssf.record.formula.*;
import org.apache.poi.hssf.record.formula.function.FunctionMetadata;
import org.apache.poi.hssf.record.formula.function.FunctionMetadataRegistry;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.AreaReference;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.util.CellReference.NameType;

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

    public static final int FORMULA_TYPE_CELL = 0;
    public static final int FORMULA_TYPE_SHARED = 1;
    public static final int FORMULA_TYPE_ARRAY =2;
    public static final int FORMULA_TYPE_CONDFOMRAT = 3;
    public static final int FORMULA_TYPE_NAMEDRANGE = 4;

    private final String formulaString;
    private final int formulaLength;
    private int pointer;

    private ParseNode _rootNode;

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
        String msg = "Parse error near char " + (pointer-1) + " '" + look + "'"
            + " in specified formula '" + formulaString + "'. Expected "
            + s;
        return new FormulaParseException(msg);
    }

    /** Recognize an Alpha Character */
    private boolean IsAlpha(char c) {
        return Character.isLetter(c) || c == '$' || c=='_';
    }

    /** Recognize a Decimal Digit */
    private boolean IsDigit(char c) {
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

    /**
     * Parses a sheet name, named range name, or simple cell reference.<br/>
     * Note - identifiers in Excel can contain dots, so this method may return a String
     * which may need to be converted to an area reference.  For example, this method 
     * may return a value like "A1..B2", in which case the caller must convert it to 
     * an area reference like "A1:B2"
     */
    private String parseIdentifier() {
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
            // allow for any sequence of dots and identifier chars
            // special case of two consecutive dots is best treated in the calling code
            while (IsAlNum(look) || look == '.') {
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

    private ParseNode parseFunctionReferenceOrName() {
        String name = parseIdentifier();
        if (look == '('){
            //This is a function
            return function(name);
        }
        return new ParseNode(parseNameOrReference(name));
    }

    private Ptg parseNameOrReference(String name) {
        
        AreaReference areaRef = parseArea(name);
        if (areaRef != null) {
            // will happen if dots are used instead of colon
            return new AreaPtg(areaRef.formatAsString());
        }

        if (look == ':' || look == '.') { // this is a AreaReference
            GetChar();

            while (look == '.') { // formulas can have . or .. or ... instead of :
                GetChar();
            }

            String first = name;
            String second = parseIdentifier();
            return new AreaPtg(first+":"+second);
        }

        if (look == '!') {
            Match('!');
            String sheetName = name;
            String first = parseIdentifier();
            short externIdx = book.getExternalSheetIndex(book.getSheetIndex(sheetName));
            areaRef = parseArea(name);
            if (areaRef != null) {
                // will happen if dots are used instead of colon
                return new Area3DPtg(areaRef.formatAsString(), externIdx);
            }
            if (look == ':') {
                Match(':');
                String second=parseIdentifier();
                if (look == '!') {
                    //The sheet name was included in both of the areas. Only really
                    //need it once
                    Match('!');
                    String third=parseIdentifier();

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
        int nameType = CellReference.classifyCellReference(name);
        if (nameType == NameType.CELL) {
            return new RefPtg(name);
        }
        if (nameType != NameType.NAMED_RANGE) {
            new FormulaParseException("Name '" + name
                + "' does not look like a cell reference or named range");
        }

        for(int i = 0; i < book.getNumberOfNames(); i++) {
            // named range name matching is case insensitive
            if(book.getNameAt(i).getNameName().equalsIgnoreCase(name)) {
                return new NamePtg(name, book);
            }
        }
        throw new FormulaParseException("Specified named range '"
                    + name + "' does not exist in the current workbook.");
    }

    /**
     * @param name an 'identifier' like string (i.e. contains alphanums, and dots)
     * @return <code>null</code> if name cannot be split at a dot
     */
    private AreaReference parseArea(String name) {
        int dotPos = name.indexOf('.');
        if (dotPos < 0) {
            return null;
        }
        int dotCount = 1;
        while (dotCount<name.length() && name.charAt(dotPos+dotCount) == '.') {
            dotCount++;
            if (dotCount>3) {
                // four or more consecutive dots does not convert to ':'
                return null;
            }
        }
        // This expression is only valid as an area ref, if the LHS and RHS of the dot(s) are both
        // cell refs.  Otherwise, this expression must be a named range name
        String partA = name.substring(0, dotPos);
        if (!isValidCellReference(partA)) {
            return null;
        }
        String partB = name.substring(dotPos+dotCount);
        if (!isValidCellReference(partB)) {
            return null;
        }
        CellReference topLeft = new CellReference(partA);
        CellReference bottomRight = new CellReference(partB);
        return new AreaReference(topLeft, bottomRight);
    }

    /**
     * @return <code>true</code> if the specified name is a valid cell reference
     */
    private static boolean isValidCellReference(String str) {
        return CellReference.classifyCellReference(str) == NameType.CELL;
    }


    /**
     * Note - Excel function names are 'case aware but not case sensitive'.  This method may end
     * up creating a defined name record in the workbook if the specified name is not an internal
     * Excel function, and has not been encountered before.
     *
     * @param name case preserved function name (as it was entered/appeared in the formula).
     */
    private ParseNode function(String name) {
        NamePtg nameToken = null;
        // Note regarding parameter -
        if(!AbstractFunctionPtg.isInternalFunctionName(name)) {
            // external functions get a Name token which points to a defined name record
            nameToken = new NamePtg(name, this.book);

            // in the token tree, the name is more or less the first argument
        }

        Match('(');
        ParseNode[] args = Arguments();
        Match(')');

        return getFunction(name, nameToken, args);
    }

    /**
     * Generates the variable function ptg for the formula.
     * <p>
     * For IF Formulas, additional PTGs are added to the tokens
     * @param name
     * @param numArgs
     * @return Ptg a null is returned if we're in an IF formula, it needs extreme manipulation and is handled in this function
     */
    private ParseNode getFunction(String name, NamePtg namePtg, ParseNode[] args) {

        FunctionMetadata fm = FunctionMetadataRegistry.getFunctionByName(name.toUpperCase());
        int numArgs = args.length;
        if(fm == null) {
            if (namePtg == null) {
                throw new IllegalStateException("NamePtg must be supplied for external functions");
            }
            // must be external function
            ParseNode[] allArgs = new ParseNode[numArgs+1];
            allArgs[0] = new ParseNode(namePtg);
            System.arraycopy(args, 0, allArgs, 1, numArgs);
            return new ParseNode(new FuncVarPtg(name, (byte)(numArgs+1)), allArgs);
        }

        if (namePtg != null) {
            throw new IllegalStateException("NamePtg no applicable to internal functions");
        }
        boolean isVarArgs = !fm.hasFixedArgsLength();
        int funcIx = fm.getIndex();
        validateNumArgs(args.length, fm);

        AbstractFunctionPtg retval;
        if(isVarArgs) {
            retval = new FuncVarPtg(name, (byte)numArgs);
        } else {
            retval = new FuncPtg(funcIx);
        }
        return new ParseNode(retval, args);
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
    private ParseNode[] Arguments() {
        //average 2 args per function
        List temp = new ArrayList(2);
        SkipWhite();
        if(look == ')') {
            return ParseNode.EMPTY_ARRAY;
        }

        boolean missedPrevArg = true;
        int numArgs = 0;
        while (true) {
            SkipWhite();
            if (isArgumentDelimiter(look)) {
                if (missedPrevArg) {
                    temp.add(new ParseNode(MissingArgPtg.instance));
                    numArgs++;
                }
                if (look == ')') {
                    break;
                }
                Match(',');
                missedPrevArg = true;
                continue;
            }
            temp.add(comparisonExpression());
            numArgs++;
            missedPrevArg = false;
            SkipWhite();
            if (!isArgumentDelimiter(look)) {
                throw expected("',' or ')'");
            }
        }
        ParseNode[] result = new ParseNode[temp.size()];
        temp.toArray(result);
        return result;
    }

   /** Parse and Translate a Math Factor  */
    private ParseNode powerFactor() {
        ParseNode result = percentFactor();
        while(true) {
            SkipWhite();
            if(look != '^') {
                return result;
            }
            Match('^');
            ParseNode other = percentFactor();
            result = new ParseNode(PowerPtg.instance, result, other);
        }
    }

    private ParseNode percentFactor() {
        ParseNode result = parseSimpleFactor();
        while(true) {
            SkipWhite();
            if(look != '%') {
                return result;
            }
            Match('%');
            result = new ParseNode(PercentPtg.instance, result);
        }
    }


    /**
     * factors (without ^ or % )
     */
    private ParseNode parseSimpleFactor() {
        SkipWhite();
        switch(look) {
            case '#':
                return new ParseNode(parseErrorLiteral());
            case '-':
                Match('-');
                return new ParseNode(UnaryMinusPtg.instance, powerFactor());
            case '+':
                Match('+');
                return new ParseNode(UnaryPlusPtg.instance, powerFactor());
            case '(':
                Match('(');
                ParseNode inside = comparisonExpression();
                Match(')');
                return new ParseNode(ParenthesisPtg.instance, inside);
            case '"':
                return new ParseNode(parseStringLiteral());
        }
        if (IsAlpha(look) || look == '\''){
            return parseFunctionReferenceOrName();
        }
        // else - assume number
        return new ParseNode(parseNumber());
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
        String part1 = parseIdentifier().toUpperCase();

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


    private StringPtg parseStringLiteral() {
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
    private ParseNode  Term() {
        ParseNode result = powerFactor();
        while(true) {
            SkipWhite();
            Ptg operator;
            switch(look) {
                case '*':
                    Match('*');
                    operator = MultiplyPtg.instance;
                    break;
                case '/':
                    Match('/');
                    operator = DividePtg.instance;
                    break;
                default:
                    return result; // finished with Term
            }
            ParseNode other = powerFactor();
            result = new ParseNode(operator, result, other);
        }
    }

    private ParseNode comparisonExpression() {
        ParseNode result = concatExpression();
        while (true) {
            SkipWhite();
            switch(look) {
                case '=':
                case '>':
                case '<':
                    Ptg comparisonToken = getComparisonToken();
                    ParseNode other = concatExpression();
                    result = new ParseNode(comparisonToken, result, other);
                    continue;
            }
            return result; // finished with predicate expression
        }
    }

    private Ptg getComparisonToken() {
        if(look == '=') {
            Match(look);
            return EqualPtg.instance;
        }
        boolean isGreater = look == '>';
        Match(look);
        if(isGreater) {
            if(look == '=') {
                Match('=');
                return GreaterEqualPtg.instance;
            }
            return GreaterThanPtg.instance;
        }
        switch(look) {
            case '=':
                Match('=');
                return LessEqualPtg.instance;
            case '>':
                Match('>');
                return NotEqualPtg.instance;
        }
        return LessThanPtg.instance;
    }


    private ParseNode concatExpression() {
        ParseNode result = additiveExpression();
        while (true) {
            SkipWhite();
            if(look != '&') {
                break; // finished with concat expression
            }
            Match('&');
            ParseNode other = additiveExpression();
            result = new ParseNode(ConcatPtg.instance, result, other);
        }
        return result;
    }


    /** Parse and Translate an Expression */
    private ParseNode additiveExpression() {
        ParseNode result = Term();
        while (true) {
            SkipWhite();
            Ptg operator;
            switch(look) {
                case '+':
                    Match('+');
                    operator = AddPtg.instance;
                    break;
                case '-':
                    Match('-');
                    operator = SubtractPtg.instance;
                    break;
                default:
                    return result; // finished with additive expression
            }
            ParseNode other = Term();
            result = new ParseNode(operator, result, other);
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


    /**
     *  API call to execute the parsing of the formula
     * @deprecated use Ptg[] FormulaParser.parse(String, HSSFWorkbook) directly
     */
    public void parse() {
        pointer=0;
        GetChar();
        _rootNode = comparisonExpression();

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
        OperandClassTransformer oct = new OperandClassTransformer(formulaType);
        // RVA is for 'operand class': 'reference', 'value', 'array'
        oct.transformFormula(_rootNode);
        return ParseNode.toTokenArray(_rootNode);
    }

    /**
     * Convenience method which takes in a list then passes it to the
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
     * Convenience method which takes in a list then passes it to the
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
            if (ptg instanceof ParenthesisPtg) {
                String contents = (String)stack.pop();
                stack.push ("(" + contents + ")");
                continue;
            }
            if (ptg instanceof AttrPtg) {
                AttrPtg attrPtg = ((AttrPtg) ptg);
                if (attrPtg.isOptimizedIf() || attrPtg.isOptimizedChoose() || attrPtg.isGoto()) {
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
                if (attrPtg.isSum()) {
                    String[] operands = getOperands(stack, attrPtg.getNumberOfOperands());
                    stack.push(attrPtg.toFormulaString(operands));
                    continue;
                }
                throw new RuntimeException("Unexpected tAttr: " + attrPtg.toString());
            }

            if (! (ptg instanceof OperationPtg)) {
                stack.push(ptg.toFormulaString(book));
                continue;
            }

            OperationPtg o = (OperationPtg) ptg;
            String[] operands = getOperands(stack, o.getNumberOfOperands());
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
    
    private static String[] getOperands(Stack stack, int nOperands) {
        String[] operands = new String[nOperands];

        for (int j = nOperands-1; j >= 0; j--) { // reverse iteration because args were pushed in-order
            if(stack.isEmpty()) {
               String msg = "Too few arguments supplied to operation. Expected (" + nOperands
                    + ") operands but got (" + (nOperands - j - 1) + ")";
                throw new IllegalStateException(msg);
            }
            operands[j] = (String) stack.pop();
        }
        return operands;
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
}
