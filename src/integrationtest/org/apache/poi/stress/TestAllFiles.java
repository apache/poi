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
import java.util.stream.Stream;

import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.tools.ant.DirectoryScanner;
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
 *  processing with it based on it's type.
 *
 *  The test is implemented as a junit {@link ParameterizedTest} test, which leads
 *  to one test-method call for each file (currently around 950 files are handled).
 *
 *  There is a a mapping of extension to implementations of the interface
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
    public static final File ROOT_DIR = new File(System.getProperty("POI.testdata.path", DEFAULT_TEST_DATA_PATH));

    public static final String[] SCAN_EXCLUDES = {
        "**/.svn/**",
        "lost+found",
        "**/.git/**",
    };

    public static Stream<Arguments> allfiles(String testName) throws IOException {
        StressMap sm = new StressMap();
        sm.load(new File(ROOT_DIR, "spreadsheet/stress.xls"));

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(ROOT_DIR);
        scanner.setExcludes(SCAN_EXCLUDES);
        scanner.setIncludes(new String[]{"**/unknown_properties.msg"});

        scanner.scan();

        final List<Arguments> result = new ArrayList<>(100);
        for (String file : scanner.getIncludedFiles()) {
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

    @ParameterizedTest(name = "#{index} {0} {1}")
    @MethodSource("extractFiles")
    void handleExtracting(String file, FileHandlerKnown handler, String password, Class<? extends Throwable> exClass, String exMessage) throws IOException {
        System.out.println("Running extractFiles on "+file);
        FileHandler fileHandler = handler.fileHandler.get();
        assertNotNull(fileHandler, "Did not find a handler for file " + file);
        Executable exec = () -> fileHandler.handleExtracting(new File(ROOT_DIR, file));
        verify(file, exec, exClass, exMessage, password);
    }

    public static Stream<Arguments> handleFiles() throws IOException {
        return allfiles("handle");
    }

    @ParameterizedTest(name = "#{index} {0} {1}")
    @MethodSource("handleFiles")
    void handleFile(String file, FileHandlerKnown handler, String password, Class<? extends Throwable> exClass, String exMessage) throws IOException {
        System.out.println("Running handleFiles on "+file);
        FileHandler fileHandler = handler.fileHandler.get();
        assertNotNull(fileHandler, "Did not find a handler for file " + file);
        try (InputStream stream = new BufferedInputStream(new FileInputStream(new File(ROOT_DIR, file)), 64 * 1024)) {
            Executable exec = () -> fileHandler.handleFile(stream, file);
            verify(file, exec, exClass, exMessage, password);
        }
    }

    public static Stream<Arguments> handleAdditionals() throws IOException {
        return allfiles("additional");
    }

    @ParameterizedTest(name = "#{index} {0} {1}")
    @MethodSource("handleAdditionals")
    void handleAdditional(String file, FileHandlerKnown handler, String password, Class<? extends Throwable> exClass, String exMessage) {
        System.out.println("Running additionals on "+file);
        FileHandler fileHandler = handler.fileHandler.get();
        assertNotNull(fileHandler, "Did not find a handler for file " + file);
        Executable exec = () -> fileHandler.handleAdditional(new File(ROOT_DIR, file));
        verify(file, exec, exClass, exMessage, password);
    }

    @SuppressWarnings("unchecked")
    private static void verify(String file, Executable exec, Class<? extends Throwable> exClass, String exMessage, String password) {
        final String errPrefix = file + " - failed. ";
        // this also removes the password for non encrypted files
        Biff8EncryptionKey.setCurrentUserPassword(password);
        if (exClass != null && AssertionFailedError.class.isAssignableFrom(exClass)) {
            try {
                exec.execute();
                fail(errPrefix + "Expected failed assertion");
            } catch (AssertionFailedError e) {
                String actMsg = pathReplace(e.getMessage());
                assertEquals(exMessage, actMsg, errPrefix);
            } catch (Throwable e) {
                fail(errPrefix + "Unexpected exception", e);
            }
        } else if (exClass != null) {
            Exception e = assertThrows((Class<? extends Exception>)exClass, exec);
            String actMsg = pathReplace(e.getMessage());
            if (NullPointerException.class.isAssignableFrom(exClass)) {
                if (actMsg != null) {
                    assertTrue(actMsg.contains(exMessage), errPrefix + "Message: "+actMsg+" - didn't contain: "+exMessage);
                }
            } else {
                assertNotNull(actMsg, errPrefix);
                assertTrue(actMsg.contains(exMessage), errPrefix + "Message: "+actMsg+" - didn't contain: "+exMessage);
            }
        } else {
            assertDoesNotThrow(exec, errPrefix);
        }
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
}
