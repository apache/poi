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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.junit.Test;

public abstract class BaseTestSlideShow {
    protected static final POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
    
    public abstract SlideShow<?, ?> createSlideShow();
    
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
}
