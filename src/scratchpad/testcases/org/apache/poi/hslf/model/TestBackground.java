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
import java.io.IOException;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.hslf.HSLFTestDataSamples;
import org.apache.poi.hslf.record.Document;
import org.apache.poi.hslf.usermodel.HSLFAutoShape;
import org.apache.poi.hslf.usermodel.HSLFFill;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSheet;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
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
    public void defaults() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow();

        assertEquals(HSLFFill.FILL_SOLID, ppt.getSlideMasters().get(0).getBackground().getFill().getFillType());

        HSLFSlide slide = ppt.createSlide();
        assertTrue(slide.getFollowMasterBackground());
        assertEquals(HSLFFill.FILL_SOLID, slide.getBackground().getFill().getFillType());

        HSLFShape shape = new HSLFAutoShape(ShapeType.RECT);
        assertEquals(HSLFFill.FILL_SOLID, shape.getFill().getFillType());
        ppt.close();
    }

    /**
     * Read fill information from an reference ppt file
     */
    @Test
    public void readBackground() throws IOException {
        HSLFSlideShow ppt = HSLFTestDataSamples.getSlideShow("backgrounds.ppt");
        HSLFFill fill;
        HSLFShape shape;

        List<HSLFSlide> slide = ppt.getSlides();

        fill = slide.get(0).getBackground().getFill();
        assertEquals(HSLFFill.FILL_PICTURE, fill.getFillType());
        shape = slide.get(0).getShapes().get(0);
        assertEquals(HSLFFill.FILL_SOLID, shape.getFill().getFillType());

        fill = slide.get(1).getBackground().getFill();
        assertEquals(HSLFFill.FILL_PATTERN, fill.getFillType());
        shape = slide.get(1).getShapes().get(0);
        assertEquals(HSLFFill.FILL_BACKGROUND, shape.getFill().getFillType());

        fill = slide.get(2).getBackground().getFill();
        assertEquals(HSLFFill.FILL_TEXTURE, fill.getFillType());
        shape = slide.get(2).getShapes().get(0);
        assertEquals(HSLFFill.FILL_PICTURE, shape.getFill().getFillType());

        fill = slide.get(3).getBackground().getFill();
        assertEquals(HSLFFill.FILL_SHADE_CENTER, fill.getFillType());
        shape = slide.get(3).getShapes().get(0);
        assertEquals(HSLFFill.FILL_SHADE, shape.getFill().getFillType());
        ppt.close();
    }

    /**
     * Create a ppt with various fill effects
     */
    @Test
    public void backgroundPicture() throws IOException {
        HSLFSlideShow ppt1 = new HSLFSlideShow();
        HSLFSlide slide;
        HSLFFill fill;
        HSLFShape shape;
        HSLFPictureData data;

        //slide 1
        slide = ppt1.createSlide();
        slide.setFollowMasterBackground(false);
        fill = slide.getBackground().getFill();
        data = ppt1.addPicture(_slTests.readFile("tomcat.png"), PictureType.PNG);
        fill.setFillType(HSLFFill.FILL_PICTURE);
        fill.setPictureData(data);

        shape = new HSLFAutoShape(ShapeType.RECT);
        shape.setAnchor(new java.awt.Rectangle(100, 100, 200, 200));
        fill = shape.getFill();
        fill.setFillType(HSLFFill.FILL_SOLID);
        slide.addShape(shape);

        //slide 2
        slide = ppt1.createSlide();
        slide.setFollowMasterBackground(false);
        fill = slide.getBackground().getFill();
        data = ppt1.addPicture(_slTests.readFile("tomcat.png"), PictureType.PNG);
        fill.setFillType(HSLFFill.FILL_PATTERN);
        fill.setPictureData(data);
        fill.setBackgroundColor(Color.green);
        fill.setForegroundColor(Color.red);

        shape = new HSLFAutoShape(ShapeType.RECT);
        shape.setAnchor(new java.awt.Rectangle(100, 100, 200, 200));
        fill = shape.getFill();
        fill.setFillType(HSLFFill.FILL_BACKGROUND);
        slide.addShape(shape);

        //slide 3
        slide = ppt1.createSlide();
        slide.setFollowMasterBackground(false);
        fill = slide.getBackground().getFill();
        data = ppt1.addPicture(_slTests.readFile("tomcat.png"), PictureType.PNG);
        fill.setFillType(HSLFFill.FILL_TEXTURE);
        fill.setPictureData(data);

        shape = new HSLFAutoShape(ShapeType.RECT);
        shape.setAnchor(new java.awt.Rectangle(100, 100, 200, 200));
        fill = shape.getFill();
        fill.setFillType(HSLFFill.FILL_PICTURE);
        data = ppt1.addPicture(_slTests.readFile("clock.jpg"), PictureType.JPEG);
        fill.setPictureData(data);
        slide.addShape(shape);

        // slide 4
        slide = ppt1.createSlide();
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
        HSLFSlideShow ppt2 = HSLFTestDataSamples.writeOutAndReadBack(ppt1);
        List<HSLFSlide> slides = ppt2.getSlides();

        fill = slides.get(0).getBackground().getFill();
        assertEquals(HSLFFill.FILL_PICTURE, fill.getFillType());
        assertEquals(3, getFillPictureRefCount(slides.get(0).getBackground(), fill));
        shape = slides.get(0).getShapes().get(0);
        assertEquals(HSLFFill.FILL_SOLID, shape.getFill().getFillType());

        fill = slides.get(1).getBackground().getFill();
        assertEquals(HSLFFill.FILL_PATTERN, fill.getFillType());
        shape = slides.get(1).getShapes().get(0);
        assertEquals(HSLFFill.FILL_BACKGROUND, shape.getFill().getFillType());

        fill = slides.get(2).getBackground().getFill();
        assertEquals(HSLFFill.FILL_TEXTURE, fill.getFillType());
        assertEquals(3, getFillPictureRefCount(slides.get(2).getBackground(), fill));
        shape = slides.get(2).getShapes().get(0);
        assertEquals(HSLFFill.FILL_PICTURE, shape.getFill().getFillType());
        assertEquals(1, getFillPictureRefCount(shape, fill));

        fill = slides.get(3).getBackground().getFill();
        assertEquals(HSLFFill.FILL_SHADE_CENTER, fill.getFillType());
        shape = slides.get(3).getShapes().get(0);
        assertEquals(HSLFFill.FILL_SHADE, shape.getFill().getFillType());
        ppt2.close();
        ppt1.close();
    }

    private int getFillPictureRefCount(HSLFShape shape, HSLFFill fill) {
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
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
