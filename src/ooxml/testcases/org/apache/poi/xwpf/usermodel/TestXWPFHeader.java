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

package org.apache.poi.xwpf.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;

final class TestXWPFHeader {

    @Test
    void testSimpleHeader() throws IOException {
        try (XWPFDocument sampleDoc = XWPFTestDataSamples.openSampleDocument("headerFooter.docx")) {

            XWPFHeaderFooterPolicy policy = sampleDoc.getHeaderFooterPolicy();

            XWPFHeader header = policy.getDefaultHeader();
            XWPFFooter footer = policy.getDefaultFooter();
            assertNotNull(header);
            assertNotNull(footer);
        }
    }

    @Test
    void testImageInHeader() throws IOException {
        try (XWPFDocument sampleDoc = XWPFTestDataSamples.openSampleDocument("headerPic.docx")) {

            XWPFHeaderFooterPolicy policy = sampleDoc.getHeaderFooterPolicy();

            XWPFHeader header = policy.getDefaultHeader();

            assertNotNull(header.getRelations());
            assertEquals(1, header.getRelations().size());
        }
    }

    @Test
    void testSetHeader() throws IOException {
        try (XWPFDocument sampleDoc = XWPFTestDataSamples.openSampleDocument("SampleDoc.docx")) {
            // no header is set (yet)
            XWPFHeaderFooterPolicy policy = sampleDoc.getHeaderFooterPolicy();
            assertNull(policy.getDefaultHeader());
            assertNull(policy.getFirstPageHeader());
            assertNull(policy.getDefaultFooter());
            assertNull(policy.getFirstPageFooter());

            CTP ctP1 = CTP.Factory.newInstance();
            CTR ctR1 = ctP1.addNewR();
            CTText t = ctR1.addNewT();
            String tText = "Paragraph in header";
            t.setStringValue(tText);

            // Commented MB 23 May 2010
            //CTP ctP2 = CTP.Factory.newInstance();
            //CTR ctR2 = ctP2.addNewR();
            //CTText t2 = ctR2.addNewT();
            //t2.setStringValue("Second paragraph.. for footer");

            // Create two paragraphs for insertion into the footer.
            // Previously only one was inserted MB 23 May 2010
            CTP ctP2 = CTP.Factory.newInstance();
            CTR ctR2 = ctP2.addNewR();
            CTText t2 = ctR2.addNewT();
            t2.setStringValue("First paragraph for the footer");

            CTP ctP3 = CTP.Factory.newInstance();
            CTR ctR3 = ctP3.addNewR();
            CTText t3 = ctR3.addNewT();
            t3.setStringValue("Second paragraph for the footer");

            XWPFParagraph p1 = new XWPFParagraph(ctP1, sampleDoc);
            XWPFParagraph[] pars = new XWPFParagraph[1];
            pars[0] = p1;

            XWPFParagraph p2 = new XWPFParagraph(ctP2, sampleDoc);
            XWPFParagraph p3 = new XWPFParagraph(ctP3, sampleDoc);
            XWPFParagraph[] pars2 = new XWPFParagraph[2];
            pars2[0] = p2;
            pars2[1] = p3;

            // Set headers
            XWPFHeader headerD = policy.createHeader(XWPFHeaderFooterPolicy.DEFAULT, pars);
            XWPFHeader headerF = policy.createHeader(XWPFHeaderFooterPolicy.FIRST);
            // Set a default footer and capture the returned XWPFFooter object.
            XWPFFooter footerD = policy.createFooter(XWPFHeaderFooterPolicy.DEFAULT, pars2);
            XWPFFooter footerF = policy.createFooter(XWPFHeaderFooterPolicy.FIRST);

            // Ensure the headers and footer were set correctly....
            assertNotNull(policy.getDefaultHeader());
            assertNotNull(policy.getFirstPageHeader());
            assertNotNull(policy.getDefaultFooter());
            assertNotNull(policy.getFirstPageFooter());
            // ....and that the footer object captured above contains two
            // paragraphs of text.
            assertEquals(2, footerD.getParagraphs().size());
            assertEquals(0, footerF.getParagraphs().size());

            // Check the header created with the paragraph got them, and the one
            // created without got none
            assertEquals(1, headerD.getParagraphs().size());
            assertEquals(tText, headerD.getParagraphs().get(0).getText());

            assertEquals(0, headerF.getParagraphs().size());

            // As an additional check, recover the defauls footer and
            // make sure that it contains two paragraphs of text and that
            // both do hold what is expected.
            footerD = policy.getDefaultFooter();
            XWPFParagraph[] paras = footerD.getParagraphs().toArray(new XWPFParagraph[0]);

            assertEquals(2, paras.length);
            assertEquals("First paragraph for the footer", paras[0].getText());
            assertEquals("Second paragraph for the footer", paras[1].getText());


            // Add some text to the empty header
            String fText1 = "New Text!";
            String fText2 = "More Text!";
            headerF.createParagraph().insertNewRun(0).setText(fText1);
            headerF.createParagraph().insertNewRun(0).setText(fText2);
            // headerF.getParagraphs().get(0).insertNewRun(0).setText(fText1);

            // Check it
            assertEquals(tText, headerD.getParagraphs().get(0).getText());
            assertEquals(fText1, headerF.getParagraphs().get(0).getText());
            assertEquals(fText2, headerF.getParagraphs().get(1).getText());


            // Save, re-open, ensure it's all still there
            XWPFDocument reopened = XWPFTestDataSamples.writeOutAndReadBack(sampleDoc);
            policy = reopened.getHeaderFooterPolicy();
            assertNotNull(policy.getDefaultHeader());
            assertNotNull(policy.getFirstPageHeader());
            assertNull(policy.getEvenPageHeader());
            assertNotNull(policy.getDefaultFooter());
            assertNotNull(policy.getFirstPageFooter());
            assertNull(policy.getEvenPageFooter());

            // Check the new headers still have their text
            headerD = policy.getDefaultHeader();
            headerF = policy.getFirstPageHeader();
            assertEquals(tText, headerD.getParagraphs().get(0).getText());
            assertEquals(fText1, headerF.getParagraphs().get(0).getText());
            assertEquals(fText2, headerF.getParagraphs().get(1).getText());

            // Check the new footers have their new text too
            footerD = policy.getDefaultFooter();
            paras = footerD.getParagraphs().toArray(new XWPFParagraph[0]);
            footerF = policy.getFirstPageFooter();

            assertEquals(2, paras.length);
            assertEquals("First paragraph for the footer", paras[0].getText());
            assertEquals("Second paragraph for the footer", paras[1].getText());
            assertEquals(1, footerF.getParagraphs().size());
        }
    }

    @Test
    void testSetWatermark() throws IOException {
        try (XWPFDocument sampleDoc = XWPFTestDataSamples.openSampleDocument("SampleDoc.docx")) {

            // No header is set (yet)
            XWPFHeaderFooterPolicy policy = sampleDoc.getHeaderFooterPolicy();
            assertNull(policy.getDefaultHeader());
            assertNull(policy.getFirstPageHeader());
            assertNull(policy.getDefaultFooter());

            policy.createWatermark("DRAFT");

            assertNotNull(policy.getDefaultHeader());
            assertNotNull(policy.getFirstPageHeader());
            assertNotNull(policy.getEvenPageHeader());

            // Re-open, and check
            XWPFDocument reopened = XWPFTestDataSamples.writeOutAndReadBack(sampleDoc);
            policy = reopened.getHeaderFooterPolicy();

            assertNotNull(policy.getDefaultHeader());
            assertNotNull(policy.getFirstPageHeader());
            assertNotNull(policy.getEvenPageHeader());
        }
    }

    @Test
    void testSetWatermarkOnEmptyDoc() throws IOException {
        try (XWPFDocument sampleDoc = new XWPFDocument()) {

            // No header is set (yet)
            XWPFHeaderFooterPolicy policy = sampleDoc.getHeaderFooterPolicy();
            assertNull(policy);

            policy = sampleDoc.createHeaderFooterPolicy();
            policy.createWatermark("DRAFT");

            assertNotNull(policy.getDefaultHeader());
            assertNotNull(policy.getFirstPageHeader());
            assertNotNull(policy.getEvenPageHeader());

            // Re-open, and check
            XWPFDocument reopened = XWPFTestDataSamples.writeOutAndReadBack(sampleDoc);
            policy = reopened.getHeaderFooterPolicy();

            assertNotNull(policy.getDefaultHeader());
            assertNotNull(policy.getFirstPageHeader());
            assertNotNull(policy.getEvenPageHeader());
        }
    }

    @Disabled
    void testAddPictureData() {
        // TODO
    }

    @Disabled
    void testGetAllPictures() {
        // TODO
    }

    @Disabled
    void testGetAllPackagePictures() {
        // TODO
    }

    @Disabled
    void testGetPictureDataById() {
        // TODO
    }

    @Test
    void bug60293() throws Exception {
        //test handling of non-standard header/footer options
        try (XWPFDocument xwpf = XWPFTestDataSamples.openSampleDocument("60293.docx")) {
            assertEquals(3, xwpf.getHeaderList().size());
        }
    }
}
