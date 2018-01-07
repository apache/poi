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

package org.apache.poi.hemf.record;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.RecordFormatException;

/**
 * Container class for four subtypes of HemfCommentPublic: BeginGroup, EndGroup, MultiFormats
 * and Windows Metafile.
 */
@Internal
public class HemfCommentPublic  {

    private static final int MAX_RECORD_LENGTH = 1_000_000;


    /**
     * Stub, to be implemented
     */
    public static class BeginGroup extends AbstractHemfComment {

        public BeginGroup(byte[] rawBytes) {
            super(rawBytes);
        }

    }

    /**
     * Stub, to be implemented
     */
    public static class EndGroup extends AbstractHemfComment {

        public EndGroup(byte[] rawBytes) {
            super(rawBytes);
        }
    }

    public static class MultiFormats extends AbstractHemfComment {

        public MultiFormats(byte[] rawBytes) {
            super(rawBytes);
        }

        /**
         *
         * @return a list of HemfMultFormatsData
         */
        public List<HemfMultiFormatsData> getData() {

            byte[] rawBytes = getRawBytes();
            //note that raw bytes includes the public comment identifier
            int currentOffset = 4 + 16;//4 public comment identifier, 16 for outputrect
            long countFormats = LittleEndian.getUInt(rawBytes, currentOffset);
            currentOffset += LittleEndianConsts.INT_SIZE;
            List<EmrFormat> emrFormatList = new ArrayList<>();
            for (long i = 0; i < countFormats; i++) {
                emrFormatList.add(new EmrFormat(rawBytes, currentOffset));
                currentOffset += 4 * LittleEndianConsts.INT_SIZE;
            }
            List<HemfMultiFormatsData> list = new ArrayList<>();
            for (EmrFormat emrFormat : emrFormatList) {
                byte[] data = IOUtils.safelyAllocate(emrFormat.size, MAX_RECORD_LENGTH);
                System.arraycopy(rawBytes, emrFormat.offset-4, data, 0, emrFormat.size);
                list.add(new HemfMultiFormatsData(emrFormat.signature, emrFormat.version, data));
            }
            return list;
        }

        private static class EmrFormat {
            long signature;
            long version;
            int size;
            int offset;

            public EmrFormat(byte[] rawBytes, int currentOffset) {
                signature = LittleEndian.getUInt(rawBytes, currentOffset); currentOffset += LittleEndianConsts.INT_SIZE;
                version = LittleEndian.getUInt(rawBytes, currentOffset); currentOffset += LittleEndianConsts.INT_SIZE;
                //spec says this must be a 32bit "aligned" typo for "signed"?
                //realistically, this has to be an int...
                size = LittleEndian.getInt(rawBytes, currentOffset); currentOffset += LittleEndianConsts.INT_SIZE;
                //y, can be long, but realistically?
                offset = LittleEndian.getInt(rawBytes, currentOffset); currentOffset += LittleEndianConsts.INT_SIZE;
                if (size < 0) {
                    throw new RecordFormatException("size for emrformat must be > 0");
                }
                if (offset < 0) {
                    throw new RecordFormatException("offset for emrformat must be > 0");
                }
            }
        }
    }

    /**
     * Stub, to be implemented
     */
    public static class WindowsMetafile extends AbstractHemfComment {

        private final byte[] wmfBytes;
        public WindowsMetafile(byte[] rawBytes) {
            super(rawBytes);
            int offset = LittleEndianConsts.INT_SIZE;//public comment identifier
            int version = LittleEndian.getUShort(rawBytes, offset); offset += LittleEndianConsts.SHORT_SIZE;
            int reserved = LittleEndian.getUShort(rawBytes, offset); offset += LittleEndianConsts.SHORT_SIZE;
            offset += LittleEndianConsts.INT_SIZE; //checksum
            offset += LittleEndianConsts.INT_SIZE; //flags
            long winMetafileSizeLong = LittleEndian.getUInt(rawBytes, offset); offset += LittleEndianConsts.INT_SIZE;
            if (winMetafileSizeLong == 0L) {
                wmfBytes = new byte[0];
                return;
            }
            wmfBytes = IOUtils.safelyAllocate(winMetafileSizeLong, MAX_RECORD_LENGTH);
            System.arraycopy(rawBytes, offset, wmfBytes, 0, wmfBytes.length);
        }

        /**
         *
         * @return an InputStream for the embedded WMF file
         */
        public InputStream getWmfInputStream() {
            return new ByteArrayInputStream(wmfBytes);
        }
    }

    /**
     * This encapulates a single record stored within
     * a HemfCommentPublic.MultiFormats record.
     */
    public static class HemfMultiFormatsData {

        long signature;
        long version;
        byte[] data;

        public HemfMultiFormatsData(long signature, long version, byte[] data) {
            this.signature = signature;
            this.version = version;
            this.data = data;
        }

        public long getSignature() {
            return signature;
        }

        public long getVersion() {
            return version;
        }

        public byte[] getData() {
            return data;
        }
    }
}
