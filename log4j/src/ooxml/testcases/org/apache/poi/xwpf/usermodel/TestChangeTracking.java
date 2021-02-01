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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.junit.jupiter.api.Test;

class TestChangeTracking {
    @Test
    void detection() throws Exception {
        try (XWPFDocument documentWithoutChangeTracking = XWPFTestDataSamples.openSampleDocument("bug56075-changeTracking_off.docx")) {
            assertFalse(documentWithoutChangeTracking.isTrackRevisions());

            try (XWPFDocument documentWithChangeTracking = XWPFTestDataSamples.openSampleDocument("bug56075-changeTracking_on.docx")) {
                assertTrue(documentWithChangeTracking.isTrackRevisions());
            }
        }
    }

    @Test
    void activateChangeTracking() throws Exception {
        try (XWPFDocument document = XWPFTestDataSamples.openSampleDocument("bug56075-changeTracking_off.docx")) {
            assertFalse(document.isTrackRevisions());

            document.setTrackRevisions(true);

            assertTrue(document.isTrackRevisions());
        }
    }

    @Test
    void integration() throws Exception {
        try (XWPFDocument doc = new XWPFDocument()) {

            XWPFParagraph p1 = doc.createParagraph();

            XWPFRun r1 = p1.createRun();
            r1.setText("Lorem ipsum dolor sit amet.");
            doc.setTrackRevisions(true);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(out.toByteArray());
            XWPFDocument document = new XWPFDocument(inputStream);
            inputStream.close();

            assertTrue(document.isTrackRevisions());
        }
    }
}
