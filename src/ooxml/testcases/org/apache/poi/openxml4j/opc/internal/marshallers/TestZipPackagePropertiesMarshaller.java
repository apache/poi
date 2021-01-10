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

package org.apache.poi.openxml4j.opc.internal.marshallers;

import static org.apache.poi.openxml4j.opc.PackagingURIHelper.PACKAGE_RELATIONSHIPS_ROOT_URI;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.openxml4j.opc.internal.PartMarshaller;
import org.junit.jupiter.api.Test;

class TestZipPackagePropertiesMarshaller {
    private final PartMarshaller marshaller = new ZipPackagePropertiesMarshaller();

    private boolean marshall() throws OpenXML4JException {
        return marshall(new ZipArchiveOutputStream(new ByteArrayOutputStream()));
    }

    private boolean marshall(OutputStream zos) throws OpenXML4JException {
        PackagePartName rootUri = PackagingURIHelper.createPartName(PACKAGE_RELATIONSHIPS_ROOT_URI);
        PackagePropertiesPart part = new PackagePropertiesPart(null, rootUri);
        return marshaller.marshall(part, zos);
    }


    @Test
    void nonZipOutputStream() {
        OutputStream notAZipOutputStream = new ByteArrayOutputStream(0);
        assertThrows(IllegalArgumentException.class, () -> marshall(notAZipOutputStream));
    }

    @Test
    void withZipOutputStream() throws Exception {
        assertTrue(marshall());
    }

    @Test
    void ioException() {
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(new ByteArrayOutputStream()) {
            @Override
            public void putArchiveEntry(final ArchiveEntry archiveEntry) throws IOException {
                throw new IOException("TestException");
            }
        };
        assertThrows(OpenXML4JException.class, () -> marshall(zos));
    }
}
