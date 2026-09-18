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

package org.apache.poi.ddf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.util.LittleEndian;
import org.junit.jupiter.api.Test;

class TestEscherBSERecordMalformedInput {

    @Test
    void truncatedBSEPayloadDoesNotProduceNegativeRemainingLength() {
        byte[] data = new byte[44];

        LittleEndian.putShort(data, 2, EscherBSERecord.RECORD_ID);
        // A BSE record has 36 bytes of fixed fields after its 8-byte header.
        // This malformed record declares only 35 bytes of payload.
        LittleEndian.putInt(data, 4, 35);

        EscherBSERecord record = new EscherBSERecord();
        int bytesRead = record.fillFields(data, 0, new DefaultEscherRecordFactory());

        assertEquals(44, bytesRead);
        assertEquals(0, record.getRemainingData().length);
    }
}
