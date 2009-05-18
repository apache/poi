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
}
