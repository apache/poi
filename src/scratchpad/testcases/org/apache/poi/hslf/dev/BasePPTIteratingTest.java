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

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.exceptions.EncryptedPowerPointFileException;
import org.apache.poi.hslf.exceptions.OldPowerPointFormatException;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.NullOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public abstract class BasePPTIteratingTest {
    protected static final OutputStream NULL_OUTPUT_STREAM = new NullOutputStream();

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

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected static final Map<String,Class<? extends Throwable>> EXCLUDED =
            new HashMap<>();

    @Parameterized.Parameters(name="{index}: {0}")
    public static Iterable<Object[]> files() {
        String dataDirName = System.getProperty(POIDataSamples.TEST_PROPERTY);
        if(dataDirName == null) {
            dataDirName = "test-data";
        }

        List<Object[]> files = new ArrayList<>();
        findFile(files, dataDirName + "/slideshow");

        return files;
    }

    private final PrintStream save = System.out;

    @Before
    public void setUpBase() throws UnsupportedEncodingException {
        // set a higher max allocation limit as some test-files require more
        IOUtils.setByteArrayMaxOverride(5*1024*1024);

        // redirect standard out during the test to avoid spamming the console with output
        System.setOut(new PrintStream(NULL_OUTPUT_STREAM, true, LocaleUtil.CHARSET_1252.name()));
    }

    @After
    public void tearDownBase() {
        System.setOut(save);

        // reset
        IOUtils.setByteArrayMaxOverride(-1);
    }

    private static void findFile(List<Object[]> list, String dir) {
        String[] files = new File(dir).list((arg0, arg1) -> arg1.toLowerCase(Locale.ROOT).endsWith(".ppt"));

        assertNotNull("Did not find any ppt files in directory " + dir, files);

        for(String file : files) {
            list.add(new Object[] { new File(dir, file) });
        }
    }

    @Parameterized.Parameter
    public File file;

    @Test
    public void testAllFiles() throws Exception {
        String fileName = file.getName();
        if (EXCLUDED.containsKey(fileName)) {
            thrown.expect(EXCLUDED.get(fileName));
        }

        try {
            runOneFile(file);
        } catch (OldPowerPointFormatException e) {
            // expected for some files
            if(!OLD_FILES.contains(file.getName())) {
                throw e;
            }
        } catch (EncryptedPowerPointFileException e) {
            // expected for some files
            if(!ENCRYPTED_FILES.contains(file.getName())) {
                throw e;
            }
        }
    }

    abstract void runOneFile(File pFile) throws Exception;
}
