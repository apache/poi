/* ====================================================================
   Copyright 2003-2004   Apache Software Foundation

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

package org.apache.poi.hssf.record.formula;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hssf.util.RangeAddress;
import org.apache.poi.hssf.util.AreaReference;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.util.SheetReferences;

import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.util.BitField;

/**
 * Title:        Area 3D Ptg - 3D referecnce (Sheet + Area)<P>
 * Description:  Defined a area in Extern Sheet. <P>
 * REFERENCE:  <P>
 * @author Libin Roman (Vista Portal LDT. Developer)
 * @author avik
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 1.0-pre
 */

public class Area3DPtg extends Ptg
{
	public final static byte sid = 0x3b;
	private final static int SIZE = 11; // 10 + 1 for Ptg
	private short field_1_index_extern_sheet;
	private short field_2_first_row;
	private short field_3_last_row;
	private short field_4_first_column;
	private short field_5_last_column;

	private BitField rowRelative = new BitField( 0x8000 );
	private BitField colRelative = new BitField( 0x4000 );

	/** Creates new AreaPtg */
	public Area3DPtg()
	{
	}

	public Area3DPtg( String arearef, short externIdx )
	{
		setArea(arearef);
		setExternSheetIndex( externIdx );

	}

	public Area3DPtg( byte[] data, int offset )
	{
		offset++;
		field_1_index_extern_sheet = LittleEndian.getShort( data, 0 + offset );
		field_2_first_row = LittleEndian.getShort( data, 2 + offset );
		field_3_last_row = LittleEndian.getShort( data, 4 + offset );
		field_4_first_column = LittleEndian.getShort( data, 6 + offset );
		field_5_last_column = LittleEndian.getShort( data, 8 + offset );
	}

	public String toString()
	{
		StringBuffer buffer = new StringBuffer();

		buffer.append( "AreaPtg\n" );
		buffer.append( "Index to Extern Sheet = " + getExternSheetIndex() ).append( "\n" );
		buffer.append( "firstRow = " + getFirstRow() ).append( "\n" );
		buffer.append( "lastRow  = " + getLastRow() ).append( "\n" );
		buffer.append( "firstCol = " + getFirstColumn() ).append( "\n" );
		buffer.append( "lastCol  = " + getLastColumn() ).append( "\n" );
		buffer.append( "firstColRel= "
				+ isFirstRowRelative() ).append( "\n" );
		buffer.append( "lastColRowRel = "
				+ isLastRowRelative() ).append( "\n" );
		buffer.append( "firstColRel   = " + isFirstColRelative() ).append( "\n" );
		buffer.append( "lastColRel    = " + isLastColRelative() ).append( "\n" );
		return buffer.toString();
	}

	public void writeBytes( byte[] array, int offset )
	{
		array[0 + offset] = (byte) ( sid + ptgClass );
		LittleEndian.putShort( array, 1 + offset, getExternSheetIndex() );
		LittleEndian.putShort( array, 3 + offset, getFirstRow() );
		LittleEndian.putShort( array, 5 + offset, getLastRow() );
		LittleEndian.putShort( array, 7 + offset, getFirstColumnRaw() );
		LittleEndian.putShort( array, 9 + offset, getLastColumnRaw() );
	}

	public int getSize()
	{
		return SIZE;
	}

	public short getExternSheetIndex()
	{
		return field_1_index_extern_sheet;
	}

	public void setExternSheetIndex( short index )
	{
		field_1_index_extern_sheet = index;
	}

	public short getFirstRow()
	{
		return field_2_first_row;
	}

	public void setFirstRow( short row )
	{
		field_2_first_row = row;
	}

	public short getLastRow()
	{
		return field_3_last_row;
	}

	public void setLastRow( short row )
	{
		field_3_last_row = row;
	}

	public short getFirstColumn()
	{
		return (short) ( field_4_first_column & 0xFF );
	}

	public short getFirstColumnRaw()
	{
		return field_4_first_column;
	}

	public boolean isFirstRowRelative()
	{
		return rowRelative.isSet( field_4_first_column );
	}

	public boolean isFirstColRelative()
	{
		return colRelative.isSet( field_4_first_column );
	}

	public void setFirstColumn( short column )
	{
		field_4_first_column &= 0xFF00;
		field_4_first_column |= column & 0xFF;
	}

	public void setFirstColumnRaw( short column )
	{
		field_4_first_column = column;
	}

	public short getLastColumn()
	{
		return (short) ( field_5_last_column & 0xFF );
	}

	public short getLastColumnRaw()
	{
		return field_5_last_column;
	}

	public boolean isLastRowRelative()
	{
		return rowRelative.isSet( field_5_last_column );
	}

	public boolean isLastColRelative()
	{
		return colRelative.isSet( field_5_last_column );
	}

	public void setLastColumn( short column )
	{
		field_5_last_column &= 0xFF00;
		field_5_last_column |= column & 0xFF;
	}

	public void setLastColumnRaw( short column )
	{
		field_5_last_column = column;
	}

	/**
	 * sets the first row to relative or not
	 * @param isRelative or not.
	 */
	public void setFirstRowRelative( boolean rel )
	{
		field_4_first_column = rowRelative.setShortBoolean( field_4_first_column, rel );
	}

	/**
	 * set whether the first column is relative
	 */
	public void setFirstColRelative( boolean rel )
	{
		field_4_first_column = colRelative.setShortBoolean( field_4_first_column, rel );
	}

	/**
	 * set whether the last row is relative or not
	 * @param last row relative
	 */
	public void setLastRowRelative( boolean rel )
	{
		field_5_last_column = rowRelative.setShortBoolean( field_5_last_column, rel );
	}

	/**
	 * set whether the last column should be relative or not
	 */
	public void setLastColRelative( boolean rel )
	{
		field_5_last_column = colRelative.setShortBoolean( field_5_last_column, rel );
	}


	/*public String getArea(){
		RangeAddress ra = new RangeAddress( getFirstColumn(),getFirstRow() + 1, getLastColumn(), getLastRow() + 1);
		String result = ra.getAddress();

		return result;
	}*/

	public void setArea( String ref )
	{
		AreaReference ar = new AreaReference( ref );

		setFirstRow( (short) ar.getCells()[0].getRow() );
		setFirstColumn( (short) ar.getCells()[0].getCol() );
		setLastRow( (short) ar.getCells()[1].getRow() );
		setLastColumn( (short) ar.getCells()[1].getCol() );
		setFirstColRelative( !ar.getCells()[0].isColAbsolute() );
		setLastColRelative( !ar.getCells()[1].isColAbsolute() );
		setFirstRowRelative( !ar.getCells()[0].isRowAbsolute() );
		setLastRowRelative( !ar.getCells()[1].isRowAbsolute() );

	}

	public String toFormulaString(Workbook book)
	{
		SheetReferences refs = book == null ? null : book.getSheetReferences();
		StringBuffer retval = new StringBuffer();
		if ( refs != null )
		{
			retval.append( refs.getSheetName( this.field_1_index_extern_sheet ) );
			retval.append( '!' );
		}
		retval.append( ( new CellReference( getFirstRow(), getFirstColumn(), !isFirstRowRelative(), !isFirstColRelative() ) ).toString() );
		retval.append( ':' );
		retval.append( ( new CellReference( getLastRow(), getLastColumn(), !isLastRowRelative(), !isLastColRelative() ) ).toString() );
		return retval.toString();
	}

	public byte getDefaultOperandClass()
	{
		return Ptg.CLASS_REF;
	}

	public Object clone()
	{
		Area3DPtg ptg = new Area3DPtg();
		ptg.field_1_index_extern_sheet = field_1_index_extern_sheet;
		ptg.field_2_first_row = field_2_first_row;
		ptg.field_3_last_row = field_3_last_row;
		ptg.field_4_first_column = field_4_first_column;
		ptg.field_5_last_column = field_5_last_column;
            ptg.setClass(ptgClass);
		return ptg;
	}


	public boolean equals( Object o )
	{
		if ( this == o ) return true;
		if ( !( o instanceof Area3DPtg ) ) return false;

		final Area3DPtg area3DPtg = (Area3DPtg) o;

		if ( field_1_index_extern_sheet != area3DPtg.field_1_index_extern_sheet ) return false;
		if ( field_2_first_row != area3DPtg.field_2_first_row ) return false;
		if ( field_3_last_row != area3DPtg.field_3_last_row ) return false;
		if ( field_4_first_column != area3DPtg.field_4_first_column ) return false;
		if ( field_5_last_column != area3DPtg.field_5_last_column ) return false;

		return true;
	}

	public int hashCode()
	{
		int result;
		result = (int) field_1_index_extern_sheet;
		result = 29 * result + (int) field_2_first_row;
		result = 29 * result + (int) field_3_last_row;
		result = 29 * result + (int) field_4_first_column;
		result = 29 * result + (int) field_5_last_column;
		return result;
	}


}
