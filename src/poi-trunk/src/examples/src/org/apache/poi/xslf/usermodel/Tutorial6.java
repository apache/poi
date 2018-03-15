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

import java.awt.Rectangle;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Hyperlinks
 */
public class Tutorial6 {

    public static void main(String[] args) throws IOException{
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            XSLFSlide slide1 = ppt.createSlide();
            XSLFSlide slide2 = ppt.createSlide();

            XSLFTextBox shape1 = slide1.createTextBox();
            shape1.setAnchor(new Rectangle(50, 50, 200, 50));
            XSLFTextRun r1 = shape1.addNewTextParagraph().addNewTextRun();
            XSLFHyperlink link1 = r1.createHyperlink();
            r1.setText("http://poi.apache.org"); // visible text
            link1.setAddress("http://poi.apache.org");  // link address

            XSLFTextBox shape2 = slide1.createTextBox();
            shape2.setAnchor(new Rectangle(300, 50, 200, 50));
            XSLFTextRun r2 = shape2.addNewTextParagraph().addNewTextRun();
            XSLFHyperlink link2 = r2.createHyperlink();
            r2.setText("Go to the second slide"); // visible text
            link2.linkToSlide(slide2);  // link address


            try (FileOutputStream out = new FileOutputStream("hyperlinks.pptx")) {
                ppt.write(out);
            }
        }
    }
}
