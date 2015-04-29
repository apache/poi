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

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.Ignore;
import org.junit.Test;

public final class POIXMLDocumentHandler {
	protected void handlePOIXMLDocument(POIXMLDocument doc) throws Exception {
		assertNotNull(doc.getAllEmbedds());
		assertNotNull(doc.getPackage());
		assertNotNull(doc.getPackagePart());
		assertNotNull(doc.getProperties());
		assertNotNull(doc.getRelations());
	}

    protected static boolean isEncrypted(InputStream stream) throws IOException {
        if (POIFSFileSystem.hasPOIFSHeader(stream)) {
            POIFSFileSystem poifs = new POIFSFileSystem(stream);
            if (poifs.getRoot().hasEntry(Decryptor.DEFAULT_POIFS_ENTRY)) {
                return true;
            }
            throw new IOException("wrong file format or file extension for OO XML file");
        }
        return false;
    }
	
	// a test-case to test this locally without executing the full TestAllFiles
    @Ignore("POIXMLDocument cannot handle this Visio file currently...")
	@Test
	public void test() throws Exception {
		OPCPackage pkg = OPCPackage.open("test-data/diagram/test.vsdx", PackageAccess.READ);
		try {
			handlePOIXMLDocument(new TestPOIXMLDocument(pkg));
		} finally {
			pkg.close();
		}
	}
	
	private final static class TestPOIXMLDocument extends POIXMLDocument {
		public TestPOIXMLDocument(OPCPackage pkg) {
			super(pkg);
		}

		public List<PackagePart> getAllEmbedds() throws OpenXML4JException {
			return null;
		}
	}
}
