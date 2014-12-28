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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Base64;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.exceptions.EncryptedPowerPointFileException;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.usermodel.PictureData;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.crypt.cryptoapi.CryptoAPIEncryptionHeader;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that DocumentEncryption works properly.
 */
public class TestDocumentEncryption {
    POIDataSamples slTests = POIDataSamples.getSlideShowInstance();

    @Before
    public void resetPassword() {
        Biff8EncryptionKey.setCurrentUserPassword(null);
    }
    
    @Test
    public void cryptoAPIDecryptionOther() throws Exception {
        Biff8EncryptionKey.setCurrentUserPassword("hello");
        String encPpts[] = {
            "Password_Protected-56-hello.ppt",
            "Password_Protected-hello.ppt",
            "Password_Protected-np-hello.ppt",
        };
        
        for (String pptFile : encPpts) {
            try {
                NPOIFSFileSystem fs = new NPOIFSFileSystem(slTests.getFile(pptFile), true);
                HSLFSlideShow hss = new HSLFSlideShow(fs);
                new SlideShow(hss);
                fs.close();
            } catch (EncryptedPowerPointFileException e) {
                fail(pptFile+" can't be decrypted");
            }
        }
    }

    @Test
    public void cryptoAPIChangeKeySize() throws Exception {
        String pptFile = "cryptoapi-proc2356.ppt";
        Biff8EncryptionKey.setCurrentUserPassword("crypto");
        NPOIFSFileSystem fs = new NPOIFSFileSystem(slTests.getFile(pptFile), true);
        HSLFSlideShow hss = new HSLFSlideShow(fs);
        // need to cache data (i.e. read all data) before changing the key size
        PictureData picsExpected[] = hss.getPictures();
        hss.getDocumentSummaryInformation();
        EncryptionInfo ei = hss.getDocumentEncryptionAtom().getEncryptionInfo();
        ((CryptoAPIEncryptionHeader)ei.getHeader()).setKeySize(0x78);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        hss.write(bos);
        fs.close();
        
        fs = new NPOIFSFileSystem(new ByteArrayInputStream(bos.toByteArray()));
        hss = new HSLFSlideShow(fs);
        PictureData picsActual[] = hss.getPictures();
        fs.close();
        
        assertEquals(picsExpected.length, picsActual.length);
        for (int i=0; i<picsExpected.length; i++) {
            assertArrayEquals(picsExpected[i].getRawData(), picsActual[i].getRawData());
        }
    }

    @Test
    public void cryptoAPIEncryption() throws Exception {
        /* documents with multiple edits need to be normalized for encryption */
        String pptFile = "57272_corrupted_usereditatom.ppt";
        NPOIFSFileSystem fs = new NPOIFSFileSystem(slTests.getFile(pptFile), true);
        HSLFSlideShow hss = new HSLFSlideShow(fs);
        hss.normalizeRecords();
        
        // normalized ppt
        ByteArrayOutputStream expected = new ByteArrayOutputStream();
        hss.write(expected);
        
        // encrypted
        Biff8EncryptionKey.setCurrentUserPassword("hello");
        ByteArrayOutputStream encrypted = new ByteArrayOutputStream();
        hss.write(encrypted);
        fs.close();

        // decrypted
        ByteArrayInputStream bis = new ByteArrayInputStream(encrypted.toByteArray());
        fs = new NPOIFSFileSystem(bis);
        hss = new HSLFSlideShow(fs);
        Biff8EncryptionKey.setCurrentUserPassword(null);
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        hss.write(actual);
        fs.close();
        
        assertArrayEquals(expected.toByteArray(), actual.toByteArray());
    }    
    
    @Test
    public void cryptoAPIDecryption() throws Exception {
        // taken from a msdn blog:
        // http://blogs.msdn.com/b/openspecification/archive/2009/05/08/dominic-salemno.aspx
        Biff8EncryptionKey.setCurrentUserPassword("crypto");
        NPOIFSFileSystem fs = new NPOIFSFileSystem(slTests.getFile("cryptoapi-proc2356.ppt"));
        HSLFSlideShow hss = new HSLFSlideShow(fs);
        SlideShow ss = new SlideShow(hss);
        
        Slide slide = ss.getSlides()[0];
        assertEquals("Dominic Salemno", slide.getTextRuns()[0].getText());

        String picCmp[][] = {
            {"0","nKsDTKqxTCR8LFkVVWlP9GSTvZ0="},
            {"95163","SuNOR+9V1UVYZIoeD65l3VTaLoc="},
            {"100864","Ql3IGrr4bNq07ZTp5iPg7b+pva8="},
            {"714114","8pdst9NjBGSfWezSZE8+aVhIRe0="},
            {"723752","go6xqW7lvkCtlOO5tYLiMfb4oxw="},
            {"770128","gZUM8YqRNL5kGNfyyYvEEernvCc="},
            {"957958","CNU2iiqUFAnk3TDXsXV1ihH9eRM="},                
        };
        
        MessageDigest md = CryptoFunctions.getMessageDigest(HashAlgorithm.sha1);
        PictureData pd[] = hss.getPictures();
        int i = 0;
        for (PictureData p : pd) {
            byte hash[] = md.digest(p.getData());
            assertEquals(Integer.parseInt(picCmp[i][0]), p.getOffset());
            assertEquals(picCmp[i][1], Base64.encodeBase64String(hash));
            i++;
        }
        
        DocumentEncryptionAtom dea = hss.getDocumentEncryptionAtom();
        
        POIFSFileSystem fs2 = new POIFSFileSystem(dea.getEncryptionInfo().getDecryptor().getDataStream(fs));
        PropertySet ps = PropertySetFactory.create(fs2.getRoot(), SummaryInformation.DEFAULT_STREAM_NAME);
        assertTrue(ps.isSummaryInformation());
        assertEquals("RC4 CryptoAPI Encryption", ps.getProperties()[1].getValue());
        ps = PropertySetFactory.create(fs2.getRoot(), DocumentSummaryInformation.DEFAULT_STREAM_NAME);
        assertTrue(ps.isDocumentSummaryInformation());
        assertEquals("On-screen Show (4:3)", ps.getProperties()[1].getValue());
        fs.close();
    }
}
