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
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.poi.benchmark.ss.InvoiceWorkbookBuilder;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
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
import org.openjdk.jmh.infra.Blackhole;

/**
 * The what-if loop: one evaluator is kept for the life of the workbook, an input cell changes,
 * the evaluator is told ({@code notifyUpdateCell}) and every formula is read again. Only the
 * formulas that depend on the changed cell are actually recomputed — the row's own chain, the
 * running total from that row down and the summary block — the rest come out of the result
 * cache. This is what {@link HSSFFormulaEvaluationBenchmark} does not measure: the cost of a
 * re-evaluation, including whatever the evaluator has to re-derive for a formula it has already
 * seen.
 * <p>
 * The workbook is the one from {@link InvoiceWorkbookBuilder}; each invocation bumps the quantity
 * of the next data row. Single thread, as the evaluator is not thread-safe.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(1)
@Threads(1)
@State(Scope.Thread)
public class HSSFFormulaReevaluationBenchmark {

    @Param({"100", "200"})
    public int rows;

    private HSSFWorkbook wb;
    private HSSFFormulaEvaluator fe;
    private List<Cell> formulaCells;
    private Sheet orders;
    private int nextRow;

    @Setup(Level.Trial)
    public void setUp() {
        wb = new InvoiceWorkbookBuilder(rows, 42L).build(new HSSFWorkbook());
        formulaCells = InvoiceWorkbookBuilder.formulaCells(wb);
        orders = wb.getSheet("Orders");
        fe = HSSFFormulaEvaluator.create(wb, null, null);
        fe.evaluateAll();
    }

    @TearDown(Level.Trial)
    public void tearDown() throws IOException {
        wb.close();
    }

    @Benchmark
    public void changeOneInputAndReevaluate(Blackhole bh) {
        Cell qty = orders.getRow(1 + nextRow).getCell(0);
        nextRow = (nextRow + 1) % rows;
        qty.setCellValue(qty.getNumericCellValue() + 1);
        fe.notifyUpdateCell(qty);
        for (Cell cell : formulaCells) {
            bh.consume(fe.evaluate(cell));
        }
    }
}
