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

import static org.apache.poi.openxml4j.OpenXML4JTestDataSamples.openSampleStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.junit.jupiter.api.Test;

public class TestPackagePartCollection {

    @Test
    void testPackagePartCollection_64473() throws Exception {
        InputStream is = openSampleStream("64473.xlsx");
        try (OPCPackage pkg = OPCPackage.open(is)) {
            List<PackagePart> allParts = pkg.getParts();
            assertEquals(15, allParts.size());
            List<PackagePart> partsByName = pkg.getPartsByName(Pattern.compile("/xl/metadata.*"));
            // contains the following parts:
            // /xl/metadata
            // /xl/metadata.xml
            assertEquals(2, partsByName.size());
        }
    }

    /**
     * Checking rule M1.11 used to build and look up every prefix of a part name,
     * which is quadratic in the length of deeply nested names
     */
    @Test
    void testDeeplyNestedPartNames() throws Exception {
        UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            zos.putNextEntry(new ZipEntry("[Content_Types].xml"));
            zos.write(("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
                "<Default Extension=\"xml\" ContentType=\"application/xml\"/></Types>").getBytes(StandardCharsets.UTF_8));
            String folder = "a/".repeat(30_000);
            for (int i = 0; i < 20; i++) {
                zos.putNextEntry(new ZipEntry(folder + i + ".xml"));
                zos.write("<x/>".getBytes(StandardCharsets.UTF_8));
            }
        }

        int parts = assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            try (OPCPackage pkg = OPCPackage.open(bos.toInputStream())) {
                return pkg.getParts().size();
            }
        });
        assertEquals(20, parts);
    }

    @Test
    void testDerivedPartNameAfterRemove() throws Exception {
        try (OPCPackage pkg = OPCPackage.create(UnsynchronizedByteArrayOutputStream.builder().get())) {
            PackagePartName name = PackagingURIHelper.createPartName("/word/document.xml");
            PackagePartName derived = PackagingURIHelper.createPartName("/word/document.xml/image1.gif");
            pkg.createPart(name, ContentTypes.XML);
            assertThrows(InvalidOperationException.class, () -> pkg.createPart(derived, ContentTypes.IMAGE_GIF));

            pkg.removePart(name);
            assertNotNull(pkg.createPart(derived, ContentTypes.IMAGE_GIF));
            pkg.revert();
        }
    }
}
