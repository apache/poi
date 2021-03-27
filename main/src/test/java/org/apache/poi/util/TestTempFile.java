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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.poi.poifs.dev.TestPOIFSDump;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestTempFile {
    private String previousTempDir;
    private File tempDir;

    @BeforeEach
    void setUp() throws IOException {
        previousTempDir = System.getProperty(TempFile.JAVA_IO_TMPDIR);
        if(previousTempDir != null) {
            assertTrue(new File(previousTempDir).exists() || new File(previousTempDir).mkdirs(),
                "Failed to create directory " + previousTempDir);
        }

        // use a separate tempdir for the tests to be able to check for leftover files
        tempDir = File.createTempFile("TestTempFile", ".tst");
        assertTrue(tempDir.delete());
        assertTrue(tempDir.mkdirs());
        System.setProperty(TempFile.JAVA_IO_TMPDIR, tempDir.getAbsolutePath());
    }

    @AfterEach
    void tearDown() throws IOException {
        if(tempDir != null) {
            String[] files = tempDir.list();
            assertNotNull(files);
            // can have the "poifiles" subdir
            if (files.length == 1) {
                assertEquals(DefaultTempFileCreationStrategy.POIFILES, files[0], "Had: " + Arrays.toString(files));
                files = new File(tempDir, files[0]).list();
                assertNotNull(files);
                assertEquals(0, files.length, "Had: " + Arrays.toString(files));
            } else {
                assertEquals(0, files.length, "Had: " + Arrays.toString(files));
            }

            // remove the directory after the tests
            TestPOIFSDump.deleteDirectory(tempDir);
        }

        if(previousTempDir == null) {
            System.clearProperty(TempFile.JAVA_IO_TMPDIR);
        } else {
            System.setProperty(TempFile.JAVA_IO_TMPDIR, previousTempDir);
        }

        // reset strategy to re-create the directory
        TempFile.setTempFileCreationStrategy(new DefaultTempFileCreationStrategy());

        // check that we can still create a tempfile
        File testFile = TempFile.createTempFile("test", ".tst");
        assertTrue(testFile.exists());
        assertTrue(testFile.delete());
    }

    @Test
    void testCreateTempFile() throws IOException
    {
        File tempFile = TempFile.createTempFile("test", ".txt");
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(1); //file can be written to
        fos.close();
        assertTrue(tempFile.exists());
        assertTrue(tempFile.isFile());
        assertTrue(tempFile.getName().startsWith("test"));
        assertTrue(tempFile.getName().endsWith(".txt"));
        assertEquals(DefaultTempFileCreationStrategy.POIFILES, tempFile.getParentFile().getName());

        // Can't think of a good way to check whether a file is actually deleted since it would require the VM to stop.
        // Solution: set TempFileCreationStrategy to something that the unit test can trigger a deletion"
        assertTrue(tempFile.delete());
    }

    @Test
    void createTempFileWithDefaultSuffix() throws IOException {
        File tempFile = TempFile.createTempFile("test", null);
        assertTrue(tempFile.getName().endsWith(".tmp"));
    }

    @Test
    void testCreateTempDirectory() throws IOException
    {
        File tempDir = TempFile.createTempDirectory("testDir");
        assertTrue(tempDir.exists());
        assertTrue(tempDir.isDirectory());
        assertTrue(tempDir.getName().startsWith("testDir"));
        assertEquals(DefaultTempFileCreationStrategy.POIFILES, tempDir.getParentFile().getName());

        // Can't think of a good way to check whether a directory is actually deleted since it would require the VM to stop.
        // Solution: set TempFileCreationStrategy to something that the unit test can trigger a deletion"
        assertTrue(tempDir.delete());
    }

    @Test
    void testSetTempFileCreationStrategy() throws IOException {
        TempFile.setTempFileCreationStrategy(new DefaultTempFileCreationStrategy());

        // Should be able to create two tempfiles with same prefix and suffix
        File file1 = TempFile.createTempFile("TestTempFile", ".tst");
        File file2 = TempFile.createTempFile("TestTempFile", ".tst");
        assertNotEquals(file1, file2);
        assertNotNull(file2);
        assertTrue(file2.delete());
        assertNotNull(file1);
        assertTrue(file1.delete());

        //noinspection ConstantConditions
        assertThrows(IllegalArgumentException.class, () -> TempFile.setTempFileCreationStrategy(null));
    }
}
