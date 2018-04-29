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
import static org.junit.Assert.assertSame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.BitSet;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.hssf.usermodel.DummyGraphics2d;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test Picture shape.
 *
 * @author Yegor Kozlov
 */
public final class TestPicture {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    @BeforeClass
    public static void disableImageIOCache() {
        ImageIO.setUseCache(false);
    }

    /**
     * Test that the reference count of a blip is incremented every time the picture is inserted.
     * This is important when the same image appears multiple times in a slide show.
     *
     */
    @Test
    public void multiplePictures() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow();

        HSLFSlide s = ppt.createSlide();
        HSLFSlide s2 = ppt.createSlide();
        HSLFSlide s3 = ppt.createSlide();

        HSLFPictureData data = ppt.addPicture(_slTests.readFile("clock.jpg"), PictureType.JPEG);
        HSLFPictureShape pict = new HSLFPictureShape(data);
        HSLFPictureShape pict2 = new HSLFPictureShape(data);
        HSLFPictureShape pict3 = new HSLFPictureShape(data);

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
        
        ppt.close();
    }

    /**
     * Picture#getEscherBSERecord threw NullPointerException if EscherContainerRecord.BSTORE_CONTAINER
     * was not found. The correct behaviour is to return null.
     */
    @Test
    public void bug46122() throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow();
        HSLFSlide slide = ppt.createSlide();
        HSLFPictureData pd = HSLFPictureData.create(PictureType.PNG);
        
        HSLFPictureShape pict = new HSLFPictureShape(pd); //index to non-existing picture data
        pict.setAnchor(new Rectangle2D.Double(50,50,100,100));
        pict.setSheet(slide);
        HSLFPictureData data = pict.getPictureData();
        assertNull(data);

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        pict.draw(graphics, null);
        
        ppt.close();
    }

    @Test
    public void macImages() throws IOException {
        HSLFSlideShowImpl hss = new HSLFSlideShowImpl(_slTests.openResourceAsStream("53446.ppt"));

        List<HSLFPictureData> pictures = hss.getPictureData();
        assertEquals(15, pictures.size());

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

        int i = 0;
        for (HSLFPictureData pd : pictures) {
            int[] dimensions = expectedSizes[i++];
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(pd.getData()));
            switch (pd.getType()) {
                case WMF:
                case EMF:
                    break;
                default:
                    assertNotNull(image);
                    assertEquals(dimensions[0], image.getWidth());
                    assertEquals(dimensions[1], image.getHeight());
                    break;
            }
        }
        
        hss.close();
    }

    @Test
    @Ignore("Just for visual validation - antialiasing is different on various systems")
    public void bug54541()
    throws IOException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        String files[] = {
//            "sample_pptx_grouping_issues.pptx",
//            "54542_cropped_bitmap.pptx",
//            "54541_cropped_bitmap.ppt",
//            "54541_cropped_bitmap2.ppt",
            "alterman_security.ppt",
//            "alterman_security3.pptx",
        };
        
        BitSet pages = new BitSet();
        pages.set(2);
        
        for (String file : files) {
            InputStream is = _slTests.openResourceAsStream(file);
            SlideShow<?,?> ss;
            if (file.endsWith("pptx")) {
                Class<?> cls = Class.forName("org.apache.poi.xslf.usermodel.XMLSlideShow");
                Constructor<?> ct = cls.getDeclaredConstructor(InputStream.class);
                ss = (SlideShow<?,?>)ct.newInstance(is);
            } else {
                ss = new HSLFSlideShow(is);
            }
            is.close();
            
            boolean debugOut = false;
            Dimension pg = ss.getPageSize();
            for (Slide<?,?> slide : ss.getSlides()) {
                int slideNo = slide.getSlideNumber();
                if (!pages.get(slideNo-1)) {
                    if (pages.nextSetBit(slideNo-1) == -1) break; else continue;
                }
                if (debugOut) {
                    DummyGraphics2d graphics = new DummyGraphics2d();
                    slide.draw(graphics);
                } else {
                    BufferedImage img = new BufferedImage(pg.width, pg.height, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D graphics = img.createGraphics();
                    DrawFactory.getInstance(graphics).fixFonts(graphics);
                    slide.draw(graphics);
                    graphics.setColor(Color.BLACK);
                    graphics.setStroke(new BasicStroke(1));
                    graphics.drawRect(0, 0, (int)pg.getWidth()-1, (int)pg.getHeight()-1);
                    ImageIO.write(img, "PNG", new File(file.replaceFirst(".pptx?", "-")+slideNo+".png"));
                }
            }
            
            ss.close();
        }
    }
}
