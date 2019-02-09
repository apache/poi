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

package org.apache.poi.ss.formula.functions;

import java.util.function.Supplier;

import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.formula.eval.NumericValueEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.StringValueEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.util.NumberComparer;

/**
 * This class performs a D* calculation. It takes an {@link IDStarAlgorithm} object and
 * uses it for calculating the result value. Iterating a database and checking the
 * entries against the set of conditions is done here.
 *
 * TODO:
 * - wildcards ? and * in string conditions
 * - functions as conditions
 */
public final class DStarRunner implements Function3Arg {
    /**
     * Enum for convenience to identify and source implementations of the D* functions
     */
    public enum DStarAlgorithmEnum {
        /** @see DGet */
        DGET(DGet::new),
        /** @see DMin */
        DMIN(DMin::new),
        /** @see DMax */
        DMAX(DMax::new),
        /** @see DSum */
        DSUM(DSum::new),
        ;
        
        private final Supplier<IDStarAlgorithm> implSupplier;

        private DStarAlgorithmEnum(Supplier<IDStarAlgorithm> implSupplier) {
            this.implSupplier = implSupplier;
        }
        
        /**
         * @return a new function implementation instance
         */
        public IDStarAlgorithm newInstance() {
            return implSupplier.get();
        }
    }
    private final DStarAlgorithmEnum algoType;

    /**
     * @param algorithm to implement
     */
    public DStarRunner(DStarAlgorithmEnum algorithm) {
        this.algoType = algorithm;
    }

    public final ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        if(args.length == 3) {
            return evaluate(srcRowIndex, srcColumnIndex, args[0], args[1], args[2]);
        }
        else {
            return ErrorEval.VALUE_INVALID;
        }
    }

    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex,
            ValueEval database, ValueEval filterColumn, ValueEval conditionDatabase) {
        // Input processing and error checks.
        if(!(database instanceof AreaEval) || !(conditionDatabase instanceof AreaEval)) {
            return ErrorEval.VALUE_INVALID;
        }
        AreaEval db = (AreaEval)database;
        AreaEval cdb = (AreaEval)conditionDatabase;
        
        try {
            filterColumn = OperandResolver.getSingleValue(filterColumn, srcRowIndex, srcColumnIndex);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }

        int fc;
        try {
            fc = getColumnForName(filterColumn, db);
        }
        catch (EvaluationException e) {
            return ErrorEval.VALUE_INVALID;
        }
        if(fc == -1) { // column not found
            return ErrorEval.VALUE_INVALID;
        }

        // Create an algorithm runner.
        IDStarAlgorithm algorithm = algoType.newInstance();

        // Iterate over all DB entries.
        final int height = db.getHeight();
        for(int row = 1; row < height; ++row) {
            boolean matches;
            try {
                matches = fullfillsConditions(db, row, cdb);
            }
            catch (EvaluationException e) {
                return ErrorEval.VALUE_INVALID;
            }
            // Filter each entry.
            if(matches) {
                ValueEval currentValueEval = resolveReference(db, row, fc);
                // Pass the match to the algorithm and conditionally abort the search.
                boolean shouldContinue = algorithm.processMatch(currentValueEval);
                if(! shouldContinue) {
                    break;
                }
            }
        }

        // Return the result of the algorithm.
        return algorithm.getResult();
    }

    private enum operator {
        largerThan,
        largerEqualThan,
        smallerThan,
        smallerEqualThan,
        equal
    }

    /**
     * 
     *
     * @param nameValueEval Must not be a RefEval or AreaEval. Thus make sure resolveReference() is called on the value first!
     * @param db Database
     * @return Corresponding column number.
     * @throws EvaluationException If it's not possible to turn all headings into strings.
     */
    private static int getColumnForName(ValueEval nameValueEval, AreaEval db)
            throws EvaluationException {
        String name = OperandResolver.coerceValueToString(nameValueEval);
        return getColumnForString(db, name);
    }

    /**
     * For a given database returns the column number for a column heading.
     * Comparison is case-insensitive.
     *
     * @param db Database.
     * @param name Column heading.
     * @return Corresponding column number.
     * @throws EvaluationException If it's not possible to turn all headings into strings.
     */
    private static int getColumnForString(AreaEval db,String name)
            throws EvaluationException {
        int resultColumn = -1;
        final int width = db.getWidth();
        for(int column = 0; column < width; ++column) {
            ValueEval columnNameValueEval = resolveReference(db, 0, column);
            if(columnNameValueEval instanceof BlankEval) {
                continue;
            }
            if(columnNameValueEval instanceof ErrorEval) {
                continue;
            }
            String columnName = OperandResolver.coerceValueToString(columnNameValueEval);
            if(name.equalsIgnoreCase(columnName)) {
                resultColumn = column;
                break;
            }
        }
        return resultColumn;
    }

    /**
     * Checks a row in a database against a condition database.
     *
     * @param db Database.
     * @param row The row in the database to check.
     * @param cdb The condition database to use for checking.
     * @return Whether the row matches the conditions.
     * @throws EvaluationException If references could not be resolved or comparison
     * operators and operands didn't match.
     */
    private static boolean fullfillsConditions(AreaEval db, int row, AreaEval cdb)
            throws EvaluationException {
        // Only one row must match to accept the input, so rows are ORed.
        // Each row is made up of cells where each cell is a condition,
        // all have to match, so they are ANDed.
        final int height = cdb.getHeight();
        for(int conditionRow = 1; conditionRow < height; ++conditionRow) {
            boolean matches = true;
            final int width = cdb.getWidth();
            for(int column = 0; column < width; ++column) { // columns are ANDed
                // Whether the condition column matches a database column, if not it's a
                // special column that accepts formulas.
                boolean columnCondition = true;
                ValueEval condition;
                
                // The condition to apply.
                condition = resolveReference(cdb, conditionRow, column);
                
                // If the condition is empty it matches.
                if(condition instanceof BlankEval)
                    continue;
                // The column in the DB to apply the condition to.
                ValueEval targetHeader = resolveReference(cdb, 0, column);

                if(!(targetHeader instanceof StringValueEval)) {
                    throw new EvaluationException(ErrorEval.VALUE_INVALID);
                }
                
                if (getColumnForName(targetHeader, db) == -1)
                    // No column found, it's again a special column that accepts formulas.
                    columnCondition = false;

                if(columnCondition) { // normal column condition
                    // Should not throw, checked above.
                    ValueEval value = resolveReference(db, row, getColumnForName(targetHeader, db));
                    if(!testNormalCondition(value, condition)) {
                        matches = false;
                        break;
                    }
                } else { // It's a special formula condition.
                    // TODO: Check whether the condition cell contains a formula and return #VALUE! if it doesn't.
                    if(OperandResolver.coerceValueToString(condition).isEmpty()) {
                        throw new EvaluationException(ErrorEval.VALUE_INVALID);
                    }
                    throw new NotImplementedException(
                            "D* function with formula conditions");
                }
            }
            if (matches) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test a value against a simple (< > <= >= = starts-with) condition string.
     *
     * @param value The value to check.
     * @param condition The condition to check for.
     * @return Whether the condition holds.
     * @throws EvaluationException If comparison operator and operands don't match.
     */
    private static boolean testNormalCondition(ValueEval value, ValueEval condition)
            throws EvaluationException {
        if(condition instanceof StringEval) {
            String conditionString = ((StringEval)condition).getStringValue();
        
            if(conditionString.startsWith("<")) { // It's a </<= condition.
                String number = conditionString.substring(1);
                if(number.startsWith("=")) {
                    number = number.substring(1);
                    return testNumericCondition(value, operator.smallerEqualThan, number);
                } else {
                    return testNumericCondition(value, operator.smallerThan, number);
                }
            } else if(conditionString.startsWith(">")) { // It's a >/>= condition.
                String number = conditionString.substring(1);
                if(number.startsWith("=")) {
                    number = number.substring(1);
                    return testNumericCondition(value, operator.largerEqualThan, number);
                } else {
                    return testNumericCondition(value, operator.largerThan, number);
                }
            } else if(conditionString.startsWith("=")) { // It's a = condition.
                String stringOrNumber = conditionString.substring(1);

                if(stringOrNumber.isEmpty()) {
                    return value instanceof BlankEval;
                }
                // Distinguish between string and number.
                boolean itsANumber;
                try {
                    Integer.parseInt(stringOrNumber);
                    itsANumber = true;
                } catch (NumberFormatException e) { // It's not an int.
                    try {
                        Double.parseDouble(stringOrNumber);
                        itsANumber = true;
                    } catch (NumberFormatException e2) { // It's a string.
                        itsANumber = false;
                    }
                }
                if(itsANumber) {
                    return testNumericCondition(value, operator.equal, stringOrNumber);
                } else { // It's a string.
                    String valueString = value instanceof BlankEval ? "" : OperandResolver.coerceValueToString(value);
                    return stringOrNumber.equals(valueString);
                }
            } else { // It's a text starts-with condition.
                if(conditionString.isEmpty()) {
                    return value instanceof StringEval;
                }
                else {
                    String valueString = value instanceof BlankEval ? "" : OperandResolver.coerceValueToString(value);
                    return valueString.startsWith(conditionString);
                }
            }
        } else if(condition instanceof NumericValueEval) {
            double conditionNumber = ((NumericValueEval) condition).getNumberValue();
            Double valueNumber = getNumberFromValueEval(value);
            return valueNumber != null && conditionNumber == valueNumber;
        } else if(condition instanceof ErrorEval) {
            if(value instanceof ErrorEval) {
                return ((ErrorEval)condition).getErrorCode() == ((ErrorEval)value).getErrorCode();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Test whether a value matches a numeric condition.
     * @param valueEval Value to check.
     * @param op Comparator to use.
     * @param condition Value to check against.
     * @return whether the condition holds.
     * @throws EvaluationException If it's impossible to turn the condition into a number.
     */
    private static boolean testNumericCondition(
            ValueEval valueEval, operator op, String condition)
            throws EvaluationException {
        // Construct double from ValueEval.
        if(!(valueEval instanceof NumericValueEval))
            return false;
        double value = ((NumericValueEval)valueEval).getNumberValue();

        // Construct double from condition.
        double conditionValue;
        try {
            conditionValue = Integer.parseInt(condition);
        } catch (NumberFormatException e) { // It's not an int.
            try {
                conditionValue = Double.parseDouble(condition);
            } catch (NumberFormatException e2) { // It's not a double.
                throw new EvaluationException(ErrorEval.VALUE_INVALID);
            }
        }

        int result = NumberComparer.compare(value, conditionValue);
        switch(op) {
        case largerThan:
            return result > 0;
        case largerEqualThan:
            return result >= 0;
        case smallerThan:
            return result < 0;
        case smallerEqualThan:
            return result <= 0;
        case equal:
            return result == 0;
        }
        return false; // Can not be reached.
    }
    
    private static Double getNumberFromValueEval(ValueEval value) {
        if(value instanceof NumericValueEval) {
            return ((NumericValueEval)value).getNumberValue();
        }
        else if(value instanceof StringValueEval) {
            String stringValue = ((StringValueEval)value).getStringValue();
            try {
                return Double.parseDouble(stringValue);
            } catch (NumberFormatException e2) {
                return null;
            }
        }
        else {
            return null;
        }
    }
    
    /**
     * Resolve a ValueEval that's in an AreaEval.
     *
     * @param db AreaEval from which the cell to resolve is retrieved. 
     * @param dbRow Relative row in the AreaEval.
     * @param dbCol Relative column in the AreaEval.
     * @return A ValueEval that is a NumberEval, StringEval, BoolEval, BlankEval or ErrorEval.
     */
    private static ValueEval resolveReference(AreaEval db, int dbRow, int dbCol) {
        try {
            return OperandResolver.getSingleValue(db.getValue(dbRow, dbCol), db.getFirstRow()+dbRow, db.getFirstColumn()+dbCol);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }
}
