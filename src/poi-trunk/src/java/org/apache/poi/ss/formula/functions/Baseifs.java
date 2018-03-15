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
import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
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
            
            // collect pairs of ranges and criteria
            AreaEval[] ae = new AreaEval[(args.length - firstCriteria)/2];
            I_MatchPredicate[] mp = new I_MatchPredicate[ae.length];
            for(int i = firstCriteria, k=0; i < args.length; i += 2, k++){
                ae[k] = convertRangeArg(args[i]);
                
                mp[k] = Countif.createCriteriaPredicate(args[i+1], ec.getRowIndex(), ec.getColumnIndex());
            }

            validateCriteriaRanges(sumRange, ae);
            validateCriteria(mp);

            double result = aggregateMatchingCells(sumRange, ae, mp);
            return new NumberEval(result);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
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
            if(predicate instanceof ErrorMatcher) {
                throw new EvaluationException(ErrorEval.valueOf(((ErrorMatcher)predicate).getValue()));
            }
        }
    }


    /**
     * @param sumRange  the range to sum, if used (uses 1 for each match if not present)
     * @param ranges  criteria ranges
     * @param predicates  array of predicates, a predicate for each value in <code>ranges</code>
     * @return the computed value
     */
    private static double aggregateMatchingCells(AreaEval sumRange, AreaEval[] ranges, I_MatchPredicate[] predicates) {
        int height = ranges[0].getHeight();
        int width = ranges[0].getWidth();

        double result = 0.0;
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

                if(matches) { // sum only if all of the corresponding criteria specified are true for that cell.
                    result += accumulate(sumRange, r, c);
                }
            }
        }
        return result;
    }

    /**
     * For counts, this would return 1, for sums it returns a cell value or zero.
     * This is only called after all the criteria are confirmed true for the coordinates.
     * @param sumRange if used
     * @param relRowIndex
     * @param relColIndex
     * @return the aggregate input value corresponding to the given range coordinates
     */
    private static double accumulate(AreaEval sumRange, int relRowIndex, int relColIndex) {
        if (sumRange == null) return 1.0; // count
        
        ValueEval addend = sumRange.getRelativeValue(relRowIndex, relColIndex);
        if (addend instanceof NumberEval) {
            return ((NumberEval)addend).getNumberValue();
        }
        // everything else (including string and boolean values) counts as zero
        return 0.0;

    }

    protected static AreaEval convertRangeArg(ValueEval eval) throws EvaluationException {
        if (eval instanceof AreaEval) {
            return (AreaEval) eval;
        }
        if (eval instanceof RefEval) {
            return ((RefEval)eval).offset(0, 0, 0, 0);
        }
        throw new EvaluationException(ErrorEval.VALUE_INVALID);
    }

}
