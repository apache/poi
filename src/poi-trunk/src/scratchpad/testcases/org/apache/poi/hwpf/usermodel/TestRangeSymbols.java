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

package org.apache.poi.hwpf.usermodel;

import java.io.IOException;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.hwpf.model.Ffn;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * API for processing of symbols, see Bugzilla 49908
 */
public final class TestRangeSymbols {
    @Test
    public void test() throws IOException {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Bug49908.doc");

        Range range = doc.getRange();

        assertTrue(range.numCharacterRuns() >= 2);
        CharacterRun chr = range.getCharacterRun(0);
        assertFalse(chr.isSymbol());

        chr = range.getCharacterRun(1);
        assertTrue(chr.isSymbol());
        assertEquals("\u0028", chr.text());
        Ffn symbolFont = chr.getSymbolFont();
        assertNotNull(symbolFont);
        assertEquals("Wingdings", symbolFont.getMainFontName());
        assertEquals(0xf028, chr.getSymbolCharacter());
    }

    @Test
    public void test61586() throws IOException {
        HWPFDocument document = HWPFTestDataSamples.openSampleFile("61586.doc");
        assertEquals("\r" +
                "\r" +
                "TEST( \r" +
                "111 (g.h/mL (AUC) and 15 (g/mL (Cmax).  \r" +
                "TEST( \r" +
                "Greek mu(\r" +
                "(\r\r", document.getText().toString());

        Range range = document.getRange();

        assertEquals(26, range.numCharacterRuns());

        // newline
        CharacterRun chr = range.getCharacterRun(0);
        assertFalse(chr.isSymbol());
        assertEquals("\r", chr.text());

        // "TEST"
        chr = range.getCharacterRun(2);
        assertFalse(chr.isSymbol());
        assertEquals("TEST", chr.text());

        // "registered" symbol
        chr = range.getCharacterRun(3);
        assertTrue(chr.isSymbol());
        assertEquals("\u0028", chr.text());
        Ffn symbolFont = chr.getSymbolFont();
        assertNotNull(symbolFont);
        assertEquals("Symbol", symbolFont.getMainFontName());
        assertEquals(0xf0e2, chr.getSymbolCharacter());
        assertEquals("(", chr.text());

        // Greek "mu" symbol
        chr = range.getCharacterRun(8);
        assertTrue(chr.isSymbol());
        assertEquals("\u0028", chr.text());
        symbolFont = chr.getSymbolFont();
        assertNotNull(symbolFont);
        assertEquals("Symbol", symbolFont.getMainFontName());
        assertEquals(0xf06d, chr.getSymbolCharacter());

        // Greek "mu" symbol
        chr = range.getCharacterRun(12);
        assertTrue(chr.isSymbol());
        assertEquals("\u0028", chr.text());
        symbolFont = chr.getSymbolFont();
        assertNotNull(symbolFont);
        assertEquals("Symbol", symbolFont.getMainFontName());
        assertEquals(0xf06d, chr.getSymbolCharacter());

        // "registered" symbol
        chr = range.getCharacterRun(17);
        assertTrue(chr.isSymbol());
        assertEquals("\u0028", chr.text());
        symbolFont = chr.getSymbolFont();
        assertNotNull(symbolFont);
        assertEquals("Symbol", symbolFont.getMainFontName());
        assertEquals(0xf0e2, chr.getSymbolCharacter());

        // Greek "mu" symbol
        chr = range.getCharacterRun(21);
        assertTrue(chr.isSymbol());
        assertEquals("\u0028", chr.text());
        symbolFont = chr.getSymbolFont();
        assertNotNull(symbolFont);
        assertEquals("Symbol", symbolFont.getMainFontName());
        assertEquals(0xf06d, chr.getSymbolCharacter());

        // normal bracket, not a symbol
        chr = range.getCharacterRun(23);
        assertFalse(chr.isSymbol());
        assertEquals("\u0028", chr.text());

        document.close();
    }
}