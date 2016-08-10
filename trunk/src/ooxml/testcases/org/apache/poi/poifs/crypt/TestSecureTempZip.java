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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.util.ZipEntrySource;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.extractor.XSSFEventBasedExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlException;
import org.junit.Test;

public class TestSecureTempZip {
    /**
     * Test case for #59841 - this is an example on how to use encrypted temp files,
     * which are streamed into POI opposed to having everything in memory
     */
    @Test
    public void protectedTempZip() throws IOException, GeneralSecurityException, XmlException, OpenXML4JException {
        final File tmpFile = TempFile.createTempFile("protectedXlsx", ".zip");
        File tikaProt = XSSFTestDataSamples.getSampleFile("protected_passtika.xlsx");
        FileInputStream fis = new FileInputStream(tikaProt);
        POIFSFileSystem poifs = new POIFSFileSystem(fis);
        EncryptionInfo ei = new EncryptionInfo(poifs);
        Decryptor dec = ei.getDecryptor();
        boolean passOk = dec.verifyPassword("tika");
        assertTrue(passOk);

        // generate session key
        SecureRandom sr = new SecureRandom();
        byte[] ivBytes = new byte[16], keyBytes = new byte[16];
        sr.nextBytes(ivBytes);
        sr.nextBytes(keyBytes);
        
        // extract encrypted ooxml file and write to custom encrypted zip file 
        InputStream is = dec.getDataStream(poifs);
        copyToFile(is, tmpFile, CipherAlgorithm.aes128, keyBytes, ivBytes);
        is.close();
        
        // provide ZipEntrySource to poi which decrypts on the fly
        ZipEntrySource source = fileToSource(tmpFile, CipherAlgorithm.aes128, keyBytes, ivBytes);

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
        tmpFile.delete();
    }
    
    private void copyToFile(InputStream is, File tmpFile, CipherAlgorithm cipherAlgorithm, byte keyBytes[], byte ivBytes[]) throws IOException, GeneralSecurityException {
        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, cipherAlgorithm.jceId);
        Cipher ciEnc = CryptoFunctions.getCipher(skeySpec, cipherAlgorithm, ChainingMode.cbc, ivBytes, Cipher.ENCRYPT_MODE, "PKCS5Padding");

        ZipInputStream zis = new ZipInputStream(is);
        FileOutputStream fos = new FileOutputStream(tmpFile);
        ZipOutputStream zos = new ZipOutputStream(fos);

        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            // the cipher output stream pads the data, therefore we can't reuse the ZipEntry with set sizes
            // as those will be validated upon close()
            ZipEntry zeNew = new ZipEntry(ze.getName());
            zeNew.setComment(ze.getComment());
            zeNew.setExtra(ze.getExtra());
            zeNew.setTime(ze.getTime());
            // zeNew.setMethod(ze.getMethod());
            zos.putNextEntry(zeNew);
            FilterOutputStream fos2 = new FilterOutputStream(zos){
                // don't close underlying ZipOutputStream
                public void close() {}
            };
            CipherOutputStream cos = new CipherOutputStream(fos2, ciEnc);
            IOUtils.copy(zis, cos);
            cos.close();
            fos2.close();
            zos.closeEntry();
            zis.closeEntry();
        }
        zos.close();
        fos.close();
        zis.close();
    }
    
    private ZipEntrySource fileToSource(File tmpFile, CipherAlgorithm cipherAlgorithm, byte keyBytes[], byte ivBytes[]) throws ZipException, IOException {
        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, cipherAlgorithm.jceId);
        Cipher ciDec = CryptoFunctions.getCipher(skeySpec, cipherAlgorithm, ChainingMode.cbc, ivBytes, Cipher.DECRYPT_MODE, "PKCS5Padding");
        ZipFile zf = new ZipFile(tmpFile);
        return new AesZipFileZipEntrySource(zf, ciDec);
    }

    static class AesZipFileZipEntrySource implements ZipEntrySource {
        final ZipFile zipFile;
        final Cipher ci;

        AesZipFileZipEntrySource(ZipFile zipFile, Cipher ci) {
            this.zipFile = zipFile;
            this.ci = ci;
        }

        /**
         * Note: the file sizes are rounded up to the next cipher block size,
         * so don't rely on file sizes of these custom encrypted zip file entries!
         */
        public Enumeration<? extends ZipEntry> getEntries() {
            return zipFile.entries();
        }

        @SuppressWarnings("resource")
        public InputStream getInputStream(ZipEntry entry) throws IOException {
            InputStream is = zipFile.getInputStream(entry);
            return new CipherInputStream(is, ci);
        }

        @Override
        public void close() throws IOException {
            zipFile.close();
        }
    }
}
