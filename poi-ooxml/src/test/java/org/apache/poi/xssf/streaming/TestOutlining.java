/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xssf.streaming;

import junit.framework.TestCase;

public final class TestOutlining extends TestCase {
	public void testSetRowGroupCollapsed() throws Exception {

		SXSSFWorkbook wb2 = new SXSSFWorkbook(100);
		wb2.setCompressTempFiles(true);
		SXSSFSheet sheet2 = (SXSSFSheet) wb2.createSheet("new sheet");

		int rowCount = 20;
		for (int i = 0; i < rowCount; i++) {
			sheet2.createRow(i);
		}

		sheet2.groupRow(4, 9);
		sheet2.groupRow(11, 19);

		sheet2.setRowGroupCollapsed(4, true);

		SXSSFRow r = (SXSSFRow) sheet2.getRow(8);
		assertTrue(r.getHidden());
		r = (SXSSFRow) sheet2.getRow(10);
		assertTrue(r.getCollapsed());
		r = (SXSSFRow) sheet2.getRow(12);
		assertNull(r.getHidden());
		wb2.dispose();
	}

	public void testSetRowGroupCollapsedError() throws Exception {

		SXSSFWorkbook wb2 = new SXSSFWorkbook(100);
		wb2.setCompressTempFiles(true);
		SXSSFSheet sheet2 = (SXSSFSheet) wb2.createSheet("new sheet");

		int rowCount = 20;
		for (int i = 0; i < rowCount; i++) {
			sheet2.createRow(i);
		}

		sheet2.groupRow(4, 9);
		sheet2.groupRow(11, 19);

		try {
			sheet2.setRowGroupCollapsed(3, true);
			fail("Should fail with an exception");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("row (3)"));
		}

		try {
			sheet2.setRowGroupCollapsed(10, true);
			fail("Should fail with an exception");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("row (10)"));
		}

		try {
			sheet2.setRowGroupCollapsed(0, true);
			fail("Should fail with an exception");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("row (0)"));
		}

		try {
			sheet2.setRowGroupCollapsed(20, true);
			fail("Should fail with an exception");
		} catch (IllegalArgumentException e) {
			assertTrue("Had: " + e.getMessage(), 
					e.getMessage().contains("Row does not exist"));
		}

		SXSSFRow r = (SXSSFRow) sheet2.getRow(8);
		assertNotNull(r);
		assertNull(r.getHidden());
		r = (SXSSFRow) sheet2.getRow(10);
		assertNull(r.getCollapsed());
		r = (SXSSFRow) sheet2.getRow(12);
		assertNull(r.getHidden());
		wb2.dispose();
	}
}
