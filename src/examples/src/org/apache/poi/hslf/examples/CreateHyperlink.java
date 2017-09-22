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

package org.apache.poi.hslf.examples;

import java.awt.Rectangle;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hslf.usermodel.HSLFHyperlink;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextBox;

/**
 * Demonstrates how to create hyperlinks in PowerPoint presentations
 */
public abstract class CreateHyperlink {
    
    public static void main(String[] args) throws IOException {
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFSlide slideA = ppt.createSlide();
            ppt.createSlide();
            HSLFSlide slideC = ppt.createSlide();

            // link to a URL
            HSLFTextBox textBox1 = slideA.createTextBox();
            textBox1.setText("Apache POI");
            textBox1.setAnchor(new Rectangle(100, 100, 200, 50));

            HSLFHyperlink link1 = textBox1.getTextParagraphs().get(0).getTextRuns().get(0).createHyperlink();
            link1.linkToUrl("http://www.apache.org");
            link1.setLabel(textBox1.getText());

            // link to another slide
            HSLFTextBox textBox2 = slideA.createTextBox();
            textBox2.setText("Go to slide #3");
            textBox2.setAnchor(new Rectangle(100, 300, 200, 50));

            HSLFHyperlink link2 = textBox2.getTextParagraphs().get(0).getTextRuns().get(0).createHyperlink();
            link2.linkToSlide(slideC);

            try (FileOutputStream out = new FileOutputStream("hyperlink.ppt")) {
                ppt.write(out);
            }
        }
   }
}
