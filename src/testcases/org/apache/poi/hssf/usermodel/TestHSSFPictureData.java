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


/*
 * HSSFWorkbook.java
 *
 * Created on September 30, 2001, 3:37 PM
 */
package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;

/**
 * Test <code>HSSFPictureData</code>.
 * The code to retrieve images from a workbook provided by Trejkaz (trejkaz at trypticon dot org) in Bug 41223.
 *
 * @author Yegor Kozlov (yegor at apache dot org)
 * @author Trejkaz (trejkaz at trypticon dot org)
 */
public class TestHSSFPictureData extends TestCase{

    static String cwd = System.getProperty("HSSF.testdata.path");

    public void testPictures() throws IOException {
        FileInputStream is = new FileInputStream(new File(cwd, "SimpleWithImages.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(is);
        is.close();

        List lst = wb.getAllPictures();
        assertEquals(2, lst.size());

        for (Iterator it = lst.iterator(); it.hasNext(); ) {
            HSSFPictureData pict = (HSSFPictureData)it.next();
            String ext = pict.suggestFileExtension();
            byte[] data = pict.getData();
            if (ext.equals("jpeg")){
                //try to read image data using javax.imageio.* (JDK 1.4+)
                BufferedImage jpg = ImageIO.read(new ByteArrayInputStream(data));
                assertNotNull(jpg);
                assertEquals(192, jpg.getWidth());
                assertEquals(176, jpg.getHeight());
            } else if (ext.equals("png")){
                //try to read image data using javax.imageio.* (JDK 1.4+)
                BufferedImage png = ImageIO.read(new ByteArrayInputStream(data));
                assertNotNull(png);
                assertEquals(300, png.getWidth());
                assertEquals(300, png.getHeight());

            } else {
                fail("unexpected picture type: " + ext);
            }
        }

    }
}
