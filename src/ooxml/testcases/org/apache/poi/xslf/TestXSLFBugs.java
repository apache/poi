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
package org.apache.poi.xslf;

import junit.framework.TestCase;

import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

public class TestXSLFBugs extends TestCase {

    public void test51187() throws Exception {
       XMLSlideShow ss = XSLFTestDataSamples.openSampleDocument("51187.pptx");
       
       assertEquals(1, ss.getSlides().length);
       XSLFSlide slide = ss.getSlides()[0];
       
       // Check the relations on it
       // Note - rId3 is a self reference
       PackagePart slidePart = ss._getXSLFSlideShow().getSlidePart(
             ss._getXSLFSlideShow().getSlideReferences().getSldIdArray(0)
       );
       assertEquals("/ppt/slides/slide1.xml", slidePart.getPartName().toString());
       assertEquals("/ppt/slideLayouts/slideLayout12.xml", slidePart.getRelationship("rId1").getTargetURI().toString());
       assertEquals("/ppt/notesSlides/notesSlide1.xml", slidePart.getRelationship("rId2").getTargetURI().toString());
       assertEquals("/ppt/slides/slide1.xml", slidePart.getRelationship("rId3").getTargetURI().toString());
       assertEquals("/ppt/media/image1.png", slidePart.getRelationship("rId4").getTargetURI().toString());
       
       // Save and re-load
       ss = XSLFTestDataSamples.writeOutAndReadBack(ss);
       assertEquals(1, ss.getSlides().length);
       
       slidePart = ss._getXSLFSlideShow().getSlidePart(
             ss._getXSLFSlideShow().getSlideReferences().getSldIdArray(0)
       );
       assertEquals("/ppt/slides/slide1.xml", slidePart.getPartName().toString());
       assertEquals("/ppt/slideLayouts/slideLayout12.xml", slidePart.getRelationship("rId1").getTargetURI().toString());
       assertEquals("/ppt/notesSlides/notesSlide1.xml", slidePart.getRelationship("rId2").getTargetURI().toString());
       // TODO Fix this
//       assertEquals("/ppt/slides/slide1.xml", slidePart.getRelationship("rId3").getTargetURI().toString());
       assertEquals("/ppt/media/image1.png", slidePart.getRelationship("rId4").getTargetURI().toString());
    }
}
