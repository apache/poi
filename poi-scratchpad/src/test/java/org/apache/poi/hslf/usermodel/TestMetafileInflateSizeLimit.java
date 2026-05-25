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

package org.apache.poi.hslf.usermodel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.blip.EMF;
import org.apache.poi.hslf.blip.PICT;
import org.apache.poi.hslf.blip.WMF;
import org.apache.poi.hslf.record.RecordAtom;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that WMF, EMF, and PICT inflate operations respect the
 * {@link RecordAtom#getMaxRecordLength()} size limit to prevent zip-bomb
 * style decompression attacks.
 */
public class TestMetafileInflateSizeLimit {

    private static final POIDataSamples SLIDE_TESTS = POIDataSamples.getSlideShowInstance();

    private int savedMaxRecordLength;

    @BeforeEach
    void saveLimit() {
        savedMaxRecordLength = RecordAtom.getMaxRecordLength();
    }

    @AfterEach
    void restoreLimit() {
        RecordAtom.setMaxRecordLength(savedMaxRecordLength);
    }

    // -----------------------------------------------------------------------
    // WMF
    // -----------------------------------------------------------------------

    @Test
    void testWmfGetDataWithinLimit() throws IOException {
        byte[] wmfBytes = SLIDE_TESTS.readFile("santa.wmf");
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFPictureData pd = ppt.addPicture(wmfBytes, PictureType.WMF);
            assertDoesNotThrow(pd::getData,
                    "WMF getData() should succeed when limit is not exceeded");
        }
    }

    @Test
    void testWmfGetDataExceedsLimitThrows() throws IOException {
        byte[] wmfBytes = SLIDE_TESTS.readFile("santa.wmf");
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFPictureData pd = ppt.addPicture(wmfBytes, PictureType.WMF);
            // Set limit far below the actual decompressed size
            RecordAtom.setMaxRecordLength(10);
            assertThrows(RecordFormatException.class, pd::getData,
                    "WMF getData() should throw RecordFormatException when limit is exceeded");
        }
    }

    // -----------------------------------------------------------------------
    // EMF
    // -----------------------------------------------------------------------

    @Test
    void testEmfGetDataWithinLimit() throws IOException {
        byte[] emfBytes = SLIDE_TESTS.readFile("wrench.emf");
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFPictureData pd = ppt.addPicture(emfBytes, PictureType.EMF);
            assertDoesNotThrow(pd::getData,
                    "EMF getData() should succeed when limit is not exceeded");
        }
    }

    @Test
    void testEmfGetDataExceedsLimitThrows() throws IOException {
        byte[] emfBytes = SLIDE_TESTS.readFile("wrench.emf");
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFPictureData pd = ppt.addPicture(emfBytes, PictureType.EMF);
            // Set limit far below the actual decompressed size
            RecordAtom.setMaxRecordLength(10);
            assertThrows(RecordFormatException.class, pd::getData,
                    "EMF getData() should throw RecordFormatException when limit is exceeded");
        }
    }

    // -----------------------------------------------------------------------
    // PICT
    // -----------------------------------------------------------------------

    @Test
    void testPictGetDataWithinLimit() throws IOException {
        byte[] pictBytes = SLIDE_TESTS.readFile("cow.pict");
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFPictureData pd = ppt.addPicture(pictBytes, PictureType.PICT);
            assertDoesNotThrow(pd::getData,
                    "PICT getData() should succeed when limit is not exceeded");
        }
    }

    @Test
    void testPictGetDataExceedsLimitThrows() throws IOException {
        byte[] pictBytes = SLIDE_TESTS.readFile("cow.pict");
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFPictureData pd = ppt.addPicture(pictBytes, PictureType.PICT);
            // Set limit far below the actual decompressed size
            RecordAtom.setMaxRecordLength(10);
            assertThrows(RecordFormatException.class, pd::getData,
                    "PICT getData() should throw RecordFormatException when limit is exceeded");
        }
    }

    // -----------------------------------------------------------------------
    // Verify types
    // -----------------------------------------------------------------------

    @Test
    void testWmfAndEmfAndPictPictureTypes() throws IOException {
        byte[] wmfBytes = SLIDE_TESTS.readFile("santa.wmf");
        byte[] emfBytes = SLIDE_TESTS.readFile("wrench.emf");
        byte[] pictBytes = SLIDE_TESTS.readFile("cow.pict");
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFPictureData wmfPd = ppt.addPicture(wmfBytes, PictureType.WMF);
            HSLFPictureData emfPd = ppt.addPicture(emfBytes, PictureType.EMF);
            HSLFPictureData pictPd = ppt.addPicture(pictBytes, PictureType.PICT);

            List<HSLFPictureData> allPics = ppt.getPictureData();
            org.junit.jupiter.api.Assertions.assertTrue(allPics.get(0) instanceof WMF);
            org.junit.jupiter.api.Assertions.assertTrue(allPics.get(1) instanceof EMF);
            org.junit.jupiter.api.Assertions.assertTrue(allPics.get(2) instanceof PICT);
        }
    }
}
