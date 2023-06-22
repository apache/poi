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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

public final class POIXMLDocumentHandler {
    protected void handlePOIXMLDocument(POIXMLDocument doc) throws Exception {
        assertNotNull(doc.getAllEmbeddedParts());
        assertNotNull(doc.getPackage());
        assertNotNull(doc.getPackagePart());
        assertNotNull(doc.getProperties());
        assertNotNull(doc.getRelations());
    }

    protected static boolean isEncrypted(InputStream stream) throws IOException {
        if (FileMagic.valueOf(stream) == FileMagic.OLE2) {
            try (POIFSFileSystem poifs = new POIFSFileSystem(stream)) {
                if (poifs.getRoot().hasEntryCaseInsensitive(Decryptor.DEFAULT_POIFS_ENTRY)) {
                    return true;
                }
            }
            throw new IOException("Wrong file format or file extension for OO XML file");
        }
        return false;
    }

    /**
     * Recurse through the document and convert all elements so they are available in the ooxml-lite jar.
     * This method only makes sense for hierarchical documents like .docx.
     * If the document is split up in different parts like in .pptx, each part needs to be provided.
     *
     * @param base the entry point
     */
    protected static void cursorRecursive(XmlObject base) {
        try (XmlCursor cur = base.newCursor()) {
            cursorRecursive(cur);
        }
    }

    private static void cursorRecursive(XmlCursor cur) {
        do {
            assertNotNull(cur.getObject());
            cur.push();
            for (boolean b = cur.toFirstAttribute(); b; b = cur.toNextAttribute()) {
                assertNotNull(cur.getObject());
            }
            cur.pop();
            cur.push();

            if (cur.toFirstChild()) {
                cursorRecursive(cur);
            }
            cur.pop();
        } while (cur.toNextSibling());
    }
}
