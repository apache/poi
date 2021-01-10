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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.hssf.record.CountryRecord;
import org.apache.poi.hssf.record.FontRecord;
import org.apache.poi.hssf.record.RecalcIdRecord;
import org.apache.poi.hssf.record.WriteAccessRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.TestHSSFWorkbook;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.udf.AggregatingUDFFinder;
import org.apache.poi.ss.formula.udf.DefaultUDFFinder;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.junit.jupiter.api.Test;

/**
 * Unit test for the Workbook class.
 */
final class TestWorkbook {
    @Test
    void testFontStuff() throws IOException {
        HSSFWorkbook hwb = new HSSFWorkbook();
		InternalWorkbook wb = TestHSSFWorkbook.getInternalWorkbook(hwb);

		assertEquals(4, wb.getNumberOfFontRecords());
		assertEquals(68, wb.getRecords().size());

		FontRecord f1 = wb.getFontRecordAt(0);
		FontRecord f4 = wb.getFontRecordAt(3);

		assertEquals(0, wb.getFontIndex(f1));
		assertEquals(3, wb.getFontIndex(f4));

		assertEquals(f1, wb.getFontRecordAt(0));
		assertEquals(f4, wb.getFontRecordAt(3));

		// There is no 4! new ones go in at 5

		FontRecord n = wb.createNewFont();
		assertEquals(69, wb.getRecords().size());
		assertEquals(5, wb.getNumberOfFontRecords());
		assertEquals(5, wb.getFontIndex(n));
		assertEquals(n, wb.getFontRecordAt(5));

		// And another
		FontRecord n6 = wb.createNewFont();
		assertEquals(70, wb.getRecords().size());
		assertEquals(6, wb.getNumberOfFontRecords());
		assertEquals(6, wb.getFontIndex(n6));
		assertEquals(n6, wb.getFontRecordAt(6));


		// Now remove the one formerly at 5
		assertEquals(70, wb.getRecords().size());
		wb.removeFontRecord(n);

		// Check that 6 has gone to 5
		assertEquals(69, wb.getRecords().size());
		assertEquals(5, wb.getNumberOfFontRecords());
		assertEquals(5, wb.getFontIndex(n6));
		assertEquals(n6, wb.getFontRecordAt(5));

		// Check that the earlier ones are unchanged
		assertEquals(0, wb.getFontIndex(f1));
		assertEquals(3, wb.getFontIndex(f4));
		assertEquals(f1, wb.getFontRecordAt(0));
		assertEquals(f4, wb.getFontRecordAt(3));

		// Finally, add another one
		FontRecord n7 = wb.createNewFont();
		assertEquals(70, wb.getRecords().size());
		assertEquals(6, wb.getNumberOfFontRecords());
		assertEquals(6, wb.getFontIndex(n7));
		assertEquals(n7, wb.getFontRecordAt(6));

		hwb.close();
	}

    @Test
    void testAddNameX() throws IOException {
        HSSFWorkbook hwb = new HSSFWorkbook();
        InternalWorkbook wb = TestHSSFWorkbook.getInternalWorkbook(hwb);
        assertNotNull(wb.getNameXPtg("ISODD", AggregatingUDFFinder.DEFAULT));

        FreeRefFunction NotImplemented = (args, ec) -> {
            throw new RuntimeException("not implemented");
        };

        /*
         * register the two test UDFs in a UDF finder, to be passed to the evaluator
         */
        UDFFinder udff1 = new DefaultUDFFinder(new String[] { "myFunc", },
                new FreeRefFunction[] { NotImplemented });
        UDFFinder udff2 = new DefaultUDFFinder(new String[] { "myFunc2", },
                new FreeRefFunction[] { NotImplemented });
        UDFFinder udff = new AggregatingUDFFinder(udff1, udff2);
        assertNotNull(wb.getNameXPtg("myFunc", udff));
        assertNotNull(wb.getNameXPtg("myFunc2", udff));

        assertNull(wb.getNameXPtg("myFunc3", udff));  // myFunc3 is unknown

        hwb.close();
    }

    @Test
    void testRecalcId() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        assertFalse(wb.getForceFormulaRecalculation());

        InternalWorkbook iwb = TestHSSFWorkbook.getInternalWorkbook(wb);
        int countryPos = iwb.findFirstRecordLocBySid(CountryRecord.sid);
        assertTrue(countryPos != -1);
        // RecalcIdRecord is not present in new workbooks
        assertNull(iwb.findFirstRecordBySid(RecalcIdRecord.sid));
        RecalcIdRecord record = iwb.getRecalcId();
        assertNotNull(record);
        assertSame(record, iwb.getRecalcId());

        assertSame(record, iwb.findFirstRecordBySid(RecalcIdRecord.sid));
        assertEquals(countryPos + 1, iwb.findFirstRecordLocBySid(RecalcIdRecord.sid));

        record.setEngineId(100);
        assertEquals(100, record.getEngineId());
        assertTrue(wb.getForceFormulaRecalculation());

        wb.setForceFormulaRecalculation(true); // resets the EngineId flag to zero
        assertEquals(0, record.getEngineId());
        assertFalse(wb.getForceFormulaRecalculation());

        wb.close();
    }

    @Test
    void testWriteAccess() {
        HSSFWorkbook wb = new HSSFWorkbook();
        InternalWorkbook iwb = TestHSSFWorkbook.getInternalWorkbook(wb);

        int oldRecordsCount = iwb.getNumRecords();
        assertEquals(68, oldRecordsCount, "records count");

        WriteAccessRecord writeAccess = iwb.getWriteAccess();
        assertNotNull(writeAccess);
        assertEquals(WriteAccessRecord.sid, writeAccess.getSid());

        int newRecordsCount = iwb.getNumRecords();
        assertEquals(oldRecordsCount, newRecordsCount, "records count after getWriteAccess");
    }
}
