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
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.poifs.common.POIFSConstants;

import junit.framework.TestCase;

/**
 * Class to test PropertyBlock functionality
 *
 * @author Marc Johnson
 */
public final class TestPropertyBlock extends TestCase {

    public void testCreatePropertyBlocks() {

        // test with 0 properties
        List            properties = new ArrayList();
        BlockWritable[] blocks     =
            PropertyBlock.createPropertyBlockArray(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS,properties);

        assertEquals(0, blocks.length);

        // test with 1 property
        properties.add(new LocalProperty("Root Entry"));
        blocks = PropertyBlock.createPropertyBlockArray(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS,properties);
        assertEquals(1, blocks.length);
        byte[] testblock = new byte[ 512 ];

        for (int j = 0; j < 4; j++)
        {
            setDefaultBlock(testblock, j);
        }
        testblock[ 0x0000 ] = ( byte ) 'R';
        testblock[ 0x0002 ] = ( byte ) 'o';
        testblock[ 0x0004 ] = ( byte ) 'o';
        testblock[ 0x0006 ] = ( byte ) 't';
        testblock[ 0x0008 ] = ( byte ) ' ';
        testblock[ 0x000A ] = ( byte ) 'E';
        testblock[ 0x000C ] = ( byte ) 'n';
        testblock[ 0x000E ] = ( byte ) 't';
        testblock[ 0x0010 ] = ( byte ) 'r';
        testblock[ 0x0012 ] = ( byte ) 'y';
        testblock[ 0x0040 ] = ( byte ) 22;
        verifyCorrect(blocks, testblock);

        // test with 3 properties
        properties.add(new LocalProperty("workbook"));
        properties.add(new LocalProperty("summary"));
        blocks = PropertyBlock.createPropertyBlockArray(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS,properties);
        assertEquals(1, blocks.length);
        testblock[ 0x0080 ] = ( byte ) 'w';
        testblock[ 0x0082 ] = ( byte ) 'o';
        testblock[ 0x0084 ] = ( byte ) 'r';
        testblock[ 0x0086 ] = ( byte ) 'k';
        testblock[ 0x0088 ] = ( byte ) 'b';
        testblock[ 0x008A ] = ( byte ) 'o';
        testblock[ 0x008C ] = ( byte ) 'o';
        testblock[ 0x008E ] = ( byte ) 'k';
        testblock[ 0x00C0 ] = ( byte ) 18;
        testblock[ 0x0100 ] = ( byte ) 's';
        testblock[ 0x0102 ] = ( byte ) 'u';
        testblock[ 0x0104 ] = ( byte ) 'm';
        testblock[ 0x0106 ] = ( byte ) 'm';
        testblock[ 0x0108 ] = ( byte ) 'a';
        testblock[ 0x010A ] = ( byte ) 'r';
        testblock[ 0x010C ] = ( byte ) 'y';
        testblock[ 0x0140 ] = ( byte ) 16;
        verifyCorrect(blocks, testblock);

        // test with 4 properties
        properties.add(new LocalProperty("wintery"));
        blocks = PropertyBlock.createPropertyBlockArray(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS,properties);
        assertEquals(1, blocks.length);
        testblock[ 0x0180 ] = ( byte ) 'w';
        testblock[ 0x0182 ] = ( byte ) 'i';
        testblock[ 0x0184 ] = ( byte ) 'n';
        testblock[ 0x0186 ] = ( byte ) 't';
        testblock[ 0x0188 ] = ( byte ) 'e';
        testblock[ 0x018A ] = ( byte ) 'r';
        testblock[ 0x018C ] = ( byte ) 'y';
        testblock[ 0x01C0 ] = ( byte ) 16;
        verifyCorrect(blocks, testblock);

        // test with 5 properties
        properties.add(new LocalProperty("foo"));
        blocks = PropertyBlock.createPropertyBlockArray(POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS,properties);
        assertEquals(2, blocks.length);
        testblock = new byte[ 1024 ];
        for (int j = 0; j < 8; j++)
        {
            setDefaultBlock(testblock, j);
        }
        testblock[ 0x0000 ] = ( byte ) 'R';
        testblock[ 0x0002 ] = ( byte ) 'o';
        testblock[ 0x0004 ] = ( byte ) 'o';
        testblock[ 0x0006 ] = ( byte ) 't';
        testblock[ 0x0008 ] = ( byte ) ' ';
        testblock[ 0x000A ] = ( byte ) 'E';
        testblock[ 0x000C ] = ( byte ) 'n';
        testblock[ 0x000E ] = ( byte ) 't';
        testblock[ 0x0010 ] = ( byte ) 'r';
        testblock[ 0x0012 ] = ( byte ) 'y';
        testblock[ 0x0040 ] = ( byte ) 22;
        testblock[ 0x0080 ] = ( byte ) 'w';
        testblock[ 0x0082 ] = ( byte ) 'o';
        testblock[ 0x0084 ] = ( byte ) 'r';
        testblock[ 0x0086 ] = ( byte ) 'k';
        testblock[ 0x0088 ] = ( byte ) 'b';
        testblock[ 0x008A ] = ( byte ) 'o';
        testblock[ 0x008C ] = ( byte ) 'o';
        testblock[ 0x008E ] = ( byte ) 'k';
        testblock[ 0x00C0 ] = ( byte ) 18;
        testblock[ 0x0100 ] = ( byte ) 's';
        testblock[ 0x0102 ] = ( byte ) 'u';
        testblock[ 0x0104 ] = ( byte ) 'm';
        testblock[ 0x0106 ] = ( byte ) 'm';
        testblock[ 0x0108 ] = ( byte ) 'a';
        testblock[ 0x010A ] = ( byte ) 'r';
        testblock[ 0x010C ] = ( byte ) 'y';
        testblock[ 0x0140 ] = ( byte ) 16;
        testblock[ 0x0180 ] = ( byte ) 'w';
        testblock[ 0x0182 ] = ( byte ) 'i';
        testblock[ 0x0184 ] = ( byte ) 'n';
        testblock[ 0x0186 ] = ( byte ) 't';
        testblock[ 0x0188 ] = ( byte ) 'e';
        testblock[ 0x018A ] = ( byte ) 'r';
        testblock[ 0x018C ] = ( byte ) 'y';
        testblock[ 0x01C0 ] = ( byte ) 16;
        testblock[ 0x0200 ] = ( byte ) 'f';
        testblock[ 0x0202 ] = ( byte ) 'o';
        testblock[ 0x0204 ] = ( byte ) 'o';
        testblock[ 0x0240 ] = ( byte ) 8;
        verifyCorrect(blocks, testblock);
    }

    private static void setDefaultBlock(byte [] testblock, int j)
    {
        int base  = j * 128;
        int index = 0;

        for (; index < 0x40; index++)
        {
            testblock[ base++ ] = ( byte ) 0;
        }
        testblock[ base++ ] = ( byte ) 2;
        testblock[ base++ ] = ( byte ) 0;
        index               += 2;
        for (; index < 0x44; index++)
        {
            testblock[ base++ ] = ( byte ) 0;
        }
        for (; index < 0x50; index++)
        {
            testblock[ base++ ] = ( byte ) 0xff;
        }
        for (; index < 0x80; index++)
        {
            testblock[ base++ ] = ( byte ) 0;
        }
    }

    private static void verifyCorrect(BlockWritable[] blocks, byte[] testblock) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(512
                                           * blocks.length);

        for (int j = 0; j < blocks.length; j++) {
            try {
				blocks[ j ].writeBlocks(stream);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
        }
        byte[] output = stream.toByteArray();

        assertEquals(testblock.length, output.length);
        for (int j = 0; j < testblock.length; j++)
        {
            assertEquals("mismatch at offset " + j, testblock[ j ],
                         output[ j ]);
        }
    }
}
