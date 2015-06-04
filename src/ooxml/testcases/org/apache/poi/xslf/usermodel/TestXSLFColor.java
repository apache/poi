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
import org.openxmlformats.schemas.drawingml.x2006.main.*;

import java.awt.Color;

/**
 * @author Yegor Kozlov
 */
public class TestXSLFColor extends TestCase {

    public void testGetters() {
        CTColor xml = CTColor.Factory.newInstance();
        CTSRgbColor c = xml.addNewSrgbClr();
        c.setVal(new byte[]{(byte)0xFF, 0, 0});

        XSLFColor color = new XSLFColor(xml, null, null);

        assertEquals(-1, color.getAlpha());
        c.addNewAlpha().setVal(50000);
        assertEquals(50, color.getAlpha());

        assertEquals(-1, color.getAlphaMod());
        c.addNewAlphaMod().setVal(50000);
        assertEquals(50, color.getAlphaMod());

        assertEquals(-1, color.getAlphaOff());
        c.addNewAlphaOff().setVal(50000);
        assertEquals(50, color.getAlphaOff());

        assertEquals(-1, color.getLumMod());
        c.addNewLumMod().setVal(50000);
        assertEquals(50, color.getLumMod());

        assertEquals(-1, color.getLumOff());
        c.addNewLumOff().setVal(50000);
        assertEquals(50, color.getLumOff());

        assertEquals(-1, color.getSat());
        c.addNewSat().setVal(50000);
        assertEquals(50, color.getSat());

        assertEquals(-1, color.getSatMod());
        c.addNewSatMod().setVal(50000);
        assertEquals(50, color.getSatMod());

        assertEquals(-1, color.getSatOff());
        c.addNewSatOff().setVal(50000);
        assertEquals(50, color.getSatOff());

        assertEquals(-1, color.getRed());
        c.addNewRed().setVal(50000);
        assertEquals(50, color.getRed());

        assertEquals(-1, color.getGreen());
        c.addNewGreen().setVal(50000);
        assertEquals(50, color.getGreen());

        assertEquals(-1, color.getBlue());
        c.addNewBlue().setVal(50000);
        assertEquals(50, color.getRed());

        assertEquals(-1, color.getShade());
        c.addNewShade().setVal(50000);
        assertEquals(50, color.getShade());

        assertEquals(-1, color.getTint());
        c.addNewTint().setVal(50000);
        assertEquals(50, color.getTint());
    }

    public void testHSL() {
        CTColor xml = CTColor.Factory.newInstance();
        CTHslColor c = xml.addNewHslClr();
        c.setHue2(14400000);
        c.setSat2(100000);
        c.setLum2(50000);

        XSLFColor color = new XSLFColor(xml, null, null);
        assertEquals(new Color(128, 00, 00), color.getColor());
    }

    public void testSRgb() {
        CTColor xml = CTColor.Factory.newInstance();
        xml.addNewSrgbClr().setVal(new byte[]{ (byte)0xFF, (byte)0xFF, 0});

        XSLFColor color = new XSLFColor(xml, null, null);
        assertEquals(new Color(0xFF, 0xFF, 0), color.getColor());
    }

    public void testSchemeColor() {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFTheme theme = ppt.createSlide().getTheme();

        CTColor xml = CTColor.Factory.newInstance();
        xml.addNewSchemeClr().setVal(STSchemeColorVal.ACCENT_2);

        XSLFColor color = new XSLFColor(xml, theme, null);
        // accent2 is theme1.xml is <a:srgbClr val="C0504D"/>
        assertEquals(Color.decode("0xC0504D"), color.getColor());

        xml = CTColor.Factory.newInstance();
        xml.addNewSchemeClr().setVal(STSchemeColorVal.LT_1);
        color = new XSLFColor(xml, theme, null);
        // <a:sysClr val="window" lastClr="FFFFFF"/>
        assertEquals(Color.decode("0xFFFFFF"), color.getColor());

        xml = CTColor.Factory.newInstance();
        xml.addNewSchemeClr().setVal(STSchemeColorVal.DK_1);
        color = new XSLFColor(xml, theme, null);
        // <a:sysClr val="windowText" lastClr="000000"/>
        assertEquals(Color.decode("0x000000"), color.getColor());
    }

    public void testPresetColor() {
        CTColor xml = CTColor.Factory.newInstance();
        xml.addNewPrstClr().setVal(STPresetColorVal.AQUAMARINE);
        XSLFColor color = new XSLFColor(xml, null, null);
        assertEquals(new Color(127, 255, 212), color.getColor());


        for(String colorName : XSLFColor.presetColors.keySet()){
            xml = CTColor.Factory.newInstance();
            STPresetColorVal.Enum val = STPresetColorVal.Enum.forString(colorName);
            assertNotNull(colorName, val);
            xml.addNewPrstClr().setVal(val);
            color = new XSLFColor(xml, null, null);
            assertEquals(XSLFColor.presetColors.get(colorName), color.getColor());
        }
    }

    public void testSys() {
        CTColor xml = CTColor.Factory.newInstance();
        CTSystemColor sys = xml.addNewSysClr();
        sys.setVal(STSystemColorVal.GRAY_TEXT);
        XSLFColor color = new XSLFColor(xml, null, null);
        assertEquals(Color.black, color.getColor());

        xml = CTColor.Factory.newInstance();
        sys = xml.addNewSysClr();
        sys.setLastClr(new byte[]{(byte)0xFF, 0, 0});
        color = new XSLFColor(xml, null, null);
        assertEquals(Color.red, color.getColor());
    }

}