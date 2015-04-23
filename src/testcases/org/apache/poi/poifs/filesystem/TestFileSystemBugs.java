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

package org.apache.poi.poifs.filesystem;

import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;

/**
 * Tests bugs across both POIFSFileSystem and NPOIFSFileSystem
 */
public final class TestFileSystemBugs extends TestCase {
    /**
     * Test that we can open files that come via Lotus notes.
     * These have a top level directory without a name....
     */
    public void testNotesOLE2Files() throws Exception {
        POIDataSamples _samples = POIDataSamples.getPOIFSInstance();

        // Open the file up
        POIFSFileSystem fs = new POIFSFileSystem(
                _samples.openResourceAsStream("Notes.ole2")
                );

        // Check the contents
        assertEquals(1, fs.getRoot().getEntryCount());

        Entry entry = fs.getRoot().getEntries().next();
        assertTrue(entry.isDirectoryEntry());
        assertTrue(entry instanceof DirectoryEntry);

        // The directory lacks a name!
        DirectoryEntry dir = (DirectoryEntry)entry;
        assertEquals("", dir.getName());

        // Has two children
        assertEquals(2, dir.getEntryCount());

        // Check them
        Iterator<Entry> it = dir.getEntries();
        entry = it.next();
        assertEquals(true, entry.isDocumentEntry());
        assertEquals("\u0001Ole10Native", entry.getName());

        entry = it.next();
        assertEquals(true, entry.isDocumentEntry());
        assertEquals("\u0001CompObj", entry.getName());
    }
}
