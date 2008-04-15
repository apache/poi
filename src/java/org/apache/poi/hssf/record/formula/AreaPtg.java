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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;

import org.apache.poi.hssf.util.AreaReference;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.hssf.record.RecordInputStream;

/**
 * Specifies a rectangular area of cells A1:A4 for instance.
 * @author  andy
 * @author Jason Height (jheight at chariot dot net dot au)
 */

public class AreaPtg
    extends Ptg implements AreaI
{
    public final static short sid  = 0x25;
    private final static int  SIZE = 9;
    /** zero based, unsigned 16 bit */
    private int             field_1_first_row;
    /** zero based, unsigned 16 bit */
    private int             field_2_last_row;
    /** zero based, unsigned 8 bit */
    private int             field_3_first_column;
    /** zero based, unsigned 8 bit */
    private int             field_4_last_column;
    
    private final static BitField   rowRelative = BitFieldFactory.getInstance(0x8000);
    private final static BitField   colRelative = BitFieldFactory.getInstance(0x4000);
    private final static BitField   columnMask      = BitFieldFactory.getInstance(0x3FFF);

    protected AreaPtg() {
      //Required for clone methods
    }
   
    public AreaPtg(String arearef) {
        AreaReference ar = new AreaReference(arearef);
        CellReference firstCell = ar.getFirstCell();
        CellReference lastCell = ar.getLastCell();
        setFirstRow(firstCell.getRow());
        setFirstColumn(firstCell.getCol());
        setLastRow(lastCell.getRow());
        setLastColumn(lastCell.getCol());
        setFirstColRelative(!firstCell.isColAbsolute());
        setLastColRelative(!lastCell.isColAbsolute());
        setFirstRowRelative(!firstCell.isRowAbsolute());
        setLastRowRelative(!lastCell.isRowAbsolute());        
    }
    
    public AreaPtg(int firstRow, int lastRow, int firstColumn, int lastColumn,
            boolean firstRowRelative, boolean lastRowRelative, boolean firstColRelative, boolean lastColRelative) {
        
        checkColumnBounds(firstColumn);
        checkColumnBounds(lastColumn);
        checkRowBounds(firstRow);
        checkRowBounds(lastRow);
      setFirstRow(firstRow);
      setLastRow(lastRow);
      setFirstColumn(firstColumn);
      setLastColumn(lastColumn);
      setFirstRowRelative(firstRowRelative);
      setLastRowRelative(lastRowRelative);
      setFirstColRelative(firstColRelative);
      setLastColRelative(lastColRelative);
    }    

    private static void checkColumnBounds(int colIx) {
        if((colIx & 0x0FF) != colIx) {
            throw new IllegalArgumentException("colIx (" + colIx + ") is out of range");
        }
    }
    private static void checkRowBounds(int rowIx) {
        if((rowIx & 0x0FFFF) != rowIx) {
            throw new IllegalArgumentException("rowIx (" + rowIx + ") is out of range");
        }
    }

    public AreaPtg(RecordInputStream in)
    {
        field_1_first_row    = in.readUShort();
        field_2_last_row     = in.readUShort();
        field_3_first_column = in.readUShort();
        field_4_last_column  = in.readUShort();
        //System.out.println(toString());
    }
    
    public String getAreaPtgName() {
      return "AreaPtg";
    }    

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append(getAreaPtgName());
        buffer.append("\n");
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
        array[offset] = (byte) (sid + ptgClass);
        LittleEndian.putShort(array,offset+1,(short)field_1_first_row);
        LittleEndian.putShort(array,offset+3,(short)field_2_last_row);
        LittleEndian.putShort(array,offset+5,(short)field_3_first_column);
        LittleEndian.putShort(array,offset+7,(short)field_4_last_column);        
    }

    public int getSize()
    {
        return SIZE;
    }

    /**
     * @return the first row in the area
     */
    public int getFirstRow()
    {
        return field_1_first_row;
    }

    /**
     * sets the first row
     * @param rowIx number (0-based)
     */
    public void setFirstRow(int rowIx) {
        checkRowBounds(rowIx);
        field_1_first_row = rowIx;
    }

    /**
     * @return last row in the range (x2 in x1,y1-x2,y2)
     */
    public int getLastRow()
    {
        return field_2_last_row;
    }

    /**
     * @param rowIx last row number in the area 
     */
    public void setLastRow(int rowIx) {
        checkRowBounds(rowIx);
        field_2_last_row = rowIx;
    }

    /**
     * @return the first column number in the area.
     */
    public int getFirstColumn()
    {
        return columnMask.getValue(field_3_first_column);
    }

    /**
     * @return the first column number + the options bit settings unstripped
     */
    public short getFirstColumnRaw()
    {
        return (short) field_3_first_column; // TODO
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
     * @param rel is relative or not.
     */
    public void setFirstRowRelative(boolean rel) {
        field_3_first_column=rowRelative.setBoolean(field_3_first_column,rel);
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
        field_3_first_column=colRelative.setBoolean(field_3_first_column,rel);
    }

    /**
     * set the first column in the area
     */
    public void setFirstColumn(int colIx) {
        checkColumnBounds(colIx);
    	field_3_first_column=columnMask.setValue(field_3_first_column, colIx);
    }

    /**
     * set the first column irespective of the bitmasks
     */
    public void setFirstColumnRaw(int column)
    {
        field_3_first_column = column;
    }

    /**
     * @return lastcolumn in the area
     */
    public int getLastColumn()
    {
        return columnMask.getValue(field_4_last_column);
    }

    /**
     * @return last column and bitmask (the raw field)
     */
    public short getLastColumnRaw()
    {
        return (short) field_4_last_column;
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
     * @param rel <code>true</code> if the last row relative, else
     * <code>false</code>
     */
    public void setLastRowRelative(boolean rel) {
        field_4_last_column=rowRelative.setBoolean(field_4_last_column,rel);
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
        field_4_last_column=colRelative.setBoolean(field_4_last_column,rel);
    }
    

    /**
     * set the last column in the area
     */
    public void setLastColumn(int colIx) {
        checkColumnBounds(colIx);
    	field_4_last_column=columnMask.setValue(field_4_last_column, colIx);
    }

    /**
     * set the last column irrespective of the bitmasks
     */
    public void setLastColumnRaw(short column)
    {
        field_4_last_column = column;
    }
    
    public String toFormulaString(Workbook book)
    {
    	return toFormulaString(this, book);
    }
    protected static String toFormulaString(AreaI area, Workbook book) {
    	CellReference topLeft = new CellReference(area.getFirstRow(),area.getFirstColumn(),!area.isFirstRowRelative(),!area.isFirstColRelative());
    	CellReference botRight = new CellReference(area.getLastRow(),area.getLastColumn(),!area.isLastRowRelative(),!area.isLastColRelative());
    	
    	if(AreaReference.isWholeColumnReference(topLeft, botRight)) {
    		return (new AreaReference(topLeft, botRight)).formatAsString();
    	} else {
    		return topLeft.formatAsString() + ":" + botRight.formatAsString(); 
    	}
    }

    public byte getDefaultOperandClass() {
        return Ptg.CLASS_REF;
    }
    
    public Object clone() {
      AreaPtg ptg = new AreaPtg();
      ptg.field_1_first_row = field_1_first_row;
      ptg.field_2_last_row = field_2_last_row;
      ptg.field_3_first_column = field_3_first_column;
      ptg.field_4_last_column = field_4_last_column;
      ptg.setClass(ptgClass);
      return ptg;
    }

}
