/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hslf.usermodel;

import org.apache.poi.hslf.*;
import org.apache.poi.hslf.usermodel.PictureData;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.Shape;
import org.apache.poi.hslf.model.Picture;
import org.apache.poi.util.LittleEndian;
import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Test extracting images from a ppt file
 *
 * @author Yegor Kozlov
 */
public class TestPictures extends TestCase{
    public static String dirname = System.getProperty("HSLF.testdata.path");
    public static String filename = dirname + "/ppt_with_png.ppt";

    public void testReadPictures() throws Exception {

        HSLFSlideShow ppt = new HSLFSlideShow(filename);
        PictureData[] pict = ppt.getPictures();
        assertNotNull(pict);
        for (int i = 0; i < pict.length; i++) {
            byte[] data = pict[i].getData();

            BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
            assertNotNull(img);
        }
        ppt.close();
    }

    public void testSerializePictures() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow(filename);
        PictureData[] pict = ppt.getPictures();
        assertNotNull(pict);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray()));
        pict = ppt.getPictures();
        assertNotNull(pict);
    }

    public void testAddPictures() throws Exception {
        int idx;
        Slide slide;
        Picture pict;

        SlideShow ppt = new SlideShow();

        idx = ppt.addPicture(new File(dirname + "/clock.jpg"), Picture.JPEG);
        slide = ppt.createSlide();
        pict = new Picture(idx);
        pict.setDefaultSize(ppt);
        slide.addShape(pict);

        idx = ppt.addPicture(new File(dirname + "/painting.png"), Picture.PNG);
        pict = new Picture(idx);
        pict.setDefaultSize(ppt);
        slide.addShape(pict);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new SlideShow(new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray())));
        assertTrue(ppt.getPictures().length == 2 );
    }

}
