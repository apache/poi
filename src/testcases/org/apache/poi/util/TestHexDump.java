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

package org.apache.poi.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Marc Johnson (mjohnson at apache dot org)
 */
public final class TestHexDump extends TestCase {


    private static char toHex(int n) {
        return Character.toUpperCase(Character.forDigit(n & 0x0F, 16));
    }

    public void testDump() throws IOException {
        byte[] testArray = new byte[ 256 ];

        for (int j = 0; j < 256; j++)
        {
            testArray[ j ] = ( byte ) j;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        HexDump.dump(testArray, 0, stream, 0);
        byte[] outputArray = new byte[ 16 * (73 + HexDump.EOL.length()) ];

        for (int j = 0; j < 16; j++)
        {
            int offset = (73 + HexDump.EOL.length()) * j;

            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) toHex(j);
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) ' ';
            for (int k = 0; k < 16; k++)
            {
                outputArray[ offset++ ] = ( byte ) toHex(j);
                outputArray[ offset++ ] = ( byte ) toHex(k);
                outputArray[ offset++ ] = ( byte ) ' ';
            }
            for (int k = 0; k < 16; k++)
            {
                outputArray[ offset++ ] = ( byte ) toAscii((j * 16) + k);
            }
            System.arraycopy(HexDump.EOL.getBytes(), 0, outputArray, offset,
                             HexDump.EOL.getBytes().length);
        }
        byte[] actualOutput = stream.toByteArray();

        assertEquals("array size mismatch", outputArray.length,
                     actualOutput.length);
        for (int j = 0; j < outputArray.length; j++)
        {
            assertEquals("array[ " + j + "] mismatch", outputArray[ j ],
                         actualOutput[ j ]);
        }

        // verify proper behavior with non-zero offset
        stream = new ByteArrayOutputStream();
        HexDump.dump(testArray, 0x10000000, stream, 0);
        outputArray = new byte[ 16 * (73 + HexDump.EOL.length()) ];
        for (int j = 0; j < 16; j++)
        {
            int offset = (73 + HexDump.EOL.length()) * j;

            outputArray[ offset++ ] = ( byte ) '1';
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) toHex(j);
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) ' ';
            for (int k = 0; k < 16; k++)
            {
                outputArray[ offset++ ] = ( byte ) toHex(j);
                outputArray[ offset++ ] = ( byte ) toHex(k);
                outputArray[ offset++ ] = ( byte ) ' ';
            }
            for (int k = 0; k < 16; k++)
            {
                outputArray[ offset++ ] = ( byte ) toAscii((j * 16) + k);
            }
            System.arraycopy(HexDump.EOL.getBytes(), 0, outputArray, offset,
                             HexDump.EOL.getBytes().length);
        }
        actualOutput = stream.toByteArray();
        assertEquals("array size mismatch", outputArray.length,
                     actualOutput.length);
        for (int j = 0; j < outputArray.length; j++)
        {
            assertEquals("array[ " + j + "] mismatch", outputArray[ j ],
                         actualOutput[ j ]);
        }

        // verify proper behavior with negative offset
        stream = new ByteArrayOutputStream();
        HexDump.dump(testArray, 0xFF000000, stream, 0);
        outputArray = new byte[ 16 * (73 + HexDump.EOL.length()) ];
        for (int j = 0; j < 16; j++)
        {
            int offset = (73 + HexDump.EOL.length()) * j;

            outputArray[ offset++ ] = ( byte ) 'F';
            outputArray[ offset++ ] = ( byte ) 'F';
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) toHex(j);
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) ' ';
            for (int k = 0; k < 16; k++)
            {
                outputArray[ offset++ ] = ( byte ) toHex(j);
                outputArray[ offset++ ] = ( byte ) toHex(k);
                outputArray[ offset++ ] = ( byte ) ' ';
            }
            for (int k = 0; k < 16; k++)
            {
                outputArray[ offset++ ] = ( byte ) toAscii((j * 16) + k);
            }
            System.arraycopy(HexDump.EOL.getBytes(), 0, outputArray, offset,
                             HexDump.EOL.getBytes().length);
        }
        actualOutput = stream.toByteArray();
        assertEquals("array size mismatch", outputArray.length,
                     actualOutput.length);
        for (int j = 0; j < outputArray.length; j++)
        {
            assertEquals("array[ " + j + "] mismatch", outputArray[ j ],
                         actualOutput[ j ]);
        }

        // verify proper behavior with non-zero index
        stream = new ByteArrayOutputStream();
        HexDump.dump(testArray, 0x10000000, stream, 0x81);
        outputArray = new byte[ (8 * (73 + HexDump.EOL.length())) - 1 ];
        for (int j = 0; j < 8; j++)
        {
            int offset = (73 + HexDump.EOL.length()) * j;

            outputArray[ offset++ ] = ( byte ) '1';
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) '0';
            outputArray[ offset++ ] = ( byte ) toHex(j + 8);
            outputArray[ offset++ ] = ( byte ) '1';
            outputArray[ offset++ ] = ( byte ) ' ';
            for (int k = 0; k < 16; k++)
            {
                int index = 0x81 + (j * 16) + k;

                if (index < 0x100)
                {
                    outputArray[ offset++ ] = ( byte ) toHex(index / 16);
                    outputArray[ offset++ ] = ( byte ) toHex(index);
                }
                else
                {
                    outputArray[ offset++ ] = ( byte ) ' ';
                    outputArray[ offset++ ] = ( byte ) ' ';
                }
                outputArray[ offset++ ] = ( byte ) ' ';
            }
            for (int k = 0; k < 16; k++)
            {
                int index = 0x81 + (j * 16) + k;

                if (index < 0x100)
                {
                    outputArray[ offset++ ] = ( byte ) toAscii(index);
                }
            }
            System.arraycopy(HexDump.EOL.getBytes(), 0, outputArray, offset,
                             HexDump.EOL.getBytes().length);
        }
        actualOutput = stream.toByteArray();
        assertEquals("array size mismatch", outputArray.length,
                     actualOutput.length);
        for (int j = 0; j < outputArray.length; j++)
        {
            assertEquals("array[ " + j + "] mismatch", outputArray[ j ],
                         actualOutput[ j ]);
        }

        // verify proper behavior with negative index
        try
        {
            HexDump.dump(testArray, 0x10000000, new ByteArrayOutputStream(), -1);
            fail("should have caught ArrayIndexOutOfBoundsException on negative index");
        }
        catch (ArrayIndexOutOfBoundsException ignored_exception)
        {

            // as expected
        }

        // verify proper behavior with index that is too large
        try
        {
            HexDump.dump(testArray, 0x10000000, new ByteArrayOutputStream(),
                         testArray.length);
            fail("should have caught ArrayIndexOutOfBoundsException on large index");
        }
        catch (ArrayIndexOutOfBoundsException ignored_exception)
        {

            // as expected
        }

        // verify proper behavior with null stream
        try
        {
            HexDump.dump(testArray, 0x10000000, null, 0);
            fail("should have caught IllegalArgumentException on negative index");
        }
        catch (IllegalArgumentException ignored_exception)
        {

            // as expected
        }

        // verify proper behaviour with empty byte array
        ByteArrayOutputStream os = new ByteArrayOutputStream( );
        HexDump.dump( new byte[0], 0, os, 0 );
        assertEquals( "No Data" + System.getProperty( "line.separator"), os.toString() );

    }

    public void testToHex() {
        assertEquals("000A", HexDump.toHex((short)0xA));
        assertEquals("0A", HexDump.toHex((byte)0xA));
        assertEquals("0000000A", HexDump.toHex(0xA));

        assertEquals("FFFF", HexDump.toHex((short)0xFFFF));
        
        confirmStr("0xFE", HexDump.byteToHex(-2));
        confirmStr("0x25", HexDump.byteToHex(37));
        confirmStr("0xFFFE", HexDump.shortToHex(-2));
        confirmStr("0x0005", HexDump.shortToHex(5));
        confirmStr("0xFFFFFF9C", HexDump.intToHex(-100));
        confirmStr("0x00001001", HexDump.intToHex(4097));
        confirmStr("0xFFFFFFFFFFFF0006", HexDump.longToHex(-65530));
        confirmStr("0x0000000000003FCD", HexDump.longToHex(16333));
    }

    private static void confirmStr(String expected, char[] actualChars) {
        assertEquals(expected, new String(actualChars));
    }

    private static char toAscii(int c) {
        char rval = '.';

        if (c >= 32 && c <= 126) {
            rval = ( char ) c;
        }
        return rval;
    }
}
