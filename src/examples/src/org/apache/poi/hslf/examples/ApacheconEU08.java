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
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.draw.DrawTableShape;
import org.apache.poi.sl.draw.SLGraphics;
import org.apache.poi.sl.usermodel.AutoShape;
import org.apache.poi.sl.usermodel.GroupShape;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.TableCell;
import org.apache.poi.sl.usermodel.TableShape;
import org.apache.poi.sl.usermodel.TextBox;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextRun;
import org.apache.poi.sl.usermodel.TextShape.TextPlaceholder;
import org.apache.poi.sl.usermodel.VerticalAlignment;

/**
 * Presentation for Fast Feather Track on ApacheconEU 2008
 *
 * @author Yegor Kozlov
 */
public final class ApacheconEU08 {

    public static void main(String[] args) throws IOException {
        try (SlideShow<?,?> ppt = new HSLFSlideShow()) {
            // SlideShow<?,?> ppt = new XMLSlideShow();
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

            String ext = ppt.getClass().getName().contains("HSLF") ? "ppt" : "pptx";
            try (FileOutputStream out = new FileOutputStream("apachecon_eu_08." + ext)) {
                ppt.write(out);
            }
        }
    }

    public static void slide1(SlideShow<?,?> ppt) throws IOException {
        Slide<?,?> slide = ppt.createSlide();

        TextBox<?,?> box1 = slide.createTextBox();
        box1.setTextPlaceholder(TextPlaceholder.CENTER_TITLE);
        box1.setText("POI-HSLF");
        box1.setAnchor(new Rectangle(54, 78, 612, 115));

        TextBox<?,?> box2 = slide.createTextBox();
        box2.setTextPlaceholder(TextPlaceholder.CENTER_BODY);
        box2.setText("Java API To Access Microsoft PowerPoint Format Files");
        box2.setAnchor(new Rectangle(108, 204, 504, 138));

        TextBox<?,?> box3 = slide.createTextBox();
        box3.getTextParagraphs().get(0).getTextRuns().get(0).setFontSize(32d);
        box3.setText(
                "Yegor Kozlov\r" +
                "yegor - apache - org");
        box3.setHorizontalCentered(true);
        box3.setAnchor(new Rectangle(206, 348, 310, 84));
    }

    public static void slide2(SlideShow<?,?> ppt) throws IOException {
        Slide<?,?> slide = ppt.createSlide();

        TextBox<?,?> box1 = slide.createTextBox();
        box1.setTextPlaceholder(TextPlaceholder.TITLE);
        box1.setText("What is HSLF?");
        box1.setAnchor(new Rectangle(36, 21, 648, 90));

        TextBox<?,?> box2 = slide.createTextBox();
        box2.setTextPlaceholder(TextPlaceholder.BODY);
        box2.setText("HorribleSLideshowFormat is the POI Project's pure Java implementation " +
                "of the Powerpoint binary file format. \r" +
                "POI sub-project since 2005\r" +
                "Started by Nick Burch, Yegor Kozlov joined soon after");
        box2.setAnchor(new Rectangle(36, 126, 648, 356));
    }

    public static void slide3(SlideShow<?,?> ppt) throws IOException {
        Slide<?,?> slide = ppt.createSlide();

        TextBox<?,?> box1 = slide.createTextBox();
        box1.setTextPlaceholder(TextPlaceholder.TITLE);
        box1.setText("HSLF in a Nutshell");
        box1.setAnchor(new Rectangle(36, 15, 648, 65));

        TextBox<?,?> box2 = slide.createTextBox();
        box2.setTextPlaceholder(TextPlaceholder.BODY);
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

        List<? extends TextParagraph<?,?,?>> tp = box2.getTextParagraphs();
        for (int i : new byte[]{0,1,2,8}) {
            tp.get(i).getTextRuns().get(0).setFontSize(28d);
        }
        for (int i : new byte[]{3,4,5,6,7}) {
            tp.get(i).getTextRuns().get(0).setFontSize(24d);
            tp.get(i).setIndentLevel(1);
        }
        box2.setAnchor(new Rectangle(36, 80, 648, 400));
    }

    public static void slide4(SlideShow<?,?> ppt) throws IOException {
        Slide<?,?> slide = ppt.createSlide();

        String[][] txt1 = {
            {"Note"},
            {"This presentation was created programmatically using POI HSLF"}
        };
        TableShape<?,?> table1 = slide.createTable(2, 1);
        for (int i = 0; i < txt1.length; i++) {
            for (int j = 0; j < txt1[i].length; j++) {
                TableCell<?,?> cell = table1.getCell(i, j);
                cell.setText(txt1[i][j]);
                TextRun rt = cell.getTextParagraphs().get(0).getTextRuns().get(0);
                rt.setFontSize(10d);
                rt.setFontFamily("Arial");
                rt.setBold(true);
                if(i == 0){
                    rt.setFontSize(32d);
                    rt.setFontColor(Color.white);
                    cell.setFillColor(new Color(0, 153, 204));
                } else {
                    rt.setFontSize(28d);
                    cell.setFillColor(new Color(235, 239, 241));
                }
                cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            }
        }

        DrawTableShape dts = new DrawTableShape(table1);
        dts.setAllBorders(1.0, Color.black);
        dts.setOutsideBorders(4.0);

        table1.setColumnWidth(0, 450);
        table1.setRowHeight(0, 50);
        table1.setRowHeight(1, 80);

        Dimension dim = ppt.getPageSize();
        Rectangle2D oldAnchor = table1.getAnchor();
        table1.setAnchor(new Rectangle2D.Double((dim.width-450)/2d, 100, oldAnchor.getWidth(), oldAnchor.getHeight()));

        TextBox<?,?> box1 = slide.createTextBox();
        box1.setHorizontalCentered(true);
        box1.getTextParagraphs().get(0).getTextRuns().get(0).setFontSize(24d);
        box1.setText("The source code is available at\r" +
                "http://people.apache.org/~yegor/apachecon_eu08/");
        box1.setAnchor(new Rectangle(80, 356, 553, 65));
    }

    public static void slide5(SlideShow<?,?> ppt) throws IOException {
        Slide<?,?> slide = ppt.createSlide();

        TextBox<?,?> box1 = slide.createTextBox();
        box1.setTextPlaceholder(TextPlaceholder.TITLE);
        box1.setText("HSLF in Action - 1\rData Extraction");
        box1.setAnchor(new Rectangle(36, 21, 648, 100));

        TextBox<?,?> box2 = slide.createTextBox();
        box2.setTextPlaceholder(TextPlaceholder.BODY);
        box2.setText(
                "Text from slides and notes\r" +
                "Images\r" +
                "Shapes and their properties (type, position in the slide, color, font, etc.)");
        box2.setAnchor(new Rectangle(36, 150, 648, 300));
    }

    public static void slide6(SlideShow<?,?> ppt) throws IOException {
        Slide<?,?> slide = ppt.createSlide();

        TextBox<?,?> box1 = slide.createTextBox();
        box1.setTextPlaceholder(TextPlaceholder.TITLE);
        box1.setText("HSLF in Action - 2");
        box1.setAnchor(new Rectangle(36, 20, 648, 90));

        TextBox<?,?> box2 = slide.createTextBox();
        box2.getTextParagraphs().get(0).getTextRuns().get(0).setFontSize(18d);
        box2.setText("Creating a simple presentation from scratch");
        box2.setAnchor(new Rectangle(170, 100, 364, 30));

        TextBox<?,?> box3 = slide.createTextBox();
        TextRun rt3 = box3.getTextParagraphs().get(0).getTextRuns().get(0);
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
    }

    public static void slide7(SlideShow<?,?> ppt) throws IOException {
        Slide<?,?> slide = ppt.createSlide();

        TextBox<?,?> box2 = slide.createTextBox();
        box2.setHorizontalCentered(true);
        box2.setVerticalAlignment(VerticalAlignment.MIDDLE);
        box2.setText("Java Code");
        box2.setFillColor(new Color(187, 224, 227));
        box2.setStrokeStyle(0.75, Color.black);
        box2.setAnchor(new Rectangle(66, 243, 170, 170));

        TextBox<?,?> box3 = slide.createTextBox();
        box3.setHorizontalCentered(true);
        box3.setVerticalAlignment(VerticalAlignment.MIDDLE);
        box3.setText("*.ppt file");
        box3.setFillColor(new Color(187, 224, 227));
        box3.setStrokeStyle(0.75, Color.black);
        box3.setAnchor(new Rectangle(473, 243, 170, 170));

        AutoShape<?,?> box4 = slide.createAutoShape();
        box4.setShapeType(ShapeType.RIGHT_ARROW);
        box4.setFillColor(new Color(187, 224, 227));
        box4.setStrokeStyle(0.75, Color.black);
        box4.setAnchor(new Rectangle(253, 288, 198, 85));
    }

    public static void slide8(SlideShow<?,?> ppt) throws IOException {
        Slide<?,?> slide = ppt.createSlide();

        TextBox<?,?> box1 = slide.createTextBox();
        box1.setTextPlaceholder(TextPlaceholder.TITLE);
        box1.setText("Wait, there is more!");
        box1.setAnchor(new Rectangle(36, 21, 648, 90));

        TextBox<?,?> box2 = slide.createTextBox();
        box2.setTextPlaceholder(TextPlaceholder.BODY);
        box2.setText(
                "Rich text\r" +
                "Tables\r" +
                "Pictures (JPEG, PNG, BMP, WMF, PICT)\r" +
                "Comprehensive formatting features");
        box2.setAnchor(new Rectangle(36, 126, 648, 356));
    }

    public static void slide9(SlideShow<?,?> ppt) throws IOException {
        Slide<?,?> slide = ppt.createSlide();

        TextBox<?,?> box1 = slide.createTextBox();
        box1.setTextPlaceholder(TextPlaceholder.TITLE);
        box1.setText("HSLF in Action - 3");
        box1.setAnchor(new Rectangle(36, 20, 648, 50));

        TextBox<?,?> box2 = slide.createTextBox();
        box2.getTextParagraphs().get(0).getTextRuns().get(0).setFontSize(18d);
        box2.setText("PPGraphics2D: PowerPoint Graphics2D driver");
        box2.setAnchor(new Rectangle(178, 70, 387, 30));

        TextBox<?,?> box3 = slide.createTextBox();
        TextRun rt3 = box3.getTextParagraphs().get(0).getTextRuns().get(0);
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
    }

    public static void slide10(SlideShow<?,?> ppt) throws IOException {
        //bar chart data. The first value is the bar color, the second is the width
        Object[] def = new Object[]{
            Color.yellow, 100,
            Color.green, 150,
            Color.gray, 75,
            Color.red, 200,
        };

        Slide<?,?> slide = ppt.createSlide();

        GroupShape<?,?> group = slide.createGroup();
        //define position of the drawing in the slide
        Rectangle bounds = new java.awt.Rectangle(200, 100, 350, 300);
        group.setAnchor(bounds);
        Graphics2D graphics = new SLGraphics(group);

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

    public static void slide11(SlideShow<?,?> ppt) throws IOException {
        Slide<?,?> slide = ppt.createSlide();

        TextBox<?,?> box1 = slide.createTextBox();
        box1.setTextPlaceholder(TextPlaceholder.TITLE);
        box1.setText("HSLF Development Plans");
        box1.setAnchor(new Rectangle(36, 21, 648, 90));

        TextBox<?,?> box2 = slide.createTextBox();
        box2.setTextPlaceholder(TextPlaceholder.BODY);
        box2.setText(
            "Support for more PowerPoint functionality\r" +
            "Rendering slides into java.awt.Graphics2D\r" +
                "A way to export slides into images or other formats\r" +
            "Integration with Apache FOP - Formatting Objects Processor\r" +
                "Transformation of XSL-FO into PPT\r" +
                "PPT2PDF transcoder"
        );

        List<? extends TextParagraph<?,?,?>> tp = box2.getTextParagraphs();
        for (int i : new byte[]{0,1,3}) {
            tp.get(i).getTextRuns().get(0).setFontSize(28d);
        }
        for (int i : new byte[]{2,4,5}) {
            tp.get(i).getTextRuns().get(0).setFontSize(24d);
            tp.get(i).setIndentLevel(1);
        }
        
        box2.setAnchor(new Rectangle(36, 126, 648, 400));
    }

    public static void slide12(SlideShow<?,?> ppt) throws IOException {
        Slide<?,?> slide = ppt.createSlide();

        TextBox<?,?> box1 = slide.createTextBox();
        box1.setTextPlaceholder(TextPlaceholder.CENTER_TITLE);
        box1.setText("Questions?");
        box1.setAnchor(new Rectangle(54, 167, 612, 115));

        TextBox<?,?> box2 = slide.createTextBox();
        box2.setTextPlaceholder(TextPlaceholder.CENTER_BODY);
        box2.setText(
                "http://poi.apache.org/hslf/\r" +
                "http://people.apache.org/~yegor");
        box2.setAnchor(new Rectangle(108, 306, 504, 138));
    }
}
