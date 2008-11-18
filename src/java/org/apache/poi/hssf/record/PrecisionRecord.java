
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

import org.apache.poi.util.LittleEndianOutput;

/**
 * Title:        Precision Record<P>
 * Description:  defines whether to store with full precision or what's displayed by the gui
 *               (meaning have really screwed up and skewed figures or only think you do!)<P>
 * REFERENCE:  PG 372 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public final class PrecisionRecord
    extends StandardRecord
{
    public final static short sid = 0xE;
    public short              field_1_precision;

    public PrecisionRecord()
    {
    }

    public PrecisionRecord(RecordInputStream in)
    {
        field_1_precision = in.readShort();
    }

    /**
     * set whether to use full precision or just skew all you figures all to hell.
     *
     * @param fullprecision - or not
     */

    public void setFullPrecision(boolean fullprecision)
    {
        if (fullprecision == true)
        {
            field_1_precision = 1;
        }
        else
        {
            field_1_precision = 0;
        }
    }

    /**
     * get whether to use full precision or just skew all you figures all to hell.
     *
     * @return fullprecision - or not
     */

    public boolean getFullPrecision()
    {
        return (field_1_precision == 1);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[PRECISION]\n");
        buffer.append("    .precision       = ").append(getFullPrecision())
            .append("\n");
        buffer.append("[/PRECISION]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_precision);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }
}
