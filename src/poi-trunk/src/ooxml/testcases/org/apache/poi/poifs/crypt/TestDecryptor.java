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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.Cipher;

import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Assume;
import org.junit.Test;

public class TestDecryptor {
    @Test
    public void passwordVerification() throws IOException, GeneralSecurityException {
        POIFSFileSystem fs = new POIFSFileSystem(POIDataSamples.getPOIFSInstance().openResourceAsStream("protect.xlsx"));

        EncryptionInfo info = new EncryptionInfo(fs);

        Decryptor d = Decryptor.getInstance(info);

        assertTrue(d.verifyPassword(Decryptor.DEFAULT_PASSWORD));

        fs.close();
    }

    @Test
    public void decrypt() throws IOException, GeneralSecurityException {
        POIFSFileSystem fs = new POIFSFileSystem(POIDataSamples.getPOIFSInstance().openResourceAsStream("protect.xlsx"));

        EncryptionInfo info = new EncryptionInfo(fs);

        Decryptor d = Decryptor.getInstance(info);

        d.verifyPassword(Decryptor.DEFAULT_PASSWORD);

        zipOk(fs.getRoot(), d);

        fs.close();
    }

    @Test
    public void agile() throws IOException, GeneralSecurityException {
        POIFSFileSystem fs = new POIFSFileSystem(POIDataSamples.getPOIFSInstance().openResourceAsStream("protected_agile.docx"));

        EncryptionInfo info = new EncryptionInfo(fs);

        assertTrue(info.getVersionMajor() == 4 && info.getVersionMinor() == 4);

        Decryptor d = Decryptor.getInstance(info);

        assertTrue(d.verifyPassword(Decryptor.DEFAULT_PASSWORD));

        zipOk(fs.getRoot(), d);

        fs.close();
    }

    private void zipOk(DirectoryNode root, Decryptor d) throws IOException, GeneralSecurityException {
        ZipInputStream zin = new ZipInputStream(d.getDataStream(root));

        while (true) {
            ZipEntry entry = zin.getNextEntry();
            if (entry==null) {
                break;
            }
            // crc32 is checked within zip-stream
            if (entry.isDirectory()) {
                continue;
            }
            zin.skip(entry.getSize());
            byte buf[] = new byte[10];
            int readBytes = zin.read(buf);
            // zin.available() doesn't work for entries
            assertEquals("size failed for "+entry.getName(), -1, readBytes);
        }
        
        zin.close();
    }

    @Test
    public void dataLength() throws Exception {
        POIFSFileSystem fs = new POIFSFileSystem(POIDataSamples.getPOIFSInstance().openResourceAsStream("protected_agile.docx"));

        EncryptionInfo info = new EncryptionInfo(fs);

        Decryptor d = Decryptor.getInstance(info);

        d.verifyPassword(Decryptor.DEFAULT_PASSWORD);

        InputStream is = d.getDataStream(fs);

        long len = d.getLength();
        assertEquals(12810, len);

        byte[] buf = new byte[(int)len];

        is.read(buf);

        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(buf));

        while (true) {
            ZipEntry entry = zin.getNextEntry();
            if (entry==null) {
                break;
            }

            while (zin.available()>0) {
                zin.skip(zin.available());
            }
        }
    }

    @Test
    public void bug57080() throws Exception {
        // the test file contains a wrong ole entry size, produced by extenxls
        // the fix limits the available size and tries to read all entries 
        File f = POIDataSamples.getPOIFSInstance().getFile("extenxls_pwd123.xlsx");
        NPOIFSFileSystem fs = new NPOIFSFileSystem(f, true);
        EncryptionInfo info = new EncryptionInfo(fs);
        Decryptor d = Decryptor.getInstance(info);
        d.verifyPassword("pwd123");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipInputStream zis = new ZipInputStream(d.getDataStream(fs));
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            bos.reset();
            IOUtils.copy(zis, bos);
            assertEquals(ze.getSize(), bos.size());
        }
        
        zis.close();
        fs.close();
    }

    @Test
    public void test58616() throws IOException, GeneralSecurityException {
        FileInputStream fis = new FileInputStream(XSSFTestDataSamples.getSampleFile("58616.xlsx"));                
        POIFSFileSystem pfs = new POIFSFileSystem(fis);                
        EncryptionInfo info = new EncryptionInfo(pfs);             
        Decryptor dec = Decryptor.getInstance(info);   
        //dec.verifyPassword(null);
        dec.getDataStream(pfs);
        pfs.close();
        fis.close();
    }

    @Test
    public void bug60320() throws IOException, GeneralSecurityException {
        int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
        Assume.assumeTrue("Please install JCE Unlimited Strength Jurisdiction Policy files for AES 256", maxKeyLen == 2147483647);

        InputStream is = POIDataSamples.getPOIFSInstance().openResourceAsStream("60320-protected.xlsx");
        POIFSFileSystem fs = new POIFSFileSystem(is);
        is.close();

        EncryptionInfo info = new EncryptionInfo(fs);

        Decryptor d = Decryptor.getInstance(info);

        boolean b = d.verifyPassword("Test001!!");
        assertTrue(b);

        zipOk(fs.getRoot(), d);
        
        fs.close();
    }    
}
