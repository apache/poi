
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

package org.apache.poi.poifs.storage;

import java.io.*;

import java.util.*;

import junit.framework.*;

import org.apache.poi.poifs.filesystem.POIFSDocument;
import org.apache.poi.poifs.property.PropertyTable;
import org.apache.poi.poifs.property.RootProperty;

/**
 * Class to test SmallBlockTableWriter functionality
 *
 * @author Marc Johnson
 */

public class TestSmallBlockTableWriter
    extends TestCase
{

    /**
     * Constructor TestSmallBlockTableWriter
     *
     * @param name
     */

    public TestSmallBlockTableWriter(String name)
    {
        super(name);
    }

    /**
     * test writing constructor
     *
     * @exception IOException
     */

    public void testWritingConstructor()
        throws IOException
    {
        List documents = new ArrayList();

        documents.add(
            new POIFSDocument(
                "doc340", new ByteArrayInputStream(new byte[ 340 ])));
        documents.add(
            new POIFSDocument(
                "doc5000", new ByteArrayInputStream(new byte[ 5000 ])));
        documents
            .add(new POIFSDocument("doc0",
                                   new ByteArrayInputStream(new byte[ 0 ])));
        documents
            .add(new POIFSDocument("doc1",
                                   new ByteArrayInputStream(new byte[ 1 ])));
        documents
            .add(new POIFSDocument("doc2",
                                   new ByteArrayInputStream(new byte[ 2 ])));
        documents
            .add(new POIFSDocument("doc3",
                                   new ByteArrayInputStream(new byte[ 3 ])));
        documents
            .add(new POIFSDocument("doc4",
                                   new ByteArrayInputStream(new byte[ 4 ])));
        documents
            .add(new POIFSDocument("doc5",
                                   new ByteArrayInputStream(new byte[ 5 ])));
        documents
            .add(new POIFSDocument("doc6",
                                   new ByteArrayInputStream(new byte[ 6 ])));
        documents
            .add(new POIFSDocument("doc7",
                                   new ByteArrayInputStream(new byte[ 7 ])));
        documents
            .add(new POIFSDocument("doc8",
                                   new ByteArrayInputStream(new byte[ 8 ])));
        documents
            .add(new POIFSDocument("doc9",
                                   new ByteArrayInputStream(new byte[ 9 ])));
        RootProperty               root = new PropertyTable().getRoot();
        SmallBlockTableWriter      sbtw = new SmallBlockTableWriter(documents,
                                              root);
        BlockAllocationTableWriter bat  = sbtw.getSBAT();

        // 15 small blocks: 6 for doc340, 0 for doc5000 (too big), 0
        // for doc0 (no storage needed), 1 each for doc1 through doc9
        assertEquals(15 * 64, root.getSize());

        // 15 small blocks rounds up to 2 big blocks
        assertEquals(2, sbtw.countBlocks());
        int start_block = 1000 + root.getStartBlock();

        sbtw.setStartBlock(start_block);
        assertEquals(start_block, root.getStartBlock());
    }

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out.println(
            "Testing org.apache.poi.poifs.storage.SmallBlockTableWriter");
        junit.textui.TestRunner.run(TestSmallBlockTableWriter.class);
    }
}
