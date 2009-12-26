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

package org.apache.poi.ss.usermodel;

import junit.framework.TestCase;

import org.apache.poi.ss.ITestDataProvider;

/**
 * Tests of implementation of {@link DataFormat}
 *
 */
public abstract class BaseTestDataFormat extends TestCase {

    private final ITestDataProvider _testDataProvider;

    protected BaseTestDataFormat(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    public final void testBuiltinFormats() {
        Workbook wb = _testDataProvider.createWorkbook();

        DataFormat df = wb.createDataFormat();

        String[] formats = BuiltinFormats.getAll();
        for (int idx = 0; idx < formats.length; idx++) {
            String fmt = formats[idx];
            assertEquals(idx, df.getFormat(fmt));
        }

        //default format for new cells is General
        Sheet sheet = wb.createSheet();
        Cell cell = sheet.createRow(0).createCell(0);
        assertEquals(0, cell.getCellStyle().getDataFormat());
        assertEquals("General", cell.getCellStyle().getDataFormatString());

        //create a custom data format
        String customFmt = "#0.00 AM/PM";
        //check it is not in built-in formats
        assertEquals(-1, BuiltinFormats.getBuiltinFormat(customFmt));
        int customIdx = df.getFormat(customFmt);
        //The first user-defined format starts at 164.
        assertTrue(customIdx >= BuiltinFormats.FIRST_USER_DEFINED_FORMAT_INDEX);
        //read and verify the string representation
        assertEquals(customFmt, df.getFormat((short)customIdx));
    }
}
