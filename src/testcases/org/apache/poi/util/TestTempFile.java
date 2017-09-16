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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.poi.poifs.dev.TestPOIFSDump;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestTempFile {
    private String previousTempDir;
    private File tempDir;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws IOException {
        previousTempDir = System.getProperty(TempFile.JAVA_IO_TMPDIR);

        // use a separate tempdir for the tests to be able to check for leftover files
        tempDir = File.createTempFile("TestTempFile", ".tst");
        assertTrue(tempDir.delete());
        assertTrue(tempDir.mkdirs());
        System.setProperty(TempFile.JAVA_IO_TMPDIR, tempDir.getAbsolutePath());
    }

    @After
    public void tearDown() throws IOException {
        String[] files = tempDir.list();
        assertNotNull(files);
        // can have the "poifiles" subdir
        if(files.length == 1) {
            assertEquals("Had: " + Arrays.toString(files), DefaultTempFileCreationStrategy.POIFILES, files[0]);
            files = new File(tempDir, files[0]).list();
            assertNotNull(files);
            assertEquals("Had: " + Arrays.toString(files), 0, files.length);
        } else {
            assertEquals("Had: " + Arrays.toString(files), 0, files.length);
        }

        // remove the directory after the tests
        TestPOIFSDump.deleteDirectory(tempDir);

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
    public void testCreateTempFile() throws IOException
    {
        File tempFile = TempFile.createTempFile("test", ".txt");
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(1); //file can be written to
        fos.close();
        assertTrue("temp file exists", tempFile.exists());
        assertTrue("temp file is a file", tempFile.isFile());
        assertTrue("temp file's name should start with test",
                tempFile.getName().startsWith("test"));
        assertTrue("temp file's name should end with .txt",
                tempFile.getName().endsWith(".txt"));
        assertEquals("temp file is saved in poifiles directory",
                DefaultTempFileCreationStrategy.POIFILES, tempFile.getParentFile().getName());

        // Can't think of a good way to check whether a file is actually deleted since it would require the VM to stop.
        // Solution: set TempFileCreationStrategy to something that the unit test can trigger a deletion"
        assertTrue("Unable to delete temp file", tempFile.delete());
    }
    
    @Test
    public void createTempFileWithDefaultSuffix() throws IOException {
        File tempFile = TempFile.createTempFile("test", null);
        assertTrue("temp file's name should end with .tmp",
                tempFile.getName().endsWith(".tmp"));
    }
    
    @Test
    public void testCreateTempDirectory() throws IOException
    {
        File tempDir = TempFile.createTempDirectory("testDir");
        assertTrue("testDir exists", tempDir.exists());
        assertTrue("testDir is a directory", tempDir.isDirectory());
        assertTrue("testDir's name starts with testDir",
                tempDir.getName().startsWith("testDir"));
        assertEquals("tempDir is saved in poifiles directory",
                DefaultTempFileCreationStrategy.POIFILES, tempDir.getParentFile().getName());

        // Can't think of a good way to check whether a directory is actually deleted since it would require the VM to stop.
        // Solution: set TempFileCreationStrategy to something that the unit test can trigger a deletion"
        assertTrue("Unable to delete tempDir", tempDir.delete());
    }
    
    @Test
    public void testSetTempFileCreationStrategy() throws IOException {
        TempFile.setTempFileCreationStrategy(new DefaultTempFileCreationStrategy());
        
        // Should be able to create two tempfiles with same prefix and suffix
        File file1 = TempFile.createTempFile("TestTempFile", ".tst");
        File file2 = TempFile.createTempFile("TestTempFile", ".tst");
        assertFalse(file1.equals(file2));
        assertNotNull(file2);
        assertTrue(file2.delete());
        assertNotNull(file1);
        assertTrue(file1.delete());
        
        thrown.expect(IllegalArgumentException.class);
        TempFile.setTempFileCreationStrategy(null);
    }
}
