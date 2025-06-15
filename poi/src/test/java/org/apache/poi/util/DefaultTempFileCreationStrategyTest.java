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

import static org.apache.poi.util.DefaultTempFileCreationStrategy.DELETE_FILES_ON_EXIT;
import static org.apache.poi.util.DefaultTempFileCreationStrategy.POIFILES;
import static org.apache.poi.util.TempFile.JAVA_IO_TMPDIR;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultTempFileCreationStrategyTest {

    private String propBefore;
    private String tmpBefore;

    @BeforeEach
    void before() {
        propBefore = System.getProperty(DELETE_FILES_ON_EXIT);
        tmpBefore = System.getProperty(JAVA_IO_TMPDIR);
    }

    @AfterEach
    void after() {
        if (propBefore == null) {
            System.clearProperty(DELETE_FILES_ON_EXIT);
        } else {
            System.setProperty(DELETE_FILES_ON_EXIT, propBefore);
        }

        System.setProperty(JAVA_IO_TMPDIR, tmpBefore);
    }

    @Test
    void testDefaultFile() throws IOException {
        DefaultTempFileCreationStrategy strategy = new DefaultTempFileCreationStrategy();
        checkGetFile(strategy);
    }

    @Test
    void testProvidedDir() throws IOException {
        DefaultTempFileCreationStrategy parentStrategy = new DefaultTempFileCreationStrategy();
        File dir = parentStrategy.createTempDirectory("testProvidedDir");
        assertNotNull(dir, "Failed to create temp directory");
        try {
            assertTrue(Files.isDirectory(dir.toPath()), "File is not a directory: " + dir);
            DefaultTempFileCreationStrategy testStrategy = new DefaultTempFileCreationStrategy(dir);
            checkGetFileAndPath(testStrategy, dir.toPath());
        } finally {
            // Clean up the directory after the test
            FileUtils.deleteDirectory(dir);
        }
    }

    @Test
    void testProvidedDirNotExists() throws IOException {
        DefaultTempFileCreationStrategy parentStrategy = new DefaultTempFileCreationStrategy();
        File dir = parentStrategy.createTempDirectory("testProvidedDir");
        assertNotNull(dir, "Failed to create temp directory");
        assertTrue(dir.delete(), "directory not deleted: " + dir);
        try {
            DefaultTempFileCreationStrategy testStrategy = new DefaultTempFileCreationStrategy(dir);
            checkGetFileAndPath(testStrategy, dir.toPath());
        } finally {
            // Clean up the directory after the test
            FileUtils.deleteDirectory(dir);
        }
    }

    @Test
    void testProvidedDirIsActuallyAPlainFile() throws IOException {
        DefaultTempFileCreationStrategy parentStrategy = new DefaultTempFileCreationStrategy();
        File dir = parentStrategy.createTempFile("test123", ".tmp");
        assertNotNull(dir, "Failed to create temp file");
        try {
            assertThrows(IllegalArgumentException.class, () -> new DefaultTempFileCreationStrategy(dir));
        } finally {
            dir.delete();
        }
    }

    private static void checkGetFile(DefaultTempFileCreationStrategy strategy) throws IOException {
        checkGetFileAndPath(strategy, null);
    }

    private static void checkGetFileAndPath(DefaultTempFileCreationStrategy strategy,
                                           Path path) throws IOException {
        File file = strategy.createTempFile("POITest", ".tmp");
        try {
            if (path != null) {
                assertTrue(file.toPath().startsWith(path),
                        "File path does not start with expected path: " + path);
            }

            assertTrue(file.getParentFile().exists(),
                    "Failed for " + file.getParentFile());

            assertTrue(file.exists(),
                    "Failed for " + file);
        } finally {
            assertTrue(file.delete());
        }
    }

    @Test
    void testDefaultDir() throws IOException {
        DefaultTempFileCreationStrategy strategy = new DefaultTempFileCreationStrategy();
        File dir = strategy.createTempDirectory("POITest");
        try {
            assertTrue(dir.getParentFile().exists(),
                    "Failed for " + dir.getParentFile());

            assertTrue(dir.exists(),
                    "Failed for " + dir);
        } finally {
            assertTrue(dir.delete());
        }
    }

    @Test
    void testWithProperty() throws IOException {
        System.setProperty(DELETE_FILES_ON_EXIT, "true");

        // we can set the property, but not easily check if it works
        // so let's just call the main method
        testDefaultFile();
    }

    @Test
    void testEmptyTempDir() {
        System.clearProperty(JAVA_IO_TMPDIR);

        DefaultTempFileCreationStrategy strategy = new DefaultTempFileCreationStrategy();
        assertThrows(IOException.class,
                () -> strategy.createTempDirectory("POITest"));
    }

    @Test
    void testCustomDirExists() throws IOException {
        File dirTest = File.createTempFile("POITest", ".dir");
        try {
            assertTrue(dirTest.delete());
            assertTrue(dirTest.mkdir());

            DefaultTempFileCreationStrategy strategy = new DefaultTempFileCreationStrategy(dirTest);
            checkGetFile(strategy);
        } finally {
            FileUtils.deleteDirectory(dirTest);
        }
    }

    @Test
    void testCustomDirAndPoiFilesExists() throws IOException {
        File dirTest = File.createTempFile("POITest", ".dir");
        try {
            assertTrue(dirTest.delete());
            assertTrue(dirTest.mkdir());
            assertTrue(new File(dirTest, POIFILES).mkdir());

            DefaultTempFileCreationStrategy strategy = new DefaultTempFileCreationStrategy(dirTest);
            checkGetFile(strategy);
        } finally {
            FileUtils.deleteDirectory(dirTest);
        }
    }
}
