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

package org.apache.poi.hslf.usermodel;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assume.assumeTrue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.poi.POIDataSamples;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.util.TempFile;
import org.junit.Test;

/**
 * Test font rendering of alternative and fallback fonts
 */
public class TestFontRendering {
    private static POIDataSamples slTests = POIDataSamples.getSlideShowInstance();

    // @Ignore2("This fails on some systems because fonts are rendered slightly different")
    @Test
    public void bug55902mixedFontWithChineseCharacters() throws IOException, FontFormatException {
        // font files need to be downloaded first via
        // ant test-scratchpad-download-resources
        String fontFiles[][] = {
            // Calibri is not available on *nix systems, so we need to use another similar free font
            { "build/scratchpad-test-resources/Cabin-Regular.ttf", "mapped", "Calibri" },

            // use "MS PGothic" if available (Windows only) ...
            // for the junit test not all chars are rendered
            { "build/scratchpad-test-resources/mona.ttf", "fallback", "Cabin" }
        };
        
        // setup fonts (especially needed, when run under *nix systems)
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Map<String,String> fontMap = new HashMap<>();
        Map<String,String> fallbackMap = new HashMap<>();
        
        for (String fontFile[] : fontFiles) {
            File f = new File(fontFile[0]);
            assumeTrue("necessary font file "+f.getName()+" not downloaded.", f.exists());
            
            Font font = Font.createFont(Font.TRUETYPE_FONT, f);
            ge.registerFont(font);
            
            Map<String,String> map = ("mapped".equals(fontFile[1]) ? fontMap : fallbackMap);
            map.put(fontFile[2], font.getFamily());
        }
        
        InputStream is = slTests.openResourceAsStream("bug55902-mixedFontChineseCharacters.ppt");
        HSLFSlideShow ss = new HSLFSlideShow(is);
        is.close();
        
        Dimension pgsize = ss.getPageSize();
        
        HSLFSlide slide = ss.getSlides().get(0);
        
        // render it
        double zoom = 1;
        AffineTransform at = new AffineTransform();
        at.setToScale(zoom, zoom);
        
        BufferedImage imgActual = new BufferedImage((int)Math.ceil(pgsize.width*zoom), (int)Math.ceil(pgsize.height*zoom), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = imgActual.createGraphics();
        graphics.setRenderingHint(Drawable.FONT_FALLBACK, fallbackMap);
        graphics.setRenderingHint(Drawable.FONT_MAP, fontMap);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setTransform(at);                
        graphics.setPaint(Color.white);
        graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));
        slide.draw(graphics);             
        
        BufferedImage imgExpected = ImageIO.read(slTests.getFile("bug55902-mixedChars.png"));
        DataBufferByte expectedDB = (DataBufferByte)imgExpected.getRaster().getDataBuffer();
        DataBufferByte actualDB = (DataBufferByte)imgActual.getRaster().getDataBuffer();
        byte[] expectedData = expectedDB.getData(0);
        byte[] actualData = actualDB.getData(0);
        
        // allow to find out what the actual difference is in CI where this fails currently
        if(!Arrays.equals(expectedData, actualData)) {
            ImageIO.write(imgActual, "PNG", TempFile.createTempFile("TestFontRendering", ".png"));
        }
        
        assertArrayEquals("Expected to have matching raster-arrays, but found differences", expectedData, actualData);
        ss.close();
    }
}
