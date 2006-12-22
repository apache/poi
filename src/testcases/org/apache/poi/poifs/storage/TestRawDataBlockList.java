
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
 * Class to test RawDataBlockList functionality
 *
 * @author Marc Johnson
 */

public class TestRawDataBlockList
    extends TestCase
{

    /**
     * Constructor TestRawDataBlockList
     *
     * @param name
     */

    public TestRawDataBlockList(String name)
    {
        super(name);
    }

    /**
     * Test creating a normal RawDataBlockList
     *
     * @exception IOException
     */

    public void testNormalConstructor()
        throws IOException
    {
        byte[] data = new byte[ 2560 ];

        for (int j = 0; j < 2560; j++)
        {
            data[ j ] = ( byte ) j;
        }
        new RawDataBlockList(new ByteArrayInputStream(data));
    }

    /**
     * Test creating an empty RawDataBlockList
     *
     * @exception IOException
     */

    public void testEmptyConstructor()
        throws IOException
    {
        new RawDataBlockList(new ByteArrayInputStream(new byte[ 0 ]));
    }

    /**
     * Test creating a short RawDataBlockList
     */

    public void testShortConstructor()
    {
        for (int k = 2049; k < 2560; k++)
        {
            byte[] data = new byte[ k ];

            for (int j = 0; j < k; j++)
            {
                data[ j ] = ( byte ) j;
            }
            try
            {
                new RawDataBlockList(new ByteArrayInputStream(data));
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
            .println("Testing org.apache.poi.poifs.storage.RawDataBlockList");
        junit.textui.TestRunner.run(TestRawDataBlockList.class);
    }
}
