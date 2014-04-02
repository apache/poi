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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hslf.exceptions.EncryptedPowerPointFileException;
import org.apache.poi.hslf.record.ColorSchemeAtom;
import org.apache.poi.hslf.record.PPDrawing;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.POIDataSamples;

/**
 * Test common functionality of the <code>Sheet</code> object.
 * For each ppt in the test directory check that all sheets are properly initialized
 *
 * @author Yegor Kozlov
 */
public final class TestSheet extends TestCase {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    /**
     * For each ppt in the test directory check that all sheets are properly initialized
     */
    public void testSheet() throws Exception {
        String[] tests = {"SampleShow.ppt", "backgrounds.ppt", "text_shapes.ppt", "pictures.ppt"};
        for (String file : tests) {
            try {
                SlideShow ppt = new SlideShow(_slTests.openResourceAsStream(file));
                doSlideShow(ppt);
            } catch (EncryptedPowerPointFileException e){
                ; //skip encrypted ppt
            }
        }
    }

    private void doSlideShow(SlideShow ppt) {
        Slide[] slide = ppt.getSlides();
        for (int i = 0; i < slide.length; i++) {
            verify(slide[i]);

            Notes notes = slide[i].getNotesSheet();
            if(notes != null) verify(notes);

            MasterSheet master = slide[i].getMasterSheet();
            assertNotNull(master);
            verify(master);
        }
    }

    private void verify(Sheet sheet){
        assertNotNull(sheet.getSlideShow());

        ColorSchemeAtom colorscheme = sheet.getColorScheme();
        assertNotNull(colorscheme);

        PPDrawing ppdrawing = sheet.getPPDrawing();
        assertNotNull(ppdrawing);

        Background background = sheet.getBackground();
        assertNotNull(background);

        assertTrue(sheet._getSheetNumber() != 0);
        assertTrue(sheet._getSheetRefId() != 0);

        TextRun[] txt = sheet.getTextRuns();
        if (txt == null) {
            throw new AssertionFailedError("no text runs");
        }
        for (int i = 0; i < txt.length; i++) {
            assertNotNull(txt[i].getSheet());
        }

        Shape[] shape = sheet.getShapes();
        if (shape == null) {
            throw new AssertionFailedError("no shapes");
        }
        for (int i = 0; i < shape.length; i++) {
            assertNotNull(shape[i].getSpContainer());
            assertNotNull(shape[i].getSheet());
            assertNotNull(shape[i].getShapeName());
            assertNotNull(shape[i].getAnchor());
        }
    }
}
