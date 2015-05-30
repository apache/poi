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
package org.apache.poi.xslf.usermodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.poi.xslf.XSLFTestDataSamples;
import org.junit.Test;

public class TestXSLFNotes {

    @Test
    public void createNewNote() {

        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide1 = ppt.createSlide();

        assertNull(ppt.getNotesMaster());
        assertNull(slide1.getNotes());

        XSLFNotes notesSlide = ppt.getNotesSlide(slide1);
        assertNotNull(ppt.getNotesMaster());
        assertNotNull(notesSlide);

        String note = null;
        for (XSLFTextShape shape : notesSlide.getPlaceholders()) {
            if (shape.getTextType() == Placeholder.BODY) {
                shape.setText("New Note");
                note = shape.getText();
                break;
            }
        }
        assertNotNull(note);
        assertEquals("New Note", note);
    }

    @Test
    public void addNote() {

        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("sample.pptx");

        XSLFSlide slide = ppt.createSlide();
        XSLFNotes notesSlide = ppt.getNotesSlide(slide);
        assertNotNull(notesSlide);

        String note = null;
        for (XSLFTextShape shape : notesSlide.getPlaceholders()) {
            if (shape.getTextType() == Placeholder.BODY) {
                shape.setText("New Note");
                note = shape.getText();
                break;
            }
        }
        assertNotNull(note);
        assertEquals("New Note", note);
    }

    @Test
    public void replaceNotes() {

        XMLSlideShow ppt = XSLFTestDataSamples.openSampleDocument("sample.pptx");

        for (XSLFSlide slide : ppt.getSlides()) {
            assertNotNull(slide.getNotes());

            XSLFNotes notesSlide = ppt.getNotesSlide(slide);
            assertNotNull(notesSlide);

            String note = null;
            for (XSLFTextShape shape : notesSlide.getPlaceholders()) {
                if (shape.getTextType() == Placeholder.BODY) {
                    shape.setText("New Note");
                    note = shape.getText();
                    break;
                }
            }
            assertNotNull(note);
            assertEquals("New Note", note);
        }
    }
}
