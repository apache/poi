/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.poifs.crypt.temp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.poi.openxml4j.util.ZipEntrySource;
import org.apache.poi.poifs.crypt.ChainingMode;
import org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.util.Beta;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.TempFile;

/**
 * An example <code>ZipEntrySource</code> that has encrypted temp files to ensure that
 * sensitive data is not stored in raw format on disk.
 */
@Beta
public final class AesZipFileZipEntrySource implements ZipEntrySource {
    private static final POILogger LOG = POILogFactory.getLogger(AesZipFileZipEntrySource.class);

    private static final String PADDING = "PKCS5Padding";

    private final File tmpFile;
    private final ZipFile zipFile;
    private final Cipher ci;
    private boolean closed;
    
    private AesZipFileZipEntrySource(File tmpFile, Cipher ci) throws IOException {
        this.tmpFile = tmpFile;
        this.zipFile = new ZipFile(tmpFile);
        this.ci = ci;
        this.closed = false;
    }
    
    /**
     * Note: the file sizes are rounded up to the next cipher block size,
     * so don't rely on file sizes of these custom encrypted zip file entries!
     */
    @Override
    public Enumeration<? extends ZipArchiveEntry> getEntries() {
        return zipFile.getEntries();
    }

    @Override
    public ZipArchiveEntry getEntry(String path) {
        return zipFile.getEntry(path);
    }

    @Override
    public InputStream getInputStream(ZipArchiveEntry entry) throws IOException {
        InputStream is = zipFile.getInputStream(entry);
        return new CipherInputStream(is, ci);
    }
    
    @Override
    public void close() throws IOException {
        if(!closed) {
            zipFile.close();
            if (!tmpFile.delete()) {
                LOG.log(POILogger.WARN, tmpFile.getAbsolutePath()+" can't be removed (or was already removed.");
            }
        }
        closed = true;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    public static AesZipFileZipEntrySource createZipEntrySource(InputStream is) throws IOException {
        // generate session key
        SecureRandom sr = new SecureRandom();
        byte[] ivBytes = new byte[16], keyBytes = new byte[16];
        sr.nextBytes(ivBytes);
        sr.nextBytes(keyBytes);
        final File tmpFile = TempFile.createTempFile("protectedXlsx", ".zip");
        copyToFile(is, tmpFile, keyBytes, ivBytes);
        IOUtils.closeQuietly(is);
        return fileToSource(tmpFile, keyBytes, ivBytes);
    }

    private static void copyToFile(InputStream is, File tmpFile, byte[] keyBytes, byte[] ivBytes) throws IOException {
        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, CipherAlgorithm.aes128.jceId);
        Cipher ciEnc = CryptoFunctions.getCipher(skeySpec, CipherAlgorithm.aes128, ChainingMode.cbc, ivBytes, Cipher.ENCRYPT_MODE, PADDING);
        
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(is);
            FileOutputStream fos = new FileOutputStream(tmpFile);
            ZipArchiveOutputStream zos = new ZipArchiveOutputStream(fos)) {

            ZipArchiveEntry ze;
            while ((ze = zis.getNextZipEntry()) != null) {
                // the cipher output stream pads the data, therefore we can't reuse the ZipEntry with set sizes
                // as those will be validated upon close()
                ZipArchiveEntry zeNew = new ZipArchiveEntry(ze.getName());
                zeNew.setComment(ze.getComment());
                zeNew.setExtra(ze.getExtra());
                zeNew.setTime(ze.getTime());
                // zeNew.setMethod(ze.getMethod());
                zos.putArchiveEntry(zeNew);
                FilterOutputStream fos2 = new FilterOutputStream(zos) {
                    // don't close underlying ZipOutputStream
                    @Override
                    public void close() {
                    }
                };
                CipherOutputStream cos = new CipherOutputStream(fos2, ciEnc);
                IOUtils.copy(zis, cos);
                cos.close();
                fos2.close();
                zos.closeArchiveEntry();
            }
        }
    }

    private static AesZipFileZipEntrySource fileToSource(File tmpFile, byte[] keyBytes, byte[] ivBytes) throws IOException {
        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, CipherAlgorithm.aes128.jceId);
        Cipher ciDec = CryptoFunctions.getCipher(skeySpec, CipherAlgorithm.aes128, ChainingMode.cbc, ivBytes, Cipher.DECRYPT_MODE, PADDING);
        return new AesZipFileZipEntrySource(tmpFile, ciDec);
    }
    
}