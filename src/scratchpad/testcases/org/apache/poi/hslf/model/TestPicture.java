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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.PictureData;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.util.JvmBugs;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test Picture shape.
 *
 * @author Yegor Kozlov
 */
public final class TestPicture {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    /**
     * Test that the reference count of a blip is incremented every time the picture is inserted.
     * This is important when the same image appears multiple times in a slide show.
     *
     */
    @Test
    public void multiplePictures() throws Exception {
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
    @Test
    public void bug46122() {
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

    @Test
    public void macImages() throws Exception {
        HSLFSlideShow hss = new HSLFSlideShow(_slTests.openResourceAsStream("53446.ppt"));

        PictureData[] pictures = hss.getPictures();
        assertEquals(15, pictures.length);

        int[][] expectedSizes = {
                null,           // WMF
                { 427, 428 },   // PNG
                { 371, 370 },   // PNG
                { 288, 183 },   // PNG
                { 285, 97 },    // PNG
                { 288, 168 },   // PNG
                null,           // WMF
                null,           // WMF
                { 199, 259 },   // PNG
                { 432, 244 },   // PNG
                { 261, 258 },   // PNG
                null,           // WMF
                null,           // WMF
                null,           // WMF
                null            // EMF
        };

        for (int i = 0; i < pictures.length; i++) {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(pictures[i].getData()));

            if (pictures[i].getType() != Picture.WMF && pictures[i].getType() != Picture.EMF) {
                assertNotNull(image);

                int[] dimensions = expectedSizes[i];
                assertEquals(dimensions[0], image.getWidth());
                assertEquals(dimensions[1], image.getHeight());
            }
        }
    }

    @Test
    @Ignore("Just for visual validation - antialiasing is different on various systems")
    public void bug54541() throws Exception {
//        InputStream xis = _slTests.openResourceAsStream("54542_cropped_bitmap.pptx");
//        XMLSlideShow xss = new XMLSlideShow(xis);
//        xis.close();
//        
//        Dimension xpg = xss.getPageSize();
//        for(XSLFSlide slide : xss.getSlides()) {
//            BufferedImage img = new BufferedImage(xpg.width, xpg.height, BufferedImage.TYPE_INT_RGB);
//            Graphics2D graphics = img.createGraphics();
//            fixFonts(graphics);
//            slide.draw(graphics);
//            ImageIO.write(img, "PNG", new File("testx.png"));
//        }
//
//        System.out.println("########################");
        
        InputStream is = _slTests.openResourceAsStream("54541_cropped_bitmap.ppt");
        SlideShow ss = new SlideShow(is);
        is.close();
        
        Dimension pg = ss.getPageSize();
        int i=1;
        for(Slide slide : ss.getSlides()) {
            BufferedImage img = new BufferedImage(pg.width, pg.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = img.createGraphics();
            fixFonts(graphics);
            slide.draw(graphics);
            ImageIO.write(img, "PNG", new File("test"+(i++)+".png"));
        }
    }
    
    @SuppressWarnings("unchecked")
    private void fixFonts(Graphics2D graphics) {
        if (!JvmBugs.hasLineBreakMeasurerBug()) return;
        Map<String,String> fontMap = (Map<String,String>)graphics.getRenderingHint(TextPainter.KEY_FONTMAP);
        if (fontMap == null) fontMap = new HashMap<String,String>();
        fontMap.put("Calibri", "Lucida Sans");
        fontMap.put("Cambria", "Lucida Bright");
        graphics.setRenderingHint(TextPainter.KEY_FONTMAP, fontMap);        
    }
}
