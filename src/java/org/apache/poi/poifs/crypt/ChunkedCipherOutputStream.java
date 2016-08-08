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

import static org.apache.poi.poifs.crypt.Decryptor.DEFAULT_POIFS_ENTRY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSWriterEvent;
import org.apache.poi.poifs.filesystem.POIFSWriterListener;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.TempFile;

@Internal
public abstract class ChunkedCipherOutputStream extends FilterOutputStream {
    private static final POILogger LOG = POILogFactory.getLogger(ChunkedCipherOutputStream.class);
    private static final int STREAMING = -1;

    protected final int _chunkSize;
    protected final int _chunkBits;

    private final byte[] _chunk;
    private final File _fileOut;
    private final DirectoryNode _dir;

    private long _pos = 0;
    private Cipher _cipher;

    public ChunkedCipherOutputStream(DirectoryNode dir, int chunkSize) throws IOException, GeneralSecurityException {
        super(null);
        this._chunkSize = chunkSize;
        int cs = chunkSize == STREAMING ? 4096 : chunkSize;
        _chunk = new byte[cs];
        _chunkBits = Integer.bitCount(cs-1);
        _fileOut = TempFile.createTempFile("encrypted_package", "crypt");
        _fileOut.deleteOnExit();
        this.out = new FileOutputStream(_fileOut);
        this._dir = dir;
        _cipher = initCipherForBlock(null, 0, false);
    }

    public ChunkedCipherOutputStream(OutputStream stream, int chunkSize) throws IOException, GeneralSecurityException {
        super(stream);
        this._chunkSize = chunkSize;
        int cs = chunkSize == STREAMING ? 4096 : chunkSize;
        _chunk = new byte[cs];
        _chunkBits = Integer.bitCount(cs-1);
        _fileOut = null;
        _dir = null;
        _cipher = initCipherForBlock(null, 0, false);
    }

    public final Cipher initCipherForBlock(int block, boolean lastChunk) throws IOException, GeneralSecurityException {
        return initCipherForBlock(_cipher, block, lastChunk);
    }

    protected abstract Cipher initCipherForBlock(Cipher existing, int block, boolean lastChunk)
    throws IOException, GeneralSecurityException;

    protected abstract void calculateChecksum(File fileOut, int oleStreamSize)
    throws GeneralSecurityException, IOException;

    protected abstract void createEncryptionInfoEntry(DirectoryNode dir, File tmpFile)
    throws IOException, GeneralSecurityException;

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte)b});
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len)
    throws IOException {
        if (len == 0) {
            return;
        }

        if (len < 0 || b.length < off+len) {
            throw new IOException("not enough bytes in your input buffer");
        }

        final int chunkMask = getChunkMask();
        while (len > 0) {
            int posInChunk = (int)(_pos & chunkMask);
            int nextLen = Math.min(_chunk.length-posInChunk, len);
            System.arraycopy(b, off, _chunk, posInChunk, nextLen);
            _pos += nextLen;
            off += nextLen;
            len -= nextLen;
            if ((_pos & chunkMask) == 0) {
                writeChunk(len > 0);
            }
        }
    }

    private int getChunkMask() {
        return _chunk.length-1;
    }

    protected void writeChunk(boolean continued) throws IOException {
        if (_pos == 0) {
            return;
        }

        int posInChunk = (int)(_pos & getChunkMask());

        // normally posInChunk is 0, i.e. on the next chunk (-> index-1)
        // but if called on close(), posInChunk is somewhere within the chunk data
        int index = (int)(_pos >> _chunkBits);
        boolean lastChunk;
        if (posInChunk==0) {
            index--;
            posInChunk = _chunk.length;
            lastChunk = false;
        } else {
            // pad the last chunk
            lastChunk = true;
        }

        int ciLen;
        try {
            boolean doFinal = true;
            if (_chunkSize == STREAMING) {
                if (continued) {
                    doFinal = false;
                }
                // reset stream (not only) in case we were interrupted by plain stream parts
                _pos = 0;
            } else {
                _cipher = initCipherForBlock(_cipher, index, lastChunk);
            }
            ciLen = invokeCipher(posInChunk, doFinal);
        } catch (GeneralSecurityException e) {
            throw new IOException("can't re-/initialize cipher", e);
        }

        out.write(_chunk, 0, ciLen);
    }

    /**
     * Helper function for overriding the cipher invocation, i.e. XOR doesn't use a cipher
     * and uses it's own implementation
     *
     * @return
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     * @throws ShortBufferException 
     */
    protected int invokeCipher(int posInChunk, boolean doFinal) throws GeneralSecurityException {
        if (doFinal) {
            return _cipher.doFinal(_chunk, 0, posInChunk, _chunk);
        } else {
            return _cipher.update(_chunk, 0, posInChunk, _chunk);
        }
    }
    
    @Override
    public void close() throws IOException {
        try {
            writeChunk(false);

            super.close();

            if (_fileOut != null) {
                int oleStreamSize = (int)(_fileOut.length()+LittleEndianConsts.LONG_SIZE);
                calculateChecksum(_fileOut, (int)_pos);
                _dir.createDocument(DEFAULT_POIFS_ENTRY, oleStreamSize, new EncryptedPackageWriter());
                createEncryptionInfoEntry(_dir, _fileOut);
            }
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    private class EncryptedPackageWriter implements POIFSWriterListener {
        @Override
        public void processPOIFSWriterEvent(POIFSWriterEvent event) {
            try {
                OutputStream os = event.getStream();

                // StreamSize (8 bytes): An unsigned integer that specifies the number of bytes used by data
                // encrypted within the EncryptedData field, not including the size of the StreamSize field.
                // Note that the actual size of the \EncryptedPackage stream (1) can be larger than this
                // value, depending on the block size of the chosen encryption algorithm
                byte buf[] = new byte[LittleEndianConsts.LONG_SIZE];
                LittleEndian.putLong(buf, 0, _pos);
                os.write(buf);

                FileInputStream fis = new FileInputStream(_fileOut);
                IOUtils.copy(fis, os);
                fis.close();

                os.close();

                if (!_fileOut.delete()) {
                    LOG.log(POILogger.ERROR, "Can't delete temporary encryption file: "+_fileOut);
                }
            } catch (IOException e) {
                throw new EncryptedDocumentException(e);
            }
        }
    }
}
