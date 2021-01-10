
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Unit test for ArrayUtil
 */
class TestArrayUtil {
	/**
	 * Helper for testArrayMoveWithin
	 */
	private Integer[] getIntsList() {
		return new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
	}

	/**
	 * Test to ensure that arrayMoveWithin works as expected
	 */
	@Test
	void testArrayMoveWithin() {
		// moveFrom, moveTo, numToMove, values...
		Integer[][] data = {
			// Moving to a later point in the array
			// Shift 1 back
			{ 4, 8, 1,      0, 1, 2, 3, 5, 6, 7, 8, 4, 9 },
			// Shift front 1 back
			{ 0, 7, 1,      1, 2, 3, 4, 5, 6, 7, 0, 8, 9 },
			// Shift 1 to end
			{ 4, 9, 1,      0, 1, 2, 3, 5, 6, 7, 8, 9, 4 },

			// Moving to an earlier point in the array
			// Shift 1 forward
			{ 8, 3, 1,      0, 1, 2, 8, 3, 4, 5, 6, 7, 9 },
			// Shift end 1 forward
			{ 9, 2, 1,      0, 1, 9, 2, 3, 4, 5, 6, 7, 8 },
			// Shift 1 to front
			{ 5, 0, 1,      5, 0, 1, 2, 3, 4, 6, 7, 8, 9 },

			// Moving many to a later point in the array
			// Shift 3 back
			{ 2, 6, 3,      0, 1, 5, 6, 7, 8, 2, 3, 4, 9 },
			// Shift 3 to back
			{ 2, 7, 3,      0, 1, 5, 6, 7, 8, 9, 2, 3, 4 },
			// Shift from 3 front
			{ 0, 5, 3,      3, 4, 5, 6, 7, 0, 1, 2, 8, 9 },

			// Moving many to an earlier point in the array
			// Shift 3 forward
			{ 6, 2, 3,      0, 1, 6, 7, 8, 2, 3, 4, 5, 9 },
			// Shift 3 to front
			{ 6, 0, 3,      6, 7, 8, 0, 1, 2, 3, 4, 5, 9 },
			// Shift from 3 back
			{ 7, 3, 3,      0, 1, 2, 7, 8, 9, 3, 4, 5, 6 }
		};

		for (Integer[] entry : data) {
			Integer[] ints = getIntsList();
			ArrayUtil.arrayMoveWithin(ints, entry[0], entry[1], entry[2]);
			assertArrayEquals(ints, Arrays.copyOfRange(entry, 3, 13));
		}

		// Check can't shift more than we have
		assertThrows(IllegalArgumentException.class, () -> ArrayUtil.arrayMoveWithin(getIntsList(), 7, 3, 5));
		// Good, we don't have 5 from 7 onwards

		// Check can't shift where would overshoot
		assertThrows(IllegalArgumentException.class, () -> ArrayUtil.arrayMoveWithin(getIntsList(), 2, 7, 5));
		// Good, we can't fit 5 in starting at 7
	}
}
