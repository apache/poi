/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */


package org.apache.poi.hssf.util;

/**
 *
 * @author  Avik Sengupta
 * @author  Dennis Doubleday (patch to seperateRowColumns())
 */
public class CellReference {

    /** Creates new CellReference */
    private int row;
    private int col;
    private String sheetName;
    private boolean rowAbs;
    private boolean colAbs;

    public CellReference(String cellRef) {
        String[] parts = separateRefParts(cellRef);
        sheetName = parts[0];
        String ref = parts[1];
        if (ref.charAt(0) == '$') {
            colAbs=true;
            ref=ref.substring(1);
        }
        col = convertColStringToNum(ref);
        ref=parts[2];
        if (ref.charAt(0) == '$') {
            rowAbs=true;
            ref=ref.substring(1);
        }
        row = Integer.parseInt(ref)-1;
    }

    public CellReference(int pRow, int pCol) {
        this(pRow,pCol,false,false);
    }

    public CellReference(int pRow, int pCol, boolean pAbsRow, boolean pAbsCol) {
        row=pRow;col=pCol;
        rowAbs = pAbsRow;
        colAbs=pAbsCol;

    }

    public int getRow(){return row;}
    public short getCol(){return (short) col;}
    public boolean isRowAbsolute(){return rowAbs;}
    public boolean isColAbsolute(){return colAbs;}
    public String getSheetName(){return sheetName;}

    /**
     * takes in a column reference portion of a CellRef and converts it from
     * ALPHA-26 number format to 0-based base 10.
     */
    private int convertColStringToNum(String ref) {
        int len = ref.length();
        int retval=0;
        int pos = 0;

        for (int k = ref.length()-1; k > -1; k--) {
            char thechar = ref.charAt(k);
            if ( pos == 0) {
                retval += (Character.getNumericValue(thechar)-9);
            } else {
                retval += (Character.getNumericValue(thechar)-9) * (pos * 26);
            }
            pos++;
        }
        return retval-1;
    }


    /**
     * Seperates the row from the columns and returns an array.  Element in
     * position one is the substring containing the columns still in ALPHA-26
     * number format.
     */
    private String[] separateRefParts(String reference) {

        // Look for end of sheet name. This will either set
        // start to 0 (if no sheet name present) or the
        // index after the sheet reference ends.
        String retval[] = new String[3];

        int start = reference.indexOf("!");
        if (start != -1) retval[0] = reference.substring(0, start);
        start += 1;

        int length = reference.length();


        char[] chars = reference.toCharArray();
        int loc = start;
        if (chars[loc]=='$') loc++;
        for (; loc < chars.length; loc++) {
            if (Character.isDigit(chars[loc]) || chars[loc] == '$') {
                break;
            }
        }

        retval[1] = reference.substring(start,loc);
        retval[2] = reference.substring(loc);
        return retval;
    }

    /**
     * takes in a 0-based base-10 column and returns a ALPHA-26 representation
     */
    private static String convertNumToColString(int col) {
        String retval = null;
        int mod = col % 26;
        int div = col / 26;
        char small=(char)(mod + 65);
        char big = (char)(div + 64);

        if (div == 0) {
            retval = ""+small;
        } else {
            retval = ""+big+""+small;
        }

        return retval;
    }


    public String toString() {
        StringBuffer retval = new StringBuffer();
        retval.append( (colAbs)?"$":"");
        retval.append( convertNumToColString(col));
        retval.append((rowAbs)?"$":"");
        retval.append(row+1);

    return retval.toString();
    }
}
