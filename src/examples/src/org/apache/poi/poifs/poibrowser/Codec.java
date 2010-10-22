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

package org.apache.poi.poifs.poibrowser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.poi.hpsf.ClassID;



/**
 * <p>Provides utility methods for encoding and decoding hexadecimal
 * data.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de) - with portions from Tomcat
 */
public class Codec
{

    /**
     * <p>The nibbles' hexadecimal values. A nibble is a half byte.</p>
     */
    protected static final byte hexval[] =
        {(byte) '0', (byte) '1', (byte) '2', (byte) '3',
         (byte) '4', (byte) '5', (byte) '6', (byte) '7',
         (byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
         (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F'};



    /**
     * <p>Converts a string into its hexadecimal notation.</p>
     */
    public static String hexEncode(final String s)
    {
        return hexEncode(s.getBytes());
    }



    /**
     * <p>Converts a byte array into its hexadecimal notation.</p>
     */
    public static String hexEncode(final byte[] s)
    {
        return hexEncode(s, 0, s.length);
    }



    /**
     * <p>Converts a part of a byte array into its hexadecimal
     * notation.</p>
     */
    public static String hexEncode(final byte[] s, final int offset,
                                   final int length)
    {
        StringBuffer b = new StringBuffer(length * 2);
        for (int i = offset; i < offset + length; i++)
        {
            int c = s[i];
            b.append((char) hexval[(c & 0xF0) >> 4]);
            b.append((char) hexval[(c & 0x0F) >> 0]);
        }
        return b.toString();
    }



    /**
     * <p>Converts a single byte into its hexadecimal notation.</p>
     */
    public static String hexEncode(final byte b)
    {
        StringBuffer sb = new StringBuffer(2);
        sb.append((char) hexval[(b & 0xF0) >> 4]);
        sb.append((char) hexval[(b & 0x0F) >> 0]);
        return sb.toString();
    }



    /**
     * <p>Converts a short value (16-bit) into its hexadecimal
     * notation.</p>
     */
    public static String hexEncode(final short s)
    {
        StringBuffer sb = new StringBuffer(4);
        sb.append((char) hexval[(s & 0xF000) >> 12]);
        sb.append((char) hexval[(s & 0x0F00) >>  8]);
        sb.append((char) hexval[(s & 0x00F0) >>  4]);
        sb.append((char) hexval[(s & 0x000F) >>  0]);
        return sb.toString();
    }



    /**
     * <p>Converts an int value (32-bit) into its hexadecimal
     * notation.</p>
     */
    public static String hexEncode(final int i)
    {
        StringBuffer sb = new StringBuffer(8);
        sb.append((char) hexval[(i & 0xF0000000) >> 28]);
        sb.append((char) hexval[(i & 0x0F000000) >> 24]);
        sb.append((char) hexval[(i & 0x00F00000) >> 20]);
        sb.append((char) hexval[(i & 0x000F0000) >> 16]);
        sb.append((char) hexval[(i & 0x0000F000) >> 12]);
        sb.append((char) hexval[(i & 0x00000F00) >>  8]);
        sb.append((char) hexval[(i & 0x000000F0) >>  4]);
        sb.append((char) hexval[(i & 0x0000000F) >>  0]);
        return sb.toString();
    }



    /**
     * <p>Converts a long value (64-bit) into its hexadecimal
     * notation.</p>
     */
    public static String hexEncode(final long l)
    {
        StringBuffer sb = new StringBuffer(16);
        sb.append(hexEncode((int) (l & 0xFFFFFFFF00000000L) >> 32));
        sb.append(hexEncode((int) (l & 0x00000000FFFFFFFFL) >>  0));
        return sb.toString();
    }



    /**
     * <p>Converts a class ID into its hexadecimal notation.</p>
     */
    public static String hexEncode(final ClassID classID)
    {
        return hexEncode(classID.getBytes());
    }



    /**
     * <p>Decodes the hexadecimal representation of a sequence of
     * bytes into a byte array. Each character in the string
     * represents a nibble (half byte) and must be one of the
     * characters '0'-'9', 'A'-'F' or 'a'-'f'.</p>
     *
     * @param s The string to be decoded
     *
     * @return The bytes
     *
     * @throws IllegalArgumentException if the string does not contain
     * a valid representation of a byte sequence.
     */
    public static byte[] hexDecode(final String s)
    {
        final int length = s.length();

        /* The string to be converted must have an even number of
           characters. */
        if (length % 2 == 1)
            throw new IllegalArgumentException
                ("String has odd length " + length);
        byte[] b = new byte[length / 2];
        char[] c = new char[length];
        s.toUpperCase().getChars(0, length, c, 0);
        for (int i = 0; i < length; i += 2)
            b[i/2] = (byte) (decodeNibble(c[i]) << 4 & 0xF0 |
                             decodeNibble(c[i+1])    & 0x0F);
        return b;
    }



    /**
     * <p>Decodes a nibble.</p>
     *
     * @param c A character in the range '0'-'9' or 'A'-'F'. Lower
     * case is not supported here.
     *
     * @return The decoded nibble in the range 0-15
     *
     * @throws IllegalArgumentException if <em>c</em> is not a
     * permitted character
     */
    protected static byte decodeNibble(final char c)
    {
        for (byte i = 0; i < hexval.length; i++)
            if ((byte) c == hexval[i])
                return i;
        throw new IllegalArgumentException("\"" + c + "\"" +
                                           " does not represent a nibble.");
    }



    /**
     * <p>For testing.</p>
     */
    public static void main(final String args[])
        throws IOException
    {
        final BufferedReader in =
            new BufferedReader(new InputStreamReader(System.in));
        String s;
        do
        {
            s = in.readLine();
            if (s != null)
            {
                String bytes = hexEncode(s);
                System.out.print("Hex encoded (String): ");
                System.out.println(bytes);
                System.out.print("Hex encoded (byte[]): ");
                System.out.println(hexEncode(s.getBytes()));
                System.out.print("Re-decoded (byte[]):  ");
                System.out.println(new String(hexDecode(bytes)));
            }
        }
        while (s != null);
    }

}
