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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import org.apache.poi.hwmf.record.HwmfRecord;
import org.apache.poi.hwmf.record.HwmfText;
import org.apache.poi.hwmf.usermodel.HwmfEmbedded;
import org.apache.poi.hwmf.usermodel.HwmfEmbeddedType;
import org.apache.poi.hwmf.usermodel.HwmfPicture;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.RecordFormatException;
import org.junit.Test;

public class TestHemfPicture {

    private static final POIDataSamples ss_samples = POIDataSamples.getSpreadSheetInstance();
    private static final POIDataSamples sl_samples = POIDataSamples.getSlideShowInstance();

    /*
    @Test
    @Ignore("Only for manual tests - need to add org.tukaani:xz:1.8 for this to work")
    public void paint() throws Exception {
        final byte buf[] = new byte[50_000_000];

        // good test samples to validate rendering:
        // emfs/commoncrawl2/NB/NBWN2YH5VFCLZRFDQU7PB7IDD4UKY7DN_2.emf
        // emfs/govdocs1/777/777525.ppt_0.emf
        // emfs/govdocs1/844/844795.ppt_2.emf
        // emfs/commoncrawl2/TO/TOYZSTNUSW5OFCFUQ6T5FBLIDLCRF3NH_0.emf

        // ISS3ANIX2PL4PXR7SZSJSPBZI7YQQE3U_6 - map of italy - stroke problem
        // 3QKAPISTXYHSFCTV6QTKTYLK6JTWJHQU_2 - text misplaced
        // KEEDHN6XES4EKK52E3AJHKCARNTQF7PO_0 - dito
        // KWG4VAU5GM3POSA4BPG6RSVQVS44SXOL_1.emf - processing freezes

        // F7GK5XOLERFURVTQALOCX3GJ6FH45LNQ strange colors
        // ISS3ANIX2PL4PXR7SZSJSPBZI7YQQE3U stroke wrong
        // KWG4VAU5GM3POSA4BPG6RSVQVS44SXOL_1

        try (SevenZFile sevenZFile = new SevenZFile(new File("tmp/plus_emf.7z"))
            ) {
            for (int idx=0;;idx++) {
                SevenZArchiveEntry entry = sevenZFile.getNextEntry();
                if (entry == null) break;
                final String etName = entry.getName();

                if (entry.isDirectory() || !etName.endsWith(".emf")) continue;

                if (!(etName.contains("3QKAPISTXYHSFCTV6QTKTYLK6JTWJHQU_2")
                )) continue;

//                || etName.contains("ISS3ANIX2PL4PXR7SZSJSPBZI7YQQE3U_6")
//                        || etName.contains("KWG4VAU5GM3POSA4BPG6RSVQVS44SXOL_1")


                System.out.println(etName);

                int size = sevenZFile.read(buf);
                ByteArrayInputStream bis = new ByteArrayInputStream(buf, 0, size);
                System.setIn(bis);

                String lastName = etName.replaceFirst(".+/", "");

                String[] args = {
                    "-format", "png", // png,gif,jpg or null for test
                    "-outdir", new File("build/tmp/").getCanonicalPath(),
                    "-outfile", lastName.replace(".emf", ".png"),
                    "-fixside", "long",
                    "-scale", "800",
                    "-ignoreParse",
                     "-dump", new File("build/tmp/", lastName.replace(".emf",".json")).getCanonicalPath(),
                    // "-quiet",
                    // "-extractEmbedded",
                    "stdin"
                };
                PPTX2PNG.main(args);
            }
        }
    }
*/
    @Test
    public void testBasicWindows() throws Exception {
        try (InputStream is = ss_samples.openResourceAsStream("SimpleEMF_windows.emf")) {
            HemfPicture pic = new HemfPicture(is);
            HemfHeader header = pic.getHeader();
            assertEquals(27864, header.getBytes());
            assertEquals(31, header.getRecords());
            assertEquals(3, header.getHandles());
            assertEquals(346000, header.getMicroDimension().getWidth(), 0);
            assertEquals(194000, header.getMicroDimension().getHeight(), 0);

            List<HemfRecord> records = pic.getRecords();

            assertEquals(31, records.size());
        }
    }

    @Test
    public void testBasicMac() throws Exception {
        try (InputStream is = ss_samples.openResourceAsStream("SimpleEMF_mac.emf")) {
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
        try (InputStream is = ss_samples.openResourceAsStream("SimpleEMF_mac.emf")) {
            HemfPicture pic = new HemfPicture(is);

            double lastY = -1;
            double lastX = -1;
            //derive this from the font information!
            long fudgeFactorX = 1000;
            StringBuilder sb = new StringBuilder();
            for (HemfRecord record : pic) {
                if (record.getEmfRecordType().equals(HemfRecordType.extTextOutW)) {
                    HemfText.EmfExtTextOutW extTextOutW = (HemfText.EmfExtTextOutW) record;
                    Point2D reference = extTextOutW.getReference();
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
    public void testWMFInsideEMF() throws Exception {

        byte[] wmfData = null;
        try (InputStream is = ss_samples.openResourceAsStream("63327.emf")) {
            HemfPicture pic = new HemfPicture(is);
            for (HemfRecord record : pic) {
                if (record.getEmfRecordType() == HemfRecordType.comment) {
                    HemfComment.EmfComment commentRecord = (HemfComment.EmfComment) record;
                    HemfComment.EmfCommentData emfCommentData = commentRecord.getCommentData();
                    if (emfCommentData instanceof HemfComment.EmfCommentDataWMF) {
                        wmfData = ((HemfComment.EmfCommentDataWMF) emfCommentData).getWMFData();
                    }
                }
            }
        }
        assertNotNull(wmfData);
        assertEquals(230, wmfData.length);
        HwmfPicture pict = new HwmfPicture(new ByteArrayInputStream(wmfData));
        String embedded = null;
        for (HwmfRecord r : pict.getRecords()) {
            if (r instanceof HwmfText.WmfTextOut) {
                embedded = ((HwmfText.WmfTextOut) r).getText(StandardCharsets.US_ASCII);
            }
        }
        assertNotNull(embedded);
        assertEquals("Hw.txt", embedded);
    }

    @Test
    public void testWindowsText() throws Exception {
        try (InputStream is = ss_samples.openResourceAsStream("SimpleEMF_windows.emf")) {
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
                if (record.getEmfRecordType().equals(HemfRecordType.extTextOutW)) {
                    HemfText.EmfExtTextOutW extTextOutW = (HemfText.EmfExtTextOutW) record;
                    Point2D reference = extTextOutW.getReference();
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
        try (InputStream is = ss_samples.openResourceAsStream("61294.emf")) {
            HemfPicture pic = new HemfPicture(is);
            for (HemfRecord record : pic) {

            }
        }
    }

    @Test(expected = RecordFormatException.class)
    public void testInfiniteLoopOnByteArray() throws Exception {
        try (InputStream is = ss_samples.openResourceAsStream("61294.emf")) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(is, bos);
            is.close();

            HemfPicture pic = new HemfPicture(new ByteArrayInputStream(bos.toByteArray()));
            for (HemfRecord record : pic) {

            }
        }
    }

    @Test
    public void nestedWmfEmf() throws Exception {
        try (InputStream is = sl_samples.openResourceAsStream("nested_wmf.emf")) {
            HemfPicture emf1 = new HemfPicture(is);
            List<HwmfEmbedded> embeds = new ArrayList<>();
            emf1.getEmbeddings().forEach(embeds::add);
            assertEquals(1, embeds.size());
            assertEquals(HwmfEmbeddedType.WMF, embeds.get(0).getEmbeddedType());

            HwmfPicture wmf = new HwmfPicture(new ByteArrayInputStream(embeds.get(0).getRawData()));
            embeds.clear();
            wmf.getEmbeddings().forEach(embeds::add);
            assertEquals(3, embeds.size());
            assertEquals(HwmfEmbeddedType.EMF, embeds.get(0).getEmbeddedType());

            HemfPicture emf2 = new HemfPicture(new ByteArrayInputStream(embeds.get(0).getRawData()));
            embeds.clear();
            emf2.getEmbeddings().forEach(embeds::add);
            assertTrue(embeds.isEmpty());
        }
    }


    /* govdocs1 064213.doc-0.emf contains an example of extextouta */
}