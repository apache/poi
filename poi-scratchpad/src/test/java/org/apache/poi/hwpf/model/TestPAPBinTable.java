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

package org.apache.poi.hwpf.model;

import static org.apache.poi.hwpf.HWPFTestDataSamples.openSampleFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hwpf.HWPFDocFixture;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.WordToTextConverter;
import org.apache.poi.hwpf.model.io.HWPFFileSystem;
import org.apache.poi.hwpf.sprm.ParagraphSprmUncompressor;
import org.apache.poi.hwpf.sprm.SprmBuffer;
import org.apache.poi.hwpf.usermodel.ParagraphProperties;
import org.junit.jupiter.api.Test;

public final class TestPAPBinTable {

    @Test
    void testObIs() throws Exception {
        // shall not fail with assertions on
        try (HWPFDocument doc = openSampleFile("ob_is.doc")) {
            assertNotNull(WordToTextConverter.getText(doc));
        }
    }

    @Test
    void testReadWrite() throws IOException {
        // @todo verify the constructors
        HWPFDocFixture _hWPFDocFixture = new HWPFDocFixture( this, HWPFDocFixture.DEFAULT_TEST_FILE );

        _hWPFDocFixture.setUp();
        TextPieceTable fakeTPT = new TextPieceTable();

        FileInformationBlock fib = _hWPFDocFixture._fib;
        byte[] mainStream = _hWPFDocFixture._mainStream;
        byte[] tableStream = _hWPFDocFixture._tableStream;

        PAPBinTable _pAPBinTable = new PAPBinTable( mainStream, tableStream,
                null, fib.getFcPlcfbtePapx(), fib.getLcbPlcfbtePapx(), fakeTPT );

        HWPFFileSystem fileSys = new HWPFFileSystem();
        ByteArrayOutputStream tableOut = fileSys.getStream( "1Table" );
        ByteArrayOutputStream mainOut = fileSys.getStream( "WordDocument" );
        ByteArrayOutputStream dataOut = fileSys.getStream("Data");
        _pAPBinTable.writeTo(mainOut, tableOut, dataOut, fakeTPT);

        byte[] newTableStream = tableOut.toByteArray();
        byte[] newMainStream = mainOut.toByteArray();

        PAPBinTable newBinTable = new PAPBinTable( newMainStream,
                newTableStream, null, 0, newTableStream.length, fakeTPT );

        List<PAPX> oldTextRuns = _pAPBinTable.getParagraphs();
        List<PAPX> newTextRuns = newBinTable.getParagraphs();

        assertEquals( oldTextRuns.size(), newTextRuns.size() );

        int size = oldTextRuns.size();
        for ( int x = 0; x < size; x++ )
        {
            PAPX oldNode = oldTextRuns.get( x );
            PAPX newNode = newTextRuns.get( x );

            assertEquals(oldNode, newNode);
        }

        _hWPFDocFixture.tearDown();
    }

    @Test
    void testRebuildParagraphMarkInsideNestedCellPapx() {
        // one PAPX over "A\r\r" marking the end of a nested table cell (GitHub #1499)
        SprmBuffer sprms = new SprmBuffer(2);
        sprms.addSprm((short) 0x2416, (byte) 1); // sprmPFInTable
        sprms.addSprm((short) 0x6649, 2); // sprmPItap
        sprms.addSprm((short) 0x244B, (byte) 1); // sprmPFInnerTableCell
        List<PAPX> paragraphs = rebuild("A\r\r", new PAPX(0, 3, sprms));

        assertEquals(2, paragraphs.size());
        ParagraphProperties first = getProperties(paragraphs.get(0), 0, 2);
        assertTrue(first.getFInTable(), "paragraph before the internal CR stays in the table");
        assertEquals(2, first.getItap());
        assertFalse(first.getFInnerTableCell(), "only the interval's last paragraph ends the cell");

        ParagraphProperties last = getProperties(paragraphs.get(1), 2, 3);
        assertTrue(last.getFInTable());
        assertEquals(2, last.getItap());
        assertTrue(last.getFInnerTableCell());
    }

    @Test
    void testRebuildParagraphMarkInsideRowEndPapx() {
        SprmBuffer sprms = new SprmBuffer(2);
        sprms.addSprm((short) 0x2416, (byte) 1); // sprmPFInTable
        sprms.addSprm((short) 0x2417, (byte) 1); // sprmPFTtp
        sprms.addSprm((short) 0x2403, (byte) 1); // sprmPJc
        List<PAPX> paragraphs = rebuild("A\r\u0007", new PAPX(0, 3, sprms));

        assertEquals(2, paragraphs.size());
        ParagraphProperties first = getProperties(paragraphs.get(0), 0, 2);
        assertTrue(first.getFInTable());
        assertEquals(1, first.getJc());
        assertFalse(first.getFTtp(), "only the interval's last paragraph ends the row");

        ParagraphProperties last = getProperties(paragraphs.get(1), 2, 3);
        assertTrue(last.getFTtp());
        assertEquals(1, last.getJc());
    }

    @Test
    void testRebuildParagraphWithoutPapx() {
        SprmBuffer sprms = new SprmBuffer(2);
        sprms.addSprm((short) 0x2403, (byte) 1); // sprmPJc
        List<PAPX> paragraphs = rebuild("A\rB\r", new PAPX(2, 4, sprms));

        assertEquals(2, paragraphs.size());
        assertEquals(0, getProperties(paragraphs.get(0), 0, 2).getJc(), "no PAPX covers the first paragraph");
        assertEquals(1, getProperties(paragraphs.get(1), 2, 4).getJc());
    }

    private static List<PAPX> rebuild(String text, PAPX papx) {
        StringBuilder docText = new StringBuilder(text);
        PAPBinTable table = new PAPBinTable();
        table.getParagraphs().add(papx);
        ComplexFileTable complexFileTable = new ComplexFileTable();
        complexFileTable.getTextPieceTable().add(new SinglentonTextPiece(docText));
        table.rebuild(docText, complexFileTable);
        return table.getParagraphs();
    }

    private static ParagraphProperties getProperties(PAPX papx, int start, int end) {
        assertEquals(start, papx.getStart());
        assertEquals(end, papx.getEnd());
        return ParagraphSprmUncompressor.uncompressPAP(new ParagraphProperties(), papx.getGrpprl(), 2);
    }
}
