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

import junit.framework.TestCase;

/**
 * Class to test SmallDocumentBlockList functionality
 *
 * @author Marc Johnson
 */
public final class TestSmallDocumentBlockList extends TestCase {

    public void testConstructor() throws IOException {
        byte[] data = new byte[ 2560 ];

        for (int j = 0; j < 2560; j++)
        {
            data[ j ] = ( byte ) j;
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        RawDataBlock[]       blocks = new RawDataBlock[ 5 ];

        for (int j = 0; j < 5; j++)
        {
            blocks[ j ] = new RawDataBlock(stream);
        }
        SmallDocumentBlockList sdbl =
            new SmallDocumentBlockList(SmallDocumentBlock.extract(blocks));

        // proof we added the blocks
        for (int j = 0; j < 40; j++)
        {
            sdbl.remove(j);
        }
        try
        {
            sdbl.remove(41);
            fail("there should have been an Earth-shattering ka-boom!");
        }
        catch (IOException ignored)
        {

            // it better have thrown one!!
        }
    }
}
