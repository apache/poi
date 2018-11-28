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

import static org.apache.poi.poifs.common.POIFSConstants.LARGER_BIG_BLOCK_SIZE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.stream.IntStream;

import org.apache.poi.poifs.property.DocumentProperty;
import org.apache.poi.poifs.storage.RawDataUtil;
import org.apache.poi.util.IOUtils;
import org.junit.Test;

/**
 * Class to test POIFSDocument functionality
 */
public class TestDocument {

    /**
     * Integration test -- really about all we can do
     */
    @Test
    public void testNPOIFSDocument() throws IOException {

        try (POIFSFileSystem poifs = new POIFSFileSystem()) {

            // verify correct number of blocks get created for document
            // that is exact multiple of block size
            checkDocument(poifs, LARGER_BIG_BLOCK_SIZE);

            // verify correct number of blocks get created for document
            // that is not an exact multiple of block size
            checkDocument(poifs, LARGER_BIG_BLOCK_SIZE + 1);

            // verify correct number of blocks get created for document
            // that is small
            checkDocument(poifs, LARGER_BIG_BLOCK_SIZE - 1);

            // verify correct number of blocks get created for document
            // that is rather small
            checkDocument(poifs, 199);


            // verify that output is correct
            POIFSDocument document = checkDocument(poifs, LARGER_BIG_BLOCK_SIZE + 1);
            DocumentProperty property = document.getDocumentProperty();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            property.writeData(stream);
            byte[] output = stream.toByteArray();
            byte[] array2 = RawDataUtil.decompress("H4sIAAAAAAAAAEtlyGMoYShiqGSwYCAH8DEwMf5HAsToMQdiRgEIGwCDyzEQgAAAAA==");

            assertArrayEquals(array2, output);
        }
    }

    private static POIFSDocument checkDocument(final POIFSFileSystem poifs, final int size) throws IOException {
        final byte[] input = new byte[size];
        IntStream.range(0, size).forEach(i -> input[i] = (byte)i);

        POIFSDocument document = ((DocumentNode)poifs.createDocument(
            new SlowInputStream(new ByteArrayInputStream(input)),
        "entry"+poifs.getRoot().getEntryCount())).getDocument();

        final int blockSize = (size >= 4096) ? 512 : 64;
        final int blockCount = (size + (blockSize-1)) / blockSize;

        final byte[] bytCpy = checkValues(blockCount, document, input);
        final POIFSDocument copied = makeCopy(document,bytCpy);

        checkValues(blockCount, copied, input);

        return document;
    }

    private static POIFSDocument makeCopy(POIFSDocument document, byte[] input) throws IOException {
        POIFSFileSystem poifs = document.getFileSystem();
        String name = "test" + input.length;
        DirectoryNode root = poifs.getRoot();
        if (root.hasEntry(name)) {
            root.deleteEntry((EntryNode)root.getEntry(name));
        }
        return ((DocumentNode)root
            .createDocument(name, new ByteArrayInputStream(input)))
            .getDocument();
    }

    private static byte[] checkValues(final int blockCountExp, POIFSDocument document, byte[] input) throws IOException {
        assertNotNull(document);
        assertNotNull(document.getDocumentProperty().getDocument());
        assertEquals(document, document.getDocumentProperty().getDocument());

        ByteArrayInputStream bis = new ByteArrayInputStream(input);

        int blockCountAct = 0, bytesRemaining = input.length;
        for (ByteBuffer bb : document) {
            assertTrue(bytesRemaining > 0);
            int bytesAct = Math.min(bb.remaining(), bytesRemaining);
            assertTrue(bytesAct <= document.getDocumentBlockSize());
            byte[] bufAct = new byte[bytesAct];
            bb.get(bufAct);

            byte[] bufExp = new byte[bytesAct];
            int bytesExp = bis.read(bufExp, 0, bytesAct);
            assertEquals(bytesExp, bytesAct);

            assertArrayEquals(bufExp, bufAct);
            blockCountAct++;
            bytesRemaining -= bytesAct;
        }

        assertEquals(blockCountExp, blockCountAct);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try (DocumentInputStream dis = document.getFileSystem().createDocumentInputStream(
                document.getDocumentProperty().getName())) {
            IOUtils.copy(dis, stream);
        }

        byte[] output = stream.toByteArray();
        assertArrayEquals(input, stream.toByteArray());
        return output;
    }
}
