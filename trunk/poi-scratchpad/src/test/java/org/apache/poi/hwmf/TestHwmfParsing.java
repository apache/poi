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

package org.apache.poi.hwmf;

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hwmf.record.HwmfFont;
import org.apache.poi.hwmf.record.HwmfRecord;
import org.apache.poi.hwmf.record.HwmfRecordType;
import org.apache.poi.hwmf.record.HwmfText;
import org.apache.poi.hwmf.usermodel.HwmfPicture;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TestHwmfParsing {

    private static final POIDataSamples samples = POIDataSamples.getSlideShowInstance();

    // ******************************************************************************
    // for manual mass parsing and rendering tests of .wmfs use HemfPictureTest.paint() !
    // ******************************************************************************

    @ParameterizedTest
    @CsvSource({
        "santa.wmf, 581",
        /* Bug 65063 */
        "empty-polygon-close.wmf, 272"
    })
    void parse(String file, int recordCnt) throws IOException {
        try (InputStream fis = samples.openResourceAsStream(file)) {
            HwmfPicture wmf = new HwmfPicture(fis);
            List<HwmfRecord> records = wmf.getRecords();
            assertEquals(recordCnt, records.size());
        }
    }

    @Test
    void testInfiniteLoop() throws Exception {
        try (InputStream is = samples.openResourceAsStream("61338.wmf")) {
            assertThrows(RecordFormatException.class, () -> new HwmfPicture(is));
        }
    }


    @Test
    @Disabled("If we decide we can use common crawl file specified, we can turn this back on")
    void testCyrillic() throws Exception {
        //TODO: move test file to framework and fix this
        File dir = new File("C:/somethingOrOther");
        File f = new File(dir, "ZMLH54SPLI76NQ7XMKVB7SMUJA2HTXTS-2.wmf");
        HwmfPicture wmf = new HwmfPicture(new FileInputStream(f));

        Charset charset = LocaleUtil.CHARSET_1252;
        StringBuilder sb = new StringBuilder();
        //this is pure hackery for specifying the font
        //this happens to work on this test file, but you need to
        //do what Graphics does by maintaining the stack, etc.!
        for (HwmfRecord r : wmf.getRecords()) {
            if (r.getWmfRecordType().equals(HwmfRecordType.createFontIndirect)) {
                HwmfFont font = ((HwmfText.WmfCreateFontIndirect)r).getFont();
                charset = (font.getCharset().getCharset() == null) ? LocaleUtil.CHARSET_1252 : font.getCharset().getCharset();
            }
            if (r.getWmfRecordType().equals(HwmfRecordType.extTextOut)) {
                HwmfText.WmfExtTextOut textOut = (HwmfText.WmfExtTextOut)r;
                sb.append(textOut.getText(charset)).append("\n");
            }
        }
        String txt = sb.toString();
        assertContains(txt, "\u041E\u0431\u0449\u043E");
        assertContains(txt, "\u0411\u0430\u043B\u0430\u043D\u0441");
    }

    @Test
    void testShift_JIS() throws Exception {
        //this file derives from common crawl: see Bug 60677
        HwmfPicture wmf = null;
        try (InputStream fis = samples.openResourceAsStream("60677.wmf")) {
            wmf = new HwmfPicture(fis);
        }

        Charset charset = LocaleUtil.CHARSET_1252;
        StringBuilder sb = new StringBuilder();
        //this is pure hackery for specifying the font
        //this happens to work on this test file, but you need to
        //do what Graphics does by maintaining the stack, etc.!
        for (HwmfRecord r : wmf.getRecords()) {
            if (r.getWmfRecordType().equals(HwmfRecordType.createFontIndirect)) {
                HwmfFont font = ((HwmfText.WmfCreateFontIndirect)r).getFont();
                charset = (font.getCharset().getCharset() == null) ? LocaleUtil.CHARSET_1252 : font.getCharset().getCharset();
            }
            if (r.getWmfRecordType().equals(HwmfRecordType.extTextOut)) {
                HwmfText.WmfExtTextOut textOut = (HwmfText.WmfExtTextOut)r;
                sb.append(textOut.getText(charset)).append("\n");
            }
        }
        String txt = sb.toString();
        assertContains(txt, "\u822A\u7A7A\u60C5\u5831\u696D\u52D9\u3078\u306E\uFF27\uFF29\uFF33");
    }

    @Test
    void testLengths() throws Exception {
        //both substring and length rely on char, not codepoints.
        //This test confirms that the substring calls in HwmfText
        //will not truncate even beyond-bmp data.
        //The last character (Deseret AY U+1040C) is comprised of 2 utf16 surrogates/codepoints
        String s = "\u666E\u6797\u65AF\uD801\uDC0C";
        Charset utf16LE = StandardCharsets.UTF_16LE;
        byte[] bytes = s.getBytes(utf16LE);
        String rebuilt = new String(bytes, utf16LE);
        rebuilt = rebuilt.substring(0, Math.min(bytes.length, rebuilt.length()));
        assertEquals(s, rebuilt);
        assertEquals(5, rebuilt.length());
        long cnt = rebuilt.codePoints().count();
        assertEquals(4, cnt);
    }
}
