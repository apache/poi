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

package org.apache.poi.ooxml.signature.service.signer.ooxml;

import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;

public final class TestOOXMLSignatureAspect extends TestCase {

	private static final TimeZone TIME_ZONE_UTC = TimeZone.getTimeZone("UTC");

	public void testFormatTimestampAsISO8601() {
		assertEquals("2010-06-05T04:03:02Z", OOXMLSignatureAspect.formatTimestampAsISO8601(makeTimestamp(2010, 6, 5, 4, 3, 2)));
	}

	private static long makeTimestamp(int year, int month, int day, int hour, int minute, int second) {
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TIME_ZONE_UTC);
		c.set(year, month-1, day, hour, minute, second);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}
}
