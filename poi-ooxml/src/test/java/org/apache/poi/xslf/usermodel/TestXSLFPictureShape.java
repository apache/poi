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

import static org.junit.Assert.assertArrayEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.poi.xslf.XSLFTestDataSamples;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPicture;

/**
 * @author Yegor Kozlov
 */
public class TestXSLFPictureShape extends TestCase {

    public void testCreate() {
        XMLSlideShow ppt = new XMLSlideShow();
        assertEquals(0, ppt.getAllPictures().size());
        byte[] data1 = new byte[100];
        for(int i = 0;i < 100;i++) { data1[i] = (byte)i; }
        int idx1 = ppt.addPicture(data1, XSLFPictureData.PICTURE_TYPE_JPEG);
        assertEquals(0, idx1);
        assertEquals(1, ppt.getAllPictures().size());

        XSLFSlide slide = ppt.createSlide();
        XSLFPictureShape shape1 = slide.createPicture(idx1);
        assertNotNull(shape1.getPictureData());
        assertArrayEquals(data1, shape1.getPictureData().getData());

        byte[] data2 = new byte[200];
        for(int i = 0;i < 200;i++) { data2[i] = (byte)i; }
        int idx2 = ppt.addPicture(data2, XSLFPictureData.PICTURE_TYPE_PNG);
        XSLFPictureShape shape2 = slide.createPicture(idx2);
        assertNotNull(shape2.getPictureData());
        assertEquals(1, idx2);
        assertEquals(2, ppt.getAllPictures().size());
        assertArrayEquals(data2, shape2.getPictureData().getData());

        ppt = XSLFTestDataSamples.writeOutAndReadBack(ppt);
        List<XSLFPictureData> pics = ppt.getAllPictures();
        assertEquals(2, pics.size());
        assertArrayEquals(data1, pics.get(0).getData());
        assertArrayEquals(data2, pics.get(1).getData());

        XSLFShape[] shapes = ppt.getSlides()[0].getShapes();
        assertArrayEquals(data1, ((XSLFPictureShape) shapes[0]).getPictureData().getData());
        assertArrayEquals(data2, ((XSLFPictureShape) shapes[1]).getPictureData().getData());
    }

    public void testCreateMultiplePictures() {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide1 = ppt.createSlide();
        XSLFGroupShape group1 = slide1.createGroup();


        int pictureIndex = 0;
        // first add 20 images to the slide
        for (int i = 0; i < 20; i++, pictureIndex++) {
            byte[] data = new byte[]{(byte)pictureIndex};
            int elementIndex = ppt.addPicture(data,
                    XSLFPictureData.PICTURE_TYPE_PNG);
            assertEquals(pictureIndex, elementIndex);   // added images have indexes 0,1,2....19
            XSLFPictureShape picture = slide1.createPicture(elementIndex);
            // POI saves images as image1.png, image2.png, etc.
            String fileName = "image" + (elementIndex + 1) + ".png";
            assertEquals(fileName, picture.getPictureData().getFileName());
            assertArrayEquals(data, picture.getPictureData().getData());
        }

        // and then add next 20 images to a group
        for (int i = 0; i < 20; i++, pictureIndex++) {
            byte[] data = new byte[]{(byte)pictureIndex};
            int elementIndex = ppt.addPicture(data,
                    XSLFPictureData.PICTURE_TYPE_PNG);
            XSLFPictureShape picture = group1.createPicture(elementIndex);
            // POI saves images as image1.png, image2.png, etc.
            assertEquals(pictureIndex, elementIndex);   // added images have indexes 0,1,2....19
            String fileName = "image" + (pictureIndex + 1) + ".png";
            assertEquals(fileName, picture.getPictureData().getFileName());
            assertArrayEquals(data, picture.getPictureData().getData());
        }

        // serialize, read back and check that all images are there

        ppt = XSLFTestDataSamples.writeOutAndReadBack(ppt);
        // pictures keyed by file name
        Map<String, XSLFPictureData> pics = new HashMap<String, XSLFPictureData>();
        for(XSLFPictureData p : ppt.getAllPictures()){
            pics.put(p.getFileName(), p);
        }
        assertEquals(40, pics.size());
        for (int i = 0; i < 40; i++) {
            byte[] data1 = new byte[]{(byte)i};
            String fileName = "image" + (i + 1) + ".png";
            XSLFPictureData data = pics.get(fileName);
            assertNotNull(data);
            assertEquals(fileName, data.getFileName());
            assertArrayEquals(data1, data.getData());
        }
    }

    public void testImageCaching() {
        XMLSlideShow ppt = new XMLSlideShow();
        byte[] img1 = new byte[]{1,2,3};
        byte[] img2 = new byte[]{3,4,5};
        int idx1 = ppt.addPicture(img1, XSLFPictureData.PICTURE_TYPE_PNG);
        assertEquals(0, idx1);
        assertEquals(0, ppt.addPicture(img1, XSLFPictureData.PICTURE_TYPE_PNG));

        int idx2 = ppt.addPicture(img2, XSLFPictureData.PICTURE_TYPE_PNG);
        assertEquals(1, idx2);
        assertEquals(1, ppt.addPicture(img2, XSLFPictureData.PICTURE_TYPE_PNG));

        XSLFSlide slide1 = ppt.createSlide();
        assertNotNull(slide1);
        XSLFSlide slide2 = ppt.createSlide();
        assertNotNull(slide2);

    }

    public void testMerge() {
        XMLSlideShow ppt1 = new XMLSlideShow();
        byte[] data1 = new byte[100];
        int idx1 = ppt1.addPicture(data1, XSLFPictureData.PICTURE_TYPE_JPEG);

        XSLFSlide slide1 = ppt1.createSlide();
        XSLFPictureShape shape1 = slide1.createPicture(idx1);
        CTPicture ctPic1 = (CTPicture)shape1.getXmlObject();
        ctPic1.getNvPicPr().getNvPr().addNewCustDataLst().addNewTags().setId("rId99");

        XMLSlideShow ppt2 = new XMLSlideShow();

        XSLFSlide slide2 = ppt2.createSlide().importContent(slide1);
        XSLFPictureShape shape2 = (XSLFPictureShape)slide2.getShapes()[0];

        assertArrayEquals(data1, shape2.getPictureData().getData());

        CTPicture ctPic2 = (CTPicture)shape2.getXmlObject();
        assertFalse(ctPic2.getNvPicPr().getNvPr().isSetCustDataLst());

    }
}