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

import static org.apache.poi.POITestCase.assertContains;

import java.io.IOException;

import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.junit.jupiter.api.Test;

/**
 * Tests for reading SmartTags from Word docx.
 */
public final class TestXWPFSmartTag {
    @Test
    void testSmartTags() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("smarttag-snippet.docx")) {
            XWPFParagraph p = doc.getParagraphArray(0);
            assertContains(p.getText(), "Carnegie Mellon University School of Computer Science");
            p = doc.getParagraphArray(2);
            assertContains(p.getText(), "Alice's Adventures");
        }
    }
}
