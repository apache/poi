/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ==================================================================== 
 */

package org.apache.poi.xslf.usermodel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.sl.usermodel.PictureData.PictureType;

/**
 * Images
 */
public class Tutorial5 {

    public static void main(String[] args) throws IOException{
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            XSLFSlide slide = ppt.createSlide();

            File img = new File(System.getProperty("POI.testdata.path", "test-data"), "slideshow/clock.jpg");
            XSLFPictureData pictureData = ppt.addPicture(img, PictureType.PNG);

            /*XSLFPictureShape shape =*/
            slide.createPicture(pictureData);

            try (FileOutputStream out = new FileOutputStream("images.pptx")) {
                ppt.write(out);
            }
        }
    }
}
