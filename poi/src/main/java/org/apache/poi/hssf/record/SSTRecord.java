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

import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.hssf.record.common.UnicodeString;
import org.apache.poi.hssf.record.cont.ContinuableRecord;
import org.apache.poi.hssf.record.cont.ContinuableRecordOutput;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IntMapper;

/**
 * Static String Table Record (0x00FC)<p>
 *
 * This holds all the strings for LabelSSTRecords.
 *
 * @see org.apache.poi.hssf.record.LabelSSTRecord
 * @see org.apache.poi.hssf.record.ContinueRecord
 */
public final class SSTRecord extends ContinuableRecord {
    public static final short sid = 0x00FC;

    private static final UnicodeString EMPTY_STRING = new UnicodeString("");

    /**
     * union of strings in the SST and EXTSST
     */
    private int field_1_num_strings;

    /**
     * according to docs ONLY SST
     */
    private int field_2_num_unique_strings;
    private IntMapper<UnicodeString> field_3_strings;

    private SSTDeserializer deserializer;

    /**
     * Offsets from the beginning of the SST record (even across continuations)
     */
    private int[] bucketAbsoluteOffsets;
    /**
     * Offsets relative the start of the current SST or continue record
     */
    private int[] bucketRelativeOffsets;

    public SSTRecord() {
        field_1_num_strings = 0;
        field_2_num_unique_strings = 0;
        field_3_strings = new IntMapper<>();
        deserializer = new SSTDeserializer(field_3_strings);
    }

    public SSTRecord(SSTRecord other) {
        super(other);
        field_1_num_strings = other.field_1_num_strings;
        field_2_num_unique_strings = other.field_2_num_unique_strings;
        field_3_strings = other.field_3_strings.copy();
        deserializer = new SSTDeserializer(field_3_strings);
        bucketAbsoluteOffsets = (other.bucketAbsoluteOffsets == null) ? null : other.bucketAbsoluteOffsets.clone();
        bucketRelativeOffsets = (other.bucketRelativeOffsets == null) ? null : other.bucketRelativeOffsets.clone();
    }

    /**
     * Add a string.
     *
     * @param string string to be added
     *
     * @return the index of that string in the table
     */
    public int addString(UnicodeString string)
    {
        field_1_num_strings++;
        UnicodeString ucs = ( string == null ) ? EMPTY_STRING
                : string;
        int rval;
        int index = field_3_strings.getIndex(ucs);

        if ( index != -1 ) {
            rval = index;
        } else {
            // This is a new string -- we didn't see it among the
            // strings we've already collected
            rval = field_3_strings.size();
            field_2_num_unique_strings++;
            SSTDeserializer.addToStringTable( field_3_strings, ucs );
        }
        return rval;
    }

    /**
     * @return number of strings
     */
    public int getNumStrings()
    {
        return field_1_num_strings;
    }

    /**
     * @return number of unique strings
     */
    public int getNumUniqueStrings()
    {
        return field_2_num_unique_strings;
    }


    /**
     * Get a particular string by its index
     *
     * @param id index into the array of strings
     *
     * @return the desired string
     */
    public UnicodeString getString(int id ) {
        return field_3_strings.get( id );
    }

    public short getSid() {
        return sid;
    }

    /**
     * Fill the fields from the data
     * <P>
     * The data consists of sets of string data. This string data is
     * arranged as follows:
     * </P>
     * <pre>
     * short  string_length;   // length of string data
     * byte   string_flag;     // flag specifying special string
     *                         // handling
     * short  run_count;       // optional count of formatting runs
     * int    extend_length;   // optional extension length
     * char[] string_data;     // string data, can be byte[] or
     *                         // short[] (length of array is
     *                         // string_length)
     * int[]  formatting_runs; // optional formatting runs (length of
     *                         // array is run_count)
     * byte[] extension;       // optional extension (length of array
     *                         // is extend_length)
     * </pre>
     * <P>
     * The string_flag is bit mapped as follows:
     * </P>
     * <P>
     * <TABLE summary="string_flag mapping">
     *   <TR>
     *      <TH>Bit number</TH>
     *      <TH>Meaning if 0</TH>
     *      <TH>Meaning if 1</TH>
     *   <TR>
     *   <TR>
     *      <TD>0</TD>
     *      <TD>string_data is byte[]</TD>
     *      <TD>string_data is short[]</TD>
     *   <TR>
     *   <TR>
     *      <TD>1</TD>
     *      <TD>Should always be 0</TD>
     *      <TD>string_flag is defective</TD>
     *   <TR>
     *   <TR>
     *      <TD>2</TD>
     *      <TD>extension is not included</TD>
     *      <TD>extension is included</TD>
     *   <TR>
     *   <TR>
     *      <TD>3</TD>
     *      <TD>formatting run data is not included</TD>
     *      <TD>formatting run data is included</TD>
     *   <TR>
     *   <TR>
     *      <TD>4</TD>
     *      <TD>Should always be 0</TD>
     *      <TD>string_flag is defective</TD>
     *   <TR>
     *   <TR>
     *      <TD>5</TD>
     *      <TD>Should always be 0</TD>
     *      <TD>string_flag is defective</TD>
     *   <TR>
     *   <TR>
     *      <TD>6</TD>
     *      <TD>Should always be 0</TD>
     *      <TD>string_flag is defective</TD>
     *   <TR>
     *   <TR>
     *      <TD>7</TD>
     *      <TD>Should always be 0</TD>
     *      <TD>string_flag is defective</TD>
     *   <TR>
     * </TABLE>
     * <P>
     * We can handle eating the overhead associated with bits 2 or 3
     * (or both) being set, but we have no idea what to do with the
     * associated data. The UnicodeString class can handle the byte[]
     * vs short[] nature of the actual string data
     *
     * @param in the RecordInputStream to read the record from
     */
    public SSTRecord(RecordInputStream in) {
        // this method is ALWAYS called after construction -- using
        // the nontrivial constructor, of course -- so this is where
        // we initialize our fields
        field_1_num_strings = in.readInt();
        field_2_num_unique_strings = in.readInt();
        field_3_strings = new IntMapper<>();

        deserializer = new SSTDeserializer(field_3_strings);
        // Bug 57456: some Excel Sheets send 0 as field=1, but have some random number in field_2,
        // we should not try to read the strings in this case.
        if(field_1_num_strings == 0) {
            field_2_num_unique_strings = 0;
            return;
        }
        deserializer.manufactureStrings( field_2_num_unique_strings, in );
    }


    /**
     * @return an iterator of the strings we hold. All instances are
     *         UnicodeStrings
     */
    Iterator<UnicodeString> getStrings()
    {
        return field_3_strings.iterator();
    }

    /**
     * @return count of the strings we hold.
     */
    int countStrings() {
        return field_3_strings.size();
    }

    protected void serialize(ContinuableRecordOutput out) {
        SSTSerializer serializer = new SSTSerializer(field_3_strings, getNumStrings(), getNumUniqueStrings() );
        serializer.serialize(out);
        bucketAbsoluteOffsets = serializer.getBucketAbsoluteOffsets();
        bucketRelativeOffsets = serializer.getBucketRelativeOffsets();
    }

    /**
     * Creates an extended string record based on the current contents of
     * the current SST record.  The offset within the stream to the SST record
     * is required because the extended string record points directly to the
     * strings in the SST record.
     * <p>
     * NOTE: THIS FUNCTION MUST ONLY BE CALLED AFTER THE SST RECORD HAS BEEN
     *       SERIALIZED.
     *
     * @param sstOffset     The offset in the stream to the start of the
     *                      SST record.
     * @return  The new SST record.
     */
    public ExtSSTRecord createExtSSTRecord(int sstOffset) {
        if (bucketAbsoluteOffsets == null || bucketRelativeOffsets == null) {
            throw new IllegalStateException("SST record has not yet been serialized.");
        }

        ExtSSTRecord extSST = new ExtSSTRecord();
        extSST.setNumStringsPerBucket((short)8);
        int[] absoluteOffsets = bucketAbsoluteOffsets.clone();
        int[] relativeOffsets = bucketRelativeOffsets.clone();
        for ( int i = 0; i < absoluteOffsets.length; i++ ) {
            absoluteOffsets[i] += sstOffset;
        }
        extSST.setBucketOffsets(absoluteOffsets, relativeOffsets);
        return extSST;
    }

    /**
     * Calculates the size in bytes of the EXTSST record as it would be if the
     * record was serialized.
     *
     * @return  The size of the ExtSST record in bytes.
     */
    public int calcExtSSTRecordSize() {
      return ExtSSTRecord.getRecordSizeForStrings(field_3_strings.size());
    }

    @Override
    public SSTRecord copy() {
        return new SSTRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.SST;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "numStrings", this::getNumStrings,
            "numUniqueStrings", this::getNumUniqueStrings,
            "strings", () -> field_3_strings.getElements(),
            "bucketAbsoluteOffsets", () -> bucketAbsoluteOffsets,
            "bucketRelativeOffsets", () -> bucketRelativeOffsets
        );
    }
}
