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

package org.apache.poi.openxml4j.opc;

import org.apache.poi.openxml4j.OpenXML4JTestDataSamples;
import org.apache.poi.openxml4j.opc.internal.EncryptedTempFilePackagePart;
import org.apache.poi.util.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEncryptedTempFilePackagePart {
    @Test
    void testRoundTrip() throws Exception {
        String text = UUID.randomUUID().toString();
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        String filepath =  OpenXML4JTestDataSamples.getSampleFileName("sample.docx");

        try (OPCPackage p = OPCPackage.open(filepath, PackageAccess.READ)) {
            PackagePartName name = new PackagePartName("/test.txt", true);
            EncryptedTempFilePackagePart part = new EncryptedTempFilePackagePart(p, name, "text/plain");
            try (OutputStream os = part.getOutputStream()) {
                os.write(bytes);
            }
            assertEquals(bytes.length, part.getSize());
            try (InputStream is = part.getInputStream()) {
                assertEquals(text, new String(IOUtils.toByteArray(is), StandardCharsets.UTF_8));
            }
        }
    }
}
