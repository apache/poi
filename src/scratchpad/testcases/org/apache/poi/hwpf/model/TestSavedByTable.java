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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link SavedByTable} and {@link SavedByEntry}.
 */
public final class TestSavedByTable {

    /** The expected entries in the test document. */
    private final List<SavedByEntry> expected = Arrays.asList(
        new SavedByEntry("cic22", "C:\\DOCUME~1\\phamill\\LOCALS~1\\Temp\\AutoRecovery save of Iraq - security.asd"),
        new SavedByEntry("cic22", "C:\\DOCUME~1\\phamill\\LOCALS~1\\Temp\\AutoRecovery save of Iraq - security.asd"),
        new SavedByEntry("cic22", "C:\\DOCUME~1\\phamill\\LOCALS~1\\Temp\\AutoRecovery save of Iraq - security.asd"),
        new SavedByEntry("JPratt", "C:\\TEMP\\Iraq - security.doc"),
        new SavedByEntry("JPratt", "A:\\Iraq - security.doc"),
        new SavedByEntry("ablackshaw", "C:\\ABlackshaw\\Iraq - security.doc"),
        new SavedByEntry("ablackshaw", "C:\\ABlackshaw\\A;Iraq - security.doc"),
        new SavedByEntry("ablackshaw", "A:\\Iraq - security.doc"),
        new SavedByEntry("MKhan", "C:\\TEMP\\Iraq - security.doc"),
        new SavedByEntry("MKhan", "C:\\WINNT\\Profiles\\mkhan\\Desktop\\Iraq.doc")
    );

    /**
     * Tests reading in the entries, comparing them against the expected
     * entries. Then tests writing the document out and reading the entries yet
     * again.
     *
     * @throws Exception if an unexpected error occurs.
     */
    @Test
    void testReadWrite() throws IOException {
        // This document is widely available on the internet as "blair.doc".
        // I tried stripping the content and saving the document but my version
        // of Word (from Office XP) strips this table out.
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile("saved-by-table.doc");

        // Check what we just read.
        assertEquals( expected, doc.getSavedByTable().getEntries(), "List of saved-by entries was not as expected" );

        // Now write the entire document out, and read it back in...
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        doc.write(byteStream);
        InputStream copyStream = new ByteArrayInputStream(byteStream.toByteArray());
        doc.close();
        HWPFDocument copy = new HWPFDocument(copyStream);

        // And check again.
        assertEquals( expected, copy.getSavedByTable().getEntries(), "List of saved-by entries was incorrect after writing" );

        copy.close();
    }
}
