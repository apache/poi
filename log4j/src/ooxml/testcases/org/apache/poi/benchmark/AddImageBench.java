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

package org.apache.poi.benchmark;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS )
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class AddImageBench {

    private Workbook wb;
    private CreationHelper helper;
    private byte[] bytes;
    private Sheet sheet;

    @Setup(Level.Iteration)
    public void doit() {
        wb = new XSSFWorkbook();
        helper = wb.getCreationHelper();
        //add a picture in this workbook.

        bytes = HSSFTestDataSamples.getTestDataFileContent("45829.png");
        sheet = wb.createSheet();
    }

    @Benchmark
    public void benchCreatePicture() throws Exception {
        Drawing drawing = sheet.createDrawingPatriarch();

        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(1);
        anchor.setRow1(1);
        drawing.createPicture(anchor, wb.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG));

    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + AddImageBench.class.getSimpleName() + ".*")
                .addProfiler(StackProfiler.class)
                .addProfiler(GCProfiler.class)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
