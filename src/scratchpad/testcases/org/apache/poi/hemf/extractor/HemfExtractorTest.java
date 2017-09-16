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


package org.apache.poi.hemf.extractor;

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hemf.record.AbstractHemfComment;
import org.apache.poi.hemf.record.HemfCommentPublic;
import org.apache.poi.hemf.record.HemfCommentRecord;
import org.apache.poi.hemf.record.HemfHeader;
import org.apache.poi.hemf.record.HemfRecord;
import org.apache.poi.hemf.record.HemfRecordType;
import org.apache.poi.hemf.record.HemfText;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.RecordFormatException;
import org.junit.Test;

public class HemfExtractorTest {

    @Test
    public void testBasicWindows() throws Exception {
        InputStream is = POIDataSamples.getSpreadSheetInstance().openResourceAsStream("SimpleEMF_windows.emf");
        HemfExtractor ex = new HemfExtractor(is);
        HemfHeader header = ex.getHeader();
        assertEquals(27864, header.getBytes());
        assertEquals(31, header.getRecords());
        assertEquals(3, header.getHandles());
        assertEquals(346000, header.getMicrometersX());
        assertEquals(194000, header.getMicrometersY());

        int records = 0;
        for (HemfRecord record : ex) {
            records++;
        }

        assertEquals(header.getRecords() - 1, records);
    }

    @Test
    public void testBasicMac() throws Exception {
        InputStream is =
                POIDataSamples.getSpreadSheetInstance().openResourceAsStream("SimpleEMF_mac.emf");
        HemfExtractor ex = new HemfExtractor(is);
        HemfHeader header = ex.getHeader();

        int records = 0;
        boolean extractedData = false;
        for (HemfRecord record : ex) {
            if (record.getRecordType() == HemfRecordType.comment) {
                AbstractHemfComment comment = ((HemfCommentRecord) record).getComment();
                if (comment instanceof HemfCommentPublic.MultiFormats) {
                    for (HemfCommentPublic.HemfMultiFormatsData d : ((HemfCommentPublic.MultiFormats) comment).getData()) {
                        byte[] data = d.getData();
                        //make sure header starts at 0
                        assertEquals('%', data[0]);
                        assertEquals('P', data[1]);
                        assertEquals('D', data[2]);
                        assertEquals('F', data[3]);

                        //make sure byte array ends at EOF\n
                        assertEquals('E', data[data.length - 4]);
                        assertEquals('O', data[data.length - 3]);
                        assertEquals('F', data[data.length - 2]);
                        assertEquals('\n', data[data.length - 1]);
                        extractedData = true;
                    }
                }
            }
            records++;
        }
        assertTrue(extractedData);
        assertEquals(header.getRecords() - 1, records);
    }

    @Test
    public void testMacText() throws Exception {
        InputStream is =
                POIDataSamples.getSpreadSheetInstance().openResourceAsStream("SimpleEMF_mac.emf");
        HemfExtractor ex = new HemfExtractor(is);

        long lastY = -1;
        long lastX = -1;
        long fudgeFactorX = 1000;//derive this from the font information!
        StringBuilder sb = new StringBuilder();
        for (HemfRecord record : ex) {
            if (record.getRecordType().equals(HemfRecordType.exttextoutw)) {
                HemfText.ExtTextOutW extTextOutW = (HemfText.ExtTextOutW) record;
                if (lastY > -1 && lastY != extTextOutW.getY()) {
                    sb.append("\n");
                    lastX = -1;
                }
                if (lastX > -1 && extTextOutW.getX() - lastX > fudgeFactorX) {
                    sb.append(" ");
                }
                sb.append(extTextOutW.getText());
                lastY = extTextOutW.getY();
                lastX = extTextOutW.getX();
            }
        }
        String txt = sb.toString();
        assertContains(txt, "Tika http://incubator.apache.org");
        assertContains(txt, "Latest News\n");
    }

    @Test
    public void testWindowsText() throws Exception {
        InputStream is = POIDataSamples.getSpreadSheetInstance().openResourceAsStream("SimpleEMF_windows.emf");
        HemfExtractor ex = new HemfExtractor(is);
        long lastY = -1;
        long lastX = -1;
        long fudgeFactorX = 1000;//derive this from the font or frame/bounds information
        StringBuilder sb = new StringBuilder();
        Set<String> expectedParts = new HashSet<>();
        expectedParts.add("C:\\Users\\tallison\\");
        expectedParts.add("testPDF.pdf");
        int foundExpected = 0;
        for (HemfRecord record : ex) {
            if (record.getRecordType().equals(HemfRecordType.exttextoutw)) {
                HemfText.ExtTextOutW extTextOutW = (HemfText.ExtTextOutW) record;
                if (lastY > -1 && lastY != extTextOutW.getY()) {
                    sb.append("\n");
                    lastX = -1;
                }
                if (lastX > -1 && extTextOutW.getX() - lastX > fudgeFactorX) {
                    sb.append(" ");
                }
                String txt = extTextOutW.getText();
                if (expectedParts.contains(txt)) {
                    foundExpected++;
                }
                sb.append(txt);
                lastY = extTextOutW.getY();
                lastX = extTextOutW.getX();
            }
        }
        String txt = sb.toString();
        assertContains(txt, "C:\\Users\\tallison\\\n");
        assertContains(txt, "asf2-git-1.x\\tika-\n");
        assertEquals(expectedParts.size(), foundExpected);
    }

    @Test(expected = RecordFormatException.class)
    public void testInfiniteLoopOnFile() throws Exception {
        InputStream is = null;
        try {
            is = POIDataSamples.getSpreadSheetInstance().openResourceAsStream("61294.emf");

            HemfExtractor ex = new HemfExtractor(is);
            for (HemfRecord record : ex) {

            }
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    @Test(expected = RecordFormatException.class)
    public void testInfiniteLoopOnByteArray() throws Exception {
        InputStream is = POIDataSamples.getSpreadSheetInstance().openResourceAsStream("61294.emf");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(is, bos);
        is.close();

        HemfExtractor ex = new HemfExtractor(new ByteArrayInputStream(bos.toByteArray()));
        for (HemfRecord record : ex) {

        }
    }

     /*
        govdocs1 064213.doc-0.emf contains an example of extextouta
     */
}