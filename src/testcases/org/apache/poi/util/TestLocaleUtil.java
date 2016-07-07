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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assume.assumeFalse;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TestLocaleUtil {
    // This unit test assumes that the user's locale isn't ja-JP and timezone isn't Asia/Tokyo
    // If this is the case, change the values to something else
    private static final Locale ja_JP = Locale.JAPAN;
    private static final TimeZone TOKYO = TimeZone.getTimeZone("Asia/Tokyo");
    private static final Calendar JAPAN_CALENDAR = Calendar.getInstance(TOKYO, ja_JP);
    
    /**
     * Reset the Locale to the user default before the test so that it isn't influenced
     * by the LocaleUtil's state being changed by previous tests.
     */
    @Before
    @SuppressForbidden("implementation around default locales in POI")
    public void setUp() {
        // reset the user locale and time zone so that tests do not interfere with each other
        // the other and better way would be to run each test in its own thread since
        // LocaleUtil uses per-thread settings.
        // Helpful, but not ASL 2.0 licensed:
        // http://www.codeaffine.com/2014/07/21/a-junit-rule-to-run-a-test-in-its-own-thread/
        LocaleUtil.setUserLocale(Locale.getDefault());
        LocaleUtil.setUserTimeZone(TimeZone.getDefault());
        
        assumeFalse(ja_JP.equals(LocaleUtil.getUserLocale()));
        assumeFalse(TOKYO.equals(LocaleUtil.getUserTimeZone()));
    }

    /**
     * Reset the Locale to the user default after the test so that it doesn't influence
     * other tests.
     */
    @After
    public void tearDown() {
        LocaleUtil.resetUserLocale();
        LocaleUtil.resetUserTimeZone();
    }
    
    @Test
    @SuppressForbidden("implementation around default locales in POI")
    public void userLocale() {
        Locale DEFAULT_LOCALE = LocaleUtil.getUserLocale();
        
        assertEquals(DEFAULT_LOCALE, LocaleUtil.getUserLocale());
        assertNotEquals(ja_JP, LocaleUtil.getUserLocale());
        
        LocaleUtil.setUserLocale(ja_JP);
        assertEquals(ja_JP, LocaleUtil.getUserLocale());
        
        LocaleUtil.resetUserLocale();
        assertEquals(DEFAULT_LOCALE, LocaleUtil.getUserLocale());
    }
    
    @Test
    @SuppressForbidden("implementation around default locales in POI")
    public void userTimeZone() {
        TimeZone DEFAULT_TIME_ZONE = LocaleUtil.getUserTimeZone();
        
        assertEquals(DEFAULT_TIME_ZONE, LocaleUtil.getUserTimeZone());
        assertNotEquals(TOKYO, LocaleUtil.getUserLocale());
        
        LocaleUtil.setUserTimeZone(TOKYO);
        assertEquals(TOKYO, LocaleUtil.getUserTimeZone());
    }
    
    @Test
    @SuppressForbidden("implementation around default locales in POI")
    public void localeCalendar() {
        Locale DEFAULT_LOCALE = LocaleUtil.getUserLocale();
        TimeZone DEFAULT_TIME_ZONE = LocaleUtil.getUserTimeZone();
        Calendar DEFAULT_CALENDAR = LocaleUtil.getLocaleCalendar();
        
        assertEquals(DEFAULT_LOCALE, LocaleUtil.getUserLocale());
        assertEquals(DEFAULT_TIME_ZONE, LocaleUtil.getUserTimeZone());
        assertCalendarEquals(DEFAULT_CALENDAR, LocaleUtil.getLocaleCalendar());
        assertNotEquals(ja_JP, LocaleUtil.getUserLocale());
        assertNotEquals(TOKYO, LocaleUtil.getUserTimeZone());
        assertCalendarNotEquals(JAPAN_CALENDAR, LocaleUtil.getLocaleCalendar());
        
        LocaleUtil.setUserLocale(ja_JP);
        LocaleUtil.setUserTimeZone(TOKYO);
        assertCalendarEquals(JAPAN_CALENDAR, LocaleUtil.getLocaleCalendar());
        assertCalendarEquals(JAPAN_CALENDAR, LocaleUtil.getLocaleCalendar(TOKYO));
        // FIXME: These might affect the time zone due to daylight savings:
        //assertCalendarEquals(JAPAN_CALENDAR, LocaleUtil.getLocaleCalendar(2016, 00, 01));
        //assertCalendarEquals(JAPAN_CALENDAR, LocaleUtil.getLocaleCalendar(2016, 00, 01, 00, 00, 00));
    }
    
    private static void assertCalendarNotEquals(Calendar expected, Calendar actual) {
        // FIXME: add more tests to compare calendars, ignoring whether the dates are equal
        assertNotEquals("time zone", expected.getTimeZone(), actual.getTimeZone());
    }
    private static void assertCalendarEquals(Calendar expected, Calendar actual) {
        // FIXME: add more tests to compare calendars, ignoring whether the set dates are equal
        assertEquals("time zone", expected.getTimeZone(), actual.getTimeZone());
    }
}
