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

import org.apache.poi.ss.formula.TwoDEval;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.formula.eval.NumericValueEval;
import org.apache.poi.ss.formula.eval.RefEval;
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
    public enum DStarAlgorithmEnum {
        DGET,
        DMIN,
        // DMAX, // DMAX is not yet implemented
    }
    private final DStarAlgorithmEnum algoType;

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
        if(!(database instanceof TwoDEval) || !(conditionDatabase instanceof TwoDEval)) {
            return ErrorEval.VALUE_INVALID;
        }
        TwoDEval db = (TwoDEval)database;
        TwoDEval cdb = (TwoDEval)conditionDatabase;

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
        IDStarAlgorithm algorithm = null;
        switch(algoType) {
            case DGET: algorithm = new DGet(); break;
            case DMIN: algorithm = new DMin(); break;
        }

        // Iterate over all DB entries.
        for(int row = 1; row < db.getHeight(); ++row) {
            boolean matches = true;
            try {
                matches = fullfillsConditions(db, row, cdb);
            }
            catch (EvaluationException e) {
                return ErrorEval.VALUE_INVALID;
            }
            // Filter each entry.
            if(matches) {
                try {
                    ValueEval currentValueEval = solveReference(db.getValue(row, fc));
                    // Pass the match to the algorithm and conditionally abort the search.
                    boolean shouldContinue = algorithm.processMatch(currentValueEval);
                    if(! shouldContinue) {
                        break;
                    }
                } catch (EvaluationException e) {
                    return e.getErrorEval();
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
     * Resolve reference(-chains) until we have a normal value.
     *
     * @param field a ValueEval which can be a RefEval.
     * @return a ValueEval which is guaranteed not to be a RefEval
     * @throws EvaluationException If a multi-sheet reference was found along the way.
     */
    private static ValueEval solveReference(ValueEval field) throws EvaluationException {
        if (field instanceof RefEval) {
            RefEval refEval = (RefEval)field;
            if (refEval.getNumberOfSheets() > 1) {
                throw new EvaluationException(ErrorEval.VALUE_INVALID);
            }
            return solveReference(refEval.getInnerValueEval(refEval.getFirstSheetIndex()));
        }
        else {
            return field;
        }
    }

    /**
     * Returns the first column index that matches the given name. The name can either be
     * a string or an integer, when it's an integer, then the respective column
     * (1 based index) is returned.
     * @param nameValueEval
     * @param db
     * @return the first column index that matches the given name (or int)
     * @throws EvaluationException
     */
    @SuppressWarnings("unused")
    private static int getColumnForTag(ValueEval nameValueEval, TwoDEval db)
            throws EvaluationException {
        int resultColumn = -1;

        // Numbers as column indicator are allowed, check that.
        if(nameValueEval instanceof NumericValueEval) {
            double doubleResultColumn = ((NumericValueEval)nameValueEval).getNumberValue();
            resultColumn = (int)doubleResultColumn;
            // Floating comparisions are usually not possible, but should work for 0.0.
            if(doubleResultColumn - resultColumn != 0.0)
                throw new EvaluationException(ErrorEval.VALUE_INVALID);
            resultColumn -= 1; // Numbers are 1-based not 0-based.
        } else {
            resultColumn = getColumnForName(nameValueEval, db);
        }
        return resultColumn;
    }

    private static int getColumnForName(ValueEval nameValueEval, TwoDEval db)
            throws EvaluationException {
        String name = getStringFromValueEval(nameValueEval);
        return getColumnForString(db, name);
    }

    /**
     * For a given database returns the column number for a column heading.
     *
     * @param db Database.
     * @param name Column heading.
     * @return Corresponding column number.
     * @throws EvaluationException If it's not possible to turn all headings into strings.
     */
    private static int getColumnForString(TwoDEval db,String name)
            throws EvaluationException {
        int resultColumn = -1;
        for(int column = 0; column < db.getWidth(); ++column) {
            ValueEval columnNameValueEval = db.getValue(0, column);
            String columnName = getStringFromValueEval(columnNameValueEval);
            if(name.equals(columnName)) {
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
    private static boolean fullfillsConditions(TwoDEval db, int row, TwoDEval cdb)
            throws EvaluationException {
        // Only one row must match to accept the input, so rows are ORed.
        // Each row is made up of cells where each cell is a condition,
        // all have to match, so they are ANDed.
        for(int conditionRow = 1; conditionRow < cdb.getHeight(); ++conditionRow) {
            boolean matches = true;
            for(int column = 0; column < cdb.getWidth(); ++column) { // columns are ANDed
                // Whether the condition column matches a database column, if not it's a
                // special column that accepts formulas.
                boolean columnCondition = true;
                ValueEval condition = null;
                try {
                    // The condition to apply.
                    condition = solveReference(cdb.getValue(conditionRow, column));
                } catch (java.lang.RuntimeException e) {
                    // It might be a special formula, then it is ok if it fails.
                    columnCondition = false;
                }
                // If the condition is empty it matches.
                if(condition instanceof BlankEval)
                    continue;
                // The column in the DB to apply the condition to.
                ValueEval targetHeader = solveReference(cdb.getValue(0, column));
                targetHeader = solveReference(targetHeader);


                if(!(targetHeader instanceof StringValueEval)) {
                    throw new EvaluationException(ErrorEval.VALUE_INVALID);
                }
                
                if (getColumnForName(targetHeader, db) == -1)
                    // No column found, it's again a special column that accepts formulas.
                    columnCondition = false;

                if(columnCondition == true) { // normal column condition
                    // Should not throw, checked above.
                    ValueEval value = db.getValue(
                            row, getColumnForName(targetHeader, db));
                    if(!testNormalCondition(value, condition)) {
                        matches = false;
                        break;
                    }
                } else { // It's a special formula condition.
                    if(getStringFromValueEval(condition).isEmpty()) {
                        throw new EvaluationException(ErrorEval.VALUE_INVALID);
                    }
                    throw new NotImplementedException(
                            "D* function with formula conditions");
                }
            }
            if (matches == true) {
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
            }
            else if(conditionString.startsWith(">")) { // It's a >/>= condition.
                String number = conditionString.substring(1);
                if(number.startsWith("=")) {
                    number = number.substring(1);
                    return testNumericCondition(value, operator.largerEqualThan, number);
                } else {
                    return testNumericCondition(value, operator.largerThan, number);
                }
            }
            else if(conditionString.startsWith("=")) { // It's a = condition.
                String stringOrNumber = conditionString.substring(1);

                if(stringOrNumber.isEmpty()) {
                    return value instanceof BlankEval;
                }
                // Distinguish between string and number.
                boolean itsANumber = false;
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
                    String valueString = value instanceof BlankEval ? "" : getStringFromValueEval(value);
                    return stringOrNumber.equals(valueString);
                }
            } else { // It's a text starts-with condition.
                if(conditionString.isEmpty()) {
                    return value instanceof StringEval;
                }
                else {
                    String valueString = value instanceof BlankEval ? "" : getStringFromValueEval(value);
                    return valueString.startsWith(conditionString);
                }
            }
        }
        else if(condition instanceof NumericValueEval) {
            double conditionNumber = ((NumericValueEval)condition).getNumberValue();
            Double valueNumber = getNumerFromValueEval(value);
            if(valueNumber == null) {
                return false;
            }
            
            return conditionNumber == valueNumber;
        }
        else if(condition instanceof ErrorEval) {
            if(value instanceof ErrorEval) {
                return ((ErrorEval)condition).getErrorCode() == ((ErrorEval)value).getErrorCode();
            }
            else {
                return false;
            }
        }
        else {
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
        double conditionValue = 0.0;
        try {
            int intValue = Integer.parseInt(condition);
            conditionValue = intValue;
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
    
    private static Double getNumerFromValueEval(ValueEval value) {
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
     * Takes a ValueEval and tries to retrieve a String value from it.
     * It tries to resolve references if there are any.
     *
     * @param value ValueEval to retrieve the string from.
     * @return String corresponding to the given ValueEval.
     * @throws EvaluationException If it's not possible to retrieve a String value.
     */
    private static String getStringFromValueEval(ValueEval value)
            throws EvaluationException {
        value = solveReference(value);
        if(!(value instanceof StringValueEval))
            throw new EvaluationException(ErrorEval.VALUE_INVALID);
        return ((StringValueEval)value).getStringValue();
    }
}
