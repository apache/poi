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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.PrintStream;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.commons.io.output.NullPrintStream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

@Isolated("Modifies the test data directory")
@ResourceLock(Resources.SYSTEM_OUT)
class TestReSave extends BaseTestIteratingXLS {
    @Override
    protected Map<String, Class<? extends Throwable>> getExcludes() {
        Map<String, Class<? extends Throwable>> excludes = super.getExcludes();
        // unsupported crypto api header
        excludes.put("35897-type4.xls", EncryptedDocumentException.class);
        excludes.put("51832.xls", EncryptedDocumentException.class);
        excludes.put("xor-encryption-abc.xls", EncryptedDocumentException.class);
        excludes.put("password.xls", EncryptedDocumentException.class);
        // HSSFWorkbook cannot open it as well
        excludes.put("43493.xls", RecordInputStream.LeftoverDataException.class);
        excludes.put("44958_1.xls", RecordInputStream.LeftoverDataException.class);
        // "Buffer overrun"
        excludes.put("XRefCalc.xls", RuntimeException.class);
        return excludes;
    }

    @Override
    void runOneFile(File fileIn) throws Exception {
        // avoid running on files leftover from previous failed runs
        if(fileIn.getName().endsWith("-saved.xls")) {
            return;
        }

        PrintStream save = System.out;
        try {
            // redirect standard out during the test to avoid spamming the console with output
            System.setOut(new NullPrintStream());

            File reSavedFile = new File(fileIn.getParentFile(), fileIn.getName().replace(".xls", "-saved.xls"));
            try {
                ReSave.main(new String[] { fileIn.getAbsolutePath() });

                // also try BiffViewer on the saved file
                new TestBiffViewer().runOneFile(reSavedFile);

                // had one case where the re-saved could not be re-saved!
                ReSave.main(new String[] { "-bos", reSavedFile.getAbsolutePath() });
            } finally {
                // clean up the re-saved file
                assertTrue(!reSavedFile.exists() || reSavedFile.delete());
            }
        } finally {
            System.setOut(save);
        }
    }

    @Disabled("Only used for local testing")
    @Test
    void testOneFile() throws Exception {
        String dataDirName = System.getProperty(POIDataSamples.TEST_PROPERTY);
        if(dataDirName == null) {
            dataDirName = "test-data";
        }

        runOneFile(new File(dataDirName + "/spreadsheet", "49931.xls"));
    }
}
