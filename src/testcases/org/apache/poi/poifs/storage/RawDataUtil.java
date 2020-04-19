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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

import org.apache.poi.util.HexRead;
import org.apache.poi.util.IOUtils;

/**
 * Test utility class.<br>
 *
 * Creates raw <code>byte[]</code> data from hex-dump String arrays.
 */
public final class RawDataUtil {

	private RawDataUtil() {}

	public static byte[] decode(String[] hexDataLines) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(hexDataLines.length * 32 + 32);

		for (String hexDataLine : hexDataLines) {
			byte[] lineData = HexRead.readFromString(hexDataLine);
			baos.write(lineData, 0, lineData.length);
		}
		return baos.toByteArray();
	}

    /**
     * Decompress previously gziped/base64ed data
     *
     * @param data the gziped/base64ed data
     * @return the raw bytes
     * @throws IOException if you copy and pasted the data wrong
     */
    public static byte[] decompress(String data) throws IOException {
        byte[] base64Bytes = Base64.getDecoder().decode(data);
        return IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(base64Bytes)));
    }

    /**
     * Compress raw data for test runs - usually called while debugging :)
     *
     * @param data the raw data
     * @return the gziped/base64ed data as String
     * @throws IOException usually not ...
     */
    public static String compress(byte[] data) throws IOException {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.util.zip.GZIPOutputStream gz = new java.util.zip.GZIPOutputStream(bos);
        gz.write(data);
        gz.finish();
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }
}
