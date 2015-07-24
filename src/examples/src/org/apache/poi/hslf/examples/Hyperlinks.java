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

import java.io.FileInputStream;
import java.util.List;

import org.apache.poi.hslf.usermodel.HSLFHyperlink;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;

/**
 * Demonstrates how to read hyperlinks from  a presentation
 *
 * @author Yegor Kozlov
 */
public final class Hyperlinks {

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            FileInputStream is = new FileInputStream(args[i]);
            HSLFSlideShow ppt = new HSLFSlideShow(is);
            is.close();

            for (HSLFSlide slide : ppt.getSlides()) {
                System.out.println("\nslide " + slide.getSlideNumber());

                // read hyperlinks from the slide's text runs
                System.out.println("- reading hyperlinks from the text runs");
                for (List<HSLFTextParagraph> txtParas : slide.getTextParagraphs()) {
                    List<HSLFHyperlink> links = HSLFHyperlink.find(txtParas);
                    String text = HSLFTextParagraph.getRawText(txtParas);

                    for (HSLFHyperlink link : links) {
                        System.out.println(toStr(link, text));
                    }
                }

                // in PowerPoint you can assign a hyperlink to a shape without text,
                // for example to a Line object. The code below demonstrates how to
                // read such hyperlinks
                System.out.println("- reading hyperlinks from the slide's shapes");
                for (HSLFShape sh : slide.getShapes()) {
                    HSLFHyperlink link = HSLFHyperlink.find(sh);
                    if (link == null) continue;
                    System.out.println(toStr(link, null));
                }
            }
        }
   }

    static String toStr(HSLFHyperlink link, String rawText) {
        //in ppt end index is inclusive
        String formatStr = "title: %1$s, address: %2$s" + (rawText == null ? "" : ", start: %3$s, end: %4$s, substring: %5$s");
        String substring = (rawText == null) ? "" : rawText.substring(link.getStartIndex(), link.getEndIndex()-1);
        return String.format(formatStr, link.getTitle(), link.getAddress(), link.getStartIndex(), link.getEndIndex(), substring);
    }
}
