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

package org.apache.poi.hssf.record.chart;

import java.util.Arrays;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.apache.poi.util.HexRead;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Tests for {@link ChartFormatRecord} Test data taken directly from a real
 * Excel file.
 *
 * @author Josh Micich
 */
public final class TestChartFormatRecord extends TestCase {
	/**
	 * This rather uninteresting data came from attachment 23347 of bug 46693 at
	 * offsets 0x6BB2 and 0x7BAF
	 */
	private static final byte[] data = HexRead.readFromString(
			"14 10 14 00 " // BIFF header
			+ "00 00 00 00 00 00 00 00 "
			+ "00 00 00 00 00 00 00 00 "
			+ "00 00 00 00");

	/**
	 * The correct size of a {@link ChartFormatRecord} is 20 bytes (not including header).
	 */
	public void testLoad() {
		RecordInputStream in = TestcaseRecordInputStream.create(data);
		ChartFormatRecord record = new ChartFormatRecord(in);
		if (in.remaining() == 2) {
			throw new AssertionFailedError("Identified bug 44693d");
		}
		assertEquals(0, in.remaining());
		assertEquals(24, record.getRecordSize());

		byte[] data2 = record.serialize();
		assertTrue(Arrays.equals(data, data2));
	}
}
