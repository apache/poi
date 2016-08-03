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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianInputStream;

@Internal
public abstract class ChunkedCipherInputStream extends LittleEndianInputStream {
    private final int _chunkSize;
    private final int _chunkBits;
    
    private final long _size;
    private final byte[] _chunk;
    private final Cipher _cipher;

    private int _lastIndex;
    private long _pos;
    private boolean _chunkIsValid = false;

    public ChunkedCipherInputStream(LittleEndianInput stream, long size, int chunkSize)
    throws GeneralSecurityException {
        this(stream, size, chunkSize, 0);
    }

    public ChunkedCipherInputStream(LittleEndianInput stream, long size, int chunkSize, int initialPos)
    throws GeneralSecurityException {
        super((InputStream)stream);
        _size = size;
        _pos = initialPos;
        this._chunkSize = chunkSize;
        if (chunkSize == -1) {
            _chunk = new byte[4096];
        } else {
            _chunk = new byte[chunkSize];
        }
        _chunkBits = Integer.bitCount(_chunk.length-1);
        _lastIndex = (int)(_pos >> _chunkBits);
        _cipher = initCipherForBlock(null, _lastIndex);
    }
    
    public final Cipher initCipherForBlock(int block) throws IOException, GeneralSecurityException {
        if (_chunkSize != -1) {
            throw new GeneralSecurityException("the cipher block can only be set for streaming encryption, e.g. CryptoAPI...");
        }
        
        _chunkIsValid = false;
        return initCipherForBlock(_cipher, block);
    }
    
    protected abstract Cipher initCipherForBlock(Cipher existing, int block)
    throws GeneralSecurityException;

    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        if (read(b) == 1) {
            return b[0];
        }
        return -1;
    }

    // do not implement! -> recursion
    // public int read(byte[] b) throws IOException;

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int total = 0;
        
        if (available() <= 0) {
            return -1;
        }

        final int chunkMask = getChunkMask();
        while (len > 0) {
            if (!_chunkIsValid) {
                try {
                    nextChunk();
                    _chunkIsValid = true;
                } catch (GeneralSecurityException e) {
                    throw new EncryptedDocumentException(e.getMessage(), e);
                }
            }
            int count = (int)(_chunk.length - (_pos & chunkMask));
            int avail = available();
            if (avail == 0) {
                return total;
            }
            count = Math.min(avail, Math.min(count, len));
            System.arraycopy(_chunk, (int)(_pos & chunkMask), b, off, count);
            off += count;
            len -= count;
            _pos += count;
            if ((_pos & chunkMask) == 0) {
                _chunkIsValid = false;
            }
            total += count;
        }

        return total;
    }

    @Override
    public long skip(final long n) throws IOException {
        long start = _pos;
        long skip = Math.min(remainingBytes(), n);

        if ((((_pos + skip) ^ start) & ~getChunkMask()) != 0) {
            _chunkIsValid = false;
        }
        _pos += skip;
        return skip;
    }

    @Override
    public int available() {
        return remainingBytes();
    }
    
    /**
     * Helper method for forbidden available call - we know the size beforehand, so it's ok ...
     *
     * @return the remaining byte until EOF
     */
    private int remainingBytes() {
        return (int)(_size - _pos);
    }
    
    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public synchronized void reset() throws IOException {
        throw new UnsupportedOperationException();
    }

    private int getChunkMask() {
        return _chunk.length-1;
    }
    
    private void nextChunk() throws GeneralSecurityException, IOException {
        if (_chunkSize != -1) {
            int index = (int)(_pos >> _chunkBits);
            initCipherForBlock(_cipher, index);
        
            if (_lastIndex != index) {
                super.skip((index - _lastIndex) << _chunkBits);
            }

            _lastIndex = index + 1;
        }

        final int todo = (int)Math.min(_size, _chunk.length);
        int readBytes = 0, totalBytes = 0;
        do {
            readBytes = super.read(_chunk, totalBytes, todo-totalBytes);
            totalBytes += Math.max(0, readBytes);
        } while (readBytes != -1 && totalBytes < todo);

        if (readBytes == -1 && _pos+totalBytes < _size) {
            throw new EOFException("buffer underrun");
        }

        if (_chunkSize == -1) {
            _cipher.update(_chunk, 0, totalBytes, _chunk);
        } else {
            _cipher.doFinal(_chunk, 0, totalBytes, _chunk);
        }
    }
}
