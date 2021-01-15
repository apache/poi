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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
    private static final File ROOT_DIR = new File("test-data");


    public static final String[] SCAN_EXCLUDES = {
        "**/.svn/**",
        "lost+found",
        "**/.git/**",
    };

    public static Stream<Arguments> allfiles(String testName) throws IOException {
        MultiValuedMap<String, ExcInfo> exMap;
        Map<String,String> handlerMap;
        try (Workbook wb = WorkbookFactory.create(new File(ROOT_DIR, "spreadsheet/stress.xls"))) {
            exMap = readExMap(wb.getSheet("Exceptions"));
            handlerMap = readHandlerMap(wb.getSheet("Handlers"));
        }

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(ROOT_DIR);
        scanner.setExcludes(SCAN_EXCLUDES);

        scanner.scan();

        final List<Arguments> result = new ArrayList<>(100);
        for (String file : scanner.getIncludedFiles()) {
            // ... failures/handlers lookup doesn't work on windows otherwise
            final String uniFile = file.replace('\\', '/');

            String firstHandler = handlerMap.entrySet().stream()
                .filter(me -> uniFile.endsWith(me.getKey()))
                .map(Map.Entry::getValue).findFirst().orElse("NULL");

            final String[] handlerStr = { firstHandler, secondHandler(firstHandler) };
            for (String hs : handlerStr) {
                if ("NULL".equals(hs)) continue;
                ExcInfo info1 = exMap.get(file).stream()
                    .filter(e ->
                        (e.tests == null || e.tests.contains(testName) || "IGNORE".equals(e.tests)) &&
                        (e.handler == null || e.handler.contains(hs))
                    ).findFirst().orElse(null);

                if (info1 == null || !"IGNORE".equals(info1.tests)) {
                    result.add(Arguments.of(
                        file,
                        hs,
                        (info1 != null) ? info1.password : null,
                        (info1 != null) ? info1.exClazz : null,
                        (info1 != null) ? info1.exMessage : null
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
    void handleExtracting(String file, String handler, String password, Class<? extends Throwable> exClass, String exMessage) throws IOException {
        System.out.println("Running extractFiles on "+file);
        FileHandler fileHandler = Handler.valueOf(handler).fileHandler.get();
        assertNotNull(fileHandler, "Did not find a handler for file " + file);
        Executable exec = () -> fileHandler.handleExtracting(new File(ROOT_DIR, file));
        verify(exec, exClass, exMessage, password);
    }


    public static Stream<Arguments> handleFiles() throws IOException {
        return allfiles("handle");
    }

    @ParameterizedTest(name = "#{index} {0} {1}")
    @MethodSource("handleFiles")
    void handleFile(String file, String handler, String password, Class<? extends Throwable> exClass, String exMessage) throws IOException {
        System.out.println("Running handleFiles on "+file);
        FileHandler fileHandler = Handler.valueOf(handler).fileHandler.get();
        assertNotNull(fileHandler, "Did not find a handler for file " + file);
        try (InputStream stream = new BufferedInputStream(new FileInputStream(new File(ROOT_DIR, file)), 64 * 1024)) {
            Executable exec = () -> fileHandler.handleFile(stream, file);
            verify(exec, exClass, exMessage, password);
        }
    }

    public static Stream<Arguments> handleAdditionals() throws IOException {
        return allfiles("additional");
    }

    @ParameterizedTest(name = "#{index} {0} {1}")
    @MethodSource("handleAdditionals")
    void handleAdditional(String file, String handler, String password, Class<? extends Throwable> exClass, String exMessage) {
        System.out.println("Running additionals on "+file);
        FileHandler fileHandler = Handler.valueOf(handler).fileHandler.get();
        assertNotNull(fileHandler, "Did not find a handler for file " + file);
        Executable exec = () -> fileHandler.handleAdditional(new File(ROOT_DIR, file));
        verify(exec, exClass, exMessage, password);
    }

    @SuppressWarnings("unchecked")
    private static void verify(Executable exec, Class<? extends Throwable> exClass, String exMessage, String password) {
        // this also removes the password for non encrypted files
        Biff8EncryptionKey.setCurrentUserPassword(password);
        if (exClass != null && AssertionFailedError.class.isAssignableFrom(exClass)) {
            try {
                exec.execute();
                fail("expected failed assertion");
            } catch (AssertionFailedError e) {
                assertEquals(exMessage, e.getMessage());
            } catch (Throwable e) {
                fail("unexpected exception", e);
            }
        } else if (exClass != null) {
            Exception e = assertThrows((Class<? extends Exception>)exClass, exec);
            String actMsg = e.getMessage();
            if (exMessage == null) {
                assertNull(actMsg);
            } else {
                assertNotNull(actMsg);
                assertTrue(actMsg.startsWith(exMessage), "Message: "+actMsg+" - didn't start with "+exMessage);
            }
        } else {
            assertDoesNotThrow(exec);
        }
    }


    private static String secondHandler(String handlerStr) {
        switch (handlerStr) {
            case "XSSF":
            case "XWPF":
            case "XSLF":
            case "XDGF":
                return "OPC";
            case "HSSF":
            case "HWPF":
            case "HSLF":
            case "HDGF":
            case "HSMF":
            case "HBPF":
                return "HPSF";
            default:
                return "NULL";
        }
    }

    private static Map<String,String> readHandlerMap(Sheet sh) {
        Map<String,String> handlerMap = new LinkedHashMap<>();
        boolean IGNORE_SCRATCHPAD = Boolean.getBoolean("scratchpad.ignore");
        boolean isFirst = true;
        for (Row row : sh) {
            if (isFirst) {
                isFirst = false;
                continue;
            }
            Cell cell = row.getCell(2);
            if (IGNORE_SCRATCHPAD || cell == null || cell.getCellType() != CellType.STRING) {
                cell = row.getCell(1);
            }
            handlerMap.put(row.getCell(0).getStringCellValue(), cell.getStringCellValue());
        }
        return handlerMap;
    }


    private static MultiValuedMap<String, ExcInfo> readExMap(Sheet sh) {
        MultiValuedMap<String, ExcInfo> exMap = new ArrayListValuedHashMap<>();

        Iterator<Row> iter = sh.iterator();
        List<BiConsumer<ExcInfo,String>> cols = initCols(iter.next());

        while (iter.hasNext()) {
            ExcInfo info = new ExcInfo();
            for (Cell cell : iter.next()) {
                if (cell.getCellType() == CellType.STRING) {
                    cols.get(cell.getColumnIndex()).accept(info, cell.getStringCellValue());
                }
            }
            exMap.put(info.file, info);
        }
        return exMap;
    }


    private static List<BiConsumer<ExcInfo,String>> initCols(Row row) {
        Map<String,BiConsumer<ExcInfo,String>> m = new HashMap<>();
        m.put("File", (e,s) -> e.file = s);
        m.put("Tests", (e,s) -> e.tests = s);
        m.put("Handler", (e,s) -> e.handler = s);
        m.put("Password", (e,s) -> e.password = s);
        m.put("Exception Class", (e,s) -> {
            try {
                e.exClazz = (Class<? extends Exception>) Class.forName(s);
            } catch (ClassNotFoundException ex) {
                fail(ex);
            }
        });
        m.put("Exception Message", (e,s) -> e.exMessage = s);

        return StreamSupport
            .stream(row.spliterator(), false)
            .map(Cell::getStringCellValue)
            .map(v -> m.getOrDefault(v, (e,s) -> {}))
            .collect(Collectors.toList());
    }

    private static class ExcInfo {
        String file;
        String tests;
        String handler;
        String password;
        Class<? extends Throwable> exClazz;
        String exMessage;


    }

    @SuppressWarnings("unused")
    private enum Handler {
        HDGF(HDGFFileHandler::new),
        HMEF(HMEFFileHandler::new),
        HPBF(HPBFFileHandler::new),
        HPSF(HPSFFileHandler::new),
        HSLF(HSLFFileHandler::new),
        HSMF(HSMFFileHandler::new),
        HSSF(HSSFFileHandler::new),
        HWPF(HWPFFileHandler::new),
        OPC(OPCFileHandler::new),
        POIFS(POIFSFileHandler::new),
        XDGF(XDGFFileHandler::new),
        XSLF(XSLFFileHandler::new),
        XSSFB(XSSFBFileHandler::new),
        XSSF(XSSFFileHandler::new),
        XWPF(XWPFFileHandler::new),
        OWPF(OWPFFileHandler::new),
        NULL(NullFileHandler::new)
        ;

        final Supplier<FileHandler> fileHandler;
        Handler(Supplier<FileHandler> fileHandler) {
            this.fileHandler = fileHandler;
        }
    }

    public static class NullFileHandler implements FileHandler {
        @Override
        public void handleFile(InputStream stream, String path) {
        }

        @Override
        public void handleExtracting(File file) {
        }

        @Override
        public void handleAdditional(File file) {
        }
    }
}
