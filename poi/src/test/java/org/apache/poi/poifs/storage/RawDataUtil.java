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

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.util.HexRead;
import org.apache.poi.util.IOUtils;

/**
 * Test utility class.<br>
 *
 * Creates raw {@code byte[]} data from hex-dump String arrays.
 */
public final class RawDataUtil {

    private RawDataUtil() {}

    public static byte[] decode(String[] hexDataLines) {
        try (UnsynchronizedByteArrayOutputStream baos =
                UnsynchronizedByteArrayOutputStream.builder().setBufferSize(hexDataLines.length * 32 + 32).get()) {
            for (String hexDataLine : hexDataLines) {
                byte[] lineData = HexRead.readFromString(hexDataLine);
                baos.write(lineData, 0, lineData.length);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("problem decoding hex data", e);
        }
    }

    /**
     * Decompress previously gziped/base64ed data
     *
     * @param data the gzipped/base64ed data
     * @return the raw bytes
     * @throws IOException if you copy and pasted the data wrong
     */
    public static byte[] decompress(String data) throws IOException {
        byte[] base64Bytes = Base64.getDecoder().decode(data);
        try (
                InputStream is = UnsynchronizedByteArrayInputStream.builder().setByteArray(base64Bytes).get();
                GZIPInputStream gzis = new GZIPInputStream(is);
        ) {
            return IOUtils.toByteArray(gzis);
        }
    }

    /**
     * Compress raw data for test runs - usually called while debugging :)
     *
     * @param data the raw data
     * @return the gzipped/base64ed data as String
     * @throws IOException usually not ...
     */
    public static String compress(byte[] data) throws IOException {
        try (
                UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get();
                java.util.zip.GZIPOutputStream gz = new java.util.zip.GZIPOutputStream(bos)
        ) {
            gz.write(data);
            gz.finish();
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        }
    }
}
