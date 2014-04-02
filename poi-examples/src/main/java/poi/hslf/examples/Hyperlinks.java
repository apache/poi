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
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.TextRun;
import org.apache.poi.hslf.model.Hyperlink;
import org.apache.poi.hslf.model.Shape;

import java.io.FileInputStream;

/**
 * Demonstrates how to read hyperlinks from  a presentation
 *
 * @author Yegor Kozlov
 */
public final class Hyperlinks {

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            FileInputStream is = new FileInputStream(args[i]);
            SlideShow ppt = new SlideShow(is);
            is.close();

            Slide[] slide = ppt.getSlides();
            for (int j = 0; j < slide.length; j++) {
                System.out.println("slide " + slide[j].getSlideNumber());

                //read hyperlinks from the slide's text runs
                System.out.println("reading hyperlinks from the text runs");
                TextRun[] txt = slide[j].getTextRuns();
                for (int k = 0; k < txt.length; k++) {
                    String text = txt[k].getText();
                    Hyperlink[] links = txt[k].getHyperlinks();
                    if(links != null) for (int l = 0; l < links.length; l++) {
                        Hyperlink link = links[l];
                        String title = link.getTitle();
                        String address = link.getAddress();
                        System.out.println("  " + title);
                        System.out.println("  " + address);
                        String substring = text.substring(link.getStartIndex(), link.getEndIndex()-1);//in ppt end index is inclusive
                        System.out.println("  " + substring);
                    }
                }

                //in PowerPoint you can assign a hyperlink to a shape without text,
                //for example to a Line object. The code below demonstrates how to
                //read such hyperlinks
                System.out.println("  reading hyperlinks from the slide's shapes");
                Shape[] sh = slide[j].getShapes();
                for (int k = 0; k < sh.length; k++) {
                    Hyperlink link = sh[k].getHyperlink();
                    if(link != null)  {
                        String title = link.getTitle();
                        String address = link.getAddress();
                        System.out.println("  " + title);
                        System.out.println("  " + address);
                    }
                }
            }

        }

   }
}
