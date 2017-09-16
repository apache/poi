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

package org.apache.poi.hemf.extractor;


import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.hemf.record.HemfHeader;
import org.apache.poi.hemf.record.HemfRecord;
import org.apache.poi.hemf.record.HemfRecordType;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.RecordFormatException;

/**
 * Read-only EMF extractor.  Lots remain
 */
@Internal
public class HemfExtractor implements Iterable<HemfRecord> {

    private HemfHeader header;
    private final LittleEndianInputStream stream;

    public HemfExtractor(InputStream is) throws IOException {
        stream = new LittleEndianInputStream(is);
        header = new HemfHeader();
        long recordId = stream.readUInt();
        long recordSize = stream.readUInt();

        header = new HemfHeader();
        header.init(stream, recordId, recordSize-8);
    }

    @Override
    public Iterator<HemfRecord> iterator() {
        return new HemfRecordIterator();
    }

    public HemfHeader getHeader() {
        return header;
    }

    private class HemfRecordIterator implements Iterator<HemfRecord> {

        private HemfRecord currentRecord;

        HemfRecordIterator() {
            //queue the first non-header record
            currentRecord = _next();
        }

        @Override
        public boolean hasNext() {
            return currentRecord != null;
        }

        @Override
        public HemfRecord next() {
            HemfRecord toReturn = currentRecord;
            currentRecord = _next();
            return toReturn;
        }

        private HemfRecord _next() {
            if (currentRecord != null && currentRecord.getRecordType().equals(HemfRecordType.eof)) {
                return null;
            }
            long recordId = stream.readUInt();
            long recordSize = stream.readUInt();

            HemfRecord record = null;
            HemfRecordType type = HemfRecordType.getById(recordId);
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
            try {
                record.init(stream, recordId, recordSize-8);
            } catch (IOException e) {
                throw new RecordFormatException(e);
            }

            return record;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not supported");
        }

    }
}
