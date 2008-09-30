
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
 * Title:        Iteration Record<P>
 * Description:  Tells whether to iterate over forumla calculations or not
 *               (if a formula is dependant upon another formula's result)
 *               (odd feature for something that can only have 32 elements in
 *                a formula!)<P>
 * REFERENCE:  PG 325 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */

public class IterationRecord
    extends Record
{
    public final static short sid = 0x11;
    private short             field_1_iteration;

    public IterationRecord()
    {
    }

    /**
     * Constructs an Iteration record and sets its fields appropriately.
     * @param in the RecordInputstream to read the record from
     */

    public IterationRecord(RecordInputStream in)
    {
        super(in);
    }

    protected void fillFields(RecordInputStream in)
    {
        field_1_iteration = in.readShort();
    }

    /**
     * set whether or not to iterate for calculations
     * @param iterate or not
     */

    public void setIteration(boolean iterate)
    {
        if (iterate)
        {
            field_1_iteration = 1;
        }
        else
        {
            field_1_iteration = 0;
        }
    }

    /**
     * get whether or not to iterate for calculations
     *
     * @return whether iterative calculations are turned off or on
     */

    public boolean getIteration()
    {
        return (field_1_iteration == 1);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[ITERATION]\n");
        buffer.append("    .iteration      = ").append(getIteration())
            .append("\n");
        buffer.append("[/ITERATION]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) 0x2);
        LittleEndian.putShort(data, 4 + offset, field_1_iteration);
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
      IterationRecord rec = new IterationRecord();
      rec.field_1_iteration = field_1_iteration;
      return rec;
    }
}
