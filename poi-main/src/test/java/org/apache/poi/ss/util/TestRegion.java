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

package org.apache.poi.ss.util;

import junit.framework.TestCase;


/**
 * Tests that the common CellReference works as we need it to
 */
@SuppressWarnings("deprecation") // the Region class is deprecated in the public API, but still needs to be tested
public final class TestRegion extends TestCase {

	public void testGetRegionRef() {
		int rowFrom = 3;
		short colFrom = 3;
		int rowTo = 9;
		short colTo = 9;
		Region region = new Region(rowFrom, colFrom, rowTo, colTo);
		assertEquals("D4:J10", region.getRegionRef());
	}

	public void testContains() {
		int rowFrom = 3;
		short colFrom = 3;
		int rowTo = 9;
		short colTo = 9;
		Region region = new Region(rowFrom, colFrom, rowTo, colTo);
		assertEquals("D4:J10", region.getRegionRef());
		assertTrue(region.contains(5, (short) 7));
		assertTrue(region.contains(9, (short) 9));
		assertFalse(region.contains(9, (short) 10));
	}

	public void testConstructors() {
		Region region_1 = new Region("A1:E7");
		assertEquals(0, region_1.getColumnFrom());
		assertEquals((short)4, region_1.getColumnTo());
	}
}
