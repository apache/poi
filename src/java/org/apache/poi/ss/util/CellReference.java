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

import org.apache.poi.hssf.record.formula.SheetNameFormatter;

/**
 * Common convertion functions between Excel style A1, C27 style
 *  cell references, and POI usermodel style row=0, column=0
 *  style references.
 * Applys for both HSSF and XSSF.
 * @author  Avik Sengupta
 * @author  Dennis Doubleday (patch to seperateRowColumns())
 */
public class CellReference {
    /** The character ($) that signifies a row or column value is absolute instead of relative */ 
    private static final char ABSOLUTE_REFERENCE_MARKER = '$';
    /** The character (!) that separates sheet names from cell references */ 
    private static final char SHEET_NAME_DELIMITER = '!';
    /** The character (') used to quote sheet names when they contain special characters */
    private static final char SPECIAL_NAME_DELIMITER = '\'';
    

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
        _colIndex = convertColStringToNum(colRef);
        
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

    /**
     * Creates a cell reference for the given row and cell.
     * Assumes these references are relative
     */
    public CellReference(int row, int col) {
    	this(row, col, false, false);
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
    public int getCol(){return _colIndex;}
    public boolean isRowAbsolute(){return _isRowAbs;}
    public boolean isColAbsolute(){return _isColAbs;}
    /**
      * @return possibly <code>null</code> if this is a 2D reference.  Special characters are not
      * escaped or delimited
      */
    public String getSheetName(){
        return _sheetName;
    }
    
    /**
     * takes in a column reference portion of a CellRef and converts it from
     * ALPHA-26 number format to 0-based base 10.
     * ALPHA-26 goes A to Z, then AA to AZ, BA to BZ, ..., ZA to ZZ, 
     *  AAA to AAZ, ABA to ABZ, ..., AZA to AZZ, BAA to BAZ etc
     */
    private int convertColStringToNum(String ref) {
        int lastIx = ref.length()-1;
        int retval=0;
        int pos = 0;

        for (int k = lastIx; k > -1; k--) {
            char thechar = ref.charAt(k);
            // Character.getNumericValue() returns the values
            //  10-35 for the letter A-Z
            int shift = (int)Math.pow(26, pos);
            retval += (Character.getNumericValue(thechar)-9) * shift;
            pos++;
        }
        return retval-1;
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
    	// Excel counts column A as the 1st column, we
    	//  treat it as the 0th one
        int excelColNum = col + 1;
        
        String colRef = "";
        int colRemain = excelColNum;
        
        while(colRemain > 0) {
        	int thisPart = colRemain % 26;
        	if(thisPart == 0) { thisPart = 26; }
        	colRemain = (colRemain - thisPart) / 26;
        	
        	// The letter A is at 65
        	char colChar = (char)(thisPart+64);
        	colRef = colChar + colRef; 
        }

        return colRef;
    }

    /**
     *  Example return values:
     *    <table border="0" cellpadding="1" cellspacing="0" summary="Example return values">
     *      <tr><th align='left'>Result</th><th align='left'>Comment</th></tr>
     *      <tr><td>A1</td><td>Cell reference without sheet</td></tr>
     *      <tr><td>Sheet1!A1</td><td>Standard sheet name</td></tr>
     *      <tr><td>'O''Brien''s Sales'!A1'&nbsp;</td><td>Sheet name with special characters</td></tr>
     *    </table>
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
    protected void appendCellReference(StringBuffer sb) {
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
