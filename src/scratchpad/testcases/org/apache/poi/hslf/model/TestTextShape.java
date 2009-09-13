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

import junit.framework.TestCase;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.POIDataSamples;

/**
 * Verify behavior of <code>TextShape</code> and its sub-classes
 *
 * @author Yegor Kozlov
 */
public final class TestTextShape extends TestCase {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    public void testCreateAutoShape(){
        TextShape shape = new AutoShape(ShapeTypes.Trapezoid);
        assertNull(shape.getTextRun());
        assertNull(shape.getText());
        assertNull(shape.getEscherTextboxWrapper());

        TextRun run = shape.createTextRun();
        assertNotNull(run);
        assertNotNull(shape.getTextRun());
        assertNotNull(shape.getEscherTextboxWrapper());
        assertEquals("", shape.getText());
        assertSame(run, shape.createTextRun());

    }

    public void testCreateTextBox(){
        TextShape shape = new TextBox();
        TextRun run = shape.getTextRun();
        assertNotNull(run);
        assertNotNull(shape.getText());
        assertNotNull(shape.getEscherTextboxWrapper());

        assertSame(run, shape.createTextRun());
        assertNotNull(shape.getTextRun());
        assertNotNull(shape.getEscherTextboxWrapper());
        assertEquals("", shape.getText());

    }

    /**
     * Verify we can get text from TextShape in the following cases:
     *  - placeholders
     *  - normal TextBox object
     *  - text in auto-shapes
     */
    public void testRead() throws IOException {
        SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("text_shapes.ppt"));

        ArrayList lst1 = new ArrayList();
        Slide slide = ppt.getSlides()[0];
        Shape[] shape = slide.getShapes();
        for (int i = 0; i < shape.length; i++) {
            assertTrue("Expected TextShape but found " + shape[i].getClass().getName(), shape[i] instanceof TextShape);
            TextShape tx = (TextShape)shape[i];
            TextRun run = tx.getTextRun();
            assertNotNull(run);
            int runType = run.getRunType();

            int type = shape[i].getShapeType();
            switch (type){
                case ShapeTypes.TextBox:
                    assertEquals("Text in a TextBox", run.getText());
                    break;
                case ShapeTypes.Rectangle:
                    if(runType == TextHeaderAtom.OTHER_TYPE)
                        assertEquals("Rectangle", run.getText());
                    else if(runType == TextHeaderAtom.TITLE_TYPE)
                        assertEquals("Title Placeholder", run.getText());
                    break;
                case ShapeTypes.Octagon:
                    assertEquals("Octagon", run.getText());
                    break;
                case ShapeTypes.Ellipse:
                    assertEquals("Ellipse", run.getText());
                    break;
                case ShapeTypes.RoundRectangle:
                    assertEquals("RoundRectangle", run.getText());
                    break;
                default:
                    fail("Unexpected shape: " + shape[i].getShapeName());

            }
            lst1.add(run.getText());
        }

        ArrayList lst2 = new ArrayList();
        TextRun[] run = slide.getTextRuns();
        for (int i = 0; i < run.length; i++) {
            lst2.add(run[i].getText());
        }

        assertTrue(lst1.containsAll(lst2));
    }

    public void testReadWrite() throws IOException {
        SlideShow ppt = new SlideShow();
        Slide slide =  ppt.createSlide();

        TextShape shape1 = new TextBox();
        TextRun run1 = shape1.createTextRun();
        run1.setText("Hello, World!");
        slide.addShape(shape1);

        shape1.moveTo(100, 100);

        TextShape shape2 = new AutoShape(ShapeTypes.Arrow);
        TextRun run2 = shape2.createTextRun();
        run2.setText("Testing TextShape");
        slide.addShape(shape2);
        shape2.moveTo(300, 300);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new SlideShow(new ByteArrayInputStream(out.toByteArray()));
        slide = ppt.getSlides()[0];
        Shape[] shape = slide.getShapes();

        assertTrue(shape[0] instanceof TextShape);
        shape1 = (TextShape)shape[0];
        assertEquals(ShapeTypes.TextBox, shape1.getShapeType());
        assertEquals("Hello, World!", shape1.getTextRun().getText());

        assertTrue(shape[1] instanceof TextShape);
        shape1 = (TextShape)shape[1];
        assertEquals(ShapeTypes.Arrow, shape1.getShapeType());
        assertEquals("Testing TextShape", shape1.getTextRun().getText());
    }

    public void testMargins() throws IOException {
        SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("text-margins.ppt"));

        Slide slide = ppt.getSlides()[0];

        HashMap map = new HashMap();
        Shape[] shape = slide.getShapes();
        for (int i = 0; i < shape.length; i++) {
            if(shape[i] instanceof TextShape){
                TextShape tx = (TextShape)shape[i];
                map.put(tx.getText(), tx);
            }
        }

        TextShape tx;

        tx = (TextShape)map.get("TEST1");
        assertEquals(0.1, tx.getMarginLeft()*Shape.EMU_PER_POINT/Shape.EMU_PER_INCH, 0.01);
        assertEquals(0.1, tx.getMarginRight()*Shape.EMU_PER_POINT/Shape.EMU_PER_INCH, 0.01);
        assertEquals(0.39, tx.getMarginTop()*Shape.EMU_PER_POINT/Shape.EMU_PER_INCH, 0.01);
        assertEquals(0.05, tx.getMarginBottom()*Shape.EMU_PER_POINT/Shape.EMU_PER_INCH, 0.01);

        tx = (TextShape)map.get("TEST2");
        assertEquals(0.1, tx.getMarginLeft()*Shape.EMU_PER_POINT/Shape.EMU_PER_INCH, 0.01);
        assertEquals(0.1, tx.getMarginRight()*Shape.EMU_PER_POINT/Shape.EMU_PER_INCH, 0.01);
        assertEquals(0.05, tx.getMarginTop()*Shape.EMU_PER_POINT/Shape.EMU_PER_INCH, 0.01);
        assertEquals(0.39, tx.getMarginBottom()*Shape.EMU_PER_POINT/Shape.EMU_PER_INCH, 0.01);

        tx = (TextShape)map.get("TEST3");
        assertEquals(0.39, tx.getMarginLeft()*Shape.EMU_PER_POINT/Shape.EMU_PER_INCH, 0.01);
        assertEquals(0.1, tx.getMarginRight()*Shape.EMU_PER_POINT/Shape.EMU_PER_INCH, 0.01);
        assertEquals(0.05, tx.getMarginTop()*Shape.EMU_PER_POINT/Shape.EMU_PER_INCH, 0.01);
        assertEquals(0.05, tx.getMarginBottom()*Shape.EMU_PER_POINT/Shape.EMU_PER_INCH, 0.01);

        tx = (TextShape)map.get("TEST4");
        assertEquals(0.1, tx.getMarginLeft()*Shape.EMU_PER_POINT/Shape.EMU_PER_INCH, 0.01);
        assertEquals(0.39, tx.getMarginRight()*Shape.EMU_PER_POINT/Shape.EMU_PER_INCH, 0.01);
        assertEquals(0.05, tx.getMarginTop()*Shape.EMU_PER_POINT/Shape.EMU_PER_INCH, 0.01);
        assertEquals(0.05, tx.getMarginBottom()*Shape.EMU_PER_POINT/Shape.EMU_PER_INCH, 0.01);
    }
}
