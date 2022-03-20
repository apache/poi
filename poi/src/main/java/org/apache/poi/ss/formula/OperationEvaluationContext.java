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

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.CollaboratingWorkbooksEnvironment.WorkbookNotFoundException;
import org.apache.poi.ss.formula.EvaluationWorkbook.ExternalName;
import org.apache.poi.ss.formula.EvaluationWorkbook.ExternalSheet;
import org.apache.poi.ss.formula.EvaluationWorkbook.ExternalSheetRange;
import org.apache.poi.ss.formula.constant.ErrorConstant;
import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.ExternalNameEval;
import org.apache.poi.ss.formula.eval.FunctionNameEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.ptg.Area3DPtg;
import org.apache.poi.ss.formula.ptg.Area3DPxg;
import org.apache.poi.ss.formula.ptg.NameXPtg;
import org.apache.poi.ss.formula.ptg.NameXPxg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPtg;
import org.apache.poi.ss.formula.ptg.Ref3DPxg;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellReference.NameType;
import org.apache.poi.util.LocaleUtil;

/**
 * Contains all the contextual information required to evaluate an operation
 * within a formula
 * <p>
 * For POI internal use only
 */
public final class OperationEvaluationContext {
    public static final FreeRefFunction UDF = UserDefinedFunction.instance;
    private final EvaluationWorkbook _workbook;
    private final int _sheetIndex;
    private final int _rowIndex;
    private final int _columnIndex;
    private final EvaluationTracker _tracker;
    private final WorkbookEvaluator _bookEvaluator;
    private final boolean _isSingleValue;
    private boolean _isInArrayContext;

    public OperationEvaluationContext(WorkbookEvaluator bookEvaluator, EvaluationWorkbook workbook, int sheetIndex, int srcRowNum,
                                      int srcColNum, EvaluationTracker tracker) {
        this(bookEvaluator, workbook, sheetIndex, srcRowNum, srcColNum, tracker, true);
    }

    public OperationEvaluationContext(WorkbookEvaluator bookEvaluator, EvaluationWorkbook workbook, int sheetIndex, int srcRowNum,
                                      int srcColNum, EvaluationTracker tracker, boolean isSingleValue) {
        _bookEvaluator = bookEvaluator;
        _workbook = workbook;
        _sheetIndex = sheetIndex;
        _rowIndex = srcRowNum;
        _columnIndex = srcColNum;
        _tracker = tracker;
        _isSingleValue = isSingleValue;
    }

    public boolean isArraymode() {
        return _isInArrayContext;
    }

    public void setArrayMode(boolean value) {
        _isInArrayContext = value;
    }

    public EvaluationWorkbook getWorkbook() {
        return _workbook;
    }

    public int getRowIndex() {
        return _rowIndex;
    }

    public int getColumnIndex() {
        return _columnIndex;
    }

    SheetRangeEvaluator createExternSheetRefEvaluator(ExternSheetReferenceToken ptg) {
        return createExternSheetRefEvaluator(ptg.getExternSheetIndex());
    }

    SheetRangeEvaluator createExternSheetRefEvaluator(String firstSheetName, String lastSheetName, int externalWorkbookNumber) {
        ExternalSheet externalSheet = _workbook.getExternalSheet(firstSheetName, lastSheetName, externalWorkbookNumber);
        return createExternSheetRefEvaluator(externalSheet);
    }

    SheetRangeEvaluator createExternSheetRefEvaluator(int externSheetIndex) {
        ExternalSheet externalSheet = _workbook.getExternalSheet(externSheetIndex);
        return createExternSheetRefEvaluator(externalSheet);
    }

    SheetRangeEvaluator createExternSheetRefEvaluator(ExternalSheet externalSheet) {
        WorkbookEvaluator targetEvaluator;
        int otherFirstSheetIndex;
        int otherLastSheetIndex = -1;
        if (externalSheet == null || externalSheet.getWorkbookName() == null) {
            // sheet is in same workbook
            targetEvaluator = _bookEvaluator;
            if (externalSheet == null) {
                otherFirstSheetIndex = 0;
            } else {
                otherFirstSheetIndex = _workbook.getSheetIndex(externalSheet.getSheetName());
            }

            if (externalSheet instanceof ExternalSheetRange) {
                String lastSheetName = ((ExternalSheetRange) externalSheet).getLastSheetName();
                otherLastSheetIndex = _workbook.getSheetIndex(lastSheetName);
            }
        } else {
            // look up sheet by name from external workbook
            String workbookName = externalSheet.getWorkbookName();
            try {
                targetEvaluator = _bookEvaluator.getOtherWorkbookEvaluator(workbookName);
            } catch (WorkbookNotFoundException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            otherFirstSheetIndex = targetEvaluator.getSheetIndex(externalSheet.getSheetName());
            if (externalSheet instanceof ExternalSheetRange) {
                String lastSheetName = ((ExternalSheetRange) externalSheet).getLastSheetName();
                otherLastSheetIndex = targetEvaluator.getSheetIndex(lastSheetName);
            }

            if (otherFirstSheetIndex < 0) {
                throw new RuntimeException("Invalid sheet name '" + externalSheet.getSheetName()
                        + "' in bool '" + workbookName + "'.");
            }
        }

        if (otherLastSheetIndex == -1) {
            // Reference to just one sheet
            otherLastSheetIndex = otherFirstSheetIndex;
        }

        SheetRefEvaluator[] evals = new SheetRefEvaluator[otherLastSheetIndex - otherFirstSheetIndex + 1];
        for (int i = 0; i < evals.length; i++) {
            int otherSheetIndex = i + otherFirstSheetIndex;
            evals[i] = new SheetRefEvaluator(targetEvaluator, _tracker, otherSheetIndex);
        }
        return new SheetRangeEvaluator(otherFirstSheetIndex, otherLastSheetIndex, evals);
    }

    /**
     * @return {@code null} if either workbook or sheet is not found
     */
    private SheetRefEvaluator createExternSheetRefEvaluator(String workbookName, String sheetName) {
        WorkbookEvaluator targetEvaluator;
        if (workbookName == null) {
            targetEvaluator = _bookEvaluator;
        } else {
            if (sheetName == null) {
                throw new IllegalArgumentException("sheetName must not be null if workbookName is provided");
            }
            try {
                targetEvaluator = _bookEvaluator.getOtherWorkbookEvaluator(workbookName);
            } catch (WorkbookNotFoundException e) {
                return null;
            }
        }
        int otherSheetIndex = sheetName == null ? _sheetIndex : targetEvaluator.getSheetIndex(sheetName);
        if (otherSheetIndex < 0) {
            return null;
        }
        return new SheetRefEvaluator(targetEvaluator, _tracker, otherSheetIndex);
    }

    public SheetRangeEvaluator getRefEvaluatorForCurrentSheet() {
        SheetRefEvaluator sre = new SheetRefEvaluator(_bookEvaluator, _tracker, _sheetIndex);
        return new SheetRangeEvaluator(_sheetIndex, sre);
    }


    /**
     * Resolves a cell or area reference dynamically.
     *
     * @param workbookName the name of the workbook containing the reference.  If {@code null}
     *                     the current workbook is assumed.  Note - to evaluate formulas which use multiple workbooks,
     *                     a {@link CollaboratingWorkbooksEnvironment} must be set up.
     * @param sheetName    the name of the sheet containing the reference.  May be {@code null}
     *                     (when {@code workbookName} is also null) in which case the current workbook and sheet is
     *                     assumed.
     * @param refStrPart1  the single cell reference or first part of the area reference.  Must not
     *                     be {@code null}.
     * @param refStrPart2  the second part of the area reference. For single cell references this
     *                     parameter must be {@code null}
     * @param isA1Style    specifies the format for {@code refStrPart1} and {@code refStrPart2}.
     *                     Pass {@code true} for 'A1' style and {@code false} for 'R1C1' style.
     * @return a {@link RefEval} or {@link AreaEval}
     * @throws RuntimeException If invalid parameters are provided
     */
    public ValueEval getDynamicReference(String workbookName, String sheetName, String refStrPart1,
                                         String refStrPart2, boolean isA1Style) {
        SheetRefEvaluator se = createExternSheetRefEvaluator(workbookName, sheetName);
        if (se == null) {
            return ErrorEval.REF_INVALID;
        }
        SheetRangeEvaluator sre = new SheetRangeEvaluator(_sheetIndex, se);

        SpreadsheetVersion ssVersion = _workbook.getSpreadsheetVersion();

        NameType part1refType = isA1Style ? classifyCellReference(refStrPart1, ssVersion) : getR1C1CellType(refStrPart1);
        switch (part1refType) {
            case BAD_CELL_OR_NAMED_RANGE:
                return ErrorEval.REF_INVALID;
            case NAMED_RANGE:
                EvaluationName nm = _workbook.getName(refStrPart1, _sheetIndex);
                if (nm == null) {
                    throw new RuntimeException("Specified name '" + refStrPart1 +
                            "' is not found in the workbook (sheetIndex=" + _sheetIndex + ").");
                }
                if (!nm.isRange()) {
                    throw new RuntimeException("Specified name '" + refStrPart1 + "' is not a range as expected.");
                }
                return _bookEvaluator.evaluateNameFormula(nm.getNameDefinition(), this);
        }
        if (refStrPart2 == null) {
            // no ':'
            switch (part1refType) {
                case COLUMN:
                    if (isA1Style) {
                        return ErrorEval.REF_INVALID;
                    } else {
                        try {
                            String upRef = refStrPart1.toUpperCase(LocaleUtil.getUserLocale());
                            int cpos = upRef.indexOf('C');
                            String cval = refStrPart1.substring(cpos + 1).trim();
                            int absoluteC;
                            if (cval.startsWith("[") && cval.endsWith("]")) {
                                int relativeC = Integer.parseInt(cval.substring(1, cval.length() - 1).trim());
                                absoluteC = getColumnIndex() + relativeC;
                            } else if (!cval.isEmpty()) {
                                absoluteC = Integer.parseInt(cval) - 1;
                            } else {
                                return ErrorEval.REF_INVALID;
                            }
                            return new LazyAreaEval(0, absoluteC, ssVersion.getLastRowIndex(), absoluteC, sre);
                        } catch (Exception e) {
                            return ErrorEval.REF_INVALID;
                        }
                    }
                case ROW:
                    if (isA1Style) {
                        return ErrorEval.REF_INVALID;
                    } else {
                        try {
                            String upRef = refStrPart1.toUpperCase(LocaleUtil.getUserLocale());
                            int rpos = upRef.indexOf('R');
                            String rval = refStrPart1.substring(rpos + 1).trim();
                            int absoluteR;
                            if (rval.startsWith("[") && rval.endsWith("]")) {
                                int relativeR = Integer.parseInt(rval.substring(1, rval.length() - 1).trim());
                                absoluteR = getRowIndex() + relativeR;
                            } else if (!rval.isEmpty()) {
                                absoluteR = Integer.parseInt(rval) - 1;
                            } else {
                                return ErrorEval.REF_INVALID;
                            }
                            return new LazyAreaEval(absoluteR, 0, absoluteR, ssVersion.getLastColumnIndex(), sre);
                        } catch (Exception e) {
                            return ErrorEval.REF_INVALID;
                        }
                    }
                case CELL:
                    CellReference cr;
                    if (isA1Style) {
                        cr = new CellReference(refStrPart1);
                    } else {
                        cr = applyR1C1Reference(new CellReference(getRowIndex(), getColumnIndex()), refStrPart1);
                    }
                    return new LazyRefEval(cr.getRow(), cr.getCol(), sre);
            }
            throw new IllegalStateException("Unexpected reference classification of '" + refStrPart1 + "'.");
        }
        NameType part2refType = isA1Style ? classifyCellReference(refStrPart2, ssVersion) : getR1C1CellType(refStrPart2);
        switch (part2refType) {
            case BAD_CELL_OR_NAMED_RANGE:
                return ErrorEval.REF_INVALID;
            case NAMED_RANGE:
                throw new RuntimeException("Cannot evaluate '" + refStrPart1
                        + "'. Indirect evaluation of defined names not supported yet");
        }

        if (part2refType != part1refType) {
            // LHS and RHS of ':' must be compatible
            return ErrorEval.REF_INVALID;
        }
        int firstRow, firstCol, lastRow, lastCol;
        switch (part1refType) {
            case COLUMN:
                firstRow = 0;
                lastRow = ssVersion.getLastRowIndex();
                firstCol = parseRowRef(refStrPart1);
                lastCol = parseRowRef(refStrPart2);
                break;
            case ROW:
                // support of cell range in the form of integer:integer
                firstCol = 0;
                lastCol = ssVersion.getLastColumnIndex();
                firstRow = parseColRef(refStrPart1);
                lastRow = parseColRef(refStrPart2);
                break;
            case CELL:
                CellReference cr;
                if (isA1Style) {
                    cr = new CellReference(refStrPart1);
                } else {
                    cr = applyR1C1Reference(new CellReference(getRowIndex(), getColumnIndex()), refStrPart1);
                }
                firstRow = cr.getRow();
                firstCol = cr.getCol();
                if (isA1Style) {
                    cr = new CellReference(refStrPart2);
                } else {
                    cr = applyR1C1Reference(new CellReference(getRowIndex(), getColumnIndex()), refStrPart2);
                }
                lastRow = cr.getRow();
                lastCol = cr.getCol();
                break;
            default:
                throw new IllegalStateException("Unexpected reference classification of '" + refStrPart1 + "'.");
        }
        return new LazyAreaEval(firstRow, firstCol, lastRow, lastCol, sre);
    }

    private static int parseRowRef(String refStrPart) {
        return CellReference.convertColStringToIndex(refStrPart);
    }

    private static int parseColRef(String refStrPart) {
        return Integer.parseInt(refStrPart) - 1;
    }

    private static NameType classifyCellReference(String str, SpreadsheetVersion ssVersion) {
        int len = str.length();
        if (len < 1) {
            return CellReference.NameType.BAD_CELL_OR_NAMED_RANGE;
        }
        return CellReference.classifyCellReference(str, ssVersion);
    }

    public FreeRefFunction findUserDefinedFunction(String functionName) {
        return _bookEvaluator.findUserDefinedFunction(functionName);
    }

    public ValueEval getRefEval(int rowIndex, int columnIndex) {
        SheetRangeEvaluator sre = getRefEvaluatorForCurrentSheet();
        return new LazyRefEval(rowIndex, columnIndex, sre);
    }

    public ValueEval getRef3DEval(Ref3DPtg rptg) {
        SheetRangeEvaluator sre = createExternSheetRefEvaluator(rptg.getExternSheetIndex());
        return new LazyRefEval(rptg.getRow(), rptg.getColumn(), sre);
    }

    public ValueEval getRef3DEval(Ref3DPxg rptg) {
        SheetRangeEvaluator sre = createExternSheetRefEvaluator(
                rptg.getSheetName(), rptg.getLastSheetName(), rptg.getExternalWorkbookNumber());
        return new LazyRefEval(rptg.getRow(), rptg.getColumn(), sre);
    }

    public ValueEval getAreaEval(int firstRowIndex, int firstColumnIndex,
                                 int lastRowIndex, int lastColumnIndex) {
        SheetRangeEvaluator sre = getRefEvaluatorForCurrentSheet();
        return new LazyAreaEval(firstRowIndex, firstColumnIndex, lastRowIndex, lastColumnIndex, sre);
    }

    public ValueEval getArea3DEval(Area3DPtg aptg) {
        SheetRangeEvaluator sre = createExternSheetRefEvaluator(aptg.getExternSheetIndex());
        return new LazyAreaEval(aptg.getFirstRow(), aptg.getFirstColumn(),
                aptg.getLastRow(), aptg.getLastColumn(), sre);
    }

    public ValueEval getArea3DEval(Area3DPxg aptg) {
        SheetRangeEvaluator sre = createExternSheetRefEvaluator(
                aptg.getSheetName(), aptg.getLastSheetName(), aptg.getExternalWorkbookNumber());
        return new LazyAreaEval(aptg.getFirstRow(), aptg.getFirstColumn(),
                aptg.getLastRow(), aptg.getLastColumn(), sre);
    }

    public ValueEval getAreaValueEval(int firstRowIndex, int firstColumnIndex,
                                      int lastRowIndex, int lastColumnIndex, Object[][] tokens) {

        ValueEval[] values = new ValueEval[tokens.length * tokens[0].length];

        int index = 0;
        for (Object[] token : tokens) {
            for (int idx = 0; idx < tokens[0].length; idx++) {
                values[index++] = convertObjectEval(token[idx]);
            }
        }

        return new CacheAreaEval(firstRowIndex, firstColumnIndex, lastRowIndex,
                lastColumnIndex, values);
    }

    private ValueEval convertObjectEval(Object token) {
        if (token == null) {
            throw new RuntimeException("Array item cannot be null");
        }
        if (token instanceof String) {
            return new StringEval((String) token);
        }
        if (token instanceof Double) {
            return new NumberEval((Double) token);
        }
        if (token instanceof Boolean) {
            return BoolEval.valueOf((Boolean) token);
        }
        if (token instanceof ErrorConstant) {
            return ErrorEval.valueOf(((ErrorConstant) token).getErrorCode());
        }
        throw new IllegalArgumentException("Unexpected constant class (" + token.getClass().getName() + ")");
    }


    public ValueEval getNameXEval(NameXPtg nameXPtg) {
        // Is the name actually on our workbook?
        ExternalSheet externSheet = _workbook.getExternalSheet(nameXPtg.getSheetRefIndex());
        if (externSheet == null || externSheet.getWorkbookName() == null) {
            // External reference to our own workbook's name
            return getLocalNameXEval(nameXPtg);
        }

        // Look it up for the external workbook
        String workbookName = externSheet.getWorkbookName();
        ExternalName externName = _workbook.getExternalName(
                nameXPtg.getSheetRefIndex(),
                nameXPtg.getNameIndex()
        );
        return getExternalNameXEval(externName, workbookName);
    }

    public ValueEval getNameXEval(NameXPxg nameXPxg) {
        ExternalSheet externSheet = _workbook.getExternalSheet(nameXPxg.getSheetName(), null, nameXPxg.getExternalWorkbookNumber());
        if (externSheet == null || externSheet.getWorkbookName() == null) {
            // External reference to our own workbook's name
            return getLocalNameXEval(nameXPxg);
        }

        // Look it up for the external workbook
        String workbookName = externSheet.getWorkbookName();
        ExternalName externName = _workbook.getExternalName(
                nameXPxg.getNameName(),
                nameXPxg.getSheetName(),
                nameXPxg.getExternalWorkbookNumber()
        );
        return getExternalNameXEval(externName, workbookName);
    }

    private ValueEval getLocalNameXEval(NameXPxg nameXPxg) {
        // Look up the sheet, if present
        int sIdx = -1;
        if (nameXPxg.getSheetName() != null) {
            sIdx = _workbook.getSheetIndex(nameXPxg.getSheetName());
        }

        // Is it a name or a function?
        String name = nameXPxg.getNameName();
        EvaluationName evalName = _workbook.getName(name, sIdx);
        if (evalName != null) {
            // Process it as a name
            return new ExternalNameEval(evalName);
        } else {
            // Must be an external function
            return new FunctionNameEval(name);
        }
    }

    private ValueEval getLocalNameXEval(NameXPtg nameXPtg) {
        String name = _workbook.resolveNameXText(nameXPtg);

        // Try to parse it as a name
        int sheetNameAt = name.indexOf('!');
        EvaluationName evalName;
        if (sheetNameAt > -1) {
            // Sheet based name
            String sheetName = name.substring(0, sheetNameAt);
            String nameName = name.substring(sheetNameAt + 1);
            evalName = _workbook.getName(nameName, _workbook.getSheetIndex(sheetName));
        } else {
            // Workbook based name
            evalName = _workbook.getName(name, -1);
        }

        if (evalName != null) {
            // Process it as a name
            return new ExternalNameEval(evalName);
        } else {
            // Must be an external function
            return new FunctionNameEval(name);
        }
    }

    public int getSheetIndex() {
        return _sheetIndex;
    }

    /**
     * default true
     *
     * @return flag indicating whether evaluation should "unwrap" the result to a single value based on the context row/column
     */
    public boolean isSingleValue() {
        return _isSingleValue;
    }

    private ValueEval getExternalNameXEval(ExternalName externName, String workbookName) {
        try {
            // Fetch the workbook this refers to, and the name as defined with that
            WorkbookEvaluator refWorkbookEvaluator = _bookEvaluator.getOtherWorkbookEvaluator(workbookName);
            EvaluationName evaluationName = refWorkbookEvaluator.getName(externName.getName(), externName.getIx() - 1);
            if (evaluationName != null && evaluationName.hasFormula()) {
                if (evaluationName.getNameDefinition().length > 1) {
                    throw new RuntimeException("Complex name formulas not supported yet");
                }

                // Need to evaluate the reference in the context of the other book
                OperationEvaluationContext refWorkbookContext = new OperationEvaluationContext(
                        refWorkbookEvaluator, refWorkbookEvaluator.getWorkbook(), -1, -1, -1, _tracker);

                Ptg ptg = evaluationName.getNameDefinition()[0];
                if (ptg instanceof Ref3DPtg) {
                    Ref3DPtg ref3D = (Ref3DPtg) ptg;
                    return refWorkbookContext.getRef3DEval(ref3D);
                } else if (ptg instanceof Ref3DPxg) {
                    Ref3DPxg ref3D = (Ref3DPxg) ptg;
                    return refWorkbookContext.getRef3DEval(ref3D);
                } else if (ptg instanceof Area3DPtg) {
                    Area3DPtg area3D = (Area3DPtg) ptg;
                    return refWorkbookContext.getArea3DEval(area3D);
                } else if (ptg instanceof Area3DPxg) {
                    Area3DPxg area3D = (Area3DPxg) ptg;
                    return refWorkbookContext.getArea3DEval(area3D);
                }
            }
            return ErrorEval.REF_INVALID;
        } catch (WorkbookNotFoundException wnfe) {
            return ErrorEval.REF_INVALID;
        }
    }

    public static CellReference applyR1C1Reference(CellReference anchorReference, String relativeReference) {
        String upRef = relativeReference.toUpperCase(LocaleUtil.getUserLocale());
        int rpos = upRef.indexOf('R');
        int cpos = upRef.indexOf('C');
        if (rpos >= 0 && cpos > rpos) {
            String rval = relativeReference.substring(rpos + 1, cpos).trim();
            String cval = relativeReference.substring(cpos + 1).trim();
            int absoluteR = -1;
            int relativeR = 0;
            if (rval.startsWith("[") && rval.endsWith("]")) {
                relativeR = Integer.parseInt(rval.substring(1, rval.length() - 1).trim());
            } else if (!rval.isEmpty()) {
                absoluteR = Integer.parseInt(rval);
            }
            int absoluteC = -1;
            int relativeC = 0;
            if (cval.startsWith("[") && cval.endsWith("]")) {
                relativeC = Integer.parseInt(cval.substring(1, cval.length() - 1).trim());
            } else if (!cval.isEmpty()) {
                absoluteC = Integer.parseInt(cval);
            }
            int newR;
            if (absoluteR >= 0) {
                newR = absoluteR - 1;
            } else {
                newR = anchorReference.getRow() + relativeR;
            }
            int newC;
            if (absoluteC >= 0) {
                newC = absoluteC - 1;
            } else {
                newC = anchorReference.getCol() + relativeC;
            }
            return new CellReference(newR, newC);
        } else {
            throw new IllegalArgumentException(relativeReference + " is not a valid R1C1 reference");
        }
    }

    private static NameType getR1C1CellType(String str) {
        String upRef = str.toUpperCase(LocaleUtil.getUserLocale());
        int rpos = upRef.indexOf('R');
        int cpos = upRef.indexOf('C');
        if (rpos != -1) {
            if (cpos == -1) {
                return NameType.ROW;
            } else {
                return NameType.CELL;
            }
        } else {
            if (cpos == -1) {
                return NameType.BAD_CELL_OR_NAMED_RANGE;
            } else {
                return NameType.COLUMN;
            }
        }
    }
}
