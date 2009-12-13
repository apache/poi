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
package org.apache.poi.poifs.storage;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.HexRead;

/**
 * Test utility class.<br/>
 *
 * Creates raw <code>byte[]</code> data from hex-dump String arrays.
 *
 * @author Josh Micich
 */
public final class RawDataUtil {

	public static byte[] decode(String[] hexDataLines) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(hexDataLines.length * 32 + 32);

		for (int i = 0; i < hexDataLines.length; i++) {
			byte[] lineData = HexRead.readFromString(hexDataLines[i]);
			baos.write(lineData, 0, lineData.length);
		}
		return baos.toByteArray();
	}

	/**
	 * Development time utility method.<br/>
	 * Transforms a byte array into hex-dump String lines in java source code format.
	 */
	public static void dumpData(byte[] data) {
		int i=0;
		System.out.println("String[] hexDataLines = {");
		System.out.print("\t\"");
		while(true) {
			char[] cc = HexDump.byteToHex(data[i]);
			System.out.print(cc[2]);
			System.out.print(cc[3]);
			i++;
			if (i>=data.length) {
				break;
			}
			if (i % 32 == 0) {
				System.out.println("\",");
				System.out.print("\t\"");
			} else {
				System.out.print(" ");
			}
		}
		System.out.println("\",");
		System.out.println("};");
	}

	/**
	 * Development time utility method.<br/>
	 * Confirms that the specified byte array is equivalent to the hex-dump String lines.
	 */
	public static void confirmEqual(byte[] expected, String[] hexDataLines) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(hexDataLines.length * 32 + 32);

		for (int i = 0; i < hexDataLines.length; i++) {
			byte[] lineData = HexRead.readFromString(hexDataLines[i]);
			baos.write(lineData, 0, lineData.length);
		}
		if (!Arrays.equals(expected, baos.toByteArray())) {
			throw new RuntimeException("different");
		}
	}
}
