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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.usermodel.ShapeType;
import org.junit.Test;

/**
 * Verify behavior of <code>TextShape</code> and its sub-classes
 *
 * @author Yegor Kozlov
 */
public final class TestTextShape {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    @Test
    public void createAutoShape(){
        HSLFTextShape shape = new HSLFAutoShape(ShapeType.TRAPEZOID);
        assertNull(shape.getTextParagraph());
        assertNull(shape.getText());
        assertNull(shape.getEscherTextboxWrapper());

        HSLFTextParagraph run = shape.createTextRun();
        assertNotNull(run);
        assertNotNull(shape.getTextParagraph());
        assertNotNull(shape.getEscherTextboxWrapper());
        assertEquals("", shape.getText());
        assertSame(run, shape.createTextRun());
        assertEquals(-1, run.getIndex());
    }

    @Test
    public void createTextBox(){
        HSLFTextShape shape = new HSLFTextBox();
        HSLFTextParagraph run = shape.getTextParagraph();
        assertNotNull(run);
        assertNotNull(shape.getText());
        assertNotNull(shape.getEscherTextboxWrapper());

        assertSame(run, shape.createTextRun());
        assertNotNull(shape.getTextParagraph());
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
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("text_shapes.ppt"));

        List<String> lst1 = new ArrayList<String>();
        HSLFSlide slide = ppt.getSlides()[0];
        HSLFShape[] shape = slide.getShapes();
        for (int i = 0; i < shape.length; i++) {
            assertTrue("Expected TextShape but found " + shape[i].getClass().getName(), shape[i] instanceof HSLFTextShape);
            HSLFTextShape tx = (HSLFTextShape)shape[i];
            HSLFTextParagraph run = tx.getTextParagraph();
            assertNotNull(run);
            int runType = run.getRunType();

            ShapeType type = shape[i].getShapeType();
            switch (type){
                case TEXT_BOX:
                    assertEquals("Text in a TextBox", run.getText());
                    break;
                case RECT:
                    if(runType == TextHeaderAtom.OTHER_TYPE)
                        assertEquals("Rectangle", run.getText());
                    else if(runType == TextHeaderAtom.TITLE_TYPE)
                        assertEquals("Title Placeholder", run.getText());
                    break;
                case OCTAGON:
                    assertEquals("Octagon", run.getText());
                    break;
                case ELLIPSE:
                    assertEquals("Ellipse", run.getText());
                    break;
                case ROUND_RECT:
                    assertEquals("RoundRectangle", run.getText());
                    break;
                default:
                    fail("Unexpected shape: " + shape[i].getShapeName());

            }
            lst1.add(run.getText());
        }

        List<String> lst2 = new ArrayList<String>();
        HSLFTextParagraph[] run = slide.getTextRuns();
        for (int i = 0; i < run.length; i++) {
            lst2.add(run[i].getText());
        }

        assertTrue(lst1.containsAll(lst2));
    }

    @Test
    public void readWrite() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow();
        HSLFSlide slide =  ppt.createSlide();

        HSLFTextShape shape1 = new HSLFTextBox();
        HSLFTextParagraph run1 = shape1.createTextRun();
        run1.setText("Hello, World!");
        slide.addShape(shape1);

        shape1.moveTo(100, 100);

        HSLFTextShape shape2 = new HSLFAutoShape(ShapeType.RIGHT_ARROW);
        HSLFTextParagraph run2 = shape2.createTextRun();
        run2.setText("Testing TextShape");
        slide.addShape(shape2);
        shape2.moveTo(300, 300);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray()));
        slide = ppt.getSlides()[0];
        HSLFShape[] shape = slide.getShapes();

        assertTrue(shape[0] instanceof HSLFTextShape);
        shape1 = (HSLFTextShape)shape[0];
        assertEquals(ShapeType.TEXT_BOX, shape1.getShapeType());
        assertEquals("Hello, World!", shape1.getTextParagraph().getText());

        assertTrue(shape[1] instanceof HSLFTextShape);
        shape1 = (HSLFTextShape)shape[1];
        assertEquals(ShapeType.RIGHT_ARROW, shape1.getShapeType());
        assertEquals("Testing TextShape", shape1.getTextParagraph().getText());
    }

    @Test
    public void margins() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("text-margins.ppt"));

        HSLFSlide slide = ppt.getSlides()[0];

        Map<String,HSLFTextShape> map = new HashMap<String,HSLFTextShape>();
        HSLFShape[] shape = slide.getShapes();
        for (int i = 0; i < shape.length; i++) {
            if(shape[i] instanceof HSLFTextShape){
                HSLFTextShape tx = (HSLFTextShape)shape[i];
                map.put(tx.getText(), tx);
            }
        }

        HSLFTextShape tx;

        tx = map.get("TEST1");
        assertEquals(0.1, tx.getMarginLeft()*HSLFShape.EMU_PER_POINT/HSLFShape.EMU_PER_INCH, 0.01);
        assertEquals(0.1, tx.getMarginRight()*HSLFShape.EMU_PER_POINT/HSLFShape.EMU_PER_INCH, 0.01);
        assertEquals(0.39, tx.getMarginTop()*HSLFShape.EMU_PER_POINT/HSLFShape.EMU_PER_INCH, 0.01);
        assertEquals(0.05, tx.getMarginBottom()*HSLFShape.EMU_PER_POINT/HSLFShape.EMU_PER_INCH, 0.01);

        tx = map.get("TEST2");
        assertEquals(0.1, tx.getMarginLeft()*HSLFShape.EMU_PER_POINT/HSLFShape.EMU_PER_INCH, 0.01);
        assertEquals(0.1, tx.getMarginRight()*HSLFShape.EMU_PER_POINT/HSLFShape.EMU_PER_INCH, 0.01);
        assertEquals(0.05, tx.getMarginTop()*HSLFShape.EMU_PER_POINT/HSLFShape.EMU_PER_INCH, 0.01);
        assertEquals(0.39, tx.getMarginBottom()*HSLFShape.EMU_PER_POINT/HSLFShape.EMU_PER_INCH, 0.01);

        tx = map.get("TEST3");
        assertEquals(0.39, tx.getMarginLeft()*HSLFShape.EMU_PER_POINT/HSLFShape.EMU_PER_INCH, 0.01);
        assertEquals(0.1, tx.getMarginRight()*HSLFShape.EMU_PER_POINT/HSLFShape.EMU_PER_INCH, 0.01);
        assertEquals(0.05, tx.getMarginTop()*HSLFShape.EMU_PER_POINT/HSLFShape.EMU_PER_INCH, 0.01);
        assertEquals(0.05, tx.getMarginBottom()*HSLFShape.EMU_PER_POINT/HSLFShape.EMU_PER_INCH, 0.01);

        tx = map.get("TEST4");
        assertEquals(0.1, tx.getMarginLeft()*HSLFShape.EMU_PER_POINT/HSLFShape.EMU_PER_INCH, 0.01);
        assertEquals(0.39, tx.getMarginRight()*HSLFShape.EMU_PER_POINT/HSLFShape.EMU_PER_INCH, 0.01);
        assertEquals(0.05, tx.getMarginTop()*HSLFShape.EMU_PER_POINT/HSLFShape.EMU_PER_INCH, 0.01);
        assertEquals(0.05, tx.getMarginBottom()*HSLFShape.EMU_PER_POINT/HSLFShape.EMU_PER_INCH, 0.01);
    }

    @Test
    public void bug52599() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("52599.ppt"));

        HSLFSlide slide = ppt.getSlides()[0];
        HSLFShape[] sh = slide.getShapes();
        assertEquals(3, sh.length);

        HSLFTextShape sh0 = (HSLFTextShape)sh[0];
        assertEquals(null, sh0.getText());
        assertEquals(null, sh0.getTextParagraph());

        HSLFTextShape sh1 = (HSLFTextShape)sh[1];
        assertEquals(null, sh1.getText());
        assertEquals(null, sh1.getTextParagraph());

        HSLFTextShape sh2 = (HSLFTextShape)sh[2];
        assertEquals("this box should be shown just once", sh2.getText());
        assertEquals(-1, sh2.getTextParagraph().getIndex());
    }
}
