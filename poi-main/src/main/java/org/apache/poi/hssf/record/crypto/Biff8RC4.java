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

import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.FilePassRecord;
import org.apache.poi.hssf.record.InterfaceHdrRecord;

/**
 * Used for both encrypting and decrypting BIFF8 streams. The internal
 * {@link RC4} instance is renewed (re-keyed) every 1024 bytes.
 *
 * @author Josh Micich
 */
final class Biff8RC4 {

	private static final int RC4_REKEYING_INTERVAL = 1024;

	private RC4 _rc4;
	/**
	 * This field is used to keep track of when to change the {@link RC4}
	 * instance. The change occurs every 1024 bytes. Every byte passed over is
	 * counted.
	 */
	private int _streamPos;
	private int _nextRC4BlockStart;
	private int _currentKeyIndex;
	private boolean _shouldSkipEncryptionOnCurrentRecord;

	private final Biff8EncryptionKey _key;

	public Biff8RC4(int initialOffset, Biff8EncryptionKey key) {
		if (initialOffset >= RC4_REKEYING_INTERVAL) {
			throw new RuntimeException("initialOffset (" + initialOffset + ")>"
					+ RC4_REKEYING_INTERVAL + " not supported yet");
		}
		_key = key;
		_streamPos = 0;
		rekeyForNextBlock();
		_streamPos = initialOffset;
		for (int i = initialOffset; i > 0; i--) {
			_rc4.output();
		}
		_shouldSkipEncryptionOnCurrentRecord = false;
	}

	private void rekeyForNextBlock() {
		_currentKeyIndex = _streamPos / RC4_REKEYING_INTERVAL;
		_rc4 = _key.createRC4(_currentKeyIndex);
		_nextRC4BlockStart = (_currentKeyIndex + 1) * RC4_REKEYING_INTERVAL;
	}

	private int getNextRC4Byte() {
		if (_streamPos >= _nextRC4BlockStart) {
			rekeyForNextBlock();
		}
		byte mask = _rc4.output();
		_streamPos++;
		if (_shouldSkipEncryptionOnCurrentRecord) {
			return 0;
		}
		return mask & 0xFF;
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
	 * {@link RC4} instance must step even when unencrypted bytes are read
	 */
	public void skipTwoBytes() {
		getNextRC4Byte();
		getNextRC4Byte();
	}

	public void xor(byte[] buf, int pOffset, int pLen) {
		int nLeftInBlock;
		nLeftInBlock = _nextRC4BlockStart - _streamPos;
		if (pLen <= nLeftInBlock) {
			// simple case - this read does not cross key blocks
			_rc4.encrypt(buf, pOffset, pLen);
			_streamPos += pLen;
			return;
		}

		int offset = pOffset;
		int len = pLen;

		// start by using the rest of the current block
		if (len > nLeftInBlock) {
			if (nLeftInBlock > 0) {
				_rc4.encrypt(buf, offset, nLeftInBlock);
				_streamPos += nLeftInBlock;
				offset += nLeftInBlock;
				len -= nLeftInBlock;
			}
			rekeyForNextBlock();
		}
		// all full blocks following
		while (len > RC4_REKEYING_INTERVAL) {
			_rc4.encrypt(buf, offset, RC4_REKEYING_INTERVAL);
			_streamPos += RC4_REKEYING_INTERVAL;
			offset += RC4_REKEYING_INTERVAL;
			len -= RC4_REKEYING_INTERVAL;
			rekeyForNextBlock();
		}
		// finish with incomplete block
		_rc4.encrypt(buf, offset, len);
		_streamPos += len;
	}

	public int xorByte(int rawVal) {
		int mask = getNextRC4Byte();
		return (byte) (rawVal ^ mask);
	}

	public int xorShort(int rawVal) {
		int b0 = getNextRC4Byte();
		int b1 = getNextRC4Byte();
		int mask = (b1 << 8) + (b0 << 0);
		return rawVal ^ mask;
	}

	public int xorInt(int rawVal) {
		int b0 = getNextRC4Byte();
		int b1 = getNextRC4Byte();
		int b2 = getNextRC4Byte();
		int b3 = getNextRC4Byte();
		int mask = (b3 << 24) + (b2 << 16) + (b1 << 8) + (b0 << 0);
		return rawVal ^ mask;
	}

	public long xorLong(long rawVal) {
		int b0 = getNextRC4Byte();
		int b1 = getNextRC4Byte();
		int b2 = getNextRC4Byte();
		int b3 = getNextRC4Byte();
		int b4 = getNextRC4Byte();
		int b5 = getNextRC4Byte();
		int b6 = getNextRC4Byte();
		int b7 = getNextRC4Byte();
		long mask =
			  (((long)b7) << 56)
			+ (((long)b6) << 48)
			+ (((long)b5) << 40)
			+ (((long)b4) << 32)
			+ (((long)b3) << 24)
			+ (b2 << 16)
			+ (b1 << 8)
			+ (b0 << 0);
		return rawVal ^ mask;
	}
}
