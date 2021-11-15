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

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.apache.poi.xslf.XSLFTestDataSamples.openSampleDocument;

class TestXSLFGraphicFrame {

    @Test
    void testHasDiagram() throws IOException {
        try (XMLSlideShow ppt = openSampleDocument("SmartArt.pptx")) {
            XSLFSlide slide = ppt.getSlides().get(0);
            XSLFGraphicFrame gf = (XSLFGraphicFrame) slide.getShapes().get(0);

            assertTrue(gf.hasDiagram());
        }
    }
}