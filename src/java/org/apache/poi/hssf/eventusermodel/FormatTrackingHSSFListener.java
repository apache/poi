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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.record.FormatRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;

/**
 * A proxy HSSFListener that keeps track of the document
 *  formatting records, and provides an easy way to look
 *  up the format strings used by cells from their ids.
 */
public class FormatTrackingHSSFListener implements HSSFListener {
	private HSSFListener childListener;
	private Map customFormatRecords = new Hashtable();
	private List xfRecords = new ArrayList();

	public FormatTrackingHSSFListener(HSSFListener childListener) {
		this.childListener = childListener;
	}
	
	/**
	 * Process this record ourselves, and then
	 *  pass it on to our child listener
	 */
	public void processRecord(Record record) {
		// Handle it ourselves
		processRecordInternally(record);
		
		// Now pass on to our child
		childListener.processRecord(record);
	}
	
	/**
	 * Process the record ourselves, but do not
	 *  pass it on to the child Listener.
	 * @param record
	 */
	public void processRecordInternally(Record record) {
		if(record instanceof FormatRecord) {
			FormatRecord fr = (FormatRecord) record;
			customFormatRecords.put(new Integer(fr.getIndexCode()), fr);
		}
		if(record instanceof ExtendedFormatRecord) {
			ExtendedFormatRecord xr = (ExtendedFormatRecord) record;
			xfRecords.add(xr);
		}
	}
	
	/**
	 * Formats the given numeric of date Cell's contents
	 *  as a String, in as close as we can to the way 
	 *  that Excel would do so.
	 * Uses the various format records to manage this.
	 * 
	 * TODO - move this to a central class in such a
	 *  way that hssf.usermodel can make use of it too
	 */
	public String formatNumberDateCell(CellValueRecordInterface cell) {
		double value;
		if(cell instanceof NumberRecord) {
			value = ((NumberRecord)cell).getValue();
		} else if(cell instanceof FormulaRecord) {
			value = ((FormulaRecord)cell).getValue();
		} else {
			throw new IllegalArgumentException("Unsupported CellValue Record passed in " + cell);
		}
		
        // Get the built in format, if there is one
		int formatIndex = getFormatIndex(cell);
		String formatString = getFormatString(cell);
		
		if(formatString == null) {
            return Double.toString(value);
        } else {
        	// Is it a date?
        	if(HSSFDateUtil.isADateFormat(formatIndex,formatString) &&
        			HSSFDateUtil.isValidExcelDate(value)) {
        		// Java wants M not m for month
        		formatString = formatString.replace('m','M');
        		// Change \- into -, if it's there
        		formatString = formatString.replaceAll("\\\\-","-");
        		
        		// Format as a date
        		Date d = HSSFDateUtil.getJavaDate(value, false);
        		DateFormat df = new SimpleDateFormat(formatString);
	            return df.format(d);
        	} else {
        		if(formatString == "General") {
        			// Some sort of wierd default
        			return Double.toString(value);
        		}
        		if(formatString == "0.00E+00") {
        			// This seems to mean output as a normal double
        			return Double.toString(value);
        		}
        		
        		// Format as a number
	            DecimalFormat df = new DecimalFormat(formatString);
	            return df.format(value);
        	}
        }
	}
	
	/**
	 * Returns the format string, eg $##.##, for the
	 *  given number format index.
	 */
	public String getFormatString(int formatIndex) {
    	String format = null;
    	if(formatIndex >= HSSFDataFormat.getNumberOfBuiltinBuiltinFormats()) {
    		FormatRecord tfr = (FormatRecord)customFormatRecords.get(new Integer(formatIndex));
    		if(tfr == null) {
    			System.err.println("Requested format at index " + formatIndex + ", but it wasn't found");
    		} else {
    			format = tfr.getFormatString();
    		}
    	} else {
        	format = HSSFDataFormat.getBuiltinFormat((short)formatIndex);
    	}
		return format;
	}
	
	/**
	 * Returns the format string, eg $##.##, used
	 *  by your cell 
	 */
	public String getFormatString(CellValueRecordInterface cell) {
		int formatIndex = getFormatIndex(cell);
	    if(formatIndex == -1) {
	    	// Not found
	    	return null;
	    }
	    return getFormatString(formatIndex);
	}
	
	/**
	 * Returns the index of the format string, used by your cell,
	 *  or -1 if none found
	 */
	public int getFormatIndex(CellValueRecordInterface cell) {
        ExtendedFormatRecord xfr = (ExtendedFormatRecord)
	    	xfRecords.get(cell.getXFIndex());
	    if(xfr == null) {
	    	System.err.println("Cell " + cell.getRow() + "," + cell.getColumn() + " uses XF with index " + cell.getXFIndex() + ", but we don't have that");
	    	return -1;
	    }
	    return xfr.getFormatIndex();
	}
}
