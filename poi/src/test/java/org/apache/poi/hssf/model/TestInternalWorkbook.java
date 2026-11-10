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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.record.CountryRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.TabIdRecord;
import org.junit.jupiter.api.Test;

class TestInternalWorkbook {
    @Test
    void testRecordsNoSheet() {
        List<Record> wbRecords = new ArrayList<>();

        // Finally we need an EoF record
        wbRecords.add(EOFRecord.instance);

        InternalWorkbook wb = InternalWorkbook.createWorkbook(wbRecords);
        assertNotNull(wb);

        assertThrows(IllegalStateException.class,
                () -> wb.setSheetName(0, "test"));
    }

    @Test
    void testRecordsWithSheet() {
        List<Record> wbRecords = new ArrayList<>();

        wbRecords.add(new CountryRecord());
        wbRecords.add(new SSTRecord());
        wbRecords.add(new TabIdRecord());

        // Finally we need an EoF record
        wbRecords.add(EOFRecord.instance);

        InternalWorkbook wb = InternalWorkbook.createWorkbook(wbRecords);
        assertNotNull(wb);

        wb.setSheetName(0, "test");
    }

    @Test
    void testRecordsInvalidTabPos() {
        List<Record> wbRecords = new ArrayList<>();

        wbRecords.add(new CountryRecord());
        wbRecords.add(new SSTRecord());
        wbRecords.add(new TabIdRecord());

        // Finally we need an EoF record
        wbRecords.add(EOFRecord.instance);

        InternalWorkbook wb = InternalWorkbook.createWorkbook(wbRecords);
        assertNotNull(wb);

        wb.getWorkbookRecordList().setTabpos(1);

        assertThrows(IllegalStateException.class,
                () -> wb.setSheetName(0, "test"));
    }
}