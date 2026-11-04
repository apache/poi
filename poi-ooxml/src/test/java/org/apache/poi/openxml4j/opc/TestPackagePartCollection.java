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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.opc.internal.MemoryPackagePart;
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
     * which is quadratic in the length of deeply nested names. Filtering the
     * prefixes by the lengths of the registered names isn't enough either, as a
     * package can also contain names with just those lengths, which is what the
     * first loop adds.
     */
    @Test
    void testDeeplyNestedPartNames() throws Exception {
        String folder = "a/".repeat(15_000);
        UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            addEntry(zos, "[Content_Types].xml",
                "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/></Types>");
            for (int i = 0; i < 50; i++) {
                addEntry(zos, folder + "d" + "x".repeat(i) + ".xml", "<a/>");
            }
            for (int i = 0; i < 50; i++) {
                addEntry(zos, folder + folder + "p" + i + ".xml", "<a/>");
            }
        }

        assertTimeout(Duration.ofSeconds(60), () -> {
            try (OPCPackage pkg = OPCPackage.open(bos.toInputStream())) {
                assertEquals(100, pkg.getParts().size());
            }
        });

        // the names are still checked
        PackagePartCollection parts = new PackagePartCollection();
        String deep = "/" + folder + "p.xml";
        put(parts, deep);
        assertThrows(InvalidOperationException.class, () -> put(parts, deep + "/derived.xml"));
    }

    /**
     * {@link PackagePartName#compare} treats runs of digits by their value, so
     * differently spelled names can be the same part for the lookup
     */
    @Test
    void testEqualNamesSpelledDifferently() throws Exception {
        PackagePartCollection parts = new PackagePartCollection();
        put(parts, "/a01b1.xml");
        assertNotNull(parts.remove(PackagingURIHelper.createPartName("/a1b01.xml")));
        put(parts, "/a01b1.xml");
        put(parts, "/a1b01.xml");
        assertEquals(1, parts.size());
        assertNotNull(parts.remove(PackagingURIHelper.createPartName("/a01b1.xml")));
        put(parts, "/a01b1.xml/x.xml");
        assertEquals(1, parts.size());
    }

    @Test
    void testDerivedPartNames() throws Exception {
        PackagePartCollection parts = new PackagePartCollection();
        put(parts, "/a/b/c.xml");
        put(parts, "/a/b/d.xml");
        put(parts, "/a/x.xml");

        assertThrows(InvalidOperationException.class, () -> put(parts, "/a/b/c.xml/y.xml"));
        assertThrows(InvalidOperationException.class, () -> put(parts, "/a/x.xml/y.xml"));
        // a prefix which doesn't end at a slash is fine
        put(parts, "/a/b/c.xm");
        put(parts, "/a/b.xml");
        assertEquals(5, parts.size());

        assertNotNull(parts.remove(PackagingURIHelper.createPartName("/a/b/c.xml")));
        assertNull(parts.remove(PackagingURIHelper.createPartName("/a/b/c.xml")));
        put(parts, "/a/b/c.xml/y.xml");
        assertThrows(InvalidOperationException.class, () -> put(parts, "/a/b/d.xml/y.xml"));
        assertEquals(5, parts.size());
    }

    /**
     * Part names are equal regardless of case, so the derived name check and
     * {@link PackagePartCollection#remove} have to ignore case as well
     */
    @Test
    void testDerivedPartNamesIgnoreCase() throws Exception {
        PackagePartCollection parts = new PackagePartCollection();
        put(parts, "/word/document.xml");
        assertThrows(InvalidOperationException.class, () -> put(parts, "/Word/Document.xml/image1.gif"));

        put(parts, "/A.xml");
        assertNotNull(parts.remove(PackagingURIHelper.createPartName("/a.xml")));
        put(parts, "/A.xml/x.xml");
        assertEquals(2, parts.size());
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

    private static void addEntry(ZipOutputStream zos, String name, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private static void put(PackagePartCollection parts, String name) throws Exception {
        PackagePartName partName = PackagingURIHelper.createPartName(name);
        parts.put(partName, new MemoryPackagePart(null, partName, ContentTypes.XML, false));
    }
}
