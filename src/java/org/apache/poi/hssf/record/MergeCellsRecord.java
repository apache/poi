
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
        

package org.apache.poi.hssf.record;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.util.LittleEndian;

/**
 * Title: Merged Cells Record
 * <br>
 * Description:  Optional record defining a square area of cells to "merged" into
 *               one cell. <br>
 * REFERENCE:  NONE (UNDOCUMENTED PRESENTLY) <br>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */
public class MergeCellsRecord
    extends Record
{
    public final static short sid = 0xe5;
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
        short numAreas    = LittleEndian.getShort(data, 0 + offset);
        field_2_regions   = new ArrayList(numAreas + 10);
        int pos = 2;

        for (int k = 0; k < numAreas; k++)
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
        //if the array size is larger than a short (65536), the record can't hold that many merges anyway
        if (field_2_regions == null) return 0;
        return (short)field_2_regions.size();
    }

    /**
     * set the number of merged areas.  You do not need to call this if you use addArea,
     * it will be incremented automatically or decremented when an area is removed.  If
     * you are setting this to 0 then you are a terrible person.  Just remove the record.
     * (just kidding about you being a terrible person..hehe)
     * @deprecated We now link the size to the actual array of merged regions
     * @see #getNumAreas()
     * @param numareas  number of areas
     */

    public void setNumAreas(short numareas)
    {
        
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

    //public int addArea(short rowfrom, short colfrom, short rowto, short colto)
    public int addArea(int rowfrom, short colfrom, int rowto, short colto)
    {
        if (field_2_regions == null)
        {
            field_2_regions = new ArrayList(10);
        }
        MergedRegion region = new MergedRegion(rowfrom, rowto, colfrom,
                                               colto);

        field_2_regions.add(region);
        return field_2_regions.size() - 1;
    }

    /**
     * essentially unmerge the cells in the "area" stored at the passed in index
     * @param area index
     */

    public void removeAreaAt(int area)
    {
        field_2_regions.remove(area);
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

            //LittleEndian.putShort(data, offset + pos, region.row_from);
            LittleEndian.putShort(data, offset + pos, ( short ) region.row_from);
            pos += 2;
            //LittleEndian.putShort(data, offset + pos, region.row_to);
            LittleEndian.putShort(data, offset + pos, ( short ) region.row_to);
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
        retval.append("     .numregions =").append(getNumAreas())
            .append("\n");
        for (int k = 0; k < getNumAreas(); k++)
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

        //public MergedRegion(short row_from, short row_to, short col_from,
        public MergedRegion(int row_from, int row_to, short col_from,
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

        //public short row_from;
        public int row_from;

        /**
         * lower right hand corner row
         */

        //public short row_to;
        public int row_to;

        /**
         * upper right hand corner col
         */

        public short col_from;

        /**
         * lower right hand corner col
         */

        public short col_to;
    }

    public Object clone() {
        MergeCellsRecord rec = new MergeCellsRecord();        
        rec.field_2_regions = new ArrayList();
        Iterator iterator = field_2_regions.iterator();
        while (iterator.hasNext()) {
           MergedRegion oldRegion = (MergedRegion)iterator.next();
           rec.addArea(oldRegion.row_from, oldRegion.col_from, oldRegion.row_to, oldRegion.col_to);
        }
        
        return rec;
    }
}
