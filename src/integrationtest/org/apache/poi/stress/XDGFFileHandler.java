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
package org.apache.poi.stress;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.util.PackageHelper;
import org.junit.Test;

public class XDGFFileHandler extends AbstractFileHandler {
    @Override
    public void handleFile(InputStream stream) throws Exception {
        // ignore password protected files
        if (POIXMLDocumentHandler.isEncrypted(stream)) return;

        TestXDGFXMLDocument doc = new TestXDGFXMLDocument(stream);
        new POIXMLDocumentHandler().handlePOIXMLDocument(doc);
    }

    @Override
    public void handleExtracting(File file) throws Exception {
        // TODO: extraction/actual operations not supported yet
    }

    // a test-case to test this locally without executing the full TestAllFiles
    @Test
    public void test() throws Exception {
        OPCPackage pkg = OPCPackage.open("test-data/diagram/test.vsdx", PackageAccess.READ);
        try {
            TestXDGFXMLDocument doc = new TestXDGFXMLDocument(pkg);
            new POIXMLDocumentHandler().handlePOIXMLDocument(doc);
        } finally {
            pkg.close();
        }
    }

    // TODO: Get rid of this when full visio ooxml support is added
    private final static class TestXDGFXMLDocument extends POIXMLDocument {
        public TestXDGFXMLDocument(OPCPackage pkg) {
            super(pkg, PackageRelationshipTypes.VISIO_CORE_DOCUMENT);
        }

        public TestXDGFXMLDocument(InputStream is) throws IOException {
            this(PackageHelper.open(is));
        }

        public List<PackagePart> getAllEmbedds() throws OpenXML4JException {
            return new ArrayList<PackagePart>();
        }
    }
}