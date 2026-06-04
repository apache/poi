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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hwmf.draw.HwmfImageRenderer;
import org.apache.poi.hwmf.record.HwmfFont;
import org.apache.poi.hwmf.record.HwmfPlaceableHeader;
import org.apache.poi.hwmf.record.HwmfRecord;
import org.apache.poi.hwmf.record.HwmfRecordType;
import org.apache.poi.hwmf.record.HwmfText;
import org.apache.poi.hwmf.record.HwmfWindowing;
import org.apache.poi.hwmf.usermodel.HwmfPicture;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@SuppressWarnings("UnnecessaryUnicodeEscape")
public class TestHwmfParsing {

    private static final POIDataSamples samples = POIDataSamples.getSlideShowInstance();

    // ******************************************************************************
    // for manual mass parsing and rendering tests of .wmfs use HemfPictureTest.paint() !
    // ******************************************************************************

    @ParameterizedTest
    @CsvSource({
        "santa.wmf, 581",
        /* Bug 65063 */
        "empty-polygon-close.wmf, 272",
        "file-45.wmf, 1315"
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
    void testInvalid() throws Exception {
        try (InputStream is = samples.openResourceAsStream("santa.wmf")) {
            byte[] bytes = IOUtils.toByteArray(is);

            // simulate an invalid commentType, it should be logged and ignored
            bytes[34] = (byte)255;
            bytes[35] = (byte)255;

            HwmfPicture wmf = new HwmfPicture(new ByteArrayInputStream(bytes));
            List<HwmfRecord> records = wmf.getRecords();
            assertEquals(581, records.size());
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
                assertInstanceOf(HwmfText.WmfExtTextOut.class, r);

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
        final HwmfPicture wmf;
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
                assertInstanceOf(HwmfText.WmfExtTextOut.class, r);

                HwmfText.WmfExtTextOut textOut = (HwmfText.WmfExtTextOut)r;
                sb.append(textOut.getText(charset)).append("\n");
            }
        }
        String txt = sb.toString();
        assertContains(txt, "\u822A\u7A7A\u60C5\u5831\u696D\u52D9\u3078\u306E\uFF27\uFF29\uFF33");
    }

    @Test
    void testLengths() {
        //both substring and length rely on char, not codepoints.
        //This test confirms that the substring calls in HwmfText
        //will not truncate even beyond-bmp data.
        //The last character (Deseret AY U+1040C) consists of 2 utf16 surrogates/codepoints
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

    @Test
    void testRejectsZeroUnitsPerInchAtParserBoundary() {
        byte[] malicious = createMinimalPlaceableWmf(0, 0, 0, 32767, 32767);
        RecordFormatException ex = assertThrows(
            RecordFormatException.class,
            () -> new HwmfPicture(new ByteArrayInputStream(malicious))
        );
        assertContains(ex.getMessage(), "unitsPerInch");
    }

    @Test
    void testWmfCreateRegionInvalidScanCount() throws Exception {
        HwmfWindowing.WmfCreateRegion record = new HwmfWindowing.WmfCreateRegion();
        byte[] data = new byte[34];
        org.apache.poi.util.LittleEndian.putShort(data, 10, (short) -1);
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
            assertThrows(RecordFormatException.class,
                () -> record.init(leis, data.length, 0));
        }
    }

    @Test
    void testValidUnitsProduceFiniteDimensions() throws IOException {
        byte[] benign = createMinimalPlaceableWmf(1440, 0, 0, 100, 100);
        HwmfImageRenderer renderer = new HwmfImageRenderer();
        renderer.loadImage(benign, PictureData.PictureType.WMF.contentType);

        assertTrue(Double.isFinite(renderer.getDimension().getWidth()));
        assertTrue(Double.isFinite(renderer.getDimension().getHeight()));
        assertTrue(renderer.getDimension().getWidth() > 0);
        assertTrue(renderer.getDimension().getHeight() > 0);
    }

    /**
     * Creates a minimal placeable WMF stream: placeable header + WMF header + EOF record.
     * The payload is intentionally small because these tests target parser validation, not WMF drawing semantics.
     */
    private static byte[] createMinimalPlaceableWmf(int unitsPerInch, int x1, int y1, int x2, int y2) {
        ByteBuffer bb = ByteBuffer.allocate(46).order(ByteOrder.LITTLE_ENDIAN);

        // Placeable header
        bb.putInt(HwmfPlaceableHeader.WMF_HEADER_MAGIC);
        bb.putShort((short) 0); // hwmf handle
        bb.putShort((short) x1);
        bb.putShort((short) y1);
        bb.putShort((short) x2);
        bb.putShort((short) y2);
        bb.putShort((short) unitsPerInch);
        bb.putInt(0); // reserved
        bb.putShort((short) 0); // checksum

        // WMF header (9 words)
        bb.putShort((short) 1); // memory metafile
        bb.putShort((short) 9); // header size in WORDs
        bb.putShort((short) 0x0300); // version
        bb.putInt(0); // file size in WORDs (unused by parser)
        bb.putShort((short) 0); // number of objects
        bb.putInt(0); // max record size
        bb.putShort((short) 0); // number of members

        // EOF record
        bb.putInt(3); // record size in WORDs
        bb.putShort((short) HwmfRecordType.eof.id);

        return bb.array();
    }
}
