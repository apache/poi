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

package org.apache.poi.hslf;

import static org.apache.poi.hslf.usermodel.HSLFSlideShow.POWERPOINT_DOCUMENT;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests that HSLFSlideShow writes the powerpoint bit of data back out
 *  correctly. Currently, that means being the same as what it read in
 */
public final class TestReWrite {
    private static final POIDataSamples SAMPLES = POIDataSamples.getSlideShowInstance();

    @ParameterizedTest
    @ValueSource(strings = { "basic_test_ppt_file.ppt", "ParagraphStylesShorterThanCharStyles.ppt" })
    void testWritesOutTheSame(String testfile) throws Exception {
        try (POIFSFileSystem pfs = new POIFSFileSystem(SAMPLES.openResourceAsStream(testfile));
             HSLFSlideShowImpl hss = new HSLFSlideShowImpl(pfs)) {

            // Write out to a byte array, and to a temp file
            UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
            hss.write(baos);

            final File file = TempFile.createTempFile("TestHSLF", ".ppt");
            final File file2 = TempFile.createTempFile("TestHSLF", ".ppt");
            hss.write(file);
            hss.write(file2);


            // Build an input stream of it, and read back as a POIFS from the stream
            try (POIFSFileSystem npfS = new POIFSFileSystem(baos.toInputStream());
                // And the same on the temp file
                POIFSFileSystem npfF = new POIFSFileSystem(file)) {

                // And another where we do an in-place write
                try (POIFSFileSystem npfRF = new POIFSFileSystem(file2, false);
                     HSLFSlideShowImpl hssRF = new HSLFSlideShowImpl(npfRF)) {
                    hssRF.write();
                }
                try (POIFSFileSystem npfRF = new POIFSFileSystem(file2)) {
                    // Check all of them in turn
                    for (POIFSFileSystem npf : new POIFSFileSystem[]{npfS, npfF, npfRF}) {
                        assertSame(pfs, npf);
                    }
                }
            }
        }
    }

    @Test
    void testWithMacroStreams() throws IOException {
        try (POIFSFileSystem pfsC = new POIFSFileSystem(SAMPLES.openResourceAsStream("WithMacros.ppt"));
            HSLFSlideShowImpl hssC = new HSLFSlideShowImpl(pfsC)) {
            // Check that they're apparently the same
            assertSlideShowWritesOutTheSame(hssC, pfsC);

            // Currently has a Macros stream
            assertNotNull(pfsC.getRoot().getEntryCaseInsensitive("Macros"));

            // Write out normally, will loose the macro stream
            UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
            hssC.write(baos);
            try (POIFSFileSystem pfsNew = new POIFSFileSystem(baos.toInputStream())) {
                assertFalse(pfsNew.getRoot().hasEntryCaseInsensitive("Macros"));
            }

            // But if we write out with nodes preserved, will be there
            baos.reset();
            hssC.write(baos, true);
            try (POIFSFileSystem pfsNew = new POIFSFileSystem(baos.toInputStream())) {
                assertTrue(pfsNew.getRoot().hasEntryCaseInsensitive("Macros"));
            }
        }
    }

    /**
     * Ensure that simply opening a slideshow (usermodel) view of it
     *  doesn't change things
     */
    @Test
    void testSlideShowWritesOutTheSame() throws Exception {
        try (POIFSFileSystem pfsA = new POIFSFileSystem(SAMPLES.openResourceAsStream("basic_test_ppt_file.ppt"));
            HSLFSlideShowImpl hssA = new HSLFSlideShowImpl(pfsA)) {
            assertSlideShowWritesOutTheSame(hssA, pfsA);
        }

        // Some bug in StyleTextPropAtom rewriting means this will fail
        // We need to identify and fix that first
        //assertSlideShowWritesOutTheSame(hssB, pfsB);
    }

    void assertSlideShowWritesOutTheSame(HSLFSlideShowImpl hss, POIFSFileSystem pfs) throws IOException {
        // Create a slideshow covering it
        @SuppressWarnings("resource")
        HSLFSlideShow ss = new HSLFSlideShow(hss);
        assertDoesNotThrow(ss::getSlides);
        assertDoesNotThrow(ss::getNotes);

        // Now write out to a byte array
        UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get();
        hss.write(baos);

        // Use POIFS to query that lot
        try (POIFSFileSystem npfs = new POIFSFileSystem(baos.toInputStream())) {
            assertSame(pfs, npfs);
        }
    }

    private void assertSame(POIFSFileSystem origPFS, POIFSFileSystem newPFS) throws IOException {
        // Check that the "PowerPoint Document" sections have the same size
        DocumentEntry oProps = (DocumentEntry) origPFS.getRoot().getEntryCaseInsensitive(POWERPOINT_DOCUMENT);
        DocumentEntry nProps = (DocumentEntry) newPFS.getRoot().getEntryCaseInsensitive(POWERPOINT_DOCUMENT);
        assertEquals(oProps.getSize(), nProps.getSize());


        // Check that they contain the same data
        try (InputStream os = origPFS.createDocumentInputStream(POWERPOINT_DOCUMENT);
             InputStream ns = newPFS.createDocumentInputStream(POWERPOINT_DOCUMENT)) {

            byte[] _oData = IOUtils.toByteArray(os, oProps.getSize());
            byte[] _nData = IOUtils.toByteArray(ns, nProps.getSize());

            assertArrayEquals(_oData, _nData);
        }
    }


    @Test
    void test48593() throws IOException {
        try (HSLFSlideShow ppt1 = new HSLFSlideShow()) {
            ppt1.createSlide();
            try (HSLFSlideShow ppt2 = HSLFTestDataSamples.writeOutAndReadBack(ppt1)) {
                ppt2.createSlide();
                try (HSLFSlideShow ppt3 = HSLFTestDataSamples.writeOutAndReadBack(ppt2)) {
                    ppt3.createSlide();
                    try (HSLFSlideShow ppt4 = HSLFTestDataSamples.writeOutAndReadBack(ppt3)) {
                        ppt4.createSlide();
                        try (HSLFSlideShow ppt5 = HSLFTestDataSamples.writeOutAndReadBack(ppt4)) {
                            assertEquals(4, ppt5.getSlides().size());
                        }
                    }
                }
            }
        }
    }
}
