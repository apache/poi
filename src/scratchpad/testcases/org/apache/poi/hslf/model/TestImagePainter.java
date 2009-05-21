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

import junit.framework.*;

import java.io.FileOutputStream;
import java.io.File;
import java.awt.*;

import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.hslf.usermodel.PictureData;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.blip.ImagePainter;
import org.apache.poi.hslf.blip.BitmapPainter;
import org.apache.poi.ddf.EscherBSERecord;

/**
 * Test Picture shape.
 *
 * @author Yegor Kozlov
 */
public final class TestImagePainter extends TestCase {

    private static class CustomImagePainer implements ImagePainter{
        public void paint(Graphics2D graphics, PictureData pict, Picture parent){
            //do noting
        }

    }

    public void testImagePainter() throws Exception {

        ImagePainter pntr = PictureData.getImagePainter(Picture.PNG);
        assertTrue(PictureData.getImagePainter(Picture.PNG) instanceof BitmapPainter);
        assertTrue(PictureData.getImagePainter(Picture.JPEG) instanceof BitmapPainter);
        assertTrue(PictureData.getImagePainter(Picture.DIB) instanceof BitmapPainter);

        PictureData.setImagePainter(Picture.WMF, new CustomImagePainer());
        assertTrue(PictureData.getImagePainter(Picture.WMF) instanceof CustomImagePainer);
    }

}
