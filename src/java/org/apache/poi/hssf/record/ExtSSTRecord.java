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

import org.apache.poi.hssf.record.cont.ContinuableRecord;
import org.apache.poi.hssf.record.cont.ContinuableRecordOutput;
import org.apache.poi.util.LittleEndianOutput;

import java.util.ArrayList;

/**
 * Title:        Extended Static String Table (0x00FF)<p>
 * Description: This record is used for a quick lookup into the SST record. This
 *              record breaks the SST table into a set of buckets. The offsets
 *              to these buckets within the SST record are kept as well as the
 *              position relative to the start of the SST record.<p>
 * REFERENCE:  PG 313 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)
 */
public final class ExtSSTRecord extends ContinuableRecord {
    public final static short sid = 0x00FF;
    public static final int DEFAULT_BUCKET_SIZE = 8;
    //Can't seem to find this documented but from the biffviewer it is clear that
    //Excel only records the indexes for the first 128 buckets.
    public static final int MAX_BUCKETS = 128;
    
    
    public static final class InfoSubRecord {
    	public static final int ENCODED_SIZE = 8;
        private int field_1_stream_pos;          // stream pointer to the SST record
        private int field_2_bucket_sst_offset;   // don't really understand this yet.
        /** unused - supposed to be zero */
        private short field_3_zero;

        /**
         * Creates new ExtSSTInfoSubRecord
         * 
         * @param streamPos stream pointer to the SST record
         * @param bucketSstOffset ... don't really understand this yet
         */
        public InfoSubRecord(int streamPos, int bucketSstOffset) {
            field_1_stream_pos        = streamPos;
            field_2_bucket_sst_offset = bucketSstOffset;
        }

        public InfoSubRecord(RecordInputStream in)
        {
            field_1_stream_pos        = in.readInt();
            field_2_bucket_sst_offset = in.readShort();
            field_3_zero              = in.readShort();
        }

        public int getStreamPos() {
            return field_1_stream_pos;
        }

        public int getBucketSSTOffset() {
            return field_2_bucket_sst_offset;
        }

        public void serialize(LittleEndianOutput out) {
            out.writeInt(field_1_stream_pos);
            out.writeShort(field_2_bucket_sst_offset);
            out.writeShort(field_3_zero);
        }
    }
    
    
    private short _stringsPerBucket;
    private InfoSubRecord[] _sstInfos;


    public ExtSSTRecord() {
    	_stringsPerBucket = DEFAULT_BUCKET_SIZE;
        _sstInfos = new InfoSubRecord[0];
    }

    public ExtSSTRecord(RecordInputStream in) {
        _stringsPerBucket = in.readShort();

        int nInfos = in.remaining() / InfoSubRecord.ENCODED_SIZE;
        ArrayList<InfoSubRecord> lst = new ArrayList<>(nInfos);

        while (in.available() > 0) {
            InfoSubRecord info = new InfoSubRecord(in);
            lst.add(info);

            if(in.available() == 0 && in.hasNextRecord() && in.getNextSid() == ContinueRecord.sid) {
                in.nextRecord();
            }
        }
        _sstInfos = lst.toArray(new InfoSubRecord[lst.size()]);
    }

    public void setNumStringsPerBucket(short numStrings) {
        _stringsPerBucket = numStrings;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[EXTSST]\n");
        buffer.append("    .dsst           = ")
            .append(Integer.toHexString(_stringsPerBucket))
            .append("\n");
        buffer.append("    .numInfoRecords = ").append(_sstInfos.length)
            .append("\n");
        for (int k = 0; k < _sstInfos.length; k++)
        {
            buffer.append("    .inforecord     = ").append(k).append("\n");
            buffer.append("    .streampos      = ")
                .append(Integer
                .toHexString(_sstInfos[k].getStreamPos())).append("\n");
            buffer.append("    .sstoffset      = ")
                .append(Integer
                .toHexString(_sstInfos[k].getBucketSSTOffset()))
                    .append("\n");
        }
        buffer.append("[/EXTSST]\n");
        return buffer.toString();
    }

    public void serialize(ContinuableRecordOutput out) {
        out.writeShort(_stringsPerBucket);
        for (int k = 0; k < _sstInfos.length; k++) {
            _sstInfos[k].serialize(out);
        }
    }
    protected int getDataSize() {
    	return 2 + InfoSubRecord.ENCODED_SIZE*_sstInfos.length;
    }

    protected InfoSubRecord[] getInfoSubRecords() {
        return _sstInfos;
    }

    public static int getNumberOfInfoRecsForStrings(int numStrings) {
      int infoRecs = (numStrings / DEFAULT_BUCKET_SIZE);
      if ((numStrings % DEFAULT_BUCKET_SIZE) != 0)
        infoRecs ++;
      //Excel seems to max out after 128 info records.
      //This isn't really documented anywhere...
      if (infoRecs > MAX_BUCKETS)
        infoRecs = MAX_BUCKETS;
      return infoRecs;
    }

    /**
     * Given a number of strings (in the sst), returns the size of the extsst record
     * 
     * @param numStrings the number of strings
     * 
     * @return the size of the extsst record
     */
    public static int getRecordSizeForStrings(int numStrings) {
        return 4 + 2 + getNumberOfInfoRecsForStrings(numStrings) * 8;
    }

    public short getSid() {
        return sid;
    }

    public void setBucketOffsets(int[] bucketAbsoluteOffsets, int[] bucketRelativeOffsets) {
    	// TODO - replace no-arg constructor with this logic
        _sstInfos = new InfoSubRecord[bucketAbsoluteOffsets.length];
        for (int i = 0; i < bucketAbsoluteOffsets.length; i++) {
            _sstInfos[i] = new InfoSubRecord(bucketAbsoluteOffsets[i], bucketRelativeOffsets[i]);
        }
    }
}
