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

import java.util.Collection;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.udf.UDFFinder;

/**
 * A workbook-free formula engine that evaluates Excel formulas against a single
 * logical row of typed inputs.
 *
 * <p>Unlike {@link FormulaEvaluator}, no {@link Workbook}, {@link Sheet}, {@link Row}
 * or {@link Cell} objects are involved. Inputs are declared once, in order; each input
 * maps to a fixed cell position of a virtual single-row sheet (first input = {@code A1},
 * second = {@code B1}, ...). Formulas are plain Excel formulas referencing those
 * positions (or any other position, which evaluates as an empty cell).</p>
 *
 * <p>Evaluation uses the very same engine as workbook-backed formula evaluation
 * ({@code WorkbookEvaluator}), so results are identical to what Excel/POI would
 * compute for the same values in a real workbook, including error propagation
 * ({@code #DIV/0!}, {@code #N/A}, ...).</p>
 *
 * <p>Typical streaming usage (compile once, evaluate once per row):</p>
 * <pre>{@code
 * StandaloneFormulaEngine engine = StandaloneFormulaEngine.newBuilder()
 *         .inputs("amount", "price")
 *         .build();
 * CompiledFormula formula = engine.compile("amount*price");
 *
 * StandaloneFormulaEvaluator evaluator = formula.newEvaluator(); // not thread-safe, reuse per thread
 * evaluator.setNumber(0, 2.5).setNumber(1, 4.0);
 * CellValue result = evaluator.evaluate();
 * }</pre>
 *
 * <p>Instances of this interface and of {@link CompiledFormula} are thread-safe;
 * {@link StandaloneFormulaEvaluator} instances are not.</p>
 *
 * <p>Limitations inherited from the virtual single-sheet backend: no defined names,
 * no sheet-qualified ({@code Sheet2!A1}) or 3-D references, no external workbook
 * references, no tables, and {@code INDIRECT} only resolves the canonical sheet
 * name {@code Sheet1}. Functions not implemented by POI (e.g. {@code DATEDIF})
 * throw {@link org.apache.poi.ss.formula.eval.NotImplementedException} at evaluation time.</p>
 *
 * @since 6.0.0
 */
public interface StandaloneFormulaEngine {

    /**
     * @return a new builder for configuring the inputs of a standalone formula engine
     */
    static Builder newBuilder() {
        return new org.apache.poi.ss.formula.StandaloneFormulaEngineImpl();
    }

    /**
     * Parses and compiles a formula against the declared inputs.
     *
     * <p>The returned {@link CompiledFormula} can be cached and shared freely;
     * compilation is the only expensive step (parsing).</p>
     *
     * @param formula the formula in Excel syntax, e.g. {@code "amount*price"}
     * @return a thread-safe compiled representation of the formula
     * @throws FormulaParseException if the formula cannot be parsed (syntax error,
     *         unknown identifier, sheet-qualified reference, ...)
     * @throws IllegalStateException if {@link Builder#build()} was not called yet
     */
    CompiledFormula compile(String formula) throws FormulaParseException;

    /**
     * Builder for {@link StandaloneFormulaEngine}.
     */
    interface Builder {

        /**
         * Adds one named input. The first added input maps to virtual cell {@code A1},
         * the second to {@code B1}, and so on.
         *
         * <p>Names must follow the Excel defined-name rules: at most 255 characters; first
         * character a letter, underscore or backslash; remaining characters letters, digits,
         * periods, underscores or backslashes; not the reserved shorthands {@code R} or
         * {@code C}; not the literals {@code TRUE} or {@code FALSE}. Additionally, at
         * {@link #build()} time, names that look like an A1-style cell reference of the
         * configured {@link SpreadsheetVersion} (e.g. {@code Q1}) are rejected, as they
         * would otherwise be parsed as a cell instead of resolving to the input.</p>
         *
         * @param name the name of the input, used by {@link CompiledFormula#getInputIndex(String)}
         * @return this builder
         * @throws IllegalArgumentException if the name is null, blank or violates the rules above
         */
        Builder input(String name);

        /**
         * Adds several named inputs, in order.
         *
         * @param names the names of the inputs
         * @return this builder
         * @throws IllegalArgumentException if one of the names violates the rules
         *         documented in {@link #input(String)}
         */
        Builder inputs(String... names);

        /**
         * Adds several named inputs, in order.
         *
         * @param names the names of the inputs
         * @return this builder
         * @throws IllegalArgumentException if one of the names violates the rules
         *         documented in {@link #input(String)}
         */
        Builder inputs(Collection<String> names);

        /**
         * Sets the spreadsheet version used for parsing and evaluation bounds.
         *
         * @param version the spreadsheet version, defaults to {@link SpreadsheetVersion#EXCEL2007}
         * @return this builder
         */
        Builder spreadsheetVersion(SpreadsheetVersion version);

        /**
         * Sets the finder for user-defined functions.
         *
         * @param udfFinder the UDF finder, defaults to the analysis tool pak
         * @return this builder
         */
        Builder udfFinder(UDFFinder udfFinder);

        /**
         * Validates the configuration and returns the engine.
         *
         * @return a thread-safe engine
         * @throws IllegalArgumentException if an input name is null, empty, violates the
         *         rules documented in {@link #input(String)} or is an A1-style cell
         *         reference of the configured spreadsheet version, if it is a
         *         case-insensitive duplicate, or if the number of inputs exceeds
         *         the column limit of the configured spreadsheet version
         */
        StandaloneFormulaEngine build();
    }
}
