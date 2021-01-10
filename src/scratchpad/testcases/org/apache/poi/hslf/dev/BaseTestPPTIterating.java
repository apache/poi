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

import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
import org.apache.poi.util.NullPrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public abstract class BaseTestPPTIterating {
    protected static final Set<String> OLD_FILES = new HashSet<>();
    static {
        OLD_FILES.add("PPT95.ppt");
        OLD_FILES.add("pp40only.ppt");
    }

    protected static final Set<String> ENCRYPTED_FILES = new HashSet<>();
    static {
        ENCRYPTED_FILES.add("cryptoapi-proc2356.ppt");
        ENCRYPTED_FILES.add("Password_Protected-np-hello.ppt");
        ENCRYPTED_FILES.add("Password_Protected-56-hello.ppt");
        ENCRYPTED_FILES.add("Password_Protected-hello.ppt");
    }

    protected static final Map<String,Class<? extends Throwable>> EXCLUDED =
            new HashMap<>();

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
    void setUpBase() throws UnsupportedEncodingException {
        // set a higher max allocation limit as some test-files require more
        IOUtils.setByteArrayMaxOverride(5*1024*1024);

        // redirect standard out during the test to avoid spamming the console with output
        System.setOut(new NullPrintStream());
    }

    @AfterEach
    void tearDownBase() {
        System.setOut(save);

        // reset
        IOUtils.setByteArrayMaxOverride(-1);
    }

    private static void findFile(List<Arguments> list, String dir) {
        String[] files = new File(dir).list((arg0, arg1) -> arg1.toLowerCase(Locale.ROOT).endsWith(".ppt"));

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
