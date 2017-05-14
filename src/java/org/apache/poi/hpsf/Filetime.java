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
import java.io.OutputStream;
import java.util.Date;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.LittleEndianConsts;

public class Filetime {
    /**
     * The difference between the Windows epoch (1601-01-01
     * 00:00:00) and the Unix epoch (1970-01-01 00:00:00) in
     * milliseconds.
     */
    private static final long EPOCH_DIFF = -11644473600000L;

    private static final int SIZE = LittleEndian.INT_SIZE * 2;
    private static final long UINT_MASK = 0x00000000FFFFFFFFL;
    private static final long NANO_100 = 1000L * 10L;
    
    private int _dwHighDateTime;
    private int _dwLowDateTime;

    Filetime() {}
    
    Filetime( int low, int high ) {
        _dwLowDateTime = low;
        _dwHighDateTime = high;
    }

    Filetime( Date date ) {
        long filetime = Filetime.dateToFileTime(date);
        _dwHighDateTime = (int) ((filetime >>> 32) & UINT_MASK);
        _dwLowDateTime = (int) (filetime & UINT_MASK);
    }
    

    void read( LittleEndianByteArrayInputStream lei ) {
        _dwLowDateTime = lei.readInt();
        _dwHighDateTime = lei.readInt();
    }

    long getHigh() {
        return _dwHighDateTime;
    }

    long getLow() {
        return _dwLowDateTime;
    }

    byte[] toByteArray() {
        byte[] result = new byte[SIZE];
        LittleEndian.putInt( result, 0 * LittleEndianConsts.INT_SIZE, _dwLowDateTime );
        LittleEndian.putInt( result, 1 * LittleEndianConsts.INT_SIZE, _dwHighDateTime );
        return result;
    }

    int write( OutputStream out ) throws IOException {
        LittleEndian.putInt( _dwLowDateTime, out );
        LittleEndian.putInt( _dwHighDateTime, out );
        return SIZE;
    }

    Date getJavaValue() {
        long l = (((long)_dwHighDateTime) << 32) | (_dwLowDateTime & UINT_MASK);
        return filetimeToDate( l );
    }
    
    /**
     * Converts a Windows FILETIME into a {@link Date}. The Windows
     * FILETIME structure holds a date and time associated with a
     * file. The structure identifies a 64-bit integer specifying the
     * number of 100-nanosecond intervals which have passed since
     * January 1, 1601.
     *
     * @param filetime The filetime to convert.
     * @return The Windows FILETIME as a {@link Date}.
     */
    public static Date filetimeToDate(final long filetime) {
        final long ms_since_16010101 = filetime / NANO_100;
        final long ms_since_19700101 = ms_since_16010101 + EPOCH_DIFF;
        return new Date(ms_since_19700101);
    }

    /**
     * Converts a {@link Date} into a filetime.
     *
     * @param date The date to be converted
     * @return The filetime
     *
     * @see #filetimeToDate(long)
     */
    public static long dateToFileTime(final Date date) {
        long ms_since_19700101 = date.getTime();
        long ms_since_16010101 = ms_since_19700101 - EPOCH_DIFF;
        return ms_since_16010101 * NANO_100;
    }
    
    /**
     * Return {@code true} if the date is undefined
     *
     * @param date the date
     * @return {@code true} if the date is undefined
     */
    public static boolean isUndefined(Date date) {
        return (date == null || dateToFileTime(date) == 0);
    }
}
