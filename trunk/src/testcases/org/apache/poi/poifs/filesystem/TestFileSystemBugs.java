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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;

/**
 * Tests bugs across both POIFSFileSystem and NPOIFSFileSystem
 */
public final class TestFileSystemBugs extends TestCase {
    protected static POIDataSamples _samples = POIDataSamples.getPOIFSInstance();
    protected static POIDataSamples _ssSamples = POIDataSamples.getSpreadSheetInstance();
    
    protected List<NPOIFSFileSystem> openedFSs;
    @Override
    protected void tearDown() throws Exception {
        if (openedFSs != null && !openedFSs.isEmpty()) {
            for (NPOIFSFileSystem fs : openedFSs) {
                try {
                    fs.close();
                } catch (Exception e) {
                    System.err.println("Error closing FS: " + e);
                }
            }
        }
        openedFSs = null;
    }
    protected DirectoryNode[] openSample(String name, boolean oldFails) throws Exception {
        return openSamples(new InputStream[] {
                _samples.openResourceAsStream(name),
                _samples.openResourceAsStream(name)
        }, oldFails);
    }
    protected DirectoryNode[] openSSSample(String name, boolean oldFails) throws Exception {
        return openSamples(new InputStream[] {
                _ssSamples.openResourceAsStream(name),
                _ssSamples.openResourceAsStream(name)
        }, oldFails);
    }
    protected DirectoryNode[] openSamples(InputStream[] inps, boolean oldFails) throws Exception {
        NPOIFSFileSystem nfs = new NPOIFSFileSystem(inps[0]);
        if (openedFSs == null) openedFSs = new ArrayList<NPOIFSFileSystem>();
        openedFSs.add(nfs);
        
        OPOIFSFileSystem ofs = null;
        try {
            ofs = new OPOIFSFileSystem(inps[1]);
            if (oldFails) fail("POIFSFileSystem should have failed but didn't");
        } catch (Exception e) {
            if (!oldFails) throw e;
        }

        if (ofs == null) return new DirectoryNode[] { nfs.getRoot() };
        return new DirectoryNode[] { ofs.getRoot(), nfs.getRoot() };
    }

    /**
     * Test that we can open files that come via Lotus notes.
     * These have a top level directory without a name....
     */
    public void testNotesOLE2Files() throws Exception {
        // Check the contents
        for (DirectoryNode root : openSample("Notes.ole2", false)) {
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
            assertEquals(true, entry.isDocumentEntry());
            assertEquals("\u0001Ole10Native", entry.getName());

            entry = it.next();
            assertEquals(true, entry.isDocumentEntry());
            assertEquals("\u0001CompObj", entry.getName());
        }
    }
    
    /**
     * Ensure that a file with a corrupted property in the
     *  properties table can still be loaded, and the remaining
     *  properties used
     * Note - only works for NPOIFSFileSystem, POIFSFileSystem
     *  can't cope with this level of corruption
     */
    public void testCorruptedProperties() throws Exception {
        for (DirectoryNode root : openSample("unknown_properties.msg", true)) {
            assertEquals(42, root.getEntryCount());
        }
    }
    
    /**
     * With heavily nested documents, ensure we still re-write the same
     */
    public void testHeavilyNestedReWrite() throws Exception {
        for (DirectoryNode root : openSSSample("ex42570-20305.xls", false)) {
            // Record the structure
            Map<String,Integer> entries = new HashMap<String, Integer>();
            fetchSizes("/", root, entries);
            
            // Prepare to copy
            DirectoryNode dest;
            if (root.getNFileSystem() != null) {
                dest = (new NPOIFSFileSystem()).getRoot();
            } else {
                dest = (new OPOIFSFileSystem()).getRoot();
            }
            
            // Copy over
            EntryUtils.copyNodes(root, dest);
            
            // Re-load, always as NPOIFS
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (root.getNFileSystem() != null) {
                root.getNFileSystem().writeFilesystem(baos);
            } else {
                root.getOFileSystem().writeFilesystem(baos);
            }
            NPOIFSFileSystem read = new NPOIFSFileSystem(
                    new ByteArrayInputStream(baos.toByteArray()));
            
            // Check the structure matches
            checkSizes("/", read.getRoot(), entries);
        }
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
