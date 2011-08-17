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

import org.apache.poi.xslf.XSLFTestDataSamples;
import org.apache.poi.POIXMLDocumentPart;

import java.util.List;

/**
 * @author Yegor Kozlov
 */
public class TestXSLFSlide extends TestCase {
    public void testReadShapes(){
        XMLSlideShow  ppt = XSLFTestDataSamples.openSampleDocument("shapes.pptx");
        XSLFSlide[] slides = ppt.getSlides();

        XSLFSlide slide1 = slides[0];
        XSLFShape[] shapes1 = slide1.getShapes();
        assertEquals(7, shapes1.length);
        assertEquals("TextBox 3", shapes1[0].getShapeName());
        assertTrue(shapes1[0] instanceof XSLFTextBox);
        XSLFAutoShape sh0 = (XSLFAutoShape)shapes1[0];
        assertEquals("Learning PPTX", sh0.getText());


        assertEquals("Straight Connector 5", shapes1[1].getShapeName());
        assertTrue(shapes1[1] instanceof XSLFConnectorShape);

        assertEquals("Freeform 6", shapes1[2].getShapeName());
        assertTrue(shapes1[2] instanceof XSLFFreeformShape);
        XSLFAutoShape sh2 = (XSLFAutoShape)shapes1[2];
        assertEquals("Cloud", sh2.getText());

        assertEquals("Picture 1", shapes1[3].getShapeName());
        assertTrue(shapes1[3] instanceof XSLFPictureShape);

        assertEquals("Table 2", shapes1[4].getShapeName());
        assertTrue(shapes1[4] instanceof XSLFGraphicFrame);

        assertEquals("Straight Arrow Connector 7", shapes1[5].getShapeName());
        assertTrue(shapes1[5] instanceof XSLFConnectorShape);

        assertEquals("Elbow Connector 9", shapes1[6].getShapeName());
        assertTrue(shapes1[6] instanceof XSLFConnectorShape);

        // titles on slide2
        XSLFSlide slide2 = slides[1];
        XSLFShape[] shapes2 = slide2.getShapes();
        assertEquals(2, shapes2.length);
        assertTrue(shapes2[0] instanceof XSLFAutoShape);
        assertEquals("PPTX Title", ((XSLFAutoShape)shapes2[0]).getText());
        assertTrue(shapes2[1] instanceof XSLFAutoShape);
        assertEquals("Subtitle\nAnd second line", ((XSLFAutoShape)shapes2[1]).getText());

        //  group shape on slide3
        XSLFSlide slide3 = slides[2];
        XSLFShape[] shapes3 = slide3.getShapes();
        assertEquals(1, shapes3.length);
        assertTrue(shapes3[0] instanceof XSLFGroupShape);
        XSLFShape[] groupShapes = ((XSLFGroupShape)shapes3[0]).getShapes();
        assertEquals(3, groupShapes.length);
        assertTrue(groupShapes[0] instanceof XSLFAutoShape);
        assertEquals("Rectangle 1", groupShapes[0].getShapeName());

        assertTrue(groupShapes[1] instanceof XSLFAutoShape);
        assertEquals("Oval 2", groupShapes[1].getShapeName());

        assertTrue(groupShapes[2] instanceof XSLFAutoShape);
        assertEquals("Right Arrow 3", groupShapes[2].getShapeName());

        XSLFSlide slide4 = slides[3];
        XSLFShape[] shapes4 = slide4.getShapes();
        assertEquals(1, shapes4.length);
        assertTrue(shapes4[0] instanceof XSLFTable);
        XSLFTable tbl = (XSLFTable)shapes4[0];
        assertEquals(3, tbl.getNumberOfColumns());
        assertEquals(6, tbl.getNumberOfRows());
    }

    public void testCreateSlide(){
        XMLSlideShow  ppt = new XMLSlideShow();
        assertEquals(0, ppt.getSlides().length);

        XSLFSlide slide = ppt.createSlide();
        assertTrue(slide.getFollowMasterBackground());
        slide.setFollowMasterBackground(false);
        assertFalse(slide.getFollowMasterBackground());
        slide.setFollowMasterBackground(true);
        assertTrue(slide.getFollowMasterBackground());
    }

}