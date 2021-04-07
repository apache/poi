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

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test the table which handles author revision marks
 */
public final class TestRevisionMarkAuthorTable {
    /**
     * Tests that an empty file doesn't have one
     */
    @Test
    void testEmptyDocument() {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile("empty.doc");

        RevisionMarkAuthorTable rmt = doc.getRevisionMarkAuthorTable();
        assertNull(rmt);
    }

    /**
     * Tests that we can load a document with
     * only simple entries in the table
     */
    @Test
    void testSimpleDocument() {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile("two_images.doc");

        RevisionMarkAuthorTable rmt = doc.getRevisionMarkAuthorTable();
        assertNotNull(rmt);
        assertEquals(1, rmt.getSize());
        assertEquals("Unknown", rmt.getAuthor(0));

        assertNull(rmt.getAuthor(1));
        assertNull(rmt.getAuthor(2));
        assertNull(rmt.getAuthor(3));
    }

    /**
     * Several authors, one of whom has no name
     */
    @Test
    void testMultipleAuthors() {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile("MarkAuthorsTable.doc");

        RevisionMarkAuthorTable rmt = doc.getRevisionMarkAuthorTable();
        assertNotNull(rmt);
        assertEquals(4, rmt.getSize());
        assertEquals("Unknown", rmt.getAuthor(0));
        assertEquals("BSanders", rmt.getAuthor(1));
        assertEquals(" ", rmt.getAuthor(2));
        assertEquals("Ryan Lauck", rmt.getAuthor(3));

        assertNull(rmt.getAuthor(4));
        assertNull(rmt.getAuthor(5));
        assertNull(rmt.getAuthor(6));
    }
}
