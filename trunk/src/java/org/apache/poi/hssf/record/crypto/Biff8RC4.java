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

package org.apache.poi.hssf.record.crypto;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.FilePassRecord;
import org.apache.poi.hssf.record.InterfaceHdrRecord;

/**
 * Used for both encrypting and decrypting BIFF8 streams. The internal
 * {@link Cipher} instance is renewed (re-keyed) every 1024 bytes.
 */
final class Biff8RC4 implements Biff8Cipher {

	private static final int RC4_REKEYING_INTERVAL = 1024;

	private Cipher _rc4;
	
	/**
	 * This field is used to keep track of when to change the {@link Cipher}
	 * instance. The change occurs every 1024 bytes. Every byte passed over is
	 * counted.
	 */
	private int _streamPos;
	private int _nextRC4BlockStart;
	private int _currentKeyIndex;
	private boolean _shouldSkipEncryptionOnCurrentRecord;
    private final Biff8RC4Key _key;
    private ByteBuffer _buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);

	public Biff8RC4(int initialOffset, Biff8RC4Key key) {
		if (initialOffset >= RC4_REKEYING_INTERVAL) {
			throw new RuntimeException("initialOffset (" + initialOffset + ")>"
					+ RC4_REKEYING_INTERVAL + " not supported yet");
		}
		_key = key;
        _rc4 = _key.getCipher();
		_streamPos = 0;
		rekeyForNextBlock();
		_streamPos = initialOffset;
		_shouldSkipEncryptionOnCurrentRecord = false;
		
	    encryptBytes(new byte[initialOffset], 0, initialOffset);
	}
	

	private void rekeyForNextBlock() {
		_currentKeyIndex = _streamPos / RC4_REKEYING_INTERVAL;
		_key.initCipherForBlock(_rc4, _currentKeyIndex);
		_nextRC4BlockStart = (_currentKeyIndex + 1) * RC4_REKEYING_INTERVAL;
	}

	private void encryptBytes(byte data[], int offset, final int bytesToRead)  {
	    if (bytesToRead == 0) return;
	    
	    if (_shouldSkipEncryptionOnCurrentRecord) {
            // even when encryption is skipped, we need to update the cipher
	        byte dataCpy[] = new byte[bytesToRead];
	        System.arraycopy(data, offset, dataCpy, 0, bytesToRead);
	        data = dataCpy;
	        offset = 0;
	    }
	    
        try {
            _rc4.update(data, offset, bytesToRead, data, offset);
        } catch (ShortBufferException e) {
            throw new EncryptedDocumentException("input buffer too small", e);
        }
	}
	
	public void startRecord(int currentSid) {
		_shouldSkipEncryptionOnCurrentRecord = isNeverEncryptedRecord(currentSid);
	}

	/**
	 * TODO: Additionally, the lbPlyPos (position_of_BOF) field of the BoundSheet8 record MUST NOT be encrypted.
	 *
	 * @return <code>true</code> if record type specified by <tt>sid</tt> is never encrypted
	 */
	private static boolean isNeverEncryptedRecord(int sid) {
		switch (sid) {
			case BOFRecord.sid:
				// sheet BOFs for sure
				// TODO - find out about chart BOFs

			case InterfaceHdrRecord.sid:
				// don't know why this record doesn't seem to get encrypted

			case FilePassRecord.sid:
				// this only really counts when writing because FILEPASS is read early

			// UsrExcl(0x0194)
			// FileLock
			// RRDInfo(0x0196)
			// RRDHead(0x0138)

				return true;
		}
		return false;
	}

	/**
	 * Used when BIFF header fields (sid, size) are being read. The internal
	 * {@link Cipher} instance must step even when unencrypted bytes are read
	 */
	public void skipTwoBytes() {
	    xor(_buffer.array(), 0, 2);
	}
	
	public void xor(byte[] buf, int pOffset, int pLen) {
		int nLeftInBlock;
		nLeftInBlock = _nextRC4BlockStart - _streamPos;
		if (pLen <= nLeftInBlock) {
            // simple case - this read does not cross key blocks
		    encryptBytes(buf, pOffset, pLen);
			_streamPos += pLen;
			return;
		}

		int offset = pOffset;
		int len = pLen;

		// start by using the rest of the current block
		if (len > nLeftInBlock) {
			if (nLeftInBlock > 0) {
	            encryptBytes(buf, offset, nLeftInBlock);
				_streamPos += nLeftInBlock;
				offset += nLeftInBlock;
				len -= nLeftInBlock;
			}
			rekeyForNextBlock();
		}
		// all full blocks following
		while (len > RC4_REKEYING_INTERVAL) {
            encryptBytes(buf, offset, RC4_REKEYING_INTERVAL);
			_streamPos += RC4_REKEYING_INTERVAL;
			offset += RC4_REKEYING_INTERVAL;
			len -= RC4_REKEYING_INTERVAL;
			rekeyForNextBlock();
		}
		// finish with incomplete block
        encryptBytes(buf, offset, len);
		_streamPos += len;
	}

	public int xorByte(int rawVal) {
	    _buffer.put(0, (byte)rawVal);
	    xor(_buffer.array(), 0, 1);
		return _buffer.get(0);
	}

	public int xorShort(int rawVal) {
	    _buffer.putShort(0, (short)rawVal);
	    xor(_buffer.array(), 0, 2);
		return _buffer.getShort(0);
	}

	public int xorInt(int rawVal) {
	    _buffer.putInt(0, rawVal);
	    xor(_buffer.array(), 0, 4);
		return _buffer.getInt(0);
	}

	public long xorLong(long rawVal) {
        _buffer.putLong(0, rawVal);
        xor(_buffer.array(), 0, 8);
        return _buffer.getLong(0);
	}
	
	public void setNextRecordSize(int recordSize) {
	    /* no-op */
	}
}
