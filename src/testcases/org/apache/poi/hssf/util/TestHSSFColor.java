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

import java.util.Hashtable;

import junit.framework.TestCase;
/**
 * @author Nick Burch
 */
public final class TestHSSFColor extends TestCase {
	public void testBasics() {
		assertNotNull(HSSFColor.YELLOW.class);
		assertTrue(HSSFColor.YELLOW.index > 0);
		assertTrue(HSSFColor.YELLOW.index2 > 0);
	}
	
	public void testContents() {
		assertEquals(3, HSSFColor.YELLOW.triplet.length);
		assertEquals(255, HSSFColor.YELLOW.triplet[0]);
		assertEquals(255, HSSFColor.YELLOW.triplet[1]);
		assertEquals(0, HSSFColor.YELLOW.triplet[2]);
		
		assertEquals("FFFF:FFFF:0", HSSFColor.YELLOW.hexString);
	}
	
	public void testTrippletHash() {
		Hashtable tripplets = HSSFColor.getTripletHash();
		
		assertEquals(
				HSSFColor.MAROON.class,
				tripplets.get(HSSFColor.MAROON.hexString).getClass()
		);
		assertEquals(
				HSSFColor.YELLOW.class,
				tripplets.get(HSSFColor.YELLOW.hexString).getClass()
		);
	}
}
