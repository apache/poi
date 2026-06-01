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

package org.apache.poi.poifs.crypt.agile;

import static org.apache.poi.poifs.crypt.Decryptor.DEFAULT_POIFS_ENTRY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TestAgileDecryptor {
    @SuppressWarnings("PrimitiveArrayArgumentToVarargsMethod")
    public static Stream<Arguments> data() {
        List<Arguments> data = new ArrayList<>();
        data.add(Arguments.of(new byte[15]));
        data.add(Arguments.of(new byte[16]));
        data.add(Arguments.of(new byte[17]));
        data.add(Arguments.of(new byte[3292]));
        data.add(Arguments.of(new byte[3293]));
        data.add(Arguments.of(new byte[4000]));

        return data.stream();
    }

    @ParameterizedTest
    @MethodSource("data")
    void testAgileDecryptor(byte[] testData) throws Exception {
        EncryptionInfo infoEnc = new EncryptionInfo(EncryptionMode.agile);
        Encryptor enc = infoEnc.getEncryptor();
        enc.confirmPassword("f");

        byte[] encData;
        byte[] encDocument;
        try (POIFSFileSystem fsEnc = new POIFSFileSystem()) {
            try (OutputStream os = enc.getDataStream(fsEnc)) {
                os.write(testData);
            }

            UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get();
            fsEnc.writeFilesystem(bos);

            bos.close();
            encData = bos.toByteArray();

            DocumentInputStream dis = fsEnc.getRoot().createDocumentInputStream(DEFAULT_POIFS_ENTRY);
            /*long _length =*/ dis.readLong();
            encDocument = IOUtils.toByteArray(dis);
        }

        byte[] actualData;
        try (POIFSFileSystem fsDec = new POIFSFileSystem(new ByteArrayInputStream(encData))) {
            EncryptionInfo infoDec = new EncryptionInfo(fsDec);
            Decryptor dec = infoDec.getDecryptor();
            assertTrue(dec.verifyPassword("f"));
            InputStream is = dec.getDataStream(fsDec);

            actualData = IOUtils.toByteArray(is);
            is.close();
        }

        // input-data and resulting decrypted data should be equal
        assertArrayEquals(testData, actualData,
                "Having " + testData.length + " bytes, had expected \n" +
                HexDump.dump(testData, 0, 0) + " and actual \n" +
                HexDump.dump(actualData, 0, 0) + " encrypted \n" +
                HexDump.dump(encDocument, 0, 0) + " full encrypted \n" +
                HexDump.dump(encData, 0, 0));
    }

    @Test
    void testExcessiveSpinCountIsRejected() throws Exception {
        // Create a normal encrypted document
        EncryptionInfo infoEnc = new EncryptionInfo(EncryptionMode.agile);
        Encryptor enc = infoEnc.getEncryptor();
        enc.confirmPassword("test");

        byte[] encData;
        try (POIFSFileSystem fsEnc = new POIFSFileSystem()) {
            try (OutputStream os = enc.getDataStream(fsEnc)) {
                os.write(new byte[16]);
            }
            UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get();
            fsEnc.writeFilesystem(bos);
            encData = bos.toByteArray();
        }

        // Read the EncryptionInfo stream and replace the spinCount with a value exceeding the maximum
        byte[] encInfoBytes;
        try (POIFSFileSystem fsMod = new POIFSFileSystem(new ByteArrayInputStream(encData));
             DocumentInputStream dis = fsMod.getRoot().createDocumentInputStream(EncryptionInfo.ENCRYPTION_INFO_ENTRY)) {
            encInfoBytes = IOUtils.toByteArray(dis);
        }

        // The first 8 bytes are version/flags; the remainder is the XML descriptor
        String xml = new String(encInfoBytes, 8, encInfoBytes.length - 8, StandardCharsets.UTF_8);
        String modifiedXml = xml.replaceAll("spinCount=\"\\d+\"",
                "spinCount=\"" + (AgileEncryptionVerifier.getMaxSpinCount() + 1) + "\"");

        byte[] xmlBytes = modifiedXml.getBytes(StandardCharsets.UTF_8);
        byte[] modifiedEncInfoBytes = new byte[8 + xmlBytes.length];
        System.arraycopy(encInfoBytes, 0, modifiedEncInfoBytes, 0, 8);
        System.arraycopy(xmlBytes, 0, modifiedEncInfoBytes, 8, xmlBytes.length);

        assertThrows(EncryptedDocumentException.class, () ->
                new EncryptionInfo(new LittleEndianByteArrayInputStream(modifiedEncInfoBytes), null));
    }
}
