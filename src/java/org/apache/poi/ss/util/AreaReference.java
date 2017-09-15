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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.util.Removal;

public class AreaReference {

    /** The character (!) that separates sheet names from cell references */ 
    private static final char SHEET_NAME_DELIMITER = '!';
    /** The character (:) that separates the two cell references in a multi-cell area reference */
    private static final char CELL_DELIMITER = ':';
    /** The character (') used to quote sheet names when they contain special characters */
    private static final char SPECIAL_NAME_DELIMITER = '\'';
    private static final SpreadsheetVersion DEFAULT_SPREADSHEET_VERSION = SpreadsheetVersion.EXCEL97;
    
    private final CellReference _firstCell;
    private final CellReference _lastCell;
    private final boolean _isSingleCell;
    private final SpreadsheetVersion _version; // never null
    
    /**
     * Create an area ref from a string representation.  Sheet names containing special characters should be
     * delimited and escaped as per normal syntax rules for formulas.<br> 
     * The area reference must be contiguous (i.e. represent a single rectangle, not a union of rectangles)
     */
    public AreaReference(String reference, SpreadsheetVersion version) {
        _version = (null != version) ? version : DEFAULT_SPREADSHEET_VERSION;
        if(! isContiguous(reference)) {
            throw new IllegalArgumentException(
                    "References passed to the AreaReference must be contiguous, " +
                    "use generateContiguous(ref) if you have non-contiguous references");
        }

        String[] parts = separateAreaRefs(reference);
        String part0 = parts[0];
        if (parts.length == 1) {
            // TODO - probably shouldn't initialize area ref when text is really a cell ref
            // Need to fix some named range stuff to get rid of this
            _firstCell = new CellReference(part0);
            
            _lastCell = _firstCell;
            _isSingleCell = true;
            return;
        }
        if (parts.length != 2) {
            throw new IllegalArgumentException("Bad area ref '" + reference + "'");
        }
        
        String part1 = parts[1];
        if (isPlainColumn(part0)) {
            if (!isPlainColumn(part1)) {
                throw new RuntimeException("Bad area ref '" + reference + "'");
            }
            // Special handling for whole-column references
            // Represented internally as x$1 to x$65536
            //  which is the maximum range of rows

            boolean firstIsAbs = CellReference.isPartAbsolute(part0);
            boolean lastIsAbs = CellReference.isPartAbsolute(part1);
            
            int col0 = CellReference.convertColStringToIndex(part0);
            int col1 = CellReference.convertColStringToIndex(part1);
            
            _firstCell = new CellReference(0, col0, true, firstIsAbs);
            _lastCell = new CellReference(0xFFFF, col1, true, lastIsAbs);
            _isSingleCell = false;
            // TODO - whole row refs
        } else {
            _firstCell = new CellReference(part0);
            _lastCell = new CellReference(part1);
            _isSingleCell = part0.equals(part1);
       }
     }
    
    private static boolean isPlainColumn(String refPart) {
        for(int i=refPart.length()-1; i>=0; i--) {
            int ch = refPart.charAt(i);
            if (ch == '$' && i==0) {
                continue;
            }
            if (ch < 'A' || ch > 'Z') {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates an area ref from a pair of Cell References.
     * @deprecated use {@link #AreaReference(CellReference, CellReference, SpreadsheetVersion)} instead
     */
    @Deprecated
    @Removal(version="3.19")
    public AreaReference(CellReference topLeft, CellReference botRight) {
        this(topLeft, botRight, DEFAULT_SPREADSHEET_VERSION);
    }
    
    /**
     * Creates an area ref from a pair of Cell References.
     */
    public AreaReference(CellReference topLeft, CellReference botRight, SpreadsheetVersion version) {
        _version = (null != version) ? version : DEFAULT_SPREADSHEET_VERSION;
        boolean swapRows = topLeft.getRow() > botRight.getRow();
        boolean swapCols = topLeft.getCol() > botRight.getCol();
        if (swapRows || swapCols) {
            int firstRow; 
            int lastRow; 
            int firstColumn; 
            int lastColumn;
            boolean firstRowAbs; 
            boolean lastRowAbs; 
            boolean firstColAbs;
            boolean lastColAbs;   
            if (swapRows) {
                firstRow = botRight.getRow();
                firstRowAbs = botRight.isRowAbsolute();
                lastRow = topLeft.getRow();
                lastRowAbs = topLeft.isRowAbsolute();
            } else {
                firstRow = topLeft.getRow();
                firstRowAbs = topLeft.isRowAbsolute();
                lastRow = botRight.getRow();
                lastRowAbs = botRight.isRowAbsolute();
            }
            if (swapCols) {
                firstColumn = botRight.getCol();
                firstColAbs = botRight.isColAbsolute();
                lastColumn = topLeft.getCol();
                lastColAbs = topLeft.isColAbsolute();
            } else {
                firstColumn = topLeft.getCol();
                firstColAbs = topLeft.isColAbsolute();
                lastColumn = botRight.getCol();
                lastColAbs = botRight.isColAbsolute();
            }
            _firstCell = new CellReference(firstRow, firstColumn, firstRowAbs, firstColAbs);
            _lastCell = new CellReference(lastRow, lastColumn, lastRowAbs, lastColAbs);
        } else {
            _firstCell = topLeft;
            _lastCell = botRight;
        }
        _isSingleCell = false;
    }

    /**
     * Is the reference for a contiguous (i.e.
     *  unbroken) area, or is it made up of
     *  several different parts?
     * (If it is, you will need to call
     *  {@link #generateContiguous(SpreadsheetVersion, String)})
     */
    public static boolean isContiguous(String reference) {
       // If there's a sheet name, strip it off
       int sheetRefEnd = reference.indexOf('!'); 
       if(sheetRefEnd != -1) {
          reference = reference.substring(sheetRefEnd);
       }

       // Check for the , as a sign of non-coniguous
       return !reference.contains(",");
    }

    public static AreaReference getWholeRow(SpreadsheetVersion version, String start, String end) {
        if (null == version) {
            version = DEFAULT_SPREADSHEET_VERSION;
        }
        return new AreaReference("$A" + start + ":$" + version.getLastColumnName() + end, version);
    }

    public static AreaReference getWholeColumn(SpreadsheetVersion version, String start, String end) {
        if (null == version) {
            version = DEFAULT_SPREADSHEET_VERSION;
        }
        return new AreaReference(start + "$1:" + end + "$" + version.getMaxRows(), version);
    }

    /**
     * Is the reference for a whole-column reference,
     *  such as C:C or D:G ?
     */
    public static boolean isWholeColumnReference(SpreadsheetVersion version, CellReference topLeft, CellReference botRight) {
        if (null == version) {
            version = DEFAULT_SPREADSHEET_VERSION; // how the code used to behave. 
        }
        
        // These are represented as something like
        //   C$1:C$65535 or D$1:F$0
        // i.e. absolute from 1st row to 0th one
        if(topLeft.getRow() == 0 && topLeft.isRowAbsolute() &&
            botRight.getRow() == version.getLastRowIndex() && botRight.isRowAbsolute()) {
            return true;
        }
        return false;
    }
    public boolean isWholeColumnReference() {
        return isWholeColumnReference(_version, _firstCell, _lastCell);
    }

    /**
     * Takes a non-contiguous area reference, and returns an array of contiguous area references
     * @return an array of contiguous area references.
     * @deprecated use {@link #generateContiguous(SpreadsheetVersion, String)} instead
     */
    @Deprecated
    @Removal(version="3.19")
    public static AreaReference[] generateContiguous(String reference) {
        return generateContiguous(DEFAULT_SPREADSHEET_VERSION, reference);
    }

    /**
     * Takes a non-contiguous area reference, and returns an array of contiguous area references
     * @return an array of contiguous area references.
     */
    public static AreaReference[] generateContiguous(SpreadsheetVersion version, String reference) {
        if (null == version) {
            version = DEFAULT_SPREADSHEET_VERSION; // how the code used to behave. 
        }
        List<AreaReference> refs = new ArrayList<AreaReference>();
        StringTokenizer st = new StringTokenizer(reference, ",");
        while(st.hasMoreTokens()) {
            refs.add(
                    new AreaReference(st.nextToken(), version)
            );
        }
        return refs.toArray(new AreaReference[refs.size()]);
    }

    /**
     * @return <code>false</code> if this area reference involves more than one cell
     */
    public boolean isSingleCell() {
        return _isSingleCell;
    }
    
    /**
     * @return the first cell reference which defines this area. Usually this cell is in the upper
     * left corner of the area (but this is not a requirement).
     */
   public CellReference getFirstCell() {
        return _firstCell;
    }
    
    /**
     * Note - if this area reference refers to a single cell, the return value of this method will
     * be identical to that of <tt>getFirstCell()</tt>
     * @return the second cell reference which defines this area.  For multi-cell areas, this is 
     * cell diagonally opposite the 'first cell'.  Usually this cell is in the lower right corner 
     * of the area (but this is not a requirement).
     */
    public CellReference getLastCell() {
        return _lastCell;
    }
    /**
     * Returns a reference to every cell covered by this area
     */
    public CellReference[] getAllReferencedCells() {
        // Special case for single cell reference
        if(_isSingleCell) {
            return  new CellReference[] { _firstCell, };
        }
 
        // Interpolate between the two
        int minRow = Math.min(_firstCell.getRow(), _lastCell.getRow());
        int maxRow = Math.max(_firstCell.getRow(), _lastCell.getRow());
        int minCol = Math.min(_firstCell.getCol(), _lastCell.getCol());
        int maxCol = Math.max(_firstCell.getCol(), _lastCell.getCol());
        String sheetName = _firstCell.getSheetName();
        
        List<CellReference> refs = new ArrayList<CellReference>();
        for(int row=minRow; row<=maxRow; row++) {
            for(int col=minCol; col<=maxCol; col++) {
                CellReference ref = new CellReference(sheetName, row, col, _firstCell.isRowAbsolute(), _firstCell.isColAbsolute());
                refs.add(ref);
            }
        }
        return refs.toArray(new CellReference[refs.size()]);
    }

    /**
     * Returns a text representation of this area reference.
     * <p>
     *  Example return values:
     *    <table border="0" cellpadding="1" cellspacing="0" summary="Example return values">
     *      <tr><th align='left'>Result</th><th align='left'>Comment</th></tr>
     *      <tr><td>A1:A1</td><td>Single cell area reference without sheet</td></tr>
     *      <tr><td>A1:$C$1</td><td>Multi-cell area reference without sheet</td></tr>
     *      <tr><td>Sheet1!A$1:B4</td><td>Standard sheet name</td></tr>
     *      <tr><td>'O''Brien''s Sales'!B5:C6'&nbsp;</td><td>Sheet name with special characters</td></tr>
     *    </table>
     * @return the text representation of this area reference as it would appear in a formula.
     */
    public String formatAsString() {
        // Special handling for whole-column references
        if(isWholeColumnReference()) {
            return
                CellReference.convertNumToColString(_firstCell.getCol())
                + ":" +
                CellReference.convertNumToColString(_lastCell.getCol());
        }
        
        StringBuffer sb = new StringBuffer(32);
        sb.append(_firstCell.formatAsString());
        if(!_isSingleCell) {
            sb.append(CELL_DELIMITER);
            if(_lastCell.getSheetName() == null) {
                sb.append(_lastCell.formatAsString());
            } else {
                // don't want to include the sheet name twice
                _lastCell.appendCellReference(sb);
            }
        }
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
     * Separates Area refs in two parts and returns them as separate elements in a String array,
     * each qualified with the sheet name (if present)
     * 
     * @return array with one or two elements. never <code>null</code>
     */
    private static String[] separateAreaRefs(String reference) {
        // TODO - refactor cell reference parsing logic to one place.
        // Current known incarnations: 
        //   FormulaParser.GetName()
        //   CellReference.separateRefParts() 
        //   AreaReference.separateAreaRefs() (here)
        //   SheetNameFormatter.format() (inverse)
        
        
        int len = reference.length();
        int delimiterPos = -1;
        boolean insideDelimitedName = false;
        for(int i=0; i<len; i++) {
            switch(reference.charAt(i)) {
                case CELL_DELIMITER:
                    if(!insideDelimitedName) {
                        if(delimiterPos >=0) {
                            throw new IllegalArgumentException("More than one cell delimiter '" 
                                    + CELL_DELIMITER + "' appears in area reference '" + reference + "'");
                        }
                        delimiterPos = i;
                    }
                    continue; //continue the for-loop
                case SPECIAL_NAME_DELIMITER:
                    break;
                default:
                    continue; //continue the for-loop
            }
            if(!insideDelimitedName) {
                insideDelimitedName = true;
                continue;
            }
            
            if(i >= len-1) {
                // reference ends with the delimited name. 
                // Assume names like: "Sheet1!'A1'" are never legal.
                throw new IllegalArgumentException("Area reference '" + reference 
                        + "' ends with special name delimiter '"  + SPECIAL_NAME_DELIMITER + "'");
            }
            if(reference.charAt(i+1) == SPECIAL_NAME_DELIMITER) {
                // two consecutive quotes is the escape sequence for a single one
                i++; // skip this and keep parsing the special name
            } else {
                // this is the end of the delimited name
                insideDelimitedName = false;
            }
        }
        if(delimiterPos < 0) {
            return new String[] { reference, };
        }

        String partA = reference.substring(0, delimiterPos);
        String partB = reference.substring(delimiterPos+1);
        if(partB.indexOf(SHEET_NAME_DELIMITER) >= 0) {
            // partB contains SHEET_NAME_DELIMITER
            // TODO - are references like "Sheet1!A1:Sheet1:B2" ever valid?  
            // FormulaParser has code to handle that.
            
            throw new RuntimeException("Unexpected " + SHEET_NAME_DELIMITER 
                    + " in second cell reference of '" + reference + "'");
        }
        
        int plingPos = partA.lastIndexOf(SHEET_NAME_DELIMITER);
        if(plingPos < 0) {
            return new String [] { partA, partB, };
        }
        
        String sheetName = partA.substring(0, plingPos + 1); // +1 to include delimiter
        
        return new String [] { partA, sheetName + partB, };
    }
}
