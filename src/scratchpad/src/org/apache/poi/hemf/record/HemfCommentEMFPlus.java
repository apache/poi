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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hemf.hemfplus.record.HemfPlusRecord;
import org.apache.poi.hemf.hemfplus.record.HemfPlusRecordType;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.RecordFormatException;

/**
 * An HemfCommentEMFPlus may contain one or more EMFPlus records
 */
@Internal
public class HemfCommentEMFPlus extends AbstractHemfComment {

    private static final int MAX_RECORD_LENGTH = 1_000_000;


    long dataSize;
    public HemfCommentEMFPlus(byte[] rawBytes) {
        //these rawBytes contain only the EMFPlusRecord(s?)
        //the EmfComment type, size, datasize and comment identifier have all been stripped.
        //The EmfPlus type, flags, size, data size should start at rawBytes[0]
        super(rawBytes);

    }

    public List<HemfPlusRecord> getRecords() {
        return HemfPlusParser.parse(getRawBytes());
    }

    private static class HemfPlusParser {

        public static List<HemfPlusRecord> parse(byte[] bytes) {
            List<HemfPlusRecord> records = new ArrayList<>();
            int offset = 0;
            while (offset < bytes.length) {
                if (offset + 12 > bytes.length) {
                    //if header will go beyond bytes, stop now
                    //TODO: log or throw
                    break;
                }
                int type = LittleEndian.getUShort(bytes, offset); offset += LittleEndian.SHORT_SIZE;
                int flags = LittleEndian.getUShort(bytes, offset); offset += LittleEndian.SHORT_SIZE;
                long sizeLong = LittleEndian.getUInt(bytes, offset); offset += LittleEndian.INT_SIZE;
                if (sizeLong >= Integer.MAX_VALUE) {
                    throw new RecordFormatException("size of emf record >= Integer.MAX_VALUE");
                }
                int size = (int)sizeLong;
                long dataSizeLong = LittleEndian.getUInt(bytes, offset); offset += LittleEndian.INT_SIZE;
                if (dataSizeLong >= Integer.MAX_VALUE) {
                    throw new RuntimeException("data size of emfplus record cannot be >= Integer.MAX_VALUE");
                }
                int dataSize = (int)dataSizeLong;
                if (dataSize + offset > bytes.length) {
                    //TODO: log or throw?
                    break;
                }
                HemfPlusRecord record = buildRecord(type, flags, dataSize, offset, bytes);
                records.add(record);
                offset += dataSize;
            }
            return records;
        }

        private static HemfPlusRecord buildRecord(int recordId, int flags, int size, int offset, byte[] bytes) {
            HemfPlusRecord record = null;
            HemfPlusRecordType type = HemfPlusRecordType.getById(recordId);
            if (type == null) {
                throw new RuntimeException("Undefined record of type:"+recordId);
            }
            try {
                record = type.clazz.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            byte[] dataBytes = IOUtils.safelyAllocate(size, MAX_RECORD_LENGTH);
            System.arraycopy(bytes, offset, dataBytes, 0, size);
            try {
                record.init(dataBytes, recordId, flags);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return record;

        }
    }
}
