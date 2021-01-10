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
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.math.BigInteger;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STFtnEdn;

class TestXWPFEndnotes {

    @Test
    void testCreateEndnotes() throws IOException{
        try (XWPFDocument docOut = new XWPFDocument()) {
            XWPFEndnotes footnotes = docOut.createEndnotes();
            assertNotNull(footnotes);

            XWPFEndnotes secondFootnotes = docOut.createEndnotes();
            assertSame(footnotes, secondFootnotes);
        }
    }

    @Test
    void testAddEndnotesToDocument() throws IOException {
        try (XWPFDocument docOut = new XWPFDocument()) {
            // NOTE: XWPFDocument.createEndnote() delegates directly
            //       to XWPFFootnotes.createEndnote() so this tests
            //       both creation of new XWPFFootnotes in document
            //       and XWPFFootnotes.createEndnote();
            XWPFEndnote endnote = docOut.createEndnote();
            BigInteger noteId = endnote.getId();

            XWPFDocument docIn = XWPFTestDataSamples.writeOutAndReadBack(docOut);

            XWPFEndnote note = docIn.getEndnoteByID(noteId.intValue());
            assertNotNull(note);
            assertEquals(STFtnEdn.NORMAL, note.getCTFtnEdn().getType());
        }
    }

}

