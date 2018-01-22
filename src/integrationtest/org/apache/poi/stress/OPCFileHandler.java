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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PushbackInputStream;

import org.apache.poi.openxml4j.opc.ContentTypes;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.junit.Test;

public class OPCFileHandler extends AbstractFileHandler {
	@Override
    public void handleFile(InputStream stream, String path) throws Exception {
        // ignore password protected files
        if (POIXMLDocumentHandler.isEncrypted(stream)) return;

        OPCPackage p = OPCPackage.open(stream);

        for (PackagePart part : p.getParts()) {
            if (part.getPartName().toString().equals("/docProps/core.xml")) {
                assertEquals(ContentTypes.CORE_PROPERTIES_PART, part.getContentType());
            }
            if (part.getPartName().toString().equals("/word/document.xml")) {
                assertTrue("Expected one of " + XWPFRelation.MACRO_DOCUMENT + ", " + XWPFRelation.DOCUMENT + ", " + XWPFRelation.TEMPLATE + 
                        ", but had " + part.getContentType(),
                        XWPFRelation.DOCUMENT.getContentType().equals(part.getContentType()) ||
                        XWPFRelation.MACRO_DOCUMENT.getContentType().equals(part.getContentType()) ||
                        XWPFRelation.TEMPLATE.getContentType().equals(part.getContentType()));
            }
            if (part.getPartName().toString().equals("/word/theme/theme1.xml")) {
                assertEquals(XWPFRelation.THEME.getContentType(), part.getContentType());
            }
        }
    }
	
    @Override
    public void handleExtracting(File file) throws Exception {
        // text-extraction is not possible currently for these types of files
    }

	// a test-case to test this locally without executing the full TestAllFiles
	@Test
	public void test() throws Exception {
        File file = new File("test-data/diagram/test.vsdx");

        try (InputStream stream = new PushbackInputStream(new FileInputStream(file), 100000)) {
            handleFile(stream, file.getPath());
        }
		
		handleExtracting(file);
	}
}
