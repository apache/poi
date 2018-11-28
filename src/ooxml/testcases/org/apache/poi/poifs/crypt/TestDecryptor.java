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
package org.apache.poi.poifs.crypt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.stream.IntStream;

import javax.crypto.Cipher;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.junit.Assume;
import org.junit.Test;

public class TestDecryptor {
    private static final POIDataSamples samples = POIDataSamples.getPOIFSInstance();

    @Test
    public void passwordVerification() throws IOException, GeneralSecurityException {
        try (InputStream is = samples.openResourceAsStream("protect.xlsx");
            POIFSFileSystem fs = new POIFSFileSystem(is)) {
            EncryptionInfo info = new EncryptionInfo(fs);
            Decryptor d = Decryptor.getInstance(info);
            assertTrue(d.verifyPassword(Decryptor.DEFAULT_PASSWORD));
        }
    }

    @Test
    public void decrypt() throws IOException, GeneralSecurityException {
        try (InputStream is = samples.openResourceAsStream("protect.xlsx");
             POIFSFileSystem fs = new POIFSFileSystem(is)) {
            EncryptionInfo info = new EncryptionInfo(fs);
            Decryptor d = Decryptor.getInstance(info);
            d.verifyPassword(Decryptor.DEFAULT_PASSWORD);
            zipOk(fs.getRoot(), d);
        }
    }

    @Test
    public void agile() throws IOException, GeneralSecurityException {
        try (InputStream is = samples.openResourceAsStream("protected_agile.docx");
            POIFSFileSystem fs = new POIFSFileSystem(is)) {
            EncryptionInfo info = new EncryptionInfo(fs);
            assertTrue(info.getVersionMajor() == 4 && info.getVersionMinor() == 4);
            Decryptor d = Decryptor.getInstance(info);
            assertTrue(d.verifyPassword(Decryptor.DEFAULT_PASSWORD));
            zipOk(fs.getRoot(), d);
        }
    }

    private void zipOk(DirectoryNode root, Decryptor d) throws IOException, GeneralSecurityException {
        try (ZipArchiveInputStream zin = new ZipArchiveInputStream(d.getDataStream(root))) {

            while (true) {
                ZipArchiveEntry entry = zin.getNextZipEntry();
                if (entry == null) {
                    break;
                }
                // crc32 is checked within zip-stream
                if (entry.isDirectory()) {
                    continue;
                }
                assertEquals(entry.getSize() - 1, zin.skip(entry.getSize() - 1));
                byte buf[] = new byte[10];
                int readBytes = zin.read(buf);
                // zin.available() doesn't work for entries
                assertEquals("size failed for " + entry.getName(), 1, readBytes);
            }
        }
    }

    @Test
    public void dataLength() throws Exception {
        try (InputStream fsIs = samples.openResourceAsStream("protected_agile.docx");
            POIFSFileSystem fs = new POIFSFileSystem(fsIs)) {
            EncryptionInfo info = new EncryptionInfo(fs);
            Decryptor d = Decryptor.getInstance(info);
            d.verifyPassword(Decryptor.DEFAULT_PASSWORD);

            try (InputStream is = d.getDataStream(fs)) {

                long len = d.getLength();
                assertEquals(12810, len);

                byte[] buf = new byte[(int) len];
                assertEquals(12810, is.read(buf));

        ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(buf));

        while (true) {
            ZipArchiveEntry entry = zin.getNextZipEntry();
            if (entry==null) {
                break;
            }

                    IOUtils.toByteArray(zin);
                }
            }
        }
    }

    @Test
    public void bug57080() throws Exception {
        // the test file contains a wrong ole entry size, produced by extenxls
        // the fix limits the available size and tries to read all entries
        File f = samples.getFile("extenxls_pwd123.xlsx");

        try (POIFSFileSystem fs = new POIFSFileSystem(f, true)) {
            EncryptionInfo info = new EncryptionInfo(fs);
            Decryptor d = Decryptor.getInstance(info);
            d.verifyPassword("pwd123");

            final ByteArrayOutputStream bos = new ByteArrayOutputStream(10000);
            try (final ZipArchiveInputStream zis = new ZipArchiveInputStream(d.getDataStream(fs))) {
                IntStream.of(3711, 1155, 445, 9376, 450, 588, 1337, 2593, 304, 7910).forEach(size -> {
                    try {
                        final ZipArchiveEntry ze = zis.getNextZipEntry();
                        assertNotNull(ze);
                        IOUtils.copy(zis, bos);
                        assertEquals(size, bos.size());
                        bos.reset();
                    } catch (IOException e) {
                        fail(e.getMessage());
                    }
                });
            }
        }
    }

    @Test
    public void test58616() throws IOException, GeneralSecurityException {
        try (InputStream is = POIDataSamples.getSpreadSheetInstance().openResourceAsStream("58616.xlsx");
            POIFSFileSystem pfs = new POIFSFileSystem(is)) {
            EncryptionInfo info = new EncryptionInfo(pfs);
            Decryptor dec = Decryptor.getInstance(info);
            dec.getDataStream(pfs).close();
        }
    }

    @Test
    public void bug60320() throws IOException, GeneralSecurityException {
        int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
        Assume.assumeTrue("Please install JCE Unlimited Strength Jurisdiction Policy files for AES 256", maxKeyLen == 2147483647);

        try (InputStream is = samples.openResourceAsStream("60320-protected.xlsx");
            POIFSFileSystem fs = new POIFSFileSystem(is)) {
            EncryptionInfo info = new EncryptionInfo(fs);
            Decryptor d = Decryptor.getInstance(info);
            assertTrue(d.verifyPassword("Test001!!"));
            zipOk(fs.getRoot(), d);
        }
    }    
}
