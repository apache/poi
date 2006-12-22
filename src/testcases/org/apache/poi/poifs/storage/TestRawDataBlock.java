
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
 * Class to test RawDataBlock functionality
 *
 * @author Marc Johnson
 */

public class TestRawDataBlock
    extends TestCase
{

    /**
     * Constructor TestRawDataBlock
     *
     * @param name
     */

    public TestRawDataBlock(String name)
    {
        super(name);
    }

    /**
     * Test creating a normal RawDataBlock
     *
     * @exception IOException
     */

    public void testNormalConstructor()
        throws IOException
    {
        byte[] data = new byte[ 512 ];

        for (int j = 0; j < 512; j++)
        {
            data[ j ] = ( byte ) j;
        }
        RawDataBlock block = new RawDataBlock(new ByteArrayInputStream(data));

        assertTrue("Should not be at EOF", !block.eof());
        byte[] out_data = block.getData();

        assertEquals("Should be same length", data.length, out_data.length);
        for (int j = 0; j < 512; j++)
        {
            assertEquals("Should be same value at offset " + j, data[ j ],
                         out_data[ j ]);
        }
    }

    /**
     * Test creating an empty RawDataBlock
     *
     * @exception IOException
     */

    public void testEmptyConstructor()
        throws IOException
    {
        byte[]       data  = new byte[ 0 ];
        RawDataBlock block = new RawDataBlock(new ByteArrayInputStream(data));

        assertTrue("Should be at EOF", block.eof());
        try
        {
            block.getData();
        }
        catch (IOException ignored)
        {

            // as expected
        }
    }

    /**
     * Test creating a short RawDataBlock
     */

    public void testShortConstructor()
    {
        for (int k = 1; k < 512; k++)
        {
            byte[] data = new byte[ k ];

            for (int j = 0; j < k; j++)
            {
                data[ j ] = ( byte ) j;
            }
            RawDataBlock block = null;

            try
            {
                block = new RawDataBlock(new ByteArrayInputStream(data));
                fail("Should have thrown IOException creating short block");
            }
            catch (IOException ignored)
            {

                // as expected
            }
        }
    }

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out
            .println("Testing org.apache.poi.poifs.storage.RawDataBlock");
        junit.textui.TestRunner.run(TestRawDataBlock.class);
    }
}
