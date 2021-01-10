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

import static org.apache.poi.ddf.DefaultEscherRecordFactory.isContainer;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherTextboxRecord;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordBase;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFTestHelper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;

class TestEscherRecordFactory {

    private static byte[] toByteArray(List<RecordBase> records) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (RecordBase rb : records) {
            Record r = (org.apache.poi.hssf.record.Record) rb;
            try {
                out.write(r.serialize());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return out.toByteArray();
    }

    @Test
    void testDetectContainer() {
        Random rnd = new Random();
        assertTrue(isContainer((short) 0x0, EscherContainerRecord.DG_CONTAINER));
        assertTrue(isContainer((short) 0x0, EscherContainerRecord.SOLVER_CONTAINER));
        assertTrue(isContainer((short) 0x0, EscherContainerRecord.SP_CONTAINER));
        assertTrue(isContainer((short) 0x0, EscherContainerRecord.DGG_CONTAINER));
        assertTrue(isContainer((short) 0x0, EscherContainerRecord.BSTORE_CONTAINER));
        assertTrue(isContainer((short) 0x0, EscherContainerRecord.SPGR_CONTAINER));

        for (short i=EscherContainerRecord.DGG_CONTAINER; i<= EscherContainerRecord.SOLVER_CONTAINER; i++){
            assertTrue(isContainer(Integer.valueOf(rnd.nextInt(Short.MAX_VALUE)).shortValue(), i));
        }

        assertFalse(isContainer((short) 0x0, Integer.valueOf(EscherContainerRecord.DGG_CONTAINER - 1).shortValue()));
        assertFalse(isContainer((short) 0x0, Integer.valueOf(EscherContainerRecord.SOLVER_CONTAINER + 1).shortValue()));

        assertTrue(isContainer((short) 0x000F, Integer.valueOf(EscherContainerRecord.DGG_CONTAINER - 1).shortValue()));
        assertTrue(isContainer((short) 0xFFFF, Integer.valueOf(EscherContainerRecord.DGG_CONTAINER - 1).shortValue()));
        assertFalse(isContainer((short) 0x000C, Integer.valueOf(EscherContainerRecord.DGG_CONTAINER - 1).shortValue()));
        assertFalse(isContainer((short) 0xCCCC, Integer.valueOf(EscherContainerRecord.DGG_CONTAINER - 1).shortValue()));
        assertFalse(isContainer((short) 0x000F, EscherTextboxRecord.RECORD_ID));
        assertFalse(isContainer((short) 0xCCCC, EscherTextboxRecord.RECORD_ID));
    }

    @Test
    void testDgContainerMustBeRootOfHSSFSheetEscherRecords() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("47251.xls");
        HSSFSheet sh = wb.getSheetAt(0);
        InternalSheet ish = HSSFTestHelper.getSheetForTest(sh);
        List<RecordBase> records = ish.getRecords();
        // records to be aggregated
        List<RecordBase> dgRecords = records.subList(19, 23);
        byte[] dgBytes = toByteArray(dgRecords);
        sh.getDrawingPatriarch();
        EscherAggregate agg = (EscherAggregate) ish.findFirstRecordBySid(EscherAggregate.sid);
        assertNotNull(agg);
        assertTrue(agg.getEscherRecords().get(0) instanceof EscherContainerRecord);
        assertEquals(EscherContainerRecord.DG_CONTAINER, agg.getEscherRecords().get(0).getRecordId());
        assertEquals((short) 0x0, agg.getEscherRecords().get(0).getOptions());
        agg = (EscherAggregate) ish.findFirstRecordBySid(EscherAggregate.sid);
        assertNotNull(agg);
        byte[] dgBytesAfterSave = agg.serialize();
        assertEquals(dgBytes.length, dgBytesAfterSave.length, "different size of drawing data before and after save");
        assertArrayEquals(dgBytes, dgBytesAfterSave, "drawing data before and after save is different");
    }
}
