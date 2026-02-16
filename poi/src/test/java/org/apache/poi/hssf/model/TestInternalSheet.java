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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.Test;

class TestInternalSheet {
    @Test
    void testEmptySheet() {
        InternalSheet sheet = InternalSheet.createSheet();
        sheet.groupColumnRange(0, 0, true);
        sheet.groupRowRange(0, 0, true);
        sheet.setDefaultColumnStyle(0, 0);
    }

    @Test
    void testMissingBOFRecord() {
        assertThrows(RecordFormatException.class,
                () -> InternalSheet.createSheet(new RecordStream(
                List.of(new BOFRecord()), 0)));
    }


    @Test
    void testInvalidBOFRecord() {
        assertThrows(RecordFormatException.class,
                () -> InternalSheet.createSheet(new RecordStream(
                        List.of(new BOFRecord()), 0)));
    }

    @Test
    void testInvalidBOFRecord2() {
        assertThrows(RecordFormatException.class,
                () -> InternalSheet.createSheet(new RecordStream(
                        List.of(BOFRecord.createSheetBOF()), 0)));
    }

    @Test
    void testEmptyRecordStream() {
        InternalSheet sheet = InternalSheet.createSheet(new RecordStream(
                List.of(BOFRecord.createSheetBOF(),
                        new WindowTwoRecord(),
                        EOFRecord.instance), 0));
        assertThrows(IllegalStateException.class,
                () -> sheet.groupColumnRange(0, 0, true));
        sheet.groupRowRange(0, 0, true);
        assertThrows(IllegalStateException.class,
                () -> sheet.setDefaultColumnStyle(0, 0));
    }
}
