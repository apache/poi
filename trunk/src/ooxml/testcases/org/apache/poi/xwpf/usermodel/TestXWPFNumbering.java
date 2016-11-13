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

import java.io.IOException;
import java.math.BigInteger;

import junit.framework.TestCase;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumLvl;

public class TestXWPFNumbering extends TestCase {

    public void testCompareAbstractNum() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Numbering.docx");
        XWPFNumbering numbering = doc.getNumbering();
        BigInteger numId = BigInteger.valueOf(1);
        assertTrue(numbering.numExist(numId));
        XWPFNum num = numbering.getNum(numId);
        BigInteger abstrNumId = num.getCTNum().getAbstractNumId().getVal();
        XWPFAbstractNum abstractNum = numbering.getAbstractNum(abstrNumId);
        BigInteger compareAbstractNum = numbering.getIdOfAbstractNum(abstractNum);
        assertEquals(abstrNumId, compareAbstractNum);
    }

    public void testAddNumberingToDoc() throws IOException {
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

    public void testGetNumIlvl() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Numbering.docx");
        BigInteger numIlvl = BigInteger.valueOf(0);
        assertEquals(numIlvl, doc.getParagraphs().get(0).getNumIlvl());
        numIlvl = BigInteger.valueOf(1);
        assertEquals(numIlvl, doc.getParagraphs().get(5).getNumIlvl());
    }

    public void testGetNumFmt() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Numbering.docx");
        assertEquals("bullet", doc.getParagraphs().get(0).getNumFmt());
        assertEquals("bullet", doc.getParagraphs().get(1).getNumFmt());
        assertEquals("bullet", doc.getParagraphs().get(2).getNumFmt());
        assertEquals("bullet", doc.getParagraphs().get(3).getNumFmt());
        assertEquals("decimal", doc.getParagraphs().get(4).getNumFmt());
        assertEquals("lowerLetter", doc.getParagraphs().get(5).getNumFmt());
        assertEquals("lowerRoman", doc.getParagraphs().get(6).getNumFmt());
    }

    public void testLvlText() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Numbering.docx");

        assertEquals("%1.%2.%3.", doc.getParagraphs().get(12).getNumLevelText());

        assertEquals("NEW-%1-FORMAT", doc.getParagraphs().get(14).getNumLevelText());

        XWPFParagraph p = doc.getParagraphs().get(18);
        assertEquals("%1.", p.getNumLevelText());
        //test that null doesn't throw NPE
        assertNull(p.getNumFmt());
    }

    public void testOverrideList() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("NumberingWOverrides.docx");
        XWPFParagraph p = doc.getParagraphs().get(4);
        XWPFNumbering numbering = doc.getNumbering();
        CTNum ctNum = numbering.getNum(p.getNumID()).getCTNum();
        assertEquals(9, ctNum.sizeOfLvlOverrideArray());
        CTNumLvl ctNumLvl = ctNum.getLvlOverrideArray(0);
        assertEquals("upperLetter", ctNumLvl.getLvl().getNumFmt().getVal().toString());
    }

}
