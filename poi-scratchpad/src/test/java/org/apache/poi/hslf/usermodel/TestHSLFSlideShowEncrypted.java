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

package org.apache.poi.hslf.usermodel;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.poi.hslf.exceptions.CorruptPowerPointFileException;
import org.apache.poi.hslf.record.DocumentEncryptionAtom;
import org.apache.poi.hslf.record.Record;
import org.junit.jupiter.api.Test;

class TestHSLFSlideShowEncrypted {

    @Test
    void addEncryptionRecordNeedsPersistRecords() {
        // without a PersistPtrHolder and UserEditAtom there is nowhere to add the encryption record
        Record[] records = new Record[0];
        assertThrows(CorruptPowerPointFileException.class,
            () -> HSLFSlideShowEncrypted.addEncryptionRecord(records, new DocumentEncryptionAtom()));
    }
}
