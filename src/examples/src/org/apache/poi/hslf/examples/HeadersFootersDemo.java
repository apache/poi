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
import org.apache.poi.hslf.model.HeadersFooters;
import org.apache.poi.hslf.model.Slide;

import java.io.FileOutputStream;

/**
 * Demonstrates how to set headers / footers
 *
 * @author Yegor Kozlov
 */
public class HeadersFootersDemo {
    public static void main(String[] args) throws Exception {
        SlideShow ppt = new SlideShow();
        
        HeadersFooters slideHeaders = ppt.getSlideHeadersFooters();
        slideHeaders.setFootersText("Created by POI-HSLF");
        slideHeaders.setSlideNumberVisible(true);
        slideHeaders.setDateTimeText("custom date time");

        HeadersFooters notesHeaders = ppt.getNotesHeadersFooters();
        notesHeaders.setFootersText("My notes footers");
        notesHeaders.setHeaderText("My notes header");

        Slide slide = ppt.createSlide();

        FileOutputStream out = new FileOutputStream("headers_footers.ppt");
        ppt.write(out);
        out.close();

    }

}
