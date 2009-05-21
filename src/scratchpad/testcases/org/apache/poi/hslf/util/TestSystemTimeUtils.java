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


import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

/**
 * Tests that SystemTimeUtils works properly.
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestSystemTimeUtils extends TestCase {
	// From real files
	private byte[] data_a = new byte[] {
		0xD6-256, 07, 01, 00,
		02, 00, 0x18, 00, 0x0A, 00, 0x1A, 00,
		0x0F, 00, 0xCD-256, 00
	};
	private byte[] data_b = new byte[] {
		00, 00, 0xE1-256, 0x2E, 0x1C, 00, 00, 00,
		01, 00, 00, 00, 0xD6-256, 0x07, 01, 00,
		02, 00, 0x18, 00, 0x15, 00, 0x19, 00, 03,
		00, 0xD5-256, 02, 0x0A, 00, 00, 00,
		0x0A, 00, 00, 00
	};

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

	public void testGetDateA() throws Exception {
		Date date = SystemTimeUtils.getDate(data_a);

		// Is 2006-01-24 (2nd day of week) 10:26:15.205
		Date exp = sdf.parse("2006-01-24 10:26:15.205");
		assertEquals(exp.getTime(), date.getTime());
		assertEquals(exp, date);
	}

	public void testGetDateB() throws Exception {
		Date date = SystemTimeUtils.getDate(data_b, 8+4);

		// Is 2006-01-24 (2nd day of week) 21:25:03.725
		Date exp = sdf.parse("2006-01-24 21:25:03.725");
		assertEquals(exp.getTime(), date.getTime());
		assertEquals(exp, date);
	}

	public void testWriteDateA() throws Exception {
		byte[] out_a = new byte[data_a.length];
		Date date = sdf.parse("2006-01-24 10:26:15.205");
		SystemTimeUtils.storeDate(date, out_a);

		for(int i=0; i<out_a.length; i++) {
			assertEquals(data_a[i], out_a[i]);
		}
	}

	public void testWriteDateB() throws Exception {
		byte[] out_b = new byte[data_b.length];
		// Copy over start and end, ignoring the 16 byte date field in the middle
		System.arraycopy(data_b, 0, out_b, 0, 12);
		System.arraycopy(data_b, 12+16, out_b, 12+16, data_b.length-12-16);

		Date date = sdf.parse("2006-01-24 21:25:03.725");
		SystemTimeUtils.storeDate(date, out_b, 12);

		for(int i=0; i<out_b.length; i++) {
			assertEquals(data_b[i], out_b[i]);
		}
	}
}
