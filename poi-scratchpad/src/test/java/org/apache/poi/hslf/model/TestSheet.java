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

package org.apache.poi.hslf.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.exceptions.EncryptedPowerPointFileException;
import org.apache.poi.hslf.record.ColorSchemeAtom;
import org.apache.poi.hslf.record.PPDrawing;
import org.apache.poi.hslf.usermodel.*;
import org.junit.jupiter.api.Test;

/**
 * Test common functionality of the <code>Sheet</code> object.
 * For each ppt in the test directory check that all sheets are properly initialized
 *
 * @author Yegor Kozlov
 */
public final class TestSheet {
    private static final POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    /**
     * For each ppt in the test directory check that all sheets are properly initialized
     */
    @Test
    void testSheet() throws Exception {
        String[] tests = {"SampleShow.ppt", "backgrounds.ppt", "text_shapes.ppt", "pictures.ppt"};
        for (String file : tests) {
            try {
                HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream(file));
                doSlideShow(ppt);
            } catch (EncryptedPowerPointFileException e){
                //skip encrypted ppt
            }
        }
    }

    private void doSlideShow(HSLFSlideShow ppt) {
        for (HSLFSlide slide : ppt.getSlides()) {
            verify(slide);

            HSLFNotes notes = slide.getNotes();
            if(notes != null) verify(notes);

            HSLFMasterSheet master = slide.getMasterSheet();
            assertNotNull(master);
            verify(master);
        }
    }

    private void verify(HSLFSheet sheet){
        assertNotNull(sheet.getSlideShow());

        ColorSchemeAtom colorscheme = sheet.getColorScheme();
        assertNotNull(colorscheme);

        PPDrawing ppdrawing = sheet.getPPDrawing();
        assertNotNull(ppdrawing);

        HSLFBackground background = sheet.getBackground();
        assertNotNull(background);

        assertTrue(sheet._getSheetNumber() != 0);
        assertTrue(sheet._getSheetRefId() != 0);

        List<List<HSLFTextParagraph>> txt = sheet.getTextParagraphs();
        // assertTrue("no text runs", txt != null && !txt.isEmpty());
        // backgrounds.ppt has no texts
        for (List<HSLFTextParagraph> t : txt) {
            for (HSLFTextParagraph tp : t) {
                assertNotNull(tp.getSheet());
            }
        }

        List<HSLFShape> shape = sheet.getShapes();
        assertTrue(shape != null && !shape.isEmpty(), "no shapes");
        for (HSLFShape s : shape) {
            assertNotNull(s.getSpContainer());
            assertNotNull(s.getSheet());
            assertNotNull(s.getShapeName());
            assertNotNull(s.getAnchor());
        }
    }
}
