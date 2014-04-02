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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.crypto.Cipher;

import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.crypt.agile.AgileEncryptionHeader;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.BoundedInputStream;
import org.apache.poi.util.IOUtils;
import org.junit.Assume;
import org.junit.Test;

public class TestEncryptor {
    @Test
    public void testAgileEncryption() throws Exception {
        int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
        Assume.assumeTrue("Please install JCE Unlimited Strength Jurisdiction Policy files for AES 256", maxKeyLen == 2147483647);

        File file = POIDataSamples.getDocumentInstance().getFile("bug53475-password-is-pass.docx");
        String pass = "pass";
        NPOIFSFileSystem nfs = new NPOIFSFileSystem(file);

        // Check the encryption details
        EncryptionInfo infoExpected = new EncryptionInfo(nfs);
        Decryptor decExpected = Decryptor.getInstance(infoExpected);
        boolean passed = decExpected.verifyPassword(pass);
        assertTrue("Unable to process: document is encrypted", passed);
        
        // extract the payload
        InputStream is = decExpected.getDataStream(nfs);
        byte payloadExpected[] = IOUtils.toByteArray(is);
        is.close();

        long decPackLenExpected = decExpected.getLength();
        assertEquals(decPackLenExpected, payloadExpected.length);

        is = nfs.getRoot().createDocumentInputStream("EncryptedPackage");
        is = new BoundedInputStream(is, is.available()-16); // ignore padding block
        byte encPackExpected[] = IOUtils.toByteArray(is);
        is.close();
        
        // listDir(nfs.getRoot(), "orig", "");
        
        nfs.close();

        // check that same verifier/salt lead to same hashes
        byte verifierSaltExpected[] = infoExpected.getVerifier().getSalt();
        byte verifierExpected[] = decExpected.getVerifier();
        byte keySalt[] = infoExpected.getHeader().getKeySalt();
        byte keySpec[] = decExpected.getSecretKey().getEncoded();
        byte integritySalt[] = decExpected.getIntegrityHmacKey();
        // the hmacs of the file always differ, as we use PKCS5-padding to pad the bytes
        // whereas office just uses random bytes
        // byte integrityHash[] = d.getIntegrityHmacValue();
        
        POIFSFileSystem fs = new POIFSFileSystem();
        EncryptionInfo infoActual = new EncryptionInfo(
              fs, EncryptionMode.agile
            , infoExpected.getVerifier().getCipherAlgorithm()
            , infoExpected.getVerifier().getHashAlgorithm()
            , infoExpected.getHeader().getKeySize()
            , infoExpected.getHeader().getBlockSize()
            , infoExpected.getVerifier().getChainingMode()
        );        

        Encryptor e = Encryptor.getInstance(infoActual);
        e.confirmPassword(pass, keySpec, keySalt, verifierExpected, verifierSaltExpected, integritySalt);
    
        OutputStream os = e.getDataStream(fs);
        IOUtils.copy(new ByteArrayInputStream(payloadExpected), os);
        os.close();

        ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
        fs.writeFilesystem(bos);
        
        nfs = new NPOIFSFileSystem(new ByteArrayInputStream(bos.toByteArray()));
        infoActual = new EncryptionInfo(nfs.getRoot());
        Decryptor decActual = Decryptor.getInstance(infoActual);
        passed = decActual.verifyPassword(pass);        
        assertTrue("Unable to process: document is encrypted", passed);
        
        // extract the payload
        is = decActual.getDataStream(nfs);
        byte payloadActual[] = IOUtils.toByteArray(is);
        is.close();
        
        long decPackLenActual = decActual.getLength();
        
        is = nfs.getRoot().createDocumentInputStream("EncryptedPackage");
        is = new BoundedInputStream(is, is.available()-16); // ignore padding block
        byte encPackActual[] = IOUtils.toByteArray(is);
        is.close();
        
        // listDir(nfs.getRoot(), "copy", "");
        
        nfs.close();
        
        AgileEncryptionHeader aehExpected = (AgileEncryptionHeader)infoExpected.getHeader();
        AgileEncryptionHeader aehActual = (AgileEncryptionHeader)infoActual.getHeader();
        assertThat(aehExpected.getEncryptedHmacKey(), equalTo(aehActual.getEncryptedHmacKey()));
        assertEquals(decPackLenExpected, decPackLenActual);
        assertThat(payloadExpected, equalTo(payloadActual));
        assertThat(encPackExpected, equalTo(encPackActual));
    }
    
    @Test
    public void testStandardEncryption() throws Exception {
        File file = POIDataSamples.getDocumentInstance().getFile("bug53475-password-is-solrcell.docx");
        String pass = "solrcell";
        
        NPOIFSFileSystem nfs = new NPOIFSFileSystem(file);

        // Check the encryption details
        EncryptionInfo infoExpected = new EncryptionInfo(nfs);
        Decryptor d = Decryptor.getInstance(infoExpected);
        boolean passed = d.verifyPassword(pass);
        assertTrue("Unable to process: document is encrypted", passed);

        // extract the payload
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream is = d.getDataStream(nfs);
        IOUtils.copy(is, bos);
        is.close();
        nfs.close();
        byte payloadExpected[] = bos.toByteArray();
        
        // check that same verifier/salt lead to same hashes
        byte verifierSaltExpected[] = infoExpected.getVerifier().getSalt();
        byte verifierExpected[] = d.getVerifier();
        byte keySpec[] = d.getSecretKey().getEncoded();
        byte keySalt[] = infoExpected.getHeader().getKeySalt();
        
        
        POIFSFileSystem fs = new POIFSFileSystem();
        EncryptionInfo infoActual = new EncryptionInfo(
              fs, EncryptionMode.standard
            , infoExpected.getVerifier().getCipherAlgorithm()
            , infoExpected.getVerifier().getHashAlgorithm()
            , infoExpected.getHeader().getKeySize()
            , infoExpected.getHeader().getBlockSize()
            , infoExpected.getVerifier().getChainingMode()
        );
        
        Encryptor e = Encryptor.getInstance(infoActual);
        e.confirmPassword(pass, keySpec, keySalt, verifierExpected, verifierSaltExpected, null);
        
        assertThat(infoExpected.getVerifier().getEncryptedVerifier(), equalTo(infoActual.getVerifier().getEncryptedVerifier()));
        assertThat(infoExpected.getVerifier().getEncryptedVerifierHash(), equalTo(infoActual.getVerifier().getEncryptedVerifierHash()));

        // now we use a newly generated salt/verifier and check
        // if the file content is still the same 

        fs = new POIFSFileSystem();
        infoActual = new EncryptionInfo(
              fs, EncryptionMode.standard
            , infoExpected.getVerifier().getCipherAlgorithm()
            , infoExpected.getVerifier().getHashAlgorithm()
            , infoExpected.getHeader().getKeySize()
            , infoExpected.getHeader().getBlockSize()
            , infoExpected.getVerifier().getChainingMode()
        );
        
        e = Encryptor.getInstance(infoActual);
        e.confirmPassword(pass);
        
        OutputStream os = e.getDataStream(fs);
        IOUtils.copy(new ByteArrayInputStream(payloadExpected), os);
        os.close();
        
        bos.reset();
        fs.writeFilesystem(bos);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        
        // FileOutputStream fos = new FileOutputStream("encrypted.docx");
        // IOUtils.copy(bis, fos);
        // fos.close();
        // bis.reset();
        
        nfs = new NPOIFSFileSystem(bis);
        infoExpected = new EncryptionInfo(nfs);
        d = Decryptor.getInstance(infoExpected);
        passed = d.verifyPassword(pass);
        assertTrue("Unable to process: document is encrypted", passed);

        bos.reset();
        is = d.getDataStream(nfs);
        IOUtils.copy(is, bos);
        is.close();
        nfs.close();
        byte payloadActual[] = bos.toByteArray();        
        
        assertThat(payloadExpected, equalTo(payloadActual));
    }
    
    
    private void listEntry(DocumentNode de, String ext, String path) throws IOException {
        path += "\\" + de.getName().replace('\u0006', '_');
        System.out.println(ext+": "+path+" ("+de.getSize()+" bytes)");
        
        String name = de.getName().replace('\u0006', '_');
        
        InputStream is = ((DirectoryNode)de.getParent()).createDocumentInputStream(de);
        FileOutputStream fos = new FileOutputStream("solr."+name+"."+ext);
        IOUtils.copy(is, fos);
        fos.close();
        is.close();
    }
    
    @SuppressWarnings("unused")
    private void listDir(DirectoryNode dn, String ext, String path) throws IOException {
        path += "\\" + dn.getName().replace('\u0006', '_');
        System.out.println(ext+": "+path+" ("+dn.getStorageClsid()+")");
        
        Iterator<Entry> iter = dn.getEntries();
        while (iter.hasNext()) {
            Entry ent = iter.next();
            if (ent instanceof DirectoryNode) {
                listDir((DirectoryNode)ent, ext, path);
            } else {
                listEntry((DocumentNode)ent, ext, path);
            }
        }
    }
}
