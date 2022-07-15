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
package org.apache.poi.hssf.dev;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.jupiter.api.Assertions;

class TestEFBiffViewer extends BaseTestIteratingXLS {
    @Override
    protected Map<String, Class<? extends Throwable>> getExcludes() {
        Map<String, Class<? extends Throwable>> excludes = super.getExcludes();
        // unsupported crypto api header
        excludes.put("35897-type4.xls", EncryptedDocumentException.class);
        excludes.put("51832.xls", EncryptedDocumentException.class);
        excludes.put("xor-encryption-abc.xls", EncryptedDocumentException.class);
        excludes.put("password.xls", EncryptedDocumentException.class);
        excludes.put("protected_66115.xls", EncryptedDocumentException.class);
        // HSSFWorkbook cannot open it as well
        excludes.put("43493.xls", RecordInputStream.LeftoverDataException.class);
        excludes.put("44958_1.xls", RecordInputStream.LeftoverDataException.class);
        // "Buffer overrun"
        // excludes.put("XRefCalc.xls", RuntimeException.class);
        return excludes;
    }

    @Override
    void runOneFile(File fileIn) throws IOException {
        HSSFRequest req = new HSSFRequest();
        req.addListenerForAllRecords(Assertions::assertNotNull);
        HSSFEventFactory factory = new HSSFEventFactory();

        try (POIFSFileSystem fs = new POIFSFileSystem(fileIn, true);
             InputStream din = BiffViewer.getPOIFSInputStream(fs)) {
            factory.processEvents(req, din);
        }
    }
}
