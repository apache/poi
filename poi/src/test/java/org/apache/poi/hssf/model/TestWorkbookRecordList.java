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

package org.apache.poi.hssf.model;

import org.apache.poi.hssf.record.chart.ChartRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.apache.poi.hssf.HSSFTestDataSamples.openSampleWorkbook;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestWorkbookRecordList {

    @Test
    public void tabposIsOnlyUpdatedIfWorkbookHasTabIdRecord() throws IOException {
        try (HSSFWorkbook wb = openSampleWorkbook("55982.xls")) {
            WorkbookRecordList records = wb.getInternalWorkbook().getWorkbookRecordList();
            assertEquals(-1, records.getTabpos());

            // Add an arbitrary record to the front of the list
            records.add(0, new ChartRecord());

            assertEquals(-1, records.getTabpos());
        }
    }
}
