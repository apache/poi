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
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.constant.ErrorConstant;
import org.apache.poi.ss.formula.function.FunctionMetadata;
import org.apache.poi.ss.formula.function.FunctionMetadataRegistry;
import org.apache.poi.ss.formula.ptg.AbstractFunctionPtg;
import org.apache.poi.ss.formula.ptg.AddPtg;
import org.apache.poi.ss.formula.ptg.Area3DPxg;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.ArrayPtg;
import org.apache.poi.ss.formula.ptg.AttrPtg;
import org.apache.poi.ss.formula.ptg.BoolPtg;
import org.apache.poi.ss.formula.ptg.ConcatPtg;
import org.apache.poi.ss.formula.ptg.DividePtg;
import org.apache.poi.ss.formula.ptg.EqualPtg;
import org.apache.poi.ss.formula.ptg.ErrPtg;
import org.apache.poi.ss.formula.ptg.FuncPtg;
import org.apache.poi.ss.formula.ptg.FuncVarPtg;
import org.apache.poi.ss.formula.ptg.GreaterEqualPtg;
import org.apache.poi.ss.formula.ptg.GreaterThanPtg;
import org.apache.poi.ss.formula.ptg.IntPtg;
import org.apache.poi.ss.formula.ptg.IntersectionPtg;
import org.apache.poi.ss.formula.ptg.LessEqualPtg;
import org.apache.poi.ss.formula.ptg.LessThanPtg;
import org.apache.poi.ss.formula.ptg.MemAreaPtg;
import org.apache.poi.ss.formula.ptg.MemFuncPtg;
import org.apache.poi.ss.formula.ptg.MissingArgPtg;
import org.apache.poi.ss.formula.ptg.MultiplyPtg;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.NameXPtg;
import org.apache.poi.ss.formula.ptg.NameXPxg;
import org.apache.poi.ss.formula.ptg.NotEqualPtg;
import org.apache.poi.ss.formula.ptg.NumberPtg;
import org.apache.poi.ss.formula.ptg.OperandPtg;
import org.apache.poi.ss.formula.ptg.OperationPtg;
import org.apache.poi.ss.formula.ptg.ParenthesisPtg;
import org.apache.poi.ss.formula.ptg.PercentPtg;
import org.apache.poi.ss.formula.ptg.PowerPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RangePtg;
import org.apache.poi.ss.formula.ptg.RefPtg;
import org.apache.poi.ss.formula.ptg.StringPtg;
import org.apache.poi.ss.formula.ptg.SubtractPtg;
import org.apache.poi.ss.formula.ptg.UnaryMinusPtg;
import org.apache.poi.ss.formula.ptg.UnaryPlusPtg;
import org.apache.poi.ss.formula.ptg.UnionPtg;
import org.apache.poi.ss.formula.ptg.ValueOperatorPtg;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Table;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellReference.NameType;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * This class parses a formula string into a List of tokens in RPN order.
 * Inspired by
 *           Lets Build a Compiler, by Jack Crenshaw
 * BNF for the formula expression is :
 * <expression> ::= <term> [<addop> <term>]*
 * <term> ::= <factor>  [ <mulop> <factor> ]*
 * <factor> ::= <number> | (<expression>) | <cellRef> | <function>
 * <function> ::= <functionName> ([expression [, expression]*])
 * <p>
 * For POI internal use only
 * <p>
 */
@Internal
public final class FormulaParser {
    private final static POILogger log = POILogFactory.getLogger(FormulaParser.class);
    private final String _formulaString;
    private final int _formulaLength;
    /** points at the next character to be read (after the {@link #look} codepoint) */
    private int _pointer;

    private ParseNode _rootNode;

    private final static char TAB = '\t'; // HSSF + XSSF
    private final static char CR = '\r';  // Normally just XSSF
    private final static char LF = '\n';  // Normally just XSSF

    /**
     * Lookahead unicode codepoint
     * gets value '\0' when the input string is exhausted
     */
    private int look;

    /**
     * Tracks whether the run of whitespace preceding "look" could be an
     * intersection operator.  See GetChar.
     */
    private boolean _inIntersection;

    private final FormulaParsingWorkbook _book;
    private final SpreadsheetVersion _ssVersion;

    private final int _sheetIndex;
    private final int _rowIndex; // 0-based


    /**
     * Create the formula parser, with the string that is to be
     *  parsed against the supplied workbook.
     * A later call the parse() method to return ptg list in
     *  rpn order, then call the getRPNPtg() to retrieve the
     *  parse results.
     * This class is recommended only for single threaded use.
     *
     * If you have a {@link org.apache.poi.hssf.usermodel.HSSFWorkbook}, and not a
     *  {@link org.apache.poi.ss.usermodel.Workbook}, then use the convenience method on
     *  {@link org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator}
     */
    private FormulaParser(String formula, FormulaParsingWorkbook book, int sheetIndex, int rowIndex) {
        _formulaString = formula;
        _pointer=0;
        _book = book;
        _ssVersion = book == null ? SpreadsheetVersion.EXCEL97 : book.getSpreadsheetVersion();
        _formulaLength = _formulaString.length();
        _sheetIndex = sheetIndex;
        _rowIndex = rowIndex;
    }

    /**
     * Parse a formula into an array of tokens
     * Side effect: creates name ({@link org.apache.poi.ss.usermodel.Workbook#createName})
     *     if formula contains unrecognized names (names are likely UDFs)
     *
     * @param formula     the formula to parse
     * @param workbook    the parent workbook
     * @param formulaType the type of the formula
     * @param sheetIndex  the 0-based index of the sheet this formula belongs to.
     *     The sheet index is required to resolve sheet-level names. <code>-1</code> means that
     *     the scope of the name will be ignored and  the parser will match names only by name
     * @param rowIndex  - the related cell's row index in 0-based form (-1 if the formula is not cell related)
     *                     used to handle structured references that have the "#This Row" quantifier.
     *                    Use rowIndex=-1 or {@link #parseStructuredReference(String, FormulaParsingWorkbook, int)} if formula
     *                    does not contain structured references.
     *
     * @return array of parsed tokens
     * @throws FormulaParseException if the formula has incorrect syntax or is otherwise invalid
     */
    public static Ptg[] parse(String formula, FormulaParsingWorkbook workbook, FormulaType formulaType, int sheetIndex, int rowIndex) {
        FormulaParser fp = new FormulaParser(formula, workbook, sheetIndex, rowIndex);
        fp.parse();
        return fp.getRPNPtg(formulaType);
    }

    /**
     * Parse a formula into an array of tokens
     * Side effect: creates name ({@link org.apache.poi.ss.usermodel.Workbook#createName})
     *     if formula contains unrecognized names (names are likely UDFs)
     *
     * @param formula     the formula to parse
     * @param workbook    the parent workbook
     * @param formulaType the type of the formula
     * @param sheetIndex  the 0-based index of the sheet this formula belongs to.
     *     The sheet index is required to resolve sheet-level names. <code>-1</code> means that
     *     the scope of the name will be ignored and  the parser will match names only by name
     *
     * @return array of parsed tokens
     * @throws FormulaParseException if the formula has incorrect syntax or is otherwise invalid
     */
    public static Ptg[] parse(String formula, FormulaParsingWorkbook workbook, FormulaType formulaType, int sheetIndex) {
        return parse(formula, workbook, formulaType, sheetIndex, -1);
    }

    /**
     * Parse a structured reference. Converts the structured
     *  reference to the area that represent it.
     *
     * @param tableText - The structured reference text
     * @param workbook - the parent workbook
     * @param rowIndex - the 0-based cell's row index ( used to handle "#This Row" quantifiers )
     * @return the area that being represented by the structured reference.
     */
    public static Area3DPxg parseStructuredReference(String tableText, FormulaParsingWorkbook workbook, int rowIndex) {
        final int sheetIndex = -1; //don't care?
        Ptg[] arr = FormulaParser.parse(tableText, workbook, FormulaType.CELL, sheetIndex, rowIndex);
        if (arr.length != 1 || !(arr[0] instanceof Area3DPxg) ) {
            throw new IllegalStateException("Illegal structured reference, had length: " + arr.length);
        }
        return (Area3DPxg) arr[0];
    }
    
    /** Read New Character From Input Stream */
    private void GetChar() {
        // The intersection operator is a space.  We track whether the run of 
        // whitespace preceding "look" counts as an intersection operator.
        if (IsWhite(look)) {
            if (look == ' ') {
                _inIntersection = true;
            }
        }
        else {
            _inIntersection = false;
        }
        
        // Check to see if we've walked off the end of the string.
        if (_pointer > _formulaLength) {
            throw new RuntimeException("Parsed past the end of the formula, pos: " + _pointer +
                    ", length: " + _formulaLength + ", formula: " + _formulaString);
        }
        if (_pointer < _formulaLength) {
            look=_formulaString.codePointAt(_pointer);
        } else {
            // Just return if so and reset 'look' to something to keep
            // SkipWhitespace from spinning
            look = (char)0;
            _inIntersection = false;
        }
        _pointer += Character.charCount(look);
    }
    private void resetPointer(int ptr) {
        _pointer = ptr;
        if (_pointer <= _formulaLength) {
            look=_formulaString.codePointAt(_pointer - Character.charCount(look));
        } else {
            // Just return if so and reset 'look' to something to keep
            // SkipWhitespace from spinning
            look = (char)0;
        }
    }

    /** Report What Was Expected */
    private RuntimeException expected(String s) {
        String msg;

        if (look == '=' && _formulaString.substring(0, _pointer-1).trim().length() < 1) {
            msg = "The specified formula '" + _formulaString
                + "' starts with an equals sign which is not allowed.";
        } else {
            msg = new StringBuilder("Parse error near char ")
                .append(_pointer-1) //this is the codepoint index, not char index, which may be larger if there are multi-byte chars
                .append(" '")
                .appendCodePoint(look)
                .append("'")
                .append(" in specified formula '")
                .append(_formulaString)
                .append("'. Expected ")
                .append(s)
                .toString();
        }
        return new FormulaParseException(msg);
    }

    /** Recognize an Alpha Character */
    private static boolean IsAlpha(int c) {
        return Character.isLetter(c) || c == '$' || c=='_';
    }

    /** Recognize a Decimal Digit */
    private static boolean IsDigit(int c) {
        return Character.isDigit(c);
    }

    /** Recognize White Space */
    private static boolean IsWhite(int c) {
        return  c ==' ' || c== TAB || c == CR || c == LF;
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
    private void Match(int x) {
        if (look != x) {
            throw expected(new StringBuilder()
                    .append("'")
                    .appendCodePoint(x)
                    .append("'")
                    .toString());
        }
        GetChar();
    }

    /** Get a Number */
    private String GetNum() {
        StringBuilder value = new StringBuilder();

        while (IsDigit(this.look)){
            value.appendCodePoint(this.look);
            GetChar();
        }
        return value.length() == 0 ? null : value.toString();
    }

    private ParseNode parseRangeExpression() {
        ParseNode result = parseRangeable();
        boolean hasRange = false;
        while (look == ':') {
            int pos = _pointer;
            GetChar();
            ParseNode nextPart = parseRangeable();
            // Note - no range simplification here. An expr like "A1:B2:C3:D4:E5" should be
            // grouped into area ref pairs like: "(A1:B2):(C3:D4):E5"
            // Furthermore, Excel doesn't seem to simplify
            // expressions like "Sheet1!A1:Sheet1:B2" into "Sheet1!A1:B2"

            checkValidRangeOperand("LHS", pos, result);
            checkValidRangeOperand("RHS", pos, nextPart);

            ParseNode[] children = { result, nextPart, };
            result = new ParseNode(RangePtg.instance, children);
            hasRange = true;
        }
        if (hasRange) {
            return augmentWithMemPtg(result);
        }
        return result;
    }

    private static ParseNode augmentWithMemPtg(ParseNode root) {
        Ptg memPtg;
        if (needsMemFunc(root)) {
            memPtg = new MemFuncPtg(root.getEncodedSize());
        } else {
            memPtg = new MemAreaPtg(root.getEncodedSize());
        }
        return new ParseNode(memPtg, root);
    }
    /**
     * From OOO doc: "Whenever one operand of the reference subexpression is a function,
     *  a defined name, a 3D reference, or an external reference (and no error occurs),
     *  a tMemFunc token is used"
     *
     */
    private static boolean needsMemFunc(ParseNode root) {
        Ptg token = root.getToken();
        if (token instanceof AbstractFunctionPtg) {
            return true;
        }
        if (token instanceof ExternSheetReferenceToken) { // 3D refs
            return true;
        }
        if (token instanceof NamePtg || token instanceof NameXPtg) { // 3D refs
            return true;
        }

        if (token instanceof OperationPtg || token instanceof ParenthesisPtg) {
            // expect RangePtg, but perhaps also UnionPtg, IntersectionPtg etc
            for(ParseNode child : root.getChildren()) {
                if (needsMemFunc(child)) {
                    return true;
                }
            }
            return false;
        }
        if (token instanceof OperandPtg) {
            return false;
        }

        return false;
    }

    /**
     * @param currentParsePosition used to format a potential error message
     */
    private static void checkValidRangeOperand(String sideName, int currentParsePosition, ParseNode pn) {
        if (!isValidRangeOperand(pn)) {
            throw new FormulaParseException("The " + sideName
                    + " of the range operator ':' at position "
                    + currentParsePosition + " is not a proper reference.");
        }
    }

    /**
     * @return <code>false</code> if sub-expression represented the specified ParseNode definitely
     * cannot appear on either side of the range (':') operator
     */
    private static boolean isValidRangeOperand(ParseNode a) {
        Ptg tkn = a.getToken();
        // Note - order is important for these instance-of checks
        if (tkn instanceof OperandPtg) {
            // notably cell refs and area refs
            return true;
        }

        // next 2 are special cases of OperationPtg
        if (tkn instanceof AbstractFunctionPtg) {
            AbstractFunctionPtg afp = (AbstractFunctionPtg) tkn;
            byte returnClass = afp.getDefaultOperandClass();
            return Ptg.CLASS_REF == returnClass;
        }
        if (tkn instanceof ValueOperatorPtg) {
            return false;
        }
        if (tkn instanceof OperationPtg) {
            return true;
        }

        // one special case of ControlPtg
        if (tkn instanceof ParenthesisPtg) {
            // parenthesis Ptg should have only one child
            return isValidRangeOperand(a.getChildren()[0]);
        }

        // one special case of ScalarConstantPtg
        if (tkn == ErrPtg.REF_INVALID) {
            return true;
        }

        // All other ControlPtgs and ScalarConstantPtgs cannot be used with ':'
        return false;
    }

    /**
     * Parses area refs (things which could be the operand of ':') and simple factors
     * Examples
     * <pre>
     *   A$1
     *   $A$1 :  $B1
     *   A1 .......    C2
     *   Sheet1 !$A1
     *   a..b!A1
     *   'my sheet'!A1
     *   .my.sheet!A1
     *   'my sheet':'my alt sheet'!A1
     *   .my.sheet1:.my.sheet2!$B$2
     *   my.named..range.
     *   'my sheet'!my.named.range
     *   .my.sheet!my.named.range
     *   foo.bar(123.456, "abc")
     *   123.456
     *   "abc"
     *   true
     *   [Foo.xls]!$A$1
     *   [Foo.xls]'my sheet'!$A$1
     *   [Foo.xls]!my.named.range
     * </pre>
     *
     */
    private ParseNode parseRangeable() {
        SkipWhite();
        int savePointer = _pointer;
        SheetIdentifier sheetIden = parseSheetName();
        
        if (sheetIden == null) {
            resetPointer(savePointer);
        } else {
            SkipWhite();
            savePointer = _pointer;
        }

        SimpleRangePart part1 = parseSimpleRangePart();
        if (part1 == null) {
            if (sheetIden != null) {
                if(look == '#'){  // error ref like MySheet!#REF!
                    return new ParseNode(ErrPtg.valueOf(parseErrorLiteral()));  
                } else {
                    // Is it a named range?
                    String name = parseAsName();
                    if (name.length() == 0) {
                        throw new FormulaParseException("Cell reference or Named Range "
                                + "expected after sheet name at index " + _pointer + ".");
                    }
                    Ptg nameXPtg = _book.getNameXPtg(name, sheetIden);
                    if (nameXPtg == null) {
                        throw new FormulaParseException("Specified name '" + name +
                                "' for sheet " + sheetIden.asFormulaString() + " not found");
                    }
                    return new ParseNode(nameXPtg);
                }
            }
            return parseNonRange(savePointer);
        }
        boolean whiteAfterPart1 = IsWhite(look);
        if (whiteAfterPart1) {
            SkipWhite();
        }

        if (look == ':') {
            int colonPos = _pointer;
            GetChar();
            SkipWhite();
            SimpleRangePart part2 = parseSimpleRangePart();
            if (part2 != null && !part1.isCompatibleForArea(part2)) {
                // second part is not compatible with an area ref e.g. S!A1:S!B2
                // where S might be a sheet name (that looks like a column name)

                part2 = null;
            }
            if (part2 == null) {
                // second part is not compatible with an area ref e.g. A1:OFFSET(B2, 1, 2)
                // reset and let caller use explicit range operator
                resetPointer(colonPos);
                if (!part1.isCell()) {
                    String prefix = "";
                    if (sheetIden != null) {
                        prefix = "'" + sheetIden.getSheetIdentifier().getName() + '!';
                    }
                    throw new FormulaParseException(prefix + part1.getRep() + "' is not a proper reference.");
                }
            }
            return createAreaRefParseNode(sheetIden, part1, part2);
        }

        if (look == '.') {
            GetChar();
            int dotCount = 1;
            while (look =='.') {
                dotCount ++;
                GetChar();
            }
            boolean whiteBeforePart2 = IsWhite(look);

            SkipWhite();
            SimpleRangePart part2 = parseSimpleRangePart();
            String part1And2 = _formulaString.substring(savePointer-1, _pointer-1);
            if (part2 == null) {
                if (sheetIden != null) {
                    throw new FormulaParseException("Complete area reference expected after sheet name at index "
                            + _pointer + ".");
                }
                return parseNonRange(savePointer);
            }


            if (whiteAfterPart1 || whiteBeforePart2) {
                if (part1.isRowOrColumn() || part2.isRowOrColumn()) {
                    // "A .. B" not valid syntax for "A:B"
                    // and there's no other valid expression that fits this grammar
                    throw new FormulaParseException("Dotted range (full row or column) expression '"
                            + part1And2 + "' must not contain whitespace.");
                }
                return createAreaRefParseNode(sheetIden, part1, part2);
            }

            if (dotCount == 1 && part1.isRow() && part2.isRow()) {
                // actually, this is looking more like a number
                return parseNonRange(savePointer);
            }

            if (part1.isRowOrColumn() || part2.isRowOrColumn()) {
                if (dotCount != 2) {
                    throw new FormulaParseException("Dotted range (full row or column) expression '" + part1And2
                            + "' must have exactly 2 dots.");
                }
            }
            return createAreaRefParseNode(sheetIden, part1, part2);
        }
        if (part1.isCell() && isValidCellReference(part1.getRep())) {
            return createAreaRefParseNode(sheetIden, part1, null);
        }
        if (sheetIden != null) {
            throw new FormulaParseException("Second part of cell reference expected after sheet name at index "
                    + _pointer + ".");
        }

        return parseNonRange(savePointer);
    }


    
    private final static String specHeaders = "Headers";
    private final static String specAll = "All";
    private final static String specData = "Data";
    private final static String specTotals = "Totals";
    private final static String specThisRow = "This Row";
    
    /**
     * Parses a structured reference, returns it as area reference.
     * Examples:
     * <pre>
     * Table1[col]
     * Table1[[#Totals],[col]]
     * Table1[#Totals]
     * Table1[#All]
     * Table1[#Data]
     * Table1[#Headers]
     * Table1[#Totals]
     * Table1[#This Row]
     * Table1[[#All],[col]]
     * Table1[[#Headers],[col]]
     * Table1[[#Totals],[col]]
     * Table1[[#All],[col1]:[col2]]
     * Table1[[#Data],[col1]:[col2]]
     * Table1[[#Headers],[col1]:[col2]]
     * Table1[[#Totals],[col1]:[col2]]
     * Table1[[#Headers],[#Data],[col2]]
     * Table1[[#This Row], [col1]]
     * Table1[ [col1]:[col2] ]
     * </pre>
     * @param tableName
     * @return Area Reference for the given table
     */
    private ParseNode parseStructuredReference(String tableName) {
        
        if ( ! (_ssVersion.equals(SpreadsheetVersion.EXCEL2007)) ) {
            throw new FormulaParseException("Structured references work only on XSSF (Excel 2007+)!");
        }
        Table tbl = _book.getTable(tableName);
        if (tbl == null) {
           throw new FormulaParseException("Illegal table name: '" + tableName + "'");
        }
        String sheetName = tbl.getSheetName();
        
        int startCol = tbl.getStartColIndex();
        int endCol = tbl.getEndColIndex();
        int startRow = tbl.getStartRowIndex();
        int endRow = tbl.getEndRowIndex();
        
        // Do NOT return before done reading all the structured reference tokens from the input stream.
        // Throwing exceptions is okay.
        int savePtr0 = _pointer;
        GetChar();
        
        boolean isTotalsSpec = false;
        boolean isThisRowSpec = false;
        boolean isDataSpec = false;
        boolean isHeadersSpec = false;
        boolean isAllSpec = false;
        int nSpecQuantifiers = 0; // The number of special quantifiers
        while (true) {
            int savePtr1 = _pointer;
            String specName = parseAsSpecialQuantifier();
            if (specName == null) {
                resetPointer(savePtr1);
                break;
            }
            switch (specName) {
                case specAll:
                    isAllSpec = true;
                    break;
                case specData:
                    isDataSpec = true;
                    break;
                case specHeaders:
                    isHeadersSpec = true;
                    break;
                case specThisRow:
                    isThisRowSpec = true;
                    break;
                case specTotals:
                    isTotalsSpec = true;
                    break;
                default:
                    throw new FormulaParseException("Unknown special quantifier " + specName);
            }
            nSpecQuantifiers++;
            if (look == ','){
                GetChar();
            } else {
                break;
            }
        }
        boolean isThisRow = false;
        SkipWhite();
        if (look == '@') {
            isThisRow = true;
            GetChar();
        }
        // parse column quantifier
        String startColumnName;
        String endColumnName = null;
        int nColQuantifiers = 0;
        int savePtr1 = _pointer;
        startColumnName = parseAsColumnQuantifier();
        if (startColumnName == null) {
            resetPointer(savePtr1);
        } else {
            nColQuantifiers++;
            if (look == ','){
                throw new FormulaParseException("The formula "+ _formulaString + " is illegal: you should not use ',' with column quantifiers");
            } else if (look == ':') {
                GetChar();
                endColumnName = parseAsColumnQuantifier();
                nColQuantifiers++;
                if (endColumnName == null) {
                    throw new FormulaParseException("The formula "+ _formulaString + " is illegal: the string after ':' must be column quantifier");
                }
            }
        }
        
        if(nColQuantifiers == 0 && nSpecQuantifiers == 0){
            resetPointer(savePtr0);
            savePtr0 = _pointer;
            startColumnName = parseAsColumnQuantifier();
            if (startColumnName != null) {
                nColQuantifiers++;
            } else {
                resetPointer(savePtr0);
                String name = parseAsSpecialQuantifier();
                if (name!=null) {
                    switch (name) {
                        case specAll:
                            isAllSpec = true;
                            break;
                        case specData:
                            isDataSpec = true;
                            break;
                        case specHeaders:
                            isHeadersSpec = true;
                            break;
                        case specThisRow:
                            isThisRowSpec = true;
                            break;
                        case specTotals:
                            isTotalsSpec = true;
                            break;
                        default:
                            throw new FormulaParseException("Unknown special quantifier " + name);
                    }
                    nSpecQuantifiers++;
                } else {
                    throw new FormulaParseException("The formula "+ _formulaString + " is illegal");
                }
            }
        } else {
            Match(']');
        }
        // Done reading from input stream
        // Ok to return now

        if (isTotalsSpec && tbl.getTotalsRowCount() == 0) {
            return new ParseNode(ErrPtg.REF_INVALID);
        }
        if ((isThisRow || isThisRowSpec) && (_rowIndex < startRow || endRow < _rowIndex)) {
            // structured reference is trying to reference a row above or below the table with [#This Row] or [@]
            if (_rowIndex >= 0) {
                return new ParseNode(ErrPtg.VALUE_INVALID);
            } else {
                throw new FormulaParseException(
                        "Formula contained [#This Row] or [@] structured reference but this row < 0. " + 
                        "Row index must be specified for row-referencing structured references.");
            }
        }
        
        int actualStartRow = startRow;
        int actualEndRow = endRow;
        int actualStartCol = startCol;
        int actualEndCol = endCol; 
        if (nSpecQuantifiers > 0) {
        //Selecting rows
            if (nSpecQuantifiers == 1 && isAllSpec) {
                //do nothing
            } else if (isDataSpec && isHeadersSpec) {
                if (tbl.getTotalsRowCount() > 0) {
                    actualEndRow = endRow - 1;
                }
            } else if (isDataSpec && isTotalsSpec) {
                actualStartRow = startRow + 1;
            } else if (nSpecQuantifiers == 1 && isDataSpec) {
                actualStartRow = startRow + 1;
                if (tbl.getTotalsRowCount() > 0) {
                    actualEndRow = endRow - 1;
                }
            } else if (nSpecQuantifiers == 1 && isHeadersSpec) {
                actualEndRow = actualStartRow;
            } else if (nSpecQuantifiers == 1 && isTotalsSpec) {
                actualStartRow = actualEndRow;
            } else if ((nSpecQuantifiers == 1 && isThisRowSpec) || isThisRow) {
                actualStartRow = _rowIndex; //The rowNum is 0 based
                actualEndRow = _rowIndex; 
            } else {
                throw new FormulaParseException("The formula "+ _formulaString + " is illegal");
            }
        } else {
            if (isThisRow) { // there is a @
                actualStartRow = _rowIndex; //The rowNum is 0 based
                actualEndRow = _rowIndex; 
            } else { // Really no special quantifiers
                actualStartRow++;
                if (tbl.getTotalsRowCount() > 0) actualEndRow--;
            }
        }

        //Selecting cols

        if (nColQuantifiers == 2) {
            if (startColumnName == null || endColumnName == null) {
                throw new IllegalStateException("Fatal error");
            }
            int startIdx = tbl.findColumnIndex(startColumnName);
            int endIdx = tbl.findColumnIndex(endColumnName);
            if (startIdx == -1 || endIdx == -1) {
                throw new FormulaParseException("One of the columns "+ startColumnName +", "+ endColumnName +" doesn't exist in table "+ tbl.getName()); 
            } 
            actualStartCol = startCol+ startIdx;
            actualEndCol = startCol + endIdx;
                
        } else if (nColQuantifiers == 1 && !isThisRow) {
            if (startColumnName == null) {
                throw new IllegalStateException("Fatal error");
            }
            int idx = tbl.findColumnIndex(startColumnName);
            if (idx == -1) {
                throw new FormulaParseException("The column "+ startColumnName + " doesn't exist in table "+ tbl.getName());
            }
            actualStartCol = startCol + idx;
            actualEndCol = actualStartCol;
        }
        CellReference topLeft = new CellReference(actualStartRow, actualStartCol);
        CellReference bottomRight = new CellReference(actualEndRow, actualEndCol);
        SheetIdentifier sheetIden = new SheetIdentifier( null, new NameIdentifier(sheetName, true));
        Ptg ptg = _book.get3DReferencePtg(new AreaReference(topLeft, bottomRight, _ssVersion), sheetIden);
        return new ParseNode(ptg);
    }
    
    /**
     * Tries to parse the next as column - can contain whitespace
     * Caller should save pointer.
    */
    private String parseAsColumnQuantifier() {
        if ( look != '[') {
            return null;
        }
        GetChar();
        if (look == '#') {
            return null;
        }
        if (look == '@') {
            GetChar();
        }
        StringBuilder name = new StringBuilder();
        while (look!=']') {
           name.appendCodePoint(look);
           GetChar();
        }
        Match(']');
        return name.toString();
    }    
    /**
     * Tries to parse the next as special quantifier
     * Caller should save pointer.
     */
    private String parseAsSpecialQuantifier(){
        if ( look != '[') {
            return null;
        }
        GetChar();
        if( look != '#') {
            return null;
        }
        GetChar();
        String name = parseAsName();
        if ( name.equals("This")) {
            name = name + ' ' + parseAsName();
        }
        Match(']');
        return name;
    }


    /**
     * Parses simple factors that are not primitive ranges or range components
     * i.e. '!', ':'(and equiv '...') do not appear
     * Examples
     * <pre>
     *   my.named...range.
     *   foo.bar(123.456, "abc")
     *   123.456
     *   "abc"
     *   true
     * </pre>
     */
    private ParseNode parseNonRange(int savePointer) {
        resetPointer(savePointer);

        if (Character.isDigit(look)) {
            return new ParseNode(parseNumber());
        }
        if (look == '"') {
            return new ParseNode(new StringPtg(parseStringLiteral()));
        }
        
        // from now on we can only be dealing with non-quoted identifiers
        // which will either be named ranges or functions
        String name = parseAsName();

        if (look == '(') {
            return function(name);
        }
        if(look == '['){
            return parseStructuredReference(name);
        }
        if (name.equalsIgnoreCase("TRUE") || name.equalsIgnoreCase("FALSE")) {
            return  new ParseNode(BoolPtg.valueOf(name.equalsIgnoreCase("TRUE")));
        }
        if (_book == null) {
            // Only test cases omit the book (expecting it not to be needed)
            throw new IllegalStateException("Need book to evaluate name '" + name + "'");
        }
        EvaluationName evalName = _book.getName(name, _sheetIndex);
        if (evalName == null) {
            throw new FormulaParseException("Specified named range '"
                    + name + "' does not exist in the current workbook.");
        }
        if (evalName.isRange()) {
            return new ParseNode(evalName.createPtg());
        }
        // TODO - what about NameX ?
        throw new FormulaParseException("Specified name '"
                + name + "' is not a range as expected.");
    }
    
    private String parseAsName() {
        StringBuilder sb = new StringBuilder();

        // defined names may begin with a letter or underscore or backslash
        if (!Character.isLetter(look) && look != '_' && look != '\\') {
            throw expected("number, string, defined name, or data table");
        }
        while (isValidDefinedNameChar(look)) {
            sb.appendCodePoint(look);
            GetChar();
        }
        SkipWhite();

        return sb.toString();
    }

    /**
     * @param ch unicode codepoint
     * @return <code>true</code> if the specified character may be used in a defined name
     */
    private static boolean isValidDefinedNameChar(int ch) {
        if (Character.isLetterOrDigit(ch)) {
            return true;
        }
        // the sheet naming rules are vague on whether unicode characters are allowed
        // assume they're allowed.
        if (ch > 128) {
            return true;
        }
        switch (ch) {
            case '.':
            case '_':
            case '?':
            case '\\': // of all things
                return true;
        }
        // includes special non-name control characters like ! $ : , ( ) [ ] and space
        return false;
    }
    
    /**
     *
     * @param sheetIden may be <code>null</code>
     * @param part1
     * @param part2 may be <code>null</code>
     */
    private ParseNode createAreaRefParseNode(SheetIdentifier sheetIden, SimpleRangePart part1,
            SimpleRangePart part2) throws FormulaParseException {
        Ptg ptg;
        if (part2 == null) {
            CellReference cr = part1.getCellReference();
            if (sheetIden == null) {
                ptg = new RefPtg(cr);
            } else {
                ptg = _book.get3DReferencePtg(cr, sheetIden);
            }
        } else {
            AreaReference areaRef = createAreaRef(part1, part2);

            if (sheetIden == null) {
                ptg = new AreaPtg(areaRef);
            } else {
                ptg = _book.get3DReferencePtg(areaRef, sheetIden);
            }
        }
        return new ParseNode(ptg);
    }

    private AreaReference createAreaRef(SimpleRangePart part1, SimpleRangePart part2) {
        if (!part1.isCompatibleForArea(part2)) {
            throw new FormulaParseException("has incompatible parts: '"
                    + part1.getRep() + "' and '" + part2.getRep() + "'.");
        }
        if (part1.isRow()) {
            return AreaReference.getWholeRow(_ssVersion, part1.getRep(), part2.getRep());
        }
        if (part1.isColumn()) {
            return AreaReference.getWholeColumn(_ssVersion, part1.getRep(), part2.getRep());
        }
        return new AreaReference(part1.getCellReference(), part2.getCellReference(), _ssVersion);
    }

    /**
     * Matches a zero or one letter-runs followed by zero or one digit-runs.
     * Either or both runs man optionally be prefixed with a single '$'.
     * (copied+modified from {@link org.apache.poi.ss.util.CellReference#CELL_REF_PATTERN})
     */
    private static final Pattern CELL_REF_PATTERN = Pattern.compile("(\\$?[A-Za-z]+)?(\\$?[0-9]+)?");

    /**
     * Parses out a potential LHS or RHS of a ':' intended to produce a plain AreaRef.  Normally these are
     * proper cell references but they could also be row or column refs like "$AC" or "10"
     * @return <code>null</code> (and leaves {@link #_pointer} unchanged if a proper range part does not parse out
     */
    private SimpleRangePart parseSimpleRangePart() {
        int ptr = _pointer-1; // TODO avoid StringIndexOutOfBounds
        boolean hasDigits = false;
        boolean hasLetters = false;
        while (ptr < _formulaLength) {
            char ch = _formulaString.charAt(ptr);
            if (Character.isDigit(ch)) {
                hasDigits = true;
            } else if (Character.isLetter(ch)) {
                hasLetters = true;
            } else if (ch =='$' || ch =='_') {
                //
            } else {
                break;
            }
            ptr++;
        }
        if (ptr <= _pointer-1) {
            return null;
        }
        String rep = _formulaString.substring(_pointer-1, ptr);
        if (!CELL_REF_PATTERN.matcher(rep).matches()) {
            return null;
        }
        // Check range bounds against grid max
        if (hasLetters && hasDigits) {
            if (!isValidCellReference(rep)) {
                return null;
            }
        } else if (hasLetters) {
            if (!CellReference.isColumnWithinRange(rep.replace("$", ""), _ssVersion)) {
                return null;
            }
        } else if (hasDigits) {
            int i;
            try {
                i = Integer.parseInt(rep.replace("$", ""));
            } catch (NumberFormatException e) {
                return null;
            }
            if (i<1 || i>_ssVersion.getMaxRows()) {
                return null;
            }
        } else {
            // just dollars ? can this happen?
            return null;
        }


        resetPointer(ptr+1); // stepping forward
        return new SimpleRangePart(rep, hasLetters, hasDigits);
    }


    /**
     * A1, $A1, A$1, $A$1, A, 1
     */
    private static final class SimpleRangePart {
        private enum Type {
            CELL, ROW, COLUMN;

            public static Type get(boolean hasLetters, boolean hasDigits) {
                if (hasLetters) {
                    return hasDigits ? CELL : COLUMN;
                }
                if (!hasDigits) {
                    throw new IllegalArgumentException("must have either letters or numbers");
                }
                return ROW;
            }
        }

        private final Type _type;
        private final String _rep;

        public SimpleRangePart(String rep, boolean hasLetters, boolean hasNumbers) {
            _rep = rep;
            _type = Type.get(hasLetters, hasNumbers);
        }

        public boolean isCell() {
            return _type == Type.CELL;
        }

        public boolean isRowOrColumn() {
            return _type != Type.CELL;
        }

        public CellReference getCellReference() {
            if (_type != Type.CELL) {
                throw new IllegalStateException("Not applicable to this type");
            }
            return new CellReference(_rep);
        }

        public boolean isColumn() {
            return _type == Type.COLUMN;
        }

        public boolean isRow() {
            return _type == Type.ROW;
        }

        public String getRep() {
            return _rep;
        }

        /**
         * @return <code>true</code> if the two range parts can be combined in an
         * {@link AreaPtg} ( Note - the explicit range operator (:) may still be valid
         * when this method returns <code>false</code> )
         */
        public boolean isCompatibleForArea(SimpleRangePart part2) {
            return _type == part2._type;
        }

        @Override
        public String toString() {
            return getClass().getName() + " [" + _rep + "]";
        }
    }
    
    private String getBookName() {
        StringBuilder sb = new StringBuilder();
        GetChar();
        while (look != ']') {
            sb.appendCodePoint(look);
            GetChar();
        }
        GetChar();
        return sb.toString();
    }

    /**
     * Note - caller should reset {@link #_pointer} upon <code>null</code> result
     * @return The sheet name as an identifier <code>null</code> if '!' is not found in the right place
     */
    private SheetIdentifier parseSheetName() {
        String bookName;
        if (look == '[') {
        	bookName = getBookName();
        } else {
            bookName = null;
        }

        if (look == '\'') {
            Match('\'');
            
            if (look == '[')
            	bookName = getBookName();
            
            StringBuilder sb = new StringBuilder();
            boolean done = look == '\'';
            while(!done) {
                sb.appendCodePoint(look);
                GetChar();
                if(look == '\'')
                {
                    Match('\'');
                    done = look != '\'';
                }
            }

            NameIdentifier iden = new NameIdentifier(sb.toString(), true);
            // quoted identifier - can't concatenate anything more
            SkipWhite();
            if (look == '!') {
                GetChar();
                return new SheetIdentifier(bookName, iden);
            }
            // See if it's a multi-sheet range, eg Sheet1:Sheet3!A1
            if (look == ':') {
                return parseSheetRange(bookName, iden);
            }
            return null;
        }

        // unquoted sheet names must start with underscore or a letter
        if (look =='_' || Character.isLetter(look)) {
            StringBuilder sb = new StringBuilder();
            // can concatenate idens with dots
            while (isUnquotedSheetNameChar(look)) {
                sb.appendCodePoint(look);
                GetChar();
            }
            NameIdentifier iden = new NameIdentifier(sb.toString(), false);
            SkipWhite();
            if (look == '!') {
                GetChar();
                return new SheetIdentifier(bookName, iden);
            }
            // See if it's a multi-sheet range, eg Sheet1:Sheet3!A1
            if (look == ':') {
                return parseSheetRange(bookName, iden);
            }
            return null;
        }
        if (look == '!' && bookName != null) {
            // Raw book reference, without a sheet
            GetChar();
            return new SheetIdentifier(bookName, null);
        }
        return null;
    }
    
    /**
     * If we have something that looks like [book]Sheet1: or 
     *  Sheet1, see if it's actually a range eg Sheet1:Sheet2!
     */
    private SheetIdentifier parseSheetRange(String bookname, NameIdentifier sheet1Name) {
        GetChar();
        SheetIdentifier sheet2 = parseSheetName();
        if (sheet2 != null) {
           return new SheetRangeIdentifier(bookname, sheet1Name, sheet2.getSheetIdentifier());
        }
        return null;
    }

    /**
     * very similar to {@link SheetNameFormatter#isSpecialChar(char)}
     * @param ch unicode codepoint
     */
    private static boolean isUnquotedSheetNameChar(int ch) {
        if(Character.isLetterOrDigit(ch)) {
            return true;
        }
        // the sheet naming rules are vague on whether unicode characters are allowed
        // assume they're allowed.
        if (ch > 128) {
            return true;
        }
        switch(ch) {
            case '.': // dot is OK
            case '_': // underscore is OK
                return true;
        }
        return false;
    }

    /**
     * @return <code>true</code> if the specified name is a valid cell reference
     */
    private boolean isValidCellReference(String str) {
        //check range bounds against grid max
        boolean result = CellReference.classifyCellReference(str, _ssVersion) == NameType.CELL;

        if(result){
            /*
             * Check if the argument is a function. Certain names can be either a cell reference or a function name
             * depending on the contenxt. Compare the following examples in Excel 2007:
             * (a) LOG10(100) + 1
             * (b) LOG10 + 1
             * In (a) LOG10 is a name of a built-in function. In (b) LOG10 is a cell reference
             */
            boolean isFunc = FunctionMetadataRegistry.getFunctionByName(str.toUpperCase(Locale.ROOT)) != null;
            if(isFunc){
                int savePointer = _pointer;
                resetPointer(_pointer + str.length());
                SkipWhite();
                // open bracket indicates that the argument is a function,
                // the returning value should be false, i.e. "not a valid cell reference"
                result = look != '(';
                resetPointer(savePointer);
            }
        }
        return result;
    }


    /**
     * Note - Excel function names are 'case aware but not case sensitive'.  This method may end
     * up creating a defined name record in the workbook if the specified name is not an internal
     * Excel function, and has not been encountered before.
     * 
     * Side effect: creates workbook name if name is not recognized (name is probably a UDF)
     *
     * @param name case preserved function name (as it was entered/appeared in the formula).
     */
    private ParseNode function(String name) {
        Ptg nameToken = null;
        if(!AbstractFunctionPtg.isBuiltInFunctionName(name)) {
            // user defined function
            // in the token tree, the name is more or less the first argument

            if (_book == null) {
                // Only test cases omit the book (expecting it not to be needed)
                throw new IllegalStateException("Need book to evaluate name '" + name + "'");
            }
            // Check to see if name is a named range in the workbook
            EvaluationName hName = _book.getName(name, _sheetIndex);
            if (hName != null) {
                if (!hName.isFunctionName()) {
                    throw new FormulaParseException("Attempt to use name '" + name
                            + "' as a function, but defined name in workbook does not refer to a function");
                }
    
                // calls to user-defined functions within the workbook
                // get a Name token which points to a defined name record
                nameToken = hName.createPtg();
            } else {
                // Check if name is an external names table
                nameToken = _book.getNameXPtg(name, null);
                if (nameToken == null) {
                    // name is not an internal or external name
                    if (log.check(POILogger.WARN)) {
                        log.log(POILogger.WARN,
                                "FormulaParser.function: Name '" + name + "' is completely unknown in the current workbook.");
                    }
                    // name is probably the name of an unregistered User-Defined Function
                    switch (_book.getSpreadsheetVersion()) {
                        case EXCEL97:
                            // HSSFWorkbooks require a name to be added to Workbook defined names table
                            addName(name);
                            hName = _book.getName(name, _sheetIndex);
                            nameToken = hName.createPtg();
                            break;
                        case EXCEL2007:
                            // XSSFWorkbooks store formula names as strings.
                            nameToken = new NameXPxg(name);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected spreadsheet version: " + _book.getSpreadsheetVersion().name());
                    }
                }
            }
        }

        Match('(');
        ParseNode[] args = Arguments();
        Match(')');

        return getFunction(name, nameToken, args);
    }
    
    /**
     * Adds a name (named range or user defined function) to underlying workbook's names table
     */
    private void addName(String functionName) {
        final Name name = _book.createName();
        name.setFunction(true);
        name.setNameName(functionName);
        name.setSheetIndex(_sheetIndex);
    }

    /**
     * Generates the variable function ptg for the formula.
     * <p>
     * For IF Formulas, additional PTGs are added to the tokens
     * @param name a {@link NamePtg} or {@link NameXPtg} or <code>null</code>
     * @return Ptg a null is returned if we're in an IF formula, it needs extreme manipulation and is handled in this function
     */
    private ParseNode getFunction(String name, Ptg namePtg, ParseNode[] args) {

        FunctionMetadata fm = FunctionMetadataRegistry.getFunctionByName(name.toUpperCase(Locale.ROOT));
        int numArgs = args.length;
        if(fm == null) {
            if (namePtg == null) {
                throw new IllegalStateException("NamePtg must be supplied for external functions");
            }
            // must be external function
            ParseNode[] allArgs = new ParseNode[numArgs+1];
            allArgs[0] = new ParseNode(namePtg);
            System.arraycopy(args, 0, allArgs, 1, numArgs);
            return new ParseNode(FuncVarPtg.create(name, numArgs+1), allArgs);
        }

        if (namePtg != null) {
            throw new IllegalStateException("NamePtg no applicable to internal functions");
        }
        boolean isVarArgs = !fm.hasFixedArgsLength();
        int funcIx = fm.getIndex();
        if (funcIx == FunctionMetadataRegistry.FUNCTION_INDEX_SUM && args.length == 1) {
            // Excel encodes the sum of a single argument as tAttrSum
            // POI does the same for consistency, but this is not critical
            return new ParseNode(AttrPtg.getSumSingle(), args);
            // The code below would encode tFuncVar(SUM) which seems to do no harm
        }
        validateNumArgs(args.length, fm);

        AbstractFunctionPtg retval;
        if(isVarArgs) {
            retval = FuncVarPtg.create(name, numArgs);
        } else {
            retval = FuncPtg.create(funcIx);
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
        //the maximum number of arguments depends on the Excel version
        int maxArgs;
        if (fm.hasUnlimitedVarags()) {
            if(_book != null) {
                maxArgs = _book.getSpreadsheetVersion().getMaxFunctionArgs();
            } else {
                //_book can be omitted by test cases
                maxArgs = fm.getMaxParams(); // just use BIFF8
            }
        } else {
            maxArgs = fm.getMaxParams();
        }

        if(numArgs > maxArgs) {
            String msg = "Too many arguments to function '" + fm.getName() + "'. ";
            if(fm.hasFixedArgsLength()) {
                msg += "Expected " + maxArgs;
            } else {
                msg += "At most " + maxArgs + " were expected";
            }
            msg += " but got " + numArgs + ".";
            throw new FormulaParseException(msg);
       }
    }

    /**
     * @param ch  unicode codepoint
     *
     */
    private static boolean isArgumentDelimiter(int ch) {
        return ch ==  ',' || ch == ')';
    }

    /** get arguments to a function */
    private ParseNode[] Arguments() {
        //average 2 args per function
        List<ParseNode> temp = new ArrayList<>(2);
        SkipWhite();
        if(look == ')') {
            return ParseNode.EMPTY_ARRAY;
        }

        boolean missedPrevArg = true;
        while (true) {
            SkipWhite();
            if (isArgumentDelimiter(look)) {
                if (missedPrevArg) {
                    temp.add(new ParseNode(MissingArgPtg.instance));
                }
                if (look == ')') {
                    break;
                }
                Match(',');
                missedPrevArg = true;
                continue;
            }
            temp.add(intersectionExpression());
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
                return parseUnary(false);
            case '+':
                Match('+');
                return parseUnary(true);
            case '(':
                Match('(');
                ParseNode inside = unionExpression();
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
        // named ranges and tables can start with underscore or backslash
        // see https://support.office.com/en-us/article/Define-and-use-names-in-formulas-4d0f13ac-53b7-422e-afd2-abd7ff379c64?ui=en-US&rs=en-US&ad=US#bmsyntax_rules_for_names
        if (IsAlpha(look) || Character.isDigit(look) || look == '\'' || look == '[' || look == '_' || look == '\\' ) {
            return parseRangeExpression();
        }
        if (look == '.') {
            return new ParseNode(parseNumber());
        }
        throw expected("cell ref or constant literal");
    }


    private ParseNode parseUnary(boolean isPlus) {

        boolean numberFollows = IsDigit(look) || look=='.';
        ParseNode factor = powerFactor();

        if (numberFollows) {
            // + or - directly next to a number is parsed with the number

            Ptg token = factor.getToken();
            if (token instanceof NumberPtg) {
                if (isPlus) {
                    return factor;
                }
                token = new NumberPtg(-((NumberPtg)token).getValue());
                return new ParseNode(token);
            }
            if (token instanceof IntPtg) {
                if (isPlus) {
                    return factor;
                }
                int intVal = ((IntPtg)token).getValue();
                // note - cannot use IntPtg for negatives
                token = new NumberPtg(-intVal);
                return new ParseNode(token);
            }
        }
        return new ParseNode(isPlus ? UnaryPlusPtg.instance : UnaryMinusPtg.instance, factor);
    }

    private ParseNode parseArray() {
        List<Object[]> rowsData = new ArrayList<>();
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
        List<Object> temp = new ArrayList<>();
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
            case '"': return parseStringLiteral();
            case '#': return ErrorConstant.valueOf(parseErrorLiteral());
            case 'F': case 'f':
            case 'T': case 't':
                return parseBooleanLiteral();
            case '-':
                Match('-');
                SkipWhite();
                return convertArrayNumber(parseNumber(), false);
        }
        // else assume number
        return convertArrayNumber(parseNumber(), true);
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

    private static Double convertArrayNumber(Ptg ptg, boolean isPositive) {
        double value;
        if (ptg instanceof IntPtg) {
            value = ((IntPtg)ptg).getValue();
        } else  if (ptg instanceof NumberPtg) {
            value = ((NumberPtg)ptg).getValue();
        } else {
            throw new RuntimeException("Unexpected ptg (" + ptg.getClass().getName() + ")");
        }
        if (!isPositive) {
            value = -value;
        }
        return Double.valueOf(value);
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
        String part1 = parseUnquotedIdentifier();
        if (part1 == null) {
            throw expected("remainder of error constant literal");
        }
        part1 = part1.toUpperCase(Locale.ROOT);

        switch(part1.charAt(0)) {
            case 'V': {
                FormulaError fe = FormulaError.VALUE;
                if(part1.equals(fe.name())) {
                    Match('!');
                    return fe.getCode();
                }
                throw expected(fe.getString());
            }
            case 'R': {
                FormulaError fe = FormulaError.REF;
                if(part1.equals(fe.name())) {
                    Match('!');
                    return fe.getCode();
                }
                throw expected(fe.getString());
            }
            case 'D': {
                FormulaError fe = FormulaError.DIV0;
                if(part1.equals("DIV")) {
                    Match('/');
                    Match('0');
                    Match('!');
                    return fe.getCode();
                }
                throw expected(fe.getString());
            }
            case 'N': {
                FormulaError fe = FormulaError.NAME;
                if(part1.equals(fe.name())) {
                    // only one that ends in '?'
                    Match('?');
                    return fe.getCode();
                }
                fe = FormulaError.NUM;
                if(part1.equals(fe.name())) {
                    Match('!');
                    return fe.getCode();
                }
                fe = FormulaError.NULL;
                if(part1.equals(fe.name())) {
                    Match('!');
                    return fe.getCode();
                }
                fe = FormulaError.NA;
                if(part1.equals("N")) {
                    Match('/');
                    if(look != 'A' && look != 'a') {
                        throw expected(fe.getString());
                    }
                    Match(look);
                    // Note - no '!' or '?' suffix
                    return fe.getCode();
                }
                throw expected("#NAME?, #NUM!, #NULL! or #N/A");
            }
        }
        throw expected("#VALUE!, #REF!, #DIV/0!, #NAME?, #NUM!, #NULL! or #N/A");
    }

    private String parseUnquotedIdentifier() {
        if (look == '\'') {
            throw expected("unquoted identifier");
        }
        StringBuilder sb = new StringBuilder();
        while (Character.isLetterOrDigit(look) || look == '.') {
            sb.appendCodePoint(look);
            GetChar();
        }
        if (sb.length() < 1) {
            return null;
        }

        return sb.toString();
    }

    /**
     * Get a PTG for an integer from its string representation.
     * return Int or Number Ptg based on size of input
     */
    private static Ptg getNumberPtgFromString(String number1, String number2, String exponent) {
        StringBuilder number = new StringBuilder();

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

        StringBuilder token = new StringBuilder();
        while (true) {
            if (look == '"') {
                GetChar();
                if (look != '"') {
                    break;
                }
             }
            token.appendCodePoint(look);
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

    private ParseNode unionExpression() {
        ParseNode result = intersectionExpression();
        boolean hasUnions = false;
        while (true) {
            SkipWhite();
            if (look == ',') {
                GetChar();
                hasUnions = true;
                ParseNode other = intersectionExpression();
                result = new ParseNode(UnionPtg.instance, result, other);
                continue;
            }
            if (hasUnions) {
                return augmentWithMemPtg(result);
            }
            return result;
        }
    }

   private ParseNode intersectionExpression() {
        ParseNode result = comparisonExpression();
        boolean hasIntersections = false;
        while (true) {
            SkipWhite();
            if (_inIntersection) {
                int savePointer = _pointer;

                // Don't getChar() as the space has already been eaten and recorded by SkipWhite().
                try {
                    ParseNode other = comparisonExpression();
                    result = new ParseNode(IntersectionPtg.instance, result, other);
                    hasIntersections = true;
                    continue;
                } catch (FormulaParseException e) {
                    // if parsing for intersection fails we assume that we actually had an arbitrary
                    // whitespace and thus should simply skip this whitespace
                    resetPointer(savePointer);
                }
            }
            if (hasIntersections) {
                return augmentWithMemPtg(result);
            }
            return result;
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
    /*
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
        _pointer=0;
        GetChar();
        _rootNode = unionExpression();

        if(_pointer <= _formulaLength) {
            String msg = "Unused input [" + _formulaString.substring(_pointer-1)
                + "] after attempting to parse the formula [" + _formulaString + "]";
            throw new FormulaParseException(msg);
        }
    }

    private Ptg[] getRPNPtg(FormulaType formulaType) {
        OperandClassTransformer oct = new OperandClassTransformer(formulaType);
        // RVA is for 'operand class': 'reference', 'value', 'array'
        oct.transformFormula(_rootNode);
        return ParseNode.toTokenArray(_rootNode);
    }
}
