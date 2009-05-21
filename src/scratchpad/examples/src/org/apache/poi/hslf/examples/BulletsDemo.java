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

import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.TextBox;

import java.io.FileOutputStream;

/**
 * How to create a single-level bulleted list
 * and change some of the bullet attributes
 *
 * @author Yegor Kozlov
 */
public final class BulletsDemo {

    public static void main(String[] args) throws Exception {

        SlideShow ppt = new SlideShow();

        Slide slide = ppt.createSlide();

        TextBox shape = new TextBox();
        RichTextRun rt = shape.getTextRun().getRichTextRuns()[0];
        shape.setText(
                "January\r" +
                "February\r" +
                "March\r" +
                "April");
        rt.setFontSize(42);
        rt.setBullet(true);
        rt.setBulletOffset(0);  //bullet offset
        rt.setTextOffset(50);   //text offset (should be greater than bullet offset)
        rt.setBulletChar('\u263A'); //bullet character
        slide.addShape(shape);

        shape.setAnchor(new java.awt.Rectangle(50, 50, 500, 300));  //position of the text box in the slide
        slide.addShape(shape);

        FileOutputStream out = new FileOutputStream("bullets.ppt");
        ppt.write(out);
        out.close();
   }
}
