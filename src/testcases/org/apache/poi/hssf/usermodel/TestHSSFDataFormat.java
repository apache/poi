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

package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.BaseTestDataFormat;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormat;

/**
 * Tests for {@link HSSFDataFormat}
 */
public final class TestHSSFDataFormat extends BaseTestDataFormat {

    public TestHSSFDataFormat() {
        super(HSSFITestDataProvider.instance);
    }

    /**
     * [Bug 49928] formatCellValue returns incorrect value for \u00a3 formatted cells
     */
    public void test49928(){
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("49928.xls");
        doTest49928Core(wb);

        // an attempt to register an existing format returns its index
        int poundFmtIdx = wb.getSheetAt(0).getRow(0).getCell(0).getCellStyle().getDataFormat();
        assertEquals(poundFmtIdx, wb.createDataFormat().getFormat(poundFmt));

        // now create a custom format with Pound (\u00a3)
        DataFormat dataFormat = wb.createDataFormat();
        short customFmtIdx = dataFormat.getFormat("\u00a3##.00[Yellow]");
        assertTrue(customFmtIdx >= BuiltinFormats.FIRST_USER_DEFINED_FORMAT_INDEX );
        assertEquals("\u00a3##.00[Yellow]", dataFormat.getFormat(customFmtIdx));
    }
}
