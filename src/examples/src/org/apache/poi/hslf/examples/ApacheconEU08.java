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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hslf.model.PPGraphics2D;
import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.hslf.usermodel.HSLFAutoShape;
import org.apache.poi.hslf.usermodel.HSLFGroupShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTable;
import org.apache.poi.hslf.usermodel.HSLFTableCell;
import org.apache.poi.hslf.usermodel.HSLFTextBox;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hslf.usermodel.HSLFTextRun;
import org.apache.poi.hslf.usermodel.HSLFLine;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.VerticalAlignment;

/**
 * Presentation for Fast Feather Track on ApacheconEU 2008
 *
 * @author Yegor Kozlov
 */
public final class ApacheconEU08 {

    public static void main(String[] args) throws IOException {
        HSLFSlideShow ppt = new HSLFSlideShow();
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

    public static void slide1(HSLFSlideShow ppt) throws IOException {
        HSLFSlide slide = ppt.createSlide();

        HSLFTextBox box1 = new HSLFTextBox();
        box1.setRunType(TextHeaderAtom.CENTER_TITLE_TYPE);
        box1.setText("POI-HSLF");
        box1.setAnchor(new Rectangle(54, 78, 612, 115));
        slide.addShape(box1);

        HSLFTextBox box2 = new HSLFTextBox();
        box2.setRunType(TextHeaderAtom.CENTRE_BODY_TYPE);
        box2.setText("Java API To Access Microsoft PowerPoint Format Files");
        box2.setAnchor(new Rectangle(108, 204, 504, 138));
        slide.addShape(box2);

        HSLFTextBox box3 = new HSLFTextBox();
        box3.getTextParagraphs().get(0).getTextRuns().get(0).setFontSize(32d);
        box3.setText(
                "Yegor Kozlov\r" +
                "yegor - apache - org");
        box3.setHorizontalCentered(true);
        box3.setAnchor(new Rectangle(206, 348, 310, 84));
        slide.addShape(box3);
    }

    public static void slide2(HSLFSlideShow ppt) throws IOException {
        HSLFSlide slide = ppt.createSlide();

        HSLFTextBox box1 = new HSLFTextBox();
        box1.setRunType(TextHeaderAtom.TITLE_TYPE);
        box1.setText("What is HSLF?");
        box1.setAnchor(new Rectangle(36, 21, 648, 90));
        slide.addShape(box1);

        HSLFTextBox box2 = new HSLFTextBox();
        box2.setRunType(TextHeaderAtom.BODY_TYPE);
        box2.setText("HorribleSLideshowFormat is the POI Project's pure Java implementation " +
                "of the Powerpoint binary file format. \r" +
                "POI sub-project since 2005\r" +
                "Started by Nick Birch, Yegor Kozlov joined soon after");
        box2.setAnchor(new Rectangle(36, 126, 648, 356));
        slide.addShape(box2);


    }

    public static void slide3(HSLFSlideShow ppt) throws IOException {
        HSLFSlide slide = ppt.createSlide();

        HSLFTextBox box1 = new HSLFTextBox();
        box1.setRunType(TextHeaderAtom.TITLE_TYPE);
        box1.setText("HSLF in a Nutshell");
        box1.setAnchor(new Rectangle(36, 15, 648, 65));
        slide.addShape(box1);

        HSLFTextBox box2 = new HSLFTextBox();
        box2.setRunType(TextHeaderAtom.BODY_TYPE);
        box2.setText(
            "HSLF provides a way to read, create and modify MS PowerPoint presentations\r" +
            "Pure Java API - you don't need PowerPoint to read and write *.ppt files\r" +
            "Comprehensive support of PowerPoint objects\r" +
                "Rich text\r" +
                "Tables\r" +
                "Shapes\r" +
                "Pictures\r" +
                "Master slides\r" +
            "Access to low level data structures"
        );

        List<HSLFTextParagraph> tp = box2.getTextParagraphs();
        for (int i : new byte[]{0,1,2,8}) {
            tp.get(i).getTextRuns().get(0).setFontSize(28d);
        }
        for (int i : new byte[]{3,4,5,6,7}) {
            tp.get(i).getTextRuns().get(0).setFontSize(24d);
            tp.get(i).setIndentLevel(1);
        }
        box2.setAnchor(new Rectangle(36, 80, 648, 400));
        slide.addShape(box2);
    }

    public static void slide4(HSLFSlideShow ppt) throws IOException {
        HSLFSlide slide = ppt.createSlide();

        String[][] txt1 = {
            {"Note"},
            {"This presentation was created programmatically using POI HSLF"}
        };
        HSLFTable table1 = new HSLFTable(2, 1);
        for (int i = 0; i < txt1.length; i++) {
            for (int j = 0; j < txt1[i].length; j++) {
                HSLFTableCell cell = table1.getCell(i, j);
                cell.setText(txt1[i][j]);
                HSLFTextRun rt = cell.getTextParagraphs().get(0).getTextRuns().get(0);
                rt.setFontSize(10d);
                rt.setFontFamily("Arial");
                rt.setBold(true);
                if(i == 0){
                    rt.setFontSize(32d);
                    rt.setFontColor(Color.white);
                    cell.getFill().setForegroundColor(new Color(0, 153, 204));
                } else {
                    rt.setFontSize(28d);
                    cell.getFill().setForegroundColor(new Color(235, 239, 241));
                }
                cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            }
        }

        HSLFLine border1 = table1.createBorder();
        border1.setLineColor(Color.black);
        border1.setLineWidth(1.0);
        table1.setAllBorders(border1);

        HSLFLine border2 = table1.createBorder();
        border2.setLineColor(Color.black);
        border2.setLineWidth(2.0);
        table1.setOutsideBorders(border2);

        table1.setColumnWidth(0, 510);
        table1.setRowHeight(0, 60);
        table1.setRowHeight(1, 100);
        slide.addShape(table1);

        table1.moveTo(100, 100);

        HSLFTextBox box1 = new HSLFTextBox();
        box1.setHorizontalCentered(true);
        box1.getTextParagraphs().get(0).getTextRuns().get(0).setFontSize(24d);
        box1.setText("The source code is available at\r" +
                "http://people.apache.org/~yegor/apachecon_eu08/");
        box1.setAnchor(new Rectangle(80, 356, 553, 65));
        slide.addShape(box1);

    }

    public static void slide5(HSLFSlideShow ppt) throws IOException {
        HSLFSlide slide = ppt.createSlide();

        HSLFTextBox box1 = new HSLFTextBox();
        box1.setRunType(TextHeaderAtom.TITLE_TYPE);
        box1.setText("HSLF in Action - 1\rData Extraction");
        box1.setAnchor(new Rectangle(36, 21, 648, 100));
        slide.addShape(box1);

        HSLFTextBox box2 = new HSLFTextBox();
        box2.setRunType(TextHeaderAtom.BODY_TYPE);
        box2.setText(
                "Text from slides and notes\r" +
                "Images\r" +
                "Shapes and their properties (type, position in the slide, color, font, etc.)");
        box2.setAnchor(new Rectangle(36, 150, 648, 300));
        slide.addShape(box2);


    }

    public static void slide6(HSLFSlideShow ppt) throws IOException {
        HSLFSlide slide = ppt.createSlide();

        HSLFTextBox box1 = new HSLFTextBox();
        box1.setRunType(TextHeaderAtom.TITLE_TYPE);
        box1.setText("HSLF in Action - 2");
        box1.setAnchor(new Rectangle(36, 20, 648, 90));
        slide.addShape(box1);

        HSLFTextBox box2 = new HSLFTextBox();
        box2.getTextParagraphs().get(0).getTextRuns().get(0).setFontSize(18d);
        box2.setText("Creating a simple presentation from scratch");
        box2.setAnchor(new Rectangle(170, 100, 364, 30));
        slide.addShape(box2);

        HSLFTextBox box3 = new HSLFTextBox();
        HSLFTextRun rt3 = box3.getTextParagraphs().get(0).getTextRuns().get(0);
        rt3.setFontFamily("Courier New");
        rt3.setFontSize(8d);
        box3.setText(
                "SlideShow ppt = new SlideShow();\u000b" +
                "Slide slide = ppt.createSlide();\u000b" +
                "\u000b" +
                "TextBox box2 = new TextBox();\u000b" +
                "box2.setHorizontalAlignment(TextBox.AlignCenter);\u000b" +
                "box2.setVerticalAlignment(TextBox.AnchorMiddle);\u000b" +
                "box2.getTextRun().setText(\"Java Code\");\u000b" +
                "box2.getFill().setForegroundColor(new Color(187, 224, 227));\u000b" +
                "box2.setLineColor(Color.black);\u000b" +
                "box2.setLineWidth(0.75);\u000b" +
                "box2.setAnchor(new Rectangle(66, 243, 170, 170));\u000b" +
                "slide.addShape(box2);\u000b" +
                "\u000b" +
                "TextBox box3 = new TextBox();\u000b" +
                "box3.setHorizontalAlignment(TextBox.AlignCenter);\u000b" +
                "box3.setVerticalAlignment(TextBox.AnchorMiddle);\u000b" +
                "box3.getTextRun().setText(\"*.ppt file\");\u000b" +
                "box3.setLineWidth(0.75);\u000b" +
                "box3.setLineColor(Color.black);\u000b" +
                "box3.getFill().setForegroundColor(new Color(187, 224, 227));\u000b" +
                "box3.setAnchor(new Rectangle(473, 243, 170, 170));\u000b" +
                "slide.addShape(box3);\u000b" +
                "\u000b" +
                "AutoShape box4 = new AutoShape(ShapeTypes.Arrow);\u000b" +
                "box4.getFill().setForegroundColor(new Color(187, 224, 227));\u000b" +
                "box4.setLineWidth(0.75);\u000b" +
                "box4.setLineColor(Color.black);\u000b" +
                "box4.setAnchor(new Rectangle(253, 288, 198, 85));\u000b" +
                "slide.addShape(box4);\u000b" +
                "\u000b" +
                "FileOutputStream out = new FileOutputStream(\"hslf-demo.ppt\");\u000b" +
                "ppt.write(out);\u000b" +
                "out.close();");
        box3.setAnchor(new Rectangle(30, 150, 618, 411));
        box3.setHorizontalCentered(true);
        slide.addShape(box3);
    }

    public static void slide7(HSLFSlideShow ppt) throws IOException {
        HSLFSlide slide = ppt.createSlide();

        HSLFTextBox box2 = new HSLFTextBox();
        box2.setHorizontalCentered(true);
        box2.setVerticalAlignment(VerticalAlignment.MIDDLE);
        box2.setText("Java Code");
        box2.getFill().setForegroundColor(new Color(187, 224, 227));
        box2.setLineColor(Color.black);
        box2.setLineWidth(0.75);
        box2.setAnchor(new Rectangle(66, 243, 170, 170));
        slide.addShape(box2);

        HSLFTextBox box3 = new HSLFTextBox();
        box3.setHorizontalCentered(true);
        box3.setVerticalAlignment(VerticalAlignment.MIDDLE);
        box3.setText("*.ppt file");
        box3.setLineWidth(0.75);
        box3.setLineColor(Color.black);
        box3.getFill().setForegroundColor(new Color(187, 224, 227));
        box3.setAnchor(new Rectangle(473, 243, 170, 170));
        slide.addShape(box3);

        HSLFAutoShape box4 = new HSLFAutoShape(ShapeType.RIGHT_ARROW);
        box4.getFill().setForegroundColor(new Color(187, 224, 227));
        box4.setLineWidth(0.75);
        box4.setLineColor(Color.black);
        box4.setAnchor(new Rectangle(253, 288, 198, 85));
        slide.addShape(box4);
    }

    public static void slide8(HSLFSlideShow ppt) throws IOException {
        HSLFSlide slide = ppt.createSlide();

        HSLFTextBox box1 = new HSLFTextBox();
        box1.setRunType(TextHeaderAtom.TITLE_TYPE);
        box1.setText("Wait, there is more!");
        box1.setAnchor(new Rectangle(36, 21, 648, 90));
        slide.addShape(box1);

        HSLFTextBox box2 = new HSLFTextBox();
        box2.setRunType(TextHeaderAtom.BODY_TYPE);
        box2.setText(
                "Rich text\r" +
                "Tables\r" +
                "Pictures (JPEG, PNG, BMP, WMF, PICT)\r" +
                "Comprehensive formatting features");
        box2.setAnchor(new Rectangle(36, 126, 648, 356));
        slide.addShape(box2);
    }

    public static void slide9(HSLFSlideShow ppt) throws IOException {
        HSLFSlide slide = ppt.createSlide();

        HSLFTextBox box1 = new HSLFTextBox();
        box1.setRunType(TextHeaderAtom.TITLE_TYPE);
        box1.setText("HSLF in Action - 3");
        box1.setAnchor(new Rectangle(36, 20, 648, 50));
        slide.addShape(box1);

        HSLFTextBox box2 = new HSLFTextBox();
        box2.getTextParagraphs().get(0).getTextRuns().get(0).setFontSize(18d);
        box2.setText("PPGraphics2D: PowerPoint Graphics2D driver");
        box2.setAnchor(new Rectangle(178, 70, 387, 30));
        slide.addShape(box2);

        HSLFTextBox box3 = new HSLFTextBox();
        HSLFTextRun rt3 = box3.getTextParagraphs().get(0).getTextRuns().get(0);
        rt3.setFontFamily("Courier New");
        rt3.setFontSize(8d);
        box3.setText(
                "//bar chart data. The first value is the bar color, the second is the width\u000b" +
                "Object[] def = new Object[]{\u000b" +
                "    Color.yellow, new Integer(100),\u000b" +
                "    Color.green, new Integer(150),\u000b" +
                "    Color.gray, new Integer(75),\u000b" +
                "    Color.red, new Integer(200),\u000b" +
                "};\u000b" +
                "\u000b" +
                "SlideShow ppt = new SlideShow();\u000b" +
                "Slide slide = ppt.createSlide();\u000b" +
                "\u000b" +
                "ShapeGroup group = new ShapeGroup();\u000b" +
                "//define position of the drawing in the slide\u000b" +
                "Rectangle bounds = new java.awt.Rectangle(200, 100, 350, 300);\u000b" +
                "group.setAnchor(bounds);\u000b" +
                "slide.addShape(group);\u000b" +
                "Graphics2D graphics = new PPGraphics2D(group);\u000b" +
                "\u000b" +
                "//draw a simple bar graph\u000b" +
                "int x = bounds.x + 50, y = bounds.y + 50;\u000b" +
                "graphics.setFont(new Font(\"Arial\", Font.BOLD, 10));\u000b" +
                "for (int i = 0, idx = 1; i < def.length; i+=2, idx++) {\u000b" +
                "    graphics.setColor(Color.black);\u000b" +
                "    int width = ((Integer)def[i+1]).intValue();\u000b" +
                "    graphics.drawString(\"Q\" + idx, x-20, y+20);\u000b" +
                "    graphics.drawString(width + \"%\", x + width + 10, y + 20);\u000b" +
                "    graphics.setColor((Color)def[i]);\u000b" +
                "    graphics.fill(new Rectangle(x, y, width, 30));\u000b" +
                "    y += 40;\u000b" +
                "}\u000b" +
                "graphics.setColor(Color.black);\u000b" +
                "graphics.setFont(new Font(\"Arial\", Font.BOLD, 14));\u000b" +
                "graphics.draw(bounds);\u000b" +
                "graphics.drawString(\"Performance\", x + 70, y + 40);\u000b" +
                "\u000b" +
                "FileOutputStream out = new FileOutputStream(\"hslf-demo.ppt\");\u000b" +
                "ppt.write(out);\u000b" +
                "out.close();");
        box3.setAnchor(new Rectangle(96, 110, 499, 378));
        box3.setHorizontalCentered(true);
        slide.addShape(box3);
    }

    public static void slide10(HSLFSlideShow ppt) throws IOException {
        //bar chart data. The first value is the bar color, the second is the width
        Object[] def = new Object[]{
            Color.yellow, new Integer(100),
            Color.green, new Integer(150),
            Color.gray, new Integer(75),
            Color.red, new Integer(200),
        };

        HSLFSlide slide = ppt.createSlide();

        HSLFGroupShape group = new HSLFGroupShape();
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

    public static void slide11(HSLFSlideShow ppt) throws IOException {
        HSLFSlide slide = ppt.createSlide();

        HSLFTextBox box1 = new HSLFTextBox();
        box1.setRunType(TextHeaderAtom.TITLE_TYPE);
        box1.setText("HSLF Development Plans");
        box1.setAnchor(new Rectangle(36, 21, 648, 90));
        slide.addShape(box1);

        HSLFTextBox box2 = new HSLFTextBox();
        box2.setRunType(TextHeaderAtom.BODY_TYPE);
        box2.setText(
            "Support for more PowerPoint functionality\r" +
            "Rendering slides into java.awt.Graphics2D\r" +
                "A way to export slides into images or other formats\r" +
            "Integration with Apache FOP - Formatting Objects Processor\r" +
                "Transformation of XSL-FO into PPT\r" +
                "PPT2PDF transcoder"
        );

        List<HSLFTextParagraph> tp = box2.getTextParagraphs();
        for (int i : new byte[]{0,1,3}) {
            tp.get(i).getTextRuns().get(0).setFontSize(28d);
        }
        for (int i : new byte[]{2,4,5}) {
            tp.get(i).getTextRuns().get(0).setFontSize(24d);
            tp.get(i).setIndentLevel(1);
        }
        
        box2.setAnchor(new Rectangle(36, 126, 648, 400));
        slide.addShape(box2);
    }

    public static void slide12(HSLFSlideShow ppt) throws IOException {
        HSLFSlide slide = ppt.createSlide();

        HSLFTextBox box1 = new HSLFTextBox();
        box1.setRunType(TextHeaderAtom.CENTER_TITLE_TYPE);
        box1.setText("Questions?");
        box1.setAnchor(new Rectangle(54, 167, 612, 115));
        slide.addShape(box1);

        HSLFTextBox box2 = new HSLFTextBox();
        box2.setRunType(TextHeaderAtom.CENTRE_BODY_TYPE);
        box2.setText(
                "http://poi.apache.org/hslf/\r" +
                "http://people.apache.org/~yegor");
        box2.setAnchor(new Rectangle(108, 306, 504, 138));
        slide.addShape(box2);
    }
}
