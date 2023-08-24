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

package org.apache.poi.openxml4j;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Isolated // changes static values, so other tests should not run at the same time
class TestOPCPackageFileLimit {
    @Test
    void testWithReducedFileLimit() throws InvalidFormatException {
        final long defaultLimit = ZipSecureFile.getMaxFileCount();
        ZipSecureFile.setMaxFileCount(5);
        try (InputStream is = HSSFTestDataSamples.openSampleFileStream("HeaderFooterComplexFormats.xlsx")) {
            OPCPackage opcPackage = OPCPackage.open(is);
            fail("expected IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("ZipSecureFile.setMaxFileCount()"),
                    "unexpected exception message: " + e.getMessage());
        } finally {
            ZipSecureFile.setMaxFileCount(defaultLimit);
        }
    }

    @Test
    void testFileWithReducedFileLimit() {
        final File file = HSSFTestDataSamples.getSampleFile("HeaderFooterComplexFormats.xlsx");
        final long defaultLimit = ZipSecureFile.getMaxFileCount();
        ZipSecureFile.setMaxFileCount(5);
        try {
            OPCPackage opcPackage = OPCPackage.open(file);
            fail("expected InvalidFormatException");
        } catch (InvalidFormatException e) {
            assertTrue(e.getMessage().contains("ZipSecureFile.setMaxFileCount()"),
                    "unexpected exception message: " + e.getMessage());
        } finally {
            ZipSecureFile.setMaxFileCount(defaultLimit);
        }
    }
}
