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
package org.apache.poi.stress;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.tools.ant.DirectoryScanner;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

/**
 *  This is an integration test which performs various actions on all stored test-files and tries
 *  to reveal problems which are introduced, but not covered (yet) by unit tests.
 *
 *  This test looks for any file under the test-data directory and tries to do some useful
 *  processing with it based on its type.
 *
 *  The test is implemented as a junit {@link ParameterizedTest} test, which leads
 *  to one test-method call for each file (currently around 950 files are handled).
 *
 *  There is a mapping of extension to implementations of the interface
 *  {@link FileHandler} which defines how the file is loaded and which actions are
 *  tried with the file.
 *
 *  The test can be expanded by adding more actions to the FileHandlers, this automatically
 *  applies the action to any such file in our test-data repository.
 *
 *  There is also a list of files that should actually fail.
 *
 *  Note: It is also a test-failure if a file that is expected to fail now actually works,
 *  i.e. if a bug was fixed in POI itself, the file should be removed from the expected-failures
 *  here as well! This is to ensure that files that should not work really do not work, e.g.
 *  that we do not remove expected sanity checks.
 */
// also need to set JVM parameter: -Djunit.jupiter.execution.parallel.enabled=true
@Execution(ExecutionMode.CONCURRENT)
public class TestAllFiles {
    private static final String DEFAULT_TEST_DATA_PATH = "test-data";
    public static final File ROOT_DIR;
    static {
		File dir = new File(System.getProperty(POIDataSamples.TEST_PROPERTY, DEFAULT_TEST_DATA_PATH));
		if (!dir.exists()) {
			dir = new File(System.getProperty(POIDataSamples.TEST_PROPERTY, "../" + DEFAULT_TEST_DATA_PATH));
		}

		ROOT_DIR = dir;
	}

    public static final String[] SCAN_EXCLUDES = {
        "**/.svn/**",
        "lost+found",
        "**/.git/**",
        //the DocType (DTD) declaration causes this to fail
        "**/ExternalEntityInText.docx",

        // exclude files failing on windows nodes, because of limited JCE policies
        "document/bug53475-password-is-pass.docx",
        "poifs/60320-protected.xlsx",
        "poifs/protected_sha512.xlsx",
        "poifs/60320-protected.xlsx",
        "poifs/protected_sha512.xlsx",
    };

    // cheap workaround of skipping the few problematic files
    public static final String[] SCAN_EXCLUDES_NOSCRATCHPAD = {
        "**/.svn/**",
        "lost+found",
        "**/.git/**",
        "**/ExternalEntityInText.docx", //the DocType (DTD) declaration causes this to fail
        "**/right-to-left.xlsx", //the threaded comments in this file cause XSSF clone to fail
        "document/word2.doc",
        "document/cpansearch.perl.org_src_tobyink_acme-rundoc-0.001_word-lib_hello_world.docm",
        "document/Fuzzed.doc",
        "hpsf/Test0313rur.adm",
        "spreadsheet/43493.xls",
        "spreadsheet/44958.xls",
        "spreadsheet/44958_1.xls",
        "spreadsheet/46904.xls",
        "spreadsheet/51832.xls",
        "spreadsheet/60284.xls",
        "spreadsheet/testArraysAndTables.xls",
        "spreadsheet/testEXCEL_3.xls",
        "spreadsheet/testEXCEL_4.xls",
        "poifs/unknown_properties.msg",
        "publisher/clusterfuzz-testcase-minimized-POIHPBFFuzzer-4701121678278656.pub",
        "hsmf/clusterfuzz-testcase-minimized-POIHSMFFuzzer-4848576776503296.msg",
        "hsmf/clusterfuzz-testcase-minimized-POIHSMFFuzzer-5336473854148608.msg",
        "slideshow/clusterfuzz-testcase-minimized-POIHSLFFuzzer-6416153805979648.ppt",
        "slideshow/clusterfuzz-testcase-minimized-POIHSLFFuzzer-6710128412590080.ppt",
        "publisher/clusterfuzz-testcase-minimized-POIHPBFFuzzer-4701121678278656.pub",
        "spreadsheet/clusterfuzz-testcase-minimized-POIHSSFFuzzer-5285517825277952.xls",
        "spreadsheet/clusterfuzz-testcase-minimized-POIHSSFFuzzer-6322470200934400.xls",
        "document/clusterfuzz-testcase-minimized-POIHWPFFuzzer-5418937293340672.doc",
        "document/clusterfuzz-testcase-minimized-POIHWPFFuzzer-5440721166139392.doc",
        "diagram/clusterfuzz-testcase-minimized-POIHDGFFuzzer-5947849161179136.vsd",
        "spreadsheet/clusterfuzz-testcase-minimized-POIHSSFFuzzer-5436547081830400.xls",
        "spreadsheet/clusterfuzz-testcase-minimized-POIHSSFFuzzer-4819588401201152.xls",
        "diagram/clusterfuzz-testcase-minimized-POIVisioFuzzer-4537225637134336.vsd",

        // exclude files failing on windows nodes, because of limited JCE policies
        "document/bug53475-password-is-pass.docx",
        "poifs/60320-protected.xlsx",
        "poifs/protected_sha512.xlsx",
        "poifs/60320-protected.xlsx",
        "poifs/protected_sha512.xlsx",
    };

    private static final Set<String> EXPECTED_FAILURES = StressTestUtils.unmodifiableHashSet(
            "document/truncated62886.docx"
    );

    public static Stream<Arguments> allfiles(String testName) throws IOException {
        StressMap sm = new StressMap();
        sm.load(new File(ROOT_DIR, "spreadsheet/stress.xls"));

        boolean noScratch = Boolean.getBoolean("scratchpad.ignore");

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(ROOT_DIR);
        scanner.setExcludes(noScratch ? SCAN_EXCLUDES_NOSCRATCHPAD : SCAN_EXCLUDES);
        scanner.scan();

        final List<Arguments> result = new ArrayList<>(100);
        for (String file : scanner.getIncludedFiles()) {
            // avoid running on files leftover from previous failed runs
            // or being created by tests run in parallel
            if(file.endsWith("-saved.xls") || file.endsWith("TestHPSFWritingFunctionality.doc")) {
                continue;
            }

            for (FileHandlerKnown handler : sm.getHandler(file)) {
                ExcInfo info1 = sm.getExcInfo(file, testName, handler);
                if (info1 == null || info1.isValid(testName, handler.name())) {
                    result.add(Arguments.of(
                        file,
                        handler,
                        (info1 != null) ? info1.getPassword() : null,
                        (info1 != null) ? info1.getExClazz() : null,
                        (info1 != null) ? info1.getExMessage() : null
                    ));
                }
            }
        }

        return result.stream();
    }

    public static Stream<Arguments> extractFiles() throws IOException {
        return allfiles("extract");
    }

    @ParameterizedTest(name = "Extracting - #{index} {0} {1}")
    @MethodSource("extractFiles")
    void handleExtracting(String file, FileHandlerKnown handler, String password, Class<? extends Throwable> exClass, String exMessage) throws IOException {
        String threadName = Thread.currentThread().getName();
        try {
            Thread.currentThread().setName("Extracting - " + file + " - " + handler);
            if (StressTestUtils.excludeFile(file, EXPECTED_FAILURES))
                return;

            System.out.println("Running extractFiles on " + file);
            FileHandler fileHandler = handler.getHandler();
            assertNotNull(fileHandler, "Did not find a handler for file " + file);
            Executable exec = () -> fileHandler.handleExtracting(new File(ROOT_DIR, file));
            verify(file, exec, exClass, exMessage, password, fileHandler);
        } finally {
            Thread.currentThread().setName(threadName);
        }
    }

    public static Stream<Arguments> handleFiles() throws IOException {
        return allfiles("handle");
    }

    @ParameterizedTest(name = "#{index} {0} {1}")
    @MethodSource("handleFiles")
    void handleFile(String file, FileHandlerKnown handler, String password, Class<? extends Throwable> exClass, String exMessage) throws IOException {
        String threadName = Thread.currentThread().getName();
        try {
            Thread.currentThread().setName("Handle - " + file + " - " + handler);

            // Some of the tests hang in JDK 8 due to Graphics-Rendering issues in JDK itself,
            // therefore we do not run some tests here
            Assumptions.assumeFalse(isJava8() && (
                    file.endsWith("23884_defense_FINAL_OOimport_edit.ppt")
            ), "Some files hang in JDK graphics rendering on Java 8 due to a JDK bug");

            System.out.println("Running handleFiles on "+file);
            FileHandler fileHandler = handler.getHandler();
            assertNotNull(fileHandler, "Did not find a handler for file " + file);
            try (InputStream stream = new BufferedInputStream(new FileInputStream(new File(ROOT_DIR, file)), 64 * 1024)) {
                Executable exec = () -> fileHandler.handleFile(stream, file);
                verify(file, exec, exClass, exMessage, password, fileHandler);
            }
        } finally {
            Thread.currentThread().setName(threadName);
        }
    }

    public static Stream<Arguments> handleAdditionals() throws IOException {
        return allfiles("additional");
    }

    @ParameterizedTest(name = "Additional - #{index} {0} {1}")
    @MethodSource("handleAdditionals")
    void handleAdditional(String file, FileHandlerKnown handler, String password, Class<? extends Throwable> exClass, String exMessage) {
        String threadName = Thread.currentThread().getName();
        try {
            Thread.currentThread().setName("Additional - " + file + " - " + handler);
            System.out.println("Running additionals on "+file);
            FileHandler fileHandler = handler.getHandler();
            assertNotNull(fileHandler, "Did not find a handler for file " + file);
            Executable exec = () -> fileHandler.handleAdditional(new File(ROOT_DIR, file));
            verify(file, exec, exClass, exMessage, password, fileHandler);
        } finally {
            Thread.currentThread().setName(threadName);
        }
    }

    @SuppressWarnings("unchecked")
    private static void verify(String file, Executable exec, Class<? extends Throwable> exClass, String exMessage, String password,
            FileHandler fileHandler) {
        final String errPrefix = file.replace("\\", "/") + " - failed for handler " + fileHandler.getClass().getSimpleName() + ": ";
        // this also removes the password for non encrypted files
        Biff8EncryptionKey.setCurrentUserPassword(password);
        if (exClass != null && AssertionFailedError.class.isAssignableFrom(exClass)) {
            try {
                exec.execute();
                fail(errPrefix + "Expected failed assertion " + exClass + " and message " + exMessage);
            } catch (AssertionFailedError e) {
                String actMsg = pathReplace(e.getMessage());
                assertEquals(exMessage, actMsg, errPrefix);
            } catch (Throwable e) {
                fail(errPrefix + "Unexpected exception, expected " + exClass + " and message " + exMessage, e);
            }
        } else if (exClass != null) {
            Exception e = assertThrows((Class<? extends Exception>)exClass, exec, errPrefix + " expected " + exClass);
            String actMsg = pathReplace(e.getMessage());

            // perform special handling of NullPointerException as
            // JDK started to add more information in some newer JDK, so
            // it sometimes has a message and sometimes not!
            if (NullPointerException.class.isAssignableFrom(exClass)) {
                if (actMsg != null) {
                    assertTrue(actMsg.contains(exMessage), errPrefix + "Message: " + actMsg + " - didn't contain: " + exMessage);
                }
            } else {
                // verify that message is either null for both or set for both
                assertTrue(actMsg != null || isBlank(exMessage),
                        errPrefix + " for " + exClass + " expected message '" + exMessage + "' but had '" + actMsg + "': " + e);

                if (actMsg != null &&
                        // sometimes ArrayIndexOutOfBoundsException has null-message?!?
                        // so skip the check for this type of exception if expected message is null
                        (exMessage != null || !ArrayIndexOutOfBoundsException.class.isAssignableFrom(exClass))) {
                    assertNotNull(exMessage,
                            errPrefix + "Expected message was null, but actMsg wasn't: Message: " + actMsg + ": " + e);
                    assertTrue(actMsg.contains(exMessage),
                            errPrefix + "Message: " + actMsg + " - didn't contain: " + exMessage);
                }
            }
        } else {
            assertDoesNotThrow(exec, errPrefix);
        }
    }

    private static boolean isBlank(final String str) {
        if (str != null) {
            final int strLen = str.length();
            for (int i = 0; i < strLen; i++) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String pathReplace(String msg) {
        if (msg == null) return null;

        // Windows path replacement
        msg = msg.replace('\\', '/');

        // Adjust file paths to remove unwanted file path info.
        int filePathIndex = msg.indexOf(ROOT_DIR.toString());
        if (filePathIndex > 0) {
            int testDataDirectoryIndex = msg.indexOf(DEFAULT_TEST_DATA_PATH);
            msg = msg.substring(0, filePathIndex) + msg.substring(testDataDirectoryIndex);
        }

        return msg;
    }

    private static boolean isJava8() {
        return System.getProperty("java.version").startsWith("1.8");
    }
}
