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

package org.apache.poi.hemf.record.emf;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import org.apache.poi.hemf.record.emfplus.HemfPlusRecord;
import org.apache.poi.hemf.record.emfplus.HemfPlusRecordIterator;
import org.apache.poi.hwmf.usermodel.HwmfPicture;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.RecordFormatException;

/**
 * Contains arbitrary data
 */
@Internal
public class HemfComment {
    private static final int MAX_RECORD_LENGTH = HwmfPicture.MAX_RECORD_LENGTH;

    public enum HemfCommentRecordType {
        emfGeneric(-1, EmfCommentDataGeneric::new, false),
        emfSpool(0x00000000, EmfCommentDataGeneric::new, false),
        emfPlus(0x2B464D45, EmfCommentDataPlus::new, false),
        emfPublic(0x43494447, null, false),
        emfBeginGroup(0x00000002, EmfCommentDataBeginGroup::new, true),
        emfEndGroup(0x00000003, EmfCommentDataEndGroup::new, true),
        emfMultiFormats(0x40000004, EmfCommentDataMultiformats::new, true),
        emfWMF(0x80000001, EmfCommentDataWMF::new, true),
        emfUnicodeString(0x00000040, EmfCommentDataUnicode::new, true),
        emfUnicodeEnd(0x00000080, EmfCommentDataUnicode::new, true)
        ;


        public final long id;
        public final Supplier<? extends EmfCommentData> constructor;
        public final boolean isEmfPublic;

        HemfCommentRecordType(long id, Supplier<? extends EmfCommentData> constructor, boolean isEmfPublic) {
            this.id = id;
            this.constructor = constructor;
            this.isEmfPublic = isEmfPublic;
        }

        public static HemfCommentRecordType getById(long id, boolean isEmfPublic) {
            for (HemfCommentRecordType wrt : values()) {
                if (wrt.id == id && wrt.isEmfPublic == isEmfPublic) return wrt;
            }
            return emfGeneric;
        }
    }

    public interface EmfCommentData {
        HemfCommentRecordType getCommentRecordType();

        long init(LittleEndianInputStream leis, long dataSize) throws IOException;
    }

    public static class EmfComment implements HemfRecord {
        private EmfCommentData data;

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.comment;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            int startIdx = leis.getReadIndex();
            data = new EmfCommentDataIterator(leis, (int)recordSize, true).next();
            return leis.getReadIndex()-startIdx;
        }

        public EmfCommentData getCommentData() {
            return data;
        }

        @Override
        public String toString() {
            return "{ data: "+data+" }";
        }
    }

    public static class EmfCommentDataIterator implements Iterator<EmfCommentData> {

        private final LittleEndianInputStream leis;
        private final int startIdx;
        private final int limit;
        private EmfCommentData currentRecord;
        /** is the caller the EmfComment */
        private final boolean emfParent;

        public EmfCommentDataIterator(LittleEndianInputStream leis, int limit, boolean emfParent) {
            this.leis = leis;
            this.limit = limit;
            this.emfParent = emfParent;
            startIdx = leis.getReadIndex();
            //queue the first non-header record
            currentRecord = _next();
        }

        @Override
        public boolean hasNext() {
            return currentRecord != null;
        }

        @Override
        public EmfCommentData next() {
            EmfCommentData toReturn = currentRecord;
            final boolean isEOF = (limit == -1 || leis.getReadIndex() >= startIdx+limit);
            // (currentRecord instanceof HemfPlusMisc.EmfEof)
            currentRecord = isEOF ? null : _next();
            return toReturn;
        }

        private EmfCommentData _next() {
            long type, recordSize;
            if (currentRecord == null && emfParent) {
                type = HemfRecordType.comment.id;
                recordSize = limit;
            } else {
                // A 32-bit unsigned integer from the RecordType enumeration that identifies this record
                // as a comment record. This value MUST be 0x00000046.
                try {
                    type = leis.readUInt();
                } catch (RuntimeException e) {
                    // EOF
                    return null;
                }
                assert(type == HemfRecordType.comment.id);
                // A 32-bit unsigned integer that specifies the size in bytes of this record in the
                // metafile. This value MUST be a multiple of 4 bytes.
                recordSize = leis.readUInt();
            }

            // A 32-bit unsigned integer that specifies the size, in bytes, of the CommentIdentifier and
            // CommentRecordParm fields in the RecordBuffer field that follows.
            // It MUST NOT include the size of itself or the size of the AlignmentPadding field, if present.
            long dataSize = leis.readUInt();

            try {
                leis.mark(2*LittleEndianConsts.INT_SIZE);
                // An optional, 32-bit unsigned integer that identifies the type of comment record.
                // See the preceding table for descriptions of these record types.
                // Valid comment identifier values are listed in the following table.
                //
                // If this field contains any other value, the comment record MUST be an EMR_COMMENT record
                final int commentIdentifier = (int)leis.readUInt();
                // A 32-bit unsigned integer that identifies the type of public comment record.
                final int publicCommentIdentifier = (int)leis.readUInt();

                final boolean isEmfPublic = (commentIdentifier == HemfCommentRecordType.emfPublic.id);
                leis.reset();

                final HemfCommentRecordType commentType = HemfCommentRecordType.getById
                    (isEmfPublic ? publicCommentIdentifier : commentIdentifier, isEmfPublic);
                assert(commentType != null);
                final EmfCommentData record = commentType.constructor.get();

                long readBytes = record.init(leis, dataSize);
                final int skipBytes = (int)(recordSize-4-readBytes);
                assert (skipBytes >= 0);
                leis.skipFully(skipBytes);

                return record;
            } catch (IOException e) {
                throw new RecordFormatException(e);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not supported");
        }

    }



    /**
     * Private data is unknown to EMF; it is meaningful only to applications that know the format of the
     * data and how to use it. EMR_COMMENT private data records MAY be ignored.
     */
    public static class EmfCommentDataGeneric implements EmfCommentData {
        private byte[] privateData;

        @Override
        public HemfCommentRecordType getCommentRecordType() {
            return HemfCommentRecordType.emfGeneric;
        }

        @Override
        public long init(LittleEndianInputStream leis, long dataSize) throws IOException {
            privateData = IOUtils.safelyAllocate(dataSize, MAX_RECORD_LENGTH);
            leis.readFully(privateData);
            return privateData.length;
        }

        @Override
        public String toString() {
            return "\""+new String(privateData, LocaleUtil.CHARSET_1252).replaceAll("\\p{Cntrl}", ".")+"\"";
        }
    }

    /** The EMR_COMMENT_EMFPLUS record contains embedded EMF+ records. */
    public static class EmfCommentDataPlus implements EmfCommentData {
        private final List<HemfPlusRecord> records = new ArrayList<>();

        @Override
        public HemfCommentRecordType getCommentRecordType() {
            return HemfCommentRecordType.emfPlus;
        }

        @Override
        public long init(final LittleEndianInputStream leis, final long dataSize)
        throws IOException {
            long startIdx = leis.getReadIndex();
            int commentIdentifier = leis.readInt();
            assert (commentIdentifier == HemfCommentRecordType.emfPlus.id);
            new HemfPlusRecordIterator(leis, (int)dataSize-LittleEndianConsts.INT_SIZE).forEachRemaining(records::add);
            return leis.getReadIndex()-startIdx;
        }

        public List<HemfPlusRecord> getRecords() {
            return Collections.unmodifiableList(records);
        }
    }

    public static class EmfCommentDataBeginGroup implements EmfCommentData {
        private final Rectangle2D bounds = new Rectangle2D.Double();
        private String description;

        @Override
        public HemfCommentRecordType getCommentRecordType() {
            return HemfCommentRecordType.emfBeginGroup;
        }

        @Override
        public long init(final LittleEndianInputStream leis, final long dataSize)
        throws IOException {
            final int startIdx = leis.getReadIndex();
            final int commentIdentifier = (int)leis.readUInt();
            assert(commentIdentifier == HemfCommentRecordType.emfPublic.id);
            final int publicCommentIdentifier = (int)leis.readUInt();
            assert(publicCommentIdentifier == HemfCommentRecordType.emfBeginGroup.id);
            HemfDraw.readRectL(leis, bounds);

            // The number of Unicode characters in the optional description string that follows.
            int nDescription = (int)leis.readUInt();

            byte[] buf = IOUtils.safelyAllocate(nDescription*2, MAX_RECORD_LENGTH);
            leis.readFully(buf);
            description = new String(buf, StandardCharsets.UTF_16LE);

            return leis.getReadIndex()-startIdx;
        }
    }

    public static class EmfCommentDataEndGroup implements EmfCommentData {
        @Override
        public HemfCommentRecordType getCommentRecordType() {
            return HemfCommentRecordType.emfEndGroup;
        }

        @Override
        public long init(final LittleEndianInputStream leis, final long dataSize)
        throws IOException {
            final int startIdx = leis.getReadIndex();
            final int commentIdentifier = (int)leis.readUInt();
            assert(commentIdentifier == HemfCommentRecordType.emfPublic.id);
            final int publicCommentIdentifier = (int)leis.readUInt();
            assert(publicCommentIdentifier == HemfCommentRecordType.emfEndGroup.id);
            return leis.getReadIndex()-startIdx;
        }
    }

    public static class EmfCommentDataMultiformats implements EmfCommentData {
        private final Rectangle2D bounds = new Rectangle2D.Double();
        private final List<EmfCommentDataFormat> formats = new ArrayList<>();

        @Override
        public HemfCommentRecordType getCommentRecordType() {
            return HemfCommentRecordType.emfMultiFormats;
        }

        @Override
        public long init(final LittleEndianInputStream leis, final long dataSize)
                throws IOException {
            final int startIdx = leis.getReadIndex();
            final int commentIdentifier = (int)leis.readUInt();
            assert(commentIdentifier == HemfCommentRecordType.emfPublic.id);
            final int publicCommentIdentifier = (int)leis.readUInt();
            assert(publicCommentIdentifier == HemfCommentRecordType.emfMultiFormats.id);
            HemfDraw.readRectL(leis, bounds);

            // A 32-bit unsigned integer that specifies the number of graphics formats contained in this record.
            int countFormats = (int)leis.readUInt();
            for (int i=0; i<countFormats; i++) {
                EmfCommentDataFormat fmt = new EmfCommentDataFormat();
                long readBytes = fmt.init(leis, dataSize, startIdx);
                formats.add(fmt);
                if (readBytes == 0) {
                    // binary data is appended without DataFormat header
                    break;
                }
            }

            for (EmfCommentDataFormat fmt : formats) {
                int skip = fmt.offData-(leis.getReadIndex()-startIdx);
                leis.skipFully(skip);
                fmt.rawData = IOUtils.safelyAllocate(fmt.sizeData, MAX_RECORD_LENGTH);
                int readBytes = leis.read(fmt.rawData);
                if (readBytes < fmt.sizeData) {
                    // EOF
                    break;
                }
            }

            return leis.getReadIndex()-startIdx;
        }

        public List<EmfCommentDataFormat> getFormats() {
            return Collections.unmodifiableList(formats);
        }
    }

    public enum EmfFormatSignature {
        ENHMETA_SIGNATURE(0x464D4520),
        EPS_SIGNATURE(0x46535045);

        int id;

        EmfFormatSignature(int id) {
            this.id = id;
        }

        public static EmfFormatSignature getById(int id) {
            for (EmfFormatSignature wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }

    }

    public static class EmfCommentDataFormat {
        private EmfFormatSignature signature;
        private int version;
        private int sizeData;
        private int offData;
        private byte[] rawData;

        public long init(final LittleEndianInputStream leis, final long dataSize, long startIdx) throws IOException {
            // A 32-bit unsigned integer that specifies the format of the image data.
            signature = EmfFormatSignature.getById(leis.readInt());

            // A 32-bit unsigned integer that specifies the format version number.
            // If the Signature field specifies encapsulated PostScript (EPS), this value MUST be 0x00000001;
            // otherwise, this value MUST be ignored.
            version = leis.readInt();

            // A 32-bit unsigned integer that specifies the size of the data in bytes.
            sizeData = leis.readInt();

            // A 32-bit unsigned integer that specifies the offset to the data from the start
            // of the identifier field in an EMR_COMMENT_PUBLIC record. The offset MUST be 32-bit aligned.
            offData = leis.readInt();
            if (sizeData < 0) {
                throw new RecordFormatException("size for emrformat must be > 0");
            }
            if (offData < 0) {
                throw new RecordFormatException("offset for emrformat must be > 0");
            }

            return 4*LittleEndianConsts.INT_SIZE;
        }

        public byte[] getRawData() {
            return rawData;
        }
    }

    public static class EmfCommentDataWMF implements EmfCommentData {
        private final Rectangle2D bounds = new Rectangle2D.Double();
        private final List<EmfCommentDataFormat> formats = new ArrayList<>();

        @Override
        public HemfCommentRecordType getCommentRecordType() {
            return HemfCommentRecordType.emfWMF;
        }

        @Override
        public long init(final LittleEndianInputStream leis, final long dataSize) throws IOException {
            final int startIdx = leis.getReadIndex();
            final int commentIdentifier = (int)leis.readUInt();
            assert(commentIdentifier == HemfCommentRecordType.emfPublic.id);
            final int publicCommentIdentifier = (int)leis.readUInt();
            assert(publicCommentIdentifier == HemfCommentRecordType.emfWMF.id);

            // A 16-bit unsigned integer that specifies the WMF metafile version in terms
            //of support for device-independent bitmaps (DIBs)
            int version = leis.readUShort();

            // A 16-bit value that MUST be 0x0000 and MUST be ignored.
            leis.skipFully(LittleEndianConsts.SHORT_SIZE);

            // A 32-bit unsigned integer that specifies the checksum for this record.
            int checksum = leis.readInt();

            // A 32-bit value that MUST be 0x00000000 and MUST be ignored.
            int flags = leis.readInt();

            // A 32-bit unsigned integer that specifies the size, in bytes, of the
            // WMF metafile in the WinMetafile field.
            int winMetafileSize = (int)leis.readUInt();

            byte[] winMetafile = IOUtils.safelyAllocate(winMetafileSize, MAX_RECORD_LENGTH);
            // some emf comments are truncated, so we don't use readFully here
            leis.read(winMetafile);

            return leis.getReadIndex()-startIdx;
        }
    }

    public static class EmfCommentDataUnicode implements EmfCommentData {
        private final Rectangle2D bounds = new Rectangle2D.Double();
        private final List<EmfCommentDataFormat> formats = new ArrayList<>();

        @Override
        public HemfCommentRecordType getCommentRecordType() {
            return HemfCommentRecordType.emfUnicodeString;
        }

        @Override
        public long init(final LittleEndianInputStream leis, final long dataSize)
                throws IOException {
            throw new RecordFormatException("UNICODE_STRING/UNICODE_END values are reserved in CommentPublic records");
        }
    }
}
