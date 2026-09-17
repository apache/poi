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

import org.apache.poi.ss.formula.eval.ErrorEval;

/**
 * A lightweight, typed view of the result of a formula evaluation - the sealed
 * counterpart of {@link CellValue}.
 *
 * <p>Where {@link CellValue} carries a slot for every possible value type,
 * implementations of this interface hold exactly the value they represent: a
 * {@link NumberValue} is 24 bytes, a {@link TextValue} 16 bytes, and the boolean
 * and error results are cached singletons, so they are allocated once. Reading the
 * accessors of a type the value does not represent yields the same neutral defaults
 * as {@link CellValue} ({@code 0.0}, {@code null}, {@code false}, {@code 0}), so
 * code written against either shape behaves identically.</p>
 *
 * <p>{@link CellValue} implements this interface, hence legacy evaluation results
 * can flow through {@code LightCellValue} typed APIs unchanged, and new code can
 * dispatch over the sealed hierarchy:</p>
 * <pre>{@code
 * LightCellValue result = evaluator.evaluate();
 * if (result instanceof LightCellValue.NumberValue number) {
 *     use(number.getNumberValue());
 * } else if (result instanceof LightCellValue.ErrorValue error) {
 *     handle(error.getErrorCode());
 * } else {
 *     handle(result);
 * }
 * }</pre>
 *
 * @since 6.0.0
 */
public sealed interface LightCellValue permits LightCellValue.NumberValue, LightCellValue.TextValue,
        LightCellValue.BooleanValue, LightCellValue.ErrorValue, CellValue {

    /**
     * @return the cell type of this value
     */
    CellType getCellType();

    /**
     * @return 0.0 unless this is a {@link NumberValue}
     */
    default double getNumberValue() {
        return 0.0;
    }

    /**
     * @return {@code null} unless this is a {@link TextValue}
     */
    default String getStringValue() {
        return null;
    }

    /**
     * @return {@code false} unless this is a {@link BooleanValue}
     */
    default boolean getBooleanValue() {
        return false;
    }

    /**
     * @return 0 unless this is an {@link ErrorValue}
     */
    default byte getErrorValue() {
        return 0;
    }

    /**
     * @return a human readable representation, matching {@link CellValue#formatAsString()}
     */
    String formatAsString();

    /**
     * Wraps a numeric value.
     *
     * @param value the value
     * @return a new {@link NumberValue}
     */
    static LightCellValue of(double value) {
        return new NumberValue(value);
    }

    /**
     * Wraps a text value.
     *
     * @param value the value, may be {@code null} like in {@link CellValue#CellValue(String)}
     * @return a new {@link TextValue}
     */
    static LightCellValue of(String value) {
        return new TextValue(value);
    }

    /**
     * Wraps a boolean value. The two possible results are cached.
     *
     * @param value the value
     * @return {@link #TRUE} or {@link #FALSE}
     */
    static LightCellValue of(boolean value) {
        return value ? BooleanValue.TRUE : BooleanValue.FALSE;
    }

    /**
     * Wraps an Excel error code. Common codes are cached.
     *
     * @param errorCode the Excel error code, e.g. the code of {@link FormulaError#NA}
     * @return an {@link ErrorValue}
     */
    static LightCellValue error(int errorCode) {
        return ErrorValue.of(errorCode);
    }

    /**
     * The cached boolean {@code TRUE} result.
     */
    LightCellValue TRUE = BooleanValue.TRUE;

    /**
     * The cached boolean {@code FALSE} result.
     */
    LightCellValue FALSE = BooleanValue.FALSE;

    /**
     * A numeric (floating point) value.
     *
     * @since 6.0.0
     */
    record NumberValue(double value) implements LightCellValue {

        @Override
        public double getNumberValue() {
            return value;
        }

        @Override
        public CellType getCellType() {
            return CellType.NUMERIC;
        }

        @Override
        public String formatAsString() {
            return String.valueOf(value);
        }

        @Override
        public String toString() {
            return getClass().getName() + " [" + formatAsString() + "]";
        }
    }

    /**
     * A text value.
     *
     * @since 6.0.0
     */
    record TextValue(String value) implements LightCellValue {

        @Override
        public String getStringValue() {
            return value;
        }

        @Override
        public CellType getCellType() {
            return CellType.STRING;
        }

        @Override
        public String formatAsString() {
            return '"' + value + '"';
        }

        @Override
        public String toString() {
            return getClass().getName() + " [" + formatAsString() + "]";
        }
    }

/**
     * A boolean value.
     *
     * @since 6.0.0
     */
    record BooleanValue(boolean value) implements LightCellValue {

        /**
         * The cached boolean {@code TRUE} result.
         */
        public static final BooleanValue TRUE = new BooleanValue(true);

        /**
         * The cached boolean {@code FALSE} result.
         */
        public static final BooleanValue FALSE = new BooleanValue(false);

        /**
         * Returns the cached instance for the given value.
         *
         * @param value the value
         * @return {@link #TRUE} or {@link #FALSE}
         */
        public static BooleanValue of(boolean value) {
            return value ? TRUE : FALSE;
        }

        @Override
        public boolean getBooleanValue() {
            return value;
        }

        @Override
        public CellType getCellType() {
            return CellType.BOOLEAN;
        }

        @Override
        public String formatAsString() {
            return value ? "TRUE" : "FALSE";
        }

        @Override
        public String toString() {
            return getClass().getName() + " [" + formatAsString() + "]";
        }
    }

    /**
     * An Excel error value, e.g. {@code #DIV/0!}. Instances for the standard error
     * codes are cached; use {@link LightCellValue#error(int)} to benefit from it.
     *
     * @since 6.0.0
     */
    record ErrorValue(int code) implements LightCellValue {

        private static final int CACHE_SIZE = 64;
        private static final ErrorValue[] CACHE = new ErrorValue[CACHE_SIZE];

        static {
            for (int i = 0; i < CACHE_SIZE; i++) {
                CACHE[i] = new ErrorValue(i);
            }
        }

        /**
         * Returns a cached instance for standard error codes, a new instance otherwise.
         *
         * @param code the Excel error code
         * @return an {@link ErrorValue}
         */
        public static ErrorValue of(int code) {
            if (code >= 0 && code < CACHE_SIZE) {
                return CACHE[code];
            }
            return new ErrorValue(code);
        }

        @Override
        public byte getErrorValue() {
            return (byte) code;
        }

        @Override
        public CellType getCellType() {
            return CellType.ERROR;
        }

        @Override
        public String formatAsString() {
            return ErrorEval.getText(code);
        }

        @Override
        public String toString() {
            return getClass().getName() + " [" + formatAsString() + "]";
        }
    }
}
