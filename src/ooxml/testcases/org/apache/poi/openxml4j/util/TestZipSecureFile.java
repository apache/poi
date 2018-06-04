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
package org.apache.poi.openxml4j.util;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Test;

import java.io.InputStream;
import java.util.Enumeration;

import static org.junit.Assert.assertTrue;

public class TestZipSecureFile {
    @Test
    public void testThresholdInputStream() throws Exception {
        // This fails in Java 10 because our reflection injection of the ThresholdInputStream causes a
        // ClassCastException in ZipFile now
        // The relevant change in the JDK is http://hg.openjdk.java.net/jdk/jdk10/rev/85ea7e83af30#l5.66

        try (ZipFile thresholdInputStream = new ZipFile(XSSFTestDataSamples.getSampleFile("template.xlsx"))) {
            try (ZipSecureFile secureFile = new ZipSecureFile(XSSFTestDataSamples.getSampleFile("template.xlsx"))) {
                Enumeration<? extends ZipArchiveEntry> entries = thresholdInputStream.getEntries();
                while (entries.hasMoreElements()) {
                    ZipArchiveEntry entry = entries.nextElement();

                    try (InputStream inputStream = secureFile.getInputStream(entry)) {
                        assertTrue(IOUtils.toByteArray(inputStream).length > 0);
                    }
                }
            }
        }
    }
}
