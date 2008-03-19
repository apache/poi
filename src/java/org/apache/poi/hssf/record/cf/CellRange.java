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

package org.apache.poi.hssf.record.cf;

/**
 * CellRange.java
 * Created on January 22, 2008, 10:05 PM
 * 
 * @author Dmitriy Kumshayev
 */

public class CellRange
{
    private int               field_1_first_row;
    private int               field_2_last_row;
    private short             field_3_first_column;
    private short             field_4_last_column;
    
    public CellRange(int firstRow, int lastRow, short firstColumn, short lastColumn)
    {
		this.field_1_first_row = firstRow;
		this.field_2_last_row = lastRow;
		this.field_3_first_column = firstColumn;
		this.field_4_last_column = lastColumn;
		validateRegion();
	}
    
    private void validateRegion()
    {
    	if( field_1_first_row < 0 || 
           	field_2_last_row < -1 ||
        	field_3_first_column < 0 ||
        	field_4_last_column < -1 ||
        	field_2_last_row>=0 && field_2_last_row<field_1_first_row  || 
        	field_4_last_column>=0 && field_4_last_column<field_3_first_column 
    	)
    	{
    		throw new IllegalArgumentException("Invalid cell region "+toString());
    	}
    }
    
	public int getFirstRow()
	{
		return field_1_first_row;
	}
	private void setFirstRow(int firstRow)
	{
		this.field_1_first_row = firstRow;
	}
	public int getLastRow()
	{
		return field_2_last_row;
	}
	private void setLastRow(int lastRow)
	{
		this.field_2_last_row = lastRow;
	}
	public short getFirstColumn()
	{
		return field_3_first_column;
	}
	private void setFirstColumn(short firstColumn)
	{
		this.field_3_first_column = firstColumn;
	}
	public short getLastColumn()
	{
		return field_4_last_column;
	}
	private void setLastColumn(short lastColumn)
	{
		this.field_4_last_column = lastColumn;
	}
	
	public static final int NO_INTERSECTION = 1;
	public static final int OVERLAP = 2;
	public static final int INSIDE = 3;
	public static final int ENCLOSES = 4;
	
	/**
	 * Intersect this range with the specified range.
	 * 
	 * @param another - the specified range
	 * @return code which reflects how the specified range is related to this range.<br/>
	 * Possible return codes are:	
	 * 		NO_INTERSECTION - the specified range is outside of this range;<br/> 
	 * 		OVERLAP - both ranges partially overlap;<br/>
	 * 		INSIDE - the specified range is inside of this one<br/>
	 * 		ENCLOSES - the specified range encloses this range<br/>
	 */
	public int intersect(CellRange another )
	{
		int   firstRow = another.getFirstRow();
		int   lastRow  = another.getLastRow();
		short firstCol = another.getFirstColumn();
		short lastCol  = another.getLastColumn();
		
		if
		( 
				gt(this.getFirstRow(),lastRow) || 
				lt(this.getLastRow(),firstRow) ||
				gt(this.getFirstColumn(),lastCol) || 
				lt(this.getLastColumn(),firstCol) 
		)
		{
			return NO_INTERSECTION;
		}
		else if( this.contains(another) )
		{
			return INSIDE;
		}
		else if( another.contains(this) )
		{
			return ENCLOSES;
		}
		else
		{
			return OVERLAP;
		}
			
	}
		
	/**
	 *  Check if the specified range is located inside of this cell range.
	 *  
	 * @param range
	 * @return true if this cell range contains the argument range inside if it's area
	 */
   public boolean contains(CellRange range)
   {
		int firstRow = range.getFirstRow();
		int lastRow = range.getLastRow();
		short firstCol = range.getFirstColumn();
		short lastCol = range.getLastColumn();
		return le(this.getFirstRow(), firstRow) && ge(this.getLastRow(), lastRow)
				&& le(this.getFirstColumn(), firstCol) && ge(this.getLastColumn(), lastCol);
	}
   
  	public boolean contains(int row, short column)
	{
		return le(this.getFirstRow(), row) && ge(this.getLastRow(), row)
				&& le(this.getFirstColumn(), column) && ge(this.getLastColumn(), column);
	}
   	
   /**
    * Check if the specified cell range has a shared border with the current range.
    * 
    * @return true if the ranges have a shared border.
    */
   	public boolean hasSharedBorder(CellRange range)
   	{
		int   firstRow = range.getFirstRow();
		int   lastRow  = range.getLastRow();
		short firstCol = range.getFirstColumn();
		short lastCol  = range.getLastColumn();
		return 
			(this.getFirstRow()>0 && this.getFirstRow() - 1 == lastRow || firstRow>0 &&this.getLastRow() == firstRow -1)&& 
			(this.getFirstColumn() == firstCol) && 
			(this.getLastColumn() == lastCol) 			||
			(this.getFirstColumn()>0 && this.getFirstColumn() - 1 == lastCol || firstCol>0 && this.getLastColumn() == firstCol -1) && 
			(this.getFirstRow() == firstRow) && 
			(this.getLastRow() == lastRow)
		;
   	}
   	
    /**
     * Create an enclosing CellRange for the two cell ranges.
     * 
     * @return enclosing CellRange
     */
	public CellRange createEnclosingCellRange(CellRange range)
	{
		if( range == null)
		{
			return cloneCellRange();
		}
		else
		{
			CellRange cellRange = 
				new CellRange(
					lt(range.getFirstRow(),getFirstRow())?range.getFirstRow():getFirstRow(),
					gt(range.getLastRow(),getLastRow())?range.getLastRow():getLastRow(),
					lt(range.getFirstColumn(),getFirstColumn())?range.getFirstColumn():getFirstColumn(),
					gt(range.getLastColumn(),getLastColumn())?range.getLastColumn():getLastColumn()
				);
			return cellRange;
		}
	}
	
	public CellRange cloneCellRange()
	{
		return new CellRange(getFirstRow(),getLastRow(),getFirstColumn(),getLastColumn());
	}
    	
	/**
	 * Copy data from antother cell range to this cell range
	 * @param cr - another cell range
	 */
	public void setCellRange(CellRange cr)
	{
		setFirstRow(cr.getFirstRow());
		setLastRow(cr.getLastRow());
		setFirstColumn(cr.getFirstColumn());
		setLastColumn(cr.getLastColumn());
	}

	/**
	 * @return true if a < b
	 */
	private static boolean lt(int a, int b)
	{
		return a == -1 ? false : (b == -1 ? true : a < b);
	}
	
	/**
	 * @return true if a <= b
	 */
	private static boolean le(int a, int b)
	{
		return a == b || lt(a,b);
	}
	
	/**
	 * @return true if a > b
	 */
	private static boolean gt(int a, int b)
	{
		return lt(b,a);
	}

	/**
	 * @return true if a >= b
	 */
	private static boolean ge(int a, int b)
	{
		return !lt(a,b);
	}
	
	public String toString()
	{
		return "("+this.getFirstRow()+","+this.getLastRow()+","+this.getFirstColumn()+","+this.getLastColumn()+")";
	}
    
}
