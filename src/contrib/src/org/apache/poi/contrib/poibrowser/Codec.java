/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * Portions of this software are based upon public domain software
 * originally written at the National Center for Supercomputing Applications,
 * University of Illinois, Urbana-Champaign.
 */

package org.apache.poi.contrib.poibrowser;

import java.io.*;
import java.util.*;



/**
 * <p>Provides utility methods for encoding and decoding hexadecimal
 * data.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de) - with portions from Tomcat
 * @version $Id$
 * @since 2002-01-24
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
     *
     * <p><strong>FIXME:</strong> If this method is called frequently,
     * it should directly implement the algorithm in the called method
     * in order to avoid creating a string instance.</p>
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
     * <p>Decodes the hexadecimal representation of a sequence of
     * bytes into a byte array. Each character in the string
     * represents a nibble (half byte) and must be one of the
     * characters '0'-'9', 'A'-'F' or 'a'-'f'.</p>
     *
     * @param s The string to be decoded
     *
     * @return The bytes
     *
     * @throw IllegalArgumentException if the string does not contain
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
