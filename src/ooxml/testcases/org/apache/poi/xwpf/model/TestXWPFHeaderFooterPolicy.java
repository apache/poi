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

package org.apache.poi.xwpf.model;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for XWPF Header Footer Stuff
 */
class TestXWPFHeaderFooterPolicy {
    private XWPFDocument noHeader;
    private XWPFDocument header;
    private XWPFDocument headerFooter;
    private XWPFDocument footer;
    private XWPFDocument oddEven;
    private XWPFDocument diffFirst;

    @BeforeEach
    void setUp() throws IOException {
        noHeader = XWPFTestDataSamples.openSampleDocument("NoHeadFoot.docx");
        header = XWPFTestDataSamples.openSampleDocument("ThreeColHead.docx");
        headerFooter = XWPFTestDataSamples.openSampleDocument("SimpleHeadThreeColFoot.docx");
        footer = XWPFTestDataSamples.openSampleDocument("FancyFoot.docx");
        oddEven = XWPFTestDataSamples.openSampleDocument("PageSpecificHeadFoot.docx");
        diffFirst = XWPFTestDataSamples.openSampleDocument("DiffFirstPageHeadFoot.docx");
    }

    @AfterEach
    void tearDown() throws IOException {
        noHeader.close();
        header.close();
        headerFooter.close();
        footer.close();
        oddEven.close();
        diffFirst.close();
    }

    @Test
    void testPolicy() {
        XWPFHeaderFooterPolicy policy;

        policy = noHeader.getHeaderFooterPolicy();
        assertNull(policy.getDefaultHeader());
        assertNull(policy.getDefaultFooter());

        assertNull(policy.getHeader(1));
        assertNull(policy.getHeader(2));
        assertNull(policy.getHeader(3));
        assertNull(policy.getFooter(1));
        assertNull(policy.getFooter(2));
        assertNull(policy.getFooter(3));


        policy = header.getHeaderFooterPolicy();
        assertNotNull(policy.getDefaultHeader());
        assertNull(policy.getDefaultFooter());

        assertEquals(policy.getDefaultHeader(), policy.getHeader(1));
        assertEquals(policy.getDefaultHeader(), policy.getHeader(2));
        assertEquals(policy.getDefaultHeader(), policy.getHeader(3));
        assertNull(policy.getFooter(1));
        assertNull(policy.getFooter(2));
        assertNull(policy.getFooter(3));


        policy = footer.getHeaderFooterPolicy();
        assertNull(policy.getDefaultHeader());
        assertNotNull(policy.getDefaultFooter());

        assertNull(policy.getHeader(1));
        assertNull(policy.getHeader(2));
        assertNull(policy.getHeader(3));
        assertEquals(policy.getDefaultFooter(), policy.getFooter(1));
        assertEquals(policy.getDefaultFooter(), policy.getFooter(2));
        assertEquals(policy.getDefaultFooter(), policy.getFooter(3));


        policy = headerFooter.getHeaderFooterPolicy();
        assertNotNull(policy.getDefaultHeader());
        assertNotNull(policy.getDefaultFooter());

        assertEquals(policy.getDefaultHeader(), policy.getHeader(1));
        assertEquals(policy.getDefaultHeader(), policy.getHeader(2));
        assertEquals(policy.getDefaultHeader(), policy.getHeader(3));
        assertEquals(policy.getDefaultFooter(), policy.getFooter(1));
        assertEquals(policy.getDefaultFooter(), policy.getFooter(2));
        assertEquals(policy.getDefaultFooter(), policy.getFooter(3));


        policy = oddEven.getHeaderFooterPolicy();
        assertNotNull(policy.getDefaultHeader());
        assertNotNull(policy.getDefaultFooter());
        assertNotNull(policy.getEvenPageHeader());
        assertNotNull(policy.getEvenPageFooter());

        assertEquals(policy.getDefaultHeader(), policy.getHeader(1));
        assertEquals(policy.getEvenPageHeader(), policy.getHeader(2));
        assertEquals(policy.getDefaultHeader(), policy.getHeader(3));
        assertEquals(policy.getDefaultFooter(), policy.getFooter(1));
        assertEquals(policy.getEvenPageFooter(), policy.getFooter(2));
        assertEquals(policy.getDefaultFooter(), policy.getFooter(3));


        policy = diffFirst.getHeaderFooterPolicy();
        assertNotNull(policy.getDefaultHeader());
        assertNotNull(policy.getDefaultFooter());
        assertNotNull(policy.getFirstPageHeader());
        assertNotNull(policy.getFirstPageFooter());
        assertNull(policy.getEvenPageHeader());
        assertNull(policy.getEvenPageFooter());

        assertEquals(policy.getFirstPageHeader(), policy.getHeader(1));
        assertEquals(policy.getDefaultHeader(), policy.getHeader(2));
        assertEquals(policy.getDefaultHeader(), policy.getHeader(3));
        assertEquals(policy.getFirstPageFooter(), policy.getFooter(1));
        assertEquals(policy.getDefaultFooter(), policy.getFooter(2));
        assertEquals(policy.getDefaultFooter(), policy.getFooter(3));
    }

    @Test
    void testCreate() throws Exception {
        try (XWPFDocument doc = new XWPFDocument()) {
            assertNull(doc.getHeaderFooterPolicy());
            assertEquals(0, doc.getHeaderList().size());
            assertEquals(0, doc.getFooterList().size());

            XWPFHeaderFooterPolicy policy = doc.createHeaderFooterPolicy();
            assertNotNull(doc.getHeaderFooterPolicy());
            assertEquals(0, doc.getHeaderList().size());
            assertEquals(0, doc.getFooterList().size());

            // Create a header and a footer
            XWPFHeader header = policy.createHeader(XWPFHeaderFooterPolicy.DEFAULT);
            XWPFFooter footer = policy.createFooter(XWPFHeaderFooterPolicy.DEFAULT);
            header.createParagraph().createRun().setText("Header Hello");
            footer.createParagraph().createRun().setText("Footer Bye");


            // Save, re-load, and check
            try (XWPFDocument docBack = XWPFTestDataSamples.writeOutAndReadBack(doc)) {
                assertNotNull(docBack.getHeaderFooterPolicy());
                assertEquals(1, docBack.getHeaderList().size());
                assertEquals(1, docBack.getFooterList().size());

                assertEquals("Header Hello\n", docBack.getHeaderList().get(0).getText());
                assertEquals("Footer Bye\n", docBack.getFooterList().get(0).getText());
            }
        }
    }

    @Test
    void testContents() {
        XWPFHeaderFooterPolicy policy;

        // Test a few simple bits off a simple header
        policy = diffFirst.getHeaderFooterPolicy();

        assertEquals(
                "I am the header on the first page, and I" + '\u2019' + "m nice and simple\n",
                policy.getFirstPageHeader().getText()
        );
        assertEquals(
                "First header column!\tMid header\tRight header!\n",
                policy.getDefaultHeader().getText()
        );


        // And a few bits off a more complex header
        policy = oddEven.getHeaderFooterPolicy();

        assertEquals(
                "[ODD Page Header text]\n\n",
                policy.getDefaultHeader().getText()
        );
        assertEquals(
                "[This is an Even Page, with a Header]\n\n",
                policy.getEvenPageHeader().getText()
        );
    }
}
