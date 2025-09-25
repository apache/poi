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
package org.apache.poi.hssf.dev;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.util.RecordFormatException;
import org.apache.tools.ant.DirectoryScanner;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Base class for integration-style tests which iterate over all test-files
 * and execute the same action to find out if any change breaks these applications.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
public abstract class BaseTestIteratingXLS {
    private static final String[] XLS_INCLUDES = {
        "spreadsheet/*.xls", "hpsf/*.xls"
    };

    public Stream<Arguments> files() {
        String dataDirName = System.getProperty(POIDataSamples.TEST_PROPERTY,
                new File("test-data").exists() ? "test-data" : "../test-data");

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(dataDirName);
        scanner.setIncludes(XLS_INCLUDES);
        scanner.scan();

        final Map<String, Class<? extends Throwable>> exc = getExcludes();
        Function<String,Arguments> mapArg = (s) -> {
            File f = new File(dataDirName, s);
            return Arguments.of(f, exc.get(f.getName()));
        };

        return Arrays.stream(scanner.getIncludedFiles()).map(mapArg);
    }

    protected Map<String,Class<? extends Throwable>> getExcludes() {
        Map<String, Class<? extends Throwable>> excludes = new HashMap<>();
        // Biff 2 / Excel 2, pre-OLE2
        excludes.put("testEXCEL_2.xls", OldExcelFormatException.class);
        // Biff 3 / Excel 3, pre-OLE2
        excludes.put("testEXCEL_3.xls", OldExcelFormatException.class);
        // Biff 4 / Excel 4, pre-OLE2
        excludes.put("testEXCEL_4.xls", OldExcelFormatException.class);
        // Biff 5 / Excel 5
        excludes.put("testEXCEL_5.xls", OldExcelFormatException.class);
        // Biff 5 / Excel 5
        excludes.put("60284.xls", OldExcelFormatException.class);
        // Biff 5 / Excel 95
        excludes.put("testEXCEL_95.xls", OldExcelFormatException.class);
        excludes.put("46904.xls", OldExcelFormatException.class);
        excludes.put("59074.xls", OldExcelFormatException.class);
        excludes.put("61300.xls", RecordFormatException.class);
        // BIFF 5
        excludes.put("64130.xls", OldExcelFormatException.class);
        // fuzzed binaries
        excludes.put("clusterfuzz-testcase-minimized-POIHSSFFuzzer-6322470200934400.xls", RuntimeException.class);
        excludes.put("clusterfuzz-testcase-minimized-POIHSSFFuzzer-4819588401201152.xls", RuntimeException.class);
        excludes.put("clusterfuzz-testcase-minimized-POIHSSFFuzzer-6537773940867072.xls", RuntimeException.class);
        excludes.put("clusterfuzz-testcase-minimized-POIHSSFFuzzer-4651309315719168.xls", RuntimeException.class);
        return excludes;
    }

    @ParameterizedTest
    @MethodSource("files")
    void testMain(File file, Class<? extends Throwable> t) throws Exception {
        // avoid running files leftover from previous failed runs
        // or created by tests running in parallel
        // otherwise this would cause sporadic failures with
        // parallel test execution
        if(file.getName().endsWith("-saved.xls")) {
            return;
        }

        Executable ex = () -> runOneFile(file);
        if (t == null) {
            assertDoesNotThrow(ex, "Failing file: " + file);
        } else {
            assertThrows(t, ex, "Failing file: " + file);
        }
    }

    abstract void runOneFile(File pFile) throws Exception;
}
