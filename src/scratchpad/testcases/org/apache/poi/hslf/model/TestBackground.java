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
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.hslf.record.Document;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.usermodel.ShapeType;
import org.junit.Test;


/**
 * Test <code>Fill</code> object.
 *
 * @author Yegor Kozlov
 */
public final class TestBackground {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    /**
     * Default background for slide, shape and slide master.
     */
    @Test
    public void defaults() {
        HSLFSlideShow ppt = new HSLFSlideShow();

        assertEquals(HSLFFill.FILL_SOLID, ppt.getSlidesMasters()[0].getBackground().getFill().getFillType());

        HSLFSlide slide = ppt.createSlide();
        assertTrue(slide.getFollowMasterBackground());
        assertEquals(HSLFFill.FILL_SOLID, slide.getBackground().getFill().getFillType());

        HSLFShape shape = new HSLFAutoShape(ShapeType.RECT);
        assertEquals(HSLFFill.FILL_SOLID, shape.getFill().getFillType());
    }

    /**
     * Read fill information from an reference ppt file
     */
    @Test
    public void readBackground() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow(_slTests.openResourceAsStream("backgrounds.ppt"));
        HSLFFill fill;
        HSLFShape shape;

        HSLFSlide[] slide = ppt.getSlides();

        fill = slide[0].getBackground().getFill();
        assertEquals(HSLFFill.FILL_PICTURE, fill.getFillType());
        shape = slide[0].getShapes()[0];
        assertEquals(HSLFFill.FILL_SOLID, shape.getFill().getFillType());

        fill = slide[1].getBackground().getFill();
        assertEquals(HSLFFill.FILL_PATTERN, fill.getFillType());
        shape = slide[1].getShapes()[0];
        assertEquals(HSLFFill.FILL_BACKGROUND, shape.getFill().getFillType());

        fill = slide[2].getBackground().getFill();
        assertEquals(HSLFFill.FILL_TEXTURE, fill.getFillType());
        shape = slide[2].getShapes()[0];
        assertEquals(HSLFFill.FILL_PICTURE, shape.getFill().getFillType());

        fill = slide[3].getBackground().getFill();
        assertEquals(HSLFFill.FILL_SHADE_CENTER, fill.getFillType());
        shape = slide[3].getShapes()[0];
        assertEquals(HSLFFill.FILL_SHADE, shape.getFill().getFillType());
    }

    /**
     * Create a ppt with various fill effects
     */
    @Test
    public void backgroundPicture() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow();
        HSLFSlide slide;
        HSLFFill fill;
        HSLFShape shape;
        int idx;

        //slide 1
        slide = ppt.createSlide();
        slide.setFollowMasterBackground(false);
        fill = slide.getBackground().getFill();
        idx = ppt.addPicture(_slTests.readFile("tomcat.png"), HSLFPictureShape.PNG);
        fill.setFillType(HSLFFill.FILL_PICTURE);
        fill.setPictureData(idx);

        shape = new HSLFAutoShape(ShapeType.RECT);
        shape.setAnchor(new java.awt.Rectangle(100, 100, 200, 200));
        fill = shape.getFill();
        fill.setFillType(HSLFFill.FILL_SOLID);
        slide.addShape(shape);

        //slide 2
        slide = ppt.createSlide();
        slide.setFollowMasterBackground(false);
        fill = slide.getBackground().getFill();
        idx = ppt.addPicture(_slTests.readFile("tomcat.png"), HSLFPictureShape.PNG);
        fill.setFillType(HSLFFill.FILL_PATTERN);
        fill.setPictureData(idx);
        fill.setBackgroundColor(Color.green);
        fill.setForegroundColor(Color.red);

        shape = new HSLFAutoShape(ShapeType.RECT);
        shape.setAnchor(new java.awt.Rectangle(100, 100, 200, 200));
        fill = shape.getFill();
        fill.setFillType(HSLFFill.FILL_BACKGROUND);
        slide.addShape(shape);

        //slide 3
        slide = ppt.createSlide();
        slide.setFollowMasterBackground(false);
        fill = slide.getBackground().getFill();
        idx = ppt.addPicture(_slTests.readFile("tomcat.png"), HSLFPictureShape.PNG);
        fill.setFillType(HSLFFill.FILL_TEXTURE);
        fill.setPictureData(idx);

        shape = new HSLFAutoShape(ShapeType.RECT);
        shape.setAnchor(new java.awt.Rectangle(100, 100, 200, 200));
        fill = shape.getFill();
        fill.setFillType(HSLFFill.FILL_PICTURE);
        idx = ppt.addPicture(_slTests.readFile("clock.jpg"), HSLFPictureShape.JPEG);
        fill.setPictureData(idx);
        slide.addShape(shape);

        // slide 4
        slide = ppt.createSlide();
        slide.setFollowMasterBackground(false);
        fill = slide.getBackground().getFill();
        fill.setFillType(HSLFFill.FILL_SHADE_CENTER);
        fill.setBackgroundColor(Color.white);
        fill.setForegroundColor(Color.darkGray);

        shape = new HSLFAutoShape(ShapeType.RECT);
        shape.setAnchor(new java.awt.Rectangle(100, 100, 200, 200));
        fill = shape.getFill();
        fill.setFillType(HSLFFill.FILL_SHADE);
        fill.setBackgroundColor(Color.red);
        fill.setForegroundColor(Color.green);
        slide.addShape(shape);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new HSLFSlideShowImpl(new ByteArrayInputStream(out.toByteArray())));
        HSLFSlide[] slides = ppt.getSlides();

        fill = slides[0].getBackground().getFill();
        assertEquals(HSLFFill.FILL_PICTURE, fill.getFillType());
        assertEquals(3, getFillPictureRefCount(slides[0].getBackground(), fill));
        shape = slides[0].getShapes()[0];
        assertEquals(HSLFFill.FILL_SOLID, shape.getFill().getFillType());

        fill = slides[1].getBackground().getFill();
        assertEquals(HSLFFill.FILL_PATTERN, fill.getFillType());
        shape = slides[1].getShapes()[0];
        assertEquals(HSLFFill.FILL_BACKGROUND, shape.getFill().getFillType());

        fill = slides[2].getBackground().getFill();
        assertEquals(HSLFFill.FILL_TEXTURE, fill.getFillType());
        assertEquals(3, getFillPictureRefCount(slides[2].getBackground(), fill));
        shape = slides[2].getShapes()[0];
        assertEquals(HSLFFill.FILL_PICTURE, shape.getFill().getFillType());
        assertEquals(1, getFillPictureRefCount(shape, fill));

        fill = slides[3].getBackground().getFill();
        assertEquals(HSLFFill.FILL_SHADE_CENTER, fill.getFillType());
        shape = slides[3].getShapes()[0];
        assertEquals(HSLFFill.FILL_SHADE, shape.getFill().getFillType());

    }

    private int getFillPictureRefCount(HSLFShape shape, HSLFFill fill) {
        EscherOptRecord opt = shape.getEscherOptRecord();
        EscherSimpleProperty p = HSLFShape.getEscherProperty(opt, EscherProperties.FILL__PATTERNTEXTURE);
        if(p != null) {
            int idx = p.getPropertyValue();

            HSLFSheet sheet = shape.getSheet();
            HSLFSlideShow ppt = sheet.getSlideShow();
            Document doc = ppt.getDocumentRecord();
            EscherContainerRecord dggContainer = doc.getPPDrawingGroup().getDggContainer();
            EscherContainerRecord bstore = HSLFShape.getEscherChild(dggContainer, EscherContainerRecord.BSTORE_CONTAINER);
            List<EscherRecord> lst = bstore.getChildRecords();
            return ((EscherBSERecord)lst.get(idx-1)).getRef();
        }
        return 0;
    }

}
