/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

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
    private boolean rowAbs;
    private boolean colAbs;
    
    public CellReference(String cellRef) {
        String[] parts = seperateRowColumns(cellRef);
        String ref = parts[0];
        if (ref.charAt(0) == '$') {
            colAbs=true; 
            ref=ref.substring(1);
        }
        col = convertColStringToNum(ref);
        ref=parts[1];
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
    public int getCol(){return col;}
    public boolean isRowAbsolute(){return rowAbs;}
    public boolean isColAbsolute(){return colAbs;}
    
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
    private String[] seperateRowColumns(String reference) {
        
        // Look for end of sheet name. This will either set
        // start to 0 (if no sheet name present) or the
        // index after the sheet reference ends.
        int start = reference.indexOf("!") + 1;

        String retval[] = new String[2];
        int length = reference.length();


        char[] chars = reference.toCharArray();
        int loc = start;
        if (chars[loc]=='$') loc++;
        for (; loc < chars.length; loc++) {
            if (Character.isDigit(chars[loc]) || chars[loc] == '$') {
                break;
            }
        }
        
        
        retval[0] = reference.substring(start,loc);
        retval[1] = reference.substring(loc);
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
