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

import junit.framework.TestCase;

import org.apache.poi.xslf.XSLFTestDataSamples;

/**
 * test common properties for sheets (slides, masters, layouts, etc.)
 *
 * @author Yegor Kozlov
 */
public class TestXSLFTheme extends TestCase {
    public void testRead(){
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();
        XSLFTheme theme = slide.getSlideLayout().getSlideMaster().getTheme();
        assertNotNull(theme);
        
        assertEquals("Office Theme", theme.getName());
        XSLFColor accent1 = theme.getColor("accent1");
        assertNotNull(accent1);
    }
}
