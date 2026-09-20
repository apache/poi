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

package org.apache.poi.benchmark.xssf;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.poi.benchmark.ss.LookupWorkbookBuilder;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
 * One full evaluation pass over a {@link XSSFWorkbook} whose formulas keep reading the same large
 * ranges (see {@link LookupWorkbookBuilder}): {@code VLOOKUP}s over a {@code tableRows}-item
 * table and {@code SUMIF}/{@code COUNTIF}s over the whole data column from every row. Where
 * {@link XSSFFormulaEvaluationBenchmark} measures a workbook in which each formula mostly reads its
 * own row, this one measures the evaluator's plain value cache: the pass makes on the order of
 * {@code rows * (tableRows / 2 + 2 * rows)} reads of only {@code 2 * (tableRows + rows)} distinct
 * plain cells.
 * <p>
 * Setup, evaluator lifetime and threading are as in {@link XSSFFormulaEvaluationBenchmark}: the
 * workbook is built once per trial, every invocation uses a fresh evaluator, single thread.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 6, time = 3)
@Measurement(iterations = 5, time = 3)
@Fork(1)
@Threads(1)
@State(Scope.Thread)
public class XSSFLookupEvaluationBenchmark {

    @Param({"100", "200"})
    public int rows;

    @Param({"1000"})
    public int tableRows;

    /**
     * {@code DEFAULT} lets the evaluator record which cells each formula depends on (needed for
     * {@code notifyUpdateCell} to work later); {@code TOTALLY_IMMUTABLE} switches that
     * book-keeping off, which is the right setting for evaluate-once workloads.
     */
    @Param({"DEFAULT", "TOTALLY_IMMUTABLE"})
    public String stability;

    private XSSFWorkbook wb;
    private IStabilityClassifier classifier;

    @Setup(Level.Trial)
    public void setUp() {
        wb = new LookupWorkbookBuilder(rows, tableRows, 42L).build(new XSSFWorkbook());
        classifier = "TOTALLY_IMMUTABLE".equals(stability) ? IStabilityClassifier.TOTALLY_IMMUTABLE : null;
    }

    @TearDown(Level.Trial)
    public void tearDown() throws IOException {
        wb.close();
    }

    /** one full pass: every formula cell is evaluated and its result written back into the cell */
    @Benchmark
    public void evaluateAll() {
        XSSFFormulaEvaluator.create(wb, classifier, null).evaluateAll();
    }
}
