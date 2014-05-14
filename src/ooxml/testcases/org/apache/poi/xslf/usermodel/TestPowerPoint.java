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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestPowerPoint extends TestCase {
    public void testCreateShapes(){
        XMLSlideShow ppt = new XMLSlideShow();

        XSLFSlideMaster[] slideMasters = ppt.getSlideMasters();
        XSLFSlideMaster slideMaster = slideMasters[0];

        XSLFSlideLayout layout = slideMaster.getLayout(SlideLayout.TITLE);

        XSLFSlide slide = ppt.createSlide(layout);
        XSLFShape[] shapes = slide.getShapes();
        assertEquals(2, shapes.length);

        ppt = XSLFTestDataSamples.writeOutAndReadBack(ppt);

        File file = new File("text.pptx");
        //System.out.println(file.getAbsolutePath());
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            ppt.write(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}