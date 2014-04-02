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

package org.apache.poi.ss;

import junit.framework.TestCase;

/**
 * Check that all enum values are properly set
 *
 * @author Yegor Kozlov
 */
public final class TestSpreadsheetVersion extends TestCase {

    public void testExcel97(){
        SpreadsheetVersion v = SpreadsheetVersion.EXCEL97;
        assertEquals(1 << 8, v.getMaxColumns());
        assertEquals(v.getMaxColumns() - 1, v.getLastColumnIndex());
        assertEquals(1 << 16, v.getMaxRows());
        assertEquals(v.getMaxRows() - 1, v.getLastRowIndex());
        assertEquals(30, v.getMaxFunctionArgs());
        assertEquals(3, v.getMaxConditionalFormats());
        assertEquals("IV", v.getLastColumnName());
    }

    public void testExcel2007(){
        SpreadsheetVersion v = SpreadsheetVersion.EXCEL2007;
        assertEquals(1 << 14, v.getMaxColumns());
        assertEquals(v.getMaxColumns() - 1, v.getLastColumnIndex());
        assertEquals(1 << 20, v.getMaxRows());
        assertEquals(v.getMaxRows() - 1, v.getLastRowIndex());
        assertEquals(255, v.getMaxFunctionArgs());
        assertEquals(Integer.MAX_VALUE, v.getMaxConditionalFormats());
        assertEquals("XFD", v.getLastColumnName());
    }
}
