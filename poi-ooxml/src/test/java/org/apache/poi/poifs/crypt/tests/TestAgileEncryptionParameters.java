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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.crypt.ChainingMode;
import org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TestAgileEncryptionParameters {
    public static Stream<Arguments> data() throws Exception {
        CipherAlgorithm[] caList = {CipherAlgorithm.aes128, CipherAlgorithm.aes192, CipherAlgorithm.aes256, CipherAlgorithm.rc2, CipherAlgorithm.des, CipherAlgorithm.des3};
        HashAlgorithm[] haList = {HashAlgorithm.sha1, HashAlgorithm.sha256, HashAlgorithm.sha384, HashAlgorithm.sha512, HashAlgorithm.md5};
        ChainingMode[] cmList = {ChainingMode.cbc, ChainingMode.cfb};

        List<byte[]> byteList = initTestData();

        List<Arguments> data = new ArrayList<>();
        for (CipherAlgorithm ca : caList) {
            for (HashAlgorithm ha : haList) {
                for (ChainingMode cm : cmList) {
                    // do not iterate all byte-arrays here to keep runtime of test at bay
                    data.add(Arguments.of(byteList.get(0), EncryptionMode.agile, ca, ha, cm));
                    data.add(Arguments.of(byteList.get(1), EncryptionMode.agile, ca, ha, cm));
                }
            }
        }

        // iterate all byte-array for each encryption-mode
        // they usually only support certain algorithms, so keep them fixed
        // also to not have a very long test-runtime
        for (byte[] bytes : byteList) {
            data.add(Arguments.of(bytes, EncryptionMode.agile, CipherAlgorithm.aes192, HashAlgorithm.sha256, ChainingMode.cbc));

            data.add(Arguments.of(bytes, EncryptionMode.standard, CipherAlgorithm.aes128, HashAlgorithm.sha1, ChainingMode.ecb));

            // CryptoAPI does not support getDataStream()
            // data.add(Arguments.of(bytes, EncryptionMode.cryptoAPI, CipherAlgorithm.rc4, HashAlgorithm.sha1, ChainingMode.cfb));

            // xor does not support createEncryptionInfoEntry()
            // data.add(Arguments.of(bytes, EncryptionMode.xor, CipherAlgorithm.des3, HashAlgorithm.md5, ChainingMode.cfb));

            data.add(Arguments.of(bytes, EncryptionMode.binaryRC4, CipherAlgorithm.rc4, HashAlgorithm.sha512, ChainingMode.ecb));
        }

        return data.stream();
    }

    public static List<byte[]> initTestData() throws Exception {
        List<byte[]> data = new ArrayList<>();

        // read a sample file for encrypting
        try (InputStream testFile = POIDataSamples.getDocumentInstance().openResourceAsStream("SampleDoc.docx")) {
            data.add(IOUtils.toByteArray(testFile));
        }

        // create a small sample workbook for encrypting
        UnsynchronizedByteArrayOutputStream bosOrig = UnsynchronizedByteArrayOutputStream.builder().get();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet();
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell = row.createCell(0);
            cell.setCellValue("Hello Apache POI");
            workbook.write(bosOrig);
        }
        bosOrig.close();

        data.add(IOUtils.toByteArray(new ByteArrayInputStream(bosOrig.toByteArray())));

        // test with a dummy-block of data that is a multiple of 16
        byte[] testData = new byte[4000];
        for (int i = 0; i < testData.length; i++) {
            testData[i] = (byte)i;
        }
        data.add(testData);

        // test with a dummy-block of data that is not a multiple of 16
        testData = new byte[3292];
        for (int i = 0; i < testData.length; i++) {
            testData[i] = (byte)i;
        }
        data.add(testData);

        return data;
    }

    @ParameterizedTest
    @MethodSource("data")
    void testAgileEncryptionModes(byte[] testData, EncryptionMode mode, CipherAlgorithm ca, HashAlgorithm ha, ChainingMode cm)
            throws Exception {
        EncryptionInfo infoEnc = new EncryptionInfo(mode, ca, ha, -1, -1, cm);
        Encryptor enc = infoEnc.getEncryptor();
        enc.confirmPassword("foobaa");

        byte[] inputData;
        try (POIFSFileSystem fsEnc = new POIFSFileSystem()) {
            try (OutputStream os = enc.getDataStream(fsEnc)) {
                os.write(testData);
            }

            try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
                fsEnc.writeFilesystem(bos);

                bos.close();
                inputData = bos.toByteArray();
            }
        }

        byte[] actualData;
        try (POIFSFileSystem fsDec = new POIFSFileSystem(new ByteArrayInputStream(inputData))) {
            EncryptionInfo infoDec = new EncryptionInfo(fsDec);
            Decryptor dec = infoDec.getDecryptor();
            boolean passed = dec.verifyPassword("foobaa");
            assertTrue(passed);
            InputStream is = dec.getDataStream(fsDec);

            actualData = IOUtils.toByteArray(is);
            is.close();
        }

        // input-data and resulting decrypted data should be equal
        assertArrayEquals(testData, actualData,
                "Having " + testData.length + " bytes and parameters - " + ca + "-" + ha + "-" + cm);
    }
}
