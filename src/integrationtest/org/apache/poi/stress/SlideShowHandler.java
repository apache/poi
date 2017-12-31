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
package org.apache.poi.stress;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextRun;
import org.apache.poi.sl.usermodel.TextShape;

public abstract class SlideShowHandler extends POIFSFileHandler {
    public void handleSlideShow(SlideShow<?,?> ss) throws IOException {
        renderSlides(ss);

        readContent(ss);
        readPictures(ss);

        // write out the file
        ByteArrayOutputStream out = writeToArray(ss);
        
        readContent(ss);

        // read in the written file
        try (SlideShow<?, ?> read = SlideShowFactory.create(new ByteArrayInputStream(out.toByteArray()))) {
            assertNotNull(read);
            readContent(read);
        }
    }

    private ByteArrayOutputStream writeToArray(SlideShow<?,?> ss) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ss.write(stream);
        } finally {
            stream.close();
        }
        
        return stream;
    }

    
    private void readContent(SlideShow<?,?> ss) {
        for (Slide<?,?> s : ss.getSlides()) {
            s.getTitle();
            readText(s);
            readText(s.getNotes());
            readText(s.getMasterSheet());
        }
    }
    
    private void readText(ShapeContainer<?,?> sc) {
        if (sc == null) return;
        for (Shape<?,?> s : sc) {
            if (s instanceof TextShape) {
                for (TextParagraph<?,?,?> tp : (TextShape<?,?>)s) {
                    for (TextRun tr : tp) {
                        tr.getRawText();
                    }
                }
            }
        }
    }
    
    private void readPictures(SlideShow<?,?> ss) {
        for (PictureData pd : ss.getPictureData()) {
            Dimension dim = pd.getImageDimension();
            assertTrue(dim.getHeight() >= 0);
            assertTrue(dim.getWidth() >= 0);
        }
    }
    
    private void renderSlides(SlideShow<?,?> ss) {
        Dimension pgsize = ss.getPageSize();

        for (Slide<?,?> s : ss.getSlides()) {
            BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = img.createGraphics();
            DrawFactory.getInstance(graphics).fixFonts(graphics);

            // default rendering options
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    
            // draw stuff
            s.draw(graphics);
            
            graphics.dispose();
            img.flush();
        }
    }
}