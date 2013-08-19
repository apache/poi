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

package org.apache.poi.ss.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.record.formula.SheetNameFormatter;

/**
 *
 * @author  Avik Sengupta
 * @author  Dennis Doubleday (patch to seperateRowColumns())
 */
public class CellReference {
	/**
	 * Used to classify identifiers found in formulas as cell references or not.
	 */
	public static final class NameType {
		public static final int CELL = 1;
		public static final int NAMED_RANGE = 2;
		public static final int COLUMN = 3;
		public static final int BAD_CELL_OR_NAMED_RANGE = -1;
	}

	/** The character ($) that signifies a row or column value is absolute instead of relative */ 
	private static final char ABSOLUTE_REFERENCE_MARKER = '$';
	/** The character (!) that separates sheet names from cell references */ 
	private static final char SHEET_NAME_DELIMITER = '!';
	/** The character (') used to quote sheet names when they contain special characters */
	private static final char SPECIAL_NAME_DELIMITER = '\'';
	
	/**
	 * Matches a run of one or more letters followed by a run of one or more digits.
	 * The run of letters is group 1 and the run of digits is group 2.  
	 * Each group may optionally be prefixed with a single '$'.
	 */
	private static final Pattern CELL_REF_PATTERN = Pattern.compile("\\$?([A-Za-z]+)\\$?([0-9]+)");
	/**
	 * Matches a run of one or more letters.  The run of letters is group 1.  
	 * The text may optionally be prefixed with a single '$'.
	 */
	private static final Pattern COLUMN_REF_PATTERN = Pattern.compile("\\$?([A-Za-z]+)");
	/**
	 * Named range names must start with a letter or underscore.  Subsequent characters may include
	 * digits or dot.  (They can even end in dot).
	 */
	private static final Pattern NAMED_RANGE_NAME_PATTERN = Pattern.compile("[_A-Za-z][_.A-Za-z0-9]*");
	private static final String BIFF8_LAST_COLUMN = "IV";
	private static final int BIFF8_LAST_COLUMN_TEXT_LEN = BIFF8_LAST_COLUMN.length();
	private static final String BIFF8_LAST_ROW = String.valueOf(0x10000);
	private static final int BIFF8_LAST_ROW_TEXT_LEN = BIFF8_LAST_ROW.length();

    private final int _rowIndex;
    private final int _colIndex;
    private final String _sheetName;
    private final boolean _isRowAbs;
    private final boolean _isColAbs;

	/**
	 * Create an cell ref from a string representation.  Sheet names containing special characters should be
	 * delimited and escaped as per normal syntax rules for formulas.
	 */
	public CellReference(String cellRef) {
		String[] parts = separateRefParts(cellRef);
		_sheetName = parts[0];
		String colRef = parts[1]; 
		if (colRef.length() < 1) {
			throw new IllegalArgumentException("Invalid Formula cell reference: '"+cellRef+"'");
		}
		_isColAbs = colRef.charAt(0) == '$';
		if (_isColAbs) {
			colRef=colRef.substring(1);
		}
		_colIndex = convertColStringToIndex(colRef);
		
		String rowRef=parts[2];
		if (rowRef.length() < 1) {
			throw new IllegalArgumentException("Invalid Formula cell reference: '"+cellRef+"'");
		}
		_isRowAbs = rowRef.charAt(0) == '$';
		if (_isRowAbs) {
			rowRef=rowRef.substring(1);
		}
		_rowIndex = Integer.parseInt(rowRef)-1; // -1 to convert 1-based to zero-based
	}

	public CellReference(int pRow, int pCol) {
		this(pRow, pCol, false, false);
	}
	public CellReference(int pRow, short pCol) {
		this(pRow, pCol & 0xFFFF, false, false);
	}

	public CellReference(int pRow, int pCol, boolean pAbsRow, boolean pAbsCol) {
		this(null, pRow, pCol, pAbsRow, pAbsCol);
	}
	public CellReference(String pSheetName, int pRow, int pCol, boolean pAbsRow, boolean pAbsCol) {
		// TODO - "-1" is a special value being temporarily used for whole row and whole column area references.
		// so these checks are currently N.Q.R.
		if(pRow < -1) {
			throw new IllegalArgumentException("row index may not be negative");
		}
		if(pCol < -1) {
			throw new IllegalArgumentException("column index may not be negative");
		}
		_sheetName = pSheetName;
		_rowIndex=pRow;
		_colIndex=pCol;
		_isRowAbs = pAbsRow;
		_isColAbs=pAbsCol;
	}

	public int getRow(){return _rowIndex;}
	public short getCol(){return (short) _colIndex;}
	public boolean isRowAbsolute(){return _isRowAbs;}
	public boolean isColAbsolute(){return _isColAbs;}
	/**
	  * @return possibly <code>null</code> if this is a 2D reference.  Special characters are not
	  * escaped or delimited
	  */
	public String getSheetName(){
		return _sheetName;
	}
	
	public static boolean isPartAbsolute(String part) {
		return part.charAt(0) == ABSOLUTE_REFERENCE_MARKER;
	}
	/**
	 * takes in a column reference portion of a CellRef and converts it from
	 * ALPHA-26 number format to 0-based base 10.
	 * 'A' -> 0
	 * 'Z' -> 25
	 * 'AA' -> 26
	 * 'IV' -> 255
	 * @return zero based column index
	 */
	public static int convertColStringToIndex(String ref) {
		ref = ref.toUpperCase();
		int count = 0;

		for (char c : ref.toCharArray()) {
			count = count * 26 + (c - 'A' + 1);
		}

		return count - 1;
	}

	/**
	 * Classifies an identifier as either a simple (2D) cell reference or a named range name
	 * @return one of the values from <tt>NameType</tt> 
	 */
	public static int classifyCellReference(String str) {
		int len = str.length();
		if (len < 1) {
			throw new IllegalArgumentException("Empty string not allowed");
		}
		char firstChar = str.charAt(0);
		switch (firstChar) {
			case ABSOLUTE_REFERENCE_MARKER:
			case '.':
			case '_':
				break;
			default:
				if (!Character.isLetter(firstChar)) {
					throw new IllegalArgumentException("Invalid first char (" + firstChar 
							+ ") of cell reference or named range.  Letter expected");
				}
		}
		if (!Character.isDigit(str.charAt(len-1))) {
			// no digits at end of str
			return validateNamedRangeName(str);
		}
		Matcher cellRefPatternMatcher = CELL_REF_PATTERN.matcher(str);
		if (!cellRefPatternMatcher.matches()) {
			return validateNamedRangeName(str);
		}
		String lettersGroup = cellRefPatternMatcher.group(1);
		String digitsGroup = cellRefPatternMatcher.group(2);
		if (cellReferenceIsWithinRange(lettersGroup, digitsGroup)) {
			// valid cell reference
			return NameType.CELL;
		}
		// If str looks like a cell reference, but is out of (row/col) range, it is a valid
		// named range name
		// This behaviour is a little weird.  For example, "IW123" is a valid named range name
		// because the column "IW" is beyond the maximum "IV".  Note - this behaviour is version
		// dependent.  In BIFF12, "IW123" is not a valid named range name, but in BIFF8 it is.
		if (str.indexOf(ABSOLUTE_REFERENCE_MARKER) >= 0) {
			// Of course, named range names cannot have '$'
			return NameType.BAD_CELL_OR_NAMED_RANGE;
		}
		return NameType.NAMED_RANGE;
	}

	private static int validateNamedRangeName(String str) {
		Matcher colMatcher = COLUMN_REF_PATTERN.matcher(str);
		if (colMatcher.matches()) {
			String colStr = colMatcher.group(1);
			if (isColumnWithnRange(colStr)) {
				return NameType.COLUMN;
			}
		}
		if (!NAMED_RANGE_NAME_PATTERN.matcher(str).matches()) {
			return NameType.BAD_CELL_OR_NAMED_RANGE;
		}
		return NameType.NAMED_RANGE;
	}
	
	
	/**
	 * Used to decide whether a name of the form "[A-Z]*[0-9]*" that appears in a formula can be 
	 * interpreted as a cell reference.  Names of that form can be also used for sheets and/or
	 * named ranges, and in those circumstances, the question of whether the potential cell 
	 * reference is valid (in range) becomes important.
	 * <p/>
	 * Note - that the maximum sheet size varies across Excel versions:
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
	 *       <th>Result&nbsp;</th></tr>
	 *   <tr><td>"A", "1"</td><td>true</td></tr>
	 *   <tr><td>"a", "111"</td><td>true</td></tr>
	 *   <tr><td>"A", "65536"</td><td>true</td></tr>
	 *   <tr><td>"A", "65537"</td><td>false</td></tr>
	 *   <tr><td>"iv", "1"</td><td>true</td></tr>
	 *   <tr><td>"IW", "1"</td><td>false</td></tr>
	 *   <tr><td>"AAA", "1"</td><td>false</td></tr>
	 *   <tr><td>"a", "111"</td><td>true</td></tr>
	 *   <tr><td>"Sheet", "1"</td><td>false</td></tr>
	 * </table></blockquote>
	 * 
	 * @param colStr a string of only letter characters
	 * @param rowStr a string of only digit characters
	 * @return <code>true</code> if the row and col parameters are within range of a BIFF8 spreadsheet.
	 */
	public static boolean cellReferenceIsWithinRange(String colStr, String rowStr) {
		if (!isColumnWithnRange(colStr)) {
			return false;
		}
		int nDigits = rowStr.length();
		if(nDigits > BIFF8_LAST_ROW_TEXT_LEN) {
			return false; 
		}
		
		if(nDigits == BIFF8_LAST_ROW_TEXT_LEN) {
			// ASCII comparison is valid if digit count is same
			if(rowStr.compareTo(BIFF8_LAST_ROW) > 0) {
				return false;
			}
		} else {
			// apparent row has less chars than max
			// no need to check range
		}
		
		return true;
	}

	private static boolean isColumnWithnRange(String colStr) {
		int numberOfLetters = colStr.length();
		if(numberOfLetters > BIFF8_LAST_COLUMN_TEXT_LEN) {
			// "Sheet1" case etc
			return false; // that was easy
		}
		if(numberOfLetters == BIFF8_LAST_COLUMN_TEXT_LEN) {
			if(colStr.toUpperCase().compareTo(BIFF8_LAST_COLUMN) > 0) {
				return false;
			}
		} else {
			// apparent column name has less chars than max
			// no need to check range
		}
		return true;
	}

	/**
	 * Separates the row from the columns and returns an array of three Strings.  The first element
	 * is the sheet name. Only the first element may be null.  The second element in is the column 
	 * name still in ALPHA-26 number format.  The third element is the row.
	 */
	private static String[] separateRefParts(String reference) {
		
		int plingPos = reference.lastIndexOf(SHEET_NAME_DELIMITER);
		String sheetName = parseSheetName(reference, plingPos);
		int start = plingPos+1;

		int length = reference.length();


		int loc = start;
		// skip initial dollars 
		if (reference.charAt(loc)==ABSOLUTE_REFERENCE_MARKER) {
			loc++;
		}
		// step over column name chars until first digit (or dollars) for row number.
		for (; loc < length; loc++) {
			char ch = reference.charAt(loc);
			if (Character.isDigit(ch) || ch == ABSOLUTE_REFERENCE_MARKER) {
				break;
			}
		}
		return new String[] {
		   sheetName,
		   reference.substring(start,loc),
		   reference.substring(loc),
		};
	}

	private static String parseSheetName(String reference, int indexOfSheetNameDelimiter) {
		if(indexOfSheetNameDelimiter < 0) {
			return null;
		}
		
		boolean isQuoted = reference.charAt(0) == SPECIAL_NAME_DELIMITER;
		if(!isQuoted) {
			return reference.substring(0, indexOfSheetNameDelimiter);
		}
		int lastQuotePos = indexOfSheetNameDelimiter-1;
		if(reference.charAt(lastQuotePos) != SPECIAL_NAME_DELIMITER) {
			throw new RuntimeException("Mismatched quotes: (" + reference + ")");
		}

		// TODO - refactor cell reference parsing logic to one place.
		// Current known incarnations: 
		//   FormulaParser.GetName()
		//   CellReference.parseSheetName() (here)
		//   AreaReference.separateAreaRefs() 
		//   SheetNameFormatter.format() (inverse)
		
		StringBuffer sb = new StringBuffer(indexOfSheetNameDelimiter);
		
		for(int i=1; i<lastQuotePos; i++) { // Note boundaries - skip outer quotes
			char ch = reference.charAt(i);
			if(ch != SPECIAL_NAME_DELIMITER) {
				sb.append(ch);
				continue;
			}
			if(i < lastQuotePos) {
				if(reference.charAt(i+1) == SPECIAL_NAME_DELIMITER) {
					// two consecutive quotes is the escape sequence for a single one
					i++; // skip this and keep parsing the special name
					sb.append(ch);
					continue;
				}
			}
			throw new RuntimeException("Bad sheet name quote escaping: (" + reference + ")");
		}
		return sb.toString();
	}

	/**
	 * Takes in a 0-based base-10 column and returns a ALPHA-26
	 *  representation.
	 * eg column #3 -> D
	 */
	protected static String convertNumToColString(int col) {
		col += 1;
		StringBuilder sb = new StringBuilder();
		sb.setLength(0);

		while (col > 0) {
			final int j = (i % 26);
			sb.append("" + (char) ('A' + (j == 0 ? 25 : (j - 1))));
			col = (col - 1) / 26;
		}

		return sb.reverse().toString();
	}

	/**
	 *  Example return values:
	 *	<table border="0" cellpadding="1" cellspacing="0" summary="Example return values">
	 *	  <tr><th align='left'>Result</th><th align='left'>Comment</th></tr>
	 *	  <tr><td>A1</td><td>Cell reference without sheet</td></tr>
	 *	  <tr><td>Sheet1!A1</td><td>Standard sheet name</td></tr>
	 *	  <tr><td>'O''Brien''s Sales'!A1'&nbsp;</td><td>Sheet name with special characters</td></tr>
	 *	</table>
	 * @return the text representation of this cell reference as it would appear in a formula.
	 */
	public String formatAsString() {
		StringBuffer sb = new StringBuffer(32);
		if(_sheetName != null) {
			SheetNameFormatter.appendFormat(sb, _sheetName);
			sb.append(SHEET_NAME_DELIMITER);
		}
		appendCellReference(sb);
		return sb.toString();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(64);
		sb.append(getClass().getName()).append(" [");
		sb.append(formatAsString());
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Returns the three parts of the cell reference, the
	 *  Sheet name (or null if none supplied), the 1 based
	 *  row number, and the A based column letter.
	 * This will not include any markers for absolute
	 *  references, so use {@link #formatAsString()}
	 *  to properly turn references into strings. 
	 */
	public String[] getCellRefParts() {
		return new String[] {
				_sheetName,
				Integer.toString(_rowIndex+1),
				convertNumToColString(_colIndex)
		};
	}

	/**
	 * Appends cell reference with '$' markers for absolute values as required.
	 * Sheet name is not included.
	 */
	/* package */ void appendCellReference(StringBuffer sb) {
		if(_isColAbs) {
			sb.append(ABSOLUTE_REFERENCE_MARKER);
		}
		sb.append( convertNumToColString(_colIndex));
		if(_isRowAbs) {
			sb.append(ABSOLUTE_REFERENCE_MARKER);
		}
		sb.append(_rowIndex+1);
	}
}
