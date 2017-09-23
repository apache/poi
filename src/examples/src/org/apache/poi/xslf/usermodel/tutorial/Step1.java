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

package org.apache.poi.xslf.usermodel.tutorial;

import java.io.FileInputStream;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

/**
 * Reading a .pptx presentation and printing basic shape properties
 */
public class Step1 {

    public static void main(String[] args) throws Exception {
        if(args.length == 0)  {
            System.out.println("Input file is required");
            return;
        }

        FileInputStream fis = new FileInputStream(args[0]);
        try (XMLSlideShow ppt = new XMLSlideShow(fis)) {
            fis.close();

            for (XSLFSlide slide : ppt.getSlides()) {
                System.out.println("Title: " + slide.getTitle());

                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape tsh = (XSLFTextShape) shape;
                        for (XSLFTextParagraph p : tsh) {
                            System.out.println("Paragraph level: " + p.getIndentLevel());
                            for (XSLFTextRun r : p) {
                                System.out.println(r.getRawText());
                                System.out.println("  bold: " + r.isBold());
                                System.out.println("  italic: " + r.isItalic());
                                System.out.println("  underline: " + r.isUnderlined());
                                System.out.println("  font.family: " + r.getFontFamily());
                                System.out.println("  font.size: " + r.getFontSize());
                                System.out.println("  font.color: " + r.getFontColor());
                            }
                        }
                    }
                }
            }
        }
    }
}
