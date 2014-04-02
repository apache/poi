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
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Class to test {@link LittleEndianInputStream} and {@link LittleEndianOutputStream}
 *
 * @author Josh Micich
 */
public final class TestLittleEndianStreams extends TestCase {

	public void testRead() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		LittleEndianOutput leo = new LittleEndianOutputStream(baos);
		leo.writeInt(12345678);
		leo.writeShort(12345);
		leo.writeByte(123);
		leo.writeShort(40000);
		leo.writeByte(200);
		leo.writeLong(1234567890123456789L);
		leo.writeDouble(123.456);

		LittleEndianInput lei = new LittleEndianInputStream(new ByteArrayInputStream(baos.toByteArray()));

		assertEquals(12345678, lei.readInt());
		assertEquals(12345, lei.readShort());
		assertEquals(123, lei.readByte());
		assertEquals(40000, lei.readUShort());
		assertEquals(200, lei.readUByte());
		assertEquals(1234567890123456789L, lei.readLong());
		assertEquals(123.456, lei.readDouble(), 0.0);
	}

	/**
	 * Up until svn r836101 {@link LittleEndianByteArrayInputStream#readFully(byte[], int, int)}
	 * had an error which resulted in the data being read and written back to the source byte
	 * array.
	 */
	public void testReadFully() {
		byte[] srcBuf = HexRead.readFromString("99 88 77 66 55 44 33");
		LittleEndianInput lei = new LittleEndianByteArrayInputStream(srcBuf);

		// do initial read to increment the read index beyond zero
		assertEquals(0x8899, lei.readUShort());

		byte[] actBuf = new byte[4];
		lei.readFully(actBuf);

		if (actBuf[0] == 0x00 && srcBuf[0] == 0x77 && srcBuf[3] == 0x44) {
			throw new AssertionFailedError("Identified bug in readFully() - source buffer was modified");
		}

		byte[] expBuf = HexRead.readFromString("77 66 55 44");
		assertTrue(Arrays.equals(actBuf, expBuf));
		assertEquals(0x33, lei.readUByte());
		assertEquals(0, lei.available());
	}
}
