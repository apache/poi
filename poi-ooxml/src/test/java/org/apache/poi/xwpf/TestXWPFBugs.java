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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.filesystem.Ole10Native;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.DocumentDocument;

class TestXWPFBugs {
    private static final POIDataSamples samples = POIDataSamples.getDocumentInstance();

    @Test()
    void truncatedDocx() {
        //started failing after uptake of commons-compress 1.21
        assertThrows(IOException.class, () -> {
            try (InputStream fis = samples.openResourceAsStream("truncated62886.docx");
                 OPCPackage opc = OPCPackage.open(fis)) {
                assertNotNull(opc);
                //XWPFWordExtractor ext = new XWPFWordExtractor(opc)) {
                //assertNotNull(ext.getText());
            }
        });
    }

    /**
     * A Word document that's encrypted with non-standard
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

        // Check we can read the Word document in that
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
     * A Word document with aes-256, i.e. aes is always 128 bit (= 128 bit block size),
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

        // Check we can read the Word document in that
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

    @Test
    void missingXsbs() throws IOException, XmlException {
        String[] files = {"bib-chernigovka.netdo.ru_download_docs_17459.docx"};
        for (String f : files) {
            ZipFile zf = new ZipFile(samples.getFile(f));
            ZipArchiveEntry entry = zf.getEntry("word/document.xml");
            DocumentDocument document = DocumentDocument.Factory.parse(zf.getInputStream(entry));
            assertNotNull(document);
            zf.close();
        }
    }

    @Test
    void bug65649() throws IOException {
        try (XWPFDocument document = new XWPFDocument(samples.openResourceAsStream("bug65649.docx"))) {
            assertEquals(731, document.getParagraphs().size());
        }
    }

    @Test
    void tika3388() throws Exception {
        try (XWPFDocument document = new XWPFDocument(samples.openResourceAsStream("tika-3388.docx"))) {
            assertEquals(1, document.getParagraphs().size());
            PackagePartName partName = PackagingURIHelper.createPartName("/word/embeddings/oleObject1.bin");
            PackagePart part = document.getPackage().getPart(partName);
            assertNotNull(part);
            try (
                InputStream partStream = part.getInputStream();
                POIFSFileSystem poifs = new POIFSFileSystem(partStream)
            ) {
                Ole10Native ole = Ole10Native.createFromEmbeddedOleObject(poifs);
                String fn = "C:\\Users\\ross\\AppData\\Local\\Microsoft\\Windows\\INetCache\\Content.Word\\約翰的測試文件\uD83D\uDD96.msg";
                assertEquals(fn, ole.getFileName());
                assertEquals(fn, ole.getFileName2());
            }
        }
    }

    @Test
    void insertParagraphDirectlyIntoBody() throws IOException {
        try (XWPFDocument document = new XWPFDocument(samples.openResourceAsStream("bug66312.docx"))) {
            XWPFParagraph paragraph = document.getParagraphArray(0);
            insertParagraph(paragraph, document);
            assertEquals("Hello", document.getParagraphArray(0).getText());
            assertEquals("World", document.getParagraphArray(1).getText());
        }
    }

    @Test
    void insertTableDirectlyIntoBody() throws IOException {
        try (XWPFDocument document = new XWPFDocument(samples.openResourceAsStream("bug66312.docx"))) {
            XWPFParagraph paragraph = document.getParagraphArray(0);
            insertTable(paragraph, document);
            assertEquals("Hello", document.getTableArray(0).getRow(0).getCell(0).getText());
            assertEquals("World", document.getParagraphArray(0).getText());
        }
    }

    @Test
    void insertParagraphIntoTable() throws IOException {
        try (XWPFDocument document = new XWPFDocument(samples.openResourceAsStream("bug66312.docx"))) {
            XWPFTableCell cell = document.getTableArray(0).getRow(0).getCell(0);
            XWPFParagraph paragraph = cell.getParagraphArray(0);
            insertParagraph(paragraph, document);
            assertEquals("Hello", cell.getParagraphArray(0).getText());
            assertEquals("World", cell.getParagraphArray(1).getText());
        }
    }

    @Test
    void insertTableIntoTable() throws IOException {
        try (XWPFDocument document = new XWPFDocument(samples.openResourceAsStream("bug66312.docx"))) {
            XWPFTableCell cell = document.getTableArray(0).getRow(0).getCell(0);
            XWPFParagraph paragraph = cell.getParagraphArray(0);
            insertTable(paragraph, document);
            assertEquals("Hello", cell.getTableArray(0).getRow(0).getCell(0).getText());
            assertEquals("World", cell.getParagraphArray(0).getText());
        }
    }


    public static void insertParagraph(XWPFParagraph xwpfParagraph, XWPFDocument document) {
        XmlCursor xmlCursor = xwpfParagraph.getCTP().newCursor();
        XWPFParagraph xwpfParagraph2 = document.insertNewParagraph(xmlCursor);
        xwpfParagraph2.createRun().setText("Hello");
    }

    public static void insertTable(XWPFParagraph xwpfParagraph, XWPFDocument document) {
        XmlCursor xmlCursor = xwpfParagraph.getCTP().newCursor();
        XWPFTable xwpfTable = document.insertNewTbl(xmlCursor);
        xwpfTable.getRow(0).getCell(0).setText("Hello");
    }
}
