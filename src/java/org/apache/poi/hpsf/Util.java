/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

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
     *
     *  <li><p>if they have the same length and</p></li>
     *
     *  <li><p>if for each <var>i</var> with
     *  <var>i</var>&nbsp;&gt;=&nbsp;0 and
     *  <var>i</var>&nbsp;&lt;&nbsp;<var>a.length</var> holds
     *  <var>a</var>[<var>i</var>]&nbsp;==&nbsp;<var>b</var>[<var>i</var>].</p></li>
     *
     * </ul>
     *
     * @param a The first byte array
     * @param b The first byte array
     * @return <code>true</code> if the byte arrays are equal, else
     * <code>false</code>
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
     *
     * @param src The source byte array.
     * @param srcOffset Offset in the source byte array.
     * @param length The number of bytes to copy.
     * @param dst The destination byte array.
     * @param dstOffset Offset in the destination byte array.
     */
    public static void copy(final byte[] src, final int srcOffset,
			    final int length, final byte[] dst,
			    final int dstOffset)
    {
        for (int i = 0; i < length; i++)
            dst[dstOffset + i] = src[srcOffset + i];
    }



    /**
     * <p>Concatenates the contents of several byte arrays into a
     * single one.</p>
     *
     * @param byteArrays The byte arrays to be concatened.
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
     * @param offset Start copying here.
     * @param length Copy this many bytes.
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
     * words stored in the structure.</p>
     *
     * @param high The higher double word of the FILETIME structure.
     * @param low The lower double word of the FILETIME structure.
     * @return The Windows FILETIME as a {@link Date}.
     */
    public static Date filetimeToDate(final int high, final int low)
    {
        final long filetime = ((long) high) << 32 | (low & 0xffffffffL);
        final long ms_since_16010101 = filetime / (1000 * 10);
        final long ms_since_19700101 = ms_since_16010101 - EPOCH_DIFF;
        return new Date(ms_since_19700101);
    }

}
