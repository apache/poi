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

package org.apache.poi.hssf.record.chart;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.junit.jupiter.api.Test;

/**
 * Tests the serialization and deserialization of the PlotAreaRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 */
final class TestPlotAreaRecord {
    byte[] data = new byte[] {

    };

    @Test
    void testLoad() {
        PlotAreaRecord record = new PlotAreaRecord(TestcaseRecordInputStream.create(0x1035, data));

        assertEquals( 4, record.getRecordSize() );
    }

    @Test
    void testStore() {
        PlotAreaRecord record = new PlotAreaRecord();

        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
    }
}
