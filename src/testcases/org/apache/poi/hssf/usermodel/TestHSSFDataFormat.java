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

import static org.apache.poi.hssf.HSSFTestDataSamples.openSampleWorkbook;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.ss.usermodel.BaseTestDataFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link HSSFDataFormat}
 */
final class TestHSSFDataFormat extends BaseTestDataFormat {
    private static final POILogger _logger = POILogFactory.getLogger(TestHSSFDataFormat.class);

    public TestHSSFDataFormat() {
        super(HSSFITestDataProvider.instance);
    }

    /**
     * Bug 51378: getDataFormatString method call crashes when reading the test file
     */
    @Test
    void test51378() throws IOException {
        List<String> expNull = Arrays.asList( "0-3-0","0-43-11" );
        try (HSSFWorkbook wb = openSampleWorkbook("12561-1.xls")) {
            for (Sheet sheet : wb) {
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        CellStyle style = cell.getCellStyle();
                        assertNotNull(style);
                        String coord = wb.getSheetIndex(sheet)+"-"+cell.getRowIndex()+"-"+cell.getColumnIndex();
                        String fmt = style.getDataFormatString();
                        assertEquals(expNull.contains(coord), fmt == null, coord+" unexpected");
                    }
                }
            }
        }
    }

}
