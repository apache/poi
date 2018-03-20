
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
 * Title:        Save Recalc Record <P>
 * Description:  defines whether to recalculate before saving (set to true)<P>
 * REFERENCE:  PG 381 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */
public final class SaveRecalcRecord
    extends StandardRecord
{
    public final static short sid = 0x5f;
    private short             field_1_recalc;

    public SaveRecalcRecord() {
    }

    public SaveRecalcRecord(RecordInputStream in)
    {
        field_1_recalc = in.readShort();
    }

    /**
     * set whether to recalculate formulas/etc before saving or not
     * @param recalc - whether to recalculate or not
     */
    public void setRecalc(boolean recalc) {
        field_1_recalc = ( short ) (recalc ? 1 : 0);
    }

    /**
     * get whether to recalculate formulas/etc before saving or not
     * @return recalc - whether to recalculate or not
     */
    public boolean getRecalc()
    {
        return (field_1_recalc == 1);
    }

    public String toString() {
        return "[SAVERECALC]\n" +
                "    .recalc         = " + getRecalc() +
                "\n" +
                "[/SAVERECALC]\n";
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_recalc);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
      SaveRecalcRecord rec = new SaveRecalcRecord();
      rec.field_1_recalc = field_1_recalc;
      return rec;
    }
}
