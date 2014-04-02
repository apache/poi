
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

import java.io.*;

import java.util.*;

import junit.framework.*;

import org.apache.poi.poifs.property.DirectoryProperty;
import org.apache.poi.poifs.property.DocumentProperty;

/**
 * Class to test DirectoryNode functionality
 *
 * @author Marc Johnson
 */
public final class TestDirectoryNode extends TestCase {

    /**
     * test trivial constructor (a DirectoryNode with no children)
     */
    public void testEmptyConstructor() {
        POIFSFileSystem   fs        = new POIFSFileSystem();
        DirectoryProperty property1 = new DirectoryProperty("parent");
        DirectoryProperty property2 = new DirectoryProperty("child");
        DirectoryNode     parent    = new DirectoryNode(property1, fs, null);
        DirectoryNode     node      = new DirectoryNode(property2, fs,
                                          parent);

        assertEquals(0, parent.getPath().length());
        assertEquals(1, node.getPath().length());
        assertEquals("child", node.getPath().getComponent(0));

        // verify that getEntries behaves correctly
        int      count = 0;
        Iterator iter  = node.getEntries();

        while (iter.hasNext())
        {
            count++;
            iter.next();
        }
        assertEquals(0, count);

        // verify behavior of isEmpty
        assertTrue(node.isEmpty());

        // verify behavior of getEntryCount
        assertEquals(0, node.getEntryCount());

        // verify behavior of getEntry
        try
        {
            node.getEntry("foo");
            fail("should have caught FileNotFoundException");
        }
        catch (FileNotFoundException ignored)
        {

            // as expected
        }

        // verify behavior of isDirectoryEntry
        assertTrue(node.isDirectoryEntry());

        // verify behavior of getName
        assertEquals(property2.getName(), node.getName());

        // verify behavior of isDocumentEntry
        assertTrue(!node.isDocumentEntry());

        // verify behavior of getParent
        assertEquals(parent, node.getParent());
    }

    /**
     * test non-trivial constructor (a DirectoryNode with children)
     */
    public void testNonEmptyConstructor() throws IOException {
        DirectoryProperty property1 = new DirectoryProperty("parent");
        DirectoryProperty property2 = new DirectoryProperty("child1");

        property1.addChild(property2);
        property1.addChild(new DocumentProperty("child2", 2000));
        property2.addChild(new DocumentProperty("child3", 30000));
        DirectoryNode node  = new DirectoryNode(property1,
                                                new POIFSFileSystem(), null);

        // verify that getEntries behaves correctly
        int           count = 0;
        Iterator      iter  = node.getEntries();

        while (iter.hasNext())
        {
            count++;
            iter.next();
        }
        assertEquals(2, count);

        // verify behavior of isEmpty
        assertTrue(!node.isEmpty());

        // verify behavior of getEntryCount
        assertEquals(2, node.getEntryCount());

        // verify behavior of getEntry
        DirectoryNode child1 = ( DirectoryNode ) node.getEntry("child1");

        child1.getEntry("child3");
        node.getEntry("child2");
        try
        {
            node.getEntry("child3");
            fail("should have caught FileNotFoundException");
        }
        catch (FileNotFoundException ignored)
        {

            // as expected
        }

        // verify behavior of isDirectoryEntry
        assertTrue(node.isDirectoryEntry());

        // verify behavior of getName
        assertEquals(property1.getName(), node.getName());

        // verify behavior of isDocumentEntry
        assertTrue(!node.isDocumentEntry());

        // verify behavior of getParent
        assertNull(node.getParent());
    }

    /**
     * test deletion methods
     */
    public void testDeletion() throws IOException {
        POIFSFileSystem fs   = new POIFSFileSystem();
        DirectoryEntry  root = fs.getRoot();

        // verify cannot delete the root directory
        assertFalse(root.delete());
        assertTrue(root.isEmpty());
        
        DirectoryEntry dir = fs.createDirectory("myDir");

        assertFalse(root.isEmpty());
        assertTrue(dir.isEmpty());

        // verify can delete empty directory
        assertFalse(root.delete());
        assertTrue(dir.delete());
        
        // Now look at a non-empty one
        dir = fs.createDirectory("NextDir");
        DocumentEntry doc =
            dir.createDocument("foo",
                               new ByteArrayInputStream(new byte[ 1 ]));

        assertFalse(root.isEmpty());
        assertFalse(dir.isEmpty());

        // verify cannot delete non-empty directory
        assertFalse(dir.delete());
        
        // but we can delete it if we remove the document
        assertTrue(doc.delete());
        assertTrue(dir.isEmpty());
        assertTrue(dir.delete());
        
        // It's really gone!
        assertTrue(root.isEmpty());
    }

    /**
     * test change name methods
     */
    public void testRename() throws IOException {
        POIFSFileSystem fs   = new POIFSFileSystem();
        DirectoryEntry  root = fs.getRoot();

        // verify cannot rename the root directory
        assertTrue(!root.renameTo("foo"));
        DirectoryEntry dir = fs.createDirectory("myDir");

        assertTrue(dir.renameTo("foo"));
        assertEquals("foo", dir.getName());
        DirectoryEntry dir2 = fs.createDirectory("myDir");

        assertTrue(!dir2.renameTo("foo"));
        assertEquals("myDir", dir2.getName());
        assertTrue(dir.renameTo("FirstDir"));
        assertTrue(dir2.renameTo("foo"));
        assertEquals("foo", dir2.getName());
    }
}
