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

package org.apache.poi.ss.formula;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.record.UnicodeString;
import org.apache.poi.hssf.record.constant.ErrorConstant;
import org.apache.poi.hssf.record.formula.AbstractFunctionPtg;
import org.apache.poi.hssf.record.formula.AddPtg;
import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.hssf.record.formula.ArrayPtg;
import org.apache.poi.hssf.record.formula.BoolPtg;
import org.apache.poi.hssf.record.formula.ConcatPtg;
import org.apache.poi.hssf.record.formula.DividePtg;
import org.apache.poi.hssf.record.formula.EqualPtg;
import org.apache.poi.hssf.record.formula.ErrPtg;
import org.apache.poi.hssf.record.formula.FuncPtg;
import org.apache.poi.hssf.record.formula.FuncVarPtg;
import org.apache.poi.hssf.record.formula.GreaterEqualPtg;
import org.apache.poi.hssf.record.formula.GreaterThanPtg;
import org.apache.poi.hssf.record.formula.IntPtg;
import org.apache.poi.hssf.record.formula.LessEqualPtg;
import org.apache.poi.hssf.record.formula.LessThanPtg;
import org.apache.poi.hssf.record.formula.MissingArgPtg;
import org.apache.poi.hssf.record.formula.MultiplyPtg;
import org.apache.poi.hssf.record.formula.NamePtg;
import org.apache.poi.hssf.record.formula.NameXPtg;
import org.apache.poi.hssf.record.formula.NotEqualPtg;
import org.apache.poi.hssf.record.formula.NumberPtg;
import org.apache.poi.hssf.record.formula.ParenthesisPtg;
import org.apache.poi.hssf.record.formula.PercentPtg;
import org.apache.poi.hssf.record.formula.PowerPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.RangePtg;
import org.apache.poi.hssf.record.formula.Ref3DPtg;
import org.apache.poi.hssf.record.formula.RefPtg;
import org.apache.poi.hssf.record.formula.StringPtg;
import org.apache.poi.hssf.record.formula.SubtractPtg;
import org.apache.poi.hssf.record.formula.UnaryMinusPtg;
import org.apache.poi.hssf.record.formula.UnaryPlusPtg;
import org.apache.poi.hssf.record.formula.function.FunctionMetadata;
import org.apache.poi.hssf.record.formula.function.FunctionMetadataRegistry;
import org.apache.poi.hssf.usermodel.HSSFErrorConstants;
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
 *  @author Josh Micich
 */
public final class FormulaParser {
    private static final class Identifier {
        private final String _name;
        private final boolean _isQuoted;

        public Identifier(String name, boolean isQuoted) {
            _name = name;
            _isQuoted = isQuoted;
        }
        public String getName() {
            return _name;
        }
        public boolean isQuoted() {
            return _isQuoted;
        }
        public String toString() {
            StringBuffer sb = new StringBuffer(64);
            sb.append(getClass().getName());
            sb.append(" [");
            if (_isQuoted) {
                sb.append("'").append(_name).append("'");
            } else {
                sb.append(_name);
            }
            sb.append("]");
            return sb.toString();
        }
    }

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


    private final String formulaString;
    private final int formulaLength;
    private int pointer;

    private ParseNode _rootNode;

    private static char TAB = '\t';

    /**
     * Lookahead Character.
     * gets value '\0' when the input string is exhausted
     */
    private char look;

    private FormulaParsingWorkbook book;



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
    private FormulaParser(String formula, FormulaParsingWorkbook book){
        formulaString = formula;
        pointer=0;
        this.book = book;
        formulaLength = formulaString.length();
    }

    public static Ptg[] parse(String formula, FormulaParsingWorkbook book) {
        return parse(formula, book, FormulaType.CELL);
    }

    public static Ptg[] parse(String formula, FormulaParsingWorkbook workbook, int formulaType) {
        FormulaParser fp = new FormulaParser(formula, workbook);
        fp.parse();
        return fp.getRPNPtg(formulaType);
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
        String msg;

        if (look == '=' && formulaString.substring(0, pointer-1).trim().length() < 1) {
            msg = "The specified formula '" + formulaString
                + "' starts with an equals sign which is not allowed.";
        } else {
            msg = "Parse error near char " + (pointer-1) + " '" + look + "'"
                + " in specified formula '" + formulaString + "'. Expected "
                + s;
        }
        return new FormulaParseException(msg);
    }

    /** Recognize an Alpha Character */
    private static boolean IsAlpha(char c) {
        return Character.isLetter(c) || c == '$' || c=='_';
    }

    /** Recognize a Decimal Digit */
    private static boolean IsDigit(char c) {
        return Character.isDigit(c);
    }

    /** Recognize an Alphanumeric */
    private static boolean IsAlNum(char c) {
        return IsAlpha(c) || IsDigit(c);
    }

    /** Recognize White Space */
    private static boolean IsWhite( char c) {
        return  c ==' ' || c== TAB;
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
    private String parseUnquotedIdentifier() {
        Identifier iden = parseIdentifier();
        if (iden.isQuoted()) {
            throw expected("unquoted identifier");
        }
        return iden.getName();
    }
    /**
     * Parses a sheet name, named range name, or simple cell reference.<br/>
     * Note - identifiers in Excel can contain dots, so this method may return a String
     * which may need to be converted to an area reference.  For example, this method
     * may return a value like "A1..B2", in which case the caller must convert it to
     * an area reference like "A1:B2"
     */
    private Identifier parseIdentifier() {
        StringBuffer sb = new StringBuffer();
        if (!IsAlpha(look) && look != '\'' && look != '[') {
            throw expected("Name");
        }
        boolean isQuoted = look == '\''; 
        if(isQuoted) {
            Match('\'');
            boolean done = look == '\'';
            while(!done) {
                sb.append(look);
                GetChar();
                if(look == '\'')
                {
                    Match('\'');
                    done = look != '\'';
                }
            }
        } else {
            // allow for any sequence of dots and identifier chars
            // special case of two consecutive dots is best treated in the calling code
            while (IsAlNum(look) || look == '.' || look == '[' || look == ']') {
                sb.append(look);
                GetChar();
            }
        }
        return new Identifier(sb.toString(), isQuoted);
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
        Identifier iden = parseIdentifier();
        if (look == '('){
            //This is a function
            return function(iden.getName());
        }
        if (!iden.isQuoted()) {
            String name = iden.getName();
            if (name.equalsIgnoreCase("TRUE") || name.equalsIgnoreCase("FALSE")) {
                return  new ParseNode(new BoolPtg(name.toUpperCase()));
            }
        }
        return parseRangeExpression(iden);
    }

    private ParseNode parseRangeExpression(Identifier iden) {
        Ptg ptgA = parseNameOrCellRef(iden);
        if (look == ':') {
            GetChar();
            Identifier iden2 = parseIdentifier();
            Ptg ptgB = parseNameOrCellRef(iden2);
            Ptg simplified = reduceRangeExpression(ptgA, ptgB);
            
            if (simplified == null) {
                ParseNode[] children = {
                    new ParseNode(ptgA),    
                    new ParseNode(ptgB),
                };
                return new ParseNode(RangePtg.instance, children);
            }
            return new ParseNode(simplified);
        }
        return new ParseNode(ptgA);
    } 
    
    /**
     * 
     * "A1", "B3" -> "A1:B3"   
     * "sheet1!A1", "B3" -> "sheet1!A1:B3"
     * 
     * @return <code>null</code> if the range expression cannot / shouldn't be reduced.
     */
    private static Ptg reduceRangeExpression(Ptg ptgA, Ptg ptgB) {
        if (!(ptgB instanceof RefPtg)) {
            // only when second ref is simple 2-D ref can the range 
            // expression be converted to an area ref
            return null;
        }
        RefPtg refB = (RefPtg) ptgB;

        if (ptgA instanceof RefPtg) {
            RefPtg refA = (RefPtg) ptgA;
            return new AreaPtg(refA.getRow(), refB.getRow(), refA.getColumn(), refB.getColumn(),
                    refA.isRowRelative(), refB.isRowRelative(), refA.isColRelative(), refB.isColRelative());
        }
        if (ptgA instanceof Ref3DPtg) {
            Ref3DPtg refA = (Ref3DPtg) ptgA;
            return new Area3DPtg(refA.getRow(), refB.getRow(), refA.getColumn(), refB.getColumn(),
                    refA.isRowRelative(), refB.isRowRelative(), refA.isColRelative(), refB.isColRelative(),
                    refA.getExternSheetIndex());
        }
        // Note - other operand types (like AreaPtg) which probably can't evaluate 
        // do not cause validation errors at parse time
        return null;
    }

    private Ptg parseNameOrCellRef(Identifier iden) {
        
        if (look == '!') {
            GetChar();
            // 3-D ref
            // this code assumes iden is a sheetName
            // TODO - handle <book name> ! <named range name>
            int externIdx = getExternalSheetIndex(iden.getName());
            String secondIden = parseUnquotedIdentifier();
            AreaReference areaRef = parseArea(secondIden);
            if (areaRef == null) {
                return new Ref3DPtg(secondIden, externIdx);
            }
            // will happen if dots are used instead of colon
            return new Area3DPtg(areaRef.formatAsString(), externIdx);
        }

        String name = iden.getName();
        AreaReference areaRef = parseArea(name);
        if (areaRef != null) {
            // will happen if dots are used instead of colon
            return new AreaPtg(areaRef.formatAsString());
        }
        // This can be either a cell ref or a named range


        int nameType = CellReference.classifyCellReference(name);
        if (nameType == NameType.CELL) {
            return new RefPtg(name);
        }
        if (look == ':') {
            if (nameType == NameType.COLUMN) {
                GetChar();
                String secondIden = parseUnquotedIdentifier();
                if (CellReference.classifyCellReference(secondIden) != NameType.COLUMN) {
                    throw new FormulaParseException("Expected full column after '" + name 
                            + ":' but got '" + secondIden + "'");
                }
                return new AreaPtg(name + ":" + secondIden);
            }
        }
        if (nameType != NameType.NAMED_RANGE) {
            new FormulaParseException("Name '" + name
                + "' does not look like a cell reference or named range");
        }
        EvaluationName evalName = book.getName(name);
        if (evalName == null) {
            throw new FormulaParseException("Specified named range '"
                    + name + "' does not exist in the current workbook.");
        }
        if (evalName.isRange()) {
            return evalName.createPtg();
        }
        throw new FormulaParseException("Specified name '"
                    + name + "' is not a range as expected");
    }

    private int getExternalSheetIndex(String name) {
        if (name.charAt(0) == '[') {
            // we have a sheet name qualified with workbook name e.g. '[MyData.xls]Sheet1'
            int pos = name.lastIndexOf(']'); // safe because sheet names never have ']'
            String wbName = name.substring(1, pos);
            String sheetName = name.substring(pos+1);
            return book.getExternalSheetIndex(wbName, sheetName);
        }
        return book.getExternalSheetIndex(name);
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
        Ptg nameToken = null;
        if(!AbstractFunctionPtg.isBuiltInFunctionName(name)) {
            // user defined function
            // in the token tree, the name is more or less the first argument

            EvaluationName hName = book.getName(name);
            if (hName == null) {

                nameToken = book.getNameXPtg(name);
                if (nameToken == null) {
                    throw new FormulaParseException("Name '" + name
                            + "' is completely unknown in the current workbook");
                }
            } else {
                if (!hName.isFunctionName()) {
                    throw new FormulaParseException("Attempt to use name '" + name
                            + "' as a function, but defined name in workbook does not refer to a function");
                }

                // calls to user-defined functions within the workbook
                // get a Name token which points to a defined name record
                nameToken = hName.createPtg();
            }
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
     * @param name a {@link NamePtg} or {@link NameXPtg} or <code>null</code>
     * @param numArgs
     * @return Ptg a null is returned if we're in an IF formula, it needs extreme manipulation and is handled in this function
     */
    private ParseNode getFunction(String name, Ptg namePtg, ParseNode[] args) {

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
                return new ParseNode(ErrPtg.valueOf(parseErrorLiteral()));
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
                return new ParseNode(new StringPtg(parseStringLiteral()));
            case '{':
                Match('{');
                ParseNode arrayNode = parseArray();
                Match('}');
                return arrayNode;
        }
        if (IsAlpha(look) || look == '\'' || look == '['){
            return parseFunctionReferenceOrName();
        }
        // else - assume number
        return new ParseNode(parseNumber());
    }


    private ParseNode parseArray() {
        List rowsData = new ArrayList();
        while(true) {
            Object[] singleRowData = parseArrayRow();
            rowsData.add(singleRowData);
            if (look == '}') {
                break;
            }
            if (look != ';') {
                throw expected("'}' or ';'");
            }
            Match(';');
        }
        int nRows = rowsData.size();
        Object[][] values2d = new Object[nRows][];
        rowsData.toArray(values2d);
        int nColumns = values2d[0].length;
        checkRowLengths(values2d, nColumns);

        return new ParseNode(new ArrayPtg(values2d));
    }
    private void checkRowLengths(Object[][] values2d, int nColumns) {
        for (int i = 0; i < values2d.length; i++) {
            int rowLen = values2d[i].length;
            if (rowLen != nColumns) {
                throw new FormulaParseException("Array row " + i + " has length " + rowLen
                        + " but row 0 has length " + nColumns);
            }
        }
    }

    private Object[] parseArrayRow() {
        List temp = new ArrayList();
        while (true) {
            temp.add(parseArrayItem());
            SkipWhite();
            switch(look) {
                case '}':
                case ';':
                    break;
                case ',':
                    Match(',');
                    continue;
                default:
                    throw expected("'}' or ','");

            }
            break;
        }

        Object[] result = new Object[temp.size()];
        temp.toArray(result);
        return result;
    }

    private Object parseArrayItem() {
        SkipWhite();
        switch(look) {
            case '"': return new UnicodeString(parseStringLiteral());
            case '#': return ErrorConstant.valueOf(parseErrorLiteral());
            case 'F': case 'f':
            case 'T': case 't':
                return parseBooleanLiteral();
        }
        // else assume number
        return convertArrayNumber(parseNumber());
    }

    private Boolean parseBooleanLiteral() {
        String iden = parseUnquotedIdentifier();
        if ("TRUE".equalsIgnoreCase(iden)) {
            return Boolean.TRUE;
        }
        if ("FALSE".equalsIgnoreCase(iden)) {
            return Boolean.FALSE;
        }
        throw expected("'TRUE' or 'FALSE'");
    }

    private static Double convertArrayNumber(Ptg ptg) {
        if (ptg instanceof IntPtg) {
            return new Double(((IntPtg)ptg).getValue());
        }
        if (ptg instanceof NumberPtg) {
            return new Double(((NumberPtg)ptg).getValue());
        }
        throw new RuntimeException("Unexpected ptg (" + ptg.getClass().getName() + ")");
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


    private int parseErrorLiteral() {
        Match('#');
        String part1 = parseUnquotedIdentifier().toUpperCase();

        switch(part1.charAt(0)) {
            case 'V':
                if(part1.equals("VALUE")) {
                    Match('!');
                    return HSSFErrorConstants.ERROR_VALUE;
                }
                throw expected("#VALUE!");
            case 'R':
                if(part1.equals("REF")) {
                    Match('!');
                    return HSSFErrorConstants.ERROR_REF;
                }
                throw expected("#REF!");
            case 'D':
                if(part1.equals("DIV")) {
                    Match('/');
                    Match('0');
                    Match('!');
                    return HSSFErrorConstants.ERROR_DIV_0;
                }
                throw expected("#DIV/0!");
            case 'N':
                if(part1.equals("NAME")) {
                    Match('?');  // only one that ends in '?'
                    return HSSFErrorConstants.ERROR_NAME;
                }
                if(part1.equals("NUM")) {
                    Match('!');
                    return HSSFErrorConstants.ERROR_NUM;
                }
                if(part1.equals("NULL")) {
                    Match('!');
                    return HSSFErrorConstants.ERROR_NULL;
                }
                if(part1.equals("N")) {
                    Match('/');
                    if(look != 'A' && look != 'a') {
                        throw expected("#N/A");
                    }
                    Match(look);
                    // Note - no '!' or '?' suffix
                    return HSSFErrorConstants.ERROR_NA;
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


    private String parseStringLiteral() {
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
        return token.toString();
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
     * 
     */
    private void parse() {
        pointer=0;
        GetChar();
        _rootNode = comparisonExpression();

        if(pointer <= formulaLength) {
            String msg = "Unused input [" + formulaString.substring(pointer-1)
                + "] after attempting to parse the formula [" + formulaString + "]";
            throw new FormulaParseException(msg);
        }
    }

    private Ptg[] getRPNPtg(int formulaType) {
        OperandClassTransformer oct = new OperandClassTransformer(formulaType);
        // RVA is for 'operand class': 'reference', 'value', 'array'
        oct.transformFormula(_rootNode);
        return ParseNode.toTokenArray(_rootNode);
    }
}
