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

import java.util.Date;
import java.util.Objects;

import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.NumericValueEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.StringValueEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.LightCellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.StandaloneFormulaEvaluator;
import org.apache.poi.util.Internal;

/**
 * The evaluator holding one logical row of inputs for a {@link CompiledFormulaImpl}.
 *
 * <p>Once set up, evaluation reuses the same {@link WorkbookEvaluator},
 * {@link OperationEvaluationContext} and {@link EvaluationTracker} for every row:
 * nothing is allocated per row besides the bound values and the evaluation
 * temporaries of the shared interpreter.</p>
 */
@Internal
final class StandaloneFormulaEvaluatorImpl implements StandaloneFormulaEvaluator {

    private final CompiledFormulaImpl _formula;
    private final VirtualFormulaWorkbook _workbook;
    private final VirtualEvaluationSheet _sheet;
    private final WorkbookEvaluator _evaluator;
    private final OperationEvaluationContext _context;
    private final int _inputCount;

    StandaloneFormulaEvaluatorImpl(CompiledFormulaImpl formula) {
        StandaloneFormulaEngineImpl engine = formula.engine();
        _formula = formula;
        _inputCount = engine.inputNames().length;
        _sheet = new VirtualEvaluationSheet(_inputCount);
        _workbook = new VirtualFormulaWorkbook(engine.version(), engine.udfFinder(), _sheet, engine.inputNames());
        _evaluator = new WorkbookEvaluator(_workbook, IStabilityClassifier.TOTALLY_IMMUTABLE, null);
        _context = new OperationEvaluationContext(_evaluator, _workbook, 0, 0, _inputCount,
                new EvaluationTracker(new EvaluationCache(null)));
    }

    @Override
    public StandaloneFormulaEvaluator setNumber(int index, double value) {
        values()[checkIndex(index)] = new NumberEval(value);
        return this;
    }

    @Override
    public StandaloneFormulaEvaluator setString(int index, String value) {
        Objects.requireNonNull(value, "value must not be null, use setBlank(int) instead");
        values()[checkIndex(index)] = new StringEval(value);
        return this;
    }

    @Override
    public StandaloneFormulaEvaluator setBoolean(int index, boolean value) {
        values()[checkIndex(index)] = BoolEval.valueOf(value);
        return this;
    }

    @Override
    public StandaloneFormulaEvaluator setDate(int index, Date value) {
        Objects.requireNonNull(value, "value must not be null, use setBlank(int) instead");
        double excelDate = DateUtil.getExcelDate(value);
        if (excelDate < 0.1) {
            throw new IllegalArgumentException("Invalid date: " + value);
        }
        return setNumber(index, excelDate);
    }

    @Override
    public StandaloneFormulaEvaluator setError(int index, FormulaError error) {
        Objects.requireNonNull(error, "error must not be null");
        values()[checkIndex(index)] = ErrorEval.valueOf(error.getCode());
        return this;
    }

    @Override
    public StandaloneFormulaEvaluator setBlank(int index) {
        values()[checkIndex(index)] = null;
        return this;
    }

    @Override
    public StandaloneFormulaEvaluator setValue(int index, Object value) {
        if (value instanceof Number number) {
            return setNumber(index, number.doubleValue());
        }
        if (value instanceof String string) {
            return setString(index, string);
        }
        if (value instanceof Boolean bool) {
            return setBoolean(index, bool);
        }
        if (value instanceof Date date) {
            return setDate(index, date);
        }
        if (value instanceof FormulaError error) {
            return setError(index, error);
        }
        if (value == null) {
            return setBlank(index);
        }
        throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
    }

    @Override
    public StandaloneFormulaEvaluator setValues(Object... values) {
        if (values.length > _inputCount) {
            throw new IllegalArgumentException("Got " + values.length + " values for " + _inputCount + " inputs");
        }
        for (int i = 0; i < values.length; i++) {
            setValue(i, values[i]);
        }
        return this;
    }

    @Override
    public StandaloneFormulaEvaluator reset() {
        ValueEval[] values = values();
        for (int i = 0; i < values.length; i++) {
            values[i] = null;
        }
        return this;
    }

    @Override
    public LightCellValue evaluate() {
        ValueEval result = _evaluator.evaluateFormula(_context, _formula.tokens());
        return toLightCellValue(result);
    }

    @Override
    public LightCellValue evaluate(Object... values) {
        setValues(values);
        return evaluate();
    }

    private int checkIndex(int index) {
        if (index < 0 || index >= _inputCount) {
            throw new IndexOutOfBoundsException("Input index " + index + " out of range 0.." + (_inputCount - 1));
        }
        return index;
    }

    private ValueEval[] values() {
        return _sheet.values();
    }

    private static LightCellValue toLightCellValue(ValueEval eval) {
        if (eval instanceof BoolEval boolEval) {
            return LightCellValue.of(boolEval.getBooleanValue());
        }
        if (eval instanceof NumericValueEval numericValueEval) {
            return LightCellValue.of(numericValueEval.getNumberValue());
        }
        if (eval instanceof StringValueEval stringValueEval) {
            return LightCellValue.of(stringValueEval.getStringValue());
        }
        if (eval instanceof ErrorEval errorEval) {
            return LightCellValue.error(errorEval.getErrorCode());
        }
        throw new IllegalStateException("Unexpected eval class (" + eval.getClass().getName() + ")");
    }
}
