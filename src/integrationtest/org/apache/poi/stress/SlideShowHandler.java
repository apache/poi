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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.usermodel.GroupShape;
import org.apache.poi.sl.usermodel.Notes;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.SimpleShape;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextRun;
import org.apache.poi.sl.usermodel.TextShape;
import org.junit.jupiter.api.Assumptions;
import org.junit.platform.commons.util.ExceptionUtils;

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

            for (Shape<?,?> shape : s) {
                readShapes(shape);
            }

            Notes<?, ?> notes = s.getNotes();
            if(notes != null) {
                for (Shape<?, ?> shape : notes) {
                    readShapes(shape);
                }
            }

            for (Shape<?,?> shape : s.getMasterSheet()) {
                readShapes(shape);
            }
        }
    }

    private void readShapes(Shape<?,?> s) {
        // recursively walk group-shapes
        if(s instanceof GroupShape) {
            GroupShape<? extends Shape<?,?>, ?> shapes = (GroupShape<? extends Shape<?,?>, ?>) s;
            for (Shape<? extends Shape<?,?>, ?> shape : shapes) {
                readShapes(shape);
            }
        }

        if(s instanceof SimpleShape) {
            SimpleShape<?, ?> simpleShape = (SimpleShape<?, ?>) s;

            simpleShape.getFillColor();
            simpleShape.getFillStyle();
            simpleShape.getStrokeStyle();
            simpleShape.getLineDecoration();
        }

        readText(s);
    }

    private void readText(Shape<?,?> s) {
        if (s instanceof TextShape) {
            for (TextParagraph<?,?,?> tp : (TextShape<?,?>)s) {
                for (TextRun tr : tp) {
                    tr.getRawText();
                }
            }
        }
    }

    private void readPictures(SlideShow<?,?> ss) {
        for (PictureData pd : ss.getPictureData()) {
            Dimension dim = pd.getImageDimension();
            assertTrue( dim.getHeight() >= 0, "Expecting a valid height, but had an image with height: " + dim.getHeight() );
            assertTrue( dim.getWidth() >= 0, "Expecting a valid width, but had an image with width: " + dim.getWidth() );
        }
    }

    private void renderSlides(SlideShow<?,?> ss) {
        Dimension pgSize = ss.getPageSize();

        for (Slide<?,?> s : ss.getSlides()) {
            BufferedImage img = new BufferedImage(pgSize.width, pgSize.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = img.createGraphics();

            // default rendering options
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            graphics.setRenderingHint(Drawable.BUFFERED_IMAGE, new WeakReference<>(img));

            try {
                // draw stuff
                s.draw(graphics);
            } catch (ArrayIndexOutOfBoundsException e) {
                // We saw exceptions with JDK 8 on Windows in the Jenkins CI which
                // seem to only be triggered by some font (maybe Calibri?!)
                // We cannot avoid this, so let's try to not make the tests fail in this case
                Assumptions.assumeFalse(
                        e.getMessage().equals("-1") &&
                        ExceptionUtils.readStackTrace(e).contains("ExtendedTextSourceLabel.getJustificationInfos"),
                        "JDK sometimes fails at this point on some fonts on Windows machines, but we " +
                                "should not fail the build because of this: " + ExceptionUtils.readStackTrace(e));

                throw e;
            }

            graphics.dispose();
            img.flush();
        }
    }
}