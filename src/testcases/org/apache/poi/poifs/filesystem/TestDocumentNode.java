
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

import junit.framework.*;

import org.apache.poi.poifs.property.DirectoryProperty;
import org.apache.poi.poifs.property.DocumentProperty;
import org.apache.poi.poifs.storage.RawDataBlock;

/**
 * Class to test DocumentNode functionality
 *
 * @author Marc Johnson
 */

public class TestDocumentNode
    extends TestCase
{

    /**
     * Constructor TestDocumentNode
     *
     * @param name
     */

    public TestDocumentNode(String name)
    {
        super(name);
    }

    /**
     * test constructor
     *
     * @exception IOException
     */

    public void testConstructor()
        throws IOException
    {
        DirectoryProperty    property1 = new DirectoryProperty("directory");
        RawDataBlock[]       rawBlocks = new RawDataBlock[ 4 ];
        ByteArrayInputStream stream    =
            new ByteArrayInputStream(new byte[ 2048 ]);

        for (int j = 0; j < 4; j++)
        {
            rawBlocks[ j ] = new RawDataBlock(stream);
        }
        POIFSDocument    document  = new POIFSDocument("document", rawBlocks,
                                         2000);
        DocumentProperty property2 = document.getDocumentProperty();
        DirectoryNode    parent    = new DirectoryNode(property1, null, null);
        DocumentNode     node      = new DocumentNode(property2, parent);

        // verify we can retrieve the document
        assertEquals(property2.getDocument(), node.getDocument());

        // verify we can get the size
        assertEquals(property2.getSize(), node.getSize());

        // verify isDocumentEntry returns true
        assertTrue(node.isDocumentEntry());

        // verify isDirectoryEntry returns false
        assertTrue(!node.isDirectoryEntry());

        // verify getName behaves correctly
        assertEquals(property2.getName(), node.getName());

        // verify getParent behaves correctly
        assertEquals(parent, node.getParent());
    }

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out
            .println("Testing org.apache.poi.poifs.filesystem.DocumentNode");
        junit.textui.TestRunner.run(TestDocumentNode.class);
    }
}
