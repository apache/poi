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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import junit.framework.TestCase;

import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.hslf.usermodel.PictureData;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.POIDataSamples;

/**
 * Test Picture shape.
 *
 * @author Yegor Kozlov
 */
public final class TestPicture extends TestCase {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    /**
     * Test that the reference count of a blip is incremented every time the picture is inserted.
     * This is important when the same image appears multiple times in a slide show.
     *
     */
    public void testMultiplePictures() throws Exception {
        SlideShow ppt = new SlideShow();

        Slide s = ppt.createSlide();
        Slide s2 = ppt.createSlide();
        Slide s3 = ppt.createSlide();

        int idx = ppt.addPicture(_slTests.readFile("clock.jpg"), Picture.JPEG);
        Picture pict = new Picture(idx);
        Picture pict2 = new Picture(idx);
        Picture pict3 = new Picture(idx);

        pict.setAnchor(new Rectangle(10,10,100,100));
        s.addShape(pict);
        EscherBSERecord bse1 = pict.getEscherBSERecord();
        assertEquals(1, bse1.getRef());

        pict2.setAnchor(new Rectangle(10,10,100,100));
        s2.addShape(pict2);
        EscherBSERecord bse2 = pict.getEscherBSERecord();
        assertSame(bse1, bse2);
        assertEquals(2, bse1.getRef());

        pict3.setAnchor(new Rectangle(10,10,100,100));
        s3.addShape(pict3);
        EscherBSERecord bse3 = pict.getEscherBSERecord();
        assertSame(bse2, bse3);
        assertEquals(3, bse1.getRef());
    }

    /**
     * Picture#getEscherBSERecord threw NullPointerException if EscherContainerRecord.BSTORE_CONTAINER
     * was not found. The correct behaviour is to return null.
     */
    public void test46122() {
        SlideShow ppt = new SlideShow();
        Slide slide = ppt.createSlide();

        Picture pict = new Picture(-1); //index to non-existing picture data
        pict.setSheet(slide);
        PictureData data = pict.getPictureData();
        assertNull(data);

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        pict.draw(graphics);
    }
}
