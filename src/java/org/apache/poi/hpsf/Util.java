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
 *
 * Portions of this software are based upon public domain software
 * originally written at the National Center for Supercomputing Applications,
 * University of Illinois, Urbana-Champaign.
 */

package org.apache.poi.hpsf;

import java.util.*;

/**
 * <p>Provides various static utility methods.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @version $Id$
 * @since 2002-02-09
 */
public class Util
{

    /**
     * <p>Checks whether two byte arrays <var>a</var> and <var>b</var>
     * are equal. They are equal</p>
     *
     * <ul>
     * <li><p>if they have the same length and</p></li>
     *
     * <li><p>if for each <var>i</var> with
     * <var>i</var>&nbsp;&gt;=&nbsp;0 and
     * <var>i</var>&nbsp;&lt;&nbsp;<var>a.length</var> holds
     * <var>a</var>[<var>i</var>]&nbsp;==&nbsp;<var>b</var>[<var>i</var>].</p></li>
     * </ul>
     */
    public static boolean equal(final byte[] a, final byte[] b)
    {
        if (a.length != b.length)
            return false;
        for (int i = 0; i < a.length; i++)
            if (a[i] != b[i])
                return false;
        return true;
    }



    /**
     * <p>Copies a part of a byte array into another byte array.</p>
     */
    public static void copy(final byte[] src, final int srcOffset,
                            final int length,
                            final byte[] dst, final int dstOffset)
    {
        for (int i = 0; i < length; i++)
            dst[dstOffset + i] = src[srcOffset + i];
    }



    /**
     * <p>Concatenates the contents of several byte arrays into a
     * single one.</p>
     *
     * @param byteArrays The byte arrays to be concatened.
     *
     * @return A new byte array containing the concatenated byte
     * arrays.
     */
    public static byte[] cat(final byte[][] byteArrays)
    {
        int capacity = 0;
        for (int i = 0; i < byteArrays.length; i++)
            capacity += byteArrays[i].length;
        final byte[] result = new byte[capacity];
        int r = 0;
        for (int i = 0; i < byteArrays.length; i++)
            for (int j = 0; j < byteArrays[i].length; j++)
                result[r++] = byteArrays[i][j];
        return result;
    }



    /**
     * <p>Copies bytes from a source byte array into a new byte
     * array.</p>
     *
     * @param src Copy from this byte array.
     *
     * @param offset Start copying here.
     *
     * @param length Copy this many bytes.
     *
     * @return The new byte array. Its length is number of copied bytes.
     */
    public static byte[] copy(final byte[] src, final int offset,
                              final int length)
    {
        final byte[] result = new byte[length];
        copy(src, offset, length, result, 0);
        return result;
    }



    /**
     * <p>The difference between the Windows epoch (1601-01-01
     * 00:00:00) and the Unix epoch (1970-01-01 00:00:00) in
     * milliseconds: 11644473600000L. (Use your favorite spreadsheet
     * program to verify the correctness of this value. By the way,
     * did you notice that you can tell from the epochs which
     * operating system is the modern one? :-))</p>
     */
    public final static long EPOCH_DIFF = 11644473600000L;

    /**
     * <p>Converts a Windows FILETIME into a {@link Date}. The Windows
     * FILETIME structure holds a date and time associated with a
     * file. The structure identifies a 64-bit integer specifying the
     * number of 100-nanosecond intervals which have passed since
     * January 1, 1601. This 64-bit value is split into the two double
     * word stored in the structure.</p>
     *
     * @param high The higher double word of the FILETIME structure.
     *
     * @param low The lower double word of the FILETIME structure.
     */
    public static Date filetimeToDate(final int high, final int low)
    {
        final long filetime = ((long) high) << 32 | ((long) low);
        final long ms_since_16010101 = filetime / (1000 * 10);
        final long ms_since_19700101 = ms_since_16010101 - EPOCH_DIFF;
        return new Date(ms_since_19700101);
    }

}

