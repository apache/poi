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
package org.apache.poi.xslf.usermodel;

import junit.framework.TestCase;

import java.awt.*;

/**
 * @author Yegor Kozlov
 */
public class TestXSLFTextRun extends TestCase {

    public void testRunProperties(){
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();
        XSLFTextShape sh = slide.createAutoShape();

        XSLFTextRun r = sh.addNewTextParagraph().addNewTextRun();
        assertEquals("en-US", r.getRPr().getLang());

        assertEquals(0., r.getCharacterSpacing());
        r.setCharacterSpacing(3);
        assertEquals(3., r.getCharacterSpacing());
        r.setCharacterSpacing(-3);
        assertEquals(-3., r.getCharacterSpacing());
        r.setCharacterSpacing(0);
        assertEquals(0., r.getCharacterSpacing());
        assertFalse(r.getRPr().isSetSpc());

        assertEquals(Color.black, r.getFontColor());
        r.setFontColor(Color.red);
        assertEquals(Color.red, r.getFontColor());

        assertEquals("Calibri", r.getFontFamily());
        r.setFontFamily("Arial");
        assertEquals("Arial", r.getFontFamily());

        assertEquals(18.0, r.getFontSize());
        r.setFontSize(13.0);
        assertEquals(13.0, r.getFontSize());

        assertEquals(false, r.isSuperscript());
        r.setSuperscript(true);
        assertEquals(true, r.isSuperscript());
        r.setSuperscript(false);
        assertEquals(false, r.isSuperscript());

        assertEquals(false, r.isSubscript());
        r.setSubscript(true);
        assertEquals(true, r.isSubscript());
        r.setSubscript(false);
        assertEquals(false, r.isSubscript());
    }
}
