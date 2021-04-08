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
package org.apache.poi.xwpf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.Cipher;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.DocumentDocument;

class TestXWPFBugs {
    private static final POIDataSamples samples = POIDataSamples.getDocumentInstance();

    @Test
    void truncatedDocx() throws Exception {
        try (InputStream fis = samples.openResourceAsStream("truncated62886.docx");
            OPCPackage opc = OPCPackage.open(fis);
            XWPFWordExtractor ext = new XWPFWordExtractor(opc)) {
            assertNotNull(ext.getText());
        }
    }

    /**
     * A word document that's encrypted with non-standard
     * Encryption options, and no cspname section. See bug 53475
     */
    @Test
    void bug53475NoCSPName() throws Exception {
        File file = samples.getFile("bug53475-password-is-solrcell.docx");
        POIFSFileSystem filesystem = new POIFSFileSystem(file, true);

        // Check the encryption details
        EncryptionInfo info = new EncryptionInfo(filesystem);
        assertEquals(128, info.getHeader().getKeySize());
        assertEquals(CipherAlgorithm.aes128, info.getHeader().getCipherAlgorithm());
        assertEquals(HashAlgorithm.sha1, info.getHeader().getHashAlgorithm());

        // Check it can be decoded
        Decryptor d = Decryptor.getInstance(info);
        assertTrue(d.verifyPassword("solrcell"), "Unable to process: document is encrypted");

        // Check we can read the word document in that
        InputStream dataStream = d.getDataStream(filesystem);
        OPCPackage opc = OPCPackage.open(dataStream);
        XWPFDocument doc = new XWPFDocument(opc);
        XWPFWordExtractor ex = new XWPFWordExtractor(doc);
        String text = ex.getText();
        assertNotNull(text);
        assertEquals("This is password protected Word document.", text.trim());
        ex.close();

        filesystem.close();
    }

    /**
     * A word document with aes-256, i.e. aes is always 128 bit (= 128 bit block size),
     * but the key can be 128/192/256 bits
     */
    @Test
    void bug53475_aes256() throws Exception {
        int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
        assumeTrue(maxKeyLen == 0x7FFFFFFF, "Please install JCE Unlimited Strength Jurisdiction Policy files for AES 256");

        File file = samples.getFile("bug53475-password-is-pass.docx");
        POIFSFileSystem filesystem = new POIFSFileSystem(file, true);

        // Check the encryption details
        EncryptionInfo info = new EncryptionInfo(filesystem);
        assertEquals(16, info.getHeader().getBlockSize());
        assertEquals(256, info.getHeader().getKeySize());
        assertEquals(CipherAlgorithm.aes256, info.getHeader().getCipherAlgorithm());
        assertEquals(HashAlgorithm.sha1, info.getHeader().getHashAlgorithm());

        // Check it can be decoded
        Decryptor d = Decryptor.getInstance(info);
        assertTrue(d.verifyPassword("pass"), "Unable to process: document is encrypted");

        // Check we can read the word document in that
        InputStream dataStream = d.getDataStream(filesystem);
        OPCPackage opc = OPCPackage.open(dataStream);
        XWPFDocument doc = new XWPFDocument(opc);
        XWPFWordExtractor ex = new XWPFWordExtractor(doc);
        String text = ex.getText();
        assertNotNull(text);
        // I know ... a stupid typo, maybe next time ...
        assertEquals("The is a password protected document.", text.trim());
        ex.close();

        filesystem.close();
    }


    @Test
    void bug59058() throws IOException, XmlException {
        String[] files = {"bug57031.docx", "bug59058.docx"};
        for (String f : files) {
            ZipFile zf = new ZipFile(samples.getFile(f));
            ZipArchiveEntry entry = zf.getEntry("word/document.xml");
            DocumentDocument document = DocumentDocument.Factory.parse(zf.getInputStream(entry));
            assertNotNull(document);
            zf.close();
        }
    }
}