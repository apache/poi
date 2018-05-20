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

package org.apache.poi.sl;

import static org.apache.poi.sl.SLCommonUtils.xslfOnly;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.POIDataSamples;
import org.apache.poi.common.usermodel.fonts.FontGroup;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.StrokeStyle.LineDash;
import org.apache.poi.sl.usermodel.TextBox;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextRun;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Test rendering - specific to font handling
 */
public class TestFonts {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    private static final String JPTEXT =
        "\u3061\u3087\u3063\u3068\u65E9\u3044\u3051\u3069T\u30B7\u30E3\u30C4\u304C\u7740\u305F\u304F\u306A" +
        "\u308B\u5B63\u7BC0\u2661\u304A\u6BCD\u3055\u3093\u306E\u5F71\u97FF\u304B\u3001\u975E\u5E38\u306B" +
        "\u6050\u7ADC\u304C\u5927\u597D\u304D\u3067\u3059\u3002\u3082\u3046\u98FC\u3044\u305F\u3044\u304F" +
        "\u3089\u3044\u5927\u597D\u304D\u3067\u3059\u3002#\u30B8\u30E5\u30E9\u30B7\u30C3\u30AF\u30EF\u30FC" +
        "\u30EB\u30C9 \u306E\u30E9\u30D7\u30C8\u30EB4\u59C9\u59B9\u3068\u304B\u6FC0\u7684\u306B\u53EF\u611B" +
        "\u304F\u3066\u53EF\u611B\u304F\u3066\u53EF\u611B\u304F\u3066\u53EF\u611B\u3044\u3067\u3059\u3002" +
        "\u3081\u308D\u3081\u308D\u3001\u5927\u597D\u304D\u2661\u304A\u6BCD\u3055\u3093\u3082\u6050\u7ADC" +
        "\u304C\u597D\u304D\u3067\u3001\u5C0F\u3055\u3044\u9803\u3001\u53E4\u4EE3\u751F\u7269\u306E\u56F3" +
        "\u9451\u3092\u4E00\u7DD2\u306B\u898B\u3066\u305F\u306E\u601D\u3044\u51FA\u3059\u301C\u3068\u3044";

    private static final String INIT_FONTS[] = { "mona.ttf" };

    // currently linux and mac return quite different values
    private static final int[] expected_sizes = {
            304, // windows 10, 1080p, MS Office 2016, system text scaling 100% instead of default 125%
            306, // Windows 10, 15.6" 3840x2160
            311, 312, 313, 318,
            348, // Windows 10, 15.6" 3840x2160
            362, // Windows 10, 13.3" 1080p high-dpi
            372, // Ubuntu Xenial, 15", 1680x1050
            377, 391, 398, 399, // Mac
            406  // Ubuntu Xenial, 15", 1680x1050
    };

    @BeforeClass
    public static void initGE() throws FontFormatException, IOException {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String s : INIT_FONTS) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, _slTests.getFile(s));
            ge.registerFont(font);
        }
    }

    @Test
    public void resizeToFitTextHSLF() throws IOException {
        assumeFalse(xslfOnly());
        SlideShow<?,?> ppt = new HSLFSlideShow();
        resizeToFitText(ppt);
        ppt.close();
    }

    @Test
    public void resizeToFitTextXSLF() throws IOException {
        SlideShow<?,?> ppt = new XMLSlideShow();
        resizeToFitText(ppt);
        ppt.close();
    }

    private void resizeToFitText(SlideShow<?,?> slideshow) throws IOException {
        Slide<?,?> sld = slideshow.createSlide();
        TextBox<?,?> tb = sld.createTextBox();
        tb.setAnchor(new Rectangle(50, 50, 200, 50));
        tb.setStrokeStyle(Color.black, LineDash.SOLID, 3);
        tb.setText(JPTEXT);

        setFont(tb, "NoSuchFont", FontGroup.LATIN);

        Dimension pgsize = slideshow.getPageSize();
        int width = (int)pgsize.getWidth();
        int height = (int)pgsize.getHeight();

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = img.createGraphics();

        Map<String,String> fallbackMap = new HashMap<>();
        fallbackMap.put("NoSuchFont", "Mona");
        // in XSLF the fonts default to the theme fonts (Calibri), if the font group is not overridden
        // see XSLFTextRun.XSLFTextInfo.getCTTextFont
        fallbackMap.put("Calibri", "Mona");
        graphics.setRenderingHint(Drawable.FONT_FALLBACK, fallbackMap);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        DrawFactory.getInstance(graphics).fixFonts(graphics);

        tb.resizeToFitText(graphics);
        graphics.dispose();

        Rectangle2D anc = tb.getAnchor();
        // ignore font metrics differences on windows / linux (... hopefully ...)
        int tbHeight = (int)anc.getHeight();
        boolean found = Arrays.binarySearch(expected_sizes, tbHeight) > -1;
        assertTrue(tbHeight+" wasn't within the expected sizes: "+Arrays.toString(expected_sizes), found);
    }

    private void setFont(TextBox<?,?> tb, String fontFamily, FontGroup fontGroup) {
        // TODO: set east asian font family - MS Office uses "MS Mincho" or "MS Gothic" as a fallback
        // see https://stackoverflow.com/questions/26063828 for good explanation about the font metrics
        // differences on different environments
        for (TextParagraph<?,?,? extends TextRun> p : tb.getTextParagraphs()) {
            for (TextRun r : p.getTextRuns()) {
                r.setFontFamily(fontFamily, fontGroup);
            }
        }
    }
}
