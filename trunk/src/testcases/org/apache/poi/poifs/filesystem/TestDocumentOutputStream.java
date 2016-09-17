
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Class to test DocumentOutputStream functionality
 *
 * @author Marc Johnson
 */
public final class TestDocumentOutputStream extends TestCase {

    /**
     * test write(int) behavior
     */
    public void testWrite1() throws IOException {
        ByteArrayOutputStream stream  = new ByteArrayOutputStream();
        DocumentOutputStream  dstream = new DocumentOutputStream(stream, 25);

        for (int j = 0; j < 25; j++)
        {
            dstream.write(j);
        }
        try
        {
            dstream.write(0);
            fail("Should have caught IOException");
        }
        catch (IOException ignored)
        {
        }
        byte[] output = stream.toByteArray();

        assertEquals(25, output.length);
        for (int j = 0; j < 25; j++)
        {
            assertEquals(( byte ) j, output[ j ]);
        }
        stream.close();
    }

    /**
     * test write(byte[]) behavior
     */
    public void testWrite2() throws IOException {
        ByteArrayOutputStream stream  = new ByteArrayOutputStream();
        DocumentOutputStream  dstream = new DocumentOutputStream(stream, 25);

        for (int j = 0; j < 6; j++)
        {
            byte[] array = new byte[ 4 ];

            Arrays.fill(array, ( byte ) j);
            dstream.write(array);
        }
        try
        {
            byte[] array = new byte[ 4 ];

            Arrays.fill(array, ( byte ) 7);
            dstream.write(array);
            fail("Should have caught IOException");
        }
        catch (IOException ignored)
        {
        }
        byte[] output = stream.toByteArray();

        assertEquals(24, output.length);
        for (int j = 0; j < 6; j++)
        {
            for (int k = 0; k < 4; k++)
            {
                assertEquals(String.valueOf((j * 4) + k), ( byte ) j,
                             output[ (j * 4) + k ]);
            }
        }
        stream.close();
    }

    /**
     * test write(byte[], int, int) behavior
     */
    public void testWrite3() throws IOException {
        ByteArrayOutputStream stream  = new ByteArrayOutputStream();
        DocumentOutputStream  dstream = new DocumentOutputStream(stream, 25);
        byte[]                array   = new byte[ 50 ];

        for (int j = 0; j < 50; j++)
        {
            array[ j ] = ( byte ) j;
        }
        dstream.write(array, 1, 25);
        try
        {
            dstream.write(array, 0, 1);
            fail("Should have caught IOException");
        }
        catch (IOException ignored)
        {
        }
        byte[] output = stream.toByteArray();

        assertEquals(25, output.length);
        for (int j = 0; j < 25; j++)
        {
            assertEquals(( byte ) (j + 1), output[ j ]);
        }
        stream.close();
    }

    /**
     * test writeFiller()
     */
    public void testWriteFiller() throws IOException {
        ByteArrayOutputStream stream  = new ByteArrayOutputStream();
        DocumentOutputStream  dstream = new DocumentOutputStream(stream, 25);

        for (int j = 0; j < 25; j++)
        {
            dstream.write(j);
        }
        try
        {
            dstream.write(0);
            fail("Should have caught IOException");
        }
        catch (IOException ignored)
        {
        }
        dstream.writeFiller(100, ( byte ) 0xff);
        byte[] output = stream.toByteArray();

        assertEquals(100, output.length);
        for (int j = 0; j < 25; j++)
        {
            assertEquals(( byte ) j, output[ j ]);
        }
        for (int j = 25; j < 100; j++)
        {
            assertEquals(String.valueOf(j), ( byte ) 0xff, output[ j ]);
        }
        stream.close();
    }
}
