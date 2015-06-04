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
import java.util.List;

import junit.framework.TestCase;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdn;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STFtnEdn;

public class TestXWPFFootnotes extends TestCase {

    public void testAddFootnotesToDocument() throws IOException {
        XWPFDocument docOut = new XWPFDocument();

        BigInteger noteId = BigInteger.valueOf(1);

        XWPFFootnotes footnotes = docOut.createFootnotes();
        CTFtnEdn ctNote = CTFtnEdn.Factory.newInstance();
        ctNote.setId(noteId);
        ctNote.setType(STFtnEdn.NORMAL);
        footnotes.addFootnote(ctNote);

        XWPFDocument docIn = XWPFTestDataSamples.writeOutAndReadBack(docOut);

        XWPFFootnote note = docIn.getFootnoteByID(noteId.intValue());
        assertEquals(note.getCTFtnEdn().getType(), STFtnEdn.NORMAL);
    }

    /**
     * Bug 55066 - avoid double loading the footnotes
     */
    public void testLoadFootnotesOnce() throws IOException {
        XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Bug54849.docx");
        List<XWPFFootnote> footnotes = doc.getFootnotes();
        int hits = 0;
        for (XWPFFootnote fn : footnotes) {
            for (IBodyElement e : fn.getBodyElements()) {
                if (e instanceof XWPFParagraph) {
                    String txt = ((XWPFParagraph) e).getText();
                    if (txt.indexOf("Footnote_sdt") > -1) {
                        hits++;
                    }
                }
            }
        }
        assertEquals("Load footnotes once", 1, hits);
    }
}

