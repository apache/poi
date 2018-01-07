
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
 * Title:        Country Record (aka WIN.INI country)<P>
 * Description:  used for localization.  Currently HSSF always sets this to 1
 * and it seems to work fine even in Germany.  (es geht's auch fuer Deutschland)<P>
 *
 * REFERENCE:  PG 298 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public final class CountryRecord
    extends StandardRecord
{
    public final static short sid = 0x8c;

    // 1 for US
    private short             field_1_default_country;
    private short             field_2_current_country;

    public CountryRecord()
    {
    }

    public CountryRecord(RecordInputStream in)
    {
        field_1_default_country = in.readShort();
        field_2_current_country = in.readShort();
    }

    /**
     * sets the default country
     *
     * @param country ID to set (1 = US)
     */

    public void setDefaultCountry(short country)
    {
        field_1_default_country = country;
    }

    /**
     * sets the current country
     *
     * @param country ID to set (1 = US)
     */

    public void setCurrentCountry(short country)
    {
        field_2_current_country = country;
    }

    /**
     * gets the default country
     *
     * @return country ID (1 = US)
     */

    public short getDefaultCountry()
    {
        return field_1_default_country;
    }

    /**
     * gets the current country
     *
     * @return country ID (1 = US)
     */

    public short getCurrentCountry()
    {
        return field_2_current_country;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[COUNTRY]\n");
        buffer.append("    .defaultcountry  = ")
            .append(Integer.toHexString(getDefaultCountry())).append("\n");
        buffer.append("    .currentcountry  = ")
            .append(Integer.toHexString(getCurrentCountry())).append("\n");
        buffer.append("[/COUNTRY]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getDefaultCountry());
        out.writeShort(getCurrentCountry());
    }

    protected int getDataSize() {
        return 4;
    }

    public short getSid()
    {
        return sid;
    }
}
