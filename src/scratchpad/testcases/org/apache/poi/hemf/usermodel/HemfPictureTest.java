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


package org.apache.poi.hemf.usermodel;

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hemf.record.emf.HemfComment;
import org.apache.poi.hemf.record.emf.HemfComment.EmfComment;
import org.apache.poi.hemf.record.emf.HemfComment.EmfCommentDataFormat;
import org.apache.poi.hemf.record.emf.HemfComment.EmfCommentDataMultiformats;
import org.apache.poi.hemf.record.emf.HemfHeader;
import org.apache.poi.hemf.record.emf.HemfRecord;
import org.apache.poi.hemf.record.emf.HemfRecordType;
import org.apache.poi.hemf.record.emf.HemfText;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.RecordFormatException;
import org.junit.Test;

public class HemfPictureTest {

    private POIDataSamples samples = POIDataSamples.getSpreadSheetInstance();

    @Test
    public void testBasicWindows() throws Exception {
        try (InputStream is = samples.openResourceAsStream("SimpleEMF_windows.emf")) {
            HemfPicture pic = new HemfPicture(is);
            HemfHeader header = pic.getHeader();
            assertEquals(27864, header.getBytes());
            assertEquals(31, header.getRecords());
            assertEquals(3, header.getHandles());
            assertEquals(346000, header.getMicrometersX());
            assertEquals(194000, header.getMicrometersY());

            List<HemfRecord> records = pic.getRecords();

            assertEquals(31, records.size());
        }
    }

    @Test
    public void testBasicMac() throws Exception {
        try (InputStream is = samples.openResourceAsStream("SimpleEMF_mac.emf")) {
            HemfPicture pic = new HemfPicture(is);
            HemfHeader header = pic.getHeader();

            int records = 0;
            boolean extractedData = false;
            for (HemfRecord record : pic) {
                if (record.getEmfRecordType() == HemfRecordType.comment) {
                    HemfComment.EmfCommentData comment = ((EmfComment) record).getCommentData();
                    if (comment instanceof EmfCommentDataMultiformats) {
                        for (EmfCommentDataFormat d : ((EmfCommentDataMultiformats) comment).getFormats()) {
                            byte[] data = d.getRawData();
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
            assertEquals(header.getRecords(), records);
        }
    }

    @Test
    public void testMacText() throws Exception {
        try (InputStream is = samples.openResourceAsStream("SimpleEMF_mac.emf")) {
            HemfPicture pic = new HemfPicture(is);

            double lastY = -1;
            double lastX = -1;
            //derive this from the font information!
            long fudgeFactorX = 1000;
            StringBuilder sb = new StringBuilder();
            for (HemfRecord record : pic) {
                if (record.getEmfRecordType().equals(HemfRecordType.exttextoutw)) {
                    HemfText.ExtTextOutW extTextOutW = (HemfText.ExtTextOutW) record;
                    Point2D reference = extTextOutW.getTextObject().getReference();
                    if (lastY > -1 && lastY != reference.getY()) {
                        sb.append("\n");
                        lastX = -1;
                    }
                    if (lastX > -1 && reference.getX() - lastX > fudgeFactorX) {
                        sb.append(" ");
                    }
                    sb.append(extTextOutW.getText());
                    lastY = reference.getY();
                    lastX = reference.getX();
                }
            }
            String txt = sb.toString();
            assertContains(txt, "Tika http://incubator.apache.org");
            assertContains(txt, "Latest News\n");
        }
    }

    @Test
    public void testWindowsText() throws Exception {
        try (InputStream is = samples.openResourceAsStream("SimpleEMF_windows.emf")) {
            HemfPicture pic = new HemfPicture(is);
            double lastY = -1;
            double lastX = -1;
            long fudgeFactorX = 1000;//derive this from the font or frame/bounds information
            StringBuilder sb = new StringBuilder();
            Set<String> expectedParts = new HashSet<>();
            expectedParts.add("C:\\Users\\tallison\\");
            expectedParts.add("testPDF.pdf");
            int foundExpected = 0;
            for (HemfRecord record : pic) {
                if (record.getEmfRecordType().equals(HemfRecordType.exttextoutw)) {
                    HemfText.ExtTextOutW extTextOutW = (HemfText.ExtTextOutW) record;
                    Point2D reference = extTextOutW.getTextObject().getReference();
                    if (lastY > -1 && lastY != reference.getY()) {
                        sb.append("\n");
                        lastX = -1;
                    }
                    if (lastX > -1 && reference.getX() - lastX > fudgeFactorX) {
                        sb.append(" ");
                    }
                    String txt = extTextOutW.getText();
                    if (expectedParts.contains(txt)) {
                        foundExpected++;
                    }
                    sb.append(txt);
                    lastY = reference.getY();
                    lastX = reference.getX();
                }
            }
            String txt = sb.toString();
            assertContains(txt, "C:\\Users\\tallison\\\n");
            assertContains(txt, "asf2-git-1.x\\tika-\n");
            assertEquals(expectedParts.size(), foundExpected);
        }
    }

    @Test(expected = RecordFormatException.class)
    public void testInfiniteLoopOnFile() throws Exception {
        try (InputStream is = samples.openResourceAsStream("61294.emf")) {
            HemfPicture pic = new HemfPicture(is);
            for (HemfRecord record : pic) {

            }
        }
    }

    @Test(expected = RecordFormatException.class)
    public void testInfiniteLoopOnByteArray() throws Exception {
        try (InputStream is = samples.openResourceAsStream("61294.emf")) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(is, bos);
            is.close();

            HemfPicture pic = new HemfPicture(new ByteArrayInputStream(bos.toByteArray()));
            for (HemfRecord record : pic) {

            }
        }
    }

     /*
        govdocs1 064213.doc-0.emf contains an example of extextouta
     */
}