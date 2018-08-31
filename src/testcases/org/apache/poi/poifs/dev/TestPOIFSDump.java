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
package org.apache.poi.poifs.dev;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.filesystem.NotOLE2FileException;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.property.PropertyTable;
import org.apache.poi.util.TempFile;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class TestPOIFSDump {

    private static PrintStream SYSTEM;
    @BeforeClass
    public static void setUp() throws UnsupportedEncodingException {
        SYSTEM = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {

            }
        }, false, "UTF-8"));
    }

    @AfterClass
    public static void resetSystemOut() {
        System.setOut(SYSTEM);
    }

    private static final String TEST_FILE = HSSFTestDataSamples.getSampleFile("46515.xls").getAbsolutePath();
    private static final String INVALID_FILE = HSSFTestDataSamples.getSampleFile("48936-strings.txt").getAbsolutePath();
    private static final String INVALID_XLSX_FILE = HSSFTestDataSamples.getSampleFile("47668.xlsx").getAbsolutePath();

    private static final String[] DUMP_OPTIONS = {
        "-dumprops",
        "-dump-props",
        "-dump-properties",
        "-dumpmini",
        "-dump-mini",
        "-dump-ministream",
        "-dump-mini-stream",
    };

    @After
    public void tearDown() throws IOException {
        // clean up the directory that POIFSDump writes to
        deleteDirectory(new File(new File(TEST_FILE+"_dump").getName()));
    }

    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        cleanDirectory(directory);

        if (!directory.delete()) {
            String message =
                    "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    private static void cleanDirectory(File directory) throws IOException {
        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }

        IOException exception = null;
        for (File file : files) {
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    private static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent){
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                String message =
                        "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }

    @Test
    public void testMain() throws Exception {
        POIFSDump.main(new String[] {
                TEST_FILE
        });

        for(String option : DUMP_OPTIONS) {
            POIFSDump.main(new String[]{
                    option,
                    TEST_FILE
            });
        }
    }
    @Test
    public void testInvalidFile() throws Exception {
        try {
            POIFSDump.main(new String[]{
                    INVALID_FILE
            });
            fail("Should fail with an exception");
        } catch (NotOLE2FileException e) {
            // expected here
        }

        try {
            POIFSDump.main(new String[]{
                    INVALID_XLSX_FILE
            });
            fail("Should fail with an exception");
        } catch (OfficeXmlFileException e) {
            // expected here
        }

        for(String option : DUMP_OPTIONS) {
            try {
                POIFSDump.main(new String[]{
                        option,
                        INVALID_FILE
                });
                fail("Should fail with an exception");
            } catch (NotOLE2FileException e) {
                // expected here
            }

            try {
                POIFSDump.main(new String[]{
                        option,
                        INVALID_XLSX_FILE
                });
                fail("Should fail with an exception");
            } catch (OfficeXmlFileException e) {
                // expected here
            }
        }
    }

    @Ignore("Calls System.exit()")
    @Test
    public void testMainNoArgs() throws Exception {
        POIFSDump.main(new String[] {});
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testFailToWrite() throws IOException {
        File dir = TempFile.createTempFile("TestPOIFSDump", ".tst");
        assertTrue("Had: " + dir, dir.exists());
        assertTrue("Had: " + dir, dir.delete());
        assertTrue("Had: " + dir, dir.mkdirs());

        FileInputStream is = new FileInputStream(TEST_FILE);
        POIFSFileSystem fs = new POIFSFileSystem(is);
        is.close();

        PropertyTable props = fs.getPropertyTable();
        assertNotNull(props);

        // try with an invalid startBlock to trigger an exception
        // to validate that file-handles are closed properly
        POIFSDump.dump(fs, 999999999, "mini-stream", dir);
    }
}