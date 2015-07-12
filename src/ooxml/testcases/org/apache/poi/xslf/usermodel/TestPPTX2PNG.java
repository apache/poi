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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.util.JvmBugs;
import org.apache.poi.xslf.XSLFTestDataSamples;
import org.junit.Test;

/**
 * Date: 10/26/11
 *
 * @author Yegor Kozlov
 */
public class TestPPTX2PNG {
    @Test
    public void render() throws Exception {
        String[] testFiles = {"backgrounds.pptx","layouts.pptx", "sample.pptx", "shapes.pptx", "themes.pptx",};
        for(String sampleFile : testFiles){
            XMLSlideShow pptx = XSLFTestDataSamples.openSampleDocument(sampleFile);
            Dimension pg = pptx.getPageSize();
            int slideNo=1;
            for(XSLFSlide slide : pptx.getSlides()){
                BufferedImage img = new BufferedImage(pg.width, pg.height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = img.createGraphics();
                fixFonts(graphics);
                slide.draw(graphics);
                // ImageIO.write(img, "PNG", new File("build/tmp/"+sampleFile.replaceFirst(".pptx?", "-")+slideNo+".png"));
                slideNo++;
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void fixFonts(Graphics2D graphics) {
        if (!JvmBugs.hasLineBreakMeasurerBug()) return;
        Map<String,String> fontMap = (Map<String,String>)graphics.getRenderingHint(Drawable.FONT_MAP);
        if (fontMap == null) fontMap = new HashMap<String,String>();
        fontMap.put("Calibri", "Lucida Sans");
        fontMap.put("Cambria", "Lucida Bright");
        graphics.setRenderingHint(Drawable.FONT_MAP, fontMap);        
    }
}
