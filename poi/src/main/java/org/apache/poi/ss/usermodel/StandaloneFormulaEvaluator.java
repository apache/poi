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

package org.apache.poi.ss.usermodel;

import java.util.Date;

/**
 * Evaluates a {@link CompiledFormula} against a row of bound input values.
 *
 * <p>An evaluator holds the mutable state of one logical row. It is
 * <strong>not thread-safe</strong>: confine it to a single thread and reuse it
 * across rows (values persist until overwritten or {@link #reset()} is called).</p>
 *
 * <p>Inputs that are never set behave like empty Excel cells
 * (blank - which reads as {@code 0} in numeric contexts, {@code FALSE} in boolean
 * contexts and {@code ""} in text contexts).</p>
 *
 * <p>{@link #evaluate()} returns an Excel-like {@link LightCellValue}: results are
 * never blank (Excel converts a blank result to {@code 0}) and errors are returned as
 * {@link CellType#ERROR} values ({@code 1/0} yields {@code #DIV/0!}) rather than
 * exceptions. {@link FormulaError#NA} can also be produced as an input via
 * {@link #setError(int, FormulaError)} to model "not available" data.
 * {@link CellValue} implements {@link LightCellValue}, so code written against the
 * result of {@link FormulaEvaluator#evaluate(Cell)} keeps working.</p>
 */
public interface StandaloneFormulaEvaluator {

    /**
     * Binds a numeric value to the input at the given index.
     *
     * @param index the input index, between 0 (inclusive) and the number of declared inputs
     * @param value the value
     * @return this evaluator, for chaining
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    StandaloneFormulaEvaluator setNumber(int index, double value);

    /**
     * Binds a text value to the input at the given index.
     *
     * @param index the input index, between 0 (inclusive) and the number of declared inputs
     * @param value the value
     * @return this evaluator, for chaining
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    StandaloneFormulaEvaluator setString(int index, String value);

    /**
     * Binds a boolean value to the input at the given index.
     *
     * @param index the input index, between 0 (inclusive) and the number of declared inputs
     * @param value the value
     * @return this evaluator, for chaining
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    StandaloneFormulaEvaluator setBoolean(int index, boolean value);

    /**
     * Binds a date value to the input at the given index. The date is stored as an
     * Excel serial number (1900 date system).
     *
     * @param index the input index, between 0 (inclusive) and the number of declared inputs
     * @param value the value
     * @return this evaluator, for chaining
     * @throws IndexOutOfBoundsException if the index is out of range
     * @throws IllegalArgumentException if the date is before the Excel epoch (serial number 0)
     */
    StandaloneFormulaEvaluator setDate(int index, Date value);

    /**
     * Binds an Excel error value to the input at the given index, e.g.
     * {@link FormulaError#NA} to model a missing value that propagates as
     * {@code #N/A} through the formula.
     *
     * @param index the input index, between 0 (inclusive) and the number of declared inputs
     * @param error the error
     * @return this evaluator, for chaining
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    StandaloneFormulaEvaluator setError(int index, FormulaError error);

    /**
     * Clears the input at the given index, making it behave as an empty cell.
     *
     * @param index the input index, between 0 (inclusive) and the number of declared inputs
     * @return this evaluator, for chaining
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    StandaloneFormulaEvaluator setBlank(int index);

    /**
     * Binds a value to the input at the given index, dispatching on its runtime type:
     * {@link Number} (as numeric), {@link String}, {@link Boolean}, {@link Date},
     * {@link FormulaError} (as error), {@code null} (as blank).
     *
     * @param index the input index, between 0 (inclusive) and the number of declared inputs
     * @param value the value
     * @return this evaluator, for chaining
     * @throws IndexOutOfBoundsException if the index is out of range
     * @throws IllegalArgumentException if the value type is not supported
     */
    StandaloneFormulaEvaluator setValue(int index, Object value);

    /**
     * Binds all inputs positionally, equivalent to calling
     * {@link #setValue(int, Object)} for each entry.
     *
     * @param values the values, one per declared input
     * @return this evaluator, for chaining
     * @throws IllegalArgumentException if more values than declared inputs are supplied
     */
    StandaloneFormulaEvaluator setValues(Object... values);

    /**
     * Clears all inputs, making every input behave as an empty cell.
     *
     * @return this evaluator, for chaining
     */
    StandaloneFormulaEvaluator reset();

    /**
     * Evaluates the compiled formula with the currently bound inputs.
     *
     * @return the evaluation result, never {@code null}
     * @throws org.apache.poi.ss.formula.eval.NotImplementedException if the formula uses
     *         a function that POI does not implement
     */
    LightCellValue evaluate();

    /**
     * Binds all inputs positionally (see {@link #setValues(Object...)}) and evaluates
     * the compiled formula.
     *
     * @param values the values, one per declared input
     * @return the evaluation result, never {@code null}
     */
    LightCellValue evaluate(Object... values);
}
