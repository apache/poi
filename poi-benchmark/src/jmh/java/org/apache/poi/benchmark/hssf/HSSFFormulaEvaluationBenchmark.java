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

package org.apache.poi.benchmark.hssf;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.poi.benchmark.ss.InvoiceWorkbookBuilder;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.IStabilityClassifier;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

/**
 * How long does one full evaluation pass over every formula of an {@link HSSFWorkbook} take?
 * <p>
 * The workbook (see {@link InvoiceWorkbookBuilder}) is built once per trial, so constructing it is
 * not part of the measurement. Each invocation creates a new evaluator and evaluates every formula
 * cell once with it, so the cost measured is that of a {@code WorkbookEvaluator} computing every
 * formula from scratch, including reading the plain input cells and building its caches.
 * <p>
 * An evaluator caches its results. It can keep the cache right across changes to individual cells
 * if it is told about them ({@code notifyUpdateCell}, {@code notifySetFormula},
 * {@code notifyDeleteCell}), but not across big changes such as adding or removing rows and
 * columns, which move cells and rewrite formulas throughout the workbook. After such changes a
 * new evaluator instance is recommended, which is what this benchmark models: one fresh evaluator
 * per full pass.
 * <p>
 * A workbook and its evaluator are not thread-safe, so the benchmark runs on a single thread.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(1)
@Threads(1)
@State(Scope.Thread)
public class HSSFFormulaEvaluationBenchmark {

    @Param({"100", "200"})
    public int rows;

    /**
     * {@code DEFAULT} lets the evaluator record which cells each formula depends on (needed for
     * {@code notifyUpdateCell} to work later); {@code TOTALLY_IMMUTABLE} switches that
     * book-keeping off, which is the right setting for evaluate-once workloads.
     */
    @Param({"DEFAULT", "TOTALLY_IMMUTABLE"})
    public String stability;

    private HSSFWorkbook wb;
    private IStabilityClassifier classifier;

    @Setup(Level.Trial)
    public void setUp() {
        wb = new InvoiceWorkbookBuilder(rows, 42L).build(new HSSFWorkbook());
        classifier = "TOTALLY_IMMUTABLE".equals(stability) ? IStabilityClassifier.TOTALLY_IMMUTABLE : null;
    }

    private HSSFFormulaEvaluator newEvaluator() {
        return HSSFFormulaEvaluator.create(wb, classifier, null);
    }

    @TearDown(Level.Trial)
    public void tearDown() throws IOException {
        wb.close();
    }

    /**
     * One full pass: every formula cell is evaluated and its result is written back into the
     * cell, as {@code FormulaEvaluator.evaluateAll()} does. (Evaluating the cells without writing
     * the results back was measured to cost the same, so there is no separate benchmark for it.)
     */
    @Benchmark
    public void evaluateAll() {
        newEvaluator().evaluateAll();
    }
}
