/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.CacheAreaEval;
import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationSheet;
import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.CountUtils.I_MatchPredicate;
import org.apache.poi.ss.formula.functions.Countif.ErrorMatcher;

/**
 * Base class for SUMIFS() and COUNTIFS() functions, as they share much of the same logic, 
 * the difference being the source of the totals.
 */
/*package*/ abstract class Baseifs implements FreeRefFunction {

    /**
     * Implementations must be stateless.
     * @return true if there should be a range argument before the criteria pairs
     */
    protected abstract boolean hasInitialRange();

    /**
     * Implements the details of a specific aggregation function
     */
    protected static interface Aggregator {
        void addValue(ValueEval d);
        ValueEval getResult();
    }

    protected abstract Aggregator createAggregator();

    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        final boolean hasInitialRange = hasInitialRange();
        final int firstCriteria = hasInitialRange ? 1 : 0;

        if( args.length < (2+firstCriteria) || args.length % 2 != firstCriteria ) {
            return ErrorEval.VALUE_INVALID;
        }

        try {
            AreaEval sumRange = null;
            if (hasInitialRange) {
                sumRange = convertRangeArg(args[0]);
            }

            int numPairs = (args.length - firstCriteria) / 2;
            AreaEval[] ae = new AreaEval[numPairs];
            ValueEval[] criteriaArgs = new ValueEval[numPairs];
            for (int i = firstCriteria, k = 0; i < (args.length - 1); i += 2, k++) {
                ae[k] = convertRangeArg(args[i]);
                criteriaArgs[k] = args[i + 1];
            }

            validateCriteriaRanges(sumRange, ae);

            // A criteria argument may stand for several criteria (an array constant such as {1,2},
            // or in array context a multi-cell range such as D1:D3, see Countif.isArrayCriteria):
            // Excel then evaluates the function once per element and returns an array of the
            // results, which is what SUMPRODUCT(SUMIFS(sum,range,criteria_range)),
            // SUM(COUNTIFS(range,{v1,v2})) and MAX(COUNTIFS(...)) rely on. Several array criteria
            // are paired element-wise and must have the same shape.
            Boolean arrayContext = null; // looked up only when a criteria argument is an array at all
            for (int k = 0; k < numPairs; k++) {
                if (criteriaArgs[k] instanceof AreaEval crit && crit.getHeight() * crit.getWidth() > 1) {
                    if (arrayContext == null) {
                        arrayContext = isArrayContext(ec);
                    }
                    if (Countif.isArrayCriteria(crit, arrayContext)) {
                        return evaluateForEachCriterion(sumRange, ae, criteriaArgs, crit, arrayContext, ec);
                    }
                }
            }

            // All criteria are scalar — normal single-pass evaluation
            I_MatchPredicate[] mp = new I_MatchPredicate[numPairs];
            for (int k = 0; k < numPairs; k++) {
                mp[k] = Countif.createCriteriaPredicate(criteriaArgs[k], ec.getRowIndex(), ec.getColumnIndex());
            }
            validateCriteria(mp);
            return aggregateMatchingCells(createAggregator(), sumRange, ae, mp);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    /**
     * @return whether the function is evaluated in array context: its result feeds an array-mode
     * function such as SUMPRODUCT, or the formula cell is part of an array formula
     * @since 6.0.0
     */
    /* package */ static boolean isArrayContext(OperationEvaluationContext ec) {
        if (ec.isArraymode()) {
            return true;
        }
        if (ec.getWorkbook() == null || ec.getSheetIndex() < 0) {
            // a context without a cell (tests, conditional formatting, data validation)
            return false;
        }
        EvaluationSheet sheet = ec.getWorkbook().getSheet(ec.getSheetIndex());
        EvaluationCell cell = sheet.getCell(ec.getRowIndex(), ec.getColumnIndex());
        return cell != null && cell.isPartOfArrayFormulaGroup();
    }

    private ValueEval evaluateForEachCriterion(AreaEval sumRange, AreaEval[] ranges, ValueEval[] criteriaArgs,
            AreaEval shape, boolean arrayContext, OperationEvaluationContext ec) throws EvaluationException {
        int height = shape.getHeight();
        int width = shape.getWidth();
        for (ValueEval criteria : criteriaArgs) {
            if (Countif.isArrayCriteria(criteria, arrayContext)
                    && (((AreaEval) criteria).getHeight() != height || ((AreaEval) criteria).getWidth() != width)) {
                throw EvaluationException.invalidValue();
            }
        }
        ValueEval[] results = new ValueEval[height * width];
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                I_MatchPredicate[] mp = new I_MatchPredicate[criteriaArgs.length];
                for (int j = 0; j < criteriaArgs.length; j++) {
                    ValueEval criterion = Countif.isArrayCriteria(criteriaArgs[j], arrayContext)
                            ? ((AreaEval) criteriaArgs[j]).getRelativeValue(r, c) : criteriaArgs[j];
                    mp[j] = Countif.createCriteriaPredicate(criterion, ec.getRowIndex(), ec.getColumnIndex());
                }
                ValueEval result;
                try {
                    validateCriteria(mp);
                    result = aggregateMatchingCells(createAggregator(), sumRange, ranges, mp);
                } catch (EvaluationException e) {
                    result = e.getErrorEval();
                }
                results[r * width + c] = result;
            }
        }
        return new CacheAreaEval(shape.getFirstRow(), shape.getFirstColumn(), shape.getLastRow(), shape.getLastColumn(), results);
    }

    /**
     * Verify that each <code>criteriaRanges</code> argument contains the same number of rows and columns
     * including the <code>sumRange</code> argument if present
     * @param sumRange if used, it must match the shape of the criteriaRanges
     * @param criteriaRanges to check
     * @throws EvaluationException if the ranges do not match.
     */
    private static void validateCriteriaRanges(AreaEval sumRange, AreaEval[] criteriaRanges) throws EvaluationException {
        int h = criteriaRanges[0].getHeight();
        int w = criteriaRanges[0].getWidth();

        if (sumRange != null
                && (sumRange.getHeight() != h
                || sumRange.getWidth() != w) ) {
            throw EvaluationException.invalidValue();
        }

        for(AreaEval r : criteriaRanges){
            if(r.getHeight() != h ||
                    r.getWidth() != w ) {
                throw EvaluationException.invalidValue();
            }
        }
    }

    /**
     * Verify that each <code>criteria</code> predicate is valid, i.e. not an error
     * @param criteria to check
     *
     * @throws EvaluationException if there are criteria which resulted in Errors.
     */
    private static void validateCriteria(I_MatchPredicate[] criteria) throws EvaluationException {
        for(I_MatchPredicate predicate : criteria) {

            // check for errors in predicate and return immediately using this error code
            if(predicate instanceof ErrorMatcher errorMatcher) {
                throw new EvaluationException(ErrorEval.valueOf(errorMatcher.getValue()));
            }
        }
    }


    /**
     * @param sumRange  the range to sum, if used (uses 1 for each match if not present)
     * @param ranges  criteria ranges
     * @param predicates  array of predicates, a predicate for each value in <code>ranges</code>
     * @return the computed value
     * @throws EvaluationException if there is an issue with eval
     */
    private static ValueEval aggregateMatchingCells(Aggregator aggregator, AreaEval sumRange, AreaEval[] ranges, I_MatchPredicate[] predicates)
            throws EvaluationException {
        int height = ranges[0].getHeight();
        int width = ranges[0].getWidth();

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {

                boolean matches = true;
                for(int i = 0; i < ranges.length; i++){
                    AreaEval aeRange = ranges[i];
                    I_MatchPredicate mp = predicates[i];

                    // Bugs 60858 and 56420 show predicate can be null
                    if (mp == null || !mp.matches(aeRange.getRelativeValue(r, c))) {
                        matches = false;
                        break;
                    }
                }

                if(matches) { // aggregate only if all of the corresponding criteria specified are true for that cell.
                    if(sumRange != null) {
                        ValueEval value = sumRange.getRelativeValue(r, c);
                        if (value instanceof ErrorEval errorEval) {
                            throw new EvaluationException(errorEval);
                        }
                        aggregator.addValue(value);
                    } else {
                        aggregator.addValue(null);
                    }
                }
            }
        }
        return aggregator.getResult();
    }

    protected static AreaEval convertRangeArg(ValueEval eval) throws EvaluationException {
        if (eval instanceof AreaEval areaEval) {
            return areaEval;
        }
        if (eval instanceof RefEval refEval) {
            return refEval.offset(0, 0, 0, 0);
        }
        throw new EvaluationException(ErrorEval.VALUE_INVALID);
    }

}
