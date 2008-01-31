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
package org.apache.poi.hssf.eventusermodel;

import java.util.Vector;

import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.hssf.record.DrawingGroupRecord;
import org.apache.poi.hssf.record.DrawingRecord;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordFactory;
import org.apache.poi.hssf.record.RecordFormatException;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.TextObjectRecord;
import org.apache.poi.hssf.record.UnknownRecord;

/**
 * A stream based way to get at complete records, with
 *  as low a memory footprint as possible.
 * This handles reading from a RecordInputStream, turning
 *  the data into full records, processing continue records
 *  etc.
 * Most users should use {@link HSSFEventFactory} /
 *  {@link HSSFListener} and have new records pushed to
 *  them, but this does allow for a "pull" style of coding.  
 */
public class HSSFRecordStream {
	private RecordInputStream in;

	/** Have we run out of records on the stream? */
	private boolean hitEOS = false;
	/** Have we returned all the records there are? */
	private boolean complete = false;
	
	/**
	 * Sometimes we end up with a bunch of
	 *  records. When we do, these should
	 *  be returned before the next normal
	 *  record processing occurs (i.e. before
	 *  we check for continue records and
	 *  return rec)
	 */
	private Vector bonusRecords = null;
	
	/** 
	 * The next record to return, which may need to have its
	 *  continue records passed to it before we do
	 */
	private Record rec = null;
	/**
	 * The most recent record that we gave to the user
	 */
	private Record lastRec = null;
	/**
	 * The most recent DrawingRecord seen
	 */
	private DrawingRecord lastDrawingRecord = new DrawingRecord();
	
	public HSSFRecordStream(RecordInputStream inp) {
		this.in = inp;
	}

	/**
	 * Returns the next (complete) record from the 
	 *  stream, or null if there are no more.
	 */
	public Record nextRecord() {
		Record r = null;
		
		// Loop until we get something
		while(r == null && !complete) {
			// Are there any bonus records that we need to
			//  return?
			r = getBonusRecord();
			
			// If not, ask for the next real record
			if(r == null) {
				r = getNextRecord();
			}
		}
		
		// All done
		return r;
	}
	
	/**
	 * If there are any "bonus" records, that should
	 *  be returned before processing new ones, 
	 *  grabs the next and returns it.
	 * If not, returns null;
	 */
	private Record getBonusRecord() {
		if(bonusRecords != null) {
			Record r = (Record)bonusRecords.remove(0);
			if(bonusRecords.size() == 0) {
				bonusRecords = null;
			}
			return r;
		}
		return null;
	}
	
	/**
	 * Returns the next available record, or null if
	 *  this pass didn't return a record that's
	 *  suitable for returning (eg was a continue record).
	 */
	private Record getNextRecord() {
		Record toReturn = null;
		
		if(in.hasNextRecord()) {
			// Grab our next record
			in.nextRecord();
			short sid = in.getSid();
			
            //
            // for some reasons we have to make the workbook to be at least 4096 bytes
            // but if we have such workbook we fill the end of it with zeros (many zeros)
            //
            // it is not good:
            // if the length( all zero records ) % 4 = 1
            // e.g.: any zero record would be readed as  4 bytes at once ( 2 - id and 2 - size ).
            // And the last 1 byte will be readed WRONG ( the id must be 2 bytes )
            //
            // So we should better to check if the sid is zero and not to read more data
            // The zero sid shows us that rest of the stream data is a fake to make workbook 
            // certain size
            //
            if ( sid == 0 )
                return null;


            // If we had a last record, and this one
            //  isn't a continue record, then pass
            //  it on to the listener
			if ((rec != null) && (sid != ContinueRecord.sid))
			{
				// This last record ought to be returned
				toReturn = rec;
			}
			
			// If this record isn't a continue record,
			//  then build it up
			if (sid != ContinueRecord.sid)
			{
				//System.out.println("creating "+sid);
				Record[] recs = RecordFactory.createRecord(in);

				// We know that the multiple record situations
				//  don't contain continue records, so just
				//  pass those on to the listener now
				if (recs.length > 1) {
					bonusRecords = new Vector(recs.length-1);
					for (int k = 0; k < (recs.length - 1); k++)	{
						bonusRecords.add(recs[k]);
					}
				}
				
				// Regardless of the number we created, always hold
				//  onto the last record to be processed on the next
				//  loop, in case it has any continue records
				rec = recs[ recs.length - 1 ];
				// Don't return it just yet though, as we probably have
				//  a record from the last round to return
			}
			else {
				// Normally, ContinueRecords are handled internally
				// However, in a few cases, there is a gap between a record at
				//  its Continue, so we have to handle them specially
				// This logic is much like in RecordFactory.createRecords()
				Record[] recs = RecordFactory.createRecord(in);
				ContinueRecord crec = (ContinueRecord)recs[0];
				if((lastRec instanceof ObjRecord) || (lastRec instanceof TextObjectRecord)) {
					// You can have Obj records between a DrawingRecord
					//  and its continue!
					lastDrawingRecord.processContinueRecord( crec.getData() );
					// Trigger them on the drawing record, now it's complete
					rec = lastDrawingRecord;
				}
				else if((lastRec instanceof DrawingGroupRecord)) {
					((DrawingGroupRecord)lastRec).processContinueRecord(crec.getData());
					// Trigger them on the drawing record, now it's complete
					rec = lastRec;
				}
				else {
                    if (rec instanceof UnknownRecord) {
                        ;//silently skip records we don't know about
                    } else {
					    throw new RecordFormatException("Records should handle ContinueRecord internally. Should not see this exception");
                    }
				}
			}

			// Update our tracking of the last record
			lastRec = rec;
			if(rec instanceof DrawingRecord) {
				lastDrawingRecord = (DrawingRecord)rec;
			}
		} else {
			// No more records
			hitEOS = true;
		}
		
		// If we've hit the end-of-stream, then
		//  finish off the last record and be done
		if(hitEOS) {
			complete = true;
			
			// Return the last record if there was
			//  one, otherwise null
			if(rec != null) {
				toReturn = rec;
				rec = null;
			}
		}
			
		return toReturn;
	}
}