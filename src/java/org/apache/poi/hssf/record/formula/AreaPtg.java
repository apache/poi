
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

/*
 * AreaPtg.java
 *
 * Created on November 17, 2001, 9:30 PM
 */
package org.apache.poi.hssf.record.formula;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.BitField;

import org.apache.poi.hssf.util.AreaReference;
import org.apache.poi.hssf.util.CellReference;

/**
 * Specifies a rectangular area of cells A1:A4 for instance.
 * @author  andy
 */

public class AreaPtg
    extends Ptg
{
    public final static short sid  = 0x25;
    private final static int  SIZE = 9;
    private short             field_1_first_row;
    private short             field_2_last_row;
    private short             field_3_first_column;
    private short             field_4_last_column;
    
    private BitField         rowRelative = new BitField(0x8000);
    private BitField         colRelative = new BitField(0x4000);
    private BitField         column      = new BitField(0x3FFF);

    
   
    protected AreaPtg(String arearef) {
        //int[] xyxy = ReferenceUtil.getXYXYFromAreaRef(arearef);
        AreaReference ar = new AreaReference(arearef);
        
        setFirstRow((short)ar.getCells()[0].getRow());
        setFirstColumn((short)ar.getCells()[0].getCol());
        setLastRow((short)ar.getCells()[1].getRow());
        setLastColumn((short)ar.getCells()[1].getCol());
        setFirstColRelative(!ar.getCells()[0].isColAbsolute());
        setLastColRelative(!ar.getCells()[1].isColAbsolute());
        setFirstRowRelative(!ar.getCells()[0].isRowAbsolute());
        setLastRowRelative(!ar.getCells()[1].isRowAbsolute());
        
    }

    public AreaPtg(byte [] data, int offset)
    {
        offset++;
        field_1_first_row    = LittleEndian.getShort(data, 0 + offset);
        field_2_last_row     = LittleEndian.getShort(data, 2 + offset);
        field_3_first_column = LittleEndian.getShort(data, 4 + offset);
        field_4_last_column  = LittleEndian.getShort(data, 6 + offset);
        System.out.println(toString());
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("AreaPtg\n");
        buffer.append("firstRow = " + getFirstRow()).append("\n");
        buffer.append("lastRow  = " + getLastRow()).append("\n");
        buffer.append("firstCol = " + getFirstColumn()).append("\n");
        buffer.append("lastCol  = " + getLastColumn()).append("\n");
        buffer.append("firstColRowRel= "
                      + isFirstRowRelative()).append("\n");
        buffer.append("lastColRowRel = "
                      + isLastRowRelative()).append("\n");
        buffer.append("firstColRel   = " + isFirstColRelative()).append("\n");
        buffer.append("lastColRel    = " + isLastColRelative()).append("\n");
        return buffer.toString();
    }

    public void writeBytes(byte [] array, int offset) {
        array[offset] = sid;
        LittleEndian.putShort(array,offset+1,field_1_first_row);
        LittleEndian.putShort(array,offset+3,field_2_last_row);
        LittleEndian.putShort(array,offset+5,field_3_first_column);
        LittleEndian.putShort(array,offset+7,field_4_last_column);        
    }

    public int getSize()
    {
        return SIZE;
    }

    /**
     * @return the first row in the area
     */
    public short getFirstRow()
    {
        return field_1_first_row;
    }

    /**
     * sets the first row
     * @param row number (0-based)
     */
    public void setFirstRow(short row)
    {
        field_1_first_row = row;
    }

    /**
     * @return last row in the range (x2 in x1,y1-x2,y2)
     */
    public short getLastRow()
    {
        return field_2_last_row;
    }

    /**
     * @param last row number in the area 
     */
    public void setLastRow(short row)
    {
        field_2_last_row = row;
    }

    /**
     * @return the first column number in the area.
     */
    public short getFirstColumn()
    {
        return column.getShortValue(field_3_first_column);
    }

    /**
     * @return the first column number + the options bit settings unstripped
     */
    public short getFirstColumnRaw()
    {
        return field_3_first_column;
    }

    /**
     * @return whether or not the first row is a relative reference or not.
     */
    public boolean isFirstRowRelative()
    {
        return rowRelative.isSet(field_3_first_column);
    }
    
    /**
     * sets the first row to relative or not
     * @param isRelative or not.
     */
    public void setFirstRowRelative(boolean rel) {
        field_3_first_column=rowRelative.setShortBoolean(field_3_first_column,rel);
    }

    /**
     * @return isrelative first column to relative or not
     */
    public boolean isFirstColRelative()
    {
        return colRelative.isSet(field_3_first_column);
    }
    
    /**
     * set whether the first column is relative 
     */
    public void setFirstColRelative(boolean rel) {
        field_3_first_column=colRelative.setShortBoolean(field_3_first_column,rel);
    }

    /**
     * set the first column in the area
     */
    public void setFirstColumn(short column)
    {
        field_3_first_column = column;   // fixme
    }

    /**
     * set the first column irespective of the bitmasks
     */
    public void setFirstColumnRaw(short column)
    {
        field_3_first_column = column;
    }

    /**
     * @return lastcolumn in the area
     */
    public short getLastColumn()
    {
        return column.getShortValue(field_4_last_column);
    }

    /**
     * @return last column and bitmask (the raw field)
     */
    public short getLastColumnRaw()
    {
        return field_4_last_column;
    }

    /**
     * @return last row relative or not
     */
    public boolean isLastRowRelative()
    {
        return rowRelative.isSet(field_4_last_column);
    }
    
    /**
     * set whether the last row is relative or not
     * @param last row relative
     */
    public void setLastRowRelative(boolean rel) {
        field_4_last_column=rowRelative.setShortBoolean(field_4_last_column,rel);
    }

    /**
     * @return lastcol relative or not
     */
    public boolean isLastColRelative()
    {
        return colRelative.isSet(field_4_last_column);
    }
    
    /**
     * set whether the last column should be relative or not
     */
    public void setLastColRelative(boolean rel) {
        field_4_last_column=colRelative.setShortBoolean(field_4_last_column,rel);
    }
    

    /**
     * set the last column in the area
     */
    public void setLastColumn(short column)
    {
        field_4_last_column = column;   // fixme
    }

    /**
     * set the last column irrespective of the bitmasks
     */
    public void setLastColumnRaw(short column)
    {
        field_4_last_column = column;
    }

    public String toFormulaString()
    {
         return (new CellReference(getFirstRow(),getFirstColumn(),!isFirstRowRelative(),!isFirstColRelative())).toString() + ":" +
                (new CellReference(getLastRow(),getLastColumn(),!isLastRowRelative(),!isLastColRelative())).toString();
    }

}
