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

package org.apache.poi.osgi;

import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.usermodel.*;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test to ensure that all our main formats can create, write
 * and read back in, when running under OSGi
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OSGiSlideShowIT extends BaseOSGiTestCase {


    // create a workbook, validate and write back
    void testSS(SlideShow ppt) throws Exception {
        Slide<?, ?> slide = ppt.createSlide();

        TextBox<?, ?> box1 = slide.createTextBox();
        box1.setTextPlaceholder(TextShape.TextPlaceholder.TITLE);
        box1.setText("HSLF in a Nutshell");
        box1.setAnchor(new Rectangle(36, 15, 648, 65));

        TextBox<?, ?> box2 = slide.createTextBox();
        box2.setTextPlaceholder(TextShape.TextPlaceholder.BODY);
        box2.setText(
                "HSLF provides a way to read, create and modify MS PowerPoint presentations\r" +
                        "Pure Java API - you don't need PowerPoint to read and write *.ppt files\r" +
                        "Comprehensive support of PowerPoint objects\r" +
                        "Rich text\r" +
                        "Tables\r" +
                        "Shapes\r" +
                        "Pictures\r" +
                        "Master slides\r" +
                        "Access to low level data structures"
        );

        List<? extends TextParagraph<?, ?, ?>> tp = box2.getTextParagraphs();
        for (int i : new byte[]{0, 1, 2, 8}) {
            tp.get(i).getTextRuns().get(0).setFontSize(28d);
        }
        for (int i : new byte[]{3, 4, 5, 6, 7}) {
            tp.get(i).getTextRuns().get(0).setFontSize(24d);
            tp.get(i).setIndentLevel(1);
        }
        box2.setAnchor(new Rectangle(36, 80, 648, 400));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ppt.write(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        ppt = SlideShowFactory.create(bais);
        assertEquals(1, ppt.getSlides().size());
        slide = (Slide) ppt.getSlides().iterator().next();
        assertEquals(2, slide.getShapes().size());
    }

    @Test
    public void testHSLF() throws Exception {
        testSS(new HSLFSlideShow());
    }

    @Test
    public void testXSLF() throws Exception {
        testSS(new XMLSlideShow());
    }

}
