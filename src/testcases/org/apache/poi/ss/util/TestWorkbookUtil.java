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
 * Tests WorkbookUtil.
 *
 * @see org.apache.poi.ss.util.WorkbookUtil
 */
public final class TestWorkbookUtil extends TestCase {
	/**
	 * borrowed test cases from 
	 * {@link org.apache.poi.hssf.record.TestBoundSheetRecord#testValidNames()}
	 */
	public void testCreateSafeNames() {
		
		String p = "Sheet1";
		String actual = WorkbookUtil.createSafeSheetName(p);
		assertEquals(p, actual);
		
		p = "O'Brien's sales";
		actual = WorkbookUtil.createSafeSheetName(p);
		assertEquals(p, actual);
		
		p = " data # ";
		actual = WorkbookUtil.createSafeSheetName(p);
		assertEquals(p, actual);
		
		p = "data $1.00";
		actual = WorkbookUtil.createSafeSheetName(p);
		assertEquals(p, actual);
		
		// now the replaced versions ...
		actual = WorkbookUtil.createSafeSheetName("data?");
		assertEquals("data ", actual);
		
		actual = WorkbookUtil.createSafeSheetName("abc/def");
		assertEquals("abc def", actual);
		
		actual = WorkbookUtil.createSafeSheetName("data[0]");
		assertEquals("data 0 ", actual);
		
		actual = WorkbookUtil.createSafeSheetName("data*");
		assertEquals("data ", actual);
		
		actual = WorkbookUtil.createSafeSheetName("abc\\def");
		assertEquals("abc def", actual);
		
		actual = WorkbookUtil.createSafeSheetName("'data");
		assertEquals(" data", actual);
		
		actual = WorkbookUtil.createSafeSheetName("data'");
		assertEquals("data ", actual);
		
		actual = WorkbookUtil.createSafeSheetName("d'at'a");
		assertEquals("d'at'a", actual);
		
		actual = WorkbookUtil.createSafeSheetName(null);
		assertEquals("null", actual);
		
		actual = WorkbookUtil.createSafeSheetName("");
		assertEquals("empty", actual);
		
		actual = WorkbookUtil.createSafeSheetName("1234567890123456789012345678901TOOLONG");
		assertEquals("1234567890123456789012345678901", actual);
	}
}
