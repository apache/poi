
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

import org.apache.poi.hssf.record.MergeCellsRecord.MergedRegion;

/**
 * Represents a from/to row/col square.  This is a object primitive
 * that can be used to represent row,col - row,col just as one would use String
 * to represent a string of characters.  Its really only useful for HSSF though.
 *
 * @author  Andrew C. Oliver acoliver at apache dot org
 */

public class Region
    implements Comparable
{
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
     * special constructor (I know this is bad but it is so wrong that its right
     * okay) that makes a region from a mergedcells's region subrecord.
     */

    public Region(MergedRegion region)
    {
        this(region.row_from, region.col_from, region.row_to, region.col_to);
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
     * Answers: "is the row/column inside this range?"
     *
     * @returns boolean - true if the cell is in the range and false if it is not
     */

    public boolean contains(int row, short col)
    {
        if ((this.rowFrom <= row) && (this.rowTo >= row)
                && (this.colFrom <= col) && (this.colTo >= col))
        {

//                System.out.println("Region ("+rowFrom+","+colFrom+","+rowTo+","+ 
//                                   colTo+") does contain "+row+","+col);
            return true;
        }
        return false;
    }

    public boolean equals(Region r)
    {
        return (compareTo(r) == 0);
    }

    /**
     * Compares that the given region is the same less than or greater than this
     * region.  If any regional coordiant passed in is less than this regions
     * coordinants then a positive integer is returned.  Otherwise a negative
     * integer is returned.
     *
     * @param r  region
     * @see #compareTo(Object)
     */

    public int compareTo(Region r)
    {
        if ((this.getRowFrom() == r.getRowFrom())
                && (this.getColumnFrom() == r.getColumnFrom())
                && (this.getRowTo() == r.getRowTo())
                && (this.getColumnTo() == r.getColumnTo()))
        {
            return 0;
        }
        if ((this.getRowFrom() < r.getRowFrom())
                || (this.getColumnFrom() < r.getColumnFrom())
                || (this.getRowTo() < r.getRowTo())
                || (this.getColumnTo() < r.getColumnTo()))
        {
            return 1;
        }
        return -1;
    }

    public int compareTo(Object o)
    {
        return compareTo(( Region ) o);
    }

    /**
     * @returns the area contained by this region (number of cells)
     */

    public int getArea()
    {
        return ((1 + (getRowTo() - getRowFrom()))
                * (1 + (getColumnTo() - getColumnFrom())));
    }
}
