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

package org.apache.poi.hwpf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.POIDocument;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.hwpf.model.CHPBinTable;
import org.apache.poi.hwpf.model.FibBase;
import org.apache.poi.hwpf.model.FileInformationBlock;
import org.apache.poi.hwpf.model.FontTable;
import org.apache.poi.hwpf.model.ListTables;
import org.apache.poi.hwpf.model.PAPBinTable;
import org.apache.poi.hwpf.model.SectionTable;
import org.apache.poi.hwpf.model.StyleSheet;
import org.apache.poi.hwpf.model.TextPieceTable;
import org.apache.poi.hwpf.usermodel.ObjectPoolImpl;
import org.apache.poi.hwpf.usermodel.ObjectsPool;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.crypt.ChunkedCipherInputStream;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.BoundedInputStream;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianByteArrayInputStream;


/**
 * This class holds much of the core of a Word document, but
 *  without some of the table structure information.
 * You generally want to work with one of
 *  {@link HWPFDocument} or {@link HWPFOldDocument}
 */
public abstract class HWPFDocumentCore extends POIDocument {
    protected static final String STREAM_OBJECT_POOL = "ObjectPool";
    protected static final String STREAM_WORD_DOCUMENT = "WordDocument";
    protected static final String STREAM_TABLE_0 = "0Table";
    protected static final String STREAM_TABLE_1 = "1Table";

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 1_000_000;

    /**
     * Size of the not encrypted part of the FIB
     */
    protected static final int FIB_BASE_LEN = 68;
    
    /**
     * [MS-DOC] 2.2.6.2/3 Office Binary Document ... Encryption:
     * "... The block number MUST be set to zero at the beginning of the stream and
     * MUST be incremented at each 512 byte boundary. ..."
     */
    protected static final int RC4_REKEYING_INTERVAL = 512;

    /** Holds OLE2 objects */
    protected ObjectPoolImpl _objectPool;

    /** The FIB */
    protected FileInformationBlock _fib;

    /** Holds styles for this document.*/
    protected StyleSheet _ss;

    /** Contains formatting properties for text*/
    protected CHPBinTable _cbt;

    /** Contains formatting properties for paragraphs*/
    protected PAPBinTable _pbt;

    /** Contains formatting properties for sections.*/
    protected SectionTable _st;

    /** Holds fonts for this document.*/
    protected FontTable _ft;

    /** Hold list tables */
    protected ListTables _lt;

    /** main document stream buffer*/
    protected byte[] _mainStream;

    private EncryptionInfo _encryptionInfo;

    protected HWPFDocumentCore() {
        super((DirectoryNode)null);
    }

    /**
     * Takes an InputStream, verifies that it's not RTF or PDF, builds a
     *  POIFSFileSystem from it, and returns that.
     */
    public static POIFSFileSystem verifyAndBuildPOIFS(InputStream istream) throws IOException {
        InputStream is = FileMagic.prepareToCheckMagic(istream);
        FileMagic fm = FileMagic.valueOf(is);

        if (fm != FileMagic.OLE2) {
            throw new IllegalArgumentException("The document is really a "+fm+" file");
        }

        return new POIFSFileSystem(is);
    }

    /**
     * This constructor loads a Word document from an InputStream.
     *
     * @param istream The InputStream that contains the Word document.
     * @throws IOException If there is an unexpected IOException from the passed
     *         in InputStream.
     */
    public HWPFDocumentCore(InputStream istream) throws IOException {
        //do Ole stuff
        this( verifyAndBuildPOIFS(istream) );
    }

    /**
     * This constructor loads a Word document from a POIFSFileSystem
     *
     * @param pfilesystem The POIFSFileSystem that contains the Word document.
     * @throws IOException If there is an unexpected IOException from the passed
     *         in POIFSFileSystem.
     */
    public HWPFDocumentCore(POIFSFileSystem pfilesystem) throws IOException {
        this(pfilesystem.getRoot());
    }

    /**
     * This constructor loads a Word document from a specific point
     *  in a POIFSFileSystem, probably not the default.
     * Used typically to open embeded documents.
     *
     * @param directory The DirectoryNode that contains the Word document.
     * @throws IOException If there is an unexpected IOException from the passed
     *         in POIFSFileSystem.
     */
    public HWPFDocumentCore(DirectoryNode directory) throws IOException {
        // Sort out the hpsf properties
        super(directory);

        // read in the main stream.
        _mainStream = getDocumentEntryBytes(STREAM_WORD_DOCUMENT, FIB_BASE_LEN, Integer.MAX_VALUE);
        _fib = new FileInformationBlock(_mainStream);

        DirectoryEntry objectPoolEntry = null;
        if (directory.hasEntry(STREAM_OBJECT_POOL)) {
            objectPoolEntry = (DirectoryEntry) directory.getEntry(STREAM_OBJECT_POOL);
        }
        _objectPool = new ObjectPoolImpl(objectPoolEntry);
    }
    /**
     * Returns the range which covers the whole of the document, but excludes
     * any headers and footers.
     */
    public abstract Range getRange();

    /**
     * Returns the range that covers all text in the file, including main text,
     * footnotes, headers and comments
     */
    public abstract Range getOverallRange();

    /**
     * Returns document text, i.e. text information from all text pieces,
     * including OLE descriptions and field codes
     */
    public String getDocumentText() {
        return getText().toString();
    }

    /**
     * Internal method to access document text
     */
    @Internal
    public abstract StringBuilder getText();

    public CHPBinTable getCharacterTable() {
        return _cbt;
    }

    public PAPBinTable getParagraphTable() {
        return _pbt;
    }

    public SectionTable getSectionTable() {
        return _st;
    }

    public StyleSheet getStyleSheet() {
        return _ss;
    }

    public ListTables getListTables() {
        return _lt;
    }

    public FontTable getFontTable() {
        return _ft;
    }

    public FileInformationBlock getFileInformationBlock() {
        return _fib;
    }

    public ObjectsPool getObjectsPool() {
        return _objectPool;
    }

    public abstract TextPieceTable getTextTable();

    @Internal
    public byte[] getMainStream() {
        return _mainStream;
    }

    @Override
    public EncryptionInfo getEncryptionInfo() throws IOException {
        if (_encryptionInfo != null) {
            return _encryptionInfo;
        }

        // Create our FIB, and check for the doc being encrypted
        FibBase fibBase;
        if (_fib != null && _fib.getFibBase() != null) {
            fibBase = _fib.getFibBase();
        } else {
            byte[] fibBaseBytes = (_mainStream != null) ? _mainStream : getDocumentEntryBytes(STREAM_WORD_DOCUMENT, -1, FIB_BASE_LEN);
            fibBase = new FibBase( fibBaseBytes, 0 );
        }
        if (!fibBase.isFEncrypted()) {
            return null;
        }

        String tableStrmName = fibBase.isFWhichTblStm() ? STREAM_TABLE_1 : STREAM_TABLE_0;
        byte[] tableStream = getDocumentEntryBytes(tableStrmName, -1, fibBase.getLKey());
        LittleEndianByteArrayInputStream leis = new LittleEndianByteArrayInputStream(tableStream);
        EncryptionMode em = fibBase.isFObfuscated() ? EncryptionMode.xor : null;
        EncryptionInfo ei = new EncryptionInfo(leis, em);
        Decryptor dec = ei.getDecryptor();
        dec.setChunkSize(RC4_REKEYING_INTERVAL);
        try {
            String pass = Biff8EncryptionKey.getCurrentUserPassword();
            if (pass == null) {
                pass = Decryptor.DEFAULT_PASSWORD;
            }
            if (!dec.verifyPassword(pass)) {
                throw new EncryptedDocumentException("document is encrypted, password is invalid - use Biff8EncryptionKey.setCurrentUserPasswort() to set password before opening");
            }
        } catch (GeneralSecurityException e) {
            throw new IOException(e.getMessage(), e);
        }
        _encryptionInfo = ei;
        return ei;
    }

    protected void updateEncryptionInfo() {
        // make sure, that we've read all the streams ...
        readProperties();
        // now check for the password
        String password = Biff8EncryptionKey.getCurrentUserPassword();
        FibBase fBase = _fib.getFibBase();
        if (password == null) {
            fBase.setLKey(0);
            fBase.setFEncrypted(false);
            fBase.setFObfuscated(false);
            _encryptionInfo = null;
        } else {
            // create password record
            if (_encryptionInfo == null) {
                _encryptionInfo = new EncryptionInfo(EncryptionMode.cryptoAPI);
                fBase.setFEncrypted(true);
                fBase.setFObfuscated(false);
            }
            Encryptor enc = _encryptionInfo.getEncryptor();
            byte salt[] = _encryptionInfo.getVerifier().getSalt();
            if (salt == null) {
                enc.confirmPassword(password);
            } else {
                byte verifier[] = _encryptionInfo.getDecryptor().getVerifier();
                enc.confirmPassword(password, null, null, verifier, salt, null);
            }
        }
    }

    /**
     * Reads OLE Stream into byte array - if an {@link EncryptionInfo} is available,
     * decrypt the bytes starting at encryptionOffset. If encryptionOffset = -1, then do not try
     * to decrypt the bytes
     *
     * @param name the name of the stream
     * @param encryptionOffset the offset from which to start decrypting, use {@code -1} for no decryption
     * @param len length of the bytes to be read, use {@link Integer#MAX_VALUE} for all bytes
     * @return the read bytes
     * @throws IOException if the stream can't be found
     */
    protected byte[] getDocumentEntryBytes(String name, int encryptionOffset, int len) throws IOException {
        DirectoryNode dir = getDirectory();
        DocumentEntry documentProps = (DocumentEntry)dir.getEntry(name);
        DocumentInputStream dis = dir.createDocumentInputStream(documentProps);
        EncryptionInfo ei = (encryptionOffset > -1) ? getEncryptionInfo() : null;
        int streamSize = documentProps.getSize();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(Math.min(streamSize,len));

        InputStream is = dis;
        try {
            if (ei != null) {
                try {
                    Decryptor dec = ei.getDecryptor();
                    is = dec.getDataStream(dis, streamSize, 0);
                    if (encryptionOffset > 0) {
                        ChunkedCipherInputStream cis = (ChunkedCipherInputStream)is;
                        byte plain[] = IOUtils.safelyAllocate(encryptionOffset, MAX_RECORD_LENGTH);
                        cis.readPlain(plain, 0, encryptionOffset);
                        bos.write(plain);
                    }
                } catch (GeneralSecurityException e) {
                    throw new IOException(e.getMessage(), e);
                }
            }
            // This simplifies a few combinations, so we actually always try to copy len bytes
            // regardless if encryptionOffset is greater than 0
            if (len < Integer.MAX_VALUE) {
                is = new BoundedInputStream(is, len);
            }
            IOUtils.copy(is, bos);
            return bos.toByteArray();
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(dis);
        }
    }
}