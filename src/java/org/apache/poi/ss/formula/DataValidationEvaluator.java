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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType;
import org.apache.poi.ss.usermodel.DataValidationConstraint.ValidationType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressBase;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.SheetUtil;

/**
 * Evaluates Data Validation constraints.<p>
 *
 * For performance reasons, this class keeps a cache of all previously retrieved {@link DataValidation} instances.  
 * Be sure to call {@link #clearAllCachedValues()} if any workbook validation definitions are 
 * added, modified, or deleted.
 * <p>
 * Changing cell values should be fine, as long as the corresponding {@link WorkbookEvaluator#clearAllCachedResultValues()}
 * is called as well.
 * 
 */
public class DataValidationEvaluator {

    /**
     * Expensive to compute, so cache them as they are retrieved.
     * <p>
     * Sheets don't implement equals, and since its an interface, 
     * there's no guarantee instances won't be recreated on the fly by some implementation.
     * So we use sheet name.
     */
    private final Map<String, List<? extends DataValidation>> validations = new HashMap<>();

    private final Workbook workbook;
    private final WorkbookEvaluator workbookEvaluator;

    /**
     * Use the same formula evaluation context used for other operations, so cell value
     * changes are automatically noticed
     * @param wb the workbook this operates on
     * @param provider provider for formula evaluation
     */
    public DataValidationEvaluator(Workbook wb, WorkbookEvaluatorProvider provider) {
        this.workbook = wb;
        this.workbookEvaluator = provider._getWorkbookEvaluator();
    }
    
    /**
     * @return evaluator
     */
    protected WorkbookEvaluator getWorkbookEvaluator() {
        return workbookEvaluator;
    }
    
    /**
     * Call this whenever validation structures change,
     * so future results stay in sync with the Workbook state.
     */
    public void clearAllCachedValues() {
        validations.clear();
    }
    
    /**
     * Lazy load validations by sheet, since reading the CT* types is expensive
     *
     * @param sheet The {@link Sheet} to load validations for.
     * @return The {@link DataValidation}s for the sheet
     */
    private List<? extends DataValidation> getValidations(Sheet sheet) {
        List<? extends DataValidation> dvs = validations.get(sheet.getSheetName());
        if (dvs == null && !validations.containsKey(sheet.getSheetName())) {
            dvs = sheet.getDataValidations();
            validations.put(sheet.getSheetName(), dvs);
        }
        return dvs;
    }
    
    /**
     * Finds and returns the {@link DataValidation} for the cell, if there is
     * one. Lookup is based on the first match from
     * {@link DataValidation#getRegions()} for the cell's sheet. DataValidation
     * regions must be in the same sheet as the DataValidation. Allowed values
     * expressions may reference other sheets, however.
     * 
     * @param cell reference to check - use this in case the cell does not actually exist yet
     * @return the DataValidation applicable to the given cell, or null if no
     *         validation applies
     */
    public DataValidation getValidationForCell(CellReference cell) {
        final DataValidationContext vc = getValidationContextForCell(cell);
        return vc == null ? null : vc.getValidation();
    }

    /**
     * Finds and returns the {@link DataValidationContext} for the cell, if there is
     * one. Lookup is based on the first match from
     * {@link DataValidation#getRegions()} for the cell's sheet. DataValidation
     * regions must be in the same sheet as the DataValidation. Allowed values
     * expressions may reference other sheets, however.
     * 
     * @param cell reference to check
     * @return the DataValidationContext applicable to the given cell, or null if no
     *         validation applies
     */
    public DataValidationContext getValidationContextForCell(CellReference cell) {
        final Sheet sheet = workbook.getSheet(cell.getSheetName());
        if (sheet == null) return null;
        final List<? extends DataValidation> dataValidations = getValidations(sheet);
        if (dataValidations == null) return null;
        for (DataValidation dv : dataValidations) {
            final CellRangeAddressList regions = dv.getRegions();
            if (regions == null) return null;
            // current implementation can't return null
            for (CellRangeAddressBase range : regions.getCellRangeAddresses()) {
                if (range.isInRange(cell)) {
                    return new DataValidationContext(dv, this, range, cell);
                }
            }
        }
        return null;
    }

    /**
     * If {@link #getValidationForCell(CellReference)} returns an instance, and the
     * {@link ValidationType} is {@link ValidationType#LIST}, return the valid
     * values, whether they are from a static list or cell range.
     * <p>
     * For all other validation types, or no validation at all, this method
     * returns null.
     * <p>
     * This method could throw an exception if the validation type is not LIST,
     * but since this method is mostly useful in UI contexts, null seems the
     * easier path.
     * 
     * @param cell reference to check - use this in case the cell does not actually exist yet
     * @return returns an unmodifiable {@link List} of {@link ValueEval}s if applicable, or
     *         null
     */
    public List<ValueEval> getValidationValuesForCell(CellReference cell) {
        DataValidationContext context = getValidationContextForCell(cell);

        if (context == null) return null;
        
        return getValidationValuesForConstraint(context);
    }

    /**
     * static so enums can reference it without creating a whole instance
     * @return returns an unmodifiable {@link List} of {@link ValueEval}s, which may be empty
     */
    protected static List<ValueEval> getValidationValuesForConstraint(DataValidationContext context) {
        final DataValidationConstraint val = context.getValidation().getValidationConstraint();
        if (val.getValidationType() != ValidationType.LIST) return null;
        
        String formula = val.getFormula1();
        
        final List<ValueEval> values = new ArrayList<>();
        
        if (val.getExplicitListValues() != null && val.getExplicitListValues().length > 0) {
            // assumes parsing interprets the overloaded property right for XSSF
            for (String s : val.getExplicitListValues()) {
                if (s != null) values.add(new StringEval(s)); // constructor throws exception on null
            }
        } else if (formula != null) {
            // evaluate formula for cell refs then get their values
            // note this should return the raw formula result, not the "unwrapped" version that returns a single value.
            ValueEval eval = context.getEvaluator().getWorkbookEvaluator().evaluateList(formula, context.getTarget(), context.getRegion());
            // formula is a StringEval if the validation is by a fixed list.  Use the explicit list later.
            // there is no way from the model to tell if the list is fixed values or formula based.
            if (eval instanceof TwoDEval) {
                TwoDEval twod = (TwoDEval) eval;
                for (int i=0; i < twod.getHeight(); i++) {
                    final ValueEval cellValue = twod.getValue(i,  0);
                    values.add(cellValue);
                }
            }
        }
        return Collections.unmodifiableList(values);
    }

    /**
     * Use the validation returned by {@link #getValidationForCell(CellReference)} if you
     * want the error display details. This is the validation checked by this
     * method, which attempts to replicate Excel's data validation rules.
     * <p>
     * Note that to properly apply some validations, care must be taken to
     * offset the base validation formula by the relative position of the
     * current cell, or the wrong value is checked.
     * 
     * @param cellRef The reference of the cell to evaluate
     * @return true if the cell has no validation or the cell value passes the
     *         defined validation, false if it fails
     */
    public boolean isValidCell(CellReference cellRef) {
        final DataValidationContext context = getValidationContextForCell(cellRef);

        if (context == null) return true;
        
        final Cell cell = SheetUtil.getCell(workbook.getSheet(cellRef.getSheetName()), cellRef.getRow(), cellRef.getCol());
        
        // now we can validate the cell
        
        // if empty, return not allowed flag
        if (   cell == null
            || isType(cell, CellType.BLANK)  
            || (isType(cell,CellType.STRING) 
                && (cell.getStringCellValue() == null || cell.getStringCellValue().isEmpty())
               )
           ) {
            return context.getValidation().getEmptyCellAllowed();
        }
        
        // cell has a value
        
        return ValidationEnum.isValid(cell, context);
    }

    /**
    * Note that this assumes the cell cached value is up to date and in sync with data edits
     *
    * @param cell The {@link Cell} to check.
    * @param type The {@link CellType} to check for.
    * @return true if the cell or cached cell formula result type match the given type
    */
    public static boolean isType(Cell cell, CellType type) {
        final CellType cellType = cell.getCellType();
        return cellType == type 
              || (cellType == CellType.FORMULA 
                  && cell.getCachedFormulaResultType() == type
                 );
    }
   

    /**
     * Not calling it ValidationType to avoid confusion for now with DataValidationConstraint.ValidationType.
     * Definition order matches OOXML type ID indexes
     */
    public static enum ValidationEnum {
        ANY {
            public boolean isValidValue(Cell cell, DataValidationContext context) {
                return true;
            }
        },
        INTEGER {
            public boolean isValidValue(Cell cell, DataValidationContext context) {
                if (super.isValidValue(cell, context)) {
                    // we know it is a number in the proper range, now check if it is an int
                    final double value = cell.getNumericCellValue(); // can't get here without a valid numeric value
                    return Double.compare(value, (int) value) == 0;
                }
                return false;
            }
        },
        DECIMAL,
        LIST {
            public boolean isValidValue(Cell cell, DataValidationContext context) {
                final List<ValueEval> valueList = getValidationValuesForConstraint(context);
                if (valueList == null) return true; // special case
                
                // compare cell value to each item
                for (ValueEval listVal : valueList) {
                    ValueEval comp = listVal instanceof RefEval ? ((RefEval) listVal).getInnerValueEval(context.getSheetIndex()) : listVal;
                    
                    // any value is valid if the list contains a blank value per Excel help
                    if (comp instanceof BlankEval) return true;
                    if (comp instanceof ErrorEval) continue; // nothing to check
                    if (comp instanceof BoolEval) {
                        if (isType(cell, CellType.BOOLEAN) && ((BoolEval) comp).getBooleanValue() == cell.getBooleanCellValue() ) {
                            return true;
                        } else {
                            continue; // check the rest
                        }
                    }
                    if (comp instanceof NumberEval) {
                        // could this have trouble with double precision/rounding errors and date/time values?
                        // do we need to allow a "close enough" double fractional range?
                        // I see 17 digits after the decimal separator in XSSF files, and for time values,
                        // there are sometimes discrepancies in the final decimal place.  
                        // I don't have a validation test case yet though. - GW
                        if (isType(cell, CellType.NUMERIC) && ((NumberEval) comp).getNumberValue() == cell.getNumericCellValue()) {
                            return true;
                        } else {
                            continue; // check the rest
                        }
                    }
                    if (comp instanceof StringEval) {
                        // interestingly, in testing, a validation value of the string "TRUE" or "true" 
                        // did not match a boolean cell value of TRUE - so apparently cell type matters
                        // also, Excel validation is case insensitive - "true" is valid for the list value "TRUE"
                        if (isType(cell, CellType.STRING) && ((StringEval) comp).getStringValue().equalsIgnoreCase(cell.getStringCellValue())) {
                            return true;
                        } else {
                            continue; // check the rest;
                        }
                    }
                }
                return false; // no matches
            }
        },
        DATE,
        TIME,
        TEXT_LENGTH {
            public boolean isValidValue(Cell cell, DataValidationContext context) {
                if (! isType(cell, CellType.STRING)) return false;
                String v = cell.getStringCellValue();
                return isValidNumericValue(Double.valueOf(v.length()), context);
            }
        },
        FORMULA {
            /**
             * Note the formula result must either be a boolean result, or anything not in error.
             * If boolean, value must be true to pass, anything else valid is also passing, errors fail.
             * @see org.apache.poi.ss.formula.DataValidationEvaluator.ValidationEnum#isValidValue(Cell, DataValidationContext)
             */
            public boolean isValidValue(Cell cell, DataValidationContext context) {
                // unwrapped single value
                ValueEval comp = context.getEvaluator().getWorkbookEvaluator().evaluate(context.getFormula1(), context.getTarget(), context.getRegion());
                if (comp instanceof RefEval) {
                    comp = ((RefEval) comp).getInnerValueEval(((RefEval) comp).getFirstSheetIndex());
                }

                if (comp instanceof BlankEval) return true;
                if (comp instanceof ErrorEval) return false;
                if (comp instanceof BoolEval) {
                    return ((BoolEval) comp).getBooleanValue();
                }
                // empirically tested in Excel - 0=false, any other number = true/valid
                // see test file DataValidationEvaluations.xlsx
                if (comp instanceof NumberEval) {
                    return ((NumberEval) comp).getNumberValue() != 0;
                }
                return false; // anything else is false, such as text
            }
        },
        ;
        
        public boolean isValidValue(Cell cell, DataValidationContext context) {
            return isValidNumericCell(cell, context);
        }
        
        /**
         * Uses the cell value, which may be the cached formula result value.
         * We won't re-evaluate cells here.  This validation would be after the cell value was updated externally.
         * Excel allows invalid values through methods like copy/paste, and only validates them when the user 
         * interactively edits the cell.   
         * @return if the cell is a valid numeric cell for the validation or not
         */
        protected boolean isValidNumericCell(Cell cell, DataValidationContext context) {
            if ( ! isType(cell, CellType.NUMERIC)) return false;

            Double value = Double.valueOf(cell.getNumericCellValue());
            return isValidNumericValue(value, context);
        }

        /**
         * Is the number a valid option for the validation?
         */
        protected boolean isValidNumericValue(Double value, final DataValidationContext context) {
            try {
                Double t1 = evalOrConstant(context.getFormula1(), context);
                // per Excel, a blank value for a numeric validation constraint formula validates true
                if (t1 == null) return true; 
                Double t2 = null;
                if (context.getOperator() == OperatorType.BETWEEN || context.getOperator() == OperatorType.NOT_BETWEEN) {
                    t2 = evalOrConstant(context.getFormula2(), context);
                    // per Excel, a blank value for a numeric validation constraint formula validates true
                    if (t2 == null) return true; 
                }
                return OperatorEnum.values()[context.getOperator()].isValid(value, t1, t2);
            } catch (NumberFormatException e) {
                // one or both formulas are in error, not evaluating to a number, so the validation is false per Excel's behavior.
                return false;
            }
        }
        
        /**
         * Evaluate a numeric formula value as either a constant or numeric expression.
         * Note that Excel treats validations with constraint formulas that evaluate to null as valid,
         * but evaluations in error or non-numeric are marked invalid.
         *
         * @param formula The text of the formula or a numeric value
         * @param context The {@link DataValidationContext} which is used for evaluation
         * @return numeric value or null if not defined or the formula evaluates to an empty/missing cell.
         * @throws NumberFormatException if the formula is non-numeric when it should be
         */
        private Double evalOrConstant(String formula, DataValidationContext context) throws NumberFormatException {
            if (formula == null || formula.trim().isEmpty()) return null; // shouldn't happen, but just in case
            try {
                return Double.valueOf(formula);
            } catch (NumberFormatException e) {
                // must be an expression, then.  Overloading by Excel in the file formats.
            }
            // note the call to the "unwrapped" version, which returns a single value
            ValueEval eval = context.getEvaluator().getWorkbookEvaluator().evaluate(formula, context.getTarget(), context.getRegion());
            if (eval instanceof RefEval) {
                eval = ((RefEval) eval).getInnerValueEval(((RefEval) eval).getFirstSheetIndex());
            }
            if (eval instanceof BlankEval) return null;
            if (eval instanceof NumberEval) return Double.valueOf(((NumberEval) eval).getNumberValue());
            if (eval instanceof StringEval) {
                final String value = ((StringEval) eval).getStringValue();
                if (value == null || value.trim().isEmpty()) return null; 
                // try to parse the cell value as a double and return it 
                return Double.valueOf(value);
            }
            throw new NumberFormatException("Formula '" + formula + "' evaluates to something other than a number");
        }
        
        /**
         * Validates against the type defined in context, as an index of the enum values array.
         * @param cell Cell to check validity of
         * @param context The Data Validation to check against
         * @return true if validation passes
         * @throws ArrayIndexOutOfBoundsException if the constraint type is an invalid index
         */
        public static boolean isValid(Cell cell, DataValidationContext context) {
            return values()[context.getValidation().getValidationConstraint().getValidationType()].isValidValue(cell, context);
        }
        
    }
    
    /**
     * Not calling it OperatorType to avoid confusion for now with DataValidationConstraint.OperatorType.
     * Definition order matches OOXML type ID indexes
     */
    public static enum OperatorEnum {
        BETWEEN {
            public boolean isValid(Double cellValue, Double v1, Double v2) {
                return cellValue.compareTo(v1) >= 0 && cellValue.compareTo(v2) <= 0;
            }
        },
        NOT_BETWEEN {
            public boolean isValid(Double cellValue, Double v1, Double v2) {
                return cellValue.compareTo(v1) < 0 || cellValue.compareTo(v2) > 0;
            }
        },
        EQUAL {
            public boolean isValid(Double cellValue, Double v1, Double v2) {
                return cellValue.compareTo(v1) == 0;
            }
        },
        NOT_EQUAL {
            public boolean isValid(Double cellValue, Double v1, Double v2) {
                return cellValue.compareTo(v1) != 0;
            }
        },
        GREATER_THAN {
            public boolean isValid(Double cellValue, Double v1, Double v2) {
                return cellValue.compareTo(v1) > 0;
            }
        },
        LESS_THAN {
            public boolean isValid(Double cellValue, Double v1, Double v2) {
                return cellValue.compareTo(v1) < 0;
            }
        },
        GREATER_OR_EQUAL {
            public boolean isValid(Double cellValue, Double v1, Double v2) {
                return cellValue.compareTo(v1) >= 0;
            }
        },
        LESS_OR_EQUAL {
            public boolean isValid(Double cellValue, Double v1, Double v2) {
                return cellValue.compareTo(v1) <= 0;
            }
        },
        ;
        
        public static final OperatorEnum IGNORED = BETWEEN;
        
        /**
         * Evaluates comparison using operator instance rules
         * @param cellValue won't be null, assumption is previous checks handled that
         * @param v1 if null, value assumed invalid, anything passes, per Excel behavior
         * @param v2 null if not needed.  If null when needed, assume anything passes, per Excel behavior
         * @return true if the comparison is valid
         */
        public abstract boolean isValid(Double cellValue, Double v1, Double v2);
    }
    
    /**
     * This class organizes and encapsulates all the pieces of information related to a single
     * data validation configuration for a single cell.  It cleanly separates the validation region,
     * the cells it applies to, the specific cell this instance references, and the validation
     * configuration and current values if applicable.
     */
    public static class DataValidationContext {
        private final DataValidation dv;
        private final DataValidationEvaluator dve;
        private final CellRangeAddressBase region;
        private final CellReference target;
        
        /**
         * Populate the context with the necessary values.
         */
        public DataValidationContext(DataValidation dv, DataValidationEvaluator dve, CellRangeAddressBase region, CellReference target) {
            this.dv = dv;
            this.dve = dve;
            this.region = region;
            this.target = target;
        }
        /**
         * @return the dv
         */
        public DataValidation getValidation() {
            return dv;
        }
        /**
         * @return the dve
         */
        public DataValidationEvaluator getEvaluator() {
            return dve;
        }
        /**
         * @return the region
         */
        public CellRangeAddressBase getRegion() {
            return region;
        }
        /**
         * @return the target
         */
        public CellReference getTarget() {
            return target;
        }
        
        public int getOffsetColumns() {
            return target.getCol() - region.getFirstColumn();
        }
        
        public int getOffsetRows() {
            return target.getRow() - region.getFirstRow();
        }
        
        public int getSheetIndex() {
            return dve.getWorkbookEvaluator().getSheetIndex(target.getSheetName());
        }
        
        public String getFormula1() {
            return dv.getValidationConstraint().getFormula1();
        }
        
        public String getFormula2() {
            return dv.getValidationConstraint().getFormula2();
        }
        
        public int getOperator() {
            return dv.getValidationConstraint().getOperator();
        }
        
    }
}
