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
package org.apache.poi.xslf.usermodel;

import junit.framework.TestCase;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.*;
import java.util.List;

import org.apache.poi.xslf.XSLFTestDataSamples;

/**
 * @author Yegor Kozlov
 */
public class TestXSLFPictureShape extends TestCase {

    public void testCreate() {
        XMLSlideShow ppt = new XMLSlideShow();
        assertEquals(0, ppt.getAllPictures().size());
        byte[] data1 = new byte[100];
        int idx1 = ppt.addPicture(data1, XSLFPictureData.PICTURE_TYPE_JPEG);
        assertEquals(0, idx1);
        assertEquals(1, ppt.getAllPictures().size());

        XSLFSlide slide = ppt.createSlide();
        XSLFPictureShape shape1 = slide.createPicture(idx1);
        assertNotNull(shape1.getPictureData());
        assertTrue(Arrays.equals(data1, shape1.getPictureData().getData()));

        byte[] data2 = new byte[200];
        int idx2 = ppt.addPicture(data2, XSLFPictureData.PICTURE_TYPE_PNG);
        XSLFPictureShape shape2 = slide.createPicture(idx2);
        assertNotNull(shape2.getPictureData());
        assertEquals(1, idx2);
        assertEquals(2, ppt.getAllPictures().size());
        assertTrue(Arrays.equals(data2, shape2.getPictureData().getData()));

        ppt = XSLFTestDataSamples.writeOutAndReadBack(ppt);
        List<XSLFPictureData> pics =  ppt.getAllPictures();
        assertEquals(2, pics.size());
        assertTrue(Arrays.equals(data1, pics.get(0).getData()));
        assertTrue(Arrays.equals(data2, pics.get(1).getData()));

        XSLFShape[] shapes = ppt.getSlides()[0].getShapes();
        assertTrue(Arrays.equals(data1, ((XSLFPictureShape)shapes[0]).getPictureData().getData()));
        assertTrue(Arrays.equals(data2, ((XSLFPictureShape)shapes[1]).getPictureData().getData()));
    }
}