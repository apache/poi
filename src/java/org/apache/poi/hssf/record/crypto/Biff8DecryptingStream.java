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

import java.io.InputStream;

import org.apache.poi.hssf.record.BiffHeaderInput;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianInputStream;

/**
 *
 * @author Josh Micich
 */
public final class Biff8DecryptingStream implements BiffHeaderInput, LittleEndianInput {

	private final LittleEndianInput _le;
	private final Biff8RC4 _rc4;

	public Biff8DecryptingStream(InputStream in, int initialOffset, Biff8EncryptionKey key) {
		_rc4 = new Biff8RC4(initialOffset, key);

		if (in instanceof LittleEndianInput) {
			// accessing directly is an optimisation
			_le = (LittleEndianInput) in;
		} else {
			// less optimal, but should work OK just the same. Often occurs in junit tests.
			_le = new LittleEndianInputStream(in);
		}
	}

	public int available() {
		return _le.available();
	}

	/**
	 * Reads an unsigned short value without decrypting
	 */
	public int readRecordSID() {
		int sid = _le.readUShort();
		_rc4.skipTwoBytes();
		_rc4.startRecord(sid);
		return sid;
	}

	/**
	 * Reads an unsigned short value without decrypting
	 */
	public int readDataSize() {
		int dataSize = _le.readUShort();
		_rc4.skipTwoBytes();
		return dataSize;
	}

	public double readDouble() {
		long valueLongBits = readLong();
		double result = Double.longBitsToDouble(valueLongBits);
		if (Double.isNaN(result)) {
			throw new RuntimeException("Did not expect to read NaN"); // (Because Excel typically doesn't write NaN
		}
		return result;
	}

	public void readFully(byte[] buf) {
		readFully(buf, 0, buf.length);
	}

	public void readFully(byte[] buf, int off, int len) {
		_le.readFully(buf, off, len);
		_rc4.xor(buf, off, len);
	}


	public int readUByte() {
		return _rc4.xorByte(_le.readUByte());
	}
	public byte readByte() {
		return (byte) _rc4.xorByte(_le.readUByte());
	}


	public int readUShort() {
		return _rc4.xorShort(_le.readUShort());
	}
	public short readShort() {
		return (short) _rc4.xorShort(_le.readUShort());
	}

	public int readInt() {
		return _rc4.xorInt(_le.readInt());
	}

	public long readLong() {
		return _rc4.xorLong(_le.readLong());
	}
}
