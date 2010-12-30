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

package org.apache.poi.util;

/**
 * Adapts a plain byte array to {@link LittleEndianInput}
 *
 * @author Josh Micich
 */
public final class LittleEndianByteArrayInputStream implements LittleEndianInput {
	private final byte[] _buf;
	private final int _endIndex;
	private int _readIndex;

	public LittleEndianByteArrayInputStream(byte[] buf, int startOffset, int maxReadLen) {
		_buf = buf;
		_readIndex = startOffset;
		_endIndex = startOffset + maxReadLen;
	}
	public LittleEndianByteArrayInputStream(byte[] buf, int startOffset) {
		this(buf, startOffset, buf.length - startOffset);
	}
	public LittleEndianByteArrayInputStream(byte[] buf) {
		this(buf, 0, buf.length);
	}

	public int available() {
		return _endIndex - _readIndex;
	}
	private void checkPosition(int i) {
		if (i > _endIndex - _readIndex) {
			throw new RuntimeException("Buffer overrun");
		}
	}

	public int getReadIndex() {
		return _readIndex;
	}
	public byte readByte() {
		checkPosition(1);
		return _buf[_readIndex++];
	}

	public int readInt() {
		checkPosition(4);
		int i = _readIndex;

		int b0 = _buf[i++] & 0xFF;
		int b1 = _buf[i++] & 0xFF;
		int b2 = _buf[i++] & 0xFF;
		int b3 = _buf[i++] & 0xFF;
		_readIndex = i;
		return (b3 << 24) + (b2 << 16) + (b1 << 8) + (b0 << 0);
	}
	public long readLong() {
		checkPosition(8);
		int i = _readIndex;

		int b0 = _buf[i++] & 0xFF;
		int b1 = _buf[i++] & 0xFF;
		int b2 = _buf[i++] & 0xFF;
		int b3 = _buf[i++] & 0xFF;
		int b4 = _buf[i++] & 0xFF;
		int b5 = _buf[i++] & 0xFF;
		int b6 = _buf[i++] & 0xFF;
		int b7 = _buf[i++] & 0xFF;
		_readIndex = i;
		return (((long)b7 << 56) +
				((long)b6 << 48) +
				((long)b5 << 40) +
				((long)b4 << 32) +
				((long)b3 << 24) +
				(b2 << 16) +
				(b1 <<  8) +
				(b0 <<  0));
	}
	public short readShort() {
		return (short)readUShort();
	}
	public int readUByte() {
		checkPosition(1);
		return _buf[_readIndex++] & 0xFF;
	}
	public int readUShort() {
		checkPosition(2);
		int i = _readIndex;

		int b0 = _buf[i++] & 0xFF;
		int b1 = _buf[i++] & 0xFF;
		_readIndex = i;
		return (b1 << 8) + (b0 << 0);
	}
	public void readFully(byte[] buf, int off, int len) {
		checkPosition(len);
		System.arraycopy(_buf, _readIndex, buf, off, len);
		_readIndex+=len;
	}
	public void readFully(byte[] buf) {
		readFully(buf, 0, buf.length);
	}
	public double readDouble() {
		return Double.longBitsToDouble(readLong());
	}
}
