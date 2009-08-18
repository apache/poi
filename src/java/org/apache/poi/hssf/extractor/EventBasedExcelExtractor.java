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

package org.apache.poi.hssf.extractor;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.eventusermodel.FormatTrackingHSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * A text extractor for Excel files, that is based
 *  on the hssf eventusermodel api.
 * It will typically use less memory than
 *  {@link ExcelExtractor}, but may not provide
 *  the same richness of formatting.
 * Returns the textual content of the file, suitable for
 *  indexing by something like Lucene, but not really
 *  intended for display to the user.
 * <p>
 * To turn an excel file into a CSV or similar, then see
 *  the XLS2CSVmra example
 * </p>
 * <link href="http://svn.apache.org/repos/asf/poi/trunk/src/examples/src/org/apache/poi/hssf/eventusermodel/examples/XLS2CSVmra.java">
 * http://svn.apache.org/repos/asf/poi/trunk/src/examples/src/org/apache/poi/hssf/eventusermodel/examples/XLS2CSVmra.java</link>
 */
public class EventBasedExcelExtractor extends POIOLE2TextExtractor {
	private POIFSFileSystem _fs;
	boolean _includeSheetNames = true;
	boolean _formulasNotResults = false;

	public EventBasedExcelExtractor(POIFSFileSystem fs) {
		super(null);
		_fs = fs;
	}

	/**
	 * Would return the document information metadata for the document,
	 *  if we supported it
	 */
	public DocumentSummaryInformation getDocSummaryInformation() {
		throw new IllegalStateException("Metadata extraction not supported in streaming mode, please use ExcelExtractor");
	}
	/**
	 * Would return the summary information metadata for the document,
	 *  if we supported it
	 */
	public SummaryInformation getSummaryInformation() {
		throw new IllegalStateException("Metadata extraction not supported in streaming mode, please use ExcelExtractor");
	}


	/**
	 * Should sheet names be included? Default is true
	 */
	public void setIncludeSheetNames(boolean includeSheetNames) {
		_includeSheetNames = includeSheetNames;
	}
	/**
	 * Should we return the formula itself, and not
	 *  the result it produces? Default is false
	 */
	public void setFormulasNotResults(boolean formulasNotResults) {
		_formulasNotResults = formulasNotResults;
	}


	/**
	 * Retreives the text contents of the file
	 */
	public String getText() {
		String text = null;
		try {
			TextListener tl = triggerExtraction();

			text = tl._text.toString();
			if(! text.endsWith("\n")) {
				text = text + "\n";
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		return text;
	}

	private TextListener triggerExtraction() throws IOException {
		TextListener tl = new TextListener();
		FormatTrackingHSSFListener ft = new FormatTrackingHSSFListener(tl);
		tl._ft = ft;

		// Register and process
		HSSFEventFactory factory = new HSSFEventFactory();
		HSSFRequest request = new HSSFRequest();
		request.addListenerForAllRecords(ft);

		factory.processWorkbookEvents(request, _fs);

		return tl;
	}

	private class TextListener implements HSSFListener {
		FormatTrackingHSSFListener _ft;
		private SSTRecord sstRecord;

		private final List<String> sheetNames;
		final StringBuffer _text = new StringBuffer();
		private int sheetNum = -1;
		private int rowNum;

		private boolean outputNextStringValue = false;
		private int nextRow = -1;

		public TextListener() {
			sheetNames = new ArrayList<String>();
		}
		public void processRecord(Record record) {
			String thisText = null;
			int thisRow = -1;

			switch(record.getSid()) {
			case BoundSheetRecord.sid:
				BoundSheetRecord sr = (BoundSheetRecord)record;
				sheetNames.add(sr.getSheetname());
				break;
			case BOFRecord.sid:
				BOFRecord bof = (BOFRecord)record;
				if(bof.getType() == BOFRecord.TYPE_WORKSHEET) {
					sheetNum++;
					rowNum = -1;

					if(_includeSheetNames) {
						if(_text.length() > 0) _text.append("\n");
						_text.append(sheetNames.get(sheetNum));
					}
				}
				break;
			case SSTRecord.sid:
				sstRecord = (SSTRecord)record;
				break;

			case FormulaRecord.sid:
				FormulaRecord frec = (FormulaRecord) record;
				thisRow = frec.getRow();

				if(_formulasNotResults) {
					thisText = HSSFFormulaParser.toFormulaString((HSSFWorkbook)null, frec.getParsedExpression());
				} else {
					if(frec.hasCachedResultString()) {
						// Formula result is a string
						// This is stored in the next record
						outputNextStringValue = true;
						nextRow = frec.getRow();
					} else {
						thisText = formatNumberDateCell(frec, frec.getValue());
					}
				}
				break;
			case StringRecord.sid:
				if(outputNextStringValue) {
					// String for formula
					StringRecord srec = (StringRecord)record;
					thisText = srec.getString();
					thisRow = nextRow;
					outputNextStringValue = false;
				}
				break;
			case LabelRecord.sid:
				LabelRecord lrec = (LabelRecord) record;
				thisRow = lrec.getRow();
				thisText = lrec.getValue();
				break;
			case LabelSSTRecord.sid:
				LabelSSTRecord lsrec = (LabelSSTRecord) record;
				thisRow = lsrec.getRow();
				if(sstRecord == null) {
					throw new IllegalStateException("No SST record found");
				}
				thisText = sstRecord.getString(lsrec.getSSTIndex()).toString();
				break;
			case NoteRecord.sid:
				NoteRecord nrec = (NoteRecord) record;
				thisRow = nrec.getRow();
				// TODO: Find object to match nrec.getShapeId()
				break;
			case NumberRecord.sid:
				NumberRecord numrec = (NumberRecord) record;
				thisRow = numrec.getRow();
				thisText = formatNumberDateCell(numrec, numrec.getValue());
				break;
			default:
				break;
			}

			if(thisText != null) {
				if(thisRow != rowNum) {
					rowNum = thisRow;
					if(_text.length() > 0)
						_text.append("\n");
				} else {
					_text.append("\t");
				}
				_text.append(thisText);
			}
		}

		/**
		 * Formats a number or date cell, be that a real number, or the
		 *  answer to a formula
		 */
		private String formatNumberDateCell(CellValueRecordInterface cell, double value) {
			// Get the built in format, if there is one
			int formatIndex = _ft.getFormatIndex(cell);
			String formatString = _ft.getFormatString(cell);

			if(formatString == null) {
				return Double.toString(value);
			}
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
			}
			if(formatString == "General") {
				// Some sort of wierd default
				return Double.toString(value);
			}

			// Format as a number
			DecimalFormat df = new DecimalFormat(formatString);
			return df.format(value);
		}
	}
}
