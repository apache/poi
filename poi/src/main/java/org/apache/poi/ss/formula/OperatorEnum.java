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

import org.apache.poi.util.Internal;

/**
 * Not calling it OperatorType to avoid confusion for now with other classes.
 * Definition order matches OOXML type ID indexes.
 * Note that this has NO_COMPARISON as the first item, unlike the similar
 * DataValidation operator enum. Thanks, Microsoft.
 */
@Internal
enum OperatorEnum {
    // always false/invalid
    NO_COMPARISON(OperatorEnum::noComp, false),
    BETWEEN(OperatorEnum::between, false),
    NOT_BETWEEN(OperatorEnum::notBetween, true),
    EQUAL(OperatorEnum::equalCheck, false),
    NOT_EQUAL(OperatorEnum::notEqual, true),
    GREATER_THAN(OperatorEnum::greaterThan, false),
    LESS_THAN(OperatorEnum::lessThan, false),
    GREATER_OR_EQUAL(OperatorEnum::greaterOrEqual, false),
    LESS_OR_EQUAL(OperatorEnum::lessOrEqual, false)        ;

    private interface CompareOp {
        <C extends Comparable<C>> boolean isValid(C cellValue, C v1, C v2);
    }

    private final CompareOp compareOp;
    private final boolean validForIncompatibleTypes;

    OperatorEnum(CompareOp compareOp, boolean validForIncompatibleTypes) {
        this.compareOp = compareOp;
        this.validForIncompatibleTypes = validForIncompatibleTypes;
    }

    /**
     * Evaluates comparison using operator instance rules
     * @param cellValue won't be null, assumption is previous checks handled that
     * @param v1 if null, per Excel behavior various results depending on the type of cellValue and the specific enum instance
     * @param v2 null if not needed.  If null when needed, various results, per Excel behavior
     * @return true if the comparison is valid
     */
    <C extends Comparable<C>> boolean isValid(C cellValue, C v1, C v2) {
        return compareOp.isValid(cellValue, v1, v2);
    }

    /**
     * Called when the cell and comparison values are of different data types
     * Needed for negation operators, which should return true.
     * @return true if this comparison is true when the types to compare are different
     */
    boolean isValidForIncompatibleTypes() {
        return validForIncompatibleTypes;
    }

    private static <C extends Comparable<C>> boolean noComp(C cellValue, C v1, C v2) {
        return false;
    }

    private static <C extends Comparable<C>> boolean between(C cellValue, C v1, C v2) {
        if (v1 == null) {
            if (cellValue instanceof Number) {
                // use zero for null
                double n1 = 0;
                double n2 = v2 == null ? 0 : ((Number) v2).doubleValue();
                return Double.compare( ((Number) cellValue).doubleValue(), n1) >= 0 && Double.compare(((Number) cellValue).doubleValue(), n2) <= 0;
            } else if (cellValue instanceof String) {
                String n1 = "";
                String n2 = v2 == null ? "" : (String) v2;
                return ((String) cellValue).compareToIgnoreCase(n1) >= 0 && ((String) cellValue).compareToIgnoreCase(n2) <= 0;
            } else if (cellValue instanceof Boolean) return false;
            return false; // just in case - not a typical possibility
        }
        return cellValue.compareTo(v1) >= 0 && cellValue.compareTo(v2) <= 0;
    }

    private static <C extends Comparable<C>> boolean notBetween(C cellValue, C v1, C v2) {
        if (v1 == null) {
            if (cellValue instanceof Number) {
                // use zero for null
                double n1 = 0;
                double n2 = v2 == null ? 0 : ((Number) v2).doubleValue();
                return Double.compare( ((Number) cellValue).doubleValue(), n1) < 0 || Double.compare(((Number) cellValue).doubleValue(), n2) > 0;
            } else if (cellValue instanceof String) {
                String n1 = "";
                String n2 = v2 == null ? "" : (String) v2;
                return ((String) cellValue).compareToIgnoreCase(n1) < 0 || ((String) cellValue).compareToIgnoreCase(n2) > 0;
            } else {
                // just in case - not a typical possibility
                return cellValue instanceof Boolean;
            }
        }
        return cellValue.compareTo(v1) < 0 || cellValue.compareTo(v2) > 0;
    }

    private static <C extends Comparable<C>> boolean equalCheck(C cellValue, C v1, C v2) {
        if (v1 == null) {
            if (cellValue instanceof Number) {
                // use zero for null
                return Double.compare( ((Number) cellValue).doubleValue(), 0) == 0;
            } else if (cellValue instanceof String) {
                return false; // even an empty string is not equal the empty cell, only another empty cell is, handled higher up
            } else if (cellValue instanceof Boolean) return false;
            return false; // just in case - not a typical possibility
        }
        if (cellValue instanceof String) {
            return cellValue.toString().compareToIgnoreCase(v1.toString()) == 0;
        }
        return cellValue.compareTo(v1) == 0;
    }

    private static <C extends Comparable<C>> boolean notEqual(C cellValue, C v1, C v2) {
        if (v1 == null) {
            return true; // non-null not equal null, returns true
        }
        if (cellValue instanceof String) {
            return cellValue.toString().compareToIgnoreCase(v1.toString()) == 0;
        }
        return cellValue.compareTo(v1) != 0;
    }

    private static <C extends Comparable<C>> boolean greaterThan(C cellValue, C v1, C v2) {
        if (v1 == null) {
            if (cellValue instanceof Number) {
                // use zero for null
                return Double.compare( ((Number) cellValue).doubleValue(), 0) > 0;
            } else if (cellValue instanceof String) {
                return true; // non-null string greater than empty cell
            } else {
                // just in case - not a typical possibility
                return cellValue instanceof Boolean;
            }
        }
        return cellValue.compareTo(v1) > 0;
    }

    private static <C extends Comparable<C>> boolean lessThan(C cellValue, C v1, C v2) {
        if (v1 == null) {
            if (cellValue instanceof Number) {
                // use zero for null
                return Double.compare( ((Number) cellValue).doubleValue(), 0) < 0;
            } else if (cellValue instanceof String) {
                return false; // non-null string greater than empty cell
            } else if (cellValue instanceof Boolean) return false;
            return false; // just in case - not a typical possibility
        }
        return cellValue.compareTo(v1) < 0;
    }

    private static <C extends Comparable<C>> boolean greaterOrEqual(C cellValue, C v1, C v2) {
        if (v1 == null) {
            if (cellValue instanceof Number) {
                // use zero for null
                return Double.compare( ((Number) cellValue).doubleValue(), 0) >= 0;
            } else if (cellValue instanceof String) {
                return true; // non-null string greater than empty cell
            } else {
                // just in case - not a typical possibility
                return cellValue instanceof Boolean;
            }
        }
        return cellValue.compareTo(v1) >= 0;
    }

    private static <C extends Comparable<C>> boolean lessOrEqual(C cellValue, C v1, C v2) {
        if (v1 == null) {
            if (cellValue instanceof Number) {
                // use zero for null
                return Double.compare( ((Number) cellValue).doubleValue(), 0) <= 0;
            } else if (cellValue instanceof String) {
                return false; // non-null string not less than empty cell
            } else if (cellValue instanceof Boolean) return false; // for completeness
            return false; // just in case - not a typical possibility
        }
        return cellValue.compareTo(v1) <= 0;
    }

}
