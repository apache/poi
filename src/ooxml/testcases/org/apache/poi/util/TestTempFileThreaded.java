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
package org.apache.poi.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.apache.poi.util.DefaultTempFileCreationStrategy.POIFILES;
import static org.apache.poi.util.TempFile.JAVA_IO_TMPDIR;
import static org.junit.Assert.assertEquals;

public class TestTempFileThreaded {
    private static final int NUMBER_OF_THREADS = 10;
    private static final int NUMBER_OF_TESTS = 200;

    private volatile Throwable exception;
    private int executions[];

    // the actual thread-safe temp-file strategy
    private static TempFileCreationStrategy createTempFileCreationStrategy(File poiTempFileDirectory) {
        return new TempFileCreationStrategy() {
            @Override
            public File createTempFile(String prefix, String suffix) throws IOException {
                long threadId = Thread.currentThread().getId();
                File threadDir = new File(poiTempFileDirectory, Long.toString(threadId));
                if (!threadDir.exists()) {
                    if (!threadDir.mkdirs()) {
                        throw new IOException("mkdir of " + threadDir + " failed");
                    }
                }

                File file = File.createTempFile(prefix, suffix, threadDir);
                file.deleteOnExit();
                return file;
            }

            @Override
            public File createTempDirectory(String prefix) {
                throw new UnsupportedOperationException("createTempDirectory");
            }
        };
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
        if (tmpDir == null) {
            throw new IOException("Systems temporary directory not defined - set the -D" + JAVA_IO_TMPDIR + " jvm property!");
        }

        TempFile.setTempFileCreationStrategy(createTempFileCreationStrategy(new File(new File(tmpDir, POIFILES), "TestTempFileThreaded")));
    }

    @Before
    public void setUp() {
        // Initialize array to allow to summarize afterwards
        executions = new int[NUMBER_OF_THREADS];
    }

    @Test
    public void runTest() throws Throwable {
        List<Thread> threads = new LinkedList<>();

        // start all threads
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            Thread t = startThread(i, new TestRunnable());
            threads.add(t);
        }

        // wait for all threads
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            threads.get(i).join();
        }

        // report exceptions if there were any
        if (exception != null) {
            throw exception;
        }

        // make sure the resulting number of executions is correct
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            // check if enough items were performed
            assertEquals("Thread " + i
                            + " did not execute all iterations", NUMBER_OF_TESTS,
                    executions[i]);
        }
    }

    private static class TestRunnable {
        Map<Integer, List<File>> files = new HashMap<>();

        public TestRunnable() {
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                files.put(i, new ArrayList<>());
            }
        }

        public void doEnd(int threadNum) {
            for (File file : files.get(threadNum)) {
                if (!file.exists()) {
                    throw new IllegalStateException("File " + file + " does not exist");
                }
                if (!file.delete()) {
                    throw new IllegalStateException("Deletion of " + file + " failed");
                }
            }
        }

        public void run(int threadNum, int iter) throws Exception {
            try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
                SXSSFSheet sheet = wb.createSheet("test");

                for (int i = 0; i < 100; i++) {
                    Row row = sheet.createRow(i);
                    for (int j = 0; j < 10; j++) {
                        Cell cell = row.createCell(j);
                        cell.setCellValue("123");
                    }
                }

                File file = TempFile.createTempFile("TestTempFile-" + threadNum + "-" + iter + "-", ".xlsx");
                try (OutputStream outputStream = new FileOutputStream(file)) {
                    wb.write(outputStream);
                }

                files.get(threadNum).add(file);

                if (iter % 30 == 0) {
                    System.out.println("thread: " + threadNum + ", iter: " + iter + ": " + file);
                }
            }
        }
    }

    private Thread startThread(final int threadNum, final TestRunnable run) {
        Thread t1 = new Thread(() -> {
            try {
                for (int iter = 0; iter < NUMBER_OF_TESTS && exception == null; iter++) {
                    // call the actual test-code
                    run.run(threadNum, iter);

                    executions[threadNum]++;
                }

                // do end-work here, we don't do this in a finally as we log
                // Exception
                // then anyway
                run.doEnd(threadNum);
            } catch (Throwable e) {
                exception = e;
            }

        }, "ThreadTestHelper-Thread " + threadNum + ": " + run.getClass().getName());

        t1.start();

        return t1;
    }
}
