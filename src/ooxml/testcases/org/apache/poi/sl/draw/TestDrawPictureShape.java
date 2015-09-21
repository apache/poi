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
package org.apache.poi.sl.draw;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Dimension;
import java.awt.Rectangle;

import org.apache.poi.POIDataSamples;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.PictureShape;
import org.apache.poi.sl.usermodel.RectAlign;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.junit.Test;

public class TestDrawPictureShape {
    final static POIDataSamples ssSamples = POIDataSamples.getSlideShowInstance();

    @Test
    public void testResize() throws Exception {
        String files[] = { "pictures.ppt", "shapes.pptx" };
        for (String file : files) {
            SlideShow<?,?> ss = SlideShowFactory.create(ssSamples.getFile(file));
            Slide<?,?> slide = ss.getSlides().get(0);
            PictureShape<?,?> picShape = null;
            for (Shape<?,?> shape : slide.getShapes()) {
                if (shape instanceof PictureShape) {
                    picShape = (PictureShape<?,?>)shape;
                    break;
                }
            }
            assertNotNull(picShape);
            PictureData pd = picShape.getPictureData();
            Dimension dimPd = pd.getImageDimension();
            new DrawPictureShape(picShape).resize();
            Dimension dimShape = picShape.getAnchor().getSize();
            assertEquals(dimPd, dimShape);
            
            int newWidth = (int)(dimPd.getWidth()*(100d/dimPd.getHeight()));
            // ... -1 is a rounding error
            Rectangle expRect = new Rectangle(50+300-newWidth-1, 50, newWidth, 100);
            Rectangle target = new Rectangle(50,50,300,100);
            new DrawPictureShape(picShape).resize(target, RectAlign.BOTTOM_RIGHT);
            Rectangle actRect = picShape.getAnchor();
            assertEquals(expRect, actRect);
        }
    }
    
    
}
