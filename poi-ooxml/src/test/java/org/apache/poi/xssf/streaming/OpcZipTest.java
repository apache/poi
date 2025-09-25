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

package org.apache.poi.xssf.streaming;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class OpcZipTest {
    @Test
    void compareOutput() throws Exception {
        Map<String, String> contents = createContents();
        try (
                UnsynchronizedByteArrayOutputStream bos1 = UnsynchronizedByteArrayOutputStream.builder().get();
                UnsynchronizedByteArrayOutputStream bos2 = UnsynchronizedByteArrayOutputStream.builder().get()
        ) {
            try (OpcOutputStream zip = new OpcOutputStream(bos1)) {
                for (Map.Entry<String, String> entry : contents.entrySet()) {
                    zip.putNextEntry(entry.getKey());
                    PrintStream printer = new PrintStream(zip, true, StandardCharsets.UTF_8.name());
                    printer.print(entry.getValue());
                    printer.flush();
                    zip.closeEntry();
                }
            }
            try (com.github.rzymek.opczip.OpcOutputStream zip = new com.github.rzymek.opczip.OpcOutputStream(bos2)) {
                for (Map.Entry<String, String> entry : contents.entrySet()) {
                    zip.putNextEntry(new ZipEntry(entry.getKey()));
                    PrintStream printer = new PrintStream(zip, true, StandardCharsets.UTF_8.name());
                    printer.print(entry.getValue());
                    printer.flush();
                    zip.closeEntry();
                }
            }
            assertArrayEquals(bos1.toByteArray(), bos2.toByteArray());
        }
    }

    private static Map<String, String> createContents() {
        Map<String, String> contents = new LinkedHashMap<>();
        for (int i = 0; i < 3; i++) {
            String name = String.format(Locale.US, "dir%s/file%s.txt", i % 3, i);
            contents.put(name, "this is the contents");
        }
        return contents;
    }
}
