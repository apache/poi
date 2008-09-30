
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
        

package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;

/**
 * Title:        Print Gridlines Record<P>
 * Description:  whether to print the gridlines when you enjoy you spreadsheet on paper.<P>
 * REFERENCE:  PG 373 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */

public class PrintGridlinesRecord
    extends Record
{
    public final static short sid = 0x2b;
    private short             field_1_print_gridlines;

    public PrintGridlinesRecord()
    {
    }

    /**
     * Constructs a PrintGridlines record and sets its fields appropriately.
     * @param in the RecordInputstream to read the record from
     */

    public PrintGridlinesRecord(RecordInputStream in)
    {
        super(in);
    }

    protected void fillFields(RecordInputStream in)
    {
        field_1_print_gridlines = in.readShort();
    }

    /**
     * set whether or not to print the gridlines (and make your spreadsheet ugly)
     *
     * @param pg  make spreadsheet ugly - Y/N
     */

    public void setPrintGridlines(boolean pg)
    {
        if (pg == true)
        {
            field_1_print_gridlines = 1;
        }
        else
        {
            field_1_print_gridlines = 0;
        }
    }

    /**
     * get whether or not to print the gridlines (and make your spreadsheet ugly)
     *
     * @return make spreadsheet ugly - Y/N
     */

    public boolean getPrintGridlines()
    {
        return (field_1_print_gridlines == 1);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[PRINTGRIDLINES]\n");
        buffer.append("    .printgridlines = ").append(getPrintGridlines())
            .append("\n");
        buffer.append("[/PRINTGRIDLINES]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) 0x2);
        LittleEndian.putShort(data, 4 + offset, field_1_print_gridlines);
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 6;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
      PrintGridlinesRecord rec = new PrintGridlinesRecord();
      rec.field_1_print_gridlines = field_1_print_gridlines;
      return rec;
    }
}
