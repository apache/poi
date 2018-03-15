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

package org.apache.poi.hslf.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.junit.Test;

/**
 * Test <code>MovieShape</code> object.
 */
public final class TestMovieShape {

    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    @Test
    public void testCreate() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow();

        HSLFSlide slide = ppt.createSlide();

        String path = "/test-movie.mpg";
        int movieIdx = ppt.addMovie(path, MovieShape.MOVIE_MPEG);
        HSLFPictureData thumbnailData = ppt.addPicture(_slTests.readFile("tomcat.png"), PictureType.PNG);

        MovieShape shape = new MovieShape(movieIdx, thumbnailData);
        shape.setAnchor(new Rectangle2D.Double(300,225,120,90));
        slide.addShape(shape);

        assertEquals(path, shape.getPath());
        assertTrue(shape.isAutoPlay());
        shape.setAutoPlay(false);
        assertFalse(shape.isAutoPlay());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);

        ppt = new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray()));
        slide = ppt.getSlides().get(0);
        shape = (MovieShape)slide.getShapes().get(0);
        assertEquals(path, shape.getPath());
        assertFalse(shape.isAutoPlay());
    }
}
