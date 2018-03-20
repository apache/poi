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

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.openxml4j.opc.internal.PartMarshaller;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.apache.poi.openxml4j.opc.PackagingURIHelper.PACKAGE_RELATIONSHIPS_ROOT_URI;
import static org.junit.Assert.assertTrue;

public class TestZipPackagePropertiesMarshaller {
    private PartMarshaller marshaller = new ZipPackagePropertiesMarshaller();

    @Test(expected=IllegalArgumentException.class)
    public void nonZipOutputStream() throws OpenXML4JException {
        OutputStream notAZipOutputStream = new ByteArrayOutputStream(0);
        marshaller.marshall(null, notAZipOutputStream);
    }

    @Test
    public void withZipOutputStream() throws Exception {
        assertTrue(marshaller.marshall(new PackagePropertiesPart(null, PackagingURIHelper.createPartName(PACKAGE_RELATIONSHIPS_ROOT_URI)),
                new ZipOutputStream(new ByteArrayOutputStream())));
    }

    @Test
    public void writingFails() throws Exception {
        assertTrue(marshaller.marshall(new PackagePropertiesPart(null, PackagingURIHelper.createPartName(PACKAGE_RELATIONSHIPS_ROOT_URI)),
                new ZipOutputStream(new ByteArrayOutputStream())));
    }

    @Test(expected=OpenXML4JException.class)
    public void ioException() throws Exception {
        marshaller.marshall(new PackagePropertiesPart(null, PackagingURIHelper.createPartName(PACKAGE_RELATIONSHIPS_ROOT_URI)),
                new ZipOutputStream(new ByteArrayOutputStream()) {
                    @Override
                    public void putNextEntry(ZipEntry e) throws IOException {
                        throw new IOException("TestException");
                    }
                });
    }
}
