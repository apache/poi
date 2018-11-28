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

package org.apache.poi.sl.usermodel;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public abstract class BaseTestSlideShowFactory {
    private static final POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    @SuppressWarnings("resource")
    protected static void testFactoryFromFile(String file) throws Exception {
        SlideShow<?,?> ss;
        // from file
        ss = SlideShowFactory.create(fromFile(file));
        assertNotNull(ss);
        assertCloseDoesNotModifyFile(file, ss);
    }

    @SuppressWarnings("resource")
    protected static void testFactoryFromStream(String file) throws Exception {
        SlideShow<?,?> ss;
        // from stream
        ss = SlideShowFactory.create(fromStream(file));
        assertNotNull(ss);
        assertCloseDoesNotModifyFile(file, ss);
    }

    @SuppressWarnings("resource")
    protected static void testFactoryFromNative(String file) throws Exception {
        SlideShow<?,?> ss;
        // from NPOIFS
        if (file.endsWith(".ppt")) {
            POIFSFileSystem npoifs = new POIFSFileSystem(fromFile(file));
            ss = SlideShowFactory.create(npoifs);
            assertNotNull(ss);
            npoifs.close();
            assertCloseDoesNotModifyFile(file, ss);
        }
        // from OPCPackage
        else if (file.endsWith(".pptx")) {
            // not implemented
            throw new UnsupportedOperationException("Test not implemented");
        }
        else {
            fail("Unexpected file extension: " + file);
        }
    }

    @SuppressWarnings("resource")
    protected static void testFactoryFromProtectedFile(String protectedFile, String password) throws Exception {
        // from protected file
        SlideShow<?,?> ss = SlideShowFactory.create(fromFile(protectedFile), password);
        assertNotNull(ss);
        assertCloseDoesNotModifyFile(protectedFile, ss);
    }

    @SuppressWarnings("resource")
    protected static void testFactoryFromProtectedStream(String protectedFile, String password) throws Exception {
        // from protected stream
        SlideShow<?,?> ss = SlideShowFactory.create(fromStream(protectedFile), password);
        assertNotNull(ss);
        assertCloseDoesNotModifyFile(protectedFile, ss);
    }

    @SuppressWarnings("resource")
    protected static void testFactoryFromProtectedNative(String protectedFile, String password) throws Exception {
        SlideShow<?,?> ss;
        // Encryption layer is a BIFF8 binary format that can be read by NPOIFSFileSystem,
        // used for both HSLF and XSLF

        // from protected NPOIFS
        if (protectedFile.endsWith(".ppt") || protectedFile.endsWith(".pptx")) {
            POIFSFileSystem npoifs = new POIFSFileSystem(fromFile(protectedFile));
            ss = SlideShowFactory.create(npoifs, password);
            assertNotNull(ss);
            npoifs.close();
            assertCloseDoesNotModifyFile(protectedFile, ss);
        }
        else {
            fail("Unrecognized file extension: " + protectedFile);
        }
    }

    @SuppressWarnings("SameParameterValue")
    protected static void testFactory(String file, String protectedFile, String password)
    throws Exception {
        testFactoryFromFile(file);
        testFactoryFromStream(file);
        testFactoryFromNative(file);

        testFactoryFromProtectedFile(protectedFile, password);
        testFactoryFromProtectedStream(protectedFile, password);
        testFactoryFromProtectedNative(protectedFile, password);
    }

    /**
     * reads either a test-data file (filename) or a file outside the test-data folder (full path)
     */
    private static byte[] readFile(String filename) {
        byte[] bytes;
        try {
            bytes = _slTests.readFile(filename);
        } catch (final RuntimeException e) {
            if (!e.getMessage().startsWith("Sample file '" + filename + "' not found in data dir")) {
                throw e;
            }
            bytes = readExternalFile(filename);
        }
        return bytes;
    }

    private static byte[] readExternalFile(String path) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            InputStream fis = new FileInputStream(path);
            byte[] buf = new byte[512];
            while (true) {
                int bytesRead = fis.read(buf);
                if (bytesRead < 1) {
                    break;
                }
                baos.write(buf, 0, bytesRead);
            }
            fis.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    /**
     * FIXME:
     * bug 58779: Closing an XMLSlideShow that was created with {@link SlideShowFactory#create(File)} modifies the file
     *
     * @param filename the sample filename or full path of the slideshow to check before and after closing
     * @param ss the slideshow to close or revert
     */
    private static void assertCloseDoesNotModifyFile(String filename, SlideShow<?,?> ss) throws IOException {
        final byte[] before = readFile(filename);
        ss.close();
        final byte[] after = readFile(filename);

        try {
            assertArrayEquals(filename + " sample file was modified as a result of closing the slideshow",
                    before, after);
        } catch (AssertionError e) {
            // if the file after closing is different, then re-set 
            // the file to the state before in order to not have a dirty SCM 
            // working tree when running this test
            try (FileOutputStream str = new FileOutputStream(_slTests.getFile(filename))) {
                str.write(before);
            }
            
            throw e;
        }
    }

    private static File fromFile(String file) {
        return (file.contains("/") || file.contains("\\"))
            ? new File(file)
            : _slTests.getFile(file);
    }

    private static InputStream fromStream(String file) throws IOException {
        return (file.contains("/") || file.contains("\\"))
            ? new FileInputStream(file)
            : _slTests.openResourceAsStream(file);
    }

}
