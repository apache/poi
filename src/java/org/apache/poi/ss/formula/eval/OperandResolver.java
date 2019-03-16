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

package org.apache.poi.ss.formula.eval;

import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.CellRangeAddress;

import java.time.DateTimeException;
import java.util.regex.Pattern;

/**
 * Provides functionality for evaluating arguments to functions and operators.
 *
 * @author Josh Micich
 * @author Brendan Nolan
 */
public final class OperandResolver {

    // Based on regular expression defined in JavaDoc at {@link java.lang.Double#valueOf}
    // modified to remove support for NaN, Infinity, Hexadecimal support and floating type suffixes
    private static final String Digits    = "(\\p{Digit}+)";
    private static final String Exp    = "[eE][+-]?"+Digits;
    private static final String fpRegex    =
                ("[\\x00-\\x20]*" +
                 "[+-]?(" +
                   "("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+
                 "(\\."+Digits+"("+Exp+")?))"+
                 "[\\x00-\\x20]*"); 
            
    
    private OperandResolver() {
        // no instances of this class
    }

    /**
     * Retrieves a single value from a variety of different argument types according to standard
     * Excel rules.  Does not perform any type conversion.
     * @param arg the evaluated argument as passed to the function or operator.
     * @param srcCellRow used when arg is a single column AreaRef
     * @param srcCellCol used when arg is a single row AreaRef
     * @return a <tt>NumberEval</tt>, <tt>StringEval</tt>, <tt>BoolEval</tt> or <tt>BlankEval</tt>.
     * Never <code>null</code> or <tt>ErrorEval</tt>.
     * @throws EvaluationException if srcCellRow or srcCellCol do not properly index into
     *  an AreaEval.  If the actual value retrieved is an ErrorEval, a corresponding
     *  EvaluationException is thrown.
     */
    public static ValueEval getSingleValue(ValueEval arg, int srcCellRow, int srcCellCol)
            throws EvaluationException {
        final ValueEval result;
        if (arg instanceof RefEval) {
            result = chooseSingleElementFromRef((RefEval) arg);
        } else if (arg instanceof AreaEval) {
            result = chooseSingleElementFromArea((AreaEval) arg, srcCellRow, srcCellCol);
        } else {
            result = arg;
        }
        if (result instanceof ErrorEval) {
            throw new EvaluationException((ErrorEval) result);
        }
        return result;
    }
    
    /**
     * Retrieves a single value from an area evaluation utilizing the 2D indices of the cell
     * within its own area reference to index the value in the area evaluation.
     *
     * @param ae area reference after evaluation
     * @param cell the source cell of the formula that contains its 2D indices
     * @return a <tt>NumberEval</tt>, <tt>StringEval</tt>, <tt>BoolEval</tt> or <tt>BlankEval</tt>. or <tt>ErrorEval<tt>
     * Never <code>null</code>.
     */

    public static ValueEval getElementFromArray(AreaEval ae, EvaluationCell cell) {
        CellRangeAddress range =  cell.getArrayFormulaRange();
        int relativeRowIndex = cell.getRowIndex() - range.getFirstRow();
        int relativeColIndex = cell.getColumnIndex() - range.getFirstColumn();

        if (ae.isColumn()) {
            if (ae.isRow()) {
                return ae.getRelativeValue(0, 0);
            }
            else if(relativeRowIndex < ae.getHeight()) {
                return ae.getRelativeValue(relativeRowIndex, 0);
            }
        }
        else if (!ae.isRow() && relativeRowIndex < ae.getHeight() && relativeColIndex < ae.getWidth()) {
            return ae.getRelativeValue(relativeRowIndex, relativeColIndex);
        }
        else if (ae.isRow() && relativeColIndex < ae.getWidth()) {
            return ae.getRelativeValue(0, relativeColIndex);
        }
        
        return ErrorEval.NA;
    }

    /**
     * Implements (some perhaps not well known) Excel functionality to select a single cell from an
     * area depending on the coordinates of the calling cell.  Here is an example demonstrating
     * both selection from a single row area and a single column area in the same formula.
     *
     *    <table border="1" cellpadding="1" cellspacing="1" summary="sample spreadsheet">
     *      <tr><th>&nbsp;</th><th>&nbsp;A&nbsp;</th><th>&nbsp;B&nbsp;</th><th>&nbsp;C&nbsp;</th><th>&nbsp;D&nbsp;</th></tr>
     *      <tr><th>1</th><td>15</td><td>20</td><td>25</td><td>&nbsp;</td></tr>
     *      <tr><th>2</th><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>200</td></tr>
     *      <tr><th>3</th><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>300</td></tr>
     *      <tr><th>3</th><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>400</td></tr>
     *    </table>
     *
     * If the formula "=1000+A1:B1+D2:D3" is put into the 9 cells from A2 to C4, the spreadsheet
     * will look like this:
     *
     *    <table border="1" cellpadding="1" cellspacing="1" summary="sample spreadsheet">
     *      <tr><th>&nbsp;</th><th>&nbsp;A&nbsp;</th><th>&nbsp;B&nbsp;</th><th>&nbsp;C&nbsp;</th><th>&nbsp;D&nbsp;</th></tr>
     *      <tr><th>1</th><td>15</td><td>20</td><td>25</td><td>&nbsp;</td></tr>
     *      <tr><th>2</th><td>1215</td><td>1220</td><td>#VALUE!</td><td>200</td></tr>
     *      <tr><th>3</th><td>1315</td><td>1320</td><td>#VALUE!</td><td>300</td></tr>
     *      <tr><th>4</th><td>#VALUE!</td><td>#VALUE!</td><td>#VALUE!</td><td>400</td></tr>
     *    </table>
     *
     * Note that the row area (A1:B1) does not include column C and the column area (D2:D3) does
     * not include row 4, so the values in C1(=25) and D4(=400) are not accessible to the formula
     * as written, but in the 4 cells A2:B3, the row and column selection works ok.<p>
     *
     * The same concept is extended to references across sheets, such that even multi-row,
     * multi-column areas can be useful.<p>
     *
     * Of course with carefully (or carelessly) chosen parameters, cyclic references can occur and
     * hence this method <b>can</b> throw a 'circular reference' EvaluationException.  Note that
     * this method does not attempt to detect cycles.  Every cell in the specified Area <tt>ae</tt>
     * has already been evaluated prior to this method call.  Any cell (or cell<b>s</b>) part of
     * <tt>ae</tt> that would incur a cyclic reference error if selected by this method, will
     * already have the value <t>ErrorEval.CIRCULAR_REF_ERROR</tt> upon entry to this method.  It
     * is assumed logic exists elsewhere to produce this behaviour.
     *
     * @return whatever the selected cell's evaluated value is.  Never <code>null</code>. Never
     *  <tt>ErrorEval</tt>.
     * @throws EvaluationException if there is a problem with indexing into the area, or if the
     *  evaluated cell has an error.
     */
    public static ValueEval chooseSingleElementFromArea(AreaEval ae,
            int srcCellRow, int srcCellCol) throws EvaluationException {
        ValueEval result = chooseSingleElementFromAreaInternal(ae, srcCellRow, srcCellCol);
        if (result instanceof ErrorEval) {
            throw new EvaluationException((ErrorEval) result);
        }
        return result;
    }

    /**
     * @return possibly <tt>ErrorEval</tt>, and <code>null</code>
     */
    private static ValueEval chooseSingleElementFromAreaInternal(AreaEval ae,
            int srcCellRow, int srcCellCol) throws EvaluationException {

//        if(false) {
//            // this is too simplistic
//            if(ae.containsRow(srcCellRow) && ae.containsColumn(srcCellCol)) {
//                throw new EvaluationException(ErrorEval.CIRCULAR_REF_ERROR);
//            }
//        /*
//        Circular references are not dealt with directly here, but it is worth noting some issues.
//
//        ANY one of the return statements in this method could return a cell that is identical
//        to the one immediately being evaluated.  The evaluating cell is identified by srcCellRow,
//        srcCellRow AND sheet.  The sheet is not available in any nearby calling method, so that's
//        one reason why circular references are not easy to detect here. (The sheet of the returned
//        cell can be obtained from ae if it is an Area3DEval.)
//
//        Another reason there's little value in attempting to detect circular references here is
//        that only direct circular references could be detected.  If the cycle involved two or more
//        cells this method could not detect it.
//
//        Logic to detect evaluation cycles of all kinds has been coded in EvaluationCycleDetector
//        (and FormulaEvaluator).
//         */
//        }

        if (ae.isColumn()) {
            if(ae.isRow()) {
                return ae.getRelativeValue(0, 0);
            }
            if(!ae.containsRow(srcCellRow)) {
                throw EvaluationException.invalidValue();
            }
            return ae.getAbsoluteValue(srcCellRow, ae.getFirstColumn());
        }
        if(!ae.isRow()) {
            // multi-column, multi-row area
            if(ae.containsRow(srcCellRow) && ae.containsColumn(srcCellCol)) {
                return ae.getAbsoluteValue(srcCellRow, srcCellCol);
            }
            throw EvaluationException.invalidValue();
        }
        if(!ae.containsColumn(srcCellCol)) {
            throw EvaluationException.invalidValue();
        }
        return ae.getAbsoluteValue(ae.getFirstRow(), srcCellCol);
    }
    
    private static ValueEval chooseSingleElementFromRef(RefEval ref) {
        return ref.getInnerValueEval( ref.getFirstSheetIndex() );
    }

    /**
     * Applies some conversion rules if the supplied value is not already an integer.<br>
     * Value is first coerced to a <tt>double</tt> ( See <tt>coerceValueToDouble()</tt> ).
     * Note - <tt>BlankEval</tt> is converted to <code>0</code>.<p>
     *
     * Excel typically converts doubles to integers by truncating toward negative infinity.<br>
     * The equivalent java code is:<br>
     * &nbsp;&nbsp;<code>return (int)Math.floor(d);</code><br>
     * <b>not</b>:<br>
     * &nbsp;&nbsp;<code>return (int)d; // wrong - rounds toward zero</code>
     *
     */
    public static int coerceValueToInt(ValueEval ev) throws EvaluationException {
        if (ev == BlankEval.instance) {
            return 0;
        }
        double d = coerceValueToDouble(ev);
        // Note - the standard java type conversion from double to int truncates toward zero.
        // but Math.floor() truncates toward negative infinity
        return (int)Math.floor(d);
    }

    /**
     * Applies some conversion rules if the supplied value is not already a number.
     * Note - <tt>BlankEval</tt> is converted to {@link NumberEval#ZERO}.
     * @param ev must be a {@link NumberEval}, {@link StringEval}, {@link BoolEval} or
     * {@link BlankEval}
     * @return actual, parsed or interpreted double value (respectively).
     * @throws EvaluationException if a StringEval is supplied and cannot be parsed
     * as a double (See <tt>parseDouble()</tt> for allowable formats).
     * @throws RuntimeException if the supplied parameter is not {@link NumberEval},
     * {@link StringEval}, {@link BoolEval} or {@link BlankEval}
     */
    public static double coerceValueToDouble(ValueEval ev) throws EvaluationException {

        if (ev == BlankEval.instance) {
            return 0.0;
        }
        if (ev instanceof NumericValueEval) {
            // this also handles booleans
            return ((NumericValueEval)ev).getNumberValue();
        }
        if (ev instanceof StringEval) {
            String sval = ((StringEval) ev).getStringValue();
            Double dd = parseDouble(sval);
            if(dd == null) dd = parseDateTime(sval);
            if (dd == null) {
                throw EvaluationException.invalidValue();
            }
            return dd.doubleValue();
        }
        throw new RuntimeException("Unexpected arg eval type (" + ev.getClass().getName() + ")");
    }

    /**
     * Converts a string to a double using standard rules that Excel would use.<br>
     * Tolerates leading and trailing spaces, <p>
     * 
     * Doesn't support currency prefixes, commas, percentage signs or arithmetic operations strings.  
     *
     *  Some examples:<br>
     *  " 123 " -&gt; 123.0<br>
     *  ".123" -&gt; 0.123<br>
     *  "1E4" -&gt; 1000<br>
     *  "-123" -&gt; -123.0<br>
     *  These not supported yet:<br>
     *  " $ 1,000.00 " -&gt; 1000.0<br>
     *  "$1.25E4" -&gt; 12500.0<br>
     *  "5**2" -&gt; 500<br>
     *  "250%" -&gt; 2.5<br>
     *
     * @return <code>null</code> if the specified text cannot be parsed as a number
     */
    public static Double parseDouble(String pText) {
        
        if (Pattern.matches(fpRegex, pText))
            try {
                return Double.parseDouble(pText);
            } catch (NumberFormatException e) {
                return null;
            }
        else {
            return null;
        }
        
    }

    public static Double parseDateTime(String pText) {

        try {
            return DateUtil.parseDateTime(pText);
        } catch (DateTimeException e) {
            return null;
        }

    }

    /**
     * @param ve must be a <tt>NumberEval</tt>, <tt>StringEval</tt>, <tt>BoolEval</tt>, or <tt>BlankEval</tt>
     * @return the converted string value. never <code>null</code>
     */
    public static String coerceValueToString(ValueEval ve) {
        if (ve instanceof StringValueEval) {
            StringValueEval sve = (StringValueEval) ve;
            return sve.getStringValue();
        }
        if (ve == BlankEval.instance) {
            return "";
        }
        throw new IllegalArgumentException("Unexpected eval class (" + ve.getClass().getName() + ")");
    }

    /**
     * @return <code>null</code> to represent blank values
     * @throws EvaluationException if ve is an ErrorEval, or if a string value cannot be converted
     */
    public static Boolean coerceValueToBoolean(ValueEval ve, boolean stringsAreBlanks) throws EvaluationException {

        if (ve == null || ve == BlankEval.instance) {
            // TODO - remove 've == null' condition once AreaEval is fixed
            return null;
        }
        if (ve instanceof BoolEval) {
            return Boolean.valueOf(((BoolEval) ve).getBooleanValue());
        }

        if (ve instanceof StringEval) {
            if (stringsAreBlanks) {
                return null;
            }
            String str = ((StringEval) ve).getStringValue();
            if (str.equalsIgnoreCase("true")) {
                return Boolean.TRUE;
            }
            if (str.equalsIgnoreCase("false")) {
                return Boolean.FALSE;
            }
            // else - string cannot be converted to boolean
            throw new EvaluationException(ErrorEval.VALUE_INVALID);
        }

        if (ve instanceof NumericValueEval) {
            NumericValueEval ne = (NumericValueEval) ve;
            double d = ne.getNumberValue();
            if (Double.isNaN(d)) {
                throw new EvaluationException(ErrorEval.VALUE_INVALID);
            }
            return Boolean.valueOf(d != 0);
        }
        if (ve instanceof ErrorEval) {
            throw new EvaluationException((ErrorEval) ve);
        }
        throw new RuntimeException("Unexpected eval (" + ve.getClass().getName() + ")");
    }
}
