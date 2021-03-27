
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Class to test FilteringDirectoryNode functionality
 */
final class TestFilteringDirectoryNode {
    private POIFSFileSystem fs;
    private DirectoryEntry dirA;
    private DirectoryEntry dirAA;
    private DirectoryEntry dirB;
    private DocumentEntry eRoot;
    private DocumentEntry eA;
    private DocumentEntry eAA;

    @BeforeEach
    void setUp() throws Exception {
        fs = new POIFSFileSystem();
        dirA = fs.createDirectory("DirA");
        dirB = fs.createDirectory("DirB");
        dirAA = dirA.createDirectory("DirAA");
        eRoot = fs.getRoot().createDocument("Root", new ByteArrayInputStream(new byte[]{}));
        eA = dirA.createDocument("NA", new ByteArrayInputStream(new byte[]{}));
        eAA = dirAA.createDocument("NAA", new ByteArrayInputStream(new byte[]{}));
    }

    @Test
    void testNoFiltering() throws Exception {
        FilteringDirectoryNode d = new FilteringDirectoryNode(fs.getRoot(), new HashSet<>());
        assertEquals(3, d.getEntryCount());
        assertEquals(dirA.getName(), d.getEntry(dirA.getName()).getName());

        assertTrue(d.getEntry(dirA.getName()).isDirectoryEntry());
        assertFalse(d.getEntry(dirA.getName()).isDocumentEntry());

        assertTrue(d.getEntry(dirB.getName()).isDirectoryEntry());
        assertFalse(d.getEntry(dirB.getName()).isDocumentEntry());

        assertFalse(d.getEntry(eRoot.getName()).isDirectoryEntry());
        assertTrue(d.getEntry(eRoot.getName()).isDocumentEntry());

        Iterator<Entry> i = d.getEntries();
        assertEquals(dirA, i.next());
        assertEquals(dirB, i.next());
        assertEquals(eRoot, i.next());
        assertThrows(NoSuchElementException.class, i::next, "Should throw NoSuchElementException when depleted");
    }

    @Test
    void testChildFiltering() throws Exception {
        List<String> excl = Arrays.asList("NotThere", "AlsoNotThere", eRoot.getName());
        FilteringDirectoryNode d1 = new FilteringDirectoryNode(fs.getRoot(), excl);

        assertEquals(2, d1.getEntryCount());
        assertTrue(d1.hasEntry(dirA.getName()));
        assertTrue(d1.hasEntry(dirB.getName()));
        assertFalse(d1.hasEntry(eRoot.getName()));

        assertEquals(dirA, d1.getEntry(dirA.getName()));
        assertEquals(dirB, d1.getEntry(dirB.getName()));
        assertThrows(FileNotFoundException.class, () -> d1.getEntry(eRoot.getName()));

        Iterator<Entry> i = d1.getEntries();
        assertEquals(dirA, i.next());
        assertEquals(dirB, i.next());
        assertThrows(NoSuchElementException.class, i::next, "Should throw NoSuchElementException when depleted");


        // Filter more
        excl = Arrays.asList("NotThere", "AlsoNotThere", eRoot.getName(), dirA.getName());
        FilteringDirectoryNode d2 = new FilteringDirectoryNode(fs.getRoot(), excl);

        assertEquals(1, d2.getEntryCount());
        assertFalse(d2.hasEntry(dirA.getName()));
        assertTrue(d2.hasEntry(dirB.getName()));
        assertFalse(d2.hasEntry(eRoot.getName()));
        assertThrows(FileNotFoundException.class, () -> d2.getEntry(dirA.getName()), "Should be filtered");
        assertEquals(dirB, d2.getEntry(dirB.getName()));
        assertThrows(FileNotFoundException.class, () -> d2.getEntry(eRoot.getName()), "Should be filtered");

        i = d2.getEntries();
        assertEquals(dirB, i.next());
        assertThrows(NoSuchElementException.class, i::next, "Should throw NoSuchElementException when depleted");

        // Filter everything
        excl = Arrays.asList("NotThere", eRoot.getName(), dirA.getName(), dirB.getName());
        FilteringDirectoryNode d3 = new FilteringDirectoryNode(fs.getRoot(), excl);

        assertEquals(0, d3.getEntryCount());
        assertFalse(d3.hasEntry(dirA.getName()));
        assertFalse(d3.hasEntry(dirB.getName()));
        assertFalse(d3.hasEntry(eRoot.getName()));
        assertThrows(FileNotFoundException.class, () -> d3.getEntry(dirA.getName()), "Should be filtered");
        assertThrows(FileNotFoundException.class, () -> d3.getEntry(dirB.getName()), "Should be filtered");
        assertThrows(FileNotFoundException.class, () -> d3.getEntry(eRoot.getName()), "Should be filtered");

        i = d3.getEntries();
        assertThrows(NoSuchElementException.class, i::next, "Should throw NoSuchElementException when depleted");
    }

    @Test
    void testNestedFiltering() throws Exception {
        List<String> excl = Arrays.asList(dirA.getName() + "/" + "MadeUp",
            dirA.getName() + "/" + eA.getName(),
            dirA.getName() + "/" + dirAA.getName() + "/Test",
            eRoot.getName());
        FilteringDirectoryNode d = new FilteringDirectoryNode(fs.getRoot(), excl);

        // Check main
        assertEquals(2, d.getEntryCount());
        assertTrue(d.hasEntry(dirA.getName()));
        assertTrue(d.hasEntry(dirB.getName()));
        assertFalse(d.hasEntry(eRoot.getName()));

        // Check filtering down
        assertTrue(d.getEntry(dirA.getName()) instanceof FilteringDirectoryNode);
        assertFalse(d.getEntry(dirB.getName()) instanceof FilteringDirectoryNode);

        DirectoryEntry fdA = (DirectoryEntry) d.getEntry(dirA.getName());
        assertFalse(fdA.hasEntry(eA.getName()));
        assertTrue(fdA.hasEntry(dirAA.getName()));

        DirectoryEntry fdAA = (DirectoryEntry) fdA.getEntry(dirAA.getName());
        assertTrue(fdAA.hasEntry(eAA.getName()));
    }

    @Test
    void testNullDirectory() {
        assertThrows(IllegalArgumentException.class, () -> new FilteringDirectoryNode(null, null));
    }
}