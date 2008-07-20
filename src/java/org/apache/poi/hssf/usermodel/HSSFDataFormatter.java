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

package org.apache.poi.hssf.usermodel;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HSSFDataFormatter contains methods for formatting the value stored in an
 * HSSFCell. This can be useful for reports and GUI presentations when you
 * need to display data exactly as it appears in Excel. Supported formats
 * include currency, SSN, percentages, decimals, dates, phone numbers, zip
 * codes, etc.
 * <p>
 * Internally, formats will be implemented using subclasses of {@link Format}
 * such as {@link DecimalFormat} and {@link SimpleDateFormat}. Therefore the
 * formats used by this class must obey the same pattern rules as these Format
 * subclasses. This means that only legal number pattern characters ("0", "#",
 * ".", "," etc.) may appear in number formats. Other characters can be
 * inserted <em>before</em> or <em> after</em> the number pattern to form a
 * prefix or suffix.
 * </p>
 * <p>
 * For example the Excel pattern <code>"$#,##0.00 "USD"_);($#,##0.00 "USD")"
 * </code> will be correctly formatted as "$1,000.00 USD" or "($1,000.00 USD)".
 * However the pattern <code>"00-00-00"</code> is incorrectly formatted by
 * DecimalFormat as "000000--". For Excel formats that are not compatible with
 * DecimalFormat, you can provide your own custom {@link Format} implementation
 * via <code>HSSFDataFormatter.addFormat(String,Format)</code>. The following
 * custom formats are already provided by this class:
 * </p>
 * <pre>
 * <ul><li>SSN "000-00-0000"</li>
 *     <li>Phone Number "(###) ###-####"</li>
 *     <li>Zip plus 4 "00000-0000"</li>
 * </ul>
 * </pre>
 * <p>
 * If the Excel format pattern cannot be parsed successfully, then a default
 * format will be used. The default number format will mimic the Excel General
 * format: "#" for whole numbers and "#.##########" for decimal numbers. You
 * can override the default format pattern with <code>
 * HSSFDataFormatter.setDefaultNumberFormat(Format)</code>. <b>Note:</b> the
 * default format will only be used when a Format cannot be created from the
 * cell's data format string.  
 * 
 * @author James May (james dot may at fmr dot com)
 *
 */
public class HSSFDataFormatter {

	/** Pattern to find a number format: "0" or  "#" */
	protected Pattern numPattern;
	
	/** Pattern to find days of week as text "ddd...." */
	protected Pattern daysAsText;
	
	/** Pattern to find "AM/PM" marker */
	protected Pattern amPmPattern;

	/** A regex to find patterns like [$$-1009] and [$�-452]. */
	protected Pattern specialPatternGroup;
	
	/** <em>General</em> format for whole numbers. */
	protected Format generalWholeNumFormat;
	
	/** <em>General</em> format for decimal numbers. */
	protected Format generalDecimalNumFormat;	
	
	/** A default format to use when a number pattern cannot be parsed. */
	protected Format defaultNumFormat;
	
	/** 
	 * A map to cache formats.
	 *  Map<String,Format> formats
	 */
	protected Map formats;
	

	/**
	 * Constructor
	 */
	public HSSFDataFormatter() {
		numPattern = Pattern.compile("[0#]+");
		daysAsText = Pattern.compile("([d]{3,})", Pattern.CASE_INSENSITIVE);
		amPmPattern = Pattern.compile("((A|P)[M/P]*)", Pattern.CASE_INSENSITIVE);
		specialPatternGroup = Pattern.compile("(\\[\\$[^-\\]]*-[0-9A-Z]+\\])");
		generalWholeNumFormat =  new DecimalFormat("#");
		generalDecimalNumFormat =  new DecimalFormat("#.##########");	
		formats = new HashMap();
		
		// init built-in formats
		init();
	}

	/**
	 * Initialize the formatter. Called after construction.
	 */
	protected void init() {		
		
		ZipPlusFourFormat zipFormat = new ZipPlusFourFormat();
		addFormat("00000\\-0000", zipFormat);
		addFormat("00000-0000", zipFormat);
		
		PhoneFormat phoneFormat = new PhoneFormat();
		// allow for format string variations
		addFormat("[<=9999999]###\\-####;\\(###\\)\\ ###\\-####", phoneFormat);
		addFormat("[<=9999999]###-####;(###) ###-####", phoneFormat);
		addFormat("###\\-####;\\(###\\)\\ ###\\-####", phoneFormat);
		addFormat("###-####;(###) ###-####", phoneFormat);		
		
		SSNFormat ssnFormat = new SSNFormat();
		addFormat("000\\-00\\-0000", ssnFormat);		
		addFormat("000-00-0000", ssnFormat);			
	}
	
	/**
	 * Return a Format for the given cell if one exists, otherwise try to
	 * create one. This method will return <code>null</code> if the any of the
	 * following is true:
	 * <ul>
	 * <li>the cell's style is null</li>
	 * <li>the style's data format string is null or empty</li>
	 * <li>the format string cannot be recognized as either a number or date</li>
	 * </ul>
	 * 
	 * @param cell The cell to retrieve a Format for
	 * @return A Format for the format String
	 */
	protected Format getFormat(HSSFCell cell) {
		if ( cell.getCellStyle() == null) {
			return null;
		}
		
		int formatIndex = cell.getCellStyle().getDataFormat();
		String formatStr = cell.getCellStyle().getDataFormatString();
		if(formatStr == null || formatStr.trim().length() == 0) {
			return null;
		}
		return getFormat(cell.getNumericCellValue(), formatIndex, formatStr);
	}
	
	private Format getFormat(double cellValue, int formatIndex, String formatStr) {
		Format format = (Format)formats.get(formatStr);
		if (format != null) {
			return format;
		} else if (formatStr.equals("General")) {
			if (HSSFDataFormatter.isWholeNumber(cellValue)) {
				return generalWholeNumFormat;
			} else {
				return generalDecimalNumFormat;
			}
		} else {
			format = createFormat(cellValue, formatIndex, formatStr);
			formats.put(formatStr, format);
			return format;
		}
	}
	
	/**
	 * Create and return a Format based on the format string from a  cell's
	 * style. If the pattern cannot be parsed, return a default pattern.
	 * 
	 * @param cell The Excel cell
	 * @return A Format representing the excel format. May return null.
	 */
	protected Format createFormat(HSSFCell cell) {	
		String sFormat = cell.getCellStyle().getDataFormatString();
		
		int formatIndex = cell.getCellStyle().getDataFormat();
		String formatStr = cell.getCellStyle().getDataFormatString();
		return createFormat(cell.getNumericCellValue(), formatIndex, formatStr); 
	}
	
	private Format createFormat(double cellValue, int formatIndex, String sFormat) {
		// remove color formatting if present
		String formatStr = sFormat.replaceAll("\\[[a-zA-Z]*\\]", "");
		
		// try to extract special characters like currency
		Matcher m = specialPatternGroup.matcher(formatStr);		
		try {
			while(m.find()) {
				String match = m.group();
				String symbol = match.substring(match.indexOf('$') + 1, match.indexOf('-'));
				if (symbol.indexOf('$') > -1) {
					StringBuffer sb = new StringBuffer();
					sb.append(symbol.substring(0, symbol.indexOf('$')));
					sb.append('\\');
					sb.append(symbol.substring(symbol.indexOf('$'), symbol.length()));
					symbol = sb.toString();
				}
				formatStr = m.replaceAll(symbol);
			}
		} catch (Exception e) {
			return getDefaultFormat(cellValue);
		}
		
		if(formatStr == null || formatStr.trim().length() == 0) {
			return getDefaultFormat(cellValue);
		}

		Format returnVal = null;
		StringBuffer sb = null;

    	if(HSSFDateUtil.isADateFormat(formatIndex,formatStr) &&
    			HSSFDateUtil.isValidExcelDate(cellValue)) {
	    	formatStr = formatStr.replaceAll("\\\\-","-");
	    	formatStr = formatStr.replaceAll("\\\\,",",");
	    	formatStr = formatStr.replaceAll("\\\\ "," ");
	    	formatStr = formatStr.replaceAll(";@", "");
	    	boolean hasAmPm = false;
	    	Matcher amPmMatcher = amPmPattern.matcher(formatStr);
	    	while (amPmMatcher.find()) {
	    		formatStr = amPmMatcher.replaceAll("a");
	    		hasAmPm = true;
	    	}
	    	
	    	Matcher dateMatcher = daysAsText.matcher(formatStr);
	    	if (dateMatcher.find()) {
				String match = dateMatcher.group(0);
				formatStr = dateMatcher.replaceAll(match.toUpperCase().replaceAll("D", "E"));
	    	}
	    	
	    	// Convert excel date format to SimpleDateFormat.
	    	// Excel uses lower case 'm' for both minutes and months.
	    	// From Excel help:
	    	/* 
			  The "m" or "mm" code must appear immediately after the "h" or"hh"
			  code or immediately before the "ss" code; otherwise, Microsoft
			  Excel displays the month instead of minutes."
			*/
	    	
	    	sb = new StringBuffer();
	    	char[] chars = formatStr.toCharArray();
	    	boolean mIsMonth = true;
	    	List ms = new ArrayList();
	    	for(int j=0; j<chars.length; j++) {
	    		char c = chars[j];
	    		if (c == 'h' || c == 'H') {
	    			mIsMonth = false;
	    			if (hasAmPm) {
	    				sb.append('h');
	    			} else {
	    				sb.append('H');
	    			}	    			
	    		}
	    		else if (c == 'm') {
	    			if(mIsMonth) {
	    				sb.append('M');
	    				ms.add(
	    						new Integer(sb.length() -1)
	    				);
	    			} else {
	    				sb.append('m');
	    			}
	    		}
	    		else if (c == 's' || c == 'S') {
	    			sb.append('s');
	    			// if 'M' precedes 's' it should be minutes ('m') 
	    			for (int i = 0; i < ms.size(); i++) {
	    				int index = ((Integer)ms.get(i)).intValue();
	    				if (sb.charAt(index) == 'M') {
	    					sb.replace(index, index+1, "m");
	    				}
	    			}
	    			mIsMonth = true;
	    			ms.clear();
	    		}
	    		else if (Character.isLetter(c)) {
	    			mIsMonth = true;
	    			ms.clear();
	    			if (c == 'y' || c == 'Y') {
	    				sb.append('y');
	    			}
	    			else if (c == 'd' || c == 'D') {
	    				sb.append('d');	    			
	    			}
	    			else {
	    				sb.append(c);
	    			}	    			
	    		}
	    		else {
	    			sb.append(c);
	    		}
	    	}
			formatStr = sb.toString();
			
			try {
				returnVal = new SimpleDateFormat(formatStr);				
			} catch(IllegalArgumentException iae) {
				
				// the pattern could not be parsed correctly,
				// so fall back to the default number format
				return getDefaultFormat(cellValue);
			}	    	
			
		} else if (numPattern.matcher(formatStr).find()) {
			sb = new StringBuffer(formatStr);
			for (int i = 0; i < sb.length(); i++) {
				char c = sb.charAt(i);
				//handle (#,##0_);
				if (c == '(') {
					int idx = sb.indexOf(")", i);
					if (idx > -1 && sb.charAt(idx -1) == '_') {
						sb.deleteCharAt(idx);
						sb.deleteCharAt(idx - 1);
						sb.deleteCharAt(i);
						i--;
					}
				} else if (c == ')' && i > 0 && sb.charAt(i - 1) == '_') {
					sb.deleteCharAt(i);
					sb.deleteCharAt(i - 1);
					i--;
				// remove quotes and back slashes
				} else if (c == '\\' || c == '"') {
					sb.deleteCharAt(i);
					i--;
					
				// for scientific/engineering notation
				} else if (c == '+' && i > 0 && sb.charAt(i - 1) == 'E') {
					sb.deleteCharAt(i);
					i--;
				}							
			}
			formatStr = sb.toString();
			try {
				returnVal = new DecimalFormat(formatStr);				
			} catch(IllegalArgumentException iae) {

				// the pattern could not be parsed correctly,
				// so fall back to the default number format
				return getDefaultFormat(cellValue);
			}
		}
		return returnVal;
	}
	
	/**
	 * Return true if the double value represents a whole number
	 * @param d the double value to check
	 * @return true if d is a whole number
	 */
	private static boolean isWholeNumber(double d) {
		return d == Math.floor(d);
	}
	
	/**
	 * Returns a default format for a cell.
	 * @param cell The cell
	 * @return a default format
	 */
	protected Format getDefaultFormat(HSSFCell cell) {
		return getDefaultFormat(cell.getNumericCellValue());
	}
	private Format getDefaultFormat(double cellValue) {
		// for numeric cells try user supplied default
		if (defaultNumFormat != null) {
			return defaultNumFormat;
			
		  // otherwise use general format	
		} else if (isWholeNumber(cellValue)){
			return generalWholeNumFormat;
		} else {
			return generalDecimalNumFormat;
		}
	}
	
	/**
	 * Returns the formatted value of an Excel date as a <tt>String</tt> based
	 * on the cell's <code>DataFormat</code>. i.e. "Thursday, January 02, 2003"
	 * , "01/02/2003" , "02-Jan" , etc.
	 * 
	 * @param cell The cell
	 * @return a formatted date string
	 */      
    protected String getFormattedDateString(HSSFCell cell) {
    	Format dateFormat = getFormat(cell);
    	Date d = cell.getDateCellValue();
    	if (dateFormat != null) {
    		return dateFormat.format(d);
    	} else {
    		return d.toString();
    	}
    }
    
	/**
	 * Returns the formatted value of an Excel number as a <tt>String</tt>
	 * based on the cell's <code>DataFormat</code>. Supported formats include
	 * currency, percents, decimals, phone number, SSN, etc.:
	 * "61.54%", "$100.00", "(800) 555-1234".
	 * 
	 * @param cell The cell
	 * @return a formatted number string
	 */ 
    protected String getFormattedNumberString(HSSFCell cell) {
    	
    	Format numberFormat = getFormat(cell);
    	double d = cell.getNumericCellValue();
    	if (numberFormat != null) {
    		return numberFormat.format(new Double(d));
    	} else {
    		return String.valueOf(d);
    	} 	
    }

    /**
     * Formats the given raw cell value, based on the supplied
     *  format index and string, according to excel style rules.
     * @see #formatCellValue(HSSFCell)
     */
    public String formatRawCellContents(double value, int formatIndex, String formatString) {
    	// Is it a date?
    	if(HSSFDateUtil.isADateFormat(formatIndex,formatString) &&
    			HSSFDateUtil.isValidExcelDate(value)) {
    		
        	Format dateFormat = getFormat(value, formatIndex, formatString);
        	Date d = HSSFDateUtil.getJavaDate(value);
        	if (dateFormat != null) {
        		return dateFormat.format(d);
        	} else {
        		return d.toString();
        	}
    	} else {
    		// Number
        	Format numberFormat = getFormat(value, formatIndex, formatString);
        	if (numberFormat != null) {
        		return numberFormat.format(new Double(value));
        	} else {
        		return String.valueOf(value);
        	} 	
    	}
    }
    
	/**
	 * <p>
	 * Returns the formatted value of a cell as a <tt>String</tt> regardless
	 * of the cell type. If the Excel format pattern cannot be parsed then the
	 * cell value will be formatted using a default format. 
	 * </p>
	 * <p>When passed a null or blank cell, this method will return an empty
	 * String (""). Formulas in formula type cells will not be evaluated.
	 * </p>
	 * 
	 * @param cell The cell
	 * @return the formatted cell value as a String
	 */
	public String formatCellValue(HSSFCell cell) {
		return formatCellValue(cell, null);
	}
    
	/**
	 * <p>
	 * Returns the formatted value of a cell as a <tt>String</tt> regardless
	 * of the cell type. If the Excel format pattern cannot be parsed then the
	 * cell value will be formatted using a default format. 
	 * </p>
	 * <p>When passed a null or blank cell, this method will return an empty
	 * String (""). Formula cells will be evaluated using the given
	 * {@link HSSFFormulaEvaluator} if the evaluator is non-null. If the
	 * evaluator is null, then the formula String will be returned. The caller
	 * is responsible for setting the currentRow on the evaluator, otherwise an
	 * IllegalArgumentException may be thrown.
	 *</p>
	 * 
	 * @param cell The cell
	 * @param evaluator The HSSFFormulaEvaluator (can be null)
	 * @return a string value of the cell
	 * @throws IllegalArgumentException if cell type is <code>
	 * HSSFCell.CELL_TYPE_FORMULA</code> <b>and</b> evaluator is not null
	 * <b>and</b> the evlaluator's currentRow has not been set.
	 */
	public String formatCellValue(HSSFCell cell, 
			HSSFFormulaEvaluator evaluator) throws IllegalArgumentException {

		String value = "";
		if (cell == null) {
			return value;
		}
		
		int cellType = cell.getCellType();
		if (evaluator != null && cellType == HSSFCell.CELL_TYPE_FORMULA) {
			try {
				cellType = evaluator.evaluateFormulaCell(cell);
			} catch (Throwable t) {
				throw new IllegalArgumentException("Did you forget to set the current" +
						" row on the HSSFFormulaEvaluator?", t);
			}
		}		
		switch (cellType)
        {
            case HSSFCell.CELL_TYPE_FORMULA :
            	// should only occur if evaluator is null
            	value = cell.getCellFormula();
                break;

            case HSSFCell.CELL_TYPE_NUMERIC :
            	
            	if (HSSFDateUtil.isCellDateFormatted(cell)) {
            		value = getFormattedDateString(cell);
            	} else {
            		value = getFormattedNumberString(cell);
            	}
                break;

            case HSSFCell.CELL_TYPE_STRING :
                value = cell.getRichStringCellValue().getString();
                break;
                
            case HSSFCell.CELL_TYPE_BOOLEAN :
            	value = String.valueOf(cell.getBooleanCellValue());
        }
		return value;
	}
		
	
	/**
	 * <p>
	 * Sets a default number format to be used when the Excel format cannot be
	 * parsed successfully. <b>Note:</b> This is a fall back for when an error
	 * occurs while parsing an Excel number format pattern. This will not
	 * affect cells with the <em>General</em> format.
	 * </p>
	 * <p>
	 * The value that will be passed to the Format's format method (specified
	 * by <code>java.text.Format#format</code>) will be a double value from a
	 * numeric cell. Therefore the code in the format method should expect a
	 * <code>Number</code> value.
	 * </p>
	 * 
	 * @param format A Format instance to be used as a default
	 * @see java.text.Format#format
	 */
	public void setDefaultNumberFormat(Format format) {
		Iterator itr = formats.entrySet().iterator();
		while(itr.hasNext()) {
			Map.Entry entry = (Map.Entry)itr.next();
			if (entry.getValue() == generalDecimalNumFormat
					|| entry.getValue() == generalWholeNumFormat) {
				entry.setValue(format);
			}
		}
		defaultNumFormat = format;
	}	
	
	/**
	 * Adds a new format to the available formats.
	 * <p>
	 * The value that will be passed to the Format's format method (specified
	 * by <code>java.text.Format#format</code>) will be a double value from a
	 * numeric cell. Therefore the code in the format method should expect a
	 * <code>Number</code> value.
	 * </p>	 
	 * @param excelFormatStr The data format string
	 * @param format A Format instance
	 */
	public void addFormat(String excelFormatStr, Format format) {
		formats.put(excelFormatStr, format);
	}

	// Some custom formats
	
	/**
	 * Format class for Excel's SSN format. This class mimics Excel's built-in
	 * SSN formatting.
	 * 
	 * @author James May
	 */
	static class SSNFormat extends Format {
		private DecimalFormat df;
		
		/** Constructor */
		public SSNFormat() {
			df = new DecimalFormat("000000000");
			df.setParseIntegerOnly(true);
		}
		
		/** Format a number as an SSN */
		public String format(Number num) {
			String result = df.format(num);
			StringBuffer sb = new StringBuffer();
			sb.append(result.substring(0, 3)).append('-');
			sb.append(result.substring(3, 5)).append('-');
			sb.append(result.substring(5, 9));
			return sb.toString();
		}
		
		public StringBuffer format(Object obj, StringBuffer toAppendTo,
				FieldPosition pos) {
			return toAppendTo.append(format((Number)obj));
		}
		
		public Object parseObject(String source, ParsePosition pos) {
			return df.parseObject(source, pos);
		}
	}
	
	/**
	 * Format class for Excel Zip + 4 format. This class mimics Excel's
	 * built-in formatting for Zip + 4.
	 * @author James May
	 */	
	static class ZipPlusFourFormat extends Format {
		private DecimalFormat df;
		
		/** Constructor */
		public ZipPlusFourFormat() {
			df = new DecimalFormat("000000000");
			df.setParseIntegerOnly(true);
		}
		
		/** Format a number as Zip + 4 */
		public String format(Number num) {
			String result = df.format(num);
			StringBuffer sb = new StringBuffer();
			sb.append(result.substring(0, 5)).append('-');
			sb.append(result.substring(5, 9));
			return sb.toString();
		}
		
		public StringBuffer format(Object obj, StringBuffer toAppendTo,
				FieldPosition pos) {
			return toAppendTo.append(format((Number)obj));
		}
		
		public Object parseObject(String source, ParsePosition pos) {
			return df.parseObject(source, pos);
		}
	}	
	
	/**
	 * Format class for Excel phone number format. This class mimics Excel's
	 * built-in phone number formatting.
	 * @author James May
	 */	
	static class PhoneFormat extends Format {
		private DecimalFormat df;

		/** Constructor */
		public PhoneFormat() {
			df = new DecimalFormat("##########");
			df.setParseIntegerOnly(true);
		}
		
		/** Format a number as a phone number */
		public String format(Number num) {
			String result = df.format(num);
			StringBuffer sb = new StringBuffer();
			String seg1, seg2, seg3;
			int len = result.length();
			if (len <= 4) {
				return result;
			}
			
			seg3 = result.substring(len - 4, len);
			seg2 = result.substring(Math.max(0, len - 7), len - 4); 
			seg1 = result.substring(Math.max(0, len - 10), Math.max(0, len - 7));
			
			if(seg1 != null && seg1.trim().length() > 0) {
				sb.append('(').append(seg1).append(") ");
			}
			if(seg2 != null && seg2.trim().length() > 0) {
				sb.append(seg2).append('-'); 
			}
			sb.append(seg3);
			return sb.toString();
		}
		
		public StringBuffer format(Object obj, StringBuffer toAppendTo,
				FieldPosition pos) {
			return toAppendTo.append(format((Number)obj));
		}
		
		public Object parseObject(String source, ParsePosition pos) {
			return df.parseObject(source, pos);
		}
	}	
}
