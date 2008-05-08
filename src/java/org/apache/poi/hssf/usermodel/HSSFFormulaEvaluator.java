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

package org.apache.poi.hssf.usermodel;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.apache.poi.hssf.model.FormulaParser;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.hssf.record.formula.AttrPtg;
import org.apache.poi.hssf.record.formula.BoolPtg;
import org.apache.poi.hssf.record.formula.ControlPtg;
import org.apache.poi.hssf.record.formula.IntPtg;
import org.apache.poi.hssf.record.formula.MemErrPtg;
import org.apache.poi.hssf.record.formula.MissingArgPtg;
import org.apache.poi.hssf.record.formula.NamePtg;
import org.apache.poi.hssf.record.formula.NameXPtg;
import org.apache.poi.hssf.record.formula.NumberPtg;
import org.apache.poi.hssf.record.formula.OperationPtg;
import org.apache.poi.hssf.record.formula.ParenthesisPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.Ref3DPtg;
import org.apache.poi.hssf.record.formula.ReferencePtg;
import org.apache.poi.hssf.record.formula.StringPtg;
import org.apache.poi.hssf.record.formula.UnionPtg;
import org.apache.poi.hssf.record.formula.UnknownPtg;
import org.apache.poi.hssf.record.formula.eval.Area2DEval;
import org.apache.poi.hssf.record.formula.eval.Area3DEval;
import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.FunctionEval;
import org.apache.poi.hssf.record.formula.eval.NameEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.OperationEval;
import org.apache.poi.hssf.record.formula.eval.Ref2DEval;
import org.apache.poi.hssf.record.formula.eval.Ref3DEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * 
 */
public class HSSFFormulaEvaluator {
                
    // params to lookup the right constructor using reflection
    private static final Class[] VALUE_CONTRUCTOR_CLASS_ARRAY = new Class[] { Ptg.class };

    private static final Class[] AREA3D_CONSTRUCTOR_CLASS_ARRAY = new Class[] { Ptg.class, ValueEval[].class };

    private static final Class[] REFERENCE_CONSTRUCTOR_CLASS_ARRAY = new Class[] { Ptg.class, ValueEval.class };

    private static final Class[] REF3D_CONSTRUCTOR_CLASS_ARRAY = new Class[] { Ptg.class, ValueEval.class };

    // Maps for mapping *Eval to *Ptg
    private static final Map VALUE_EVALS_MAP = new HashMap();

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
     * Returns an underlying FormulaParser, for the specified
     *  Formula String and HSSFWorkbook.
     * This will allow you to generate the Ptgs yourself, if
     *  your needs are more complex than just having the
     *  formula evaluated. 
     */
    public static FormulaParser getUnderlyingParser(HSSFWorkbook workbook, String formula) {
        return new FormulaParser(formula, workbook);
    }
    
    /**
     * If cell contains a formula, the formula is evaluated and returned,
     * else the CellValue simply copies the appropriate cell value from
     * the cell and also its cell type. This method should be preferred over
     * evaluateInCell() when the call should not modify the contents of the
     * original cell. 
     * @param cell
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
     * If cell contains formula, it evaluates the formula,
     *  and saves the result of the formula. The cell
     *  remains as a formula cell.
     * Else if cell does not contain formula, this method leaves
     *  the cell unchanged. 
     * Note that the type of the formula result is returned,
     *  so you know what kind of value is also stored with
     *  the formula. 
     * <pre>
     * int evaluatedCellType = evaluator.evaluateFormulaCell(cell);
     * </pre>
     * Be aware that your cell will hold both the formula,
     *  and the result. If you want the cell replaced with
     *  the result of the formula, use {@link #evaluateInCell(HSSFCell)}
     * @param cell The cell to evaluate
     * @return The type of the formula result (the cell's type remains as HSSFCell.CELL_TYPE_FORMULA however)
     */
    public int evaluateFormulaCell(HSSFCell cell) {
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
                return cv.getCellType();
            }
        }
        return -1;
    }
        
    /**
     * If cell contains formula, it evaluates the formula, and
     *  puts the formula result back into the cell, in place
     *  of the old formula.
     * Else if cell does not contain formula, this method leaves
     *  the cell unchanged. 
     * Note that the same instance of HSSFCell is returned to 
     * allow chained calls like:
     * <pre>
     * int evaluatedCellType = evaluator.evaluateInCell(cell).getCellType();
     * </pre>
     * Be aware that your cell value will be changed to hold the
     *  result of the formula. If you simply want the formula
     *  value computed for you, use {@link #evaluateFormulaCell(HSSFCell)}
     * @param cell
     */
    public HSSFCell evaluateInCell(HSSFCell cell) {
        if (cell != null) {
            switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_FORMULA:
                CellValue cv = getCellValueForEval(internalEvaluate(cell, row, sheet, workbook));
                switch (cv.getCellType()) {
                case HSSFCell.CELL_TYPE_BOOLEAN:
                    cell.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
                    cell.setCellValue(cv.getBooleanValue());
                    break;
                case HSSFCell.CELL_TYPE_ERROR:
                    cell.setCellErrorValue(cv.getErrorValue());
                    break;
                case HSSFCell.CELL_TYPE_NUMERIC:
                    cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                    cell.setCellValue(cv.getNumberValue());
                    break;
                case HSSFCell.CELL_TYPE_STRING:
                    cell.setCellType(HSSFCell.CELL_TYPE_STRING);
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
     * Loops over all cells in all sheets of the supplied
     *  workbook.
     * For cells that contain formulas, their formulas are
     *  evaluated, and the results are saved. These cells
     *  remain as formula cells.
     * For cells that do not contain formulas, no changes
     *  are made.
     * This is a helpful wrapper around looping over all 
     *  cells, and calling evaluateFormulaCell on each one.
     */
	public static void evaluateAllFormulaCells(HSSFWorkbook wb) {
		for(int i=0; i<wb.getNumberOfSheets(); i++) {
			HSSFSheet sheet = wb.getSheetAt(i);
			HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(sheet, wb);

			for (Iterator rit = sheet.rowIterator(); rit.hasNext();) {
				HSSFRow r = (HSSFRow)rit.next();
				evaluator.setCurrentRow(r);

				for (Iterator cit = r.cellIterator(); cit.hasNext();) {
					HSSFCell c = (HSSFCell)cit.next();
					if (c.getCellType() == HSSFCell.CELL_TYPE_FORMULA)
						evaluator.evaluateFormulaCell(c);
				}
			}
		}
	}
        
    
    /**
     * Returns a CellValue wrapper around the supplied ValueEval instance.
     * @param eval
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
            else if (eval instanceof ErrorEval) {
                retval = new CellValue(HSSFCell.CELL_TYPE_ERROR);
                retval.setErrorValue((byte)((ErrorEval)eval).getErrorCode());
//                retval.setRichTextStringValue(new HSSFRichTextString("#An error occurred. check cell.getErrorCode()"));
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
     */
    private static ValueEval internalEvaluate(HSSFCell srcCell, HSSFRow srcRow, HSSFSheet sheet, HSSFWorkbook workbook) {
        int srcRowNum = srcRow.getRowNum();
        short srcColNum = srcCell.getCellNum();
        
        
        EvaluationCycleDetector tracker = EvaluationCycleDetectorManager.getTracker();
        
        if(!tracker.startEvaluate(workbook, sheet, srcRowNum, srcColNum)) {
            return ErrorEval.CIRCULAR_REF_ERROR;
        }
        try {
            return evaluateCell(workbook, sheet, srcRowNum, srcColNum, srcCell.getCellFormula());
        } finally {
            tracker.endEvaluate(workbook, sheet, srcRowNum, srcColNum);
        }
    }
    private static ValueEval evaluateCell(HSSFWorkbook workbook, HSSFSheet sheet, 
            int srcRowNum, short srcColNum, String cellFormulaText) {
        FormulaParser parser = new FormulaParser(cellFormulaText, workbook);
        parser.parse();
        Ptg[] ptgs = parser.getRPNPtg();
        // -- parsing over --
        

        Stack stack = new Stack();
        for (int i = 0, iSize = ptgs.length; i < iSize; i++) {

            // since we don't know how to handle these yet :(
            Ptg ptg = ptgs[i];
            if (ptg instanceof ControlPtg) { continue; }
            if (ptg instanceof MemErrPtg) { continue; }
            if (ptg instanceof MissingArgPtg) { continue; }
            if (ptg instanceof NamePtg) { 
            	// named ranges, macro functions
                NamePtg namePtg = (NamePtg) ptg;
                stack.push(new NameEval(namePtg.getIndex()));
                continue; 
            }
            if (ptg instanceof NameXPtg) {
            	// TODO - external functions
                continue;
            }
            if (ptg instanceof UnknownPtg) { continue; }

            if (ptg instanceof OperationPtg) {
                OperationPtg optg = (OperationPtg) ptg;

                // parens can be ignored since we have RPN tokens
                if (optg instanceof ParenthesisPtg) { continue; }
                if (optg instanceof AttrPtg) { continue; }
                if (optg instanceof UnionPtg) { continue; }

                OperationEval operation = OperationEvaluatorFactory.create(optg);

                int numops = operation.getNumberOfOperands();
                Eval[] ops = new Eval[numops];

                // storing the ops in reverse order since they are popping
                for (int j = numops - 1; j >= 0; j--) {
                    Eval p = (Eval) stack.pop();
                    ops[j] = p;
                }
                Eval opresult = invokeOperation(operation, ops, srcRowNum, srcColNum, workbook, sheet);
                stack.push(opresult);
            }
            else if (ptg instanceof ReferencePtg) {
                ReferencePtg refPtg = (ReferencePtg) ptg;
                int colIx = refPtg.getColumn();
                int rowIx = refPtg.getRow();
                HSSFRow row = sheet.getRow(rowIx);
                HSSFCell cell = (row != null) ? row.getCell(colIx) : null;
                stack.push(createRef2DEval(refPtg, cell, row, sheet, workbook));
            }
            else if (ptg instanceof Ref3DPtg) {
                Ref3DPtg refPtg = (Ref3DPtg) ptg;
                int colIx = refPtg.getColumn();
                int rowIx = refPtg.getRow();
                Workbook wb = workbook.getWorkbook();
                HSSFSheet xsheet = workbook.getSheetAt(wb.getSheetIndexFromExternSheetIndex(refPtg.getExternSheetIndex()));
                HSSFRow row = xsheet.getRow(rowIx);
                HSSFCell cell = (row != null) ? row.getCell(colIx) : null;
                stack.push(createRef3DEval(refPtg, cell, row, xsheet, workbook));
            }
            else if (ptg instanceof AreaPtg) {
                AreaPtg ap = (AreaPtg) ptg;
                AreaEval ae = evaluateAreaPtg(sheet, workbook, ap);
                stack.push(ae);
            }
            else if (ptg instanceof Area3DPtg) {
                Area3DPtg a3dp = (Area3DPtg) ptg;
                AreaEval ae = evaluateArea3dPtg(workbook, a3dp);
                stack.push(ae);
            }
            else {
                Eval ptgEval = getEvalForPtg(ptg);
                stack.push(ptgEval);
            }
        }

        ValueEval value = ((ValueEval) stack.pop());
        if (!stack.isEmpty()) {
            throw new IllegalStateException("evaluation stack not empty");
        }
        value = dereferenceValue(value, srcRowNum, srcColNum);
        if (value instanceof BlankEval) {
        	// Note Excel behaviour here. A blank final final value is converted to zero.  
            return NumberEval.ZERO;
            // Formulas _never_ evaluate to blank.  If a formula appears to have evaluated to 
            // blank, the actual value is empty string. This can be verified with ISBLANK().
        }
        return value;
    }

    /**
     * Dereferences a single value from any AreaEval or RefEval evaluation result.
     * If the supplied evaluationResult is just a plain value, it is returned as-is.
	 * @return a <tt>NumberEval</tt>, <tt>StringEval</tt>, <tt>BoolEval</tt>,
	 *  <tt>BlankEval</tt> or <tt>ErrorEval</tt>. Never <code>null</code>.
     */
    private static ValueEval dereferenceValue(ValueEval evaluationResult, int srcRowNum, short srcColNum) {
        if (evaluationResult instanceof RefEval) {
            RefEval rv = (RefEval) evaluationResult;
            return rv.getInnerValueEval();
        }
        if (evaluationResult instanceof AreaEval) {
            AreaEval ae = (AreaEval) evaluationResult;
            if (ae.isRow()) {
                if(ae.isColumn()) {
                    return ae.getValues()[0];
                }
                return ae.getValueAt(ae.getFirstRow(), srcColNum);
            }
            if (ae.isColumn()) {
                return ae.getValueAt(srcRowNum, ae.getFirstColumn());
            }
            return ErrorEval.VALUE_INVALID;
        }
        return evaluationResult;
    }

    private static Eval invokeOperation(OperationEval operation, Eval[] ops, int srcRowNum, short srcColNum,
            HSSFWorkbook workbook, HSSFSheet sheet) {

        if(operation instanceof FunctionEval) {
            FunctionEval fe = (FunctionEval) operation;
            if(fe.isFreeRefFunction()) {
                return fe.getFreeRefFunction().evaluate(ops, srcRowNum, srcColNum, workbook, sheet);
            }
        }
        return operation.evaluate(ops, srcRowNum, srcColNum);
    }
    
    public static AreaEval evaluateAreaPtg(HSSFSheet sheet, HSSFWorkbook workbook, AreaPtg ap) {
        int row0 = ap.getFirstRow();
        int col0 = ap.getFirstColumn();
        int row1 = ap.getLastRow();
        int col1 = ap.getLastColumn();
        
        // If the last row is -1, then the
        //  reference is for the rest of the column
        // (eg C:C)
        // TODO: Handle whole column ranges properly
        if(row1 == -1 && row0 >= 0) {
            row1 = (short)sheet.getLastRowNum();
        }
        ValueEval[] values = evalArea(workbook, sheet, row0, col0, row1, col1);
        return new Area2DEval(ap, values);
    }

    public static AreaEval evaluateArea3dPtg(HSSFWorkbook workbook, Area3DPtg a3dp) {
    	int row0 = a3dp.getFirstRow();
    	int col0 = a3dp.getFirstColumn();
    	int row1 = a3dp.getLastRow();
    	int col1 = a3dp.getLastColumn();
        Workbook wb = workbook.getWorkbook();
        HSSFSheet xsheet = workbook.getSheetAt(wb.getSheetIndexFromExternSheetIndex(a3dp.getExternSheetIndex()));
        
        // If the last row is -1, then the
        //  reference is for the rest of the column
        // (eg C:C)
        // TODO: Handle whole column ranges properly
        if(row1 == -1 && row0 >= 0) {
            row1 = (short)xsheet.getLastRowNum();
        }
        
        ValueEval[] values = evalArea(workbook, xsheet, row0, col0, row1, col1);
        return new Area3DEval(a3dp, values);
    }
    
    private static ValueEval[] evalArea(HSSFWorkbook workbook, HSSFSheet sheet, 
    		int row0, int col0, int row1, int col1) {
        ValueEval[] values = new ValueEval[(row1 - row0 + 1) * (col1 - col0 + 1)];
        for (int x = row0; sheet != null && x < row1 + 1; x++) {
            HSSFRow row = sheet.getRow(x);
            for (int y = col0; y < col1 + 1; y++) {
                ValueEval cellEval;
                if(row == null) {
                	cellEval = BlankEval.INSTANCE;
                } else {
                	cellEval = getEvalForCell(row.getCell(y), row, sheet, workbook);
                }
				values[(x - row0) * (col1 - col0 + 1) + (y - col0)] = cellEval;
            }
        }
        return values;
    }

    /**
     * returns an appropriate Eval impl instance for the Ptg. The Ptg must be
     * one of: Area3DPtg, AreaPtg, ReferencePtg, Ref3DPtg, IntPtg, NumberPtg,
     * StringPtg, BoolPtg <br/>special Note: OperationPtg subtypes cannot be
     * passed here!
     * 
     * @param ptg
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
     */
    protected static ValueEval getEvalForCell(HSSFCell cell, HSSFRow row, HSSFSheet sheet, HSSFWorkbook workbook) {

        if (cell == null) {
            return BlankEval.INSTANCE;
        }
        switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_NUMERIC:
                return new NumberEval(cell.getNumericCellValue());
            case HSSFCell.CELL_TYPE_STRING:
                return new StringEval(cell.getRichStringCellValue().getString());
            case HSSFCell.CELL_TYPE_FORMULA:
                return internalEvaluate(cell, row, sheet, workbook);
            case HSSFCell.CELL_TYPE_BOOLEAN:
                return BoolEval.valueOf(cell.getBooleanCellValue());
            case HSSFCell.CELL_TYPE_BLANK:
                return BlankEval.INSTANCE;
            case HSSFCell.CELL_TYPE_ERROR:
                return ErrorEval.valueOf(cell.getErrorCellValue());
        }
        throw new RuntimeException("Unexpected cell type (" + cell.getCellType() + ")");
    }

    /**
     * Creates a Ref2DEval for ReferencePtg.
     * Non existent cells are treated as RefEvals containing BlankEval.
     */
    private static Ref2DEval createRef2DEval(ReferencePtg ptg, HSSFCell cell, 
            HSSFRow row, HSSFSheet sheet, HSSFWorkbook workbook) {
        if (cell == null) {
            return new Ref2DEval(ptg, BlankEval.INSTANCE);
        }
        
        switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_NUMERIC:
                return new Ref2DEval(ptg, new NumberEval(cell.getNumericCellValue()));
            case HSSFCell.CELL_TYPE_STRING:
                return new Ref2DEval(ptg, new StringEval(cell.getRichStringCellValue().getString()));
            case HSSFCell.CELL_TYPE_FORMULA:
                return new Ref2DEval(ptg, internalEvaluate(cell, row, sheet, workbook));
            case HSSFCell.CELL_TYPE_BOOLEAN:
                return new Ref2DEval(ptg, BoolEval.valueOf(cell.getBooleanCellValue()));
            case HSSFCell.CELL_TYPE_BLANK:
                return new Ref2DEval(ptg, BlankEval.INSTANCE);
            case HSSFCell.CELL_TYPE_ERROR:
                return new  Ref2DEval(ptg, ErrorEval.valueOf(cell.getErrorCellValue()));
        }
        throw new RuntimeException("Unexpected cell type (" + cell.getCellType() + ")");
    }

    /**
     * create a Ref3DEval for Ref3DPtg.
     */
    private static Ref3DEval createRef3DEval(Ref3DPtg ptg, HSSFCell cell, 
            HSSFRow row, HSSFSheet sheet, HSSFWorkbook workbook) {
        if (cell == null) {
            return new Ref3DEval(ptg, BlankEval.INSTANCE);
        }
        switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_NUMERIC:
                return new Ref3DEval(ptg, new NumberEval(cell.getNumericCellValue()));
            case HSSFCell.CELL_TYPE_STRING:
                return new Ref3DEval(ptg, new StringEval(cell.getRichStringCellValue().getString()));
            case HSSFCell.CELL_TYPE_FORMULA:
                return new Ref3DEval(ptg, internalEvaluate(cell, row, sheet, workbook));
            case HSSFCell.CELL_TYPE_BOOLEAN:
                return new Ref3DEval(ptg, BoolEval.valueOf(cell.getBooleanCellValue()));
            case HSSFCell.CELL_TYPE_BLANK:
                return new Ref3DEval(ptg, BlankEval.INSTANCE);
            case HSSFCell.CELL_TYPE_ERROR:
                return new Ref3DEval(ptg, ErrorEval.valueOf(cell.getErrorCellValue()));
        }
        throw new RuntimeException("Unexpected cell type (" + cell.getCellType() + ")");
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
        FormulaParser fp = new FormulaParser(formula, workbook);
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
