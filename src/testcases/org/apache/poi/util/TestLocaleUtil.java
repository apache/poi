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

import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;


public class TestLocaleUtil {
    private static final Locale ja_JP = Locale.JAPAN;
    private static final TimeZone TOKYO = TimeZone.getTimeZone("Asia/Tokyo");
    
    @Test
    @SuppressForbidden("implementation around default locales in POI")
    public void userLocale() {
        Locale DEFAULT_LOCALE = LocaleUtil.getUserLocale();
        assertEquals(DEFAULT_LOCALE, LocaleUtil.getUserLocale());
        assertNotEquals(ja_JP, LocaleUtil.getUserLocale());
        
        LocaleUtil.setUserLocale(ja_JP);
        assertEquals(ja_JP, LocaleUtil.getUserLocale());
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
}
