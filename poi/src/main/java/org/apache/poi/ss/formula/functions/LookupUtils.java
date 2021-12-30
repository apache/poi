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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.formula.TwoDEval;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.MissingArgEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.NumericValueEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.util.Internal;

/**
 * Common functionality used by VLOOKUP, HLOOKUP, LOOKUP and MATCH
 */
@Internal
public final class LookupUtils {

    public enum MatchMode {
        ExactMatch(0),
        ExactMatchFallbackToSmallerValue(-1),
        ExactMatchFallbackToLargerValue(1),
        WildcardMatch(2);

        private final int intValue;

        MatchMode(final int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() { return intValue; }
    }

    public enum SearchMode {
        IterateForward(1),
        IterateBackward(-1),
        BinarySearchForward(2),
        BinarySearchBackward(-2);

        private final int intValue;

        SearchMode(final int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() { return intValue; }
    }

    private static Map<Integer, MatchMode> matchModeMap = new HashMap<>();
    private static Map<Integer, SearchMode> searchModeMap = new HashMap<>();

    static {
        for (MatchMode mode : MatchMode.values()) {
            matchModeMap.put(mode.getIntValue(), mode);
        }
        for (SearchMode mode : SearchMode.values()) {
            searchModeMap.put(mode.getIntValue(), mode);
        }
    }

    public static MatchMode matchMode(int m) {
        MatchMode mode = matchModeMap.get(m);
        if (mode == null) {
            throw new IllegalArgumentException("unknown match mode " + m);
        }
        return mode;
    }

    public static SearchMode searchMode(int s) {
        SearchMode mode = searchModeMap.get(s);
        if (mode == null) {
            throw new IllegalArgumentException("unknown search mode " + s);
        }
        return mode;
    }

    /**
     * Represents a single row or column within an {@code AreaEval}.
     */
    public interface ValueVector {
        ValueEval getItem(int index);
        int getSize();

        default Iterator<Integer> indexIterator() {
            return new Iterator<Integer>() {
                private int pos = 0;

                @Override
                public boolean hasNext() {
                    return pos < getSize();
                }

                @Override
                public Integer next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    return pos++;
                }
            };
        }

        default Iterator<Integer> reverseIndexIterator() {
            return new Iterator<Integer>() {
                private int pos = getSize() - 1;

                @Override
                public boolean hasNext() {
                    return pos > 0;
                }

                @Override
                public Integer next() {
                    pos--;
                    if (pos < 0) throw new NoSuchElementException();
                    return pos;
                }
            };
        }
    }

    private static final class RowVector implements ValueVector {

        private final TwoDEval _tableArray;
        private final int _size;
        private final int _rowIndex;

        public RowVector(TwoDEval tableArray, int rowIndex) {
            _rowIndex = rowIndex;
            int lastRowIx =  tableArray.getHeight() - 1;
            if(rowIndex < 0 || rowIndex > lastRowIx) {
                throw new IllegalArgumentException("Specified row index (" + rowIndex
                        + ") is outside the allowed range (0.." + lastRowIx + ")");
            }
            _tableArray = tableArray;
            _size = tableArray.getWidth();
        }

        @Override
        public ValueEval getItem(int index) {
            if(index > _size) {
                throw new ArrayIndexOutOfBoundsException("Specified index (" + index
                        + ") is outside the allowed range (0.." + (_size-1) + ")");
            }
            return _tableArray.getValue(_rowIndex, index);
        }

        @Override
        public int getSize() {
            return _size;
        }
    }

    private static final class ColumnVector implements ValueVector {

        private final TwoDEval _tableArray;
        private final int _size;
        private final int _columnIndex;

        public ColumnVector(TwoDEval tableArray, int columnIndex) {
            _columnIndex = columnIndex;
            int lastColIx =  tableArray.getWidth()-1;
            if(columnIndex < 0 || columnIndex > lastColIx) {
                throw new IllegalArgumentException("Specified column index (" + columnIndex
                        + ") is outside the allowed range (0.." + lastColIx + ")");
            }
            _tableArray = tableArray;
            _size = _tableArray.getHeight();
        }

        @Override
        public ValueEval getItem(int index) {
            if(index > _size) {
                throw new ArrayIndexOutOfBoundsException("Specified index (" + index
                        + ") is outside the allowed range (0.." + (_size-1) + ")");
            }
            return _tableArray.getValue(index, _columnIndex);
        }
        @Override
        public int getSize() {
            return _size;
        }
    }

    private static final class SheetVector implements ValueVector {
        private final RefEval _re;
        private final int _size;

        public SheetVector(RefEval re) {
            _size = re.getNumberOfSheets();
            _re = re;
        }

        @Override
        public ValueEval getItem(int index) {
            if(index >= _size) {
                throw new ArrayIndexOutOfBoundsException("Specified index (" + index
                        + ") is outside the allowed range (0.." + (_size-1) + ")");
            }
            int sheetIndex = _re.getFirstSheetIndex() + index;
            return _re.getInnerValueEval(sheetIndex);
        }
        @Override
        public int getSize() {
            return _size;
        }
    }

    public static ValueVector createRowVector(TwoDEval tableArray, int relativeRowIndex) {
        return new RowVector(tableArray, relativeRowIndex);
    }
    public static ValueVector createColumnVector(TwoDEval tableArray, int relativeColumnIndex) {
        return new ColumnVector(tableArray, relativeColumnIndex);
    }
    /**
     * @return {@code null} if the supplied area is neither a single row nor a single column
     */
    public static ValueVector createVector(TwoDEval ae) {
        if (ae.isColumn()) {
            return createColumnVector(ae, 0);
        }
        if (ae.isRow()) {
            return createRowVector(ae, 0);
        }
        return null;
    }

    public static ValueVector createVector(RefEval re) {
        return new SheetVector(re);
    }

    /**
     * Enumeration to support <b>4</b> valued comparison results.<p>
     * Excel lookup functions have complex behaviour in the case where the lookup array has mixed
     * types, and/or is unordered.  Contrary to suggestions in some Excel documentation, there
     * does not appear to be a universal ordering across types.  The binary search algorithm used
     * changes behaviour when the evaluated 'mid' value has a different type to the lookup value.<p>
     *
     * A simple int might have done the same job, but there is risk in confusion with the well
     * known {@code Comparable.compareTo()} and {@code Comparator.compare()} which both use
     * a ubiquitous 3 value result encoding.
     */
    public static final class CompareResult {
        private final boolean _isTypeMismatch;
        private final boolean _isLessThan;
        private final boolean _isEqual;
        private final boolean _isGreaterThan;

        private CompareResult(boolean isTypeMismatch, int simpleCompareResult) {
            if(isTypeMismatch) {
                _isTypeMismatch = true;
                _isLessThan = false;
                _isEqual = false;
                _isGreaterThan = false;
            } else {
                _isTypeMismatch = false;
                _isLessThan = simpleCompareResult < 0;
                _isEqual = simpleCompareResult == 0;
                _isGreaterThan = simpleCompareResult > 0;
            }
        }
        public static final CompareResult TYPE_MISMATCH = new CompareResult(true, 0);
        public static final CompareResult LESS_THAN = new CompareResult(false, -1);
        public static final CompareResult EQUAL = new CompareResult(false, 0);
        public static final CompareResult GREATER_THAN = new CompareResult(false, +1);

        public static CompareResult valueOf(int simpleCompareResult) {
            if(simpleCompareResult < 0) {
                return LESS_THAN;
            }
            if(simpleCompareResult > 0) {
                return GREATER_THAN;
            }
            return EQUAL;
        }

        public static CompareResult valueOf(boolean matches) {
            if(matches) {
                return EQUAL ;
            }
            return LESS_THAN;
        }


        public boolean isTypeMismatch() {
            return _isTypeMismatch;
        }
        public boolean isLessThan() {
            return _isLessThan;
        }
        public boolean isEqual() {
            return _isEqual;
        }
        public boolean isGreaterThan() {
            return _isGreaterThan;
        }
        public String toString() {
            return getClass().getName() + " [" +
                    formatAsString() +
                    "]";
        }

        private String formatAsString() {
            if(_isTypeMismatch) {
                return "TYPE_MISMATCH";
            }
            if(_isLessThan) {
                return "LESS_THAN";
            }
            if(_isEqual) {
                return "EQUAL";
            }
            if(_isGreaterThan) {
                return "GREATER_THAN";
            }
            // toString must be reliable
            return "??error??";
        }
    }

    public interface LookupValueComparer {
        /**
         * @return one of 4 instances or {@code CompareResult}: {@code LESS_THAN}, {@code EQUAL},
         * {@code GREATER_THAN} or {@code TYPE_MISMATCH}
         */
        CompareResult compareTo(ValueEval other);
    }

    private static abstract class LookupValueComparerBase implements LookupValueComparer {

        private final Class<? extends ValueEval> _targetClass;
        protected LookupValueComparerBase(ValueEval targetValue) {
            if(targetValue == null) {
                throw new RuntimeException("targetValue cannot be null");
            }
            _targetClass = targetValue.getClass();
        }
        @Override
        public final CompareResult compareTo(ValueEval other) {
            if (other == null) {
                throw new RuntimeException("compare to value cannot be null");
            }
            if (_targetClass != other.getClass()) {
                return CompareResult.TYPE_MISMATCH;
            }
            return compareSameType(other);
        }
        public String toString() {
            return getClass().getName() + " [" +
                    getValueAsString() +
                    "]";
        }
        protected abstract CompareResult compareSameType(ValueEval other);
        /** used only for debug purposes */
        protected abstract String getValueAsString();
    }

    private static class StringLookupComparer extends LookupValueComparerBase {

        protected final String _value;
        protected final Pattern _wildCardPattern;
        protected final boolean _matchExact;
        protected final boolean _isMatchFunction;

        protected StringLookupComparer(StringEval se, boolean matchExact, boolean isMatchFunction) {
            super(se);
            _value = se.getStringValue();
            _wildCardPattern = Countif.StringMatcher.getWildCardPattern(_value);
            _matchExact = matchExact;
            _isMatchFunction = isMatchFunction;
        }

        protected String convertToString(ValueEval other) {
            StringEval se = (StringEval) other;
            return se.getStringValue();
        }

        @Override
        protected CompareResult compareSameType(ValueEval other) {
            String stringValue = convertToString(other);
            if (_wildCardPattern != null && (_isMatchFunction || !_matchExact)) {
                Matcher matcher = _wildCardPattern.matcher(stringValue);
                boolean matches = matcher.matches();

                return CompareResult.valueOf(matches);
            }

            return CompareResult.valueOf(_value.compareToIgnoreCase(stringValue));
        }
        @Override
        protected String getValueAsString() {
            return _value;
        }
    }

    private static final class TolerantStringLookupComparer extends StringLookupComparer {

        static StringEval convertToStringEval(ValueEval eval) {
            if (eval instanceof StringEval) {
                return (StringEval)eval;
            }
            String sv = OperandResolver.coerceValueToString(eval);
            return new StringEval(sv);
        }

        protected TolerantStringLookupComparer(ValueEval eval, boolean matchExact, boolean isMatchFunction) {
            super(convertToStringEval(eval), matchExact, isMatchFunction);
        }

        @Override
        protected String convertToString(ValueEval other) {
            return OperandResolver.coerceValueToString(other);
        }
    }

    private static final class NumberLookupComparer extends LookupValueComparerBase {
        private final double _value;

        protected NumberLookupComparer(NumberEval ne) {
            super(ne);
            _value = ne.getNumberValue();
        }
        @Override
        protected CompareResult compareSameType(ValueEval other) {
            NumberEval ne = (NumberEval) other;
            return CompareResult.valueOf(Double.compare(_value, ne.getNumberValue()));
        }
        @Override
        protected String getValueAsString() {
            return String.valueOf(_value);
        }
    }
    private static final class BooleanLookupComparer extends LookupValueComparerBase {
        private final boolean _value;

        protected BooleanLookupComparer(BoolEval be) {
            super(be);
            _value = be.getBooleanValue();
        }
        @Override
        protected CompareResult compareSameType(ValueEval other) {
            BoolEval be = (BoolEval) other;
            boolean otherVal = be.getBooleanValue();
            if(_value == otherVal) {
                return CompareResult.EQUAL;
            }
            // TRUE > FALSE
            if(_value) {
                return CompareResult.GREATER_THAN;
            }
            return CompareResult.LESS_THAN;
        }
        @Override
        protected String getValueAsString() {
            return String.valueOf(_value);
        }
    }

    /**
     * Processes the third argument to VLOOKUP, or HLOOKUP (<b>col_index_num</b>
     * or <b>row_index_num</b> respectively).<br>
     * Sample behaviour:
     *    <table>
     *      <caption>Sample behaviour</caption>
     *      <tr><th>Input&nbsp;&nbsp;&nbsp;Return</th><th>Value&nbsp;&nbsp;</th><th>Thrown Error</th></tr>
     *      <tr><td>5</td><td>4</td><td>&nbsp;</td></tr>
     *      <tr><td>2.9</td><td>2</td><td>&nbsp;</td></tr>
     *      <tr><td>"5"</td><td>4</td><td>&nbsp;</td></tr>
     *      <tr><td>"2.18e1"</td><td>21</td><td>&nbsp;</td></tr>
     *      <tr><td>"-$2"</td><td>-3</td><td>*</td></tr>
     *      <tr><td>FALSE</td><td>-1</td><td>*</td></tr>
     *      <tr><td>TRUE</td><td>0</td><td>&nbsp;</td></tr>
     *      <tr><td>"TRUE"</td><td>&nbsp;</td><td>#REF!</td></tr>
     *      <tr><td>"abc"</td><td>&nbsp;</td><td>#REF!</td></tr>
     *      <tr><td>""</td><td>&nbsp;</td><td>#REF!</td></tr>
     *      <tr><td>&lt;blank&gt;</td><td>&nbsp;</td><td>#VALUE!</td></tr>
     *    </table><br>
     *
     * Note - out of range errors (result index too high) are handled by the caller.
     * @return column or row index as a zero-based value, never negative.
     * @throws EvaluationException when the specified arg cannot be coerced to a non-negative integer
     */
    public static int resolveRowOrColIndexArg(ValueEval rowColIndexArg, int srcCellRow, int srcCellCol) throws EvaluationException {
        if(rowColIndexArg == null) {
            throw new IllegalArgumentException("argument must not be null");
        }

        ValueEval veRowColIndexArg;
        try {
            veRowColIndexArg = OperandResolver.getSingleValue(rowColIndexArg, srcCellRow, (short)srcCellCol);
        } catch (EvaluationException e) {
            // All errors get translated to #REF!
            throw EvaluationException.invalidRef();
        }
        int oneBasedIndex;
        if(veRowColIndexArg instanceof StringEval) {
            StringEval se = (StringEval) veRowColIndexArg;
            String strVal = se.getStringValue();
            Double dVal = OperandResolver.parseDouble(strVal);
            if(dVal == null) {
                // String does not resolve to a number. Raise #REF! error.
                throw EvaluationException.invalidRef();
                // This includes text booleans "TRUE" and "FALSE".  They are not valid.
            }
            // else - numeric value parses OK
        }
        // actual BoolEval values get interpreted as FALSE->0 and TRUE->1
        oneBasedIndex = OperandResolver.coerceValueToInt(veRowColIndexArg);
        if (oneBasedIndex < 1) {
            // note this is asymmetric with the errors when the index is too large (#REF!)
            throw EvaluationException.invalidValue();
        }
        return oneBasedIndex - 1; // convert to zero based
    }



    /**
     * The second argument (table_array) should be an area ref, but can actually be a cell ref, in
     * which case it is interpreted as a 1x1 area ref.  Other scalar values cause #VALUE! error.
     */
    public static TwoDEval resolveTableArrayArg(ValueEval eval) throws EvaluationException {
        if (eval instanceof TwoDEval) {
            return (TwoDEval) eval;
        }

        if(eval instanceof RefEval) {
            RefEval refEval = (RefEval) eval;
            // Make this cell ref look like a 1x1 area ref.

            // It doesn't matter if eval is a 2D or 3D ref, because that detail is never asked of AreaEval.
            return refEval.offset(0, 0, 0, 0);
        }
        throw EvaluationException.invalidValue();
    }


    /**
     * Resolves the last (optional) parameter (<b>range_lookup</b>) to the VLOOKUP and HLOOKUP functions.
     * @param rangeLookupArg must not be {@code null}
     */
    public static boolean resolveRangeLookupArg(ValueEval rangeLookupArg, int srcCellRow, int srcCellCol) throws EvaluationException {

        ValueEval valEval = OperandResolver.getSingleValue(rangeLookupArg, srcCellRow, srcCellCol);
        if(valEval == MissingArgEval.instance) {
            // Tricky:
            // forth arg exists but is not supplied: "=VLOOKUP(A1,A2:A4,2,)"
            return false;
        }
        if(valEval instanceof BlankEval) {
            // Tricky:
            // fourth arg supplied but evaluates to blank
            // this does not get the default value
            return false;
        }
        if(valEval instanceof BoolEval) {
            // Happy day flow
            BoolEval boolEval = (BoolEval) valEval;
            return boolEval.getBooleanValue();
        }

        if (valEval instanceof StringEval) {
            String stringValue = ((StringEval) valEval).getStringValue();
            if(stringValue.length() < 1) {
                // More trickiness:
                // Empty string is not the same as BlankEval.  It causes #VALUE! error
                throw EvaluationException.invalidValue();
            }
            // TODO move parseBoolean to OperandResolver
            Boolean b = Countif.parseBoolean(stringValue);
            if(b != null) {
                // string converted to boolean OK
                return b;
            }
            // Even more trickiness:
            // Note - even if the StringEval represents a number value (for example "1"),
            // Excel does not resolve it to a boolean.
            throw EvaluationException.invalidValue();
            // This is in contrast to the code below,, where NumberEvals values (for
            // example 0.01) *do* resolve to equivalent boolean values.
        }
        if (valEval instanceof NumericValueEval) {
            NumericValueEval nve = (NumericValueEval) valEval;
            // zero is FALSE, everything else is TRUE
            return 0.0 != nve.getNumberValue();
        }
        throw new RuntimeException("Unexpected eval type (" + valEval + ")");
    }

    public static int lookupFirstIndexOfValue(ValueEval lookupValue, ValueVector vector, boolean isRangeLookup) throws EvaluationException {
        LookupValueComparer lookupComparer = createLookupComparer(lookupValue, isRangeLookup, false);
        int result;
        if(isRangeLookup) {
            result = performBinarySearch(vector, lookupComparer);
        } else {
            result = lookupFirstIndexOfValue(lookupComparer, vector, MatchMode.ExactMatch);
        }
        if(result < 0) {
            throw new EvaluationException(ErrorEval.NA);
        }
        return result;
    }

    public static int xlookupIndexOfValue(ValueEval lookupValue, ValueVector vector, MatchMode matchMode, SearchMode searchMode) throws EvaluationException {
        ValueEval modifiedLookup = lookupValue;
        if (lookupValue instanceof StringEval &&
                (matchMode == MatchMode.ExactMatchFallbackToLargerValue || matchMode == MatchMode.ExactMatchFallbackToSmallerValue)) {
            String lookupText = ((StringEval)lookupValue).getStringValue();
            StringBuilder sb = new StringBuilder(lookupText.length());
            boolean containsWildcard = false;
            for (char c : lookupText.toCharArray()) {
                switch (c) {
                    case '~':
                    case '?':
                    case '*':
                        containsWildcard = true;
                        break;
                    default:
                        sb.append(c);
                }
                if (containsWildcard)
                    break;
            }
            if (containsWildcard) {
                modifiedLookup = new StringEval(sb.toString());
            }
        }
        LookupValueComparer lookupComparer = createTolerantLookupComparer(modifiedLookup, matchMode != MatchMode.WildcardMatch, true);
        int result;
        if (searchMode == SearchMode.BinarySearchForward) {
            result = binarySearchIndexOfValue(lookupComparer, vector, matchMode, false);
        } else if (searchMode == SearchMode.BinarySearchBackward) {
            result = binarySearchIndexOfValue(lookupComparer, vector, matchMode, true);
        } else if (searchMode == SearchMode.IterateBackward) {
            result = lookupLastIndexOfValue(lookupComparer, vector, matchMode);
        } else {
            result = lookupFirstIndexOfValue(lookupComparer, vector, matchMode);
        }
        if(result < 0) {
            throw new EvaluationException(ErrorEval.NA);
        }
        return result;
    }

    /**
     * Finds first (lowest index) matching occurrence of specified value.
     * @param lookupComparer the value to be found in column or row vector
     * @param vector the values to be searched. For VLOOKUP this is the first column of the
     *  tableArray. For HLOOKUP this is the first row of the tableArray.
     * @param matchMode
     * @return zero based index into the vector, -1 if value cannot be found
     */
    private static int lookupFirstIndexOfValue(LookupValueComparer lookupComparer, ValueVector vector,
                                               MatchMode matchMode) {
        return lookupIndexOfValue(lookupComparer, vector, matchMode, false);
    }

    /**
     * Finds last (greatest index) matching occurrence of specified value.
     * @param lookupComparer the value to be found in column or row vector
     * @param vector the values to be searched. For VLOOKUP this is the first column of the
     *  tableArray. For HLOOKUP this is the first row of the tableArray.
     * @param matchMode
     * @return zero based index into the vector, -1 if value cannot be found
     */
    private static int lookupLastIndexOfValue(LookupValueComparer lookupComparer, ValueVector vector,
                                              MatchMode matchMode) {
        return lookupIndexOfValue(lookupComparer, vector, matchMode, true);
    }

    private static int lookupIndexOfValue(LookupValueComparer lookupComparer, ValueVector vector,
                                          MatchMode matchMode, boolean reverse) {
        int bestMatchIdx = -1;
        ValueEval bestMatchEval = null;
        Iterator<Integer> idxIter = reverse ? vector.reverseIndexIterator() : vector.indexIterator();
        while (idxIter.hasNext()) {
            int i = idxIter.next();
            ValueEval valueEval = vector.getItem(i);
            CompareResult result = lookupComparer.compareTo(valueEval);
            if (result.isEqual()) {
                return i;
            }
            switch (matchMode) {
                case ExactMatchFallbackToLargerValue:
                    if (result.isLessThan()) {
                        if (bestMatchEval == null) {
                            bestMatchIdx = i;
                            bestMatchEval = valueEval;
                        } else {
                            LookupValueComparer matchComparer = createTolerantLookupComparer(valueEval, true, true);
                            if (matchComparer.compareTo(bestMatchEval).isLessThan()) {
                                bestMatchIdx = i;
                                bestMatchEval = valueEval;
                            }
                        }
                    }
                    break;
                case ExactMatchFallbackToSmallerValue:
                    if (result.isGreaterThan()) {
                        if (bestMatchEval == null) {
                            bestMatchIdx = i;
                            bestMatchEval = valueEval;
                        } else {
                            LookupValueComparer matchComparer = createTolerantLookupComparer(valueEval, true, true);
                            if (matchComparer.compareTo(bestMatchEval).isGreaterThan()) {
                                bestMatchIdx = i;
                                bestMatchEval = valueEval;
                            }
                        }
                    }
                    break;
            }
        }
        return bestMatchIdx;
    }

    private static int binarySearchIndexOfValue(LookupValueComparer lookupComparer, ValueVector vector,
                                                MatchMode matchMode, boolean reverse) {
        int bestMatchIdx = -1;
        ValueEval bestMatchEval = null;
        HashSet<Integer> alreadySearched = new HashSet<>();
        BinarySearchIndexes bsi = new BinarySearchIndexes(vector.getSize());
        while (true) {
            int i = bsi.getMidIx();
            if(i < 0 || alreadySearched.contains(i)) {
                return bestMatchIdx;
            }
            alreadySearched.add(i);
            ValueEval valueEval = vector.getItem(i);
            CompareResult result = lookupComparer.compareTo(valueEval);
            if (result.isEqual()) {
                return i;
            }
            switch (matchMode) {
                case ExactMatchFallbackToLargerValue:
                    if (result.isLessThan()) {
                        if (bestMatchEval == null) {
                            bestMatchIdx = i;
                            bestMatchEval = valueEval;
                        } else {
                            LookupValueComparer matchComparer = createTolerantLookupComparer(valueEval, true, true);
                            if (matchComparer.compareTo(bestMatchEval).isLessThan()) {
                                bestMatchIdx = i;
                                bestMatchEval = valueEval;
                            }
                        }
                    }
                    break;
                case ExactMatchFallbackToSmallerValue:
                    if (result.isGreaterThan()) {
                        if (bestMatchEval == null) {
                            bestMatchIdx = i;
                            bestMatchEval = valueEval;
                        } else {
                            LookupValueComparer matchComparer = createTolerantLookupComparer(valueEval, true, true);
                            if (matchComparer.compareTo(bestMatchEval).isGreaterThan()) {
                                bestMatchIdx = i;
                                bestMatchEval = valueEval;
                            }
                        }
                    }
                    break;
            }
            if (result.isTypeMismatch()) {
                int newIdx = handleMidValueTypeMismatch(lookupComparer, vector, bsi, i, reverse);
                if (newIdx >= 0) {
                    return newIdx;
                }
            } else if (reverse) {
                bsi.narrowSearch(i, result.isGreaterThan());
            } else {
                bsi.narrowSearch(i, result.isLessThan());
            }
        }
    }

    /**
     * Encapsulates some standard binary search functionality so the unusual Excel behaviour can
     * be clearly distinguished.
     */
    private static final class BinarySearchIndexes {

        private int _lowIx;
        private int _highIx;

        public BinarySearchIndexes(int highIx) {
            _lowIx = -1;
            _highIx = highIx;
        }

        /**
         * @return -1 if the search range is empty
         */
        public int getMidIx() {
            int ixDiff = _highIx - _lowIx;
            if(ixDiff < 2) {
                return -1;
            }
            return _lowIx + (ixDiff / 2);
        }

        public int getLowIx() {
            return _lowIx;
        }
        public int getHighIx() {
            return _highIx;
        }
        public void narrowSearch(int midIx, boolean isLessThan) {
            if(isLessThan) {
                _highIx = midIx;
            } else {
                _lowIx = midIx;
            }
        }
    }
    /**
     * Excel has funny behaviour when the some elements in the search vector are the wrong type.
     *
     */
    private static int performBinarySearch(ValueVector vector, LookupValueComparer lookupComparer) {
        // both low and high indexes point to values assumed too low and too high.
        BinarySearchIndexes bsi = new BinarySearchIndexes(vector.getSize());

        while(true) {
            int midIx = bsi.getMidIx();

            if(midIx < 0) {
                return bsi.getLowIx();
            }
            CompareResult cr = lookupComparer.compareTo(vector.getItem(midIx));
            if(cr.isTypeMismatch()) {
                int newMidIx = handleMidValueTypeMismatch(lookupComparer, vector, bsi, midIx, false);
                if(newMidIx < 0) {
                    continue;
                }
                midIx = newMidIx;
                cr = lookupComparer.compareTo(vector.getItem(midIx));
            }
            if(cr.isEqual()) {
                return findLastIndexInRunOfEqualValues(lookupComparer, vector, midIx, bsi.getHighIx());
            }
            bsi.narrowSearch(midIx, cr.isLessThan());
        }
    }
    /**
     * Excel seems to handle mismatched types initially by just stepping 'mid' ix forward to the
     * first compatible value.
     * @param midIx 'mid' index (value which has the wrong type)
     * @param reverse the data is sorted in reverse order
     * @return usually -1, signifying that the BinarySearchIndex has been narrowed to the new mid
     * index.  Zero or greater signifies that an exact match for the lookup value was found
     */
    private static int handleMidValueTypeMismatch(LookupValueComparer lookupComparer, ValueVector vector,
            BinarySearchIndexes bsi, int midIx, boolean reverse) {
        int newMid = midIx;
        int highIx = bsi.getHighIx();

        while(true) {
            newMid++;
            if(newMid == highIx) {
                // every element from midIx to highIx was the wrong type
                // move highIx down to the low end of the mid values
                bsi.narrowSearch(midIx, true);
                return -1;
            }
            CompareResult cr = lookupComparer.compareTo(vector.getItem(newMid));
            if(cr.isLessThan() && !reverse && newMid == highIx-1) {
                // move highIx down to the low end of the mid values
                bsi.narrowSearch(midIx, true);
                return -1;
                // but only when "newMid == highIx-1"? slightly weird.
                // It would seem more efficient to always do this.
            } else if(cr.isGreaterThan() && reverse && newMid == highIx-1) {
                // move highIx down to the low end of the mid values
                bsi.narrowSearch(midIx, true);
                return -1;
                // but only when "newMid == highIx-1"? slightly weird.
                // It would seem more efficient to always do this.
            }
            if(cr.isTypeMismatch()) {
                // keep stepping over values until the right type is found
                continue;
            }
            if(cr.isEqual()) {
                return newMid;
            }
            // Note - if moving highIx down (due to lookup<vector[newMid]),
            // this execution path only moves highIx it down as far as newMid, not midIx,
            // which would be more efficient.
            if (reverse) {
                bsi.narrowSearch(newMid, cr.isGreaterThan());
            } else {
                bsi.narrowSearch(newMid, cr.isLessThan());
            }
            return -1;
        }
    }
    /**
     * Once the binary search has found a single match, (V/H)LOOKUP steps one by one over subsequent
     * values to choose the last matching item.
     */
    private static int findLastIndexInRunOfEqualValues(LookupValueComparer lookupComparer, ValueVector vector,
                int firstFoundIndex, int maxIx) {
        for(int i=firstFoundIndex+1; i<maxIx; i++) {
            if(!lookupComparer.compareTo(vector.getItem(i)).isEqual()) {
                return i-1;
            }
        }
        return maxIx - 1;
    }

    static LookupValueComparer createLookupComparer(ValueEval lookupValue, boolean matchExact, boolean isMatchFunction) {

        if (lookupValue == BlankEval.instance) {
            // blank eval translates to zero
            // Note - a blank eval in the lookup column/row never matches anything
            // empty string in the lookup column/row can only be matched by explicit empty string
            return new NumberLookupComparer(NumberEval.ZERO);
        }
        if (lookupValue instanceof StringEval) {
            //TODO eventually here return a WildcardStringLookupComparer
            return new StringLookupComparer((StringEval) lookupValue, matchExact, isMatchFunction);
        }
        if (lookupValue instanceof NumberEval) {
            return new NumberLookupComparer((NumberEval) lookupValue);
        }
        if (lookupValue instanceof BoolEval) {
            return new BooleanLookupComparer((BoolEval) lookupValue);
        }
        throw new IllegalArgumentException("Bad lookup value type (" + lookupValue.getClass().getName() + ")");
    }

    private static LookupValueComparer createTolerantLookupComparer(ValueEval lookupValue, boolean matchExact, boolean isMatchFunction) {
        if (lookupValue == BlankEval.instance) {
            return new TolerantStringLookupComparer(new StringEval(""), matchExact, isMatchFunction);
        }
        if (lookupValue instanceof BoolEval) {
            return new BooleanLookupComparer((BoolEval) lookupValue);
        }
        if (matchExact && lookupValue instanceof NumberEval) {
            return new NumberLookupComparer((NumberEval) lookupValue);
        }
        return new TolerantStringLookupComparer(lookupValue, matchExact, isMatchFunction);
    }
}
