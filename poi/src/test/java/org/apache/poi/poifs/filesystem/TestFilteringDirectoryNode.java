
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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;

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
        assertEquals(dirA.getName(), d.getEntryCaseInsensitive(dirA.getName()).getName());

        assertTrue(d.getEntryCaseInsensitive(dirA.getName()).isDirectoryEntry());
        assertFalse(d.getEntryCaseInsensitive(dirA.getName()).isDocumentEntry());

        assertTrue(d.getEntryCaseInsensitive(dirB.getName()).isDirectoryEntry());
        assertFalse(d.getEntryCaseInsensitive(dirB.getName()).isDocumentEntry());

        assertFalse(d.getEntryCaseInsensitive(eRoot.getName()).isDirectoryEntry());
        assertTrue(d.getEntryCaseInsensitive(eRoot.getName()).isDocumentEntry());

        Iterator<Entry> i = d.getEntries();
        assertEquals(dirA, i.next());
        assertEquals(dirB, i.next());
        assertEquals(eRoot, i.next());
        assertThrows(NoSuchElementException.class, i::next, "Should throw NoSuchElementException when depleted");

        Spliterator<Entry> s = d.spliterator();
        s.tryAdvance(entry -> assertEquals(dirA, entry));
        s.tryAdvance(entry -> assertEquals(dirB, entry));
        s.tryAdvance(entry -> assertEquals(eRoot, entry));
        assertFalse(s.tryAdvance(entry -> fail("Should be depleted")), "Should return false when depleted");
    }

    @Test
    void testChildFiltering() throws Exception {
        List<String> excl = Arrays.asList("NotThere", "AlsoNotThere", eRoot.getName());
        FilteringDirectoryNode d1 = new FilteringDirectoryNode(fs.getRoot(), excl);

        assertEquals(2, d1.getEntryCount());
        assertTrue(d1.hasEntryCaseInsensitive(dirA.getName()));
        assertTrue(d1.hasEntryCaseInsensitive(dirB.getName()));
        assertFalse(d1.hasEntryCaseInsensitive(eRoot.getName()));

        assertEquals(dirA, d1.getEntryCaseInsensitive(dirA.getName()));
        assertEquals(dirB, d1.getEntryCaseInsensitive(dirB.getName()));
        assertThrows(FileNotFoundException.class, () -> d1.getEntryCaseInsensitive(eRoot.getName()));

        Iterator<Entry> i = d1.getEntries();
        assertEquals(dirA, i.next());
        assertEquals(dirB, i.next());
        assertThrows(NoSuchElementException.class, i::next, "Should throw NoSuchElementException when depleted");

        Spliterator<Entry> s1 = d1.spliterator();
        s1.tryAdvance(entry -> assertEquals(dirA, entry));
        s1.tryAdvance(entry -> assertEquals(dirB, entry));
        assertFalse(s1.tryAdvance(entry -> fail("Should be depleted")), "Should return false when depleted");


        // Filter more
        excl = Arrays.asList("NotThere", "AlsoNotThere", eRoot.getName(), dirA.getName());
        FilteringDirectoryNode d2 = new FilteringDirectoryNode(fs.getRoot(), excl);

        assertEquals(1, d2.getEntryCount());
        assertFalse(d2.hasEntryCaseInsensitive(dirA.getName()));
        assertTrue(d2.hasEntryCaseInsensitive(dirB.getName()));
        assertFalse(d2.hasEntryCaseInsensitive(eRoot.getName()));
        assertThrows(FileNotFoundException.class, () -> d2.getEntryCaseInsensitive(dirA.getName()), "Should be filtered");
        assertEquals(dirB, d2.getEntryCaseInsensitive(dirB.getName()));
        assertThrows(FileNotFoundException.class, () -> d2.getEntryCaseInsensitive(eRoot.getName()), "Should be filtered");

        i = d2.getEntries();
        assertEquals(dirB, i.next());
        assertThrows(NoSuchElementException.class, i::next, "Should throw NoSuchElementException when depleted");

        Spliterator<Entry> s2 = d2.spliterator();
        s2.tryAdvance(entry -> assertEquals(dirB, entry));
        assertFalse(s2.tryAdvance(entry -> fail("Should be depleted")), "Should return false when depleted");

        // Filter everything
        excl = Arrays.asList("NotThere", eRoot.getName(), dirA.getName(), dirB.getName());
        FilteringDirectoryNode d3 = new FilteringDirectoryNode(fs.getRoot(), excl);

        assertEquals(0, d3.getEntryCount());
        assertFalse(d3.hasEntryCaseInsensitive(dirA.getName()));
        assertFalse(d3.hasEntryCaseInsensitive(dirB.getName()));
        assertFalse(d3.hasEntryCaseInsensitive(eRoot.getName()));
        assertThrows(FileNotFoundException.class, () -> d3.getEntryCaseInsensitive(dirA.getName()), "Should be filtered");
        assertThrows(FileNotFoundException.class, () -> d3.getEntryCaseInsensitive(dirB.getName()), "Should be filtered");
        assertThrows(FileNotFoundException.class, () -> d3.getEntryCaseInsensitive(eRoot.getName()), "Should be filtered");

        i = d3.getEntries();
        assertThrows(NoSuchElementException.class, i::next, "Should throw NoSuchElementException when depleted");

        Spliterator<Entry> s3 = d3.spliterator();
        assertFalse(s3.tryAdvance(entry -> fail("Should be depleted")), "Should return false when depleted");
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
        assertTrue(d.hasEntryCaseInsensitive(dirA.getName()));
        assertTrue(d.hasEntryCaseInsensitive(dirB.getName()));
        assertFalse(d.hasEntryCaseInsensitive(eRoot.getName()));

        // Check filtering down
        assertTrue(d.getEntryCaseInsensitive(dirA.getName()) instanceof FilteringDirectoryNode);
        assertFalse(d.getEntryCaseInsensitive(dirB.getName()) instanceof FilteringDirectoryNode);

        DirectoryEntry fdA = (DirectoryEntry) d.getEntryCaseInsensitive(dirA.getName());
        assertFalse(fdA.hasEntryCaseInsensitive(eA.getName()));
        assertTrue(fdA.hasEntryCaseInsensitive(dirAA.getName()));

        DirectoryEntry fdAA = (DirectoryEntry) fdA.getEntryCaseInsensitive(dirAA.getName());
        assertTrue(fdAA.hasEntryCaseInsensitive(eAA.getName()));
    }

    @Test
    void testNullDirectory() {
        assertThrows(IllegalArgumentException.class, () -> new FilteringDirectoryNode(null, null));
    }
}
