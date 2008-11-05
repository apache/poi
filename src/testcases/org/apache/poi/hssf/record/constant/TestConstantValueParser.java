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

package org.apache.poi.hssf.record.constant;

import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.apache.poi.hssf.usermodel.HSSFErrorConstants;
import org.apache.poi.util.HexRead;
import org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.apache.poi.util.LittleEndianInput;
/**
 * 
 * @author Josh Micich
 */
public final class TestConstantValueParser extends TestCase {
	private static final Object[] SAMPLE_VALUES = {
			Boolean.TRUE,
			null,
			new Double(1.1),
			"Sample text",
			ErrorConstant.valueOf(HSSFErrorConstants.ERROR_DIV_0),
		};
	private static final byte[] SAMPLE_ENCODING = HexRead.readFromString(
		"04 01 00 00 00 00 00 00 00 " +
		"00 00 00 00 00 00 00 00 00 " +
		"01 9A 99 99 99 99 99 F1 3F " +
		"02 0B 00 00 53 61 6D 70 6C 65 20 74 65 78 74 " +
		"10 07 00 00 00 00 00 00 00");
	
	public void testGetEncodedSize() {
		int actual = ConstantValueParser.getEncodedSize(SAMPLE_VALUES);
		assertEquals(51, actual);
	}
	public void testEncode() {
		int size = ConstantValueParser.getEncodedSize(SAMPLE_VALUES);
		byte[] data = new byte[size];
		
		ConstantValueParser.encode(new LittleEndianByteArrayOutputStream(data, 0), SAMPLE_VALUES);
		
		if (!Arrays.equals(data, SAMPLE_ENCODING)) {
			fail("Encoding differs");
		}
	}
	public void testDecode() {
		LittleEndianInput in = TestcaseRecordInputStream.createLittleEndian(SAMPLE_ENCODING);
		
		Object[] values = ConstantValueParser.parse(in, 4);
		for (int i = 0; i < values.length; i++) {
			if(!isEqual(SAMPLE_VALUES[i], values[i])) {
				fail("Decoded result differs");
			}
		}
	}
	private static boolean isEqual(Object a, Object b) {
		if (a == null) {
			return b == null;
		}
		return a.equals(b);
	}
}
