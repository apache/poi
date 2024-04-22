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

package org.apache.poi.examples.hssf.eventusermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.Test;

class TestXLS2CSVmra {
    @Test
    void test() throws Exception {
        XLS2CSVmra.main(new String[] { HSSFTestDataSamples.getSampleFile("SampleSS.xls").getAbsolutePath() });
    }

    @Test
    void testWithMinCols() throws Exception {
        XLS2CSVmra.main(new String[] { HSSFTestDataSamples.getSampleFile("SampleSS.xls").getAbsolutePath(), "100" });
    }

    @Test
    void testProcess() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outStream, false, StandardCharsets.UTF_8.name());
        XLS2CSVmra cvs = new XLS2CSVmra(
                new POIFSFileSystem(new FileInputStream(HSSFTestDataSamples.getSampleFile("SampleSS.xls").getAbsolutePath())),
                out, -1);

        cvs.process();

        outStream.flush();

        assertEquals(sanitize("\n"
                + "First Sheet [1]:\n"
                + "\"Test spreadsheet\"\n"
                + "\"2nd row\",\"2nd row 2nd column\"\n"
                + "\n"
                + "\"This one is red\"\n"
                + "\n"
                + "Sheet Number 2 [2]:\n"
                + "\"Start of 2nd sheet\"\n"
                + "\"Sheet 2 row 2\"\n"
                + "\n"
                + "\"I'm in bold blue, on a yellow background\"\n"
                + "\n"
                + "\"cb=1\",\"cb=10\",\"cb=2\",\"cb=sum\"\n"
                + "1,10,2,13\n"
                + "\n"
                + "Sheet3 [3]:\n"), sanitize(new String(outStream.toByteArray(), StandardCharsets.UTF_8)));
    }

    @Test
    void testProcessNumberRecord() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outStream, false, StandardCharsets.UTF_8.name());
        XLS2CSVmra cvs = new XLS2CSVmra(
                new POIFSFileSystem(new FileInputStream(HSSFTestDataSamples.getSampleFile("empty.xls").getAbsolutePath())),
                out, -1);

        // need to call process() first to initialize members
        cvs.process();

        outStream.flush();

        assertEquals(sanitize("\n"
                + "Лист1 [1]:\n"
                + "\n"
                + "Лист2 [2]:\n"
                + "\n"
                + "Лист3 [3]:\n"), sanitize(new String(outStream.toByteArray(), StandardCharsets.UTF_8)));


        NumberRecord record = new NumberRecord();
        record.setValue(1.243);

        cvs.processRecord(record);

        outStream.flush();

        assertEquals(sanitize("\n"
                + "Лист1 [1]:\n"
                + "\n"
                + "Лист2 [2]:\n"
                + "\n"
                + "Лист3 [3]:\n"
                + "1.243"), sanitize(new String(outStream.toByteArray(), StandardCharsets.UTF_8)));
    }

    private String sanitize(String str) {
        return str.replace("\r\n", "\n");
    }
}