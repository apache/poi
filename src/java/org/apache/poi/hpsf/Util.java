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

package org.apache.poi.hpsf;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.poi.util.Internal;
import org.apache.poi.util.SuppressForbidden;

/**
 * <p>Provides various static utility methods.</p>
 */
@Internal
public class Util
{
    /**
     * <p>The difference between the Windows epoch (1601-01-01
     * 00:00:00) and the Unix epoch (1970-01-01 00:00:00) in
     * milliseconds: 11644473600000L. (Use your favorite spreadsheet
     * program to verify the correctness of this value. By the way,
     * did you notice that you can tell from the epochs which
     * operating system is the modern one? :-))</p>
     */
    public static final long EPOCH_DIFF = 11644473600000L;


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
        return filetimeToDate(filetime);
    }

    /**
     * <p>Converts a Windows FILETIME into a {@link Date}. The Windows
     * FILETIME structure holds a date and time associated with a
     * file. The structure identifies a 64-bit integer specifying the
     * number of 100-nanosecond intervals which have passed since
     * January 1, 1601.</p>
     *
     * @param filetime The filetime to convert.
     * @return The Windows FILETIME as a {@link Date}.
     */
    public static Date filetimeToDate(final long filetime)
    {
        final long ms_since_16010101 = filetime / (1000 * 10);
        final long ms_since_19700101 = ms_since_16010101 - EPOCH_DIFF;
        return new Date(ms_since_19700101);
    }



    /**
     * <p>Converts a {@link Date} into a filetime.</p>
     *
     * @param date The date to be converted
     * @return The filetime
     *
     * @see #filetimeToDate(long)
     * @see #filetimeToDate(int, int)
     */
    public static long dateToFileTime(final Date date)
    {
        long ms_since_19700101 = date.getTime();
        long ms_since_16010101 = ms_since_19700101 + EPOCH_DIFF;
        return ms_since_16010101 * (1000 * 10);
    }



    /**
     * <p>Compares to object arrays with regarding the objects' order. For
     * example, [1, 2, 3] and [2, 1, 3] are equal.</p>
     *
     * @param c1 The first object array.
     * @param c2 The second object array.
     * @return <code>true</code> if the object arrays are equal,
     * <code>false</code> if they are not.
     */
    public static boolean equals(Object[] c1, Object[] c2)
    {
        for (int i1 = 0; i1 < c1.length; i1++)
        {
            final Object obj1 = c1[i1];
            boolean matchFound = false;
            for (int i2 = 0; !matchFound && i2 < c1.length; i2++)
            {
                final Object obj2 = c2[i2];
                if (obj1.equals(obj2))
                {
                    matchFound = true;
                    c2[i2] = null;
                }
            }
            if (!matchFound)
                return false;
        }
        return true;
    }

    /**
     * <p>Pads a byte array with 0x00 bytes so that its length is a multiple of
     * 4.</p>
     *
     * @param ba The byte array to pad.
     * @return The padded byte array.
     */
    public static byte[] pad4(final byte[] ba)
    {
        final int PAD = 4;
        final byte[] result;
        int l = ba.length % PAD;
        if (l == 0)
            result = ba;
        else
        {
            l = PAD - l;
            result = new byte[ba.length + l];
            System.arraycopy(ba, 0, result, 0, ba.length);
        }
        return result;
    }


    /**
     * <p>Returns a textual representation of a {@link Throwable}, including a
     * stacktrace.</p>
     *
     * @param t The {@link Throwable}
     *
     * @return a string containing the output of a call to
     * <code>t.printStacktrace()</code>.
     */
    @SuppressForbidden("uses printStackTrace")
    public static String toString(final Throwable t)
    {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.close();
        try
        {
            sw.close();
            return sw.toString();
        }
        catch (IOException e)
        {
            final StringBuffer b = new StringBuffer(t.getMessage());
            b.append("\n");
            b.append("Could not create a stacktrace. Reason: ");
            b.append(e.getMessage());
            return b.toString();
        }
    }

}
