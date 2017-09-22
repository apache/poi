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

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.RecordFormatException;

/**
 * This is the outer comment record that is recognized
 * by the initial parse by {@link HemfRecordType#comment}.
 * However, there are four types of comment: EMR_COMMENT,
 * EMR_COMMENT_EMFPLUS, EMR_COMMENT_EMFSPOOL, and EMF_COMMENT_PUBLIC.
 * To get the underlying comment, call {@link #getComment()}.
 *
 */
@Internal
public class HemfCommentRecord implements HemfRecord {
    private static final int MAX_RECORD_LENGTH = 1_000_000;

    public final static long COMMENT_EMFSPOOL = 0x00000000;
    public final static long COMMENT_EMFPLUS = 0x2B464D45;
    public final static long COMMENT_PUBLIC = 0x43494447;


    private AbstractHemfComment comment;
    @Override
    public HemfRecordType getRecordType() {
        return HemfRecordType.comment;
    }

    @Override
    public long init(LittleEndianInputStream leis, long recordId, long recordSize) throws IOException {
        long dataSize = leis.readUInt();  recordSize -= LittleEndian.INT_SIZE;

        byte[] optionalCommentIndentifierBuffer = new byte[4];
        leis.readFully(optionalCommentIndentifierBuffer);
        dataSize = dataSize-LittleEndian.INT_SIZE; //size minus the first int which could be a comment identifier
        recordSize -= LittleEndian.INT_SIZE;
        long optionalCommentIdentifier = LittleEndian.getInt(optionalCommentIndentifierBuffer) & 0x00FFFFFFFFL;
        if (optionalCommentIdentifier == COMMENT_EMFSPOOL) {
            comment = new HemfCommentEMFSpool(readToByteArray(leis, dataSize, recordSize));
        } else if (optionalCommentIdentifier == COMMENT_EMFPLUS) {
            comment = new HemfCommentEMFPlus(readToByteArray(leis, dataSize, recordSize));
        } else if (optionalCommentIdentifier == COMMENT_PUBLIC) {
            comment = CommentPublicParser.parse(readToByteArray(leis, dataSize, recordSize));
        } else {
            comment = new HemfComment(readToByteArray(optionalCommentIndentifierBuffer, leis, dataSize, recordSize));
        }

        return recordSize;
    }

    //this prepends the initial "int" which turned out NOT to be
    //a signifier of emfplus, spool, public.
    private byte[] readToByteArray(byte[] initialBytes, LittleEndianInputStream leis,
                                   long remainingDataSize, long remainingRecordSize) throws IOException {
        if (remainingDataSize > Integer.MAX_VALUE) {
            throw new RecordFormatException("Data size can't be > Integer.MAX_VALUE");
        }

        if (remainingRecordSize > Integer.MAX_VALUE) {
            throw new RecordFormatException("Record size can't be > Integer.MAX_VALUE");
        }
        if (remainingRecordSize == 0) {
            return new byte[0];
        }

        int dataSize = (int)remainingDataSize;
        int recordSize = (int)remainingRecordSize;
        byte[] arr = IOUtils.safelyAllocate(dataSize+initialBytes.length, MAX_RECORD_LENGTH);
        System.arraycopy(initialBytes,0,arr, 0, initialBytes.length);
        long read = IOUtils.readFully(leis, arr, initialBytes.length, dataSize);
        if (read != dataSize) {
            throw new RecordFormatException("InputStream ended before full record could be read");
        }
        long toSkip = recordSize-dataSize;
        long skipped = IOUtils.skipFully(leis, toSkip);
        if (toSkip != skipped) {
            throw new RecordFormatException("InputStream ended before full record could be read");
        }

        return arr;
    }

    private byte[] readToByteArray(LittleEndianInputStream leis, long dataSize, long recordSize) throws IOException {

        if (recordSize == 0) {
            return new byte[0];
        }

        byte[] arr = IOUtils.safelyAllocate(dataSize, MAX_RECORD_LENGTH);

        long read = IOUtils.readFully(leis, arr);
        if (read != dataSize) {
            throw new RecordFormatException("InputStream ended before full record could be read");
        }
        long toSkip = recordSize-dataSize;
        long skipped = IOUtils.skipFully(leis, recordSize-dataSize);
        if (toSkip != skipped) {
            throw new RecordFormatException("InputStream ended before full record could be read");
        }
        return arr;
    }

    public AbstractHemfComment getComment() {
        return comment;
    }

    private static class CommentPublicParser {
        private static final long WINDOWS_METAFILE = 0x80000001L; //wmf
        private static final long BEGINGROUP = 0x00000002; //beginning of a group of drawing records
        private static final long ENDGROUP = 0x00000003; //end of a group of drawing records
        private static final long MULTIFORMATS = 0x40000004; //allows multiple definitions of an image, including encapsulated postscript
        private static final long UNICODE_STRING = 0x00000040; //reserved. must not be used
        private static final long UNICODE_END = 0x00000080; //reserved, must not be used

        private static AbstractHemfComment parse(byte[] bytes) {
            long publicCommentIdentifier = LittleEndian.getUInt(bytes, 0);
            if (publicCommentIdentifier == WINDOWS_METAFILE) {
                return new HemfCommentPublic.WindowsMetafile(bytes);
            } else if (publicCommentIdentifier == BEGINGROUP) {
                return new HemfCommentPublic.BeginGroup(bytes);
            } else if (publicCommentIdentifier == ENDGROUP) {
                return new HemfCommentPublic.EndGroup(bytes);
            } else if (publicCommentIdentifier == MULTIFORMATS) {
                return new HemfCommentPublic.MultiFormats(bytes);
            } else if (publicCommentIdentifier == UNICODE_STRING || publicCommentIdentifier == UNICODE_END) {
                throw new RuntimeException("UNICODE_STRING/UNICODE_END values are reserved in CommentPublic records");
            }
            throw new RuntimeException("Unrecognized public comment type:" +publicCommentIdentifier + " ; " + WINDOWS_METAFILE);
        }
    }
}
