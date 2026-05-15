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

package org.apache.poi.poifs.crypt.tests;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.util.ZipEntrySource;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.temp.AesZipFileZipEntrySource;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.extractor.XSSFBEventBasedExcelExtractor;
import org.apache.poi.xssf.extractor.XSSFEventBasedExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Test;

class TestSecureTempZip {

    @Test
    void encryptedTempZipStreamsUseIndependentCiphers() throws IOException {
        byte[] payload = new byte[4096];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte)(i & 0xff);
        }

        try (AesZipFileZipEntrySource source = AesZipFileZipEntrySource.createZipEntrySource(
                zipInputStream(new ZipPayload("payload.bin", payload)))) {
            ZipArchiveEntry entry = source.getEntry("payload.bin");
            try (InputStream first = source.getInputStream(entry);
                 InputStream second = source.getInputStream(entry)) {
                byte[] firstPrefix = first.readNBytes(127);
                assertArrayEquals(payload, IOUtils.toByteArray(second));

                ByteArrayOutputStream firstBytes = new ByteArrayOutputStream();
                firstBytes.write(firstPrefix);
                firstBytes.write(IOUtils.toByteArray(first));
                assertArrayEquals(payload, firstBytes.toByteArray());
            }
        }
    }

    @Test
    void encryptedTempZipCreationAppliesMaxEntrySize() throws IOException {
        long oldMaxEntrySize = ZipSecureFile.getMaxEntrySize();
        try {
            ZipSecureFile.setMaxEntrySize(32);
            byte[] payload = new byte[128];

            IOException ex = assertThrows(IOException.class, () ->
                    AesZipFileZipEntrySource.createZipEntrySource(zipInputStream(new ZipPayload("large.bin", payload))));
            assertTrue(ex.getMessage().contains("Zip bomb detected"));
        } finally {
            ZipSecureFile.setMaxEntrySize(oldMaxEntrySize);
        }
    }

    @Test
    void encryptedTempZipCreationAppliesMaxFileCount() throws IOException {
        long oldMaxFileCount = ZipSecureFile.getMaxFileCount();
        try {
            ZipSecureFile.setMaxFileCount(1);

            IOException ex = assertThrows(IOException.class, () ->
                    AesZipFileZipEntrySource.createZipEntrySource(zipInputStream(
                            new ZipPayload("first.bin", new byte[] {1}),
                            new ZipPayload("second.bin", new byte[] {2}))));
            assertTrue(ex.getMessage().contains("MAX_FILE_COUNT"));
        } finally {
            ZipSecureFile.setMaxFileCount(oldMaxFileCount);
        }
    }

    /**
     * Test case for #59841 - this is an example on how to use encrypted temp files,
     * which are streamed into POI opposed to having everything in memory
     */
    @Test
    void protectedTempZip() throws IOException, GeneralSecurityException, XmlException, OpenXML4JException {
        File tikaProt = XSSFTestDataSamples.getSampleFile("protected_passtika.xlsx");
        FileInputStream fis = new FileInputStream(tikaProt);
        POIFSFileSystem poifs = new POIFSFileSystem(fis);
        EncryptionInfo ei = new EncryptionInfo(poifs);
        Decryptor dec = ei.getDecryptor();
        boolean passOk = dec.verifyPassword("tika");
        assertTrue(passOk);

        // extract encrypted ooxml file and write to custom encrypted zip file
        InputStream is = dec.getDataStream(poifs);

        // provide ZipEntrySource to poi which decrypts on the fly
        ZipEntrySource source = AesZipFileZipEntrySource.createZipEntrySource(is);

        // test the source
        OPCPackage opc = OPCPackage.open(source);
        String expected = "This is an Encrypted Excel spreadsheet.";

        XSSFEventBasedExcelExtractor extractor = new XSSFEventBasedExcelExtractor(opc);
        extractor.setIncludeSheetNames(false);
        String txt = extractor.getText();
        assertEquals(expected, txt.trim());

        XSSFWorkbook wb = new XSSFWorkbook(opc);
        txt = wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue();
        assertEquals(expected, txt);

        extractor.close();

        wb.close();
        opc.close();
        source.close();
        poifs.close();
        fis.close();
    }

    /**
     * Now try with xlsb.
     */
    @Test
    void protectedXLSBZip() throws IOException, GeneralSecurityException, XmlException, OpenXML4JException {
        //The test file requires that JCE unlimited be installed.
        //If it isn't installed, skip this test.
        int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
        assumeTrue(maxKeyLen == 0x7FFFFFFF, "Please install JCE Unlimited Strength Jurisdiction Policy files for AES 256");

        File tikaProt = XSSFTestDataSamples.getSampleFile("protected_passtika.xlsb");
        FileInputStream fis = new FileInputStream(tikaProt);
        POIFSFileSystem poifs = new POIFSFileSystem(fis);
        EncryptionInfo ei = new EncryptionInfo(poifs);
        Decryptor dec = ei.getDecryptor();
        boolean passOk = dec.verifyPassword("tika");
        assertTrue(passOk);

        // extract encrypted ooxml file and write to custom encrypted zip file
        InputStream is = dec.getDataStream(poifs);

        // provide ZipEntrySource to poi which decrypts on the fly
        ZipEntrySource source = AesZipFileZipEntrySource.createZipEntrySource(is);

        // test the source
        OPCPackage opc = OPCPackage.open(source);
        String expected = "You can't see me";

        XSSFBEventBasedExcelExtractor extractor = new XSSFBEventBasedExcelExtractor(opc);
        extractor.setIncludeSheetNames(false);
        String txt = extractor.getText();
        assertEquals(expected, txt.trim());

        extractor.close();
        opc.close();
        poifs.close();
        fis.close();
    }

    @Test
    void rejectsZipBombInput() throws IOException {
        byte[] zipBytes = buildHighlyCompressedZip("xl/workbook.xml", 256 * 1024);

        double defaultRatio = ZipSecureFile.getMinInflateRatio();
        long defaultGrace = ZipSecureFile.getGraceEntrySize();
        ZipSecureFile.setGraceEntrySize(0);
        ZipSecureFile.setMinInflateRatio(0.50d);
        try {
            IOException exception = assertThrows(IOException.class, () -> {
                try (InputStream is = new ByteArrayInputStream(zipBytes);
                     AesZipFileZipEntrySource source = AesZipFileZipEntrySource.createZipEntrySource(is)) {
                    // no-op
                }
            });
            assertTrue(exception.getMessage().contains("ZipSecureFile.setMinInflateRatio()"),
                    "unexpected exception message: " + exception.getMessage());
        } finally {
            ZipSecureFile.setMinInflateRatio(defaultRatio);
            ZipSecureFile.setGraceEntrySize(defaultGrace);
        }
    }

    private static InputStream zipInputStream(ZipPayload... payloads) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos)) {
            for (ZipPayload payload : payloads) {
                zos.putArchiveEntry(new ZipArchiveEntry(payload.name));
                zos.write(payload.bytes);
                zos.closeArchiveEntry();
            }
        }
        return new ByteArrayInputStream(bos.toByteArray());
    }

    private static byte[] buildHighlyCompressedZip(String entryName, int payloadSize) throws IOException {
        byte[] payload = new byte[payloadSize];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos)) {
            ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
            zos.putArchiveEntry(entry);
            zos.write(payload);
            zos.closeArchiveEntry();
        }
        return bos.toByteArray();
    }

    private static final class ZipPayload {
        private final String name;
        private final byte[] bytes;

        private ZipPayload(String name, byte[] bytes) {
            this.name = name;
            this.bytes = bytes;
        }

}
