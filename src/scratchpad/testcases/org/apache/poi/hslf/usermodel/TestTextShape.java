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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hslf.HSLFTestDataSamples;
import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.sl.usermodel.ShapeType;
import org.junit.Test;

/**
 * Verify behavior of <code>TextShape</code> and its sub-classes
 */
public final class TestTextShape {
    @Test
    public void createAutoShape(){
        HSLFTextShape shape = new HSLFAutoShape(ShapeType.TRAPEZOID);
        assertNull(shape.getEscherTextboxWrapper());
        assertNotNull(shape.getTextParagraphs());
        assertNotNull(shape.getEscherTextboxWrapper());
        assertEquals("", shape.getText());
        assertEquals(-1, shape.getTextParagraphs().get(0).getIndex());
    }

    @Test
    public void createTextBox(){
        HSLFTextShape shape = new HSLFTextBox();
        List<HSLFTextParagraph> paras = shape.getTextParagraphs();
        assertNotNull(paras);
        assertNotNull(shape.getText());
        assertNotNull(shape.getEscherTextboxWrapper());

        assertNotNull(shape.getTextParagraphs());
        assertNotNull(shape.getEscherTextboxWrapper());
        assertEquals("", shape.getText());

    }

    /**
     * Verify we can get text from TextShape in the following cases:
     *  - placeholders
     *  - normal TextBox object
     *  - text in auto-shapes
     */
    @Test
    public void read() throws IOException {
        HSLFSlideShow ppt = HSLFTestDataSamples.getSlideShow("text_shapes.ppt");

        List<String> lst1 = new ArrayList<>();
        HSLFSlide slide = ppt.getSlides().get(0);
        for (HSLFShape shape : slide.getShapes()) {
            assertTrue("Expected TextShape but found " + shape.getClass().getName(), shape instanceof HSLFTextShape);
            HSLFTextShape tx = (HSLFTextShape)shape;
            List<HSLFTextParagraph> paras = tx.getTextParagraphs();
            assertNotNull(paras);
            int runType = paras.get(0).getRunType();

            ShapeType type = shape.getShapeType();
            String rawText = HSLFTextParagraph.getRawText(paras);
            switch (type){
                case TEXT_BOX:
                    assertEquals("Text in a TextBox", rawText);
                    break;
                case RECT:
                    if(runType == TextHeaderAtom.OTHER_TYPE) {
                        assertEquals("Rectangle", rawText);
                    } else if(runType == TextHeaderAtom.TITLE_TYPE) {
                        assertEquals("Title Placeholder", rawText);
                    }
                    break;
                case OCTAGON:
                    assertEquals("Octagon", rawText);
                    break;
                case ELLIPSE:
                    assertEquals("Ellipse", rawText);
                    break;
                case ROUND_RECT:
                    assertEquals("RoundRectangle", rawText);
                    break;
                default:
                    fail("Unexpected shape: " + shape.getShapeName());

            }
            lst1.add(rawText);
        }

        List<String> lst2 = new ArrayList<>();
        for (List<HSLFTextParagraph> paras : slide.getTextParagraphs()) {
            lst2.add(HSLFTextParagraph.getRawText(paras));
        }

        assertTrue(lst1.containsAll(lst2));
        ppt.close();
    }

    @Test
    public void readWrite() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow();
        HSLFSlide slide =  ppt.createSlide();

        HSLFTextShape shape1 = new HSLFTextBox();
        shape1.setText("Hello, World!");
        slide.addShape(shape1);

        shape1.moveTo(100, 100);

        HSLFTextShape shape2 = new HSLFAutoShape(ShapeType.RIGHT_ARROW);
        shape2.setText("Testing TextShape");
        slide.addShape(shape2);
        shape2.moveTo(300, 300);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray()));
        slide = ppt.getSlides().get(0);
        List<HSLFShape> shape = slide.getShapes();

        assertTrue(shape.get(0) instanceof HSLFTextShape);
        shape1 = (HSLFTextShape)shape.get(0);
        assertEquals(ShapeType.TEXT_BOX, shape1.getShapeType());
        assertEquals("Hello, World!", shape1.getText());

        assertTrue(shape.get(1) instanceof HSLFTextShape);
        shape1 = (HSLFTextShape)shape.get(1);
        assertEquals(ShapeType.RIGHT_ARROW, shape1.getShapeType());
        assertEquals("Testing TextShape", shape1.getText());
        ppt.close();
    }

    @Test
    public void margins() throws IOException {
        HSLFSlideShow ppt = HSLFTestDataSamples.getSlideShow("text-margins.ppt");

        HSLFSlide slide = ppt.getSlides().get(0);

        Map<String,HSLFTextShape> map = new HashMap<>();
        for (HSLFShape shape : slide.getShapes()) {
            if(shape instanceof HSLFTextShape){
                HSLFTextShape tx = (HSLFTextShape)shape;
                map.put(tx.getText(), tx);
            }
        }

        HSLFTextShape tx;

        tx = map.get("TEST1");
        assertEquals(7.2, tx.getLeftInset(), 0);
        assertEquals(7.2, tx.getRightInset(), 0);
        assertEquals(28.34, tx.getTopInset(), 0.01);
        assertEquals(3.6, tx.getBottomInset(), 0);

        tx = map.get("TEST2");
        assertEquals(7.2, tx.getLeftInset(), 0);
        assertEquals(7.2, tx.getRightInset(), 0);
        assertEquals(3.6, tx.getTopInset(), 0);
        assertEquals(28.34, tx.getBottomInset(), 0.01);

        tx = map.get("TEST3");
        assertEquals(28.34, tx.getLeftInset(), 0.01);
        assertEquals(7.2, tx.getRightInset(), 0);
        assertEquals(3.6, tx.getTopInset(), 0);
        assertEquals(3.6, tx.getBottomInset(), 0);

        tx = map.get("TEST4");
        assertEquals(7.2, tx.getLeftInset(), 0);
        assertEquals(28.34, tx.getRightInset(), 0.01);
        assertEquals(3.6, tx.getTopInset(), 0);
        assertEquals(3.6, tx.getBottomInset(), 0);
        
        ppt.close();
    }

    @Test
    public void bug52599() throws IOException {
        HSLFSlideShow ppt = HSLFTestDataSamples.getSlideShow("52599.ppt");

        HSLFSlide slide = ppt.getSlides().get(0);
        List<HSLFShape> sh = slide.getShapes();
        assertEquals(3, sh.size());

        HSLFTextShape sh0 = (HSLFTextShape)sh.get(0);
        assertNotNull(sh0.getTextParagraphs());
        assertEquals("", sh0.getText());

        HSLFTextShape sh1 = (HSLFTextShape)sh.get(1);
        assertNotNull(sh1.getTextParagraphs());
        assertEquals("", sh1.getText());

        HSLFTextShape sh2 = (HSLFTextShape)sh.get(2);
        assertEquals("this box should be shown just once", sh2.getText());
        assertEquals(-1, sh2.getTextParagraphs().get(0).getIndex());
        ppt.close();
    }
}
