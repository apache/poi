
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

package org.apache.poi.hssf.record;

import java.util.ArrayList;

import org.apache.poi.util.LittleEndian;

/**
 * Title: Merged Cells Record<P>
 * Description:  Optional record defining a square area of cells to "merged" into
 *               one cell. <P>
 * REFERENCE:  NONE (UNDOCUMENTED PRESENTLY) <P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class MergeCellsRecord
    extends Record
{
    public final static short sid = 0xe5;
    private short             field_1_num_areas;
    private ArrayList         field_2_regions;

    public MergeCellsRecord()
    {
    }

    /**
     * Constructs a MergedCellsRecord and sets its fields appropriately
     *
     * @param sid     id must be 0xe5 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public MergeCellsRecord(short sid, short size, byte [] data)
    {
        super(sid, size, data);
    }

    /**
     * Constructs a MergedCellsRecord and sets its fields appropriately
     *
     * @param sid     id must be 0xe5 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset the offset of the record's data
     */

    public MergeCellsRecord(short sid, short size, byte [] data, int offset)
    {
        super(sid, size, data, offset);
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_num_areas = LittleEndian.getShort(data, 0 + offset);
        field_2_regions   = new ArrayList(field_1_num_areas + 10);
        int pos = 2;

        for (int k = 0; k < field_1_num_areas; k++)
        {
            MergedRegion region =
                new MergedRegion(LittleEndian
                    .getShort(data, pos + offset), LittleEndian
                    .getShort(data, pos + 2 + offset), LittleEndian
                    .getShort(data, pos + 4 + offset), LittleEndian
                    .getShort(data, pos + 6 + offset));

            pos += 8;
            field_2_regions.add(region);
        }
    }

    /**
     * get the number of merged areas.  If this drops down to 0 you should just go
     * ahead and delete the record.
     * @return number of areas
     */

    public short getNumAreas()
    {
        return field_1_num_areas;
    }

    /**
     * set the number of merged areas.  You do not need to call this if you use addArea,
     * it will be incremented automatically or decremented when an area is removed.  If
     * you are setting this to 0 then you are a terrible person.  Just remove the record.
     * (just kidding about you being a terrible person..hehe)
     *
     * @param numareas  number of areas
     */

    public void setNumAreas(short numareas)
    {
        field_1_num_areas = numareas;
    }

    /**
     * Add an area to consider a merged cell.  The index returned is only gauranteed to
     * be correct provided you do not add ahead of or remove ahead of it  (in which case
     * you should increment or decrement appropriately....in other words its an arrayList)
     *
     * @param rowfrom - the upper left hand corner's row
     * @param colfrom - the upper left hand corner's col
     * @param rowto - the lower right hand corner's row
     * @param colto - the lower right hand corner's col
     * @return new index of said area (don't depend on it if you add/remove)
     */

    public int addArea(short rowfrom, short colfrom, short rowto, short colto)
    {
        if (field_2_regions == null)
        {
            field_2_regions = new ArrayList(10);
        }
        MergedRegion region = new MergedRegion(rowfrom, rowto, colfrom,
                                               colto);

        field_2_regions.add(region);
        field_1_num_areas++;
        return field_2_regions.size() - 1;
    }

    /**
     * essentially unmerge the cells in the "area" stored at the passed in index
     * @param area index
     */

    public void removeAreaAt(int area)
    {
        field_2_regions.remove(area);
        field_1_num_areas--;
    }

    /**
     * return the MergedRegion at the given index.
     *
     * @return MergedRegion representing the area that is Merged (r1,c1 - r2,c2)
     */

    public MergedRegion getAreaAt(int index)
    {
        return ( MergedRegion ) field_2_regions.get(index);
    }

    public int getRecordSize()
    {
        int retValue;

        retValue = 6 + (8 * field_2_regions.size());
        return retValue;
    }

    public short getSid()
    {
        return sid;
    }

    public int serialize(int offset, byte [] data)
    {
        int recordsize = getRecordSize();
        int pos        = 6;

        LittleEndian.putShort(data, offset + 0, sid);
        LittleEndian.putShort(data, offset + 2, ( short ) (recordsize - 4));
        LittleEndian.putShort(data, offset + 4, getNumAreas());
        for (int k = 0; k < getNumAreas(); k++)
        {
            MergedRegion region = getAreaAt(k);

            LittleEndian.putShort(data, offset + pos, region.row_from);
            pos += 2;
            LittleEndian.putShort(data, offset + pos, region.row_to);
            pos += 2;
            LittleEndian.putShort(data, offset + pos, region.col_from);
            pos += 2;
            LittleEndian.putShort(data, offset + pos, region.col_to);
            pos += 2;
        }
        return recordsize;
    }

    public String toString()
    {
        StringBuffer retval = new StringBuffer();

        retval.append("[MERGEDCELLS]").append("\n");
        retval.append("     .sid        =").append(sid).append("\n");
        retval.append("     .numregions =").append(field_1_num_areas)
            .append("\n");
        for (int k = 0; k < field_1_num_areas; k++)
        {
            MergedRegion region = ( MergedRegion ) field_2_regions.get(k);

            retval.append("     .rowfrom    =").append(region.row_from)
                .append("\n");
            retval.append("     .colfrom    =").append(region.col_from)
                .append("\n");
            retval.append("     .rowto      =").append(region.row_to)
                .append("\n");
            retval.append("     .colto      =").append(region.col_to)
                .append("\n");
        }
        retval.append("[MERGEDCELLS]").append("\n");
        return retval.toString();
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A MERGEDCELLS RECORD!! "
                                            + id);
        }
    }

    /**
     * this is a low level representation of a MergedRegion of cells.  It is an
     * inner class because we do not want it used without reference to this class.
     *
     */

    public class MergedRegion
    {

        /**
         * create a merged region all in one stroke.
         */

        public MergedRegion(short row_from, short row_to, short col_from,
                            short col_to)
        {
            this.row_from = row_from;
            this.row_to   = row_to;
            this.col_from = col_from;
            this.col_to   = col_to;
        }

        /**
         * upper lefthand corner row
         */

        public short row_from;

        /**
         * lower right hand corner row
         */

        public short row_to;

        /**
         * upper right hand corner col
         */

        public short col_from;

        /**
         * lower right hand corner col
         */

        public short col_to;
    }
}
