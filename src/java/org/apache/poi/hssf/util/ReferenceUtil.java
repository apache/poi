/*
 * ReferenceUtil.java
 *
 * Created on April 28, 2002, 1:09 PM
 */

package org.apache.poi.hssf.util;

/**
 * Handles conversion between A1= 0,0 (cell ref to numeric conversion)
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 */
public class ReferenceUtil {

    /** You don't neeed to construct this */
    private ReferenceUtil() {
    }
    
    /**
     * takes in a cell reference string A1 for instance and returns an integer
     * array with the first element being the row number and the second being 
     * the column number, all in 0-based base 10 format.
     * @param reference a cell reference such as A1 or AA1 or IV1
     * @return xyarray int array containing row and column number
     */
    public static int[] getXYFromReference(String reference) {
           int[] retval = new int[2];
           String[] parts = seperateRowColumns(reference);           
           retval[1] = convertColStringToNum(parts[0]);
           retval[0] = Integer.parseInt(parts[1])-1;
           return retval;
    }
    
    /**
     * takes in a row and column and returns a string cellref
     * @param row the 0 based row such as 0 
     * @param col the 0 based col number such as 1 
     * @return cellreference string such as B1
     */
    public static String getReferenceFromXY(int row, int col) {
        String retval = convertNumToColString(col) + ""+(row+1);
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
    
    /**
     * takes in a column reference portion of a CellRef and converts it from 
     * ALPHA-26 number format to 0-based base 10.
     */
    private static int convertColStringToNum(String ref) {
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
    private static String[] seperateRowColumns(String reference) {
        int loc = 0; // location of first number
        String retval[] = new String[2];
        int length = reference.length();
        
        char[] chars = reference.toCharArray();
        
        for (loc = 0; loc < chars.length; loc++) {
            if (Character.isDigit(chars[loc])) {
                break;
            }
        }
        
        retval[0] = reference.substring(0,loc);
        retval[1] = reference.substring(loc);        
        return retval;
    }
        
}
