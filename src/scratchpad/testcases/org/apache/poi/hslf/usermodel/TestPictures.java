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
import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * Test extracting images from a ppt file
 *
 * @author Yegor Kozlov
 */
public class TestPictures extends TestCase{

    public void testPictures() throws Exception {
        String dirname = System.getProperty("HSLF.testdata.path");
        String filename = dirname + "/ppt_with_png.ppt";

        HSLFSlideShow ppt = new HSLFSlideShow(filename);
        Picture[] pict = ppt.getPictures();
        assertNotNull(pict);
        for (int i = 0; i < pict.length; i++) {
            byte[] data = pict[i].getData();
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
            assertNotNull(img);
        }
        ppt.close();
    }
}
