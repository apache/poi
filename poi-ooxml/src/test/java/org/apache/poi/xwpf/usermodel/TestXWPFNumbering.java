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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTAbstractNum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumLvl;

class TestXWPFNumbering {

    @Test
    void testCompareAbstractNum() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Numbering.docx")) {
            XWPFNumbering numbering = doc.getNumbering();
            BigInteger numId = BigInteger.valueOf(1);
            assertTrue(numbering.numExist(numId));
            XWPFNum num = numbering.getNum(numId);
            BigInteger abstrNumId = num.getCTNum().getAbstractNumId().getVal();
            XWPFAbstractNum abstractNum = numbering.getAbstractNum(abstrNumId);
            BigInteger compareAbstractNum = numbering.getIdOfAbstractNum(abstractNum);
            assertEquals(abstrNumId, compareAbstractNum);
        }
    }

    @Test
    void testAddNumberingToDoc() throws IOException {
        BigInteger abstractNumId = BigInteger.valueOf(1);
        BigInteger numId = BigInteger.valueOf(1);

        XWPFDocument docOut = new XWPFDocument();
        XWPFNumbering numbering = docOut.createNumbering();
        numId = numbering.addNum(abstractNumId);

        XWPFDocument docIn = XWPFTestDataSamples.writeOutAndReadBack(docOut);

        numbering = docIn.getNumbering();
        assertTrue(numbering.numExist(numId));
        XWPFNum num = numbering.getNum(numId);

        BigInteger compareAbstractNum = num.getCTNum().getAbstractNumId().getVal();
        assertEquals(abstractNumId, compareAbstractNum);
    }

    @Test
    void testAddAbstractNumIfAbstractNumNotEqualNull() throws IOException {
        BigInteger abstractNumId = BigInteger.valueOf(1);
        XWPFDocument docOut = new XWPFDocument();
        XWPFNumbering numbering = docOut.createNumbering();

        CTAbstractNum cTAbstractNum = CTAbstractNum.Factory.newInstance();
        // must set the AbstractNumId, Otherwise fail
        cTAbstractNum.setAbstractNumId(abstractNumId);
        XWPFAbstractNum abstractNum = new XWPFAbstractNum(cTAbstractNum);
        abstractNumId = numbering.addAbstractNum(abstractNum);
        BigInteger numId = numbering.addNum(abstractNumId);

        XWPFDocument docIn = XWPFTestDataSamples.writeOutAndReadBack(docOut);

        numbering = docIn.getNumbering();
        XWPFNum num = numbering.getNum(numId);
        BigInteger compareAbstractNum = num.getCTNum().getAbstractNumId().getVal();
        assertEquals(abstractNumId, compareAbstractNum);
    }

    @Test
    void testAddAbstractNumIfAbstractNumEqualNull() throws IOException {
        XWPFDocument docOut = new XWPFDocument();
        XWPFNumbering numbering = docOut.createNumbering();

        XWPFAbstractNum abstractNum = new XWPFAbstractNum();
        BigInteger abstractNumId = numbering.addAbstractNum(abstractNum);
        BigInteger numId = numbering.addNum(abstractNumId);

        XWPFDocument docIn = XWPFTestDataSamples.writeOutAndReadBack(docOut);

        numbering = docIn.getNumbering();
        XWPFNum num = numbering.getNum(numId);

        BigInteger compareAbstractNum = num.getCTNum().getAbstractNumId().getVal();
        assertEquals(abstractNumId, compareAbstractNum);
    }

    @Test
    void testGetNumIlvl() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Numbering.docx")) {
            BigInteger numIlvl = BigInteger.valueOf(0);
            assertEquals(numIlvl, doc.getParagraphs().get(0).getNumIlvl());
            numIlvl = BigInteger.valueOf(1);
            assertEquals(numIlvl, doc.getParagraphs().get(5).getNumIlvl());
        }
    }

    @Test
    void testGetNumFmt() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Numbering.docx")) {
            assertEquals("bullet", doc.getParagraphs().get(0).getNumFmt());
            assertEquals("bullet", doc.getParagraphs().get(1).getNumFmt());
            assertEquals("bullet", doc.getParagraphs().get(2).getNumFmt());
            assertEquals("bullet", doc.getParagraphs().get(3).getNumFmt());
            assertEquals("decimal", doc.getParagraphs().get(4).getNumFmt());
            assertEquals("lowerLetter", doc.getParagraphs().get(5).getNumFmt());
            assertEquals("lowerRoman", doc.getParagraphs().get(6).getNumFmt());
        }
    }

    @Test
    void testLvlText() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Numbering.docx")) {

            assertEquals("%1.%2.%3.", doc.getParagraphs().get(12).getNumLevelText());

            assertEquals("NEW-%1-FORMAT", doc.getParagraphs().get(14).getNumLevelText());

            XWPFParagraph p = doc.getParagraphs().get(18);
            assertEquals("%1.", p.getNumLevelText());
            //test that null doesn't throw NPE
            assertNull(p.getNumFmt());
        }
    }

    @Test
    void testOverrideList() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("NumberingWOverrides.docx")) {
            XWPFParagraph p = doc.getParagraphs().get(4);
            XWPFNumbering numbering = doc.getNumbering();
            CTNum ctNum = numbering.getNum(p.getNumID()).getCTNum();
            assertEquals(9, ctNum.sizeOfLvlOverrideArray());
            CTNumLvl ctNumLvl = ctNum.getLvlOverrideArray(0);
            assertEquals("upperLetter", ctNumLvl.getLvl().getNumFmt().getVal().toString());
        }
    }

    @Test
    void testAddAbstractNum() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("NumberingWithOutOfOrderId.docx")) {
            doc.getNumbering().addAbstractNum(new XWPFAbstractNum());
            long count = doc.getNumbering()
                    .getAbstractNums().stream()
                    .map(e -> e.getCTAbstractNum().getAbstractNumId().intValue())
                    .distinct().count();
            assertEquals(doc.getNumbering().getAbstractNums().size(), count);
        }
    }

    @Test
    void testNPE() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("clusterfuzz-testcase-minimized-POIXWPFFuzzer-6120975439364096.docx");
            OutputStream out = NullOutputStream.INSTANCE) {

            // settings and numbering are null for this malformed document
            assertNull(doc.getNumbering());
            assertNull(doc.getSettings());

            assertThrows(IllegalStateException.class, () ->
                doc.write(out), "Fails because settings are not populated in this malformed document");
        }
    }
}
