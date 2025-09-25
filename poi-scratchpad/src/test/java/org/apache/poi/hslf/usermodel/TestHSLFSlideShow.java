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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.sl.usermodel.BaseTestSlideShow;
import org.apache.poi.sl.usermodel.SlideShow;
import org.junit.jupiter.api.Test;

public class TestHSLFSlideShow extends BaseTestSlideShow<HSLFShape, HSLFTextParagraph> {
    @Override
    public HSLFSlideShow createSlideShow() {
        return new HSLFSlideShow();
    }

    // make sure junit4 executes this test class
    @Test
    void dummy() {
        assertNotNull(createSlideShow());
    }

    @Override
    public HSLFSlideShow reopen(SlideShow<HSLFShape, HSLFTextParagraph> show) throws IOException {
        try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
            show.write(bos);
            try (InputStream is = bos.toInputStream()) {
                return new HSLFSlideShow(is);
            }
        }
    }
}
