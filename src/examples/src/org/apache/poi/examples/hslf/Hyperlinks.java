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

package org.apache.poi.examples.hslf;

import java.io.FileInputStream;
import java.util.List;
import java.util.Locale;

import org.apache.poi.hslf.usermodel.HSLFHyperlink;
import org.apache.poi.hslf.usermodel.HSLFSimpleShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hslf.usermodel.HSLFTextRun;

/**
 * Demonstrates how to read hyperlinks from  a presentation
 */
@SuppressWarnings({"java:S106", "java:S4823"})
public final class Hyperlinks {

    private Hyperlinks() {}

    public static void main(String[] args) throws Exception {
        for (String arg : args) {
            try (FileInputStream is = new FileInputStream(arg);
                 HSLFSlideShow ppt = new HSLFSlideShow(is)) {

                for (HSLFSlide slide : ppt.getSlides()) {
                    System.out.println("\nslide " + slide.getSlideNumber());

                    // read hyperlinks from the slide's text runs
                    System.out.println("- reading hyperlinks from the text runs");
                    slide.getTextParagraphs().stream().
                        flatMap(List::stream).
                        map(HSLFTextParagraph::getTextRuns).
                        flatMap(List::stream).
                        forEach(run -> out(run.getHyperlink(), run));

                    // in PowerPoint you can assign a hyperlink to a shape without text,
                    // for example to a Line object. The code below demonstrates how to
                    // read such hyperlinks
                    System.out.println("- reading hyperlinks from the slide's shapes");
                    slide.getShapes().stream().
                        filter(sh -> sh instanceof HSLFSimpleShape).
                        forEach(sh -> out(((HSLFSimpleShape) sh).getHyperlink(), null));
                }
            }
        }
    }

    private static void out(HSLFHyperlink link, HSLFTextRun run) {
        if (link == null) {
            return;
        }
        String rawText = run == null ? null : run.getRawText();
        //in ppt end index is inclusive
        String formatStr = "title: %1$s, address: %2$s" + (rawText == null ? "" : ", start: %3$s, end: %4$s, substring: %5$s");
        String line = String.format(Locale.ROOT, formatStr, link.getLabel(), link.getAddress(), link.getStartIndex(), link.getEndIndex(), rawText);
        System.out.println(line);
    }
}
