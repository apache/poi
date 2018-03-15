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

package org.apache.poi.sl;

import static org.apache.poi.sl.SLCommonUtils.xslfOnly;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.junit.Test;

public class TestSlide {

    @Test
    public void hideHSLF() throws IOException {
        assumeFalse(xslfOnly());
        SlideShow<?,?> ppt1 = new HSLFSlideShow();
        hideSlide(ppt1);
        ppt1.close();
    }
    
    @Test
    public void hideXSLF() throws IOException {
        SlideShow<?,?> ppt1 = new XMLSlideShow();
        hideSlide(ppt1);
        ppt1.close();
    }
    
    private void hideSlide(SlideShow<?,?> ppt1) throws IOException {
        ppt1.createSlide().setHidden(true);
        ppt1.createSlide();
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ppt1.write(bos);
        ppt1.close();
        
        InputStream is = new ByteArrayInputStream(bos.toByteArray());
        SlideShow<?,?> ppt2 = SlideShowFactory.create(is);

        Boolean[] hiddenState = ppt2.getSlides().stream().map(e -> e.isHidden()).toArray(Boolean[]::new);
        
        assertTrue(hiddenState[0]);
        assertFalse(hiddenState[1]);
        
        ppt2.close();
    }
}