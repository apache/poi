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
        

package org.apache.poi.hssf.record.formula;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Formats sheet names for use in formula expressions.
 * 
 * @author Josh Micich
 */
final class SheetNameFormatter {
	
	private static final String BIFF8_LAST_COLUMN = "IV";
	private static final int BIFF8_LAST_COLUMN_TEXT_LEN = BIFF8_LAST_COLUMN.length();
	private static final String BIFF8_LAST_ROW = String.valueOf(0x10000);
	private static final int BIFF8_LAST_ROW_TEXT_LEN = BIFF8_LAST_ROW.length();

	private static final char DELIMITER = '\'';
	
	private static final Pattern CELL_REF_PATTERN = Pattern.compile("([A-Za-z])+[0-9]+");

	private SheetNameFormatter() {
		// no instances of this class
	}
	/**
	 * Used to format sheet names as they would appear in cell formula expressions.
	 * @return the sheet name unchanged if there is no need for delimiting.  Otherwise the sheet
	 * name is enclosed in single quotes (').  Any single quotes which were already present in the 
	 * sheet name will be converted to double single quotes ('').  
	 */
	public static String format(String rawSheetName) {
		StringBuffer sb = new StringBuffer(rawSheetName.length() + 2);
		appendFormat(sb, rawSheetName);
		return sb.toString();
	}
	
	/**
	 * Convenience method for when a StringBuffer is already available
	 * 
	 * @param out - sheet name will be appended here possibly with delimiting quotes 
	 */
	public static void appendFormat(StringBuffer out, String rawSheetName) {
		boolean needsQuotes = needsDelimiting(rawSheetName);
		if(needsQuotes) {
			out.append(DELIMITER);
			appendAndEscape(out, rawSheetName);
			out.append(DELIMITER);
		} else {
			out.append(rawSheetName);
		}
	}

	private static void appendAndEscape(StringBuffer sb, String rawSheetName) {
		int len = rawSheetName.length();
		for(int i=0; i<len; i++) {
			char ch = rawSheetName.charAt(i);
			if(ch == DELIMITER) {
				// single quotes (') are encoded as ('')
				sb.append(DELIMITER);
			}
			sb.append(ch);
		}
	}

	private static boolean needsDelimiting(String rawSheetName) {
		int len = rawSheetName.length();
		if(len < 1) {
			throw new RuntimeException("Zero length string is an invalid sheet name");
		}
		if(Character.isDigit(rawSheetName.charAt(0))) {
			// sheet name with digit in the first position always requires delimiting
			return true;
		}
		for(int i=0; i<len; i++) {
			char ch = rawSheetName.charAt(i);
			if(isSpecialChar(ch)) {
				return true;
			}
		}
		if(Character.isLetter(rawSheetName.charAt(0))
				&& Character.isDigit(rawSheetName.charAt(len-1))) {
			// note - values like "A$1:$C$20" don't get this far 
			if(nameLooksLikePlainCellReference(rawSheetName)) {
				return true;
			}
		}
		return false;
	}
	
    /**
     * @return <code>true</code> if the presence of the specified character in a sheet name would 
     * require the sheet name to be delimited in formulas.  This includes every non-alphanumeric 
     * character besides underscore '_'.
     */
    /* package */ static boolean isSpecialChar(char ch) {
        // note - Character.isJavaIdentifierPart() would allow dollars '$'
        if(Character.isLetterOrDigit(ch)) {
            return false;
        }
        switch(ch) {
            case '_': // underscore is ok
                return false;
            case '\n':
            case '\r':
            case '\t':
                throw new RuntimeException("Illegal character (0x" 
                        + Integer.toHexString(ch) + ") found in sheet name");
        }
        return true;
    }
	

	/**
	 * Used to decide whether sheet names like 'AB123' need delimiting due to the fact that they 
	 * look like cell references.
	 * <p/>
	 * This code is currently being used for translating formulas represented with <code>Ptg</code>
	 * tokens into human readable text form.  In formula expressions, a sheet name always has a 
	 * trailing '!' so there is little chance for ambiguity.  It doesn't matter too much what this 
	 * method returns but it is worth noting the likely consumers of these formula text strings:
	 * <ol>
	 * <li>POI's own formula parser</li>
	 * <li>Visual reading by human</li>
	 * <li>VBA automation entry into Excel cell contents e.g.  ActiveCell.Formula = "=c64!A1"</li>
	 * <li>Manual entry into Excel cell contents</li>
	 * <li>Some third party formula parser</li>
	 * </ol>
	 * 
	 * At the time of writing, POI's formula parser tolerates cell-like sheet names in formulas
	 * with or without delimiters.  The same goes for Excel(2007), both manual and automated entry.  
	 * <p/>
	 * For better or worse this implementation attempts to replicate Excel's formula renderer.
	 * Excel uses range checking on the apparent 'row' and 'column' components.  Note however that
	 * the maximum sheet size varies across versions:
	 * <p/>
	 * <blockquote><table border="0" cellpadding="1" cellspacing="0" 
	 *                 summary="Notable cases.">
	 *   <tr><th>Version&nbsp;&nbsp;</th><th>File Format&nbsp;&nbsp;</th>
	 *   	<th>Last Column&nbsp;&nbsp;</th><th>Last Row</th></tr>
	 *   <tr><td>97-2003</td><td>BIFF8</td><td>"IV" (2^8)</td><td>65536 (2^14)</td></tr>
	 *   <tr><td>2007</td><td>BIFF12</td><td>"XFD" (2^14)</td><td>1048576 (2^20)</td></tr>
	 * </table></blockquote>
	 * POI currently targets BIFF8 (Excel 97-2003), so the following behaviour can be observed for
	 * this method:
	 * <blockquote><table border="0" cellpadding="1" cellspacing="0" 
	 *                 summary="Notable cases.">
	 *   <tr><th>Input&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th>
	 *   	<th>Result&nbsp;</th></tr>
	 *   <tr><td>"A1", 1</td><td>true</td></tr>
	 *   <tr><td>"a111", 1</td><td>true</td></tr>
	 *   <tr><td>"A65536", 1</td><td>true</td></tr>
	 *   <tr><td>"A65537", 1</td><td>false</td></tr>
	 *   <tr><td>"iv1", 2</td><td>true</td></tr>
	 *   <tr><td>"IW1", 2</td><td>false</td></tr>
	 *   <tr><td>"AAA1", 3</td><td>false</td></tr>
	 *   <tr><td>"a111", 1</td><td>true</td></tr>
	 *   <tr><td>"Sheet1", 6</td><td>false</td></tr>
	 * </table></blockquote>
	 */
	/* package */ static boolean cellReferenceIsWithinRange(String rawSheetName, int numberOfLetters) {
		
		if(numberOfLetters > BIFF8_LAST_COLUMN_TEXT_LEN) {
			// "Sheet1" case etc
			return false; // that was easy
		}
		int nDigits = rawSheetName.length() - numberOfLetters;
		if(nDigits > BIFF8_LAST_ROW_TEXT_LEN) {
			return false; 
		}
		if(numberOfLetters == BIFF8_LAST_COLUMN_TEXT_LEN) {
			String colStr = rawSheetName.substring(0, BIFF8_LAST_COLUMN_TEXT_LEN).toUpperCase();
			if(colStr.compareTo(BIFF8_LAST_COLUMN) > 0) {
				return false;
			}
		} else {
			// apparent column name has less chars than max
			// no need to check range
		}
		
		if(nDigits == BIFF8_LAST_ROW_TEXT_LEN) {
			String colStr = rawSheetName.substring(numberOfLetters);
			// ASCII comparison is valid if digit count is same
			if(colStr.compareTo(BIFF8_LAST_ROW) > 0) {
				return false;
			}
		} else {
			// apparent row has less chars than max
			// no need to check range
		}
		
		return true;
	}

	/**
	 * Note - this method assumes the specified rawSheetName has only letters and digits.  It 
	 * cannot be used to match absolute or range references (using the dollar or colon char).
	 * <p/>
	 * Some notable cases:
	 *    <blockquote><table border="0" cellpadding="1" cellspacing="0" 
	 *                 summary="Notable cases.">
	 *      <tr><th>Input&nbsp;</th><th>Result&nbsp;</th><th>Comments</th></tr>
	 *      <tr><td>"A1"&nbsp;&nbsp;</td><td>true</td><td>&nbsp;</td></tr>
	 *      <tr><td>"a111"&nbsp;&nbsp;</td><td>true</td><td>&nbsp;</td></tr>
	 *      <tr><td>"AA"&nbsp;&nbsp;</td><td>false</td><td>&nbsp;</td></tr>
	 *      <tr><td>"aa1"&nbsp;&nbsp;</td><td>true</td><td>&nbsp;</td></tr>
	 *      <tr><td>"A1A"&nbsp;&nbsp;</td><td>false</td><td>&nbsp;</td></tr>
	 *      <tr><td>"A1A1"&nbsp;&nbsp;</td><td>false</td><td>&nbsp;</td></tr>
	 *      <tr><td>"A$1:$C$20"&nbsp;&nbsp;</td><td>false</td><td>Not a plain cell reference</td></tr>
	 *      <tr><td>"SALES20080101"&nbsp;&nbsp;</td><td>true</td>
	 *      		<td>Still needs delimiting even though well out of range</td></tr>
	 *    </table></blockquote>
	 *  
	 * @return <code>true</code> if there is any possible ambiguity that the specified rawSheetName
	 * could be interpreted as a valid cell name.
	 */
	/* package */ static boolean nameLooksLikePlainCellReference(String rawSheetName) {
		Matcher matcher = CELL_REF_PATTERN.matcher(rawSheetName);
		if(!matcher.matches()) {
			return false;
		}
		
		// rawSheetName == "Sheet1" gets this far.
		String lettersPrefix = matcher.group(1);
		return cellReferenceIsWithinRange(rawSheetName, lettersPrefix.length());
	}

}
