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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import org.apache.poi.sl.usermodel.Placeholder;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;

/**
 * @author Yegor Kozlov
 */
class TestXSLFTextBox {

    @Test
    void testPlaceholder() throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();

        XSLFTextBox shape = slide.createTextBox();
        assertNull(shape.getTextType());
        shape.setPlaceholder(Placeholder.TITLE);
        assertEquals(Placeholder.TITLE, shape.getTextType());
        shape.setPlaceholder(null);
        assertNull(shape.getTextType());
        shape.setText("Apache POI");

        ppt.close();
    }

    /**
     * text box inherits default text proeprties from presentation.xml
     */
    @Test
    void testDefaultTextStyle() throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();

        // default character properties for paragraphs with level=1
        CTTextCharacterProperties pPr = ppt.getCTPresentation().getDefaultTextStyle().getLvl1PPr().getDefRPr();

        XSLFTextBox shape = slide.createTextBox();
        shape.setText("Apache POI");
        assertEquals(1, shape.getTextParagraphs().size());
        assertEquals(1, shape.getTextParagraphs().get(0).getTextRuns().size());

        XSLFTextRun r = shape.getTextParagraphs().get(0).getTextRuns().get(0);

        assertEquals(1800, pPr.getSz());
        assertEquals(18.0, r.getFontSize(), 0);
        assertEquals("Calibri", r.getFontFamily());

        pPr.setSz(900);
        pPr.getLatin().setTypeface("Arial");
        assertEquals(9.0, r.getFontSize(), 0);
        assertEquals("Arial", r.getFontFamily());

        // unset font size in presentation.xml. The value should be taken from master slide
        // from /p:sldMaster/p:txStyles/p:otherStyle/a:lvl1pPr/a:defRPr
        ppt.getCTPresentation().getDefaultTextStyle().getLvl1PPr().getDefRPr().unsetSz();
        pPr = slide.getSlideMaster().getXmlObject().getTxStyles().getOtherStyle().getLvl1PPr().getDefRPr();
        assertEquals(1800, pPr.getSz());
        assertEquals(18.0, r.getFontSize(), 0);
        pPr.setSz(2000);
        assertEquals(20.0, r.getFontSize(), 0);

        pPr.unsetSz();  // Should never be
        assertNull(r.getFontSize());

        ppt.close();
    }
}