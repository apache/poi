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

import static org.apache.poi.POIDataSamples.getDocumentInstance;
import static org.apache.poi.POIDataSamples.getSlideShowInstance;
import static org.apache.poi.POIDataSamples.getSpreadSheetInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import org.apache.poi.POIDataSamples;
import org.apache.poi.POIDocument;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.ooxml.extractor.ExtractorFactory;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.poifs.crypt.cryptoapi.CryptoAPIEncryptionHeader;
import org.apache.poi.poifs.storage.RawDataUtil;
import org.apache.xmlbeans.XmlException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestHxxFEncryption {
    @Parameter(value = 0)
    public POIDataSamples sampleDir;

    @Parameter(value = 1)
    public String file;

    @Parameter(value = 2)
    public String password;

    @Parameter(value = 3)
    public String expected;

    @Parameters(name="{1}")
    public static Collection<Object[]> data() throws IOException {
        return Arrays.asList(
            // binary rc4
            new Object[]{ getDocumentInstance(), "password_tika_binaryrc4.doc", "tika", "This is an encrypted Word 2007 File." },
            // cryptoapi
            new Object[]{ getDocumentInstance(), "password_password_cryptoapi.doc", "password", "This is a test" },
            // binary rc4
            new Object[]{ getSpreadSheetInstance(), "password.xls", "password",
                x("H4sIAAAAAAAAAF1Uu24bMRDs/RULVwkgCUhSpHaZwkDgpHJH8fZ0G/Nx4ZI6y13yG/mRfIb9R5mlZFlIpdPtcnZmdnjPf57/vvx6+f3h6obuv3"+
                  "ylbY5bEiVHe1fEpUp5pOgkrK0iabehm7FyoZi1ks8xcvHiQu8h5bLnorTlnUvkJ/YPOHKsLVInAqCs91KakuaxLq4w3g00SgCo9Xou1UnCmSBe"+
                  "MhpRY6qHmXVFteQfQJ5yUaaOw4qXwgPVjPGAqhNH5bBHAfTmwqqoSkLdFT/J3nC0eZBRk7yiu5s7yoU+r+9l3tDtm5A3jgt6AQxNOY2ya+U4sK"+
                  "XZ+YczbpfSVVuzFOuunKraqIVD2ND3yVXauT3TNthR/O3IJAM7gzTOGeIcXZvj14ahotW8wSognlMu0Yyp/Fi7O6s+CK6haUUjtPCji7MVcgqH"+
                  "jh+42tqeqPDMroJ/lBAE4AZbJbJu6Fu35ej42Tw9mYeTwVXoBKJiPeFV94q2rZJAyNEPo/qOdWYLBpq3B2JX8GDZeJ14mZf3tOQWBmpd9yQ7kI"+
                  "DCY/jmkj1oGOicFy62r9vutC5uJsVEMFgmAXXfYcC6BRBKNHCybALFJolnrDcPXNLl+K60Vctt09YZT7YgbeOICGJ/ZgC2JztOnm1JhX3eJXni"+
                  "U5Bqhezzlu334vD/Ajr3yDGXw5G9IZ6aLmLfQafY42N3J7cjj1LaXOHihSrcC5ThmuYIB5FX5AU8tKlnNG9Dn1EnsdD4KcnPhsSNPRiXtz461b"+
                  "VZw8Pm6vn0afh4fvr0D5P/+cMuBAAA") },
            // cryptoapi
            new Object[]{ getSpreadSheetInstance(), "35897-type4.xls", "freedom", "Sheet1\nhello there!" },
            // cryptoapi (PPT only supports cryptoapi...)
            new Object[]{ getSlideShowInstance(), "cryptoapi-proc2356.ppt", "crypto", "Dominic Salemno" }
        );
    }

    private static String x(String base64) throws IOException {
        return new String(RawDataUtil.decompress(base64), StandardCharsets.UTF_8);
    }
    
    @Test
    public void extract() throws IOException, OpenXML4JException, XmlException {
        File f = sampleDir.getFile(file);
        Biff8EncryptionKey.setCurrentUserPassword(password);
        try (POITextExtractor te = ExtractorFactory.createExtractor(f)) {
            String actual = te.getText().trim();
            assertEquals(expected, actual);
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(null);
        }
    }
    
    @Test
    public void changePassword() throws IOException, OpenXML4JException, XmlException {
        newPassword("test");
    }
    
    @Test
    public void removePassword() throws IOException, OpenXML4JException, XmlException {
        newPassword(null);
    }
    
    public void newPassword(String newPass) throws IOException, OpenXML4JException, XmlException {
        File f = sampleDir.getFile(file);
        Biff8EncryptionKey.setCurrentUserPassword(password);
        try (POITextExtractor te1 = ExtractorFactory.createExtractor(f)) {
            Biff8EncryptionKey.setCurrentUserPassword(newPass);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (POIDocument doc = (POIDocument) te1.getDocument()) {
                doc.write(bos);
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            try (POITextExtractor te2 = ExtractorFactory.createExtractor(bis)) {
                String actual = te2.getText().trim();
                assertEquals(expected, actual);
            }
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(null);
        }
    }

    /** changing the encryption mode and key size in poor mans style - see comments below */
    @Test
    public void changeEncryption() throws IOException, OpenXML4JException, XmlException {
        File f = sampleDir.getFile(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Biff8EncryptionKey.setCurrentUserPassword(password);
        try (POITextExtractor te1 = ExtractorFactory.createExtractor(f)) {
            // first remove encryption
            Biff8EncryptionKey.setCurrentUserPassword(null);
            try (POIDocument doc = (POIDocument) te1.getDocument()) {
                doc.write(bos);
            }
            // then use default setting, which is cryptoapi
            String newPass = "newPass";
            try (POITextExtractor te2 = ExtractorFactory.createExtractor(new ByteArrayInputStream(bos.toByteArray()))) {
                Biff8EncryptionKey.setCurrentUserPassword(newPass);
                try (POIDocument doc = (POIDocument) te2.getDocument()) {
                    bos.reset();
                    doc.write(bos);
                }
            }
            // and finally update cryptoapi setting
            try (POITextExtractor te3 = ExtractorFactory.createExtractor(new ByteArrayInputStream(bos.toByteArray()));
                 POIDocument doc = (POIDocument) te3.getDocument()) {
                // need to cache data (i.e. read all data) before changing the key size
                if (doc instanceof HSLFSlideShowImpl) {
                    HSLFSlideShowImpl hss = (HSLFSlideShowImpl) doc;
                    hss.getPictureData();
                    hss.getDocumentSummaryInformation();
                }
                EncryptionInfo ei = doc.getEncryptionInfo();
                assertNotNull(ei);
                assertTrue(ei.getHeader() instanceof CryptoAPIEncryptionHeader);
                assertEquals(0x28, ei.getHeader().getKeySize());
                ei.getHeader().setKeySize(0x78);
                bos.reset();
                doc.write(bos);
            }
            // check the setting
            try (POITextExtractor te4 = ExtractorFactory.createExtractor(new ByteArrayInputStream(bos.toByteArray()));
                 POIDocument doc = (POIDocument) te4.getDocument()) {
                EncryptionInfo ei = doc.getEncryptionInfo();
                assertNotNull(ei);
                assertTrue(ei.getHeader() instanceof CryptoAPIEncryptionHeader);
                assertEquals(0x78, ei.getHeader().getKeySize());
            }
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(null);
        }
    }
}
