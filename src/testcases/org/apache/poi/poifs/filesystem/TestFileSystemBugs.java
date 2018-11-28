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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.POIDataSamples;
import org.junit.After;
import org.junit.Test;

/**
 * Tests bugs across both POIFSFileSystem and NPOIFSFileSystem
 */
public final class TestFileSystemBugs {
    private static POIDataSamples _samples = POIDataSamples.getPOIFSInstance();
    private static POIDataSamples _ssSamples = POIDataSamples.getSpreadSheetInstance();

    private List<POIFSFileSystem> openedFSs;

    @After
    public void tearDown() {
        if (openedFSs != null && !openedFSs.isEmpty()) {
            for (POIFSFileSystem fs : openedFSs) {
                try {
                    fs.close();
                } catch (Exception e) {
                    System.err.println("Error closing FS: " + e);
                }
            }
        }
        openedFSs = null;
    }

    private DirectoryNode openSample(String name) throws Exception {
        try (InputStream inps = _samples.openResourceAsStream(name)) {
            return openSample(inps);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private DirectoryNode openSSSample(String name) throws Exception {
        try (InputStream inps = _ssSamples.openResourceAsStream(name)) {
            return openSample(inps);
        }
    }

    private DirectoryNode openSample(InputStream inps) throws Exception {
        POIFSFileSystem nfs = new POIFSFileSystem(inps);
        if (openedFSs == null) {
            openedFSs = new ArrayList<>();
        }
        openedFSs.add(nfs);

        return nfs.getRoot();
    }

    /**
     * Test that we can open files that come via Lotus notes.
     * These have a top level directory without a name....
     */
    @Test
    public void testNotesOLE2Files() throws Exception {
        // Check the contents
        DirectoryNode root = openSample("Notes.ole2");
        assertEquals(1, root.getEntryCount());

        Entry entry = root.getEntries().next();
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
        assertTrue(entry.isDocumentEntry());
        assertEquals(Ole10Native.OLE10_NATIVE, entry.getName());

        entry = it.next();
        assertTrue(entry.isDocumentEntry());
        assertEquals("\u0001CompObj", entry.getName());
    }
    
    /**
     * Ensure that a file with a corrupted property in the
     *  properties table can still be loaded, and the remaining
     *  properties used
     * Note - only works for NPOIFSFileSystem, POIFSFileSystem
     *  can't cope with this level of corruption
     */
    @Test
    public void testCorruptedProperties() throws Exception {
        DirectoryNode root = openSample("unknown_properties.msg");
        assertEquals(42, root.getEntryCount());
    }
    
    /**
     * With heavily nested documents, ensure we still re-write the same
     */
    @Test
    public void testHeavilyNestedReWrite() throws Exception {
        DirectoryNode root = openSSSample("ex42570-20305.xls");
        // Record the structure
        Map<String,Integer> entries = new HashMap<>();
        fetchSizes("/", root, entries);

        // Prepare to copy
        DirectoryNode dest = new POIFSFileSystem().getRoot();

        // Copy over
        EntryUtils.copyNodes(root, dest);

        // Re-load, always as NPOIFS
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        root.getNFileSystem().writeFilesystem(baos);

        POIFSFileSystem read = new POIFSFileSystem(
                new ByteArrayInputStream(baos.toByteArray()));

        // Check the structure matches
        checkSizes("/", read.getRoot(), entries);
    }

    private void fetchSizes(String path, DirectoryNode dir, Map<String,Integer> entries) {
        for (Entry entry : dir) {
            if (entry instanceof DirectoryNode) {
                String ourPath = path + entry.getName() + "/";
                entries.put(ourPath, -1);
                fetchSizes(ourPath, (DirectoryNode)entry, entries);
            } else {
                DocumentNode doc = (DocumentNode)entry;
                entries.put(path+entry.getName(), doc.getSize());
            }
        }
    }
    private void checkSizes(String path, DirectoryNode dir, Map<String,Integer> entries) {
        for (Entry entry : dir) {
            if (entry instanceof DirectoryNode) {
                String ourPath = path + entry.getName() + "/";
                assertTrue(entries.containsKey(ourPath));
                assertEquals(-1, entries.get(ourPath).intValue());
                checkSizes(ourPath, (DirectoryNode)entry, entries);
            } else {
                DocumentNode doc = (DocumentNode)entry;
                assertEquals(entries.get(path+entry.getName()).intValue(), doc.getSize());
            }
        }
    }
}
