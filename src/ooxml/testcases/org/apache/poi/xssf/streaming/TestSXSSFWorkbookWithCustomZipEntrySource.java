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

package org.apache.poi.xssf.streaming;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import org.apache.poi.openxml4j.util.ZipEntrySource;
import org.apache.poi.poifs.crypt.AesZipFileZipEntrySource;
import org.apache.poi.poifs.crypt.ChainingMode;
import org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

/**
 * This class tests that an SXSSFWorkbook can be written and read where all temporary disk I/O
 * is encrypted, but the final saved workbook is not encrypted
 */
public final class TestSXSSFWorkbookWithCustomZipEntrySource {

    @Test
    public void customZipEntrySource() throws IOException, GeneralSecurityException {
        final String sheetName = "TestSheet1";
        final String cellValue = "customZipEntrySource";
        SXSSFWorkbookWithCustomZipEntrySource workbook = new SXSSFWorkbookWithCustomZipEntrySource();
        SXSSFSheet sheet1 = workbook.createSheet(sheetName);
        SXSSFRow row1 = sheet1.createRow(1);
        SXSSFCell cell1 = row1.createCell(1);
        cell1.setCellValue(cellValue);
        ByteArrayOutputStream os = new ByteArrayOutputStream(8192);
        workbook.write(os);
        workbook.close();
        workbook.dispose();
        XSSFWorkbook xwb = new XSSFWorkbook(new ByteArrayInputStream(os.toByteArray()));
        XSSFSheet xs1 = xwb.getSheetAt(0);
        assertEquals(sheetName, xs1.getSheetName());
        XSSFRow xr1 = xs1.getRow(1);
        XSSFCell xc1 = xr1.getCell(1);
        assertEquals(cellValue, xc1.getStringCellValue());
        xwb.close();
    }
    
    static class SXSSFWorkbookWithCustomZipEntrySource extends SXSSFWorkbook {

        private static final POILogger logger = POILogFactory.getLogger(SXSSFWorkbookWithCustomZipEntrySource.class);

        @Override
        public void write(OutputStream stream) throws IOException {
            flushSheets();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            getXSSFWorkbook().write(os);
            ZipEntrySource source = null;
            try {
                // provide ZipEntrySource to poi which decrypts on the fly
                source = AesZipFileZipEntrySource.createZipEntrySource(new ByteArrayInputStream(os.toByteArray()));
                injectData(source, stream);
            } catch (GeneralSecurityException e) {
                throw new IOException(e);
            } finally {
                source.close();
            }
        }

        @Override
        protected SheetDataWriter createSheetDataWriter() throws IOException {
            //log values to ensure these values are accessible to subclasses
            logger.log(POILogger.INFO, "isCompressTempFiles: " + isCompressTempFiles());
            logger.log(POILogger.INFO, "SharedStringSource: " + getSharedStringSource());
            return new SheetDataWriterWithDecorator();
        }
    }
    
    static class SheetDataWriterWithDecorator extends SheetDataWriter {
        final static CipherAlgorithm cipherAlgorithm = CipherAlgorithm.aes128;
        SecretKeySpec skeySpec;
        byte[] ivBytes;

        public SheetDataWriterWithDecorator() throws IOException {
            super();
        }

        void init() {
            if(skeySpec == null) {
                SecureRandom sr = new SecureRandom();
                ivBytes = new byte[16];
                byte[] keyBytes = new byte[16];
                sr.nextBytes(ivBytes);
                sr.nextBytes(keyBytes);
                skeySpec = new SecretKeySpec(keyBytes, cipherAlgorithm.jceId);
            }
        }
        
        @Override
        protected OutputStream decorateOutputStream(FileOutputStream fos) {
            init();
            Cipher ciEnc = CryptoFunctions.getCipher(skeySpec, cipherAlgorithm, ChainingMode.cbc, ivBytes, Cipher.ENCRYPT_MODE, "PKCS5Padding");
            return new CipherOutputStream(fos, ciEnc);
        }

        @Override
        protected InputStream decorateInputStream(FileInputStream fis) {
            Cipher ciDec = CryptoFunctions.getCipher(skeySpec, cipherAlgorithm, ChainingMode.cbc, ivBytes, Cipher.DECRYPT_MODE, "PKCS5Padding");
            return new CipherInputStream(fis, ciDec);
        }
        
    }
}
