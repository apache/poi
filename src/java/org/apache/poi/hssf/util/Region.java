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

package org.apache.poi.hssf.util;


/**
 * Represents a from/to row/col square.  This is a object primitive
 * that can be used to represent row,col - row,col just as one would use String
 * to represent a string of characters.  Its really only useful for HSSF though.
 *
 * @author  Andrew C. Oliver acoliver at apache dot org
 * @deprecated (Aug-2008) use {@link CellRangeAddress}
 */
public class Region {
    private int   rowFrom;
    private short colFrom;
    private int   rowTo;
    private short colTo;

    /**
     * Creates a new instance of Region (0,0 - 0,0)
     */

    public Region()
    {
    }

    public Region(int rowFrom, short colFrom, int rowTo, short colTo)
    {
        this.rowFrom = rowFrom;
        this.rowTo   = rowTo;
        this.colFrom = colFrom;
        this.colTo   = colTo;
    }


    /**
     * get the upper left hand corner column number
     *
     * @return column number for the upper left hand corner
     */

    public short getColumnFrom()
    {
        return colFrom;
    }

    /**
     * get the upper left hand corner row number
     *
     * @return row number for the upper left hand corner
     */

    public int getRowFrom()
    {
        return rowFrom;
    }

    /**
     * get the lower right hand corner column number
     *
     * @return column number for the lower right hand corner
     */

    public short getColumnTo()
    {
        return colTo;
    }

    /**
     * get the lower right hand corner row number
     *
     * @return row number for the lower right hand corner
     */

    public int getRowTo()
    {
        return rowTo;
    }

    /**
     * set the upper left hand corner column number
     *
     * @param colFrom  column number for the upper left hand corner
     */

    public void setColumnFrom(short colFrom)
    {
        this.colFrom = colFrom;
    }

    /**
     * set the upper left hand corner row number
     *
     * @param rowFrom  row number for the upper left hand corner
     */

    public void setRowFrom(int rowFrom)
    {
        this.rowFrom = rowFrom;
    }

    /**
     * set the lower right hand corner column number
     *
     * @param colTo  column number for the lower right hand corner
     */

    public void setColumnTo(short colTo)
    {
        this.colTo = colTo;
    }

    /**
     * get the lower right hand corner row number
     *
     * @param rowTo  row number for the lower right hand corner
     */

    public void setRowTo(int rowTo)
    {
        this.rowTo = rowTo;
    }


	/**
	 * Convert a List of CellRange objects to an array of regions 
	 *  
	 * @param List of CellRange objects
	 * @return regions
	 */
	public static Region[] convertCellRangesToRegions(CellRangeAddress[] cellRanges) {
		int size = cellRanges.length;
		if(size < 1) {
			return new Region[0];
		}
		
		Region[] result = new Region[size];

		for (int i = 0; i != size; i++) {
			result[i] = convertToRegion(cellRanges[i]);
		}
		return result;
	}


		
	private static Region convertToRegion(CellRangeAddress cr) {
		
		return new Region(cr.getFirstRow(), (short)cr.getFirstColumn(), cr.getLastRow(), (short)cr.getLastColumn());
	}

	public static CellRangeAddress[] convertRegionsToCellRanges(Region[] regions) {
		int size = regions.length;
		if(size < 1) {
			return new CellRangeAddress[0];
		}
		
		CellRangeAddress[] result = new CellRangeAddress[size];

		for (int i = 0; i != size; i++) {
			result[i] = convertToCellRangeAddress(regions[i]);
		}
		return result;
	}

	public static CellRangeAddress convertToCellRangeAddress(Region r) {
		return new CellRangeAddress(r.getRowFrom(), r.getRowTo(), r.getColumnFrom(), r.getColumnTo());
	}
}
