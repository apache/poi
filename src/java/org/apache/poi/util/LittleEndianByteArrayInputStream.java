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

import java.io.ByteArrayInputStream;

/**
 * Adapts a plain byte array to {@link LittleEndianInput}
 */
public final class LittleEndianByteArrayInputStream extends ByteArrayInputStream implements LittleEndianInput {
	public LittleEndianByteArrayInputStream(byte[] buf, int startOffset, int maxReadLen) { // NOSONAR
	    super(buf, startOffset, maxReadLen);
	}
	
	public LittleEndianByteArrayInputStream(byte[] buf, int startOffset) {
	    super(buf, startOffset, buf.length - startOffset);
	}
	
	public LittleEndianByteArrayInputStream(byte[] buf) {
	    super(buf);
	}

	private void checkPosition(int i) {
		if (i > count - pos) {
			throw new RuntimeException("Buffer overrun");
		}
	}

	public int getReadIndex() {
		return pos;
	}

	@Override
    public byte readByte() {
		checkPosition(1);
		return (byte)read();
	}

	@Override
    public int readInt() {
	    final int size = LittleEndianConsts.INT_SIZE;
		checkPosition(size);
		int le = LittleEndian.getInt(buf, pos);
        long skipped = super.skip(size);
        assert skipped == size : "Buffer overrun";
		return le;
	}

	@Override
    public long readLong() {
	    final int size = LittleEndianConsts.LONG_SIZE;
		checkPosition(size);
		long le = LittleEndian.getLong(buf, pos);
        long skipped = super.skip(size);
        assert skipped == size : "Buffer overrun";
		return le;
	}

	@Override
    public short readShort() {
		return (short)readUShort();
	}

	@Override
    public int readUByte() {
	    return readByte() & 0xFF;
	}

	@Override
    public int readUShort() {
        final int size = LittleEndianConsts.SHORT_SIZE;
        checkPosition(size);
        int le = LittleEndian.getUShort(buf, pos);
        long skipped = super.skip(size);
        assert skipped == size : "Buffer overrun";
        return le;
	}

    @Override
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }
	
	@Override
    public void readFully(byte[] buffer, int off, int len) {
		checkPosition(len);
		read(buffer, off, len);
	}

	@Override
    public void readFully(byte[] buffer) {
        checkPosition(buffer.length);
        read(buffer, 0, buffer.length);
	}

    @Override
    public void readPlain(byte[] buf, int off, int len) {
        readFully(buf, off, len);
    }
}
