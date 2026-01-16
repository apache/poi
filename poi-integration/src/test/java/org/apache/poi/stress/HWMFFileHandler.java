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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.poi.hwmf.record.HwmfFont;
import org.apache.poi.hwmf.record.HwmfRecord;
import org.apache.poi.hwmf.record.HwmfRecordType;
import org.apache.poi.hwmf.record.HwmfText;
import org.apache.poi.hwmf.usermodel.HwmfPicture;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Test;

public class HWMFFileHandler implements FileHandler {

    @Override
    public void handleExtracting(File file) throws Exception {
        try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            HwmfPicture picture = new HwmfPicture(stream);
            Charset charset = LocaleUtil.CHARSET_1252;

            // mimic a bit what e.g. Tika does to extract some information from .wmf files
            for (HwmfRecord record : picture.getRecords()) {
                if (record.getWmfRecordType().equals(HwmfRecordType.createFontIndirect)) {
                    HwmfFont font = ((HwmfText.WmfCreateFontIndirect) record).getFont();
                    charset = (font.getCharset() == null || font.getCharset().getCharset() == null) ?
                                    LocaleUtil.CHARSET_1252 : font.getCharset().getCharset();
                }

                if (record.getWmfRecordType().equals(HwmfRecordType.extTextOut)) {
                    assertInstanceOf(HwmfText.WmfExtTextOut.class, record);
                    HwmfText.WmfExtTextOut textOut = (HwmfText.WmfExtTextOut) record;
                    textOut.getText(charset);
                } else if (record.getWmfRecordType().equals(HwmfRecordType.textOut)) {
                    assertInstanceOf(HwmfText.WmfTextOut.class, record);
                    HwmfText.WmfTextOut textOut = (HwmfText.WmfTextOut) record;
                    textOut.getText(charset);
                }
            }
        }
    }

    @Override
    public void handleAdditional(File file) throws Exception {
        // no additional checks for now
    }

    @Override
    public void handleFile(InputStream stream, String path) throws Exception {
        HwmfPicture picture = new HwmfPicture(stream);

        for (HwmfRecord record : picture.getRecords()) {
            record.getWmfRecordType();
            record.getGenericRecordType();
        }

        BufferedImage dest = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        picture.draw(dest.createGraphics());
    }

    @Test
    void test() throws Exception {
        String file = "test-data/slideshow/santa.wmf";

        try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            handleFile(stream, file);
        }

        handleExtracting(new File(file));
    }
}
