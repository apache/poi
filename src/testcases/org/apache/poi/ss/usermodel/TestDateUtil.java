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

package org.apache.poi.ss.usermodel;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

public class TestDateUtil {

    @Test
    public void getJavaDate_InvalidValue() {
        double dateValue = -1;
        TimeZone tz = TimeZone.getDefault();
        boolean use1904windowing = false;
        boolean roundSeconds = false;

        assertEquals(null, DateUtil.getJavaDate(dateValue));
        assertEquals(null, DateUtil.getJavaDate(dateValue, tz));
        assertEquals(null, DateUtil.getJavaDate(dateValue, use1904windowing));
        assertEquals(null, DateUtil.getJavaDate(dateValue, use1904windowing, tz));
        assertEquals(null, DateUtil.getJavaDate(dateValue, use1904windowing, tz, roundSeconds));
    }

    @Test
    public void getJavaDate_ValidValue() {
        double dateValue = 0;
        TimeZone tz = TimeZone.getDefault();
        boolean use1904windowing = false;
        boolean roundSeconds = false;

        Calendar calendar = Calendar.getInstance(tz);
        calendar.set(1900, 0, 0, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date date = calendar.getTime();

        assertEquals(date, DateUtil.getJavaDate(dateValue));
        assertEquals(date, DateUtil.getJavaDate(dateValue, tz));
        assertEquals(date, DateUtil.getJavaDate(dateValue, use1904windowing));
        assertEquals(date, DateUtil.getJavaDate(dateValue, use1904windowing, tz));
        assertEquals(date, DateUtil.getJavaDate(dateValue, use1904windowing, tz, roundSeconds));
    }

    @Test
    public void getJavaCalendar_InvalidValue() {
        double dateValue = -1;
        TimeZone tz = TimeZone.getDefault();
        boolean use1904windowing = false;
        boolean roundSeconds = false;

        assertEquals(null, DateUtil.getJavaCalendar(dateValue));
        assertEquals(null, DateUtil.getJavaCalendar(dateValue, use1904windowing));
        assertEquals(null, DateUtil.getJavaCalendar(dateValue, use1904windowing, tz));
        assertEquals(null, DateUtil.getJavaCalendar(dateValue, use1904windowing, tz, roundSeconds));
    }

    @Test
    public void getJavaCalendar_ValidValue() {
        double dateValue = 0;
        TimeZone tz = TimeZone.getDefault();
        boolean use1904windowing = false;
        boolean roundSeconds = false;

        Calendar calendar = Calendar.getInstance(tz);
        calendar.set(1900, 0, 0, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        assertEquals(calendar, DateUtil.getJavaCalendar(dateValue));
        assertEquals(calendar, DateUtil.getJavaCalendar(dateValue, use1904windowing));
        assertEquals(calendar, DateUtil.getJavaCalendar(dateValue, use1904windowing, tz));
        assertEquals(calendar, DateUtil.getJavaCalendar(dateValue, use1904windowing, tz, roundSeconds));
    }
}
