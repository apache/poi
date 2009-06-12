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
import java.lang.reflect.Field;

import junit.framework.TestCase;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.DummyPOILogger;

/**
 * Class to test RawDataBlockList functionality
 *
 * @author Marc Johnson
 */
public final class TestRawDataBlockList extends TestCase {
	static {
        // We always want to use our own
        //  logger
        System.setProperty(
        		"org.apache.poi.util.POILogger",
        		"org.apache.poi.util.DummyPOILogger"
        );
	}

    /**
     * Test creating a normal RawDataBlockList
     */
    public void testNormalConstructor() throws IOException {
        byte[] data = new byte[ 2560 ];

        for (int j = 0; j < 2560; j++)
        {
            data[ j ] = ( byte ) j;
        }
        new RawDataBlockList(new ByteArrayInputStream(data), POIFSConstants.BIG_BLOCK_SIZE);
    }

    /**
     * Test creating an empty RawDataBlockList
     */
    public void testEmptyConstructor() throws IOException {
        new RawDataBlockList(new ByteArrayInputStream(new byte[ 0 ]), POIFSConstants.BIG_BLOCK_SIZE);
    }

    /**
     * Test creating a short RawDataBlockList
     */
    public void testShortConstructor() throws Exception {
        // Get the logger to be used
        DummyPOILogger logger = new DummyPOILogger();
        Field fld = RawDataBlock.class.getDeclaredField("log");
        fld.setAccessible(true);
        fld.set(null, logger);
        assertEquals(0, logger.logged.size());

        // Test for various short sizes
        for (int k = 2049; k < 2560; k++)
        {
            byte[] data = new byte[ k ];

            for (int j = 0; j < k; j++)
            {
                data[ j ] = ( byte ) j;
            }

            // Check we logged the error
            logger.reset();
            new RawDataBlockList(new ByteArrayInputStream(data), POIFSConstants.BIG_BLOCK_SIZE);
            assertEquals(1, logger.logged.size());
        }
    }
}
