
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

import org.apache.poi.util.LittleEndian;

import java.util.ArrayList;

/**
 * Title:        Extended Static String Table<P>
 * Description: This record is used for a quick lookup into the SST record. This
 *              record breaks the SST table into a set of buckets. The offsets
 *              to these buckets within the SST record are kept as well as the
 *              position relative to the start of the SST record.
 * REFERENCE:  PG 313 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at apache dot org)
 * @version 2.0-pre
 * @see org.apache.poi.hssf.record.ExtSSTInfoSubRecord
 */

public class ExtSSTRecord
    extends Record
{
    public static final int DEFAULT_BUCKET_SIZE = 8;
    //Cant seem to find this documented but from the biffviewer it is clear that
    //Excel only records the indexes for the first 128 buckets.
    public static final int MAX_BUCKETS = 128;
    public final static short sid = 0xff;
    private short             field_1_strings_per_bucket = DEFAULT_BUCKET_SIZE;
    private ArrayList         field_2_sst_info;


    public ExtSSTRecord()
    {
        field_2_sst_info = new ArrayList();
    }

    public ExtSSTRecord(RecordInputStream in)
    {
        field_2_sst_info           = new ArrayList();
        field_1_strings_per_bucket = in.readShort();
        while (in.remaining() > 0) {
            ExtSSTInfoSubRecord rec = new ExtSSTInfoSubRecord(in);

            field_2_sst_info.add(rec);
        }
    }

    public void setNumStringsPerBucket(short numStrings)
    {
        field_1_strings_per_bucket = numStrings;
    }

    public void addInfoRecord(ExtSSTInfoSubRecord rec)
    {
        field_2_sst_info.add(rec);
    }

    public short getNumStringsPerBucket()
    {
        return field_1_strings_per_bucket;
    }

    public int getNumInfoRecords()
    {
        return field_2_sst_info.size();
    }

    public ExtSSTInfoSubRecord getInfoRecordAt(int elem)
    {
        return ( ExtSSTInfoSubRecord ) field_2_sst_info.get(elem);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[EXTSST]\n");
        buffer.append("    .dsst           = ")
            .append(Integer.toHexString(getNumStringsPerBucket()))
            .append("\n");
        buffer.append("    .numInfoRecords = ").append(getNumInfoRecords())
            .append("\n");
        for (int k = 0; k < getNumInfoRecords(); k++)
        {
            buffer.append("    .inforecord     = ").append(k).append("\n");
            buffer.append("    .streampos      = ")
                .append(Integer
                .toHexString(getInfoRecordAt(k).getStreamPos())).append("\n");
            buffer.append("    .sstoffset      = ")
                .append(Integer
                .toHexString(getInfoRecordAt(k).getBucketSSTOffset()))
                    .append("\n");
        }
        buffer.append("[/EXTSST]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));
        LittleEndian.putShort(data, 4 + offset, field_1_strings_per_bucket);
        int pos = 6;

        for (int k = 0; k < getNumInfoRecords(); k++)
        {
            ExtSSTInfoSubRecord rec = getInfoRecordAt(k);
            pos += rec.serialize(pos + offset, data);
        }
        return pos;
    }

    /** Returns the size of this record */
    public int getRecordSize()
    {
        return 6 + 8*getNumInfoRecords();
    }

    public static final int getNumberOfInfoRecsForStrings(int numStrings) {
      int infoRecs = (numStrings / DEFAULT_BUCKET_SIZE);
      if ((numStrings % DEFAULT_BUCKET_SIZE) != 0)
        infoRecs ++;
      //Excel seems to max out after 128 info records.
      //This isnt really documented anywhere...
      if (infoRecs > MAX_BUCKETS)
        infoRecs = MAX_BUCKETS;
      return infoRecs;
    }

    /** Given a number of strings (in the sst), returns the size of the extsst record*/
    public static final int getRecordSizeForStrings(int numStrings) {
      return 4 + 2 + (getNumberOfInfoRecsForStrings(numStrings) * 8);
    }

    public short getSid()
    {
        return sid;
    }

    public void setBucketOffsets( int[] bucketAbsoluteOffsets, int[] bucketRelativeOffsets )
    {
        this.field_2_sst_info = new ArrayList(bucketAbsoluteOffsets.length);
        for ( int i = 0; i < bucketAbsoluteOffsets.length; i++ )
        {
            ExtSSTInfoSubRecord r = new ExtSSTInfoSubRecord();
            r.setBucketRecordOffset((short)bucketRelativeOffsets[i]);
            r.setStreamPos(bucketAbsoluteOffsets[i]);
            field_2_sst_info.add(r);
        }
    }

}
