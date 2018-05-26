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
package org.apache.poi.sl.usermodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.TabStop.TabStopType;
import org.junit.Test;

public abstract class BaseTestSlideShow {
    protected static final POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
    
    public abstract SlideShow<?, ?> createSlideShow();

    public abstract SlideShow<?, ?> reopen(SlideShow<?, ?> show);
    
    @Test
    public void addPicture_File() throws IOException {
        SlideShow<?,?> show = createSlideShow();
        File f = slTests.getFile("clock.jpg");
        
        assertEquals(0, show.getPictureData().size());
        PictureData picture = show.addPicture(f, PictureType.JPEG);
        assertEquals(1, show.getPictureData().size());
        assertSame(picture, show.getPictureData().get(0));
        
        show.close();
    }
    
    @Test
    public void addPicture_Stream() throws IOException {
        SlideShow<?,?> show = createSlideShow();
        try {
            InputStream stream = slTests.openResourceAsStream("clock.jpg");
            try {
                assertEquals(0, show.getPictureData().size());
                PictureData picture = show.addPicture(stream, PictureType.JPEG);
                assertEquals(1, show.getPictureData().size());
                assertSame(picture, show.getPictureData().get(0));

            } finally {
                stream.close();
            }
        } finally {
            show.close();
        }
    }
    
    @Test
    public void addPicture_ByteArray() throws IOException {
        SlideShow<?,?> show = createSlideShow();
        byte[] data = slTests.readFile("clock.jpg");
        
        assertEquals(0, show.getPictureData().size());
        PictureData picture = show.addPicture(data, PictureType.JPEG);
        assertEquals(1, show.getPictureData().size());
        assertSame(picture, show.getPictureData().get(0));
        
        show.close();
    }
    
    @Test
    public void findPicture() throws IOException {
        SlideShow<?,?> show = createSlideShow();
        byte[] data = slTests.readFile("clock.jpg");
        
        assertNull(show.findPictureData(data));
        PictureData picture = show.addPicture(data, PictureType.JPEG);
        PictureData found = show.findPictureData(data);
        assertNotNull(found);
        assertEquals(picture, found);
        
        show.close();
    }
    
    @Test
    public void addTabStops() throws IOException {
        try (final SlideShow<?,?> show1 = createSlideShow()) {
            // first set the TabStops in the Master sheet
            final MasterSheet<?, ?> master1 = show1.getSlideMasters().get(0);
            final AutoShape<?, ?> master1_as = (AutoShape<?,?>)master1.getPlaceholder(Placeholder.BODY);
            final TextParagraph<?, ?, ? extends TextRun> master1_tp = master1_as.getTextParagraphs().get(0);
            master1_tp.clearTabStops();
            int i1 = 0;
            for (final TabStopType tst : TabStopType.values()) {
                master1_tp.addTabStops(10+i1*10, tst);
                i1++;
            }
            
            // then set it on a normal slide
            final Slide<?,?> slide1 = show1.createSlide();
            final AutoShape<?, ?> slide1_as = slide1.createAutoShape();
            slide1_as.setText("abc");
            slide1_as.setAnchor(new Rectangle2D.Double(100,100,100,100));
            final TextParagraph<?, ?, ? extends TextRun> slide1_tp = slide1_as.getTextParagraphs().get(0);
            slide1_tp.getTextRuns().get(0).setFontColor(new Color(0x563412));
            slide1_tp.clearTabStops();
            int i2 = 0;
            for (final TabStopType tst : TabStopType.values()) {
                slide1_tp.addTabStops(15+i2*5, tst);
                i2++;
            }
            
            try (final SlideShow<?, ?> show2 = reopen(show1)) {
                final MasterSheet<?, ?> master2 = show2.getSlideMasters().get(0);
                final AutoShape<?, ?> master2_as = (AutoShape<?,?>)master2.getPlaceholder(Placeholder.BODY);
                final TextParagraph<?, ?, ? extends TextRun> master2_tp = master2_as.getTextParagraphs().get(0);
                final List<? extends TabStop> master2_tabStops = master2_tp.getTabStops();
                assertNotNull(master2_tabStops);
                int i3 = 0;
                for (final TabStopType tst : TabStopType.values()) {
                    final TabStop ts = master2_tabStops.get(i3);
                    assertEquals(10+i3*10, ts.getPositionInPoints(), 0.0);
                    assertEquals(tst, ts.getType());
                    i3++;
                }
                
                
                final Slide<?,?> slide2 = show2.getSlides().get(0);
                final AutoShape<?,?> slide2_as = (AutoShape<?,?>)slide2.getShapes().get(0);
                final TextParagraph<?, ?, ? extends TextRun> slide2_tp = slide2_as.getTextParagraphs().get(0);
                final List<? extends TabStop> slide2_tabStops = slide2_tp.getTabStops();
                assertNotNull(slide2_tabStops);
                int i4 = 0;
                for (final TabStopType tst : TabStopType.values()) {
                    final TabStop ts = slide2_tabStops.get(i4);
                    assertEquals(15+i4*5, ts.getPositionInPoints(), 0.0);
                    assertEquals(tst, ts.getType());
                    i4++;
                }
            }
        }        
    }

    @Test
    public void shapeAndSlideName() throws IOException {
        final String file = "SampleShow.ppt"+(getClass().getSimpleName().contains("XML")?"x":"");
        try (final InputStream is = slTests.openResourceAsStream(file);
             final SlideShow<? extends Shape, ?> ppt = SlideShowFactory.create(is)) {
            final List<? extends Shape> shapes1 = ppt.getSlides().get(0).getShapes();
            assertEquals("The Title", shapes1.get(0).getShapeName());
            assertEquals("Another Subtitle", shapes1.get(1).getShapeName());
            final List<? extends Shape> shapes2 = ppt.getSlides().get(1).getShapes();
            assertEquals("Title 1", shapes2.get(0).getShapeName());
            assertEquals("Content Placeholder 2", shapes2.get(1).getShapeName());

            for (final Slide<?,?> slide : ppt.getSlides()) {
                final String expected = slide.getSlideNumber()==1 ? "FirstSlide" : "Slide2";
                assertEquals(expected, slide.getSlideName());
            }
        }
    }
}
