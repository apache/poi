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

import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingRowDummyRecord;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.BoolErrRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.RKRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;

/**
 * <p>A HSSFListener which tracks rows and columns, and will
 *  trigger your HSSFListener for all rows and cells,
 *  even the ones that aren't actually stored in the file.</p>
 * <p>This allows your code to have a more "Excel" like
 *  view of the data in the file, and not have to worry
 *  (as much) about if a particular row/cell is in the
 *  file, or was skipped from being written as it was
 *  blank.
 */
public class MissingRecordAwareHSSFListener implements HSSFListener {
	private HSSFListener childListener;
	
	// Need to have different counters for cell rows and
	//  row rows, as you sometimes get a RowRecord in the
	//  middle of some cells, and that'd break everything
	private int lastRowRow = -1;
	
	private int lastCellRow = -1;
	private int lastCellColumn = -1;
	
	/**
	 * Constructs a new MissingRecordAwareHSSFListener, which
	 *  will fire processRecord on the supplied child
	 *  HSSFListener for all Records, and missing records.
	 * @param listener The HSSFListener to pass records on to
	 */
	public MissingRecordAwareHSSFListener(HSSFListener listener) {
		childListener = listener;
	}

	public void processRecord(Record record) {
		int thisRow = -1;
		int thisColumn = -1;
		
		switch (record.getSid())
        {
            // the BOFRecord can represent either the beginning of a sheet or the workbook
            case BOFRecord.sid:
                BOFRecord bof = (BOFRecord) record;
                if (bof.getType() == bof.TYPE_WORKBOOK)
                {
                	// Reset the row and column counts - new workbook
                	lastRowRow = -1;
                	lastCellRow = -1;
                	lastCellColumn = -1;
                    //System.out.println("Encountered workbook");
                } else if (bof.getType() == bof.TYPE_WORKSHEET)
                {
                	// Reset the row and column counts - new sheet
                	lastRowRow = -1;
                	lastCellRow = -1;
                	lastCellColumn = -1;
                    //System.out.println("Encountered sheet reference");
                }
                break;
            case BoundSheetRecord.sid:
                BoundSheetRecord bsr = (BoundSheetRecord) record;
                //System.out.println("New sheet named: " + bsr.getSheetname());
                break;
            case RowRecord.sid:
                RowRecord rowrec = (RowRecord) record;
                //System.out.println("Row " + rowrec.getRowNumber() + " found, first column at "
                //        + rowrec.getFirstCol() + " last column at " + rowrec.getLastCol());
                
                // If there's a jump in rows, fire off missing row records
                if(lastRowRow+1 < rowrec.getRowNumber()) {
                	for(int i=(lastRowRow+1); i<rowrec.getRowNumber(); i++) {
                		MissingRowDummyRecord dr = new MissingRowDummyRecord(i);
                		childListener.processRecord(dr);
                	}
                }
                
                // Record this as the last row we saw
                lastRowRow = rowrec.getRowNumber();
                break;
                
                
            // These are all the "cell" records
                
            case BlankRecord.sid:
            	BlankRecord brec = (BlankRecord) record;
                thisRow = brec.getRow();
                thisColumn = brec.getColumn();
                break;
            case BoolErrRecord.sid:
            	BoolErrRecord berec = (BoolErrRecord) record;
                thisRow = berec.getRow();
                thisColumn = berec.getColumn();
                break;
            case FormulaRecord.sid:
            	FormulaRecord frec = (FormulaRecord) record;
            	thisRow = frec.getRow();
            	thisColumn = frec.getColumn();
                break;
            case LabelRecord.sid:
            	LabelRecord lrec = (LabelRecord) record;
                thisRow = lrec.getRow();
                thisColumn = lrec.getColumn();
                //System.out.println("Cell found containing String "
                //        + " at row " + lrec.getRow() + " and column " + lrec.getColumn());
                break;
            case LabelSSTRecord.sid:
            	LabelSSTRecord lsrec = (LabelSSTRecord) record;
                thisRow = lsrec.getRow();
                thisColumn = lsrec.getColumn();
                //System.out.println("Cell found containing String "
                //        + " at row " + lsrec.getRow() + " and column " + lsrec.getColumn());
                break;
            case NoteRecord.sid:
            	NoteRecord nrec = (NoteRecord) record;
            	thisRow = nrec.getRow();
            	thisColumn = nrec.getColumn();
                break;
            case NumberRecord.sid:
                NumberRecord numrec = (NumberRecord) record;
                thisRow = numrec.getRow();
                thisColumn = numrec.getColumn();
                //System.out.println("Cell found with value " + numrec.getValue()
                //        + " at row " + numrec.getRow() + " and column " + numrec.getColumn());
                break;
            case RKRecord.sid:
            	RKRecord rkrec = (RKRecord) record;
            	thisRow = rkrec.getRow();
            	thisColumn = rkrec.getColumn();
                break;
            default:
            	//System.out.println(record.getClass());
            	break;
        }
		
		// If we're on cells, and this cell isn't in the same
		//  row as the last one, then fire the 
		//  dummy end-of-row records?
		if(thisRow != lastCellRow && lastCellRow > -1) {
			for(int i=lastCellRow; i<thisRow; i++) {
				int cols = -1;
				if(i == lastCellRow) {
					cols = lastCellColumn;
				}
				LastCellOfRowDummyRecord r = new LastCellOfRowDummyRecord(i, cols);
				childListener.processRecord(r);
			}
		}
		
		// If we've just finished with the cells, then fire the
		//  final dummy end-of-row record
		if(lastCellRow != -1 && lastCellColumn != -1 && thisRow == -1) {
			LastCellOfRowDummyRecord r = new LastCellOfRowDummyRecord(lastCellRow, lastCellColumn);
			childListener.processRecord(r);
			
			lastCellRow = -1;
			lastCellColumn = -1;
		}
		
		// If we've moved onto a new row, the ensure we re-set
		//  the column counter
		if(thisRow != lastCellRow) {
			lastCellColumn = -1;
		}
		
		// If there's a gap in the cells, then fire
		//  the dummy cell records?
		if(lastCellColumn != (thisColumn-1)) {
			for(int i=lastCellColumn+1; i<thisColumn; i++) {
				MissingCellDummyRecord r = new MissingCellDummyRecord(thisRow, i);
				childListener.processRecord(r);
			}
		}
		
		// Update cell and row counts as needed
		if(thisColumn != -1) {
			lastCellColumn = thisColumn;
			lastCellRow = thisRow;
		}
		
		childListener.processRecord(record);
	}
}
