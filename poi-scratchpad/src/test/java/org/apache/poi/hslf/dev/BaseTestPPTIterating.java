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
package org.apache.poi.hslf.dev;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.exceptions.EncryptedPowerPointFileException;
import org.apache.poi.hslf.exceptions.OldPowerPointFormatException;
import org.apache.poi.util.IOUtils;
import org.apache.commons.io.output.NullPrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Isolated   // this test changes global static BYTE_ARRAY_MAX_OVERRIDE
public abstract class BaseTestPPTIterating {
    static final Set<String> OLD_FILES = new HashSet<>(Arrays.asList(
        "PPT95.ppt", "pp40only.ppt"
    ));

    static final Set<String> ENCRYPTED_FILES = new HashSet<>(Arrays.asList(
        "cryptoapi-proc2356.ppt",
        "Password_Protected-np-hello.ppt",
        "Password_Protected-56-hello.ppt",
        "Password_Protected-hello.ppt",
        "ppt_with_png_encrypted.ppt"
    ));

    static final Map<String,Class<? extends Throwable>> EXCLUDED = new HashMap<>();
    static {
        EXCLUDED.put("clusterfuzz-testcase-minimized-POIHSLFFuzzer-6416153805979648.ppt", Exception.class);
        EXCLUDED.put("clusterfuzz-testcase-minimized-POIHSLFFuzzer-6710128412590080.ppt", RuntimeException.class);
        EXCLUDED.put("clusterfuzz-testcase-minimized-POIFuzzer-5429732352851968.ppt", FileNotFoundException.class);
        EXCLUDED.put("clusterfuzz-testcase-minimized-POIFuzzer-5681320547975168.ppt", FileNotFoundException.class);
        EXCLUDED.put("clusterfuzz-testcase-minimized-POIHSLFFuzzer-5962760801091584.ppt", RuntimeException.class);
        EXCLUDED.put("clusterfuzz-testcase-minimized-POIHSLFFuzzer-5231088823566336.ppt", FileNotFoundException.class);
        EXCLUDED.put("clusterfuzz-testcase-minimized-POIFuzzer-6411649193738240.ppt", FileNotFoundException.class);
        EXCLUDED.put("clusterfuzz-testcase-minimized-POIHSLFFuzzer-4838893004128256.ppt", FileNotFoundException.class);
        EXCLUDED.put("clusterfuzz-testcase-minimized-POIHSLFFuzzer-4624961081573376.ppt", FileNotFoundException.class);
        EXCLUDED.put("clusterfuzz-testcase-minimized-POIHSLFFuzzer-5018229722382336.ppt", RuntimeException.class);
        EXCLUDED.put("clusterfuzz-testcase-minimized-POIHSLFFuzzer-6192650357112832.ppt", RuntimeException.class);
        EXCLUDED.put("clusterfuzz-testcase-minimized-POIHSLFFuzzer-6614960949821440.ppt", RuntimeException.class);
    }

    public static Stream<Arguments> files() {
        String dataDirName = System.getProperty(POIDataSamples.TEST_PROPERTY);
        if(dataDirName == null) {
            dataDirName = "test-data";
        }

        List<Arguments> files = new ArrayList<>();
        findFile(files, dataDirName + "/slideshow");

        return files.stream();
    }

    private final PrintStream save = System.out;

    @BeforeEach
    void setUpBase() {
        // set a higher max allocation limit as some test-files require more
        IOUtils.setByteArrayMaxOverride(5*1024*1024);

        // redirect standard out during the test to avoid spamming the console with output
        System.setOut(NullPrintStream.INSTANCE);
    }

    @AfterEach
    void tearDownBase() {
        System.setOut(save);

        // reset
        IOUtils.setByteArrayMaxOverride(-1);
    }

    private static void findFile(List<Arguments> list, String dir) {
        File dirFile = new File(dir);
        assertTrue(dirFile.exists(), "Directory does not exist: " + dirFile.getAbsolutePath());
        assertTrue(dirFile.isDirectory(), "Not a directory: " + dirFile.getAbsolutePath());

        String[] files = dirFile.list((arg0, arg1) -> arg1.toLowerCase(Locale.ROOT).endsWith(".ppt"));

        assertNotNull(files, "Did not find any ppt files in directory " + dir);

        for(String file : files) {
            list.add(Arguments.of(new File(dir, file)));
        }
    }

    @ParameterizedTest
    @MethodSource("files")
    void testAllFiles(File file) throws Exception {
        String fileName = file.getName();
        Class<? extends Throwable> t = null;
        if (EXCLUDED.containsKey(fileName)) {
            t = EXCLUDED.get(fileName);
        } else if (getFailedOldFiles().contains(fileName)) {
            t = OldPowerPointFormatException.class;
        } else if (getFailedEncryptedFiles().contains(fileName)) {
            t = EncryptedPowerPointFileException.class;
        }

        if (t == null) {
            runOneFile(file);
        } else {
            assertThrows(t, () -> runOneFile(file));
        }
    }

    abstract void runOneFile(File pFile) throws Exception;

    protected Set<String> getFailedEncryptedFiles() {
        return ENCRYPTED_FILES;
    }

    protected Set<String> getFailedOldFiles() {
        return OLD_FILES;
    }
}
