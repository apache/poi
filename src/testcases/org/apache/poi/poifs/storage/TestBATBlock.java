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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Class to test BATBlock functionality
 *
 * @author Marc Johnson
 */
public final class TestBATBlock extends TestCase {

    /**
     * Test the createBATBlocks method. The test involves setting up
     * various arrays of int's and ensuring that the correct number of
     * BATBlocks is created for each array, and that the data from
     * each array is correctly written to the BATBlocks.
     */
    public void testCreateBATBlocks() throws IOException {

        // test 0 length array (basic sanity)
        BATBlock[] rvalue = BATBlock.createBATBlocks(createTestArray(0));

        assertEquals(0, rvalue.length);

        // test array of length 1
        rvalue = BATBlock.createBATBlocks(createTestArray(1));
        assertEquals(1, rvalue.length);
        verifyContents(rvalue, 1);

        // test array of length 127
        rvalue = BATBlock.createBATBlocks(createTestArray(127));
        assertEquals(1, rvalue.length);
        verifyContents(rvalue, 127);

        // test array of length 128
        rvalue = BATBlock.createBATBlocks(createTestArray(128));
        assertEquals(1, rvalue.length);
        verifyContents(rvalue, 128);

        // test array of length 129
        rvalue = BATBlock.createBATBlocks(createTestArray(129));
        assertEquals(2, rvalue.length);
        verifyContents(rvalue, 129);
    }

    private static int[] createTestArray(int count) {
        int[] rvalue = new int[ count ];

        for (int j = 0; j < count; j++)
        {
            rvalue[ j ] = j;
        }
        return rvalue;
    }

    private static void verifyContents(BATBlock[] blocks, int entries) throws IOException {
        byte[] expected = new byte[ 512 * blocks.length ];

        Arrays.fill(expected, ( byte ) 0xFF);
        int offset = 0;

        for (int j = 0; j < entries; j++)
        {
            expected[ offset++ ] = ( byte ) j;
            expected[ offset++ ] = 0;
            expected[ offset++ ] = 0;
            expected[ offset++ ] = 0;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream(512
                                           * blocks.length);

        for (int j = 0; j < blocks.length; j++)
        {
            blocks[ j ].writeBlocks(stream);
        }
        byte[] actual = stream.toByteArray();

        assertEquals(expected.length, actual.length);
        for (int j = 0; j < expected.length; j++)
        {
            assertEquals(expected[ j ], actual[ j ]);
        }
    }

    public void testCreateXBATBlocks() throws IOException {
        // test 0 length array (basic sanity)
        BATBlock[] rvalue = BATBlock.createXBATBlocks(createTestArray(0), 1);

        assertEquals(0, rvalue.length);

        // test array of length 1
        rvalue = BATBlock.createXBATBlocks(createTestArray(1), 1);
        assertEquals(1, rvalue.length);
        verifyXBATContents(rvalue, 1, 1);

        // test array of length 127
        rvalue = BATBlock.createXBATBlocks(createTestArray(127), 1);
        assertEquals(1, rvalue.length);
        verifyXBATContents(rvalue, 127, 1);

        // test array of length 128
        rvalue = BATBlock.createXBATBlocks(createTestArray(128), 1);
        assertEquals(2, rvalue.length);
        verifyXBATContents(rvalue, 128, 1);

        // test array of length 254
        rvalue = BATBlock.createXBATBlocks(createTestArray(254), 1);
        assertEquals(2, rvalue.length);
        verifyXBATContents(rvalue, 254, 1);

        // test array of length 255
        rvalue = BATBlock.createXBATBlocks(createTestArray(255), 1);
        assertEquals(3, rvalue.length);
        verifyXBATContents(rvalue, 255, 1);
    }

    private static void verifyXBATContents(BATBlock[] blocks, int entries, int start_block)
			throws IOException {
        byte[] expected = new byte[ 512 * blocks.length ];

        Arrays.fill(expected, ( byte ) 0xFF);
        int offset = 0;

        for (int j = 0; j < entries; j++)
        {
            if ((j % 127) == 0)
            {
                if (j != 0)
                {
                    offset += 4;
                }
            }
            expected[ offset++ ] = ( byte ) j;
            expected[ offset++ ] = 0;
            expected[ offset++ ] = 0;
            expected[ offset++ ] = 0;
        }
        for (int j = 0; j < (blocks.length - 1); j++)
        {
            offset               = 508 + (j * 512);
            expected[ offset++ ] = ( byte ) (start_block + j + 1);
            expected[ offset++ ] = 0;
            expected[ offset++ ] = 0;
            expected[ offset++ ] = 0;
        }
        offset               = (blocks.length * 512) - 4;
        expected[ offset++ ] = ( byte ) -2;
        expected[ offset++ ] = ( byte ) -1;
        expected[ offset++ ] = ( byte ) -1;
        expected[ offset++ ] = ( byte ) -1;
        ByteArrayOutputStream stream = new ByteArrayOutputStream(512
                                           * blocks.length);

        for (int j = 0; j < blocks.length; j++)
        {
            blocks[ j ].writeBlocks(stream);
        }
        byte[] actual = stream.toByteArray();

        assertEquals(expected.length, actual.length);
        for (int j = 0; j < expected.length; j++)
        {
            assertEquals("offset " + j, expected[ j ], actual[ j ]);
        }
    }

    public void testCalculateXBATStorageRequirements() {
        int[] blockCounts = { 0, 1, 127, 128 };
        int[] requirements = { 0, 1, 1, 2 };

        for (int j = 0; j < blockCounts.length; j++)
        {
            assertEquals(
                "requirement for " + blockCounts[ j ], requirements[ j ],
                BATBlock.calculateXBATStorageRequirements(blockCounts[ j ]));
        }
    }

    public void testEntriesPerBlock() {
        assertEquals(128, BATBlock.entriesPerBlock());
    }
    public void testEntriesPerXBATBlock() {
        assertEquals(127, BATBlock.entriesPerXBATBlock());
    }
    public void testGetXBATChainOffset() {
        assertEquals(508, BATBlock.getXBATChainOffset());
    }
}
