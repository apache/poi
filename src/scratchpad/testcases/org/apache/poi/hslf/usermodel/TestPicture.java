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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test Picture shape.
 */
public final class TestPicture {
    private static final POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    @BeforeAll
    public static void disableImageIOCache() {
        ImageIO.setUseCache(false);
    }

    /**
     * Test that the reference count of a blip is incremented every time the picture is inserted.
     * This is important when the same image appears multiple times in a slide show.
     *
     */
    @Test
    void multiplePictures() throws IOException {
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFSlide s = ppt.createSlide();
            HSLFSlide s2 = ppt.createSlide();
            HSLFSlide s3 = ppt.createSlide();

            HSLFPictureData data = ppt.addPicture(_slTests.readFile("clock.jpg"), PictureType.JPEG);
            HSLFPictureShape pict = new HSLFPictureShape(data);
            HSLFPictureShape pict2 = new HSLFPictureShape(data);
            HSLFPictureShape pict3 = new HSLFPictureShape(data);

            pict.setAnchor(new Rectangle(10, 10, 100, 100));
            s.addShape(pict);
            EscherBSERecord bse1 = pict.getEscherBSERecord();
            assertEquals(1, bse1.getRef());

            pict2.setAnchor(new Rectangle(10, 10, 100, 100));
            s2.addShape(pict2);
            EscherBSERecord bse2 = pict.getEscherBSERecord();
            assertSame(bse1, bse2);
            assertEquals(2, bse1.getRef());

            pict3.setAnchor(new Rectangle(10, 10, 100, 100));
            s3.addShape(pict3);
            EscherBSERecord bse3 = pict.getEscherBSERecord();
            assertSame(bse2, bse3);
            assertEquals(3, bse1.getRef());
        }
    }

    /**
     * Picture#getEscherBSERecord threw NullPointerException if EscherContainerRecord.BSTORE_CONTAINER
     * was not found. The correct behaviour is to return null.
     */
    @Test
    void bug46122() throws IOException {
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFSlide slide = ppt.createSlide();
            HSLFPictureData pd = HSLFPictureData.create(PictureType.PNG);

            HSLFPictureShape pict = new HSLFPictureShape(pd); //index to non-existing picture data
            pict.setAnchor(new Rectangle2D.Double(50, 50, 100, 100));
            pict.setSheet(slide);
            HSLFPictureData data = pict.getPictureData();
            assertNull(data);

            BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = img.createGraphics();
            pict.draw(graphics, null);
        }
    }

    @Test
    void macImages() throws IOException {
        try (InputStream is = _slTests.openResourceAsStream("53446.ppt");
             HSLFSlideShowImpl hss = new HSLFSlideShowImpl(is)) {

            List<HSLFPictureData> pictures = hss.getPictureData();
            assertEquals(15, pictures.size());

            int[][] expectedSizes = {
                    null,           // WMF
                    {427, 428},   // PNG
                    {371, 370},   // PNG
                    {288, 183},   // PNG
                    {285, 97},    // PNG
                    {288, 168},   // PNG
                    null,           // WMF
                    null,           // WMF
                    {199, 259},   // PNG
                    {432, 244},   // PNG
                    {261, 258},   // PNG
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
                        assertNotNull(dimensions);
                        assertEquals(dimensions[0], image.getWidth());
                        assertEquals(dimensions[1], image.getHeight());
                        break;
                }
            }
        }
    }
}
