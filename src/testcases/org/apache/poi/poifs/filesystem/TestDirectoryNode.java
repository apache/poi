
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

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

public class TestDirectoryNode
    extends TestCase
{

    /**
     * Constructor TestDirectoryNode
     *
     * @param name
     */

    public TestDirectoryNode(String name)
    {
        super(name);
    }

    /**
     * test trivial constructor (a DirectoryNode with no children)
     *
     * @exception IOException
     */

    public void testEmptyConstructor()
        throws IOException
    {
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
     *
     * @exception IOException
     */

    public void testNonEmptyConstructor()
        throws IOException
    {
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
     *
     * @exception IOException
     */

    public void testDeletion()
        throws IOException
    {
        POIFSFileSystem fs   = new POIFSFileSystem();
        DirectoryEntry  root = fs.getRoot();

        // verify cannot delete the root directory
        assertTrue(!root.delete());
        DirectoryEntry dir = fs.createDirectory("myDir");

        assertTrue(!root.isEmpty());

        // verify can delete empty directory
        assertTrue(dir.delete());
        dir = fs.createDirectory("NextDir");
        DocumentEntry doc =
            dir.createDocument("foo",
                               new ByteArrayInputStream(new byte[ 1 ]));

        assertTrue(!dir.isEmpty());

        // verify cannot delete empty directory
        assertTrue(!dir.delete());
        assertTrue(doc.delete());

        // verify now we can delete it
        assertTrue(dir.delete());
        assertTrue(root.isEmpty());
    }

    /**
     * test change name methods
     *
     * @exception IOException
     */

    public void testRename()
        throws IOException
    {
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

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out
            .println("Testing org.apache.poi.poifs.filesystem.DirectoryNode");
        junit.textui.TestRunner.run(TestDirectoryNode.class);
    }
}
