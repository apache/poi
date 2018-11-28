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

package org.apache.poi.hslf.record;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hslf.exceptions.EncryptedPowerPointFileException;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.crypt.cryptoapi.CryptoAPIDecryptor;
import org.apache.poi.poifs.crypt.cryptoapi.CryptoAPIEncryptionHeader;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.Test;

/**
 * Tests that DocumentEncryption works properly.
 */
public class TestDocumentEncryption {
    private static final POIDataSamples slTests = POIDataSamples.getSlideShowInstance();

    @Test
    public void cryptoAPIDecryptionOther() throws Exception {
        String encPpts[] = {
            "Password_Protected-56-hello.ppt",
            "Password_Protected-hello.ppt",
            "Password_Protected-np-hello.ppt",
        };

        Biff8EncryptionKey.setCurrentUserPassword("hello");
        try {
            for (String pptFile : encPpts) {
                try (POIFSFileSystem fs = new POIFSFileSystem(slTests.getFile(pptFile), true);
                     HSLFSlideShow ppt = new HSLFSlideShow(fs)) {
                    assertTrue(ppt.getSlides().size() > 0);
                } catch (EncryptedPowerPointFileException e) {
                    fail(pptFile + " can't be decrypted");
                }
            }
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(null);
        }
    }

    @Test
    public void cryptoAPIChangeKeySize() throws Exception {
        String pptFile = "cryptoapi-proc2356.ppt";
        Biff8EncryptionKey.setCurrentUserPassword("crypto");
        try (POIFSFileSystem fs = new POIFSFileSystem(slTests.getFile(pptFile), true);
             HSLFSlideShowImpl hss = new HSLFSlideShowImpl(fs)) {
            // need to cache data (i.e. read all data) before changing the key size
            List<HSLFPictureData> picsExpected = hss.getPictureData();
            hss.getDocumentSummaryInformation();
            DocumentEncryptionAtom documentEncryptionAtom = hss.getDocumentEncryptionAtom();
            assertNotNull(documentEncryptionAtom);
            EncryptionInfo ei = documentEncryptionAtom.getEncryptionInfo();
            ((CryptoAPIEncryptionHeader) ei.getHeader()).setKeySize(0x78);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            hss.write(bos);

            try (POIFSFileSystem fs2 = new POIFSFileSystem(new ByteArrayInputStream(bos.toByteArray()));
                 HSLFSlideShowImpl hss2 = new HSLFSlideShowImpl(fs2)) {
                List<HSLFPictureData> picsActual = hss2.getPictureData();

                assertEquals(picsExpected.size(), picsActual.size());
                for (int i = 0; i < picsExpected.size(); i++) {
                    assertArrayEquals(picsExpected.get(i).getRawData(), picsActual.get(i).getRawData());
                }
            }
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(null);
        }
    }

    @Test
    public void cryptoAPIEncryption() throws Exception {
        /* documents with multiple edits need to be normalized for encryption */
        String pptFile = "57272_corrupted_usereditatom.ppt";
        ByteArrayOutputStream encrypted = new ByteArrayOutputStream();
        ByteArrayOutputStream expected = new ByteArrayOutputStream();
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        try {
            try (POIFSFileSystem fs = new POIFSFileSystem(slTests.getFile(pptFile), true);
                 HSLFSlideShowImpl hss = new HSLFSlideShowImpl(fs)) {
                hss.normalizeRecords();

                // normalized ppt
                hss.write(expected);

                // encrypted
                Biff8EncryptionKey.setCurrentUserPassword("hello");
                hss.write(encrypted);
            }

            // decrypted
            ByteArrayInputStream bis = new ByteArrayInputStream(encrypted.toByteArray());
            try (POIFSFileSystem fs = new POIFSFileSystem(bis);
                 HSLFSlideShowImpl hss = new HSLFSlideShowImpl(fs)) {
                Biff8EncryptionKey.setCurrentUserPassword(null);
                hss.write(actual);
            }
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(null);
        }
        
        assertArrayEquals(expected.toByteArray(), actual.toByteArray());
    }    
    
    @Test
    public void cryptoAPIDecryption() throws Exception {
        // taken from a msdn blog:
        // http://blogs.msdn.com/b/openspecification/archive/2009/05/08/dominic-salemno.aspx
        Biff8EncryptionKey.setCurrentUserPassword("crypto");
        try (POIFSFileSystem fs = new POIFSFileSystem(slTests.getFile("cryptoapi-proc2356.ppt"));
             HSLFSlideShow ss = new HSLFSlideShow(fs)) {

            HSLFSlide slide = ss.getSlides().get(0);
            String rawText = HSLFTextParagraph.getRawText(slide.getTextParagraphs().get(0));
            assertEquals("Dominic Salemno", rawText);

            String picCmp[][] = {
                    {"0", "nKsDTKqxTCR8LFkVVWlP9GSTvZ0="},
                    {"95163", "SuNOR+9V1UVYZIoeD65l3VTaLoc="},
                    {"100864", "Ql3IGrr4bNq07ZTp5iPg7b+pva8="},
                    {"714114", "8pdst9NjBGSfWezSZE8+aVhIRe0="},
                    {"723752", "go6xqW7lvkCtlOO5tYLiMfb4oxw="},
                    {"770128", "gZUM8YqRNL5kGNfyyYvEEernvCc="},
                    {"957958", "CNU2iiqUFAnk3TDXsXV1ihH9eRM="},
            };

            MessageDigest md = CryptoFunctions.getMessageDigest(HashAlgorithm.sha1);
            List<HSLFPictureData> pd = ss.getSlideShowImpl().getPictureData();
            int i = 0;
            for (HSLFPictureData p : pd) {
                byte hash[] = md.digest(p.getData());
                assertEquals(Integer.parseInt(picCmp[i][0]), p.getOffset());
                assertEquals(picCmp[i][1], Base64.encodeBase64String(hash));
                i++;
            }

            DocumentEncryptionAtom dea = ss.getSlideShowImpl().getDocumentEncryptionAtom();
            assertNotNull(dea);

            CryptoAPIDecryptor dec = (CryptoAPIDecryptor) dea.getEncryptionInfo().getDecryptor();
            try (POIFSFileSystem fs2 = dec.getSummaryEntries(fs.getRoot(), "EncryptedSummary")) {
                PropertySet ps = PropertySetFactory.create(fs2.getRoot(), SummaryInformation.DEFAULT_STREAM_NAME);
                assertNotNull(ps);
                assertTrue(ps.isSummaryInformation());
                assertEquals("RC4 CryptoAPI Encryption", ps.getProperties()[1].getValue());
                ps = PropertySetFactory.create(fs2.getRoot(), DocumentSummaryInformation.DEFAULT_STREAM_NAME);
                assertNotNull(ps);
                assertTrue(ps.isDocumentSummaryInformation());
                assertEquals("On-screen Show (4:3)", ps.getProperties()[1].getValue());
            }
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(null);
        }
    }
}
