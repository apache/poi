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
package org.apache.poi.stress;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * Needs to be implemented in this package to have access to
 * HSSFWorkbook.getWorkbook()
 */
class HSSFRecordsStresser {
    public static void handleWorkbook(HSSFWorkbook wb) {
        List<org.apache.poi.hssf.record.Record> records = wb.getWorkbook().getRecords();
        for(org.apache.poi.hssf.record.Record record : records) {
            // some Records do not implement clone ?!
            // equals instead of instanceof is on purpose here to only skip exactly this class and not any derived ones
//            if(record.getClass().equals(InterfaceHdrRecord.class) ||
//                    record.getClass().equals(MMSRecord.class) ||
//                    record.getClass().equals(InterfaceEndRecord.class) ||
//                    record.getClass().equals(WriteAccessRecord.class) ||
//                    record.getClass().equals(CodepageRecord.class) ||
//                    record.getClass().equals(DSFRecord.class)) {
//                continue;
//            }
            try {
                Record newRecord = record.copy();

                assertEquals( record.getClass(), newRecord.getClass(), "Expecting the same class back from clone(), but had Record of type " + record.getClass() + " and got back a " + newRecord.getClass() + " from clone()" );

                byte[] origBytes = record.serialize();
                byte[] newBytes = newRecord.serialize();

                assertArrayEquals( origBytes, newBytes, "Record of type " + record.getClass() + " should return the same byte array via the clone() method, but did return a different array" );
            } catch (RuntimeException e) {
                // some Records do not implement clone, ignore those for now
                assertTrue(e.getMessage().contains("needs to define a clone method"));
            }
        }
    }

    // a test-case to test this locally without executing the full TestAllFiles
    @Test
    void test() throws Exception {
        try (InputStream stream = new FileInputStream(HSSFTestDataSamples.getSampleFile("15556.xls"))) {
            HSSFWorkbook wb = new HSSFWorkbook(stream);
            handleWorkbook(wb);
            wb.close();
        }
    }
}
