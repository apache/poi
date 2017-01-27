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

package org.apache.poi.hssf.record;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.LittleEndianInput;

/**
 * A Record Input Stream derivative that makes access to byte arrays used in the
 * test cases work a bit easier.
 * <p> Creates the stream and moves to the first record.
 *
 * @author Jason Height (jheight at apache.org)
 */
public final class TestcaseRecordInputStream {

	private TestcaseRecordInputStream() {
		// no instances of this class
	}

	/**
	 * Prepends a mock record identifier to the supplied data and opens a record input stream
	 */
	public static LittleEndianInput createLittleEndian(byte[] data) {
		return new LittleEndianByteArrayInputStream(data);

	}
	public static RecordInputStream create(int sid, byte[] data) {
		return create(mergeDataAndSid(sid, data.length, data));
	}
	/**
	 * First 4 bytes of <tt>data</tt> are assumed to be record identifier and length. The supplied
	 * <tt>data</tt> can contain multiple records (sequentially encoded in the same way)
	 */
	public static RecordInputStream create(byte[] data) {
		InputStream is = new ByteArrayInputStream(data);
		RecordInputStream result = new RecordInputStream(is);
		result.nextRecord();
		return result;
	}

	public static byte[] mergeDataAndSid(int sid, int length, byte[] data) {
	  byte[] result = new byte[data.length + 4];
	  LittleEndian.putUShort(result, 0, sid);
	  LittleEndian.putUShort(result, 2, length);
	  System.arraycopy(data, 0, result, 4, data.length);
	  return result;
	}
	/**
	 * Confirms data sections are equal
	 * @param expectedData - just raw data (without sid or size short ints)
	 * @param actualRecordBytes this includes 4 prefix bytes (sid & size)
	 */
	public static void confirmRecordEncoding(int expectedSid, byte[] expectedData, byte[] actualRecordBytes)
			throws AssertionFailedError {
		confirmRecordEncoding(null, expectedSid, expectedData, actualRecordBytes);
	}
	/**
	 * Confirms data sections are equal
	 * @param msgPrefix message prefix to be displayed in case of failure
	 * @param expectedData - just raw data (without ushort sid, ushort size)
	 * @param actualRecordBytes this includes 4 prefix bytes (sid & size)
	 */
	public static void confirmRecordEncoding(String msgPrefix, int expectedSid, byte[] expectedData, byte[] actualRecordBytes)
			throws AssertionFailedError {
		int expectedDataSize = expectedData.length;
		Assert.assertEquals("Size of encode data mismatch", actualRecordBytes.length - 4, expectedDataSize);
		Assert.assertEquals(expectedSid, LittleEndian.getShort(actualRecordBytes, 0));
		Assert.assertEquals(expectedDataSize, LittleEndian.getShort(actualRecordBytes, 2));
		for (int i = 0; i < expectedDataSize; i++)
			if (expectedData[i] != actualRecordBytes[i+4]) {
				StringBuilder sb = new StringBuilder(64);
				if (msgPrefix != null) {
					sb.append(msgPrefix).append(": ");
				}
				sb.append("At offset ").append(i);
				sb.append(": expected ").append(HexDump.byteToHex(expectedData[i]));
				sb.append(" but found ").append(HexDump.byteToHex(actualRecordBytes[i+4]));
				throw new AssertionFailedError(sb.toString());
			}
	}
}
