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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.RecordFormatException;

/**
 * Title:  Record Factory<p>
 * Description:  Takes a stream and outputs an array of Record objects.
 *
 * @see org.apache.poi.hssf.eventmodel.EventRecordFactory
 */
public final class RecordFactory {
    private static final int NUM_RECORDS = 512;

    // how many records we read at max by default (can be adjusted via IOUtils)
    //increased to 5 million due to https://bz.apache.org/bugzilla/show_bug.cgi?id=65887
    private static final int DEFAULT_MAX_NUMBER_OF_RECORDS = 5_000_000;
    private static int MAX_NUMBER_OF_RECORDS = DEFAULT_MAX_NUMBER_OF_RECORDS;

    public static void setMaxNumberOfRecords(int maxNumberOfRecords) {
        MAX_NUMBER_OF_RECORDS = maxNumberOfRecords;
    }

    public static int getMaxNumberOfRecords() {
        return MAX_NUMBER_OF_RECORDS;
    }

    private RecordFactory() {}

    /**
     * Debug / diagnosis method<p>
     *
     * Gets the POI implementation class for a given {@code sid}.  Only a subset of the BIFF
     * records are actually interpreted by POI.  A few others are known but not interpreted
     * (see {@link UnknownRecord#getBiffName(int)}).
     *
     * @param sid the record sid
     *
     * @return the POI implementation class for the specified record {@code sid}.
     * {@code null} if the specified record is not interpreted by POI.
     */
    public static Class<? extends Record> getRecordClass(int sid) {
        return HSSFRecordTypes.forSID(sid).clazz;
    }

    /**
     * create a record, if there are MUL records than multiple records
     * are returned digested into the non-mul form.
     *
     * @param in the RecordInputStream to read from
     * @return the extracted records
     */
    public static org.apache.poi.hssf.record.Record [] createRecord(RecordInputStream in) {
        Record record = createSingleRecord(in);
        if (record instanceof DBCellRecord) {
            // Not needed by POI.  Regenerated from scratch by POI when spreadsheet is written
            return new Record[] { null, };
        }
        if (record instanceof RKRecord) {
            return new Record[] { convertToNumberRecord((RKRecord) record), };
        }
        if (record instanceof MulRKRecord) {
            return convertRKRecords((MulRKRecord)record);
        }
        return new Record[] { record, };
    }

    public static org.apache.poi.hssf.record.Record createSingleRecord(RecordInputStream in) {
        HSSFRecordTypes rec = HSSFRecordTypes.forSID(in.getSid());
        if (!rec.isParseable()) {
            rec = HSSFRecordTypes.UNKNOWN;
        }
        return rec.recordConstructor.apply(in);
    }

    /**
     * RK record is a slightly smaller alternative to NumberRecord
     * POI likes NumberRecord better
     *
     * @param rk the RK record to convert
     * @return the NumberRecord
     */
    public static NumberRecord convertToNumberRecord(RKRecord rk) {
        NumberRecord num = new NumberRecord();

        num.setColumn(rk.getColumn());
        num.setRow(rk.getRow());
        num.setXFIndex(rk.getXFIndex());
        num.setValue(rk.getRKNumber());
        return num;
    }

    /**
     * Converts a {@link MulRKRecord} into an equivalent array of {@link NumberRecord NumberRecords}
     *
     * @param mrk the MulRKRecord to convert
     * @return the equivalent array of {@link NumberRecord NumberRecords}
     */
    public static NumberRecord[] convertRKRecords(MulRKRecord mrk) {
        int numColumns = mrk.getNumColumns();
        if (numColumns < 0) {
            throw new RecordFormatException("Cannot create RKRecords with negative number of columns: " + numColumns);
        }

        NumberRecord[] mulRecs = new NumberRecord[numColumns];
        for (int k = 0; k < numColumns; k++) {
            NumberRecord nr = new NumberRecord();

            nr.setColumn((short) (k + mrk.getFirstColumn()));
            nr.setRow(mrk.getRow());
            nr.setXFIndex(mrk.getXFAt(k));
            nr.setValue(mrk.getRKNumberAt(k));
            mulRecs[k] = nr;
        }
        return mulRecs;
    }

    /**
     * Converts a {@link MulBlankRecord} into an equivalent array of {@link BlankRecord BlankRecords}
     *
     * @param mbk the MulBlankRecord to convert
     * @return the equivalent array of {@link BlankRecord BlankRecords}
     */
    public static BlankRecord[] convertBlankRecords(MulBlankRecord mbk) {
        BlankRecord[] mulRecs = new BlankRecord[mbk.getNumColumns()];
        for (int k = 0; k < mbk.getNumColumns(); k++) {
            BlankRecord br = new BlankRecord();

            br.setColumn((short) (k + mbk.getFirstColumn()));
            br.setRow(mbk.getRow());
            br.setXFIndex(mbk.getXFAt(k));
            mulRecs[k] = br;
        }
        return mulRecs;
    }

    /**
     * @return an array of all the SIDS for all known records
     */
    public static short[] getAllKnownRecordSIDs() {
        int[] intSid = Arrays.stream(HSSFRecordTypes.values()).mapToInt(HSSFRecordTypes::getSid).toArray();
        short[] shortSid = new short[intSid.length];
        for (int i=0; i<intSid.length; i++) {
            shortSid[i] = (short)intSid[i];
        }
        return shortSid;
    }

    /**
     * Create an array of records from an input stream
     *
     * @param in the InputStream from which the records will be obtained
     *
     * @return an array of Records created from the InputStream
     *
     * @throws org.apache.poi.util.RecordFormatException on error processing the InputStream
     */
    public static List<org.apache.poi.hssf.record.Record> createRecords(InputStream in) throws RecordFormatException {

        List<org.apache.poi.hssf.record.Record> records = new ArrayList<>(NUM_RECORDS);

        RecordFactoryInputStream recStream = new RecordFactoryInputStream(in, true);

        Record record;
        while ((record = recStream.nextRecord())!=null) {
            records.add(record);

            IOUtils.safelyAllocateCheck(records.size(), MAX_NUMBER_OF_RECORDS);
        }

        return records;
    }
}
