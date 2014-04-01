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

package org.apache.poi.hslf.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.poi.util.LittleEndian;

/**
 * A helper class for dealing with SystemTime Structs, as defined at
 * http://msdn.microsoft.com/library/en-us/sysinfo/base/systemtime_str.asp .
 *
 * Discrepancies between Calendar and SYSTEMTIME:
 *  - that January = 1 in SYSTEMTIME, 0 in Calendar.
 *  - that the day of the week (0) starts on Sunday in SYSTEMTIME, and Monday in Calendar
 * It is also the case that this does not store the timezone, and no... it is not
 * stored as UTC either, but rather the local system time (yuck.)
 *
 * @author Daniel Noll
 * @author Nick Burch
 */
public final class SystemTimeUtils {
	/**
	 * Get the date found in the byte array, as a java Data object
	 */
	public static Date getDate(byte[] data) {
		return getDate(data,0);
	}
	/**
	 * Get the date found in the byte array, as a java Data object
	 */
	public static Date getDate(byte[] data, int offset) {
        Calendar cal = new GregorianCalendar();

        cal.set(Calendar.YEAR,         LittleEndian.getShort(data,offset));
        cal.set(Calendar.MONTH,        LittleEndian.getShort(data,offset+2)-1);
        // Not actually needed - can be found from day of month
        //cal.set(Calendar.DAY_OF_WEEK,  LittleEndian.getShort(data,offset+4)+1);
        cal.set(Calendar.DAY_OF_MONTH, LittleEndian.getShort(data,offset+6));
        cal.set(Calendar.HOUR_OF_DAY,  LittleEndian.getShort(data,offset+8));
        cal.set(Calendar.MINUTE,       LittleEndian.getShort(data,offset+10));
        cal.set(Calendar.SECOND,       LittleEndian.getShort(data,offset+12));
        cal.set(Calendar.MILLISECOND,  LittleEndian.getShort(data,offset+14));

        return cal.getTime();
	}

	/**
	 * Convert the supplied java Date into a SystemTime struct, and write it
	 *  into the supplied byte array.
	 */
	public static void storeDate(Date date, byte[] dest) {
		storeDate(date, dest, 0);
	}
	/**
	 * Convert the supplied java Date into a SystemTime struct, and write it
	 *  into the supplied byte array.
	 */
	public static void storeDate(Date date, byte[] dest, int offset) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);

        LittleEndian.putShort(dest, offset + 0, (short) cal.get(Calendar.YEAR));
        LittleEndian.putShort(dest, offset + 2, (short)(cal.get(Calendar.MONTH) + 1));
        LittleEndian.putShort(dest, offset + 4, (short)(cal.get(Calendar.DAY_OF_WEEK)-1));
        LittleEndian.putShort(dest, offset + 6, (short) cal.get(Calendar.DAY_OF_MONTH));
        LittleEndian.putShort(dest, offset + 8, (short) cal.get(Calendar.HOUR_OF_DAY));
        LittleEndian.putShort(dest, offset + 10,(short) cal.get(Calendar.MINUTE));
        LittleEndian.putShort(dest, offset + 12,(short) cal.get(Calendar.SECOND));
        LittleEndian.putShort(dest, offset + 14,(short) cal.get(Calendar.MILLISECOND));
	}
}
