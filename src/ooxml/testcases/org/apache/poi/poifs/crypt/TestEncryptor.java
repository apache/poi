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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.agile.AgileDecryptor;
import org.apache.poi.poifs.crypt.agile.AgileEncryptionHeader;
import org.apache.poi.poifs.crypt.agile.AgileEncryptionVerifier;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.BoundedInputStream;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

public class TestEncryptor {
    @Test
    public void binaryRC4Encryption() throws Exception {
        // please contribute a real sample file, which is binary rc4 encrypted
        // ... at least the output can be opened in Excel Viewer 
        String password = "pass";

        InputStream is = POIDataSamples.getSpreadSheetInstance().openResourceAsStream("SimpleMultiCell.xlsx");
        ByteArrayOutputStream payloadExpected = new ByteArrayOutputStream();
        IOUtils.copy(is, payloadExpected);
        is.close();
        
        POIFSFileSystem fs = new POIFSFileSystem();
        EncryptionInfo ei = new EncryptionInfo(EncryptionMode.binaryRC4);
        Encryptor enc = ei.getEncryptor();
        enc.confirmPassword(password);
        
        OutputStream os = enc.getDataStream(fs.getRoot());
        payloadExpected.writeTo(os);
        os.close();
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        fs.writeFilesystem(bos);
        
        fs = new POIFSFileSystem(new ByteArrayInputStream(bos.toByteArray()));
        ei = new EncryptionInfo(fs);
        Decryptor dec = ei.getDecryptor();
        boolean b = dec.verifyPassword(password);
        assertTrue(b);
        
        ByteArrayOutputStream payloadActual = new ByteArrayOutputStream();
        is = dec.getDataStream(fs.getRoot());
        IOUtils.copy(is,payloadActual);
        is.close();
        
        assertArrayEquals(payloadExpected.toByteArray(), payloadActual.toByteArray());
    }

    @Test
    public void agileEncryption() throws Exception {
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

        is = nfs.getRoot().createDocumentInputStream(Decryptor.DEFAULT_POIFS_ENTRY);
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
              EncryptionMode.agile
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
        fs.close();

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
        
        is = nfs.getRoot().createDocumentInputStream(Decryptor.DEFAULT_POIFS_ENTRY);
        is = new BoundedInputStream(is, is.available()-16); // ignore padding block
        byte encPackActual[] = IOUtils.toByteArray(is);
        is.close();
        
        // listDir(nfs.getRoot(), "copy", "");
        
        nfs.close();
        
        AgileEncryptionHeader aehExpected = (AgileEncryptionHeader)infoExpected.getHeader();
        AgileEncryptionHeader aehActual = (AgileEncryptionHeader)infoActual.getHeader();
        assertArrayEquals(aehExpected.getEncryptedHmacKey(), aehActual.getEncryptedHmacKey());
        assertEquals(decPackLenExpected, decPackLenActual);
        assertArrayEquals(payloadExpected, payloadActual);
        assertArrayEquals(encPackExpected, encPackActual);
    }
    
    @Test
    public void standardEncryption() throws Exception {
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
        
        
        EncryptionInfo infoActual = new EncryptionInfo(
              EncryptionMode.standard
            , infoExpected.getVerifier().getCipherAlgorithm()
            , infoExpected.getVerifier().getHashAlgorithm()
            , infoExpected.getHeader().getKeySize()
            , infoExpected.getHeader().getBlockSize()
            , infoExpected.getVerifier().getChainingMode()
        );
        
        Encryptor e = Encryptor.getInstance(infoActual);
        e.confirmPassword(pass, keySpec, keySalt, verifierExpected, verifierSaltExpected, null);
        
        assertArrayEquals(infoExpected.getVerifier().getEncryptedVerifier(), infoActual.getVerifier().getEncryptedVerifier());
        assertArrayEquals(infoExpected.getVerifier().getEncryptedVerifierHash(), infoActual.getVerifier().getEncryptedVerifierHash());

        // now we use a newly generated salt/verifier and check
        // if the file content is still the same 

        infoActual = new EncryptionInfo(
              EncryptionMode.standard
            , infoExpected.getVerifier().getCipherAlgorithm()
            , infoExpected.getVerifier().getHashAlgorithm()
            , infoExpected.getHeader().getKeySize()
            , infoExpected.getHeader().getBlockSize()
            , infoExpected.getVerifier().getChainingMode()
        );
        
        e = Encryptor.getInstance(infoActual);
        e.confirmPassword(pass);

        POIFSFileSystem fs = new POIFSFileSystem();
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
        
        assertArrayEquals(payloadExpected, payloadActual);
    }
    
    /**
     * Ensure we can encrypt a package that is missing the Core
     *  Properties, eg one from dodgy versions of Jasper Reports 
     * See https://github.com/nestoru/xlsxenc/ and
     * http://stackoverflow.com/questions/28593223
     */
    @Test
    public void encryptPackageWithoutCoreProperties() throws Exception {
        // Open our file without core properties
        File inp = POIDataSamples.getOpenXML4JInstance().getFile("OPCCompliance_NoCoreProperties.xlsx");
        OPCPackage pkg = OPCPackage.open(inp.getPath());
        
        // It doesn't have any core properties yet
        assertEquals(0, pkg.getPartsByContentType(ContentTypes.CORE_PROPERTIES_PART).size());
        assertNotNull(pkg.getPackageProperties());
        assertNotNull(pkg.getPackageProperties().getLanguageProperty());
        assertNull(pkg.getPackageProperties().getLanguageProperty().getValue());
        
        // Encrypt it
        EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
        NPOIFSFileSystem fs = new NPOIFSFileSystem();
        
        Encryptor enc = info.getEncryptor();
        enc.confirmPassword("password");
        OutputStream os = enc.getDataStream(fs);
        pkg.save(os);
        os.close();
        pkg.revert();
        
        // Save the resulting OLE2 document, and re-open it
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fs.writeFilesystem(baos);
        fs.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        NPOIFSFileSystem inpFS = new NPOIFSFileSystem(bais);
        
        // Check we can decrypt it
        info = new EncryptionInfo(inpFS);
        Decryptor d = Decryptor.getInstance(info);
        assertEquals(true, d.verifyPassword("password"));
        
        OPCPackage inpPkg = OPCPackage.open(d.getDataStream(inpFS));
        
        // Check it now has empty core properties
        assertEquals(1, inpPkg.getPartsByContentType(ContentTypes.CORE_PROPERTIES_PART).size());
        assertNotNull(inpPkg.getPackageProperties());
        assertNotNull(inpPkg.getPackageProperties().getLanguageProperty());
        assertNull(inpPkg.getPackageProperties().getLanguageProperty().getValue());

        inpPkg.close();
        inpFS.close();
    }
    
    @Test
    @Ignore
    public void inPlaceRewrite() throws Exception {
        File f = TempFile.createTempFile("protected_agile", ".docx");
        // File f = new File("protected_agile.docx");
        FileOutputStream fos = new FileOutputStream(f);
        InputStream fis = POIDataSamples.getPOIFSInstance().openResourceAsStream("protected_agile.docx");
        IOUtils.copy(fis, fos);
        fis.close();
        fos.close();
        
        NPOIFSFileSystem fs = new NPOIFSFileSystem(f, false);

        // decrypt the protected file - in this case it was encrypted with the default password
        EncryptionInfo encInfo = new EncryptionInfo(fs);
        Decryptor d = encInfo.getDecryptor();
        boolean b = d.verifyPassword(Decryptor.DEFAULT_PASSWORD);
        assertTrue(b);

        // do some strange things with it ;)
        InputStream docIS = d.getDataStream(fs);
        XWPFDocument docx = new XWPFDocument(docIS);
        docx.getParagraphArray(0).insertNewRun(0).setText("POI was here! All your base are belong to us!");
        docx.getParagraphArray(0).insertNewRun(1).addBreak();

        // and encrypt it again
        Encryptor e = encInfo.getEncryptor();
        e.confirmPassword("AYBABTU");
        docx.write(e.getDataStream(fs));
        docx.close();
        docIS.close();
        
        docx.close();
        fs.close();
    }
    
    
    private void listEntry(DocumentNode de, String ext, String path) throws IOException {
        path += "\\" + de.getName().replaceAll("[\\p{Cntrl}]", "_");
        System.out.println(ext+": "+path+" ("+de.getSize()+" bytes)");
        
        String name = de.getName().replaceAll("[\\p{Cntrl}]", "_");
        
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

    /*
     * this test simulates the generation of bugs 60320 sample file
     * as the padding bytes of the EncryptedPackage stream are random or in POIs case PKCS5-padded
     * one would need to mock those bytes to get the same hmacValues - see diff below
     *
     * this use-case is experimental - for the time being the setters of the encryption classes
     * are spreaded between two packages and are protected - so you would need to violate
     * the packages rules and provide a helper class in the *poifs.crypt package-namespace.
     * the default way of defining the encryption settings is via the EncryptionInfo class
     */
    @Test
    public void bug60320CustomEncrypt() throws Exception {
        int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
        Assume.assumeTrue("Please install JCE Unlimited Strength Jurisdiction Policy files for AES 256", maxKeyLen == 2147483647);

        // --- src/java/org/apache/poi/poifs/crypt/ChunkedCipherOutputStream.java  (revision 1766745)
        // +++ src/java/org/apache/poi/poifs/crypt/ChunkedCipherOutputStream.java  (working copy)
        // @@ -208,6 +208,13 @@
        //      protected int invokeCipher(int posInChunk, boolean doFinal) throws GeneralSecurityException {
        //          byte plain[] = (_plainByteFlags.isEmpty()) ? null : _chunk.clone();
        //  
        // +        if (posInChunk < 4096) {
        // +            _cipher.update(_chunk, 0, posInChunk, _chunk);
        // +            byte bla[] = { (byte)0x7A,(byte)0x0F,(byte)0x27,(byte)0xF0,(byte)0x17,(byte)0x6E,(byte)0x77,(byte)0x05,(byte)0xB9,(byte)0xDA,(byte)0x49,(byte)0xF9,(byte)0xD7,(byte)0x8E,(byte)0x03,(byte)0x1D };
        // +            System.arraycopy(bla, 0, _chunk, posInChunk-2, bla.length);
        // +            return posInChunk-2+bla.length;
        // +        }
        // +        
        //          int ciLen = (doFinal)
        //              ? _cipher.doFinal(_chunk, 0, posInChunk, _chunk)
        //              : _cipher.update(_chunk, 0, posInChunk, _chunk);
        //
        //      --- src/ooxml/java/org/apache/poi/poifs/crypt/agile/AgileDecryptor.java (revision 1766745)
        //      +++ src/ooxml/java/org/apache/poi/poifs/crypt/agile/AgileDecryptor.java (working copy)
        //      
        //      @@ -300,7 +297,7 @@
        //      protected static Cipher initCipherForBlock(Cipher existing, int block, boolean lastChunk, EncryptionInfo encryptionInfo, SecretKey skey, int encryptionMode)
        //      throws GeneralSecurityException {
        //          EncryptionHeader header = encryptionInfo.getHeader();
        // -        String padding = (lastChunk ? "PKCS5Padding" : "NoPadding");
        // +        String padding = "NoPadding"; // (lastChunk ? "PKCS5Padding" : "NoPadding");
        //          if (existing == null || !existing.getAlgorithm().endsWith(padding)) {
        //              existing = getCipher(skey, header.getCipherAlgorithm(), header.getChainingMode(), header.getKeySalt(), encryptionMode, padding);
        //          }

        InputStream is = POIDataSamples.getPOIFSInstance().openResourceAsStream("60320-protected.xlsx");
        POIFSFileSystem fsOrig = new POIFSFileSystem(is);
        is.close();
        EncryptionInfo infoOrig = new EncryptionInfo(fsOrig);
        Decryptor decOrig = infoOrig.getDecryptor();
        boolean b = decOrig.verifyPassword("Test001!!");
        assertTrue(b);
        InputStream decIn = decOrig.getDataStream(fsOrig);
        byte[] zipInput = IOUtils.toByteArray(decIn);
        decIn.close();

        InputStream epOrig = fsOrig.getRoot().createDocumentInputStream("EncryptedPackage");
        // ignore the 16 padding bytes
        byte[] epOrigBytes = IOUtils.toByteArray(epOrig, 9400);
        epOrig.close();
        
        EncryptionInfo eiNew = new EncryptionInfo(EncryptionMode.agile);
        AgileEncryptionHeader aehHeader = (AgileEncryptionHeader)eiNew.getHeader();
        aehHeader.setCipherAlgorithm(CipherAlgorithm.aes128);
        aehHeader.setHashAlgorithm(HashAlgorithm.sha1);
        AgileEncryptionVerifier aehVerifier = (AgileEncryptionVerifier)eiNew.getVerifier();
        
        // this cast might look strange - if the setters would be public, it will become obsolete
        // see http://stackoverflow.com/questions/5637650/overriding-protected-methods-in-java
        ((EncryptionVerifier)aehVerifier).setCipherAlgorithm(CipherAlgorithm.aes256);
        aehVerifier.setHashAlgorithm(HashAlgorithm.sha512);
        
        Encryptor enc = eiNew.getEncryptor();
        enc.confirmPassword("Test001!!",
            infoOrig.getDecryptor().getSecretKey().getEncoded(),
            infoOrig.getHeader().getKeySalt(),
            infoOrig.getDecryptor().getVerifier(),
            infoOrig.getVerifier().getSalt(),
            infoOrig.getDecryptor().getIntegrityHmacKey()
        );
        NPOIFSFileSystem fsNew = new NPOIFSFileSystem();
        OutputStream os = enc.getDataStream(fsNew);
        os.write(zipInput);
        os.close();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        fsNew.writeFilesystem(bos);
        fsNew.close();
        
        NPOIFSFileSystem fsReload = new NPOIFSFileSystem(new ByteArrayInputStream(bos.toByteArray()));
        InputStream epReload = fsReload.getRoot().createDocumentInputStream("EncryptedPackage");
        byte[] epNewBytes = IOUtils.toByteArray(epReload, 9400);
        epReload.close();
        
        assertArrayEquals(epOrigBytes, epNewBytes);
        
        EncryptionInfo infoReload = new EncryptionInfo(fsOrig);
        Decryptor decReload = infoReload.getDecryptor();
        b = decReload.verifyPassword("Test001!!");
        assertTrue(b);
        
        AgileEncryptionHeader aehOrig = (AgileEncryptionHeader)infoOrig.getHeader();
        AgileEncryptionHeader aehReload = (AgileEncryptionHeader)infoReload.getHeader();
        assertEquals(aehOrig.getBlockSize(), aehReload.getBlockSize());
        assertEquals(aehOrig.getChainingMode(), aehReload.getChainingMode());
        assertEquals(aehOrig.getCipherAlgorithm(), aehReload.getCipherAlgorithm());
        assertEquals(aehOrig.getCipherProvider(), aehReload.getCipherProvider());
        assertEquals(aehOrig.getCspName(), aehReload.getCspName());
        assertArrayEquals(aehOrig.getEncryptedHmacKey(), aehReload.getEncryptedHmacKey());
        // this only works, when the paddings are mocked to be the same ...
        // assertArrayEquals(aehOrig.getEncryptedHmacValue(), aehReload.getEncryptedHmacValue());
        assertEquals(aehOrig.getFlags(), aehReload.getFlags());
        assertEquals(aehOrig.getHashAlgorithm(), aehReload.getHashAlgorithm());
        assertArrayEquals(aehOrig.getKeySalt(), aehReload.getKeySalt());
        assertEquals(aehOrig.getKeySize(), aehReload.getKeySize());
        
        AgileEncryptionVerifier aevOrig = (AgileEncryptionVerifier)infoOrig.getVerifier();
        AgileEncryptionVerifier aevReload = (AgileEncryptionVerifier)infoReload.getVerifier();
        assertEquals(aevOrig.getBlockSize(), aevReload.getBlockSize());
        assertEquals(aevOrig.getChainingMode(), aevReload.getChainingMode());
        assertEquals(aevOrig.getCipherAlgorithm(), aevReload.getCipherAlgorithm());
        assertArrayEquals(aevOrig.getEncryptedKey(), aevReload.getEncryptedKey());
        assertArrayEquals(aevOrig.getEncryptedVerifier(), aevReload.getEncryptedVerifier());
        assertArrayEquals(aevOrig.getEncryptedVerifierHash(), aevReload.getEncryptedVerifierHash());
        assertEquals(aevOrig.getHashAlgorithm(), aevReload.getHashAlgorithm());
        assertEquals(aevOrig.getKeySize(), aevReload.getKeySize());
        assertArrayEquals(aevOrig.getSalt(), aevReload.getSalt());
        assertEquals(aevOrig.getSpinCount(), aevReload.getSpinCount());

        AgileDecryptor adOrig = (AgileDecryptor)infoOrig.getDecryptor();
        AgileDecryptor adReload = (AgileDecryptor)infoReload.getDecryptor();
        
        assertArrayEquals(adOrig.getIntegrityHmacKey(), adReload.getIntegrityHmacKey());
        // doesn't work without mocking ... see above
        // assertArrayEquals(adOrig.getIntegrityHmacValue(), adReload.getIntegrityHmacValue());
        assertArrayEquals(adOrig.getSecretKey().getEncoded(), adReload.getSecretKey().getEncoded());
        assertArrayEquals(adOrig.getVerifier(), adReload.getVerifier());

        fsReload.close();
    }
}
