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

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.io.FileOutputStream;

/**
 * Simple demo that creates a pptx slide show using the XSLF API
 *
 * @author Yegor Kozlov
 */
public class SlidesAndShapes {

    public static void main(String[] args) throws Exception {
        XMLSlideShow ppt = new XMLSlideShow();
        ppt.setPageSize(new Dimension(792, 612));

        XSLFSlide slide1 = ppt.createSlide();
        XSLFTextBox textBox = slide1.createTextBox();
        XSLFTextRun r1 = textBox.addNewTextParagraph().addNewTextRun();
        r1.setBold(true);
        r1.setItalic(true);
        r1.setFontColor(Color.yellow);
        r1.setFontFamily("Arial");
        r1.setFontSize(24);
        r1.setText("Apache");
        XSLFTextRun r2 = textBox.addNewTextParagraph().addNewTextRun();
        r2.setStrikethrough(true);
        r2.setUnderline(true);
        r2.setText("POI\u2122");
        XSLFTextRun r3 = textBox.addNewTextParagraph().addNewTextRun();
        r3.setFontFamily("Wingdings");
        r3.setText(" Version 3.8");

        textBox.setAnchor(new Rectangle(50, 50, 200, 100));
        textBox.setLineColor(Color.black);
        textBox.setFillColor(Color.orange);

        XSLFAutoShape shape2 = slide1.createAutoShape();

        shape2.setAnchor(new Rectangle(100, 100, 200, 200));

        XSLFFreeformShape shape3 = slide1.createFreeform();
        Rectangle rect = new Rectangle(150, 150, 300, 300);
        GeneralPath path = new GeneralPath(rect);
        path.append(new Ellipse2D.Double(200, 200, 100, 50), false);
        shape3.setPath(path);
        shape3.setAnchor(path.getBounds2D());
        shape3.setLineColor(Color.black);
        shape3.setFillColor(Color.lightGray);

        XSLFSlide slide2 = ppt.createSlide();
        XSLFGroupShape group = slide2.createGroup();

        group.setAnchor(new Rectangle(0, 0, 792, 612));
        group.setInteriorAnchor(new Rectangle(-10, -10, 20, 20));

        XSLFAutoShape shape4 = group.createAutoShape();
        shape4.setAnchor(new Rectangle(0, 0, 5, 5));
        shape4.setLineWidth(5);
        shape4.setLineColor(Color.black);


        FileOutputStream out = new FileOutputStream("xslf-demo.pptx");
        ppt.write(out);
        out.close();
    }

}
