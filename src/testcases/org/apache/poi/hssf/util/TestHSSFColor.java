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

package org.apache.poi.hssf.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.junit.jupiter.api.Test;

final class TestHSSFColor {
    @Test
    void testBasics() {
		assertTrue(HSSFColorPredefined.YELLOW.getIndex() > 0);
		assertTrue(HSSFColorPredefined.YELLOW.getIndex2() > 0);
	}

    @Test
	void testContents() {
	    short[] triplet = HSSFColorPredefined.YELLOW.getTriplet();
		assertEquals(3, triplet.length);
		assertEquals(255, triplet[0]);
		assertEquals(255, triplet[1]);
		assertEquals(0, triplet[2]);

		assertEquals("FFFF:FFFF:0", HSSFColorPredefined.YELLOW.getHexString());
	}

    @Test
	void testTripletHash() {
		Map<String, HSSFColor> triplets = HSSFColor.getTripletHash();

		assertEquals(
				HSSFColorPredefined.MAROON.getColor(),
				triplets.get(HSSFColorPredefined.MAROON.getHexString())
		);
		assertEquals(
				HSSFColorPredefined.YELLOW.getColor(),
				triplets.get(HSSFColorPredefined.YELLOW.getHexString())
		);
	}
}
