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

import org.apache.poi.hslf.usermodel.*;
import org.apache.poi.hslf.model.*;
import org.apache.poi.hslf.record.TextHeaderAtom;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;
import java.awt.*;

/**
 * Presentation for Fast Feather Track on ApacheconEU 2008
 *
 * @author Yegor Kozlov
 */
public final class ApacheconEU08 {

    public static void main(String[] args) throws IOException {
        SlideShow ppt = new SlideShow();
        ppt.setPageSize(new Dimension(720, 540));

        slide1(ppt);
        slide2(ppt);
        slide3(ppt);
        slide4(ppt);
        slide5(ppt);
        slide6(ppt);
        slide7(ppt);
        slide8(ppt);
        slide9(ppt);
        slide10(ppt);
        slide11(ppt);
        slide12(ppt);

        FileOutputStream out = new FileOutputStream("apachecon_eu_08.ppt");
        ppt.write(out);
        out.close();

    }

    public static void slide1(SlideShow ppt) throws IOException {
        Slide slide = ppt.createSlide();

        TextBox box1 = new TextBox();
        TextRun tr1 = box1.getTextRun();
        tr1.setRunType(TextHeaderAtom.CENTER_TITLE_TYPE);
        tr1.setText("POI-HSLF");
        box1.setAnchor(new Rectangle(54, 78, 612, 115));
        slide.addShape(box1);

        TextBox box2 = new TextBox();
        TextRun tr2 = box2.getTextRun();
        tr2.setRunType(TextHeaderAtom.CENTRE_BODY_TYPE);
        tr2.setText("Java API To Access Microsoft PowerPoint Format Files");
        box2.setAnchor(new Rectangle(108, 204, 504, 138));
        slide.addShape(box2);

        TextBox box3 = new TextBox();
        TextRun tr3 = box3.getTextRun();
        tr3.getRichTextRuns()[0].setFontSize(32);
        box3.setHorizontalAlignment(TextBox.AlignCenter);
        tr3.setText(
                "Yegor Kozlov\r" +
                "yegor - apache - org");
        box3.setAnchor(new Rectangle(206, 348, 310, 84));
        slide.addShape(box3);
    }

    public static void slide2(SlideShow ppt) throws IOException {
        Slide slide = ppt.createSlide();

        TextBox box1 = new TextBox();
        TextRun tr1 = box1.getTextRun();
        tr1.setRunType(TextHeaderAtom.TITLE_TYPE);
        tr1.setText("What is HSLF?");
        box1.setAnchor(new Rectangle(36, 21, 648, 90));
        slide.addShape(box1);

        TextBox box2 = new TextBox();
        TextRun tr2 = box2.getTextRun();
        tr2.setRunType(TextHeaderAtom.BODY_TYPE);
        tr2.setText("HorribleSLideshowFormat is the POI Project's pure Java implementation " +
                "of the Powerpoint binary file format. \r" +
                "POI sub-project since 2005\r" +
                "Started by Nick Birch, Yegor Kozlov joined soon after");
        box2.setAnchor(new Rectangle(36, 126, 648, 356));
        slide.addShape(box2);


    }

    public static void slide3(SlideShow ppt) throws IOException {
        Slide slide = ppt.createSlide();

        TextBox box1 = new TextBox();
        TextRun tr1 = box1.getTextRun();
        tr1.setRunType(TextHeaderAtom.TITLE_TYPE);
        tr1.setText("HSLF in a Nutshell");
        box1.setAnchor(new Rectangle(36, 15, 648, 65));
        slide.addShape(box1);

        TextBox box2 = new TextBox();
        TextRun tr2 = box2.getTextRun();
        tr2.setRunType(TextHeaderAtom.BODY_TYPE);
        tr2.setText(
                "HSLF provides a way to read, create and modify MS PowerPoint presentations\r" +
                "Pure Java API - you don't need PowerPoint to read and write *.ppt files\r" +
                "Comprehensive support of PowerPoint objects");
        tr2.getRichTextRuns()[0].setFontSize(28);
        box2.setAnchor(new Rectangle(36, 80, 648, 200));
        slide.addShape(box2);

        TextBox box3 = new TextBox();
        TextRun tr3 = box3.getTextRun();
        tr3.setRunType(TextHeaderAtom.BODY_TYPE);
        tr3.setText(
                "Rich text\r" +
                "Tables\r" +
                "Shapes\r" +
                "Pictures\r" +
                "Master slides");
        tr3.getRichTextRuns()[0].setFontSize(24);
        tr3.getRichTextRuns()[0].setIndentLevel(1);
        box3.setAnchor(new Rectangle(36, 265, 648, 150));
        slide.addShape(box3);

        TextBox box4 = new TextBox();
        TextRun tr4 = box4.getTextRun();
        tr4.setRunType(TextHeaderAtom.BODY_TYPE);
        tr4.setText("Access to low level data structures");
        box4.setAnchor(new Rectangle(36, 430, 648, 50));
        slide.addShape(box4);
    }

    public static void slide4(SlideShow ppt) throws IOException {
        Slide slide = ppt.createSlide();

        String[][] txt1 = {
            {"Note"},
            {"This presentation was created programmatically using POI HSLF"}
        };
        Table table1 = new Table(2, 1);
        for (int i = 0; i < txt1.length; i++) {
            for (int j = 0; j < txt1[i].length; j++) {
                TableCell cell = table1.getCell(i, j);
                cell.setText(txt1[i][j]);
                cell.getTextRun().getRichTextRuns()[0].setFontSize(10);
                RichTextRun rt = cell.getTextRun().getRichTextRuns()[0];
                rt.setFontName("Arial");
                rt.setBold(true);
                if(i == 0){
                    rt.setFontSize(32);
                    rt.setFontColor(Color.white);
                    cell.getFill().setForegroundColor(new Color(0, 153, 204));
                } else {
                    rt.setFontSize(28);
                    cell.getFill().setForegroundColor(new Color(235, 239, 241));
                }
                cell.setVerticalAlignment(TextBox.AnchorMiddle);
            }
        }

        Line border1 = table1.createBorder();
        border1.setLineColor(Color.black);
        border1.setLineWidth(1.0);
        table1.setAllBorders(border1);

        Line border2 = table1.createBorder();
        border2.setLineColor(Color.black);
        border2.setLineWidth(2.0);
        table1.setOutsideBorders(border2);

        table1.setColumnWidth(0, 510);
        table1.setRowHeight(0, 60);
        table1.setRowHeight(1, 100);
        slide.addShape(table1);

        table1.moveTo(100, 100);

        TextBox box1 = new TextBox();
        box1.setHorizontalAlignment(TextBox.AlignCenter);
        TextRun tr1 = box1.getTextRun();
        tr1.setText("The source code is available at\r" +
                "http://people.apache.org/~yegor/apachecon_eu08/");
        RichTextRun rt = tr1.getRichTextRuns()[0];
        rt.setFontSize(24);
        box1.setAnchor(new Rectangle(80, 356, 553, 65));
        slide.addShape(box1);

    }

    public static void slide5(SlideShow ppt) throws IOException {
        Slide slide = ppt.createSlide();

        TextBox box1 = new TextBox();
        TextRun tr1 = box1.getTextRun();
        tr1.setRunType(TextHeaderAtom.TITLE_TYPE);
        tr1.setText("HSLF in Action - 1\rData Extraction");
        box1.setAnchor(new Rectangle(36, 21, 648, 100));
        slide.addShape(box1);

        TextBox box2 = new TextBox();
        TextRun tr2 = box2.getTextRun();
        tr2.setRunType(TextHeaderAtom.BODY_TYPE);
        tr2.setText(
                "Text from slides and notes\r" +
                "Images\r" +
                "Shapes and their properties (type, position in the slide, color, font, etc.)");
        box2.setAnchor(new Rectangle(36, 150, 648, 300));
        slide.addShape(box2);


    }

    public static void slide6(SlideShow ppt) throws IOException {
        Slide slide = ppt.createSlide();

        TextBox box1 = new TextBox();
        TextRun tr1 = box1.getTextRun();
        tr1.setRunType(TextHeaderAtom.TITLE_TYPE);
        tr1.setText("HSLF in Action - 2");
        box1.setAnchor(new Rectangle(36, 20, 648, 90));
        slide.addShape(box1);

        TextBox box2 = new TextBox();
        TextRun tr2 = box2.getTextRun();
        tr2.getRichTextRuns()[0].setFontSize(18);
        tr2.setText("Creating a simple presentation from scratch");
        box2.setAnchor(new Rectangle(170, 100, 364, 30));
        slide.addShape(box2);

        TextBox box3 = new TextBox();
        TextRun tr3 = box3.getTextRun();
        RichTextRun rt3 = tr3.getRichTextRuns()[0];
        rt3.setFontName("Courier New");
        rt3.setFontSize(8);
        tr3.setText(
                "        SlideShow ppt = new SlideShow();\r" +
                "        Slide slide = ppt.createSlide();\r" +
                "\r" +
                "        TextBox box2 = new TextBox();\r" +
                "        box2.setHorizontalAlignment(TextBox.AlignCenter);\r" +
                "        box2.setVerticalAlignment(TextBox.AnchorMiddle);\r" +
                "        box2.getTextRun().setText(\"Java Code\");\r" +
                "        box2.getFill().setForegroundColor(new Color(187, 224, 227));\r" +
                "        box2.setLineColor(Color.black);\r" +
                "        box2.setLineWidth(0.75);\r" +
                "        box2.setAnchor(new Rectangle(66, 243, 170, 170));\r" +
                "        slide.addShape(box2);\r" +
                "\r" +
                "        TextBox box3 = new TextBox();\r" +
                "        box3.setHorizontalAlignment(TextBox.AlignCenter);\r" +
                "        box3.setVerticalAlignment(TextBox.AnchorMiddle);\r" +
                "        box3.getTextRun().setText(\"*.ppt file\");\r" +
                "        box3.setLineWidth(0.75);\r" +
                "        box3.setLineColor(Color.black);\r" +
                "        box3.getFill().setForegroundColor(new Color(187, 224, 227));\r" +
                "        box3.setAnchor(new Rectangle(473, 243, 170, 170));\r" +
                "        slide.addShape(box3);\r" +
                "\r" +
                "        AutoShape box4 = new AutoShape(ShapeTypes.Arrow);\r" +
                "        box4.getFill().setForegroundColor(new Color(187, 224, 227));\r" +
                "        box4.setLineWidth(0.75);\r" +
                "        box4.setLineColor(Color.black);\r" +
                "        box4.setAnchor(new Rectangle(253, 288, 198, 85));\r" +
                "        slide.addShape(box4);\r" +
                "\r" +
                "        FileOutputStream out = new FileOutputStream(\"hslf-demo.ppt\");\r" +
                "        ppt.write(out);\r" +
                "        out.close();");
        box3.setAnchor(new Rectangle(30, 150, 618, 411));
        slide.addShape(box3);
    }

    public static void slide7(SlideShow ppt) throws IOException {
        Slide slide = ppt.createSlide();

        TextBox box2 = new TextBox();
        box2.setHorizontalAlignment(TextBox.AlignCenter);
        box2.setVerticalAlignment(TextBox.AnchorMiddle);
        box2.getTextRun().setText("Java Code");
        box2.getFill().setForegroundColor(new Color(187, 224, 227));
        box2.setLineColor(Color.black);
        box2.setLineWidth(0.75);
        box2.setAnchor(new Rectangle(66, 243, 170, 170));
        slide.addShape(box2);

        TextBox box3 = new TextBox();
        box3.setHorizontalAlignment(TextBox.AlignCenter);
        box3.setVerticalAlignment(TextBox.AnchorMiddle);
        box3.getTextRun().setText("*.ppt file");
        box3.setLineWidth(0.75);
        box3.setLineColor(Color.black);
        box3.getFill().setForegroundColor(new Color(187, 224, 227));
        box3.setAnchor(new Rectangle(473, 243, 170, 170));
        slide.addShape(box3);

        AutoShape box4 = new AutoShape(ShapeTypes.Arrow);
        box4.getFill().setForegroundColor(new Color(187, 224, 227));
        box4.setLineWidth(0.75);
        box4.setLineColor(Color.black);
        box4.setAnchor(new Rectangle(253, 288, 198, 85));
        slide.addShape(box4);
    }

    public static void slide8(SlideShow ppt) throws IOException {
        Slide slide = ppt.createSlide();

        TextBox box1 = new TextBox();
        TextRun tr1 = box1.getTextRun();
        tr1.setRunType(TextHeaderAtom.TITLE_TYPE);
        tr1.setText("Wait, there is more!");
        box1.setAnchor(new Rectangle(36, 21, 648, 90));
        slide.addShape(box1);

        TextBox box2 = new TextBox();
        TextRun tr2 = box2.getTextRun();
        tr2.setRunType(TextHeaderAtom.BODY_TYPE);
        tr2.setText(
                "Rich text\r" +
                "Tables\r" +
                "Pictures (JPEG, PNG, BMP, WMF, PICT)\r" +
                "Comprehensive formatting features");
        box2.setAnchor(new Rectangle(36, 126, 648, 356));
        slide.addShape(box2);
    }

    public static void slide9(SlideShow ppt) throws IOException {
        Slide slide = ppt.createSlide();

        TextBox box1 = new TextBox();
        TextRun tr1 = box1.getTextRun();
        tr1.setRunType(TextHeaderAtom.TITLE_TYPE);
        tr1.setText("HSLF in Action - 3");
        box1.setAnchor(new Rectangle(36, 20, 648, 50));
        slide.addShape(box1);

        TextBox box2 = new TextBox();
        TextRun tr2 = box2.getTextRun();
        tr2.getRichTextRuns()[0].setFontSize(18);
        tr2.setText("PPGraphics2D: PowerPoint Graphics2D driver");
        box2.setAnchor(new Rectangle(178, 70, 387, 30));
        slide.addShape(box2);

        TextBox box3 = new TextBox();
        TextRun tr3 = box3.getTextRun();
        RichTextRun rt3 = tr3.getRichTextRuns()[0];
        rt3.setFontName("Courier New");
        rt3.setFontSize(8);
        tr3.setText(
                "        //bar chart data. The first value is the bar color, the second is the width\r" +
                "        Object[] def = new Object[]{\r" +
                "            Color.yellow, new Integer(100),\r" +
                "            Color.green, new Integer(150),\r" +
                "            Color.gray, new Integer(75),\r" +
                "            Color.red, new Integer(200),\r" +
                "        };\r" +
                "\r" +
                "        SlideShow ppt = new SlideShow();\r" +
                "        Slide slide = ppt.createSlide();\r" +
                "\r" +
                "        ShapeGroup group = new ShapeGroup();\r" +
                "        //define position of the drawing in the slide\r" +
                "        Rectangle bounds = new java.awt.Rectangle(200, 100, 350, 300);\r" +
                "        group.setAnchor(bounds);\r" +
                "        slide.addShape(group);\r" +
                "        Graphics2D graphics = new PPGraphics2D(group);\r" +
                "\r" +
                "        //draw a simple bar graph\r" +
                "        int x = bounds.x + 50, y = bounds.y + 50;\r" +
                "        graphics.setFont(new Font(\"Arial\", Font.BOLD, 10));\r" +
                "        for (int i = 0, idx = 1; i < def.length; i+=2, idx++) {\r" +
                "            graphics.setColor(Color.black);\r" +
                "            int width = ((Integer)def[i+1]).intValue();\r" +
                "            graphics.drawString(\"Q\" + idx, x-20, y+20);\r" +
                "            graphics.drawString(width + \"%\", x + width + 10, y + 20);\r" +
                "            graphics.setColor((Color)def[i]);\r" +
                "            graphics.fill(new Rectangle(x, y, width, 30));\r" +
                "            y += 40;\r" +
                "        }\r" +
                "        graphics.setColor(Color.black);\r" +
                "        graphics.setFont(new Font(\"Arial\", Font.BOLD, 14));\r" +
                "        graphics.draw(bounds);\r" +
                "        graphics.drawString(\"Performance\", x + 70, y + 40);\r" +
                "\r" +
                "        FileOutputStream out = new FileOutputStream(\"hslf-demo.ppt\");\r" +
                "        ppt.write(out);\r" +
                "        out.close();");
        box3.setAnchor(new Rectangle(96, 110, 499, 378));
        slide.addShape(box3);
    }

    public static void slide10(SlideShow ppt) throws IOException {
        //bar chart data. The first value is the bar color, the second is the width
        Object[] def = new Object[]{
            Color.yellow, new Integer(100),
            Color.green, new Integer(150),
            Color.gray, new Integer(75),
            Color.red, new Integer(200),
        };

        Slide slide = ppt.createSlide();

        ShapeGroup group = new ShapeGroup();
        //define position of the drawing in the slide
        Rectangle bounds = new java.awt.Rectangle(200, 100, 350, 300);
        group.setAnchor(bounds);
        slide.addShape(group);
        Graphics2D graphics = new PPGraphics2D(group);

        //draw a simple bar graph
        int x = bounds.x + 50, y = bounds.y + 50;
        graphics.setFont(new Font("Arial", Font.BOLD, 10));
        for (int i = 0, idx = 1; i < def.length; i+=2, idx++) {
            graphics.setColor(Color.black);
            int width = ((Integer)def[i+1]).intValue();
            graphics.drawString("Q" + idx, x-20, y+20);
            graphics.drawString(width + "%", x + width + 10, y + 20);
            graphics.setColor((Color)def[i]);
            graphics.fill(new Rectangle(x, y, width, 30));
            y += 40;
        }
        graphics.setColor(Color.black);
        graphics.setFont(new Font("Arial", Font.BOLD, 14));
        graphics.draw(bounds);
        graphics.drawString("Performance", x + 70, y + 40);

    }

    public static void slide11(SlideShow ppt) throws IOException {
        Slide slide = ppt.createSlide();

        TextBox box1 = new TextBox();
        TextRun tr1 = box1.getTextRun();
        tr1.setRunType(TextHeaderAtom.TITLE_TYPE);
        tr1.setText("HSLF Development Plans");
        box1.setAnchor(new Rectangle(36, 21, 648, 90));
        slide.addShape(box1);

        TextBox box2 = new TextBox();
        TextRun tr2 = box2.getTextRun();
        tr2.setRunType(TextHeaderAtom.BODY_TYPE);
        tr2.getRichTextRuns()[0].setFontSize(32);
        tr2.setText(
                "Support for more PowerPoint functionality\r" +
                "Rendering slides into java.awt.Graphics2D");
        box2.setAnchor(new Rectangle(36, 126, 648, 100));
        slide.addShape(box2);

        TextBox box3 = new TextBox();
        TextRun tr3 = box3.getTextRun();
        tr3.setRunType(TextHeaderAtom.BODY_TYPE);
        tr3.getRichTextRuns()[0].setIndentLevel(1);
        tr3.setText(
                "A way to export slides into images or other formats");
        box3.setAnchor(new Rectangle(36, 220, 648, 70));
        slide.addShape(box3);

        TextBox box4 = new TextBox();
        TextRun tr4 = box4.getTextRun();
        tr4.setRunType(TextHeaderAtom.BODY_TYPE);
        tr4.getRichTextRuns()[0].setFontSize(32);
        tr4.setText(
                "Integration with Apache FOP - Formatting Objects Processor");
        box4.setAnchor(new Rectangle(36, 290, 648, 90));
        slide.addShape(box4);

        TextBox box5 = new TextBox();
        TextRun tr5 = box5.getTextRun();
        tr5.setRunType(TextHeaderAtom.BODY_TYPE);
        tr5.getRichTextRuns()[0].setIndentLevel(1);
        tr5.setText(
                "Transformation of XSL-FO into PPT\r" +
                "PPT2PDF transcoder");
        box5.setAnchor(new Rectangle(36, 380, 648, 100));
        slide.addShape(box5);
    }

    public static void slide12(SlideShow ppt) throws IOException {
        Slide slide = ppt.createSlide();

        TextBox box1 = new TextBox();
        TextRun tr1 = box1.getTextRun();
        tr1.setRunType(TextHeaderAtom.CENTER_TITLE_TYPE);
        tr1.setText("Questions?");
        box1.setAnchor(new Rectangle(54, 167, 612, 115));
        slide.addShape(box1);

        TextBox box2 = new TextBox();
        TextRun tr2 = box2.getTextRun();
        tr2.setRunType(TextHeaderAtom.CENTRE_BODY_TYPE);
        tr2.setText(
                "http://poi.apache.org/hslf/\r" +
                "http://people.apache.org/~yegor");
        box2.setAnchor(new Rectangle(108, 306, 504, 138));
        slide.addShape(box2);
    }
}
