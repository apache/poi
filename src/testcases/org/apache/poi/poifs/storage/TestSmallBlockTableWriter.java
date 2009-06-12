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

package org.apache.poi.poifs.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.poifs.filesystem.POIFSDocument;
import org.apache.poi.poifs.property.PropertyTable;
import org.apache.poi.poifs.property.RootProperty;

/**
 * Class to test SmallBlockTableWriter functionality
 *
 * @author Marc Johnson
 */
public final class TestSmallBlockTableWriter extends TestCase {

    public void testWritingConstructor() throws IOException {
        List<POIFSDocument> documents = new ArrayList<POIFSDocument>();

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
}
