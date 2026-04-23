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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.sl.usermodel.BaseTestSlideShow;
import org.apache.poi.sl.usermodel.PictureData;
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

    @Test
    void setPassword() throws IOException {
        final byte[] data = slTests.readFile("clock.jpg");
        final String password = "123xyz";
        try(UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get()) {
            try (HSLFSlideShow show = createSlideShow()) {
                assertEquals(0, show.getPictureData().size());
                PictureData picture = show.addPicture(data, PictureData.PictureType.JPEG);
                assertEquals(1, show.getPictureData().size());
                assertSame(picture, show.getPictureData().get(0));
                show.setOutputPassword(password.toCharArray());
                show.write(baos);
            }
            try (HSLFSlideShow show = new HSLFSlideShow(baos.toInputStream(), password.toCharArray())) {
                assertEquals(1, show.getPictureData().size());
                assertArrayEquals(data, show.getPictureData().get(0).getData());
            }
        }
    }

    @Test
    void setPasswordBiff8() throws IOException {
        final byte[] data = slTests.readFile("clock.jpg");
        final String password = "123xyz";
        // kept for legacy testing, prefer to set the password like the `setPassword` test does
        try(UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get()) {
            try (HSLFSlideShow show = createSlideShow()) {
                assertEquals(0, show.getPictureData().size());
                PictureData picture = show.addPicture(data, PictureData.PictureType.JPEG);
                assertEquals(1, show.getPictureData().size());
                assertSame(picture, show.getPictureData().get(0));
                Biff8EncryptionKey.setCurrentUserPassword(password);
                show.write(baos);
            }
            Biff8EncryptionKey.setCurrentUserPassword(password);
            try (HSLFSlideShow show = new HSLFSlideShow(baos.toInputStream())) {
                assertEquals(1, show.getPictureData().size());
                assertArrayEquals(data, show.getPictureData().get(0).getData());
            }
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(null);
        }
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
