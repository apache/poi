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
package org.apache.poi.hssf.record.cont;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.apache.poi.util.HexRead;
import org.junit.jupiter.api.Test;

final class TestContinuableRecordInput {
    /**
     * Values split by a CONTINUE record are read across the record break
     */
    @Test
    void readAcrossContinue() {
        RecordInputStream in = TestcaseRecordInputStream.create(HexRead.readFromString(
                "AA AA 01 00 34 " + // record with the first byte of a short
                "3C 00 01 00 12 " + // CONTINUE with the second byte
                "3C 00 03 00 01 02 03 " + // CONTINUE with half of a double
                "3C 00 05 00 04 05 06 07 08"
        ));
        ContinuableRecordInput cri = new ContinuableRecordInput(in);
        assertEquals(0x1234, cri.readShort());
        assertEquals(Double.longBitsToDouble(0x0807060504030201L), cri.readDouble());
    }
}
