
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

import java.io.*;

import junit.framework.*;

/**
 * Class to test SmallDocumentBlockList functionality
 *
 * @author Marc Johnson
 */

public class TestSmallDocumentBlockList
    extends TestCase
{

    /**
     * Constructor TestSmallDocumentBlockList
     *
     * @param name
     */

    public TestSmallDocumentBlockList(String name)
    {
        super(name);
    }

    /**
     * Test creating a SmallDocumentBlockList
     *
     * @exception IOException
     */

    public void testConstructor()
        throws IOException
    {
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

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out.println(
            "Testing org.apache.poi.poifs.storage.SmallDocumentBlockList");
        junit.textui.TestRunner.run(TestSmallDocumentBlockList.class);
    }
}
