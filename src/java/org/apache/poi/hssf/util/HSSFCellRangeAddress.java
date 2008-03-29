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

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

import java.util.ArrayList;

/**
 * <p>Title: HSSFCellRangeAddress</p>
 * <p>Description:
 *          Implementation of the cell range address lists,like is described in
 *          OpenOffice.org's Excel Documentation .
 *          In BIFF8 there is a common way to store absolute cell range address
 *          lists in several records (not formulas). A cell range address list
 *          consists of a field with the number of ranges and the list of the range
 *          addresses. Each cell range address (called an ADDR structure) contains
 *          4 16-bit-values.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author Dragos Buleandra (dragos.buleandra@trade2b.ro)
 * @version 2.0-pre
 */

public class HSSFCellRangeAddress
{
	private static POILogger logger = POILogFactory.getLogger(HSSFCellRangeAddress.class);
	
    /**
     * Number of following ADDR structures
     */
    private short             field_addr_number;

    /**
     * List of ADDR structures. Each structure represents a cell range
     */
    private ArrayList         field_regions_list;

    public HSSFCellRangeAddress()
    {

    }

    /**
     * Construct a new HSSFCellRangeAddress object and sets its fields appropriately .
     * Even this isn't an Excel record , I kept the same behavior for reading/writing
     * the object's data as for a regular record .
     * 
     * @param in the RecordInputstream to read the record from
     */
    public HSSFCellRangeAddress(RecordInputStream in)
    {
        this.fillFields(in);
    }

    public void fillFields(RecordInputStream in)
    {
        this.field_addr_number = in.readShort(); 
		this.field_regions_list = new ArrayList(this.field_addr_number);

		for (int k = 0; k < this.field_addr_number; k++)
		{
            short first_row = in.readShort(); 
            short first_col = in.readShort();
            
            short last_row  = first_row;
            short last_col  = first_col;
            if(in.remaining() >= 4) {
	            last_row  = in.readShort();
	            last_col  = in.readShort();
            } else {
            	// Ran out of data
            	// For now, issue a warning, finish, and 
            	//  hope for the best....
            	logger.log(POILogger.WARN, "Ran out of data reading cell references for DVRecord");
            	k = this.field_addr_number;
            }

			AddrStructure region = new AddrStructure(first_row, first_col, last_row, last_col);
			this.field_regions_list.add(region);
		}
    }

    /**
     * Get the number of following ADDR structures.
     * The number of this structures is automatically set when reading an Excel file
     * and/or increased when you manually add a new ADDR structure .
     * This is the reason there isn't a set method for this field .
     * @return number of ADDR structures
     */
    public short getADDRStructureNumber()
    {
        return this.field_addr_number;
    }

    /**
     * Add an ADDR structure .
     * @param first_row - the upper left hand corner's row
     * @param first_col - the upper left hand corner's col
     * @param last_row  - the lower right hand corner's row
     * @param last_col  - the lower right hand corner's col
     * @return the index of this ADDR structure
     */
    public int addADDRStructure(short first_row, short first_col, short last_row, short last_col)
    {
        if (this.field_regions_list == null)
        {
            //just to be sure :-)
            this.field_addr_number= 0;
            this.field_regions_list = new ArrayList(10);
        }
        AddrStructure region = new AddrStructure(first_row, last_row, first_col, last_col);

        this.field_regions_list.add(region);
        this.field_addr_number++;
        return this.field_addr_number;
    }

    /**
     * Remove the ADDR structure stored at the passed in index
     * @param index The ADDR structure's index
     */
    public void removeADDRStructureAt(int index)
    {
        this.field_regions_list.remove(index);
        this.field_addr_number--;
    }

    /**
     * return the ADDR structure at the given index.
     * @return AddrStructure representing
     */
    public AddrStructure getADDRStructureAt(int index)
    {
        return ( AddrStructure ) this.field_regions_list.get(index);
    }

    public int serialize(int offset, byte [] data)
    {
        int pos  = 2;

        LittleEndian.putShort(data, offset, this.getADDRStructureNumber());
        for (int k = 0; k < this.getADDRStructureNumber(); k++)
        {
            AddrStructure region = this.getADDRStructureAt(k);
            LittleEndian.putShort(data, offset + pos, region.getFirstRow());
            pos += 2;
            LittleEndian.putShort(data, offset + pos, region.getLastRow());
            pos += 2;
            LittleEndian.putShort(data, offset + pos, region.getFirstColumn());
            pos += 2;
            LittleEndian.putShort(data, offset + pos, region.getLastColumn());
            pos += 2;
        }
        return this.getSize();
    }

    public int getSize()
    {
       return 2 + this.field_addr_number*8;
    }

    public class AddrStructure
    {
        private short _first_row;
        private short _first_col;
        private short _last_row;
        private short _last_col;

        public AddrStructure(short first_row, short last_row, short first_col, short last_col)
        {
            this._first_row = first_row;
            this._last_row   = last_row;
            this._first_col = first_col;
            this._last_col   = last_col;
        }

		/**
		 * get the upper left hand corner column number
		 * @return column number for the upper left hand corner
		 */
		public short getFirstColumn()
		{
			return this._first_col;
		}

		/**
		 * get the upper left hand corner row number
		 * @return row number for the upper left hand corner
		 */
		public short getFirstRow()
		{
			return this._first_row;
		}

		/**
		 * get the lower right hand corner column number
		 * @return column number for the lower right hand corner
		 */
		public short getLastColumn()
		{
			return this._last_col;
		}

		/**
		 * get the lower right hand corner row number
		 * @return row number for the lower right hand corner
		 */
		public short getLastRow()
		{
			return this._last_row;
		}

		/**
		 * set the upper left hand corner column number
		 * @param this._first_col  column number for the upper left hand corner
		 */
		public void setFirstColumn(short first_col)
		{
			this._first_col = first_col;
		}

		/**
		 * set the upper left hand corner row number
		 * @param rowFrom  row number for the upper left hand corner
		 */
		public void setFirstRow(short first_row)
		{
			this._first_row = first_row;
		}

		/**
		 * set the lower right hand corner column number
		 * @param colTo  column number for the lower right hand corner
		 */
		public void setLastColumn(short last_col)
		{
			this._last_col = last_col;
		}

		/**
		 * get the lower right hand corner row number
		 * @param rowTo  row number for the lower right hand corner
		 */
		public void setLastRow(short last_row)
		{
			this._last_row = last_row;
		}
	}
}


