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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wraps an {@link InputStream} providing {@link LittleEndianInput}<p/>
 *
 * This class does not buffer any input, so the stream read position maintained
 * by this class is consistent with that of the inner stream.
 *
 * @author Josh Micich
 */
public class LittleEndianInputStream extends FilterInputStream implements LittleEndianInput {
	public LittleEndianInputStream(InputStream is) {
		super(is);
	}
	public int available() {
		try {
			return super.available();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public byte readByte() {
		return (byte)readUByte();
	}
	public int readUByte() {
		int ch;
		try {
			ch = in.read();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		checkEOF(ch);
		return ch;
	}
	public double readDouble() {
		return Double.longBitsToDouble(readLong());
	}
	public int readInt() {
		int ch1;
		int ch2;
		int ch3;
		int ch4;
		try {
			ch1 = in.read();
			ch2 = in.read();
			ch3 = in.read();
			ch4 = in.read();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		checkEOF(ch1 | ch2 | ch3 | ch4);
		return (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0);
	}
	public long readLong() {
		int b0;
		int b1;
		int b2;
		int b3;
		int b4;
		int b5;
		int b6;
		int b7;
		try {
			b0 = in.read();
			b1 = in.read();
			b2 = in.read();
			b3 = in.read();
			b4 = in.read();
			b5 = in.read();
			b6 = in.read();
			b7 = in.read();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		checkEOF(b0 | b1 | b2 | b3 | b4 | b5 | b6 | b7);
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
	public int readUShort() {
		int ch1;
		int ch2;
		try {
			ch1 = in.read();
			ch2 = in.read();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		checkEOF(ch1 | ch2);
		return (ch2 << 8) + (ch1 << 0);
	}
	private static void checkEOF(int value) {
		if (value <0) {
			throw new RuntimeException("Unexpected end-of-file");
		}
	}

	public void readFully(byte[] buf) {
		readFully(buf, 0, buf.length);
	}

	public void readFully(byte[] buf, int off, int len) {
		int max = off+len;
		for(int i=off; i<max; i++) {
			int ch;
			try {
				ch = in.read();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			checkEOF(ch);
			buf[i] = (byte) ch;
		}
	}
}
