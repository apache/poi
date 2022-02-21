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

package org.apache.poi.ss.formula.atp;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.TwoDEval;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.formula.functions.ArrayFunction;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.functions.LookupUtils;

/**
 * Implementation of Excel function XLOOKUP()
 *
 * <b>Syntax</b><br>
 * <b>XLOOKUP</b>(<b>lookup_value</b>, <b>lookup_array</b>, <b>return_array</b>, <b>[if_not_found]</b>, <b>[match_mode]</b>, <b>[search_mode]</b>)<p>
 *
 * https://support.microsoft.com/en-us/office/xlookup-function-b7fd680e-6d10-43e6-84f9-88eae8bf5929
 * 
 * @since POI 5.2.0
 */
final class XLookupFunction implements FreeRefFunction, ArrayFunction {

    public static final FreeRefFunction instance = new XLookupFunction(ArgumentsEvaluator.instance);

    private final ArgumentsEvaluator evaluator;

    private XLookupFunction(ArgumentsEvaluator anEvaluator) {
        // enforces singleton
        this.evaluator = anEvaluator;
    }

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        int srcRowIndex = ec.getRowIndex();
        int srcColumnIndex = ec.getColumnIndex();
        return _evaluate(args, srcRowIndex, srcColumnIndex);
    }

    @Override
    public ValueEval evaluateArray(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        return _evaluate(args, srcRowIndex, srcColumnIndex);
    }

    private ValueEval _evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        if (args.length < 3) {
            return ErrorEval.VALUE_INVALID;
        }
        ValueEval notFound = BlankEval.instance;
        if (args.length > 3) {
            try {
                ValueEval notFoundValue = OperandResolver.getSingleValue(args[3], srcRowIndex, srcColumnIndex);
                if (notFoundValue != null) {
                    notFound = notFoundValue;
                }
            } catch (EvaluationException e) {
                return e.getErrorEval();
            }
        }
        LookupUtils.MatchMode matchMode = LookupUtils.MatchMode.ExactMatch;
        if (args.length > 4) {
            try {
                ValueEval matchModeValue = OperandResolver.getSingleValue(args[4], srcRowIndex, srcColumnIndex);
                int matchInt = OperandResolver.coerceValueToInt(matchModeValue);
                matchMode = LookupUtils.matchMode(matchInt);
            } catch (EvaluationException e) {
                return e.getErrorEval();
            } catch (Exception e) {
                return ErrorEval.VALUE_INVALID;
            }
        }
        LookupUtils.SearchMode searchMode = LookupUtils.SearchMode.IterateForward;
        if (args.length > 5) {
            try {
                ValueEval searchModeValue = OperandResolver.getSingleValue(args[5], srcRowIndex, srcColumnIndex);
                int searchInt = OperandResolver.coerceValueToInt(searchModeValue);
                searchMode = LookupUtils.searchMode(searchInt);
            } catch (EvaluationException e) {
                return e.getErrorEval();
            } catch (Exception e) {
                return ErrorEval.VALUE_INVALID;
            }
        }
        return evaluate(srcRowIndex, srcColumnIndex, args[0], args[1], args[2], notFound, matchMode, searchMode);
    }

    private ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval lookupEval, ValueEval indexEval,
                               ValueEval returnEval, ValueEval notFound, LookupUtils.MatchMode matchMode,
                               LookupUtils.SearchMode searchMode) {
        try {
            ValueEval lookupValue = OperandResolver.getSingleValue(lookupEval, srcRowIndex, srcColumnIndex);
            TwoDEval tableArray = LookupUtils.resolveTableArrayArg(indexEval);
            LookupUtils.ValueVector vector;
            if (tableArray.isColumn()) {
                vector = LookupUtils.createColumnVector(tableArray, 0);
            } else {
                vector = LookupUtils.createRowVector(tableArray, 0);
            }
            int matchedIdx;
            try {
                matchedIdx = LookupUtils.xlookupIndexOfValue(lookupValue, vector, matchMode, searchMode);
            } catch (EvaluationException e) {
                if (ErrorEval.NA.equals(e.getErrorEval())) {
                    if (notFound != BlankEval.instance) {
                        if (returnEval instanceof AreaEval) {
                            AreaEval area = (AreaEval)returnEval;
                            int width = area.getWidth();
                            if (width <= 1) {
                                return notFound;
                            }
                            return notFoundAreaEval(notFound, width);
                        } else {
                            return notFound;
                        }
                    }
                    return ErrorEval.NA;
                } else {
                    return e.getErrorEval();
                }
            }
            if (returnEval instanceof AreaEval) {
                AreaEval area = (AreaEval)returnEval;
                if (tableArray.isColumn()) {
                    return area.offset(matchedIdx, matchedIdx,0, area.getWidth() - 1);
                } else {
                    return area.offset(0, area.getHeight() - 1, matchedIdx, matchedIdx);
                }
            } else {
                return returnEval;
            }
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    private AreaEval notFoundAreaEval(ValueEval notFound, int width) {
        return new AreaEval() {
            @Override
            public int getFirstRow() {
                return 0;
            }

            @Override
            public int getLastRow() {
                return 0;
            }

            @Override
            public int getFirstColumn() {
                return 0;
            }

            @Override
            public int getLastColumn() {
                return width - 1;
            }

            @Override
            public ValueEval getAbsoluteValue(int row, int col) {
                if (col == 0) {
                    return notFound;
                }
                return new StringEval("");
            }

            @Override
            public boolean contains(int row, int col) {
                return containsRow(row) && containsColumn(col);
            }

            @Override
            public boolean containsColumn(int col) {
                return col < width;
            }

            @Override
            public boolean containsRow(int row) {
                return row == 0;
            }

            @Override
            public int getWidth() {
                return width;
            }

            @Override
            public int getHeight() {
                return 1;
            }

            @Override
            public ValueEval getRelativeValue(int relativeRowIndex, int relativeColumnIndex) {
                return getAbsoluteValue(relativeRowIndex, relativeColumnIndex);
            }

            @Override
            public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx, int relLastColIx) {
                return null;
            }

            @Override
            public ValueEval getValue(int sheetIndex, int rowIndex, int columnIndex) {
                return getAbsoluteValue(rowIndex, columnIndex);
            }

            @Override
            public int getFirstSheetIndex() {
                return 0;
            }

            @Override
            public int getLastSheetIndex() {
                return 0;
            }

            @Override
            public ValueEval getValue(int rowIndex, int columnIndex) {
                return getAbsoluteValue(rowIndex, columnIndex);
            }

            @Override
            public boolean isColumn() {
                return false;
            }

            @Override
            public TwoDEval getRow(int rowIndex) {
                return null;
            }

            @Override
            public TwoDEval getColumn(int columnIndex) {
                return null;
            }

            @Override
            public boolean isSubTotal(int rowIndex, int columnIndex) {
                return false;
            }

            @Override
            public boolean isRowHidden(int rowIndex) {
                return false;
            }
        };
    }
}
