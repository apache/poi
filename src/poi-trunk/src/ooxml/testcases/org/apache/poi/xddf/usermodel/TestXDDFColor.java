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

package org.apache.poi.xddf.usermodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFTheme;
import org.junit.Test;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTScRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSystemColor;
import org.openxmlformats.schemas.drawingml.x2006.main.STPresetColorVal;
import org.openxmlformats.schemas.drawingml.x2006.main.STSchemeColorVal;
import org.openxmlformats.schemas.drawingml.x2006.main.STSystemColorVal;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

public class TestXDDFColor {
    private static final String XMLNS = "xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\"/>";

    @Test
    public void testSchemeColor() throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            XSLFTheme theme = ppt.createSlide().getTheme();

            XDDFColor color = XDDFColor.forColorContainer(getThemeColor(theme, STSchemeColorVal.ACCENT_2));
            // accent2 in theme1.xml is <a:srgbClr val="C0504D"/>
            Diff d1 = DiffBuilder.compare(Input.fromString("<a:srgbClr val=\"C0504D\" " + XMLNS))
                    .withTest(color.getColorContainer().toString()).build();
            assertFalse(d1.toString(), d1.hasDifferences());

            color = XDDFColor.forColorContainer(getThemeColor(theme, STSchemeColorVal.LT_1));
            Diff d2 = DiffBuilder.compare(Input.fromString("<a:sysClr lastClr=\"FFFFFF\" val=\"window\" " + XMLNS))
                    .withTest(color.getColorContainer().toString()).build();
            assertFalse(d2.toString(), d2.hasDifferences());

            color = XDDFColor.forColorContainer(getThemeColor(theme, STSchemeColorVal.DK_1));
            Diff d3 = DiffBuilder.compare(Input.fromString("<a:sysClr lastClr=\"000000\" val=\"windowText\" " + XMLNS))
                    .withTest(color.getColorContainer().toString()).build();
            assertFalse(d3.toString(), d3.hasDifferences());
        }
    }

    private CTColor getThemeColor(XSLFTheme theme, STSchemeColorVal.Enum value) {
        // find referenced CTColor in the theme
        return theme.getCTColor(value.toString());
    }

    @Test
    public void testPreset() {
        CTColor xml = CTColor.Factory.newInstance();
        xml.addNewPrstClr().setVal(STPresetColorVal.AQUAMARINE);
        String expected = XDDFColor.forColorContainer(xml).getXmlObject().toString();
        XDDFColor built = XDDFColor.from(PresetColor.AQUAMARINE);
        assertEquals(expected, built.getXmlObject().toString());
    }

    @Test
    public void testSystemDefined() {
        CTColor xml = CTColor.Factory.newInstance();
        CTSystemColor sys = xml.addNewSysClr();
        sys.setVal(STSystemColorVal.CAPTION_TEXT);
        String expected = XDDFColor.forColorContainer(xml).getXmlObject().toString();

        XDDFColor built = new XDDFColorSystemDefined(sys, xml);
        assertEquals(expected, built.getXmlObject().toString());

        built = XDDFColor.from(SystemColor.CAPTION_TEXT);
        assertEquals(expected, built.getXmlObject().toString());
    }

    @Test
    public void testRgbBinary() {
        CTColor xml = CTColor.Factory.newInstance();
        CTSRgbColor color = xml.addNewSrgbClr();
        byte[] bs = new byte[]{-1, -1, -1};
        color.setVal(bs);
        String expected = XDDFColor.forColorContainer(xml).getXmlObject().toString();

        XDDFColor built = XDDFColor.from(bs);
        assertEquals(expected, built.getXmlObject().toString());
        assertEquals("FFFFFF", ((XDDFColorRgbBinary)built).toRGBHex());
    }

    @Test
    public void testRgbPercent() {
        CTColor xml = CTColor.Factory.newInstance();
        CTScRgbColor color = xml.addNewScrgbClr();
        color.setR(0);
        color.setG(0);
        color.setB(0);
        String expected = XDDFColor.forColorContainer(xml).getXmlObject().toString();

        XDDFColorRgbPercent built = (XDDFColorRgbPercent) XDDFColor.from(-1, -1, -1);
        assertEquals(expected, built.getXmlObject().toString());
        assertEquals("000000", built.toRGBHex());

        color.setR(100_000);
        color.setG(100_000);
        color.setB(100_000);
        expected = XDDFColor.forColorContainer(xml).getXmlObject().toString();

        built = (XDDFColorRgbPercent) XDDFColor.from(654321, 654321, 654321);
        assertEquals(expected, built.getXmlObject().toString());
        assertEquals("FFFFFF", built.toRGBHex());
    }
}
