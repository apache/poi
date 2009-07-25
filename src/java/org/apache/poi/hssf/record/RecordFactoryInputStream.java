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

import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A stream based way to get at complete records, with
 * as low a memory footprint as possible.
 * This handles reading from a RecordInputStream, turning
 * the data into full records, processing continue records
 * etc.
 * Most users should use {@link HSSFEventFactory} /
 * {@link HSSFListener} and have new records pushed to
 * them, but this does allow for a "pull" style of coding.
 */
public class RecordFactoryInputStream {
    private final RecordInputStream recStream;

    /**
     * Have we returned all the records there are?
     */
    private boolean complete = false;

    /**
     * Sometimes we end up with a bunch of
     * records. When we do, these should
     * be returned before the next normal
     * record processing occurs (i.e. before
     * we check for continue records and
     * return rec)
     */
    private final LinkedList bonusRecords = new LinkedList();

    /**
     * The most recent record that we gave to the user
     */
    private Record lastRecord = null;
    /**
     * The most recent DrawingRecord seen
     */
    private DrawingRecord lastDrawingRecord = new DrawingRecord();

    private int bofDepth = 0;

    private boolean lastRecordWasEOFLevelZero = false;

    private boolean includeContinueRecords = false;

    public RecordFactoryInputStream(RecordInputStream inp) {
        recStream = inp;
    }

    /**
     * Returns the next (complete) record from the
     * stream, or null if there are no more.
     */
    public Record nextRecord() {
        Record r = null;

        // Loop until we get something
        while (r == null && !complete) {
            // Are there any bonus records that we need to
            //  return?
            r = getBonusRecord();

            // If not, ask for the next real record
            if (r == null) {
                r = getNextRecord();
            }
        }

        // All done
        return r;
    }

    /**
     * If there are any "bonus" records, that should
     * be returned before processing new ones,
     * grabs the next and returns it.
     * If not, returns null;
     */
    private Record getBonusRecord() {
        if (!bonusRecords.isEmpty()) {
            return (Record) bonusRecords.removeFirst();
        }
        return null;
    }

    /**
     * Returns the next available record, or null if
     * this pass didn't return a record that's
     * suitable for returning (eg was a continue record).
     */
    private Record getNextRecord() {
        /*
        * How to recognise end of stream?
        * In the best case, the underlying input stream (in) ends just after the last EOF record
        * Usually however, the stream is padded with an arbitrary byte count.  Excel and most apps
        * reliably use zeros for padding and if this were always the case, this code could just
        * skip all the (zero sized) records with sid==0.  However, bug 46987 shows a file with
        * non-zero padding that is read OK by Excel (Excel also fixes the padding).
        *
        * So to properly detect the workbook end of stream, this code has to identify the last
        * EOF record.  This is not so easy because the worbook bof+eof pair do not bracket the
        * whole stream.  The worksheets follow the workbook, but it is not easy to tell how many
        * sheet sub-streams should be present.  Hence we are looking for an EOF record that is not
        * immediately followed by a BOF record.  One extra complication is that bof+eof sub-
        * streams can be nested within worksheet streams and it's not clear in these cases what
        * record might follow any EOF record.  So we also need to keep track of the bof/eof
        * nesting level.
        */

        if (recStream.hasNextRecord()) {
            // Grab our next record
            recStream.nextRecord();

            if (lastRecordWasEOFLevelZero && recStream.getSid() != BOFRecord.sid) {
                // Normally InputStream (in) contains only zero padding after this point
                complete = true;
                return null;
            }

            Record record = RecordFactory.createSingleRecord(recStream);
            lastRecordWasEOFLevelZero = false;

            if (record instanceof BOFRecord) {
                bofDepth++;
                return record;
            }

            if (record instanceof EOFRecord) {
                bofDepth--;
                if (bofDepth < 1) {
                    lastRecordWasEOFLevelZero = true;
                }

                return record;
            }

            if (record instanceof DBCellRecord) {
                // Not needed by POI.  Regenerated from scratch by POI when spreadsheet is written
                return null;
            }

            if (record instanceof RKRecord) {
                return RecordFactory.convertToNumberRecord((RKRecord) record);
            }

            if (record instanceof MulRKRecord) {
                NumberRecord[] records = RecordFactory.convertRKRecords((MulRKRecord) record);

                List<NumberRecord> list = Arrays.asList(records);
                bonusRecords.addAll(list.subList(1, list.size()));

                return records[0];
            }

            if (record.getSid() == DrawingGroupRecord.sid
                    && lastRecord instanceof DrawingGroupRecord) {
                DrawingGroupRecord lastDGRecord = (DrawingGroupRecord) lastRecord;
                lastDGRecord.join((AbstractEscherHolderRecord) record);
                return null;
            } else if (record.getSid() == ContinueRecord.sid) {
                ContinueRecord contRec = (ContinueRecord) record;

                if (lastRecord instanceof ObjRecord || lastRecord instanceof TextObjectRecord) {
                    // Drawing records have a very strange continue behaviour.
                    //There can actually be OBJ records mixed between the continues.
                    lastDrawingRecord.processContinueRecord(contRec.getData());
                    //we must remember the position of the continue record.
                    //in the serialization procedure the original structure of records must be preserved
                    if (includeContinueRecords) {
                        return record;
                    } else {
                        return null;
                    }
                } else if (lastRecord instanceof DrawingGroupRecord) {
                    ((DrawingGroupRecord) lastRecord).processContinueRecord(contRec.getData());
                    return null;
                } else if (lastRecord instanceof DrawingRecord) {
                    ((DrawingRecord) lastRecord).processContinueRecord(contRec.getData());
                    return null;
                } else if (lastRecord instanceof UnknownRecord) {
                    //Gracefully handle records that we don't know about,
                    //that happen to be continued
                    return record;
                } else if (lastRecord instanceof EOFRecord) {
                    // This is really odd, but excel still sometimes
                    //  outputs a file like this all the same
                    return record;
                } else {
                    throw new RecordFormatException("Unhandled Continue Record");
                }
            } else {
                lastRecord = record;
                if (record instanceof DrawingRecord) {
                    lastDrawingRecord = (DrawingRecord) record;
                }

                return record;
            }

        } else {
            // No more records
            complete = true;
            return null;
        }
    }

    /**
     * Return or not ContinueRecord in nextRecord
     */
    public void setIncludeContinueRecords(boolean includeContinueRecords) {
        this.includeContinueRecords = includeContinueRecords;
    }
}