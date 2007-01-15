/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/*
 * Created on May 5, 2005
 *
 */
package org.apache.poi.hssf.usermodel;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.poi.hssf.model.FormulaParser;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.formula.AddPtg;
import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.hssf.record.formula.AttrPtg;
import org.apache.poi.hssf.record.formula.BoolPtg;
import org.apache.poi.hssf.record.formula.ConcatPtg;
import org.apache.poi.hssf.record.formula.ControlPtg;
import org.apache.poi.hssf.record.formula.DividePtg;
import org.apache.poi.hssf.record.formula.EqualPtg;
import org.apache.poi.hssf.record.formula.FuncPtg;
import org.apache.poi.hssf.record.formula.FuncVarPtg;
import org.apache.poi.hssf.record.formula.GreaterEqualPtg;
import org.apache.poi.hssf.record.formula.GreaterThanPtg;
import org.apache.poi.hssf.record.formula.IntPtg;
import org.apache.poi.hssf.record.formula.LessEqualPtg;
import org.apache.poi.hssf.record.formula.LessThanPtg;
import org.apache.poi.hssf.record.formula.MemErrPtg;
import org.apache.poi.hssf.record.formula.MissingArgPtg;
import org.apache.poi.hssf.record.formula.MultiplyPtg;
import org.apache.poi.hssf.record.formula.NamePtg;
import org.apache.poi.hssf.record.formula.NameXPtg;
import org.apache.poi.hssf.record.formula.NotEqualPtg;
import org.apache.poi.hssf.record.formula.NumberPtg;
import org.apache.poi.hssf.record.formula.OperationPtg;
import org.apache.poi.hssf.record.formula.ParenthesisPtg;
import org.apache.poi.hssf.record.formula.PowerPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.Ref3DPtg;
import org.apache.poi.hssf.record.formula.ReferencePtg;
import org.apache.poi.hssf.record.formula.StringPtg;
import org.apache.poi.hssf.record.formula.SubtractPtg;
import org.apache.poi.hssf.record.formula.UnaryMinusPtg;
import org.apache.poi.hssf.record.formula.UnaryPlusPtg;
import org.apache.poi.hssf.record.formula.UnionPtg;
import org.apache.poi.hssf.record.formula.UnknownPtg;
import org.apache.poi.hssf.record.formula.eval.AddEval;
import org.apache.poi.hssf.record.formula.eval.Area2DEval;
import org.apache.poi.hssf.record.formula.eval.Area3DEval;
import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ConcatEval;
import org.apache.poi.hssf.record.formula.eval.DivideEval;
import org.apache.poi.hssf.record.formula.eval.EqualEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.FuncVarEval;
import org.apache.poi.hssf.record.formula.eval.GreaterEqualEval;
import org.apache.poi.hssf.record.formula.eval.GreaterThanEval;
import org.apache.poi.hssf.record.formula.eval.LessEqualEval;
import org.apache.poi.hssf.record.formula.eval.LessThanEval;
import org.apache.poi.hssf.record.formula.eval.MultiplyEval;
import org.apache.poi.hssf.record.formula.eval.NotEqualEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.OperationEval;
import org.apache.poi.hssf.record.formula.eval.PowerEval;
import org.apache.poi.hssf.record.formula.eval.Ref2DEval;
import org.apache.poi.hssf.record.formula.eval.Ref3DEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.SubtractEval;
import org.apache.poi.hssf.record.formula.eval.UnaryMinusEval;
import org.apache.poi.hssf.record.formula.eval.UnaryPlusEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * 
 * Limitations: Unfortunately, cyclic references will cause stackoverflow
 * exception
 */
public class HSSFFormulaEvaluator {

    // params to lookup the right constructor using reflection
    private static final Class[] OPERATION_CONSTRUCTOR_CLASS_ARRAY = new Class[] { Ptg.class };

    private static final Class[] VALUE_CONTRUCTOR_CLASS_ARRAY = new Class[] { Ptg.class };

    private static final Class[] AREA3D_CONSTRUCTOR_CLASS_ARRAY = new Class[] { Ptg.class, ValueEval[].class };

    private static final Class[] REFERENCE_CONSTRUCTOR_CLASS_ARRAY = new Class[] { Ptg.class, ValueEval.class };

    private static final Class[] REF3D_CONSTRUCTOR_CLASS_ARRAY = new Class[] { Ptg.class, ValueEval.class };

    // Maps for mapping *Eval to *Ptg
    private static final Map VALUE_EVALS_MAP = new HashMap();

    private static final Map OPERATION_EVALS_MAP = new HashMap();

    /*
     * Following is the mapping between the Ptg tokens returned 
     * by the FormulaParser and the *Eval classes that are used 
     * by the FormulaEvaluator
     */
    static {
        VALUE_EVALS_MAP.put(BoolPtg.class, BoolEval.class);
        VALUE_EVALS_MAP.put(IntPtg.class, NumberEval.class);
        VALUE_EVALS_MAP.put(NumberPtg.class, NumberEval.class);
        VALUE_EVALS_MAP.put(StringPtg.class, StringEval.class);

        OPERATION_EVALS_MAP.put(AddPtg.class, AddEval.class);
        OPERATION_EVALS_MAP.put(ConcatPtg.class, ConcatEval.class);
        OPERATION_EVALS_MAP.put(DividePtg.class, DivideEval.class);
        OPERATION_EVALS_MAP.put(EqualPtg.class, EqualEval.class);
        //OPERATION_EVALS_MAP.put(ExpPtg.class, ExpEval.class); // TODO: check
        // this
        OPERATION_EVALS_MAP.put(FuncPtg.class, FuncVarEval.class); // TODO:
                                                                   // check this
        OPERATION_EVALS_MAP.put(FuncVarPtg.class, FuncVarEval.class);
        OPERATION_EVALS_MAP.put(GreaterEqualPtg.class, GreaterEqualEval.class);
        OPERATION_EVALS_MAP.put(GreaterThanPtg.class, GreaterThanEval.class);
        OPERATION_EVALS_MAP.put(LessEqualPtg.class, LessEqualEval.class);
        OPERATION_EVALS_MAP.put(LessThanPtg.class, LessThanEval.class);
        OPERATION_EVALS_MAP.put(MultiplyPtg.class, MultiplyEval.class);
        OPERATION_EVALS_MAP.put(NotEqualPtg.class, NotEqualEval.class);
        OPERATION_EVALS_MAP.put(PowerPtg.class, PowerEval.class);
        OPERATION_EVALS_MAP.put(SubtractPtg.class, SubtractEval.class);
        OPERATION_EVALS_MAP.put(UnaryMinusPtg.class, UnaryMinusEval.class);
        OPERATION_EVALS_MAP.put(UnaryPlusPtg.class, UnaryPlusEval.class);

    }

    
    protected HSSFRow row;
    protected HSSFSheet sheet;
    protected HSSFWorkbook workbook;
    
    public HSSFFormulaEvaluator(HSSFSheet sheet, HSSFWorkbook workbook) {
        this.sheet = sheet;
        this.workbook = workbook;
    }
    
    public void setCurrentRow(HSSFRow row) {
        this.row = row;
    }

    
    /**
     * If cell contains a formula, the formula is evaluated and returned,
     * else the CellValue simply copies the appropriate cell value from
     * the cell and also its cell type. This method should be preferred over
     * evaluateInCell() when the call should not modify the contents of the
     * original cell. 
     * @param cell
     * @return
     */
    public CellValue evaluate(HSSFCell cell) {
        CellValue retval = null;
        if (cell != null) {
            switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_BLANK:
                retval = new CellValue(HSSFCell.CELL_TYPE_BLANK);
                break;
            case HSSFCell.CELL_TYPE_BOOLEAN:
                retval = new CellValue(HSSFCell.CELL_TYPE_BOOLEAN);
                retval.setBooleanValue(cell.getBooleanCellValue());
                break;
            case HSSFCell.CELL_TYPE_ERROR:
                retval = new CellValue(HSSFCell.CELL_TYPE_ERROR);
                retval.setErrorValue(cell.getErrorCellValue());
                break;
            case HSSFCell.CELL_TYPE_FORMULA:
                retval = getCellValueForEval(internalEvaluate(cell, row, sheet, workbook));
                break;
            case HSSFCell.CELL_TYPE_NUMERIC:
                retval = new CellValue(HSSFCell.CELL_TYPE_NUMERIC);
                retval.setNumberValue(cell.getNumericCellValue());
                break;
            case HSSFCell.CELL_TYPE_STRING:
                retval = new CellValue(HSSFCell.CELL_TYPE_STRING);
                retval.setRichTextStringValue(cell.getRichStringCellValue());
                break;
            }
        }
        return retval;
    }
    
    
    /**
     * If cell contains formula, it evaluates the formula, and puts the 
     * formula result back into the cell.
     * Else if cell does not contain formula, this method leaves the cell 
     * unchanged. Note that the same instance of HSSFCell is returned to 
     * allow chained calls like:
     * <pre>
     * int evaluatedCellType = evaluator.evaluateInCell(cell).getCellType();
     * </pre>
     * @param cell
     */
    public HSSFCell evaluateInCell(HSSFCell cell) {
        if (cell != null) {
            switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_FORMULA:
                CellValue cv = getCellValueForEval(internalEvaluate(cell, row, sheet, workbook));
                switch (cv.getCellType()) {
                case HSSFCell.CELL_TYPE_BOOLEAN:
                    cell.setCellValue(cv.getBooleanValue());
                    break;
                case HSSFCell.CELL_TYPE_ERROR:
                    cell.setCellValue(cv.getErrorValue());
                    break;
                case HSSFCell.CELL_TYPE_NUMERIC:
                	cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                    cell.setCellValue(cv.getNumberValue());
                    break;
                case HSSFCell.CELL_TYPE_STRING:
                    cell.setCellValue(cv.getRichTextStringValue());
                    break;
                case HSSFCell.CELL_TYPE_BLANK:
                    break;
                case HSSFCell.CELL_TYPE_FORMULA: // this will never happen, we have already evaluated the formula
                    break;
                }
            }
        }
        return cell;
    }
        
    
    /**
     * Returns a CellValue wrapper around the supplied ValueEval instance.
     * @param eval
     * @return
     */
    protected static CellValue getCellValueForEval(ValueEval eval) {
        CellValue retval = null;
        if (eval != null) {
            if (eval instanceof NumberEval) {
                NumberEval ne = (NumberEval) eval;
                retval = new CellValue(HSSFCell.CELL_TYPE_NUMERIC);
                retval.setNumberValue(ne.getNumberValue());
            }
            else if (eval instanceof BoolEval) {
                BoolEval be = (BoolEval) eval;
                retval = new CellValue(HSSFCell.CELL_TYPE_BOOLEAN);
                retval.setBooleanValue(be.getBooleanValue());
            }
            else if (eval instanceof StringEval) {
                StringEval ne = (StringEval) eval;
                retval = new CellValue(HSSFCell.CELL_TYPE_STRING);
                retval.setStringValue(ne.getStringValue());
            }
            else if (eval instanceof BlankEval) {
                retval = new CellValue(HSSFCell.CELL_TYPE_BLANK);
            }
            else {
                retval = new CellValue(HSSFCell.CELL_TYPE_ERROR);
            }
        }
        return retval;
    }
    
    /**
     * Dev. Note: Internal evaluate must be passed only a formula cell 
     * else a runtime exception will be thrown somewhere inside the method.
     * (Hence this is a private method.)
     * 
     * @param formula
     * @param sheet
     * @param workbook
     * @return
     */
    protected static ValueEval internalEvaluate(HSSFCell srcCell, HSSFRow srcRow, HSSFSheet sheet, HSSFWorkbook workbook) {
        int srcRowNum = srcRow.getRowNum();
        short srcColNum = srcCell.getCellNum();
        FormulaParser parser = new FormulaParser(srcCell.getCellFormula(), workbook.getWorkbook());
        parser.parse();
        Ptg[] ptgs = parser.getRPNPtg();
        // -- parsing over --
        

        Stack stack = new Stack();
        for (int i = 0, iSize = ptgs.length; i < iSize; i++) {

            // since we dont know how to handle these yet :(
            if (ptgs[i] instanceof ControlPtg) { continue; }
            if (ptgs[i] instanceof MemErrPtg) { continue; }
            if (ptgs[i] instanceof MissingArgPtg) { continue; }
            if (ptgs[i] instanceof NamePtg) { continue; }
            if (ptgs[i] instanceof NameXPtg) { continue; }
            if (ptgs[i] instanceof UnknownPtg) { continue; }

            if (ptgs[i] instanceof OperationPtg) {
                OperationPtg optg = (OperationPtg) ptgs[i];

                // parens can be ignored since we have RPN tokens
                if (optg instanceof ParenthesisPtg) { continue; }
                if (optg instanceof AttrPtg) { continue; }
                if (optg instanceof UnionPtg) { continue; }

                OperationEval operation = (OperationEval) getOperationEvalForPtg(optg);

                int numops = operation.getNumberOfOperands();
                Eval[] ops = new Eval[numops];

                // storing the ops in reverse order since they are popping
                for (int j = numops - 1; j >= 0; j--) {
                    Eval p = (Eval) stack.pop();
                    ops[j] = p;
                }
                Eval opresult = operation.evaluate(ops, srcRowNum, srcColNum);
                stack.push(opresult);
            }
            else if (ptgs[i] instanceof ReferencePtg) {
                ReferencePtg ptg = (ReferencePtg) ptgs[i];
                short colnum = ptg.getColumn();
                short rownum = ptg.getRow();
                HSSFRow row = sheet.getRow(rownum);
                HSSFCell cell = (row != null) ? row.getCell(colnum) : null;
                pushRef2DEval(ptg, stack, cell, row, sheet, workbook);
            }
            else if (ptgs[i] instanceof Ref3DPtg) {
                Ref3DPtg ptg = (Ref3DPtg) ptgs[i];
                short colnum = ptg.getColumn();
                short rownum = ptg.getRow();
                Workbook wb = workbook.getWorkbook();
                HSSFSheet xsheet = workbook.getSheetAt(wb.getSheetIndexFromExternSheetIndex(ptg.getExternSheetIndex()));
                HSSFRow row = xsheet.getRow(rownum);
                HSSFCell cell = (row != null) ? row.getCell(colnum) : null;
                pushRef3DEval(ptg, stack, cell, row, xsheet, workbook);
            }
            else if (ptgs[i] instanceof AreaPtg) {
                AreaPtg ap = (AreaPtg) ptgs[i];
                short row0 = ap.getFirstRow();
                short col0 = ap.getFirstColumn();
                short row1 = ap.getLastRow();
                short col1 = ap.getLastColumn();
                ValueEval[] values = new ValueEval[(row1 - row0 + 1) * (col1 - col0 + 1)];
                for (short x = row0; sheet != null && x < row1 + 1; x++) {
                    HSSFRow row = sheet.getRow(x);
                    for (short y = col0; row != null && y < col1 + 1; y++) {
                        values[(x - row0) * (col1 - col0 + 1) + (y - col0)] = 
                            getEvalForCell(row.getCell(y), row, sheet, workbook);
                    }
                }
                AreaEval ae = new Area2DEval(ap, values);
                stack.push(ae);
            }
            else if (ptgs[i] instanceof Area3DPtg) {
                Area3DPtg a3dp = (Area3DPtg) ptgs[i];
                short row0 = a3dp.getFirstRow();
                short col0 = a3dp.getFirstColumn();
                short row1 = a3dp.getLastRow();
                short col1 = a3dp.getLastColumn();
                HSSFSheet xsheet = workbook.getSheetAt(a3dp.getExternSheetIndex());
                ValueEval[] values = new ValueEval[(row1 - row0 + 1) * (col1 - col0 + 1)];
                for (short x = row0; sheet != null && x < row1 + 1; x++) {
                    HSSFRow row = sheet.getRow(x);
                    for (short y = col0; row != null && y < col1 + 1; y++) {
                        values[(x - row0) * (col1 - col0 + 1) + (y - col0)] = 
                            getEvalForCell(row.getCell(y), row, xsheet, workbook);
                    }
                }
                AreaEval ae = new Area3DEval(a3dp, values);
                stack.push(ae);
            }
            else {
                Eval ptgEval = getEvalForPtg(ptgs[i]);
                stack.push(ptgEval);
            }
        }
        ValueEval value = ((ValueEval) stack.pop());
        if (value instanceof RefEval) {
            RefEval rv = (RefEval) value;
            value = rv.getInnerValueEval();
        }
        else if (value instanceof AreaEval) {
            AreaEval ae = (AreaEval) value;
            if (ae.isRow()) 
                value = ae.getValueAt(ae.getFirstRow(), srcColNum);
            else if (ae.isColumn()) 
                value = ae.getValueAt(srcRowNum, ae.getFirstColumn());
            else
                value = ErrorEval.VALUE_INVALID;
        }
        return value;
    }

    /**
     * returns the OperationEval concrete impl instance corresponding
     * to the suplied operationPtg
     * @param ptg
     * @return
     */
    protected static Eval getOperationEvalForPtg(OperationPtg ptg) {
        Eval retval = null;

        Class clazz = (Class) OPERATION_EVALS_MAP.get(ptg.getClass());
        try {
            Constructor constructor = clazz.getConstructor(OPERATION_CONSTRUCTOR_CLASS_ARRAY);
            retval = (OperationEval) constructor.newInstance(new Ptg[] { ptg });
        }
        catch (Exception e) {
            throw new RuntimeException("Fatal Error: ", e);
        }
        return retval;
    }

    /**
     * returns an appropriate Eval impl instance for the Ptg. The Ptg must be
     * one of: Area3DPtg, AreaPtg, ReferencePtg, Ref3DPtg, IntPtg, NumberPtg,
     * StringPtg, BoolPtg <br/>special Note: OperationPtg subtypes cannot be
     * passed here!
     * 
     * @param ptg
     * @return
     */
    protected static Eval getEvalForPtg(Ptg ptg) {
        Eval retval = null;

        Class clazz = (Class) VALUE_EVALS_MAP.get(ptg.getClass());
        try {
            if (ptg instanceof Area3DPtg) {
                Constructor constructor = clazz.getConstructor(AREA3D_CONSTRUCTOR_CLASS_ARRAY);
                retval = (OperationEval) constructor.newInstance(new Ptg[] { ptg });
            }
            else if (ptg instanceof AreaPtg) {
                Constructor constructor = clazz.getConstructor(AREA3D_CONSTRUCTOR_CLASS_ARRAY);
                retval = (OperationEval) constructor.newInstance(new Ptg[] { ptg });
            }
            else if (ptg instanceof ReferencePtg) {
                Constructor constructor = clazz.getConstructor(REFERENCE_CONSTRUCTOR_CLASS_ARRAY);
                retval = (OperationEval) constructor.newInstance(new Ptg[] { ptg });
            }
            else if (ptg instanceof Ref3DPtg) {
                Constructor constructor = clazz.getConstructor(REF3D_CONSTRUCTOR_CLASS_ARRAY);
                retval = (OperationEval) constructor.newInstance(new Ptg[] { ptg });
            }
            else {
                if (ptg instanceof IntPtg || ptg instanceof NumberPtg || ptg instanceof StringPtg
                        || ptg instanceof BoolPtg) {
                    Constructor constructor = clazz.getConstructor(VALUE_CONTRUCTOR_CLASS_ARRAY);
                    retval = (ValueEval) constructor.newInstance(new Ptg[] { ptg });
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Fatal Error: ", e);
        }
        return retval;

    }

    /**
     * Given a cell, find its type and from that create an appropriate ValueEval
     * impl instance and return that. Since the cell could be an external
     * reference, we need the sheet that this belongs to.
     * Non existent cells are treated as empty.
     * @param cell
     * @param sheet
     * @param workbook
     * @return
     */
    protected static ValueEval getEvalForCell(HSSFCell cell, HSSFRow row, HSSFSheet sheet, HSSFWorkbook workbook) {
        ValueEval retval = BlankEval.INSTANCE;
        if (cell != null) {
            switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_NUMERIC:
                retval = new NumberEval(cell.getNumericCellValue());
                break;
            case HSSFCell.CELL_TYPE_STRING:
                retval = new StringEval(cell.getRichStringCellValue().getString());
                break;
            case HSSFCell.CELL_TYPE_FORMULA:
                retval = internalEvaluate(cell, row, sheet, workbook);
                break;
            case HSSFCell.CELL_TYPE_BOOLEAN:
                retval = cell.getBooleanCellValue() ? BoolEval.TRUE : BoolEval.FALSE;
                break;
            case HSSFCell.CELL_TYPE_BLANK:
                retval = BlankEval.INSTANCE;
                break;
            case HSSFCell.CELL_TYPE_ERROR:
                retval = ErrorEval.UNKNOWN_ERROR; // TODO: think about this...
                break;
            }
        }
        return retval;
    }

    /**
     * create a Ref2DEval for ReferencePtg and push it on the stack.
     * Non existent cells are treated as RefEvals containing BlankEval.
     * @param ptg
     * @param stack
     * @param cell
     * @param sheet
     * @param workbook
     */
    protected static void pushRef2DEval(ReferencePtg ptg, Stack stack, 
            HSSFCell cell, HSSFRow row, HSSFSheet sheet, HSSFWorkbook workbook) {
        if (cell != null)
            switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_NUMERIC:
                stack.push(new Ref2DEval(ptg, new NumberEval(cell.getNumericCellValue()), false));
                break;
            case HSSFCell.CELL_TYPE_STRING:
                stack.push(new Ref2DEval(ptg, new StringEval(cell.getRichStringCellValue().getString()), false));
                break;
            case HSSFCell.CELL_TYPE_FORMULA:
                stack.push(new Ref2DEval(ptg, internalEvaluate(cell, row, sheet, workbook), true));
                break;
            case HSSFCell.CELL_TYPE_BOOLEAN:
                stack.push(new Ref2DEval(ptg, cell.getBooleanCellValue() ? BoolEval.TRUE : BoolEval.FALSE, false));
                break;
            case HSSFCell.CELL_TYPE_BLANK:
                stack.push(new Ref2DEval(ptg, BlankEval.INSTANCE, false));
                break;
            case HSSFCell.CELL_TYPE_ERROR:
                stack.push(new Ref2DEval(ptg, ErrorEval.UNKNOWN_ERROR, false)); // TODO: think abt this
                break;
            }
        else {
            stack.push(new Ref2DEval(ptg, BlankEval.INSTANCE, false));
        }
    }

    /**
     * create a Ref3DEval for Ref3DPtg and push it on the stack.
     * 
     * @param ptg
     * @param stack
     * @param cell
     * @param sheet
     * @param workbook
     */
    protected static void pushRef3DEval(Ref3DPtg ptg, Stack stack, HSSFCell cell, 
            HSSFRow row, HSSFSheet sheet, HSSFWorkbook workbook) {
        if (cell != null)
            switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_NUMERIC:
                stack.push(new Ref3DEval(ptg, new NumberEval(cell.getNumericCellValue()), false));
                break;
            case HSSFCell.CELL_TYPE_STRING:
                stack.push(new Ref3DEval(ptg, new StringEval(cell.getRichStringCellValue().getString()), false));
                break;
            case HSSFCell.CELL_TYPE_FORMULA:
                stack.push(new Ref3DEval(ptg, internalEvaluate(cell, row, sheet, workbook), true));
                break;
            case HSSFCell.CELL_TYPE_BOOLEAN:
                stack.push(new Ref3DEval(ptg, cell.getBooleanCellValue() ? BoolEval.TRUE : BoolEval.FALSE, false));
                break;
            case HSSFCell.CELL_TYPE_BLANK:
                stack.push(new Ref3DEval(ptg, BlankEval.INSTANCE, false));
                break;
            case HSSFCell.CELL_TYPE_ERROR:
                stack.push(new Ref3DEval(ptg, ErrorEval.UNKNOWN_ERROR, false)); // TODO: think abt this
                break;
            }
        else {
            stack.push(new Ref3DEval(ptg, BlankEval.INSTANCE, false));
        }
    }
    
    /**
     * Mimics the 'data view' of a cell. This allows formula evaluator 
     * to return a CellValue instead of precasting the value to String
     * or Number or boolean type.
     * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
     */
    public static final class CellValue {
        private int cellType;
        private HSSFRichTextString richTextStringValue;
        private double numberValue;
        private boolean booleanValue;
        private byte errorValue;
        
        /**
         * CellType should be one of the types defined in HSSFCell
         * @param cellType
         */
        public CellValue(int cellType) {
            super();
            this.cellType = cellType;
        }
        /**
         * @return Returns the booleanValue.
         */
        public boolean getBooleanValue() {
            return booleanValue;
        }
        /**
         * @param booleanValue The booleanValue to set.
         */
        public void setBooleanValue(boolean booleanValue) {
            this.booleanValue = booleanValue;
        }
        /**
         * @return Returns the numberValue.
         */
        public double getNumberValue() {
            return numberValue;
        }
        /**
         * @param numberValue The numberValue to set.
         */
        public void setNumberValue(double numberValue) {
            this.numberValue = numberValue;
        }
        /**
         * @return Returns the stringValue. This method is deprecated, use
         * getRichTextStringValue instead
         * @deprecated
         */
        public String getStringValue() {
            return richTextStringValue.getString();
        }
        /**
         * @param stringValue The stringValue to set. This method is deprecated, use
         * getRichTextStringValue instead.
         * @deprecated
         */
        public void setStringValue(String stringValue) {
            this.richTextStringValue = new HSSFRichTextString(stringValue);
        }
        /**
         * @return Returns the cellType.
         */
        public int getCellType() {
            return cellType;
        }
        /**
         * @return Returns the errorValue.
         */
        public byte getErrorValue() {
            return errorValue;
        }
        /**
         * @param errorValue The errorValue to set.
         */
        public void setErrorValue(byte errorValue) {
            this.errorValue = errorValue;
        }
        /**
         * @return Returns the richTextStringValue.
         */
		public HSSFRichTextString getRichTextStringValue() {
			return richTextStringValue;
		}
        /**
         * @param richTextStringValue The richTextStringValue to set.
         */
		public void setRichTextStringValue(HSSFRichTextString richTextStringValue) {
			this.richTextStringValue = richTextStringValue;
		}
    }

    /**
     * debug method
     * 
     * @param formula
     * @param sheet
     * @param workbook
     */
    void inspectPtgs(String formula) {
        FormulaParser fp = new FormulaParser(formula, workbook.getWorkbook());
        fp.parse();
        Ptg[] ptgs = fp.getRPNPtg();
        System.out.println("<ptg-group>");
        for (int i = 0, iSize = ptgs.length; i < iSize; i++) {
            System.out.println("<ptg>");
            System.out.println(ptgs[i]);
            if (ptgs[i] instanceof OperationPtg) {
                System.out.println("numoperands: " + ((OperationPtg) ptgs[i]).getNumberOfOperands());
            }
            System.out.println("</ptg>");
        }
        System.out.println("</ptg-group>");
    }

}
