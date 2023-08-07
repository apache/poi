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

package org.apache.poi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.examples.xssf.eventusermodel.XLSX2CSV;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestXLSX2CSV {
    private PrintStream err;
    private final UnsynchronizedByteArrayOutputStream errorBytes = UnsynchronizedByteArrayOutputStream.builder().get();

    @BeforeEach
    public void setUp() throws UnsupportedEncodingException {
        // remember and replace default error streams
        err = System.err;

        PrintStream error = new PrintStream(errorBytes, true, StandardCharsets.UTF_8.name());
        System.setErr(error);
    }

    @AfterEach
    public void tearDown() {
        // restore output-streams again
        System.setErr(err);

        // Print out found error
        if (errorBytes.size() > 0) {
            System.err.println("Had stderr: " + errorBytes.toString(StandardCharsets.UTF_8));
        }
    }

    @Test
    public void testNoArgument() throws Exception {
        // returns with some System.err
        XLSX2CSV.main(new String[0]);

        String output = errorBytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("XLSX2CSV <xlsx file>"), "Had: " + output);
    }

    @Test
    public void testInvalidFile() throws Exception {
        // returns with some System.err
        XLSX2CSV.main(new String[] { "not-existing-file.xlsx" });

        String output = errorBytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Not found or not a file: not-existing-file.xlsx"), "Had: " + output);
    }

    @Test
    public void testSampleFile() throws Exception {
        final UnsynchronizedByteArrayOutputStream outputBytes = UnsynchronizedByteArrayOutputStream.builder().get();
        PrintStream out = new PrintStream(outputBytes, true, StandardCharsets.UTF_8.name());

        // The package open is instantaneous, as it should be.
        try (OPCPackage p = OPCPackage.open(XSSFTestDataSamples.getSampleFile("sample.xlsx").getAbsolutePath(), PackageAccess.READ)) {
            XLSX2CSV xlsx2csv = new XLSX2CSV(p, out, -1);
            xlsx2csv.process();
        }

        String errorOutput = errorBytes.toString(StandardCharsets.UTF_8);
        assertEquals("", errorOutput);

        String output = outputBytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("\"Lorem\",111"), "Had: " + output);
        assertTrue(output.contains(",\"hello, xssf\",,\"hello, xssf\""), "Had: " + output);
    }

    @Test
    public void testInvalidSampleFile() throws Exception {
        final UnsynchronizedByteArrayOutputStream outputBytes = UnsynchronizedByteArrayOutputStream.builder().get();
        PrintStream out = new PrintStream(outputBytes, true, StandardCharsets.UTF_8.name());

        // The package open is instantaneous, as it should be.
        try (OPCPackage p = OPCPackage.open(XSSFTestDataSamples.getSampleFile("clusterfuzz-testcase-minimized-XLSX2CSVFuzzer-5025401116950528.xlsx").getAbsolutePath(), PackageAccess.READ)) {
            XLSX2CSV xlsx2csv = new XLSX2CSV(p, out, -1);
            assertThrows(POIXMLException.class,
                    xlsx2csv::process);
        }

        String errorOutput = errorBytes.toString(StandardCharsets.UTF_8);
        assertEquals("", errorOutput);

        String output = outputBytes.toString(StandardCharsets.UTF_8);
        assertEquals("", output, "Had: " + output);
    }

    @Test
    public void testMinColumns() throws Exception {
        final UnsynchronizedByteArrayOutputStream outputBytes = UnsynchronizedByteArrayOutputStream.builder().get();
        PrintStream out = new PrintStream(outputBytes, true, StandardCharsets.UTF_8.name());

        // The package open is instantaneous, as it should be.
        try (OPCPackage p = OPCPackage.open(XSSFTestDataSamples.getSampleFile("sample.xlsx").getAbsolutePath(), PackageAccess.READ)) {
            XLSX2CSV xlsx2csv = new XLSX2CSV(p, out, 5);
            xlsx2csv.process();
        }

        String errorOutput = errorBytes.toString(StandardCharsets.UTF_8);
        assertEquals("", errorOutput);

        String output = outputBytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("\"Lorem\",111,,,"), "Had: " + output);
        assertTrue(output.contains(",\"hello, xssf\",,\"hello, xssf\","), "Had: " + output);
    }
}
