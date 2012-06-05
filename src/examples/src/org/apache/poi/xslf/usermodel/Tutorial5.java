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

import org.apache.poi.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Images
 *
 * @author Yegor Kozlov
 */
public class Tutorial5 {

    public static void main(String[] args) throws IOException{
        XMLSlideShow ppt = new XMLSlideShow();

        XSLFSlide slide = ppt.createSlide();
        File img = new File(System.getProperty("POI.testdata.path"), "slideshow/clock.jpg");
        byte[] data = IOUtils.toByteArray(new FileInputStream(img));
        int pictureIndex = ppt.addPicture(data, XSLFPictureData.PICTURE_TYPE_PNG);

        XSLFPictureShape shape = slide.createPicture(pictureIndex);

        FileOutputStream out = new FileOutputStream("images.pptx");
        ppt.write(out);
        out.close();
    }
}
